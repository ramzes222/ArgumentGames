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
    private ArrayList<GraphCircle> nodes = new ArrayList<>();
    private ArrayList<GraphArrow> arrows = new ArrayList<>();
    private GraphNode selected = null;
    private Pane graphPane;
    enum InteractMode {
        SELECT_MODE,
        MOVE_MODE,
        PAN_MODE
    }
    private InteractMode interactMode = InteractMode.SELECT_MODE;

    public Graph(Pane graphPane) {
        this.width = graphPane.getPrefWidth();
        this.height = graphPane.getPrefHeight();
        this.graphPane = graphPane;
    }

    public void setInteractMode(InteractMode m) {
        switch (m) {
            case SELECT_MODE:
                interactMode = m;
                break;
            case MOVE_MODE:
                interactMode = m;
                break;
            case PAN_MODE:
                break;
        }
    }

    private double heightInBounds(double y) { return Math.min( Math.max(0, y), this.height); }
    private double widthInBounds(double x) { return Math.min( Math.max(0, x), this.width); }

    public void addNode() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add new node");
        //dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter the name of the new node:");

        Optional<String> newName = dialog.showAndWait();

        newName.ifPresent(name -> {
            // Check if the name is unique among existing nodes
            Boolean unique = true;
            for (GraphCircle c: nodes) { if (c.getName().equals(name)) {unique = false; break;}}

            if (unique) {
                GraphCircle n = new GraphCircle(name);
                n.setLayoutX(200);
                n.setLayoutY(100);
                makeDraggable(n);
                graphPane.getChildren().add(n);
                // Implement selection
                n.setOnMouseClicked(e -> {
                    if (interactMode == InteractMode.SELECT_MODE) {
                        if (selected == n) {
                            selected.deselect();
                            selected = null;
                        } else {
                            if (selected != null) selected.deselect();
                            selected = n;
                            selected.select();
                        }
                    }
                });
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
            GraphCircle fromNode = (GraphCircle) selected;

            // Add arrow for visuals
            MouseArrow newEdgeMouseArrow = new MouseArrow(fromNode.getLayoutX(), fromNode.getLayoutY(), graphPane);
        } else {
            // This should be prevented by other functions, but
            // just in case show a warning
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No node selected");
            alert.setContentText("To add an arrow you must select a node first!");
            alert.showAndWait();
        }
    }

    public void addArrow(GraphCircle a, GraphCircle b) {
        GraphArrow arrow = new GraphArrow(a, b);
        graphPane.getChildren().addAll(arrow, arrow.getArrowTip());
        arrows.add(arrow);
        arrow.setOnMouseClicked(e -> {
            if (interactMode == InteractMode.SELECT_MODE) {
                if (selected == arrow) {
                    selected.deselect();
                    selected = null;
                } else {
                    if (selected != null) selected.deselect();
                    selected = arrow;
                    selected.select();
                }
            }
        });
        a.addArrow(arrow);
        b.addArrow(arrow);
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
