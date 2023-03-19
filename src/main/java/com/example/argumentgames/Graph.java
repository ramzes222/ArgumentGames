package com.example.argumentgames;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Graph {
    private final double moveAwayStep = 10;
    private double dragOriginY = 0, dragOriginX = 0, gCircleRadius = 45;
    private final Button addGCircleButton, addGArrowButton, cleanupButton;
    private final RadioButton setSelectModeButton, setMoveModeButton, setPanModeButton;
    private final ArrayList<GraphCircle> gCircles = new ArrayList<>();
    private final ArrayList<GraphArrow> gArrows = new ArrayList<>();
    private GraphNode selected = null;
    private final Pane graphPane;
    private final ToggleGroup tg = new ToggleGroup();
    private EventHandler<MouseEvent> graphWideEvent;

    private Framework currFramework;
    enum InteractMode {
        SELECT_MODE,
        MOVE_MODE,
        PAN_MODE,
        SPECIAL_MODE
    }
    private InteractMode interactMode = InteractMode.SELECT_MODE;

    public Graph(Pane graphPane, RadioButton setSelectModeButton, RadioButton setMoveModeButton, RadioButton setPanModeButton, Button addGCircleButton, Button addGArrowButton, Button cleanupButton) {
        // Save variables
        this.setSelectModeButton = setSelectModeButton;
        this.setMoveModeButton = setMoveModeButton;
        this.setPanModeButton = setPanModeButton;
        this.graphPane = graphPane;
        this.addGCircleButton = addGCircleButton;
        this.addGArrowButton = addGArrowButton;
        this.cleanupButton = cleanupButton;
        //
        // Setup interact mode buttons
        setUpInteractModeButton(setMoveModeButton, InteractMode.MOVE_MODE);
        setUpInteractModeButton(setSelectModeButton, InteractMode.SELECT_MODE);
        setUpInteractModeButton(setPanModeButton, InteractMode.PAN_MODE);
        this.setSelectModeButton.fire();
        //
        // Setup control buttons
        this.addGCircleButton.setOnAction(e-> { beginManualAddGCircle(); });
        this.addGArrowButton.setOnAction(e-> { beginManualAddGArrow(); });
        this.cleanupButton.setOnAction(e-> { cleanNodes(); });
        setUpClip();
    }

    private void setUpClip() {
        final Rectangle outputClip = new Rectangle();
        outputClip.setArcWidth(5);
        outputClip.setArcHeight(5);
        outputClip.setWidth(graphPane.getWidth());
        outputClip.setHeight(graphPane.getHeight());
        graphPane.setClip(outputClip);
        graphPane.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            outputClip.setWidth(newValue.getWidth());
            outputClip.setHeight(newValue.getHeight());
        });
    }
    public void loadFramework(Framework framework) {
        graphPane.getChildren().clear();
        gCircles.clear();
        gArrows.clear();
        currFramework = framework;
        ArrayList<FrameworkArgument> argList = framework.getArguments();
        // Add visual representations of Arguments
        for (FrameworkArgument arg : argList) {
            GraphCircle c = addGCircle(arg.getName());
            c.setXY(arg.prefX, arg.prefY);
        }
        // Add visual attacks between them
        for (FrameworkAttack att : framework.getAttacks()) {
            GraphArrow a = addGArrow( getGCircle(att.getFrom().getName()), getGCircle(att.getTo().getName()) );
            if (a!=null) a.setControlPointXY(att.prefControlX, att.prefControlY);
        }
    }
    private void setUpInteractModeButton(RadioButton b, InteractMode i) {
        b.getStyleClass().remove("radio-button");
        b.setToggleGroup(this.tg);
        b.setOnAction(e -> {
            setInteractMode(i);
        });
    }

    private void setInteractMode(InteractMode m) {
        switch (m) {
            case SELECT_MODE -> {
                graphPane.setOnMousePressed(null); graphPane.setOnMouseDragged(null);
                interactMode = m;
                setDisableButtons(false);
                // Implement selection
                for (GraphCircle n : gCircles) {
                    n.setMouseTransparent(false);
                    n.setOnMouseDragged(null);
                    n.setOnMousePressed(null);
                    n.setOnMouseClicked(e -> {
                        if (selected == n) {
                            selected.deselect();
                            selected = null;
                        } else {
                            if (selected != null) selected.deselect();
                            selected = n;
                            selected.select();
                        }
                    });
                }
                for (GraphArrow a : gArrows) {
                    a.setMouseTransparent(false);
                    a.setOnMouseClicked(e -> {
                        if (selected == a) {
                            selected.deselect();
                            selected = null;
                        } else {
                            if (selected != null) selected.deselect();
                            selected = a;
                            selected.select();
                        }
                    });
                }
            }
            //
            //
            case MOVE_MODE -> {
                graphPane.setOnMousePressed(null); graphPane.setOnMouseDragged(null);
                interactMode = m;
                if (this.addGArrowButton != null) {
                    this.addGArrowButton.setDisable(true);
                }
                if (selected != null) {
                    selected.deselect();
                    selected = null;
                }
                // Turn off other events, then make draggable
                for (GraphCircle n : gCircles) {
                    n.setMouseTransparent(false);
                    n.setOnMouseClicked(null);
                    makeDraggable(n);
                }
                // For arrows, keep behaviour from selecting them
                for (GraphArrow a : gArrows) {
                    a.setMouseTransparent(false);
                    a.setOnMouseClicked(e -> {
                        if (selected == a) {
                            selected.deselect();
                            selected = null;
                        } else {
                            if (selected != null) selected.deselect();
                            selected = a;
                            selected.select();
                        }
                    });
                }
            }
            //
            //
            case PAN_MODE -> {
                // Make nodes and arrows transparent - only clicks on the Pane matter
                interactMode = m;
                if (this.addGArrowButton != null) {
                    this.addGArrowButton.setDisable(true);
                }
                if (selected != null) {
                    selected.deselect();
                    selected = null;
                }
                for (GraphCircle c: gCircles) { c.setMouseTransparent(true); }
                for (GraphArrow a: gArrows) { a.setMouseTransparent(true); }
                graphPane.setOnMousePressed(e -> {
                    if (interactMode == InteractMode.PAN_MODE) {
                        dragOriginX = e.getSceneX();
                        dragOriginY = e.getSceneY();
                    }
                });
                graphPane.setOnMouseDragged(e -> {
                    if (interactMode == InteractMode.PAN_MODE) {
                        double xDrag = e.getSceneX() - dragOriginX;
                        double yDrag = e.getSceneY() - dragOriginY;
                        dragOriginX = e.getSceneX();
                        dragOriginY = e.getSceneY();
                        for (GraphArrow a: gArrows) { a.translateControlPointXY(xDrag, yDrag); }
                        for (GraphCircle c: gCircles) { c.translateXY(xDrag, yDrag); }
                    }
                });
            }
            //
            //
            case SPECIAL_MODE -> {
                graphPane.setOnMousePressed(null); graphPane.setOnMouseDragged(null);
                interactMode = m;
                if (selected != null) {
                    selected.deselect();
                    selected = null;
                }
                setDisableButtons(true);
                // Disable all GCircle/edge click events
                for (GraphCircle n : gCircles) {
                    n.setMouseTransparent(false);
                    n.setOnMouseClicked(null);
                    n.setOnMouseDragged(null);
                    n.setOnMousePressed(null);
                }
                for (GraphArrow a : gArrows) {
                    a.setMouseTransparent(false);
                    a.setOnMouseClicked(null);
                    a.setOnMouseDragged(null);
                    a.setOnMousePressed(null);
                }
            }
        }
    }

    // Sets all control buttons to disabled (or enabled, as the parameter)
    private void setDisableButtons(boolean b) {
        setSelectModeButton.setDisable(b);
        setMoveModeButton.setDisable(b);
        setPanModeButton.setDisable(b);
        addGArrowButton.setDisable(b);
        addGCircleButton.setDisable(b);
        cleanupButton.setDisable(b);
    }

    // Ran when clicking AddCircle Button
    // Begins the process of manually adding a new Argument to the graph (represented as gCircle)
    public void beginManualAddGCircle() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add new node");
        dialog.setContentText("Please enter the name of the new node:");
        Optional<String> newName = dialog.showAndWait();
        newName.ifPresent(name -> {
            // Check if the name only contains letters, numbers, or spaces
            Pattern acceptedSymbols = Pattern.compile("[^a-zA-Z0-9 ]");
            Matcher m = acceptedSymbols.matcher(name);
            if (m.find()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unallowed symbol in name");
                alert.setContentText("You entered '" + name + "'. Only letters, numbers, and spaces can be used.");
                alert.showAndWait();
                return;
            }
            // Check if the name is unique in the framework
            if (currFramework.nameExists(name)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Node already exists");
                alert.setContentText("There already exists a node with name " + name + "!");
                alert.showAndWait();
                return;
            }
            addGCircle(name);
            // Save the new argument to the framework
            currFramework.addArgument(name);
        });
    }

    // Adds a new gCircle to the graph under the specified name
    // Only adds the visual representation of an argument - does not affect the framework
    private GraphCircle addGCircle(String name) {
        GraphCircle n = new GraphCircle(name, gCircleRadius);
        n.setLayoutX(250);
        n.setLayoutY(300);
        // Implement selection
        if (interactMode == InteractMode.SELECT_MODE) {
            n.setOnMouseClicked(e -> {
                if (selected == n) {
                    selected.deselect();
                    selected = null;
                } else {
                    if (selected != null) selected.deselect();
                    selected = n;
                    selected.select();
                }
            });}
        graphPane.getChildren().add(n);
        gCircles.add(n);
        return n;
    }

    public GraphCircle getGCircle(String name) {
        for (GraphCircle n: gCircles) {
            if (n.getName().equals(name)) return n;
        }
        return null;
    }

    // Move the provided GraphCircle n away from nearby other nodes on the graph
    // Will attempt to move it into clear space, so it's not covered by others
    // Returns whether a move has been made

    public void moveNode(GraphCircle circle) {
        boolean wasMoved = true;
        int counter = 100;
        if (circle != null) {
            while (wasMoved && counter > 0) { wasMoved = moveGCircleIntoEmptySpace( circle ); counter--;}
        }
    }

    // Moves the nodes away from each other to clean up the graph
    // Executes moveGCircleIntoEmptySpace on all nodes until no collision
    // or until reaching max iteration
    private void cleanNodes() {
        int maxIterations = 1000;
        while (maxIterations > 0) {
            maxIterations--;
            for (GraphCircle c: gCircles) { moveGCircleIntoEmptySpace(c); }
        }

    }

    // Move the target node away from the closest other node
    // Does not take Borders into account (as we can pan around the graph anyway)
    private boolean moveGCircleIntoEmptySpace(GraphCircle n) {
        double x = n.getLayoutX(), y = n.getLayoutY(), targetX=0, targetY=0, distanceToTarget;
        //
        // Get the closest gCircle
        GraphCircle closestGCircle = findClosestGCircle(n);
        if (closestGCircle == null) return false;
        //
        distanceToTarget = GeometricHelper.gCircle_gCircle_toDistance(n, closestGCircle);
        if (distanceToTarget == 0) {
            // SPECIAL CASE
            // If the node is directly on top of another, move it slightly down and finish.
            // Next iteration will fix it
            n.getCenterYProperty().set(y + 5);
            n.setLayoutY(y + 5);
            return true;
        }
        targetX = closestGCircle.getLayoutX();
        targetY = closestGCircle.getLayoutY();
        //
        // Now we definitely have the target X,Y to move away from
        // If we're at least acceptable distance away, does nothing
        // Otherwise, moves a few steps directly away
        // Acceptable distance: 2*GCircle radius + moveAwayStep
        if (distanceToTarget < 2*gCircleRadius + moveAwayStep) {
            Random r = new Random();
            // To prevent loops, add a little noise
            double angle = GeometricHelper.x_y_toAngle( x - targetX, targetY - y) + r.nextDouble(-10, 10);
            double newX = x + GeometricHelper.angle_distance_toX( -angle, moveAwayStep );
            double newY = y + GeometricHelper.angle_distance_toY( -angle, moveAwayStep );
            n.getCenterXProperty().set(newX);
            n.setLayoutX(newX);
            n.getCenterYProperty().set(newY);
            n.setLayoutY(newY);
            return true;
        } else return false;
    }

    private GraphCircle findClosestGCircle(GraphCircle origin) {
        double closestDistance = 0;
        GraphCircle closestGCircle = null;
        for (GraphCircle c: gCircles ) {
            if (c != origin) {
                double distance = GeometricHelper.gCircle_gCircle_toDistance(origin, c);
                if (closestGCircle == null || distance < closestDistance) {
                    closestGCircle = c;
                    closestDistance = distance;
                }
            }
        }
        return closestGCircle;
    }

    public void beginManualAddGArrow() {
        // Check which Node (GCircle) is currently selected
        // It will be the origin of the edge
        if (selected != null && selected.getClass() == GraphCircle.class) {
            // Remember origin node and style it
            GraphCircle fromGCircle = (GraphCircle) selected;
            fromGCircle.highlight(Color.LAWNGREEN);
            selected = null;
            //
            // Add arrow for visuals
            MouseArrow newGArrowMouseArrow = new MouseArrow(fromGCircle.getLayoutX(), fromGCircle.getLayoutY(), graphPane);
            setInteractMode(InteractMode.SPECIAL_MODE);
            //
            // Add cancel even when right-clicking
            graphWideEvent = e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    endAddGArrowEvent(newGArrowMouseArrow);
                    fromGCircle.dehighlight();
                    graphPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, graphWideEvent);}
            };
            graphPane.addEventFilter(MouseEvent.MOUSE_CLICKED, graphWideEvent);
            //
            // Add event to all nodes
            for (GraphCircle toGCircle: gCircles) { toGCircle.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (!currFramework.attackExists(fromGCircle.getName(), toGCircle.getName())) {
                        addGArrow(fromGCircle, toGCircle);
                        currFramework.addAttack(fromGCircle.getName(), toGCircle.getName());
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Attack already exists");
                        alert.setContentText("There already exists an attack between the nodes " + fromGCircle.getName() + " and " + toGCircle.getName() + "!");
                        alert.showAndWait();
                    }
                    endAddGArrowEvent(newGArrowMouseArrow);
                    fromGCircle.dehighlight();
                    graphPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, graphWideEvent);
                }
            }); }
        } else {
            // This should be prevented by other functions, but
            // just in case show a warning
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No node selected");
            alert.setContentText("To add an arrow you must select a node first!");
            alert.showAndWait();
        }
    }

    private void endAddGArrowEvent(MouseArrow mArrow) {
        // Perform cleanup after manual Add GArrow has been completed or cancelled
        setInteractMode(InteractMode.SELECT_MODE);
        mArrow.delete();
        mArrow = null;
        graphPane.setOnMouseMoved(null);
    }

    private GraphArrow addGArrow(GraphCircle a, GraphCircle b) {
        if (a == b) {
            // An argument cannot attack itself
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Selected the same node twice!");
            alert.setContentText("To add an attack you must select two different arguments!");
            alert.showAndWait();
            return null;
        } else {
            GraphArrow arrow = new GraphArrow(a, b);
            graphPane.getChildren().addAll(arrow, arrow.getArrowTip(), arrow.getControlPoint());
            gArrows.add(arrow);
            arrow.setOnMouseClicked(e -> {
                // OLD CONDITION: interactMode == InteractMode.SELECT_MODE
                if (selected == arrow) {
                    selected.deselect();
                    selected = null;
                } else {
                    if (selected != null) selected.deselect();
                    selected = arrow;
                    selected.select();
                }
            });
            // Save arrow reference in the connected nodes
            a.addArrow(arrow);
            b.addArrow(arrow);
            a.toFront();
            b.toFront();
            return arrow;
        }
    }

    private void makeDraggable(GraphCircle node) {
        node.setOnMousePressed(e -> {
            if (interactMode == InteractMode.MOVE_MODE) {
                node.toFront();
                dragOriginX = e.getSceneX() - node.getLayoutX();
                dragOriginY = e.getSceneY() - node.getLayoutY();
            }
        });
        node.setOnMouseDragged(e -> {
            if (interactMode == InteractMode.MOVE_MODE) {
                double newX = e.getSceneX() - dragOriginX;
                node.getCenterXProperty().set(newX);
                node.setLayoutX(newX);
                double newY = e.getSceneY() - dragOriginY;
                node.getCenterYProperty().set(newY);
                node.setLayoutY(newY);
                node.rotateArrows();
            }
        });
    }

    public String getSelectedArgumentName() {
        if (selected!=null) return selected.getName();
        return null;
    }
}
