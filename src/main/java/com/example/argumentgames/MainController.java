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
    Button leftAddNodeButton, leftAddEdgeButton, leftCleanupButton, rightBuildTreeButton;
    @FXML
    Pane leftGraphPane, rightGraphPane;
    @FXML
    ChoiceBox<String> gameTypeChoiceBox;

    Framework currentFramework;

    Graph frameworkGraph;
    TreeGraph gameTree;
    public MainController() {
    }

    public void initialize() {
        // Set up the two Graphs
        currentFramework = new Framework();
        frameworkGraph = new Graph(leftGraphPane, leftSelectButton, leftMoveButton, leftPanButton, leftAddNodeButton, leftAddEdgeButton, leftCleanupButton);
        frameworkGraph.loadFramework(currentFramework);
        gameTree = new TreeGraph(rightGraphPane, rightSelectButton, rightMoveButton, rightPanButton, rightBuildTreeButton);

        // Construct the menu Bar

    }

    @FXML
    public void buildTreeFromFramework() {
        String rootName = frameworkGraph.getSelectedArgumentName();
        if (rootName==null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Please select an argument first");
            alert.setContentText("To generate a game tree, first select an argument. The game will be rooted at that argument");
            alert.showAndWait();
        } else {
            FrameworkArgument rootOfTree = currentFramework.getArgumentByName(rootName);
            boolean isGrounded = gameTypeChoiceBox.getValue().equals("Grounded");
            TreeArgument gameTreeRoot = currentFramework.buildGameTree(rootOfTree, isGrounded);
            gameTree.buildTree(gameTreeRoot);
        }
    }
}