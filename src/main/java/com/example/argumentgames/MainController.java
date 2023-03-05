package com.example.argumentgames;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;

public class MainController {
    @FXML
    RadioButton leftSelectButton, leftMoveButton, rightSelectButton, rightMoveButton;
    @FXML
    Pane leftGraphPane, rightGraphPane;
    public MainController() {
    }

    public void initialize() {
        // Set up the two Graphs
        Graph frameworkGraph = new Graph(leftGraphPane);
        Graph gameGraph = new Graph(rightGraphPane);

        // Construct the menu Bar
        // Select, Move mode buttons
        ToggleGroup leftModeToggleGroup = new ToggleGroup();
        ToggleGroup rightModeToggleGroup = new ToggleGroup();
        setUpInteractModeButton(leftMoveButton, Graph.InteractMode.MOVE_MODE, leftModeToggleGroup, frameworkGraph);
        setUpInteractModeButton(leftSelectButton, Graph.InteractMode.SELECT_MODE, leftModeToggleGroup, frameworkGraph);
        setUpInteractModeButton(rightMoveButton, Graph.InteractMode.MOVE_MODE, rightModeToggleGroup, gameGraph);
        setUpInteractModeButton(rightSelectButton, Graph.InteractMode.SELECT_MODE, rightModeToggleGroup, gameGraph);

        leftSelectButton.fire();
        rightSelectButton.fire();
    }

    private void setUpInteractModeButton(RadioButton b, Graph.InteractMode i, ToggleGroup t, Graph g) {
        b.getStyleClass().remove("radio-button");
        b.setToggleGroup(t);
        b.setOnAction(e -> {
            g.setInteractMode(i);
        });
    }
}