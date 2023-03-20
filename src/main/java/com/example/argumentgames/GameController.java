package com.example.argumentgames;

import javafx.scene.control.RadioButton;
import javafx.scene.paint.Color;

import java.util.ArrayList;

// Controlls the game process by communicating with the Graph and Tree objects
public class GameController {
    boolean isGrounded;
    Graph frameworkGraph;
    Framework framework;
    TreeGraph gameTree;

    TreeArgument currentlySelected = null;

    boolean isProTurn;

    public void startGame(Graph g, Framework f, TreeGraph t, boolean isGrounded) {
        // Save objects
        framework = f;
        frameworkGraph = g;
        gameTree = t;
        this.isGrounded = isGrounded;

        // Setup start space
        gameTree.disableAll();
        frameworkGraph.disableAll();
        isProTurn = true;

        // Cleanup previous round
        // 1. Make tCircles unselectable
        for (TreeCircle c : gameTree.gettCircles() ) {c.makeGameUnselectable();}


        // Enter game mode
        frameworkGraph.enterGameMode(this);
        gameTree.enterGameMode(this);

        // Move the first argument - Root
        moveArgument(gameTree.getRoot());
    }

    public void selectArgumentToMove(String name) {
        if (currentlySelected != null) {
            for (TreeArgument arg : currentlySelected.getChildren()) {
                if (arg.getName().equals(name)) moveArgument(arg);
            }
        }
    }

    // Move the provided argument onto the game tree
    // Advance to the next round
    private void moveArgument(TreeArgument movedArg) {
        frameworkGraph.disableAll();

        // The moved state is always in
        movedArg.setState(1);
        // Display the argument and its arrow
        movedArg.getVisualTCircle().setDisplayVisible(true);

        // Setup next round
        // 1. Swap the player's turns
        isProTurn = !isProTurn;
        // 2. Make all tCircles unselectable
        gameTree.unselect();
        for (TreeCircle c : gameTree.gettCircles() ) {c.makeGameUnselectable();}
        // 3. Get all "in" arguments in the current tree placed by the other player
        ArrayList<TreeArgument> counterableArguments = gameTree.getRoot().getOfStateAndLayer(1, !isProTurn);
        // 4. Mark the counterable arguments visually
        //    Allow them to be selected
        for (TreeArgument countArg : counterableArguments) { countArg.getVisualTCircle().makeGameSelectable(); }
    }

    public void selectArgumentToCounter(String name, TreeArgument argument) {
        currentlySelected = argument;
        frameworkGraph.disableAll();
        // Take all arguments that attack the selected one - these can be moved
        ArrayList<FrameworkArgument> selectableArguments = framework.getArgumentByName(name).getAttackedBy();
        // Change display of affected nodes
        frameworkGraph.getGCircle(name).gameAttackable();
        for (FrameworkArgument arg:selectableArguments) {
            // Check if the argument hasn't been moved already
            for (TreeArgument correspondingTreeArg : argument.getChildren()) {
                if (correspondingTreeArg.getName().equals( arg.getName() ) && correspondingTreeArg.getState()==0) {
                    frameworkGraph.getGCircle(arg.getName()).gameSelectable();
                    frameworkGraph.getGCircle(arg.getName()).makeGameSelectable();
                    frameworkGraph.getGArrow(arg.getName(), name).highlight();
                }
            }


        }
    }

    // Ends the game, regardless of current state
    // Cleanups the tree/graph
    public void endGame() {
        // Enable Graph interactions previously disabled
        frameworkGraph.exitGameMode();

        // Return all enabled look
        gameTree.visualEnableAll();
        frameworkGraph.visualEnableAll();
    }
}
