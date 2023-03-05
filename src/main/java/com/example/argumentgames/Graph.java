package com.example.argumentgames;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

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

    public void addNode(String name, Pane graphPane) {
        GraphCircle n = new GraphCircle(name);
        Random rand = new Random(System.currentTimeMillis());
        //stack.setLayoutX(rand.nextDouble(500));
        //stack.setLayoutY(rand.nextDouble(200));
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
    }

    public GraphCircle getNode(String name) {
        for (GraphCircle n:
             nodes) {
            if (n.getName().equals(name)) return n;
        }
        return null;
    }

    public void addArrow(GraphCircle a, GraphCircle b, Pane graphPane) {
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
