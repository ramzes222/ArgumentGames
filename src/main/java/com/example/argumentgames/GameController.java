package com.example.argumentgames;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeTableRow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// Controlls the game process by communicating with the Graph and Tree objects
public class GameController {
    boolean isGrounded;
    Graph frameworkGraph;
    Framework framework;
    TreeGraph gameTree;
    Label gameLabel;

    TreeArgument currentlySelected = null;

    boolean isProTurn, isComputerPlaying;

    public void startGame(Graph g, Framework f, TreeGraph t, boolean isGrounded, boolean isComputerPlaying, Label gameLabel) {
        // Save objects
        framework = f;
        frameworkGraph = g;
        gameTree = t;
        this.isGrounded = isGrounded;
        this.isComputerPlaying = isComputerPlaying;
        this.gameLabel = gameLabel;
        this.gameLabel.setVisible(true);
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
        // Display a message in the gameLabel
        String text = "";
        if (isProTurn) text = "Proponent"; else text = "Opponent";
        text += " moves argument '" + movedArg.getName() + "'";
        gameLabel.setText(text);

        frameworkGraph.disableAll();
        // The moved state is always in
        movedArg.setState(1);
        // Display the argument and its arrow
        movedArg.getVisualTCircle().setDisplayVisible(true);

        // 1. Swap the player's turns
        isProTurn = !isProTurn;

        // 2. Check if the game ends
        if (checkIfGameEnd()) {
            // Game over - do not setup next round
            stopInteractions();
            return;
        }

        // Otherwise, setup next round
        // 3. Make all tCircles unselectable
        gameTree.unselect();
        for (TreeCircle c : gameTree.gettCircles() ) {c.makeGameUnselectable();}
        // 4. If it's players turn, set up interactivity
        //    If it's computer turn, make its move
        if (isProTurn && isComputerPlaying) {
            computerTurn();
        } else {
            // 4. Get all "in" arguments in the current tree placed by the other player
            ArrayList<TreeArgument> counterableArguments = gameTree.getRoot().getOfStateAndLayer(1, !isProTurn);
            // 5. Mark the counterable arguments visually
            //    Allow them to be selected
            for (TreeArgument countArg : counterableArguments) { countArg.getVisualTCircle().makeGameSelectable(); }
        }
    }

    // Checks to see if there are still any remaining possible moves
    // If there are none, stops the game
    // Returns true if the game is over, false otherwise
    private boolean checkIfGameEnd() {
        // Get all "in" arguments in the current tree placed by the other player
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
        gameLabel.setText(text);
        alert.setHeaderText(text);
        alert.setContentText("No more moves are possible.\nYou may keep looking at the final generated game tree. " +
                "\nTo finish, click the button between the two graphs.");
        alert.setHeight(500);
            alert.showAndWait();
        return true;
    }

    // Performs the turn as the computer
    private void computerTurn() {
        gameLabel.setText("Computer is thinking...");
        // Get all arguments that could be countered
        ArrayList<TreeArgument> counterableArguments = gameTree.getRoot().getOfStateAndLayer(1, !isProTurn);
        // 5. Mark the counterable arguments visually
        for (TreeArgument countArg : counterableArguments) { countArg.getVisualTCircle().setVisual("computerSelectable"); }
        ArrayList<TreeArgument> goodMoves = new ArrayList<>();
        for (TreeArgument countered : counterableArguments) {
            // Take all arguments that attack the selected one - these can be moved
            // Collect all that are in the winning strategy
            for (TreeArgument child : countered.getChildren()) { if (child.isInWinningStrategy) goodMoves.add(child); }
        }
        PauseTransition compMoveWait = new PauseTransition(Duration.seconds(2));
        compMoveWait.setOnFinished(e-> {
            Random rand = new Random(System.currentTimeMillis());
            if (goodMoves.size() == 0) {
                // No move in winning strategy possible - the game is lost, computer will perform a random move
                TreeArgument selectedMove = null;
                while (selectedMove == null) {
                    TreeArgument selectedArgumentToCounter =
                            counterableArguments.get(rand.nextInt(counterableArguments.size()));
                    if (!selectedArgumentToCounter.getChildren().isEmpty()) selectedMove =
                            selectedArgumentToCounter.getChildren().get(rand.nextInt(selectedArgumentToCounter.getChildren().size()));
                }
                moveArgument(selectedMove);
            } else {
                // Perform a random good move - as all are in winning strategy, all will lead us to victory
                TreeArgument selectedArgument =
                        goodMoves.get(rand.nextInt(goodMoves.size()));
                moveArgument(selectedArgument);
            }
        });
        compMoveWait.play();
    }

    public void selectArgumentToCounter(String name, TreeArgument argument) {
        currentlySelected = argument;
        frameworkGraph.disableAll();
        // Take all arguments that attack the selected one - these can be moved
        ArrayList<FrameworkArgument> selectableArguments = framework.getArgumentByName(name).getAttackedBy();
        // Change display of affected nodes
        frameworkGraph.getGCircle(name).setVisual("gameAttackable");
        for (FrameworkArgument arg:selectableArguments) {
            // Check if the argument hasn't been moved already
            for (TreeArgument correspondingTreeArg : argument.getChildren()) {
                if (correspondingTreeArg.getName().equals( arg.getName() ) && correspondingTreeArg.getState()==0) {
                    frameworkGraph.getGCircle(arg.getName()).setVisual("gameSelectable");
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
        gameLabel.setVisible(false);
        // Enable Graph interactions previously disabled
        frameworkGraph.exitGameMode();

        // Return all enabled look
        gameTree.visualEnableAll();
        frameworkGraph.visualEnableAll();
    }
}
