package com.example.argumentgames;

import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeTableRow;
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
        // Reset game tree
        gameTree.getRoot().resetState();

        // Find the winning strategy, if it exists
        gameTree.getRoot().updateWinningStrategy();

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

        // 1. Swap the player's turns
        isProTurn = !isProTurn;

        // Check if the game ends
        if (checkIfGameEnd()) {
            // Game over - do not setup next round
            stopInteractions();
            return;
        }

        // Otherwise, setup next round
        // 2. Make all tCircles unselectable
        gameTree.unselect();
        for (TreeCircle c : gameTree.gettCircles() ) {c.makeGameUnselectable();}
        // 3. Get all "in" arguments in the current tree placed by the other player
        ArrayList<TreeArgument> counterableArguments = gameTree.getRoot().getOfStateAndLayer(1, !isProTurn);
        // 4. Mark the counterable arguments visually
        //    Allow them to be selected
        for (TreeArgument countArg : counterableArguments) { countArg.getVisualTCircle().makeGameSelectable(); }
    }

    // Checks to see if there are still any remaining possible moves
    // If there are none, stops the game
    // Returns true if the game is over, false otherwise
    private boolean checkIfGameEnd() {
        // 3. Get all "in" arguments in the current tree placed by the other player
        ArrayList<TreeArgument> counterableArguments = gameTree.getRoot().getOfStateAndLayer(1, !isProTurn);
        // Check if they have any unmoved children
        for (TreeArgument argToCounter : counterableArguments) {
            for (TreeArgument child : argToCounter.getChildren()) {
                // After moving arguments get 1 or 2 state
                // Arguments with state 0 are unmoved
                if (child.getState()==0) return false;
            }
        }
        // If, after going through all possibilities, no moves remain, end the game
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("End of the game");
        String text = "";
        if (isProTurn) text+="Opponent";
        else text+="Proponent";
        text+= " has won!";
        alert.setHeaderText(text);
        alert.setContentText("No more moves are possible.\nYou may keep looking at the final generated game tree. " +
                "\nTo finish, click the button between the two graphs.");
        alert.setHeight(500);
        alert.showAndWait();
        return true;
    }

    // Performs the turn as the computer
    private void computerTurn() {

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

    // Disables all interactions with the graph
    // Effectively ends the game, but does not clean up
    public void stopInteractions() {
        frameworkGraph.getgCircles().forEach(GraphCircle::makeGameUnselectable);
        gameTree.gettCircles().forEach(TreeCircle::makeGameUnselectable);
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
