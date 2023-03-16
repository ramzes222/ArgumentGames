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
    private double dragOriginY = 0, dragOriginX = 0, tCircleRadius = 30;
    private final RadioButton setSelectModeButton, setMoveModeButton, setPanModeButton;
    private final Button buildTreeButton;
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
    private TreeGraph.InteractMode interactMode = TreeGraph.InteractMode.SELECT_MODE;

    public TreeGraph(Pane graphPane, RadioButton setSelectModeButton, RadioButton setMoveModeButton,
                     RadioButton setPanModeButton, Button buildTreeButton) {
        // Save variables
        this.treePane = graphPane;
        this.setSelectModeButton = setSelectModeButton;
        this.setMoveModeButton = setMoveModeButton;
        this.setPanModeButton = setPanModeButton;
        this.buildTreeButton = buildTreeButton;
        //
        // Setup interact mode buttons
        setUpInteractModeButton(setMoveModeButton, TreeGraph.InteractMode.MOVE_MODE);
        setUpInteractModeButton(setSelectModeButton, TreeGraph.InteractMode.SELECT_MODE);
        setUpInteractModeButton(setPanModeButton, TreeGraph.InteractMode.PAN_MODE);
        this.setSelectModeButton.fire();
        // Setup other buttons
        this.buildTreeButton.setOnAction(e-> {
            TreeArgument arg = new TreeArgument("a", null);
            TreeArgument child1 = new TreeArgument("b", arg);
            TreeArgument child2 = new TreeArgument("c", arg);
            TreeArgument child3 = new TreeArgument("d", child1);
            buildTree(arg);
        });
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
    private void setUpInteractModeButton(RadioButton b, TreeGraph.InteractMode i) {
        b.getStyleClass().remove("radio-button");
        b.setToggleGroup(this.tg);
        b.setOnAction(e -> {
            setInteractMode(i);
        });
    }

    // Changes the interact mode for the tree
    private void setInteractMode(TreeGraph.InteractMode m) {
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
                    if (interactMode == TreeGraph.InteractMode.PAN_MODE) {
                        dragOriginX = e.getSceneX();
                        dragOriginY = e.getSceneY();
                    }
                });
                treePane.setOnMouseDragged(e -> {
                    if (interactMode == TreeGraph.InteractMode.PAN_MODE) {
                        double xDrag = e.getSceneX() - dragOriginX;
                        double yDrag = e.getSceneY() - dragOriginY;
                        dragOriginX = e.getSceneX();
                        dragOriginY = e.getSceneY();
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
            if (interactMode == TreeGraph.InteractMode.MOVE_MODE) {
                node.toFront();
                dragOriginX = e.getSceneX() - node.getLayoutX();
                dragOriginY = e.getSceneY() - node.getLayoutY();
            }
        });
        node.setOnMouseDragged(e -> {
            if (interactMode == TreeGraph.InteractMode.MOVE_MODE) {
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
    private TreeCircle addGCircle(String name) {
        TreeCircle n = new TreeCircle(name, 45);
        n.setLayoutX(50);
        n.setLayoutY(50);
        // Implement selection
        if (interactMode == TreeGraph.InteractMode.SELECT_MODE) {
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
        // Return the newly created circle
        return n;
    }

    // Rebuilds the tree, only moving the existing nodes
    private void buildTree(TreeArgument root) {
        if (root == null) { return; }
        double middleX = treePane.getWidth()/2;
        double yLevel = tCircleRadius;
        double yDistance = 3*tCircleRadius, xDistance = 3*tCircleRadius;
        // Create the root
        TreeCircle rootCircle = new TreeCircle(root, tCircleRadius); treePane.getChildren().add(rootCircle);
        rootCircle.moveToXY(middleX, yLevel);
        // Get the list of children of root
        ArrayList<TreeArgument> nextLayer = root.getChildren();
        //
        // Iterate over tree levels
        while (!nextLayer.isEmpty()) {
            ArrayList<TreeArgument> nextChildren = new ArrayList<>();
            // Set the new x and y values
            yLevel += yDistance;
            double itemCount = nextLayer.size();
            double xPos = middleX - ((itemCount-1)/2)*xDistance;
            for (TreeArgument a: nextLayer) {
                TreeCircle newCircle = new TreeCircle(a, tCircleRadius); treePane.getChildren().add(newCircle);
                newCircle.moveToXY(xPos, yLevel);
                xPos += xDistance;
                nextChildren.addAll(a.getChildren());
            }
            nextLayer = nextChildren;
        }
    }
}
