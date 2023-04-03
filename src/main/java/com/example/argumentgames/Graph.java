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
    private double dragOriginY = 0;
    private double dragOriginX = 0;
    private final double gCircleRadius = 45;
    private final Button addGCircleButton, addGArrowButton, cleanupButton, deleteButton;
    private final RadioButton setSelectModeButton, setMoveModeButton, setPanModeButton;
    private final ArrayList<GraphCircle> gCircles = new ArrayList<>();
    private final ArrayList<GraphArrow> gArrows = new ArrayList<>();
    private final ArrayList<GraphMetaArrow> gMetaArrows = new ArrayList<>();
    private GraphNode selected = null;
    private final Pane graphPane;
    private final ToggleGroup tg = new ToggleGroup();
    private EventHandler<MouseEvent> graphWideEvent;
    private final HashMap<String, Color> colorLookup;
    private final HashMap<String, Boolean> booleanLookup;

    private Framework currFramework;
    enum InteractMode {
        SELECT_MODE,
        MOVE_MODE,
        PAN_MODE,
        SPECIAL_MODE
    }
    private InteractMode interactMode = InteractMode.SELECT_MODE;

    public Graph(Pane graphPane, RadioButton setSelectModeButton, RadioButton setMoveModeButton, RadioButton setPanModeButton, Button addGCircleButton, Button addGArrowButton, Button deleteButton, Button cleanupButton, HashMap<String, Color> colorLookup, HashMap<String, Boolean> booleanLookup) {
        // Save variables
        this.setSelectModeButton = setSelectModeButton;
        this.setMoveModeButton = setMoveModeButton;
        this.setPanModeButton = setPanModeButton;
        this.graphPane = graphPane;
        this.addGCircleButton = addGCircleButton;
        this.addGArrowButton = addGArrowButton;
        this.deleteButton = deleteButton;
        this.cleanupButton = cleanupButton;
        this.colorLookup = colorLookup;
        this.booleanLookup = booleanLookup;
        //
        // Setup interact mode buttons
        setUpInteractModeButton(setMoveModeButton, InteractMode.MOVE_MODE);
        setUpInteractModeButton(setSelectModeButton, InteractMode.SELECT_MODE);
        setUpInteractModeButton(setPanModeButton, InteractMode.PAN_MODE);
        this.setSelectModeButton.fire();
        //
        // Setup control buttons
        this.addGCircleButton.setOnAction(e-> beginManualAddGCircle());
        this.addGArrowButton.setOnAction(e-> beginManualAddGArrow());
        this.cleanupButton.setOnAction(e-> cleanUp());
        this.deleteButton.setOnAction(e-> deleteSelected());
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
        setSelectModeButton.fire();
        setInteractMode(InteractMode.SELECT_MODE);
    }
    private void setUpInteractModeButton(RadioButton b, InteractMode i) {
        b.getStyleClass().remove("radio-button");
        b.setToggleGroup(this.tg);
        b.setOnAction(e -> setInteractMode(i));
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
                this.addGArrowButton.setDisable(true);
                this.deleteButton.setDisable(true);
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
                this.addGArrowButton.setDisable(true);
                this.deleteButton.setDisable(true);
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
        deleteButton.setDisable(b);
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
            Pattern acceptedSymbols = Pattern.compile("[^a-zA-Z0-9()_<>\\- ]");
            Matcher m = acceptedSymbols.matcher(name);
            if (m.find()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unallowed symbol in name");
                alert.setContentText("You entered '" + name + "'. Only letters, numbers, spaces, and the following special characters: ()<>-_ can be used.");
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
            // Save the new argument to the framework
            currFramework.addArgument(name);
            addGCircle(name);
            setInteractMode(interactMode);
        });
    }

    // Adds a new gCircle to the graph under the specified name
    // Only adds the visual representation of an argument - does not affect the framework
    private GraphCircle addGCircle(String name) {
        GraphCircle n = new GraphCircle(name, gCircleRadius, colorLookup);
        n.setXY(200, 300);
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

    public GraphArrow getGArrow(String from, String to) {
        for (GraphArrow a: gArrows) {
            if (a.getFromName().equals(from) && a.getToName().equals(to)) return a;
        }
        return null;
    }

    // Moves the nodes away from each other to clean up the graph
    // Executes moveGCircleIntoEmptySpace on all nodes until no collision
    // or until reaching max iteration
    public void cleanUp() {
        int maxIterations = 1000;
        boolean wasChanged = true;
        while (maxIterations > 0 && wasChanged) {
            wasChanged = false;
            maxIterations--;
            for (GraphCircle c: gCircles) {
                if (moveGCircleIntoEmptySpace(c)) wasChanged = true;
            }
        }

    }

    // Move the target node away from the closest other node
    // Does not take Borders into account (as we can pan around the graph anyway)
    private boolean moveGCircleIntoEmptySpace(GraphCircle n) {
        double x = n.getLayoutX(), y = n.getLayoutY(), targetX, targetY, distanceToTarget;
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
            n.setXY(x, y+5);
            return true;
        }
        targetX = closestGCircle.getLayoutX();
        targetY = closestGCircle.getLayoutY();
        //
        // Now we definitely have the target X,Y to move away from
        // If we're at least acceptable distance away, does nothing
        // Otherwise, moves a few steps directly away
        // Acceptable distance: 2*GCircle radius + moveAwayStep
        double moveAwayStep = 10;
        if (distanceToTarget < 2*gCircleRadius + moveAwayStep) {
            Random r = new Random();
            // To prevent loops, add a little noise
            double angle = GeometricHelper.x_y_toAngle( x - targetX, targetY - y) + r.nextDouble(-10, 10);
            double newX = x + GeometricHelper.angle_distance_toX( -angle, moveAwayStep);
            double newY = y + GeometricHelper.angle_distance_toY( -angle, moveAwayStep);
            n.setXY(newX, newY);
            // Make the arrows straight
            n.getConnectedArrows().forEach(GraphArrow::centerControlPoint);
            return true;
        } else {
            // Make the arrows straight
            n.getConnectedArrows().forEach(GraphArrow::centerControlPoint);
            return false;
        }
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
        boolean metaAllowed = booleanLookup.get("allowMetaArguments");
        // Check which Node (GCircle) is currently selected
        // It will be the origin of the edge
        if (selected != null && selected.getClass() == GraphCircle.class) {
            // Remember origin node and style it
            GraphCircle fromGCircle = (GraphCircle) selected;
            fromGCircle.highlight(colorLookup.get("attackArrowColor"));
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
                    fromGCircle.highlight(Color.TRANSPARENT);
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
                        setInteractMode(interactMode);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Attack already exists");
                        alert.setContentText("There already exists an attack between the nodes " + fromGCircle.getName() + " and " + toGCircle.getName() + "!");
                        alert.showAndWait();
                    }
                    endAddGArrowEvent(newGArrowMouseArrow);
                    fromGCircle.highlight(Color.TRANSPARENT);
                    graphPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, graphWideEvent);
                }
            }); }
            // Add event to all arrows, if meta attacks are allowed
            if (!metaAllowed) return;
            for (GraphArrow toGArrow: gArrows) { toGArrow.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (!currFramework.metaAttackExists(fromGCircle.getName(), toGArrow.getName())) {
                        // Check if the result is still stratified
                        if (currFramework.isMetaAttackAllowed(fromGCircle.getName(), toGArrow.getName())) {
                            addGMetaArrow(fromGCircle, toGArrow);
                            currFramework.addMetaAttack(fromGCircle.getName(), toGArrow.getName());
                            setInteractMode(interactMode);
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Meta attack not allowed!");
                            alert.setContentText("This meta attack would make the framework non-stratified! \nNon-stratified frameworks are not allowed in the current version.");
                            alert.showAndWait();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Meta attack already exists");
                        alert.setContentText("There already exists an attack between the node " + fromGCircle.getName() + " and attack " + toGArrow.getName() + "!");
                        alert.showAndWait();
                    }
                    endAddGArrowEvent(newGArrowMouseArrow);
                    fromGCircle.highlight(Color.TRANSPARENT);
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
            GraphArrow arrow = new GraphArrow(a, b, colorLookup);
            graphPane.getChildren().addAll(arrow, arrow.getArrowTip(), arrow.getControlPoint(), arrow.getMidPoint());
            gArrows.add(arrow);
            // Save arrow reference in the connected nodes
            a.addArrow(arrow);
            b.addArrow(arrow);
            a.toFront();
            b.toFront();
            return arrow;
        }
    }

    private GraphMetaArrow addGMetaArrow(GraphCircle a, GraphArrow b) {
        GraphMetaArrow mArrow = new GraphMetaArrow(a, b, colorLookup);
        graphPane.getChildren().addAll(mArrow, mArrow.getArrowTip(), mArrow.getControlPoint());
        gMetaArrows.add(mArrow);
        mArrow.setOnMouseClicked(e -> {
            if (selected == mArrow) {
                selected.deselect();
                selected = null;
            } else {
                if (selected != null) selected.deselect();
                selected = mArrow;
                selected.select();
            }
        });
        a.toFront();
        // Save arrow reference in the connected nodes
        a.addMetaArrow(mArrow);
        b.addMetaArrow(mArrow);
        return mArrow;
    }

    private void deleteSelected() {
        if (selected == null) return;
        if (selected.getClass() == GraphCircle.class) {
            // Delete node
            GraphCircle nodeToDelete = (GraphCircle) selected;
            selected = null;
            // Delete connected arrows
            ArrayList<GraphArrow> arrowsToDelete = (ArrayList<GraphArrow>) nodeToDelete.getConnectedArrows().clone();
            for (GraphArrow a: arrowsToDelete) {
                a.delete();
                gArrows.remove(a);
                graphPane.getChildren().removeAll(a, a.getArrowTip(), a.getControlPoint(), a.getMidPoint());
                currFramework.removeAttack(a.getFromName(), a.getToName());
            }
            // Delete connected meta arrows
            ArrayList<GraphMetaArrow> metaArrowsToDelete = (ArrayList<GraphMetaArrow>) nodeToDelete.getConnectedMetaArrows().clone();
            for (GraphMetaArrow a: metaArrowsToDelete) {
                a.delete();
                gMetaArrows.remove(a);
                graphPane.getChildren().removeAll(a, a.getArrowTip(), a.getControlPoint());
                currFramework.removeMetaAttack(a.getFromName(), a.getToName());
            }
            gCircles.remove(nodeToDelete);
            graphPane.getChildren().removeAll(nodeToDelete);
            currFramework.removeArgument(nodeToDelete.getName());
        } else if (selected.getClass() == GraphArrow.class) {
            // Delete edge
            GraphArrow edgeToDelete = (GraphArrow) selected;
            selected = null;
            edgeToDelete.delete();
            gArrows.remove(edgeToDelete);
            graphPane.getChildren().removeAll(edgeToDelete, edgeToDelete.getArrowTip(), edgeToDelete.getControlPoint(), edgeToDelete.getMidPoint());
            currFramework.removeAttack(edgeToDelete.getFromName(), edgeToDelete.getToName());
        } else {
            // Delete meta arrow
            GraphMetaArrow metaEdgeToDelete = (GraphMetaArrow) selected;
            selected = null;
            metaEdgeToDelete.delete();
            gMetaArrows.remove(metaEdgeToDelete);
            graphPane.getChildren().removeAll(metaEdgeToDelete, metaEdgeToDelete.getArrowTip(), metaEdgeToDelete.getControlPoint());
            currFramework.removeMetaAttack(metaEdgeToDelete.getFromName(), metaEdgeToDelete.getToName());
        }
    }

    // Changes the appearance of all nodes and arrows to make them grayed out
    // Also makes them unclickable
    // Used in Games to signify which nodes are disabled
    public void disableAll() {
        for (GraphCircle c : gCircles) {
            c.makeGameUnselectable();
        }
        for (GraphArrow a : gArrows) {
            a.disable();
        }
    }

    public void visualEnableAll() {
        for (GraphCircle c : gCircles) {
            c.setVisual("base");
            c.enable();
            c.setMouseTransparent(false);
        }
        for (GraphArrow a : gArrows) {
            a.enable();
        }
    }

    // Disables the buttons that could change the framework
    // Returns the Select mode button - the reference to it is used by the Game Controller
    public void enterGameMode(GameController game) {
        setMoveModeButton.fire();
        addGCircleButton.setDisable(true);
        addGArrowButton.setDisable(true);
        setSelectModeButton.setOnAction(e -> {
            graphPane.setOnMousePressed(null); graphPane.setOnMouseDragged(null);
            interactMode = InteractMode.SELECT_MODE;
            // Get which circles should be enabled
            for (GraphCircle n : gCircles) {
                n.setMouseTransparent(false);
                n.setOnMouseDragged(null);
                n.setOnMousePressed(null);
                n.setOnMouseClicked(e2 -> {
                    if (n.isGameSelectEnabled()) {
                        game.selectArgumentToMove(n.getName());
                    }
                });
            }
            // Still allow arrows to be selected
            for (GraphArrow a : gArrows) {
                a.setMouseTransparent(false);
                a.setOnMouseClicked(e2 -> {
                    if (selected == a) {
                        a.deselectGray();
                        selected = null;
                    } else {
                        if (selected != null) selected.deselect();
                        selected = a;
                        a.selectGray();
                    }
                });
            }
        });
        setMoveModeButton.setOnAction(e -> {
            graphPane.setOnMousePressed(null); graphPane.setOnMouseDragged(null);
            interactMode = InteractMode.MOVE_MODE;
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
                a.setOnMouseClicked(e2 -> {
                    if (selected == a) {
                        a.deselectGray();
                        selected = null;
                    } else {
                        if (selected != null) selected.deselect();
                        selected = a;
                        a.selectGray();
                    }
                });
            }
        });
        setMoveModeButton.fire();
        setSelectModeButton.fire();
    }

    public void exitGameMode() {
        addGCircleButton.setDisable(false);
        addGArrowButton.setDisable(false);
        // Restore Select functionality
        setUpInteractModeButton(setSelectModeButton, InteractMode.SELECT_MODE);
        setMoveModeButton.fire();
        setSelectModeButton.fire();
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
                if (booleanLookup.get("straightLineAttacks")) node.straightenArrows();
                node.rotateArrows();
            }
        });
    }

    public void reloadColors() {
        for (GraphCircle c: gCircles) c.reloadVisual();
        for (GraphArrow a: gArrows) a.reloadVisual();
    }

    public String getSelectedArgumentName() {
        if (selected!=null) return selected.getName();
        return null;
    }

    public ArrayList<GraphCircle> getgCircles() {return gCircles;}
}
