package com.example.argumentgames;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Pair;

public class Graph {
    private double width, height, dragOriginY = 0, dragOriginX = 0;
    private Button addNodeButton = null, addEdgeButton = null;
    private RadioButton setSelectModeButton, setMoveModeButton;
    private ArrayList<GraphCircle> nodes = new ArrayList<>();
    private ArrayList<GraphArrow> arrows = new ArrayList<>();
    private GraphNode selected = null;
    private Pane graphPane;
    private ToggleGroup tg = new ToggleGroup();
    enum InteractMode {
        SELECT_MODE,
        MOVE_MODE,
        PAN_MODE,
        SPECIAL_MODE
    }
    private InteractMode interactMode = InteractMode.SELECT_MODE;

    public Graph(Pane graphPane, RadioButton setSelectModeButton, RadioButton setMoveModeButton) {
        // Save variables
        this.width = graphPane.getPrefWidth();
        this.height = graphPane.getPrefHeight();
        this.graphPane = graphPane;
        this.setSelectModeButton = setSelectModeButton;
        this.setMoveModeButton = setMoveModeButton;
        //
        // Setup interact mode buttons
        setUpInteractModeButton(setMoveModeButton, InteractMode.MOVE_MODE);
        setUpInteractModeButton(setSelectModeButton, InteractMode.SELECT_MODE);
        this.setSelectModeButton.fire();
    }
    public Graph(Pane graphPane, RadioButton setSelectModeButton, RadioButton setMoveModeButton, Button addNodeButton, Button addEdgeButton) {
        // Save variables
        this.width = graphPane.getPrefWidth();
        this.height = graphPane.getPrefHeight();
        this.setSelectModeButton = setSelectModeButton;
        this.setMoveModeButton = setMoveModeButton;
        this.graphPane = graphPane;
        this.addNodeButton = addNodeButton;
        this.addEdgeButton = addEdgeButton;
        //
        // Setup interact mode buttons
        setUpInteractModeButton(setMoveModeButton, InteractMode.MOVE_MODE);
        setUpInteractModeButton(setSelectModeButton, InteractMode.SELECT_MODE);
        this.setSelectModeButton.fire();
        //
        // Setup control buttons
        this.addNodeButton.setOnAction(e-> { addNode(); });
        this.addEdgeButton.setOnAction(e-> { beginAddEdge(); });
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
            case SELECT_MODE -> {
                interactMode = m;
                setDisableButtons(false);
                for (GraphCircle n : nodes) {
                    n.setOnMouseDragged(null);
                    n.setOnMousePressed(null);
                }
                // Implement selection
                for (GraphCircle n : nodes) {
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
            case MOVE_MODE -> {
                interactMode = m;
                if (this.addEdgeButton != null) {
                    this.addEdgeButton.setDisable(true);
                }
                if (selected != null) {
                    selected.deselect();
                    selected = null;
                }
                // Turn off other events, then make draggable
                for (GraphCircle n : nodes) {
                    n.setOnMouseClicked(null);
                    makeDraggable(n);
                }
            }
            case SPECIAL_MODE -> {
                interactMode = m;
                if (selected != null) {
                    selected.deselect();
                    selected = null;
                }
                setDisableButtons(true);
                // Disable all node/edge click events
                for (GraphCircle n : nodes) {
                    n.setOnMouseClicked(null);
                    n.setOnMouseDragged(null);
                    n.setOnMousePressed(null);
                }
                for (GraphArrow a : arrows) {
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
        if (addEdgeButton != null) addEdgeButton.setDisable(b);
        if (addNodeButton != null) addNodeButton.setDisable(b);
    }

    private double heightInBounds(double y) { return Math.min( Math.max(0, y), this.height); }
    private double widthInBounds(double x) { return Math.min( Math.max(0, x), this.width); }

    public void addNode() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add new node");
        dialog.setContentText("Please enter the name of the new node:");
        Optional<String> newName = dialog.showAndWait();
        newName.ifPresent(name -> {
            // Check if the name is unique among existing nodes
            boolean unique = true;
            for (GraphCircle c: nodes) { if (c.getName().equals(name)) {unique = false; break;}}
            if (unique) {
                GraphCircle n = new GraphCircle(name);
                n.setLayoutX(200);
                n.setLayoutY(100);
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
                nodes.add(n);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Node already exists");
                alert.setContentText("There already exists a node with name " + name + "!");
                alert.showAndWait();
            }
        });
    }

    public GraphCircle getNode(String name) {
        for (GraphCircle n:
             nodes) {
            if (n.getName().equals(name)) return n;
        }
        return null;
    }

    public void beginAddEdge() {
        // Check which Node is currently selected
        // It will be the origin of the edge
        if (selected != null && selected.getClass() == GraphCircle.class) {
            // Remember origin node and style it
            GraphCircle fromNode = (GraphCircle) selected;
            fromNode.highlight();
            selected = null;
            //
            // Add arrow for visuals
            MouseArrow newEdgeMouseArrow = new MouseArrow(fromNode.getLayoutX(), fromNode.getLayoutY(), graphPane);
            setInteractMode(InteractMode.SPECIAL_MODE);
            //
            // Add event to all nodes
            System.out.println("b");
            for (GraphCircle toNode: nodes) { toNode.setOnMouseClicked(e -> { addArrow(fromNode, toNode); endAddEdgeEvent(newEdgeMouseArrow); }); }
        } else {
            // This should be prevented by other functions, but
            // just in case show a warning
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No node selected");
            alert.setContentText("To add an arrow you must select a node first!");
            alert.showAndWait();
        }
    }

    private void endAddEdgeEvent(MouseArrow mArrow) {
        // Perform cleanup after manual Add Edge has been completed
        setInteractMode(InteractMode.SELECT_MODE);
        mArrow.delete();
        mArrow = null;
        graphPane.setOnMouseMoved(null);
    }

    private void addArrow(GraphCircle a, GraphCircle b) {
        System.out.println("c");
        if (a == b) {
            // An argument cannot attack itself
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Selected the same node twice!");
            alert.setContentText("To add an attack you must select two different arguments!");
            alert.showAndWait();
        } else {
            GraphArrow arrow = new GraphArrow(a, b);
            graphPane.getChildren().addAll(arrow, arrow.getArrowTip());
            arrows.add(arrow);
//            arrow.setOnMouseClicked(e -> {
//                if (interactMode == InteractMode.SELECT_MODE) {
//                    if (selected == arrow) {
//                        selected.deselect();
//                        selected = null;
//                    } else {
//                        if (selected != null) selected.deselect();
//                        selected = arrow;
//                        selected.select();
//                    }
//                }
//            });
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
