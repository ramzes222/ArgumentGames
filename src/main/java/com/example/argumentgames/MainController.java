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
        // Set up test framework
        currentFramework.addArgument("a");
        currentFramework.addArgument("b");
        currentFramework.addArgument("c");
        currentFramework.addArgument("d");
        currentFramework.addAttack("a", "b");
        currentFramework.addAttack("b", "a");
        currentFramework.addAttack("a", "c");
        currentFramework.addAttack("b", "c");
        currentFramework.addAttack("c", "d");

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