package com.example.argumentgames;

import java.util.Random;
import java.util.Stack;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

public class MainController {
    @FXML
    private Label welcomeText;
    @FXML
    private TextField circlesInput;
    @FXML
    private Pane graphPane;

    private Graph graph;

    public MainController() {

    }

    public void initialize() {
        graph = new Graph(graphPane.getPrefWidth(), graphPane.getPrefHeight());

        graph.addShape(graphPane);
    }

    @FXML
    protected void generateCircle() {
        graph.addNode(circlesInput.getText(), graphPane);

        circlesInput.clear();
    }

    @FXML
    protected void test() {
        graph.turnToA(graphPane);
//        if (graph.getNode("A") == null) {
//            graph.addNode("A", graphPane);
//            graph.addNode("B", graphPane);
//            graph.addArrow(graph.getNode("A"), graph.getNode("B"), graphPane);
//        }
//        GraphNode n = graph.getNode("A");
//        System.out.println("XProp: " + n.getCenterXProperty().doubleValue() + "    YProp: " + n.getCenterYProperty().doubleValue());
//        System.out.println("X: " + n.getStack().getLayoutX() + "    Y: " + n.getStack().getLayoutY());
    }
}