package com.example.argumentgames;
import java.util.*;

import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Graph {
    private final double moveAwayStep = 10;
    private double dragOriginY = 0, dragOriginX = 0, gCircleRadius = 45;
    private Button addGCircleButton = null, addGArrowButton = null;
    private final RadioButton setSelectModeButton, setMoveModeButton;
    private final ArrayList<GraphCircle> gCircles = new ArrayList<>();
    private final ArrayList<GraphArrow> gArrows = new ArrayList<>();
    private GraphNode selected = null;
    private final Pane graphPane;
    private final ToggleGroup tg = new ToggleGroup();
    private EventHandler<MouseEvent> graphWideEvent;
    enum InteractMode {
        SELECT_MODE,
        MOVE_MODE,
        //PAN_MODE,
        SPECIAL_MODE
    }
    private InteractMode interactMode = InteractMode.SELECT_MODE;

    public Graph(Pane graphPane, RadioButton setSelectModeButton, RadioButton setMoveModeButton) {
        // Save variables
        this.graphPane = graphPane;
        this.setSelectModeButton = setSelectModeButton;
        this.setMoveModeButton = setMoveModeButton;
        //
        // Setup interact mode buttons
        setUpInteractModeButton(setMoveModeButton, InteractMode.MOVE_MODE);
        setUpInteractModeButton(setSelectModeButton, InteractMode.SELECT_MODE);
        this.setSelectModeButton.fire();
    }
    public Graph(Pane graphPane, RadioButton setSelectModeButton, RadioButton setMoveModeButton, Button addGCircleButton, Button addGArrowButton) {
        // Save variables
        this.setSelectModeButton = setSelectModeButton;
        this.setMoveModeButton = setMoveModeButton;
        this.graphPane = graphPane;
        this.addGCircleButton = addGCircleButton;
        this.addGArrowButton = addGArrowButton;
        //
        // Setup interact mode buttons
        setUpInteractModeButton(setMoveModeButton, InteractMode.MOVE_MODE);
        setUpInteractModeButton(setSelectModeButton, InteractMode.SELECT_MODE);
        this.setSelectModeButton.fire();
        //
        // Setup control buttons
        this.addGCircleButton.setOnAction(e-> { addGCircle(); });
        this.addGArrowButton.setOnAction(e-> { beginAddGArrow(); });
    }

    private void setUpInteractModeButton(RadioButton b, InteractMode i) {
        b.getStyleClass().remove("radio-button");
        b.setToggleGroup(this.tg);
        b.setOnAction(e -> {
            setInteractMode(i);
        });
    }

    public void setInteractMode(InteractMode m) {
        switch (m) {
            //
            //
            case SELECT_MODE -> {
                interactMode = m;
                setDisableButtons(false);
                // Implement selection
                for (GraphCircle n : gCircles) {
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
                    n.setOnMouseClicked(null);
                    makeDraggable(n);
                }
            }
            //
            //
            case SPECIAL_MODE -> {
                interactMode = m;
                if (selected != null) {
                    selected.deselect();
                    selected = null;
                }
                setDisableButtons(true);
                // Disable all GCircle/edge click events
                for (GraphCircle n : gCircles) {
                    n.setOnMouseClicked(null);
                    n.setOnMouseDragged(null);
                    n.setOnMousePressed(null);
                }
                for (GraphArrow a : gArrows) {
                    a.setOnMouseClicked(null);
                    a.setOnMouseDragged(null);
                    a.setOnMousePressed(null);
                }
            }
        }
    }

    private void setDisableButtons(boolean b) {
        setSelectModeButton.setDisable(b);
        setMoveModeButton.setDisable(b);
        if (addGArrowButton != null) addGArrowButton.setDisable(b);
        if (addGCircleButton != null) addGCircleButton.setDisable(b);
    }

    //private double heightInBounds(double y) { return Math.min( Math.max(0, y), this.height); }
    //private double widthInBounds(double x) { return Math.min( Math.max(0, x), this.width); }

    public void addGCircle() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add new node");
        dialog.setContentText("Please enter the name of the new node:");
        Optional<String> newName = dialog.showAndWait();
        newName.ifPresent(name -> {
            // Check if the name is unique among existing GCircles
            boolean unique = true;
            for (GraphCircle c: gCircles) { if (c.getName().equals(name)) {unique = false; break;}}
            if (unique) {
                GraphCircle n = new GraphCircle(name, 45);
                n.setLayoutX(50);
                n.setLayoutY(50);
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
                //
                // Move the new node to an empty space
                moveNode(n);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Node already exists");
                alert.setContentText("There already exists a node with name " + name + "!");
                alert.showAndWait();
            }
        });
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
    private boolean moveGCircleIntoEmptySpace(GraphCircle n) {
        double x = n.getLayoutX(), y = n.getLayoutY(), targetX=0, targetY=0, distanceToTarget, width = graphPane.getWidth(), height = graphPane.getHeight();
        //
        // Check if the graph border is close
        // If it is, always move away from border first - then care about nodes
        // Left Border
        if (x < gCircleRadius) {
            n.getCenterXProperty().set(gCircleRadius); n.setLayoutX(gCircleRadius); return true; }
        // Right Border
        if (width - x < gCircleRadius) {
            n.getCenterXProperty().set(width - gCircleRadius); n.setLayoutX(width - gCircleRadius); return true; }
        // Top Border
        if (y < gCircleRadius) {
            n.getCenterYProperty().set(gCircleRadius); n.setLayoutY(gCircleRadius); return true; }
        // Bottom Border
        if (height - y < gCircleRadius) {
            n.getCenterYProperty().set(height - gCircleRadius); n.setLayoutY(height - gCircleRadius); return true; }
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
        // If we're at least acceptable distance away, does nothin
        // Otherwise, moves a few steps directly away
        // Acceptable distance: 2*GCircle radius + moveAwayStep
        if (distanceToTarget < 2*gCircleRadius + moveAwayStep) {
            Random r = new Random();
            // To prevent loops, add a little noise
            double angle = GeometricHelper.x_y_toAngle( x - targetX, targetY - y) + r.nextDouble(-5, 5);
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

    public void beginAddGArrow() {
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
            for (GraphCircle toGCircle: gCircles) { toGCircle.setOnMouseClicked(e -> { if (e.getButton() == MouseButton.PRIMARY) { addGArrow(fromGCircle, toGCircle); endAddGArrowEvent(newGArrowMouseArrow); fromGCircle.dehighlight();
                graphPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, graphWideEvent); }}); }

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

    private void addGArrow(GraphCircle a, GraphCircle b) {
        if (a == b) {
            // An argument cannot attack itself
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Selected the same node twice!");
            alert.setContentText("To add an attack you must select two different arguments!");
            alert.showAndWait();
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
}
