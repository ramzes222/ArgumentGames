package com.example.argumentgames;
import java.util.*;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

public class Graph {

    private double dragOriginX = 0;
    private double dragOriginY = 0;
    private double width, height;
    private ArrayList<GraphNode> nodes = new ArrayList<>();
    private ArrayList<GraphArrow> arrows = new ArrayList<>();

    public Graph(Double width, Double height) {
        this.width = width;
        this.height = height;
    }

    public void addNode(String name, Pane graphPane) {
        GraphNode n = new GraphNode(name);
        StackPane stack = n.getStack();
        Random rand = new Random(System.currentTimeMillis());
        //stack.setLayoutX(rand.nextDouble(500));
        //stack.setLayoutY(rand.nextDouble(200));
        stack.setLayoutX(0);
        stack.setLayoutY(0);
        makeDraggable(n);
        graphPane.getChildren().add(stack);

        nodes.add(n);
    }

    public GraphNode getNode(String name) {
        for (GraphNode n:
             nodes) {
            if (n.getName().equals(name)) return n;
        }
        return null;
    }

    public void addArrow(GraphNode a, GraphNode b, Pane graphPane) {
        GraphArrow arrow = new GraphArrow(a, b);
        graphPane.getChildren().addAll(arrow.getNodes());
        arrows.add(arrow);
    }

    private void makeDraggable(GraphNode gNode) {
        Node node = gNode.getStack();
        node.setOnMousePressed(e -> {
            node.toFront();
            dragOriginX = e.getSceneX() - node.getLayoutX();
            dragOriginY = e.getSceneY() - node.getLayoutY();
        });
        node.setOnMouseDragged(e -> {
            Double newX = e.getSceneX() - dragOriginX;
            gNode.getCenterXProperty().set(newX);
            node.setLayoutX(newX);
            Double newY = e.getSceneY() - dragOriginY;
            gNode.getCenterYProperty().set(newY);
            node.setLayoutY(newY);
//            for (GraphArrow ar: arrows
//                 ) {
//                int conType = ar.getConnectionType(node);
//                if (conType!=-1) ar.moveArrowPoint(conType, dragOriginX+transX, dragOriginY+transY);
//            }
        });
    }
}
