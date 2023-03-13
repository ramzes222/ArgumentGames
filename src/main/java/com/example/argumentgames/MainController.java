package com.example.argumentgames;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.scene.control.Dialog;

public class MainController {
    @FXML
    RadioButton leftSelectButton, leftMoveButton, leftPanButton, rightSelectButton, rightMoveButton, rightPanButton;
    @FXML
    Button leftAddNodeButton, leftAddEdgeButton;
    @FXML
    Pane leftGraphPane, rightGraphPane;

    Graph frameworkGraph, gameGraph;
    public MainController() {
    }

    public void initialize() {
        // Set up the two Graphs
        frameworkGraph = new Graph(leftGraphPane, leftSelectButton, leftMoveButton, leftPanButton, leftAddNodeButton, leftAddEdgeButton);
        gameGraph = new Graph(rightGraphPane, rightSelectButton, rightMoveButton, rightPanButton);

        // Construct the menu Bar

    }

    @FXML
    public void testAction() {
    }
}