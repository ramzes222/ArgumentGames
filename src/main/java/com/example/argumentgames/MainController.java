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
    enum InteractMode {
        SELECT_MODE,
        MOVE_MODE,
        PAN_MODE
    }
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
        graph = new Graph(graphPane);

        // Testing setup
        graph.addNode("A", graphPane);
        graph.addNode("B", graphPane);
        graph.addArrow(graph.getNode("A"), graph.getNode("B"), graphPane);
    }

    @FXML
    protected void selectMode() {
        graph.setInteractMode(InteractMode.SELECT_MODE);
    }

    @FXML
    protected void generateCircle() {
        graph.addNode(circlesInput.getText(), graphPane);

        circlesInput.clear();
    }

    @FXML
    protected void test() {
        final Rectangle outputClip = new Rectangle();
        //outputClip.setArcWidth(arc);
        //outputClip.setArcHeight(arc);
        outputClip.setWidth(graphPane.getWidth());
        outputClip.setHeight(graphPane.getHeight());
        graphPane.setClip(outputClip);

        graphPane.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            outputClip.setWidth(newValue.getWidth());
            outputClip.setHeight(newValue.getHeight());
        });
    }
}