package com.example.argumentgames;

import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Random;

// Controls the game process by communicating with the Graph and Tree objects
public class GameController {
    boolean isGrounded;
    Button passButton;
    Graph frameworkGraph;
    Framework framework;
    TreeGraph gameTree;
    Label gameLabel;
    TreeArgument currentlySelected = null;
    boolean isProTurn, isComputerPlaying, baseRuleset = true, lastPass = false;
    /* RULESETS:
    baseRuleset = True
    Pro can pass, opp cannot

    baseRuleset = False
    Players can pass only when no possible moves
    Game is exhaustive - every argument will be moved
    */

    public void startGame(Graph g, Framework f, TreeGraph t, boolean isGrounded, boolean isComputerPlaying, boolean baseRuleset, Label gameLabel, Button passButton) {
        // Save objects
        framework = f;
        frameworkGraph = g;
        gameTree = t;
        this.isGrounded = isGrounded;
        this.isComputerPlaying = isComputerPlaying;
        this.baseRuleset = baseRuleset;
        this.gameLabel = gameLabel;
        this.gameLabel.setVisible(true);
        this.passButton = passButton;
        // Setup pass button
        passButton.setOnAction(e->moveArgument(null));
        lastPass = false;
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
    // May pass null arg - that indicates a player passes
    private void moveArgument(TreeArgument movedArg) {
        if (movedArg != null) {
            // Player did not pass - move argument
            // Display a message in the gameLabel
            String text;
            if (isProTurn) text = "Proponent"; else text = "Opponent";
            text += " moves argument '" + movedArg.getName() + "'";
            gameLabel.setText(text);

            frameworkGraph.disableAll();
            // The moved state is always in
            movedArg.setState(1);
            // Display the argument and its arrow
            movedArg.appear();
        } else {
            // Player passed - move no argument, but advance turn
            // Display a message in the gameLabel
            String text;
            if (isProTurn) text = "Proponent"; else text = "Opponent";
            text += " passed.";
            gameLabel.setText(text);
        }
        // 1. Swap the player's turns
        isProTurn = !isProTurn;
        passButton.setVisible(false);

        // 2. Check if the game ends
        if (lastPass && (movedArg==null)) {
            // Game over - both players passed
            // Do not set up next round
            gameEndSignal();
            stopInteractions();
            return;
        }
        // Save pass information
        lastPass = (movedArg==null);

        // Otherwise, setup next round
        // 3. Make all tCircles unselectable
        gameTree.unselect();
        for (TreeCircle c : gameTree.gettCircles() ) {c.makeGameUnselectable();}
        // 4. If it's players turn, set up interactivity
        //    If it's computer turn, make its move
        if (isProTurn) {
            if (isComputerPlaying) {
                gameLabel.setText("Computer is thinking...");
                // Give visual indicators
                // Get all arguments that could be countered - all args moved by opponent
                ArrayList<TreeArgument> counterableArguments = gameTree.getRoot().getOfLayer(!isProTurn);
                // 5. Mark the counterable arguments visually
                for (TreeArgument countArg : counterableArguments) {
                    countArg.getVisualTCircle().setVisual("computerSelectable");
                }
                // After a short wait perform the move
                PauseTransition compMoveWait = new PauseTransition(Duration.seconds(2));
                compMoveWait.setOnFinished(e -> computerTurn());
                compMoveWait.play();
            } else {
                // If Base Ruleset, display the pass button
                if (baseRuleset) passButton.setVisible(true);
            }
        } else {
            // 4. Get all "in" arguments in the current tree placed by the other player
            ArrayList<TreeArgument> counterableArguments = gameTree.getRoot().getOfLayer(!isProTurn);
            // 5. Mark the counterable arguments visually
            //    Allow them to be selected
            for (TreeArgument countArg : counterableArguments) { countArg.getVisualTCircle().makeGameSelectable(); }
        }
    }

    // Signal end of the game with a message
    private void gameEndSignal() {
        // If, after going through all possibilities, no moves remain, end the game
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("End of the game");
        String text = gameLabel.getText() + "   ";
        // Choose winner depending on the status of the root argument
        if (gameTree.getRoot().getState()==1) text+="Opponent";
        else text+="Proponent";
        text+= " has won!";
        gameLabel.setText(text);
        alert.setHeaderText(text);
        alert.setContentText("""
                Both players passed, ending the game.
                You may keep looking at the final generated game tree.\s
                To finish, click the button between the two graphs.""");
        alert.setHeight(500);
        alert.show();
    }

    // Performs the turn as the computer
    private void computerTurn() {
        Random rand = new Random(System.currentTimeMillis());
        // Get all in arguments that could be countered - computer prefers moves that do something
        ArrayList<TreeArgument> favoredArguments = gameTree.getRoot().getOfStateAndLayer(1, !isProTurn);
        ArrayList<TreeArgument> goodMoves = new ArrayList<>();
        for (TreeArgument countered : favoredArguments) {
            // Look at all options of attacking one of the favored arguments
            // Collect all that are also part of winning strategy
            for (TreeArgument child : countered.getChildren()) {
                if (child.isInWinningStrategy && child.getState()==0) goodMoves.add(child);
            }
        }
        if (goodMoves.size() > 0) {
            // Perform a random good move - as all are in winning strategy, all will lead us to victory
            TreeArgument selectedArgument =
                    goodMoves.get(rand.nextInt(goodMoves.size()));
            moveArgument(selectedArgument);
        } else {
            //
            // No favored moves - check for stalling moves within winning strategy
            ArrayList<TreeArgument> stallingArguments = gameTree.getRoot().getOfStateAndLayer(2, !isProTurn);
            for (TreeArgument countered : stallingArguments) {
                for (TreeArgument child : countered.getChildren()) {
                    if (child.isInWinningStrategy && child.getState()==0) goodMoves.add(child);
                }
            }
            if (goodMoves.size() > 0) {
                // Perform a random good move - as all are in winning strategy, all will lead us to victory
                TreeArgument selectedArgument =
                        goodMoves.get(rand.nextInt(goodMoves.size()));
                moveArgument(selectedArgument);
            } else {
                // No moves we want to make - pass turn if able
                if (baseRuleset) {
                    moveArgument(null);
                } else {
                    // No move in winning strategy possible - the game is lost, computer will perform a random move
                    ArrayList<TreeArgument> counterableArguments = gameTree.getRoot().getOfLayer(!isProTurn);
                    for (TreeArgument countered : counterableArguments) {
                        for (TreeArgument child : countered.getChildren()) {
                            if (child.getState()==0) goodMoves.add(child);
                        }
                    }
                    if (goodMoves.size()==0) {
                        // No moves possible at all - pass
                        moveArgument(null);
                    } else {
                        // Move a random unoptimal move
                        TreeArgument selectedArgument =
                                goodMoves.get(rand.nextInt(goodMoves.size()));
                        moveArgument(selectedArgument);
                    }
                }
            }
        }
    }

    // The current player attempts to pass
    // Check if the attempt is valid - if not, display a message
    private void passAttempt() {
        ArrayList<TreeArgument> possibleMoves = new ArrayList<>();
        ArrayList<TreeArgument> counterableArguments = gameTree.getRoot().getOfLayer(!isProTurn);
        for (TreeArgument countered : counterableArguments) {
            for (TreeArgument child : countered.getChildren()) {
                if (child.getState()==0) possibleMoves.add(child);
            }
        }
        if (possibleMoves.size()==0) {
            // Pass is allowed - so pass
            moveArgument(null);
        } else {
            // There are still possible moves
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pass not possible");
            alert.setHeaderText("There are still possible moves!");
            alert.setContentText("You may only pass when no moves are possible, under the current ruleset!");
            alert.setHeight(400);
            alert.show();

        }
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
        gameTree.exitGameMode();

        // Return all enabled look
        gameTree.visualEnableAll();
        frameworkGraph.visualEnableAll();
    }
}
