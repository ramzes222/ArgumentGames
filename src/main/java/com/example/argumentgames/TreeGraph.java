package com.example.argumentgames;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class TreeGraph {
    private final double moveAwayStep = 10;
    private double dragOriginY = 0, dragOriginX = 0, gCircleRadius = 45;
    private final RadioButton setSelectModeButton, setMoveModeButton, setPanModeButton;
    private final ArrayList<TreeCircle> tCircles = new ArrayList<>();
    private final ArrayList<TreeArrow> tArrows = new ArrayList<>();
    private TreeCircle selected = null;
    private final Pane treePane;
    private final ToggleGroup tg = new ToggleGroup();
    private EventHandler<MouseEvent> graphWideEvent;
    enum InteractMode {
        SELECT_MODE,
        MOVE_MODE,
        PAN_MODE,
        SPECIAL_MODE
    }
    private Graph.InteractMode interactMode = Graph.InteractMode.SELECT_MODE;

    public TreeGraph(Pane graphPane, RadioButton setSelectModeButton, RadioButton setMoveModeButton, RadioButton setPanModeButton) {
        // Save variables
        this.treePane = graphPane;
        this.setSelectModeButton = setSelectModeButton;
        this.setMoveModeButton = setMoveModeButton;
        this.setPanModeButton = setPanModeButton;
        //
        // Setup interact mode buttons
        setUpInteractModeButton(setMoveModeButton, Graph.InteractMode.MOVE_MODE);
        setUpInteractModeButton(setSelectModeButton, Graph.InteractMode.SELECT_MODE);
        setUpInteractModeButton(setPanModeButton, Graph.InteractMode.PAN_MODE);
        this.setSelectModeButton.fire();
        setUpClip();
    }

    // Creates a boundary for the area
    // Only the nodes inside of the area will be visible - anything outside will be cut
    private void setUpClip() {
        final Rectangle outputClip = new Rectangle();
        outputClip.setArcWidth(5);
        outputClip.setArcHeight(5);
        outputClip.setWidth(treePane.getWidth());
        outputClip.setHeight(treePane.getHeight());
        treePane.setClip(outputClip);
        treePane.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            outputClip.setWidth(newValue.getWidth());
            outputClip.setHeight(newValue.getHeight());
        });
    }

    // Performs basic setup for buttons that change the interact mode
    // Changes their appearance, sets action, and assigns toggle group
    private void setUpInteractModeButton(RadioButton b, Graph.InteractMode i) {
        b.getStyleClass().remove("radio-button");
        b.setToggleGroup(this.tg);
        b.setOnAction(e -> {
            setInteractMode(i);
        });
    }

    // Changes the interact mode for the tree
    private void setInteractMode(Graph.InteractMode m) {
        switch (m) {
            // SELECT MODE
            // Allows user to click on Nodes to select them
            // Allows user to highlight nodes
            case SELECT_MODE -> {
                treePane.setOnMousePressed(null); treePane.setOnMouseDragged(null);
                interactMode = m;
                setDisableButtons(false);
                // Implement selection
                for (TreeCircle n : tCircles) {
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
            }
            // MOVE MODE
            // Allows user to drag the nodes around and move them
            case MOVE_MODE -> {
                treePane.setOnMousePressed(null); treePane.setOnMouseDragged(null);
                interactMode = m;
                if (selected != null) {
                    selected.deselect();
                    selected = null;
                }
                // Turn off other events, then make draggable
                for (TreeCircle n : tCircles) {
                    n.setMouseTransparent(false);
                    n.setOnMouseClicked(null);
                    makeDraggable(n);
                }
            }
            // PAN MODE
            // Allows user to drag the mouse to "look around", and scroll to zoom in and out
            case PAN_MODE -> {
                // Make nodes and arrows transparent - only clicks on the Pane matter
                interactMode = m;
                if (selected != null) {
                    selected.deselect();
                    selected = null;
                }
                for (TreeCircle c: tCircles) { c.setMouseTransparent(true); }
                treePane.setOnMousePressed(e -> {
                    if (interactMode == Graph.InteractMode.PAN_MODE) {
                        dragOriginX = e.getSceneX();
                        dragOriginY = e.getSceneY();
                    }
                });
                treePane.setOnMouseDragged(e -> {
                    if (interactMode == Graph.InteractMode.PAN_MODE) {
                        double xDrag = e.getSceneX() - dragOriginX;
                        double yDrag = e.getSceneY() - dragOriginY;
                        dragOriginX = e.getSceneX();
                        dragOriginY = e.getSceneY();
                        for (TreeArrow a: tArrows) { a.translateControlPointXY(xDrag, yDrag); }
                        for (TreeCircle c: tCircles) { c.translateXY(xDrag, yDrag); }
                    }
                });
            }
            // SPECIAL MODE
            // Turns off all other events and interactions
            // Used in special cases with unique events
            case SPECIAL_MODE -> {
                treePane.setOnMousePressed(null); treePane.setOnMouseDragged(null);
                interactMode = m;
                if (selected != null) {
                    selected.deselect();
                    selected = null;
                }
                setDisableButtons(true);
                // Disable all GCircle/edge click events
                for (TreeCircle n : tCircles) {
                    n.setMouseTransparent(false);
                    n.setOnMouseClicked(null);
                    n.setOnMouseDragged(null);
                    n.setOnMousePressed(null);
                }
                for (TreeArrow a : tArrows) {
                    a.setMouseTransparent(false);
                    a.setOnMouseClicked(null);
                    a.setOnMouseDragged(null);
                    a.setOnMousePressed(null);
                }
            }
        }
    }

    // Adds the events that allow the provided Circle to be dragged around by the user
    private void makeDraggable(TreeCircle node) {
        node.setOnMousePressed(e -> {
            if (interactMode == Graph.InteractMode.MOVE_MODE) {
                node.toFront();
                dragOriginX = e.getSceneX() - node.getLayoutX();
                dragOriginY = e.getSceneY() - node.getLayoutY();
            }
        });
        node.setOnMouseDragged(e -> {
            if (interactMode == Graph.InteractMode.MOVE_MODE) {
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

    // Sets all control buttons to disabled (or enabled, as the parameter)
    private void setDisableButtons(boolean b) {
        setSelectModeButton.setDisable(b);
        setMoveModeButton.setDisable(b);
        setPanModeButton.setDisable(b);
    }

    // Adds a new tCircle to the tree under the specified name
    // Only adds the visual representation of an argument - does not affect the underlying game tree
    private void addGCircle(String name) {
        GraphCircle n = new GraphCircle(name, 45);
        n.setLayoutX(50);
        n.setLayoutY(50);
        // Implement selection
        if (interactMode == Graph.InteractMode.SELECT_MODE) {
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
        treePane.getChildren().add(n);
        tCircles.add(n);
        //
        // Move the new node to an empty space
        moveNode(n);
    }

    // Rebuilds the tree, only moving the existing nodes
    private void buildTree(TreeNode root) {
        if (root == null) { return; }


    }
}
