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

        } else {
            FrameworkArgument rootOfTree = currentFramework.getArgumentByName(rootName);
            TreeArgument gameTreeRoot = currentFramework.buildGameTree(rootOfTree, false);
            gameTree.buildTree(gameTreeRoot);
        }
    }
}