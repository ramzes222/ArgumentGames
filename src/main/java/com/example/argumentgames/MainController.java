package com.example.argumentgames;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class MainController {
    @FXML
    RadioButton leftSelectButton, leftMoveButton, leftPanButton, rightSelectButton, rightMoveButton, rightPanButton;
    @FXML
    Button leftAddNodeButton, leftAddEdgeButton, leftCleanupButton, rightBuildTreeButton, gameButton, buildTreeButton, leftDeleteButton;
    @FXML
    Pane leftGraphPane, rightGraphPane;
    @FXML
    Label gameLabel;

    @FXML
    ChoiceBox<String> gameTypeChoiceBox;
    @FXML
    ImageView gameButtonImageView;
    @FXML
    Menu fileMenu;

    Framework currentFramework;

    Graph frameworkGraph;
    TreeGraph gameTree;

    GameController gc;
    boolean gameInProgress;

    File currentlyUsedFile;
    public MainController() {
    }

    public void initialize() {
        // Set up the two Graphs
        currentFramework = new Framework();
        frameworkGraph = new Graph(leftGraphPane, leftSelectButton, leftMoveButton, leftPanButton, leftAddNodeButton, leftAddEdgeButton, leftDeleteButton, leftCleanupButton);
        frameworkGraph.loadFramework(currentFramework);
        gameTree = new TreeGraph(rightGraphPane, rightSelectButton, rightMoveButton, rightPanButton, rightBuildTreeButton);

        // Construct the menu Bar
        gameLabel.prefWidthProperty().bind(rightGraphPane.widthProperty());
        gameLabel.toFront();
    }

    // Clears the current framework and changes the file path
    public void newData() {
        // Ask to confirm with a dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New file");
        alert.setHeaderText("Are you sure you want to create a new file?");
        alert.setContentText("You will lose all changes made in the current file!");
        Optional<ButtonType> response = alert.showAndWait();

        if (response.get() == ButtonType.OK){
            FileChooser fc = new FileChooser();
            // Set FileChooser settings
            fc.getExtensionFilters().add( new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            // Set the directory that the dialogue opens to
            File initialDirectory = new File(System.getProperty("user.home"));
            if (!initialDirectory.canRead()) { initialDirectory = new File("c:/"); }
            fc.setInitialDirectory(initialDirectory);

            File selectedFile = fc.showSaveDialog(leftGraphPane.getScene().getWindow());
            if (selectedFile != null && ( selectedFile.exists() == selectedFile.canWrite() )) {
                String extension = selectedFile.getPath().substring( selectedFile.getPath().lastIndexOf(".") + 1 );
                if (extension.equals("txt")) {
                    // Remember the file, then save
                    currentFramework.clear();
                    frameworkGraph.loadFramework(currentFramework);
                    gameTree.clear();
                    currentlyUsedFile = selectedFile;
                    displayCurrentFileAsHeader();
                    saveToCurrentlyUsedFile();
                } else {
                    Alert alert2 = new Alert(Alert.AlertType.ERROR);
                    alert2.setTitle("Files must be saved as .txt");
                    alert2.setContentText("Please select a file with a .txt extension!");
                    alert2.showAndWait();
                }
            }
        }
    }

    // Saves the current framework to a new file
    public void saveAsFileData() {
        FileChooser fc = new FileChooser();
        // Set FileChooser settings
        fc.getExtensionFilters().add( new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        // Set the directory that the dialogue opens to
        File initialDirectory = new File(System.getProperty("user.home"));
        if (!initialDirectory.canRead()) { initialDirectory = new File("c:/"); }
        fc.setInitialDirectory(initialDirectory);

        File selectedFile = fc.showSaveDialog(leftGraphPane.getScene().getWindow());
        if (selectedFile != null && ( selectedFile.exists() == selectedFile.canWrite() )) {
            String extension = selectedFile.getPath().substring( selectedFile.getPath().lastIndexOf(".") + 1 );
            if (extension.equals("txt")) {
                // Save the file
                currentlyUsedFile = selectedFile;
                displayCurrentFileAsHeader();
                saveToCurrentlyUsedFile();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Files must be saved as .txt");
                alert.setContentText("Please select a file with a .txt extension!");
                alert.showAndWait();
            }
        }
    }

    // Loads the framework from a newly selected file
    public void loadData() {
        FileChooser fc = new FileChooser();
        // Set FileChooser settings
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        // Set the directory that the dialogue opens to
        File initialDirectory = new File(System.getProperty("user.home"));
        if (!initialDirectory.canRead()) { initialDirectory = new File("c:/"); }
        fc.setInitialDirectory(initialDirectory);

        File selectedFile = fc.showOpenDialog(leftGraphPane.getScene().getWindow());
        if (selectedFile != null && selectedFile.exists() && selectedFile.canRead() ) {
            String extension = selectedFile.getPath().substring( selectedFile.getPath().lastIndexOf(".") + 1 );
            if (extension.equals("txt")) {
                // Load the file
                currentlyUsedFile = selectedFile;
                displayCurrentFileAsHeader();
                loadFromCurrentlyUsedFile();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Only .txt files can be read");
                alert.setContentText("Please select a file with a .txt extension!");
                alert.showAndWait();
            }
        }
    }

    // Save the current framework to currently used file
    public void saveToCurrentlyUsedFile() {
        if ( currentlyUsedFile == null ) {
            // No file loaded- prompt user to Save As
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No file is loaded!");
            alert.setContentText("Please select a file to save to first!");
            alert.showAndWait();
            saveAsFileData();
            return;
        };
        FileWriter fr = null;
        BufferedWriter bw = null;
        try {
            fr = new FileWriter(currentlyUsedFile);
            bw = new BufferedWriter(fr);
            // Start writing Nodes
            bw.write("Nodes:" + System.getProperty("line.separator"));
            for (FrameworkArgument arg : currentFramework.getArguments()) {
                GraphCircle c = frameworkGraph.getGCircle(arg.getName());
                if (c!=null) {
                    arg.prefX = c.getLayoutX();
                    arg.prefY = c.getLayoutY(); }
                bw.write(arg.getName() + "," + arg.prefX + "," + arg.prefY + System.getProperty("line.separator"));
            }
            // Write Edges
            bw.write("Edges:" + System.getProperty("line.separator"));
            for (FrameworkAttack att : currentFramework.getAttacks()) {
                GraphArrow a = frameworkGraph.getGArrow(att.getFrom().getName(), att.getTo().getName());
                if (a!=null) {
                    att.prefControlX = a.getControlX();
                    att.prefControlY = a.getControlY(); }
                bw.write(att.getFrom().getName() + "," + att.getTo().getName() + "," + att.prefControlX + "," + att.prefControlY + System.getProperty("line.separator"));
            }
        } catch (IOException ex){
            ex.printStackTrace();
        } finally {
            try {
                bw.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFromCurrentlyUsedFile() {
        try {
            currentFramework.clear();
            FileReader fr = new FileReader(currentlyUsedFile);
            BufferedReader br = new BufferedReader(fr);
            String nextLine;
            boolean loadingNodes = true;
            boolean requiresCleanUp = false;
            while ((nextLine = br.readLine()) != null) {
                if (nextLine.equals("Nodes:")) loadingNodes = true;
                else if (nextLine.equals("Edges:")) loadingNodes = false;
                else {
                    String[] words = nextLine.split(",");
                    if (loadingNodes) {
                        // Interpret the data as a node
                        // If the positions are not provided, use default values
                        if (words.length < 3) {
                            currentFramework.addArgument(words[0], 200, 200);
                            requiresCleanUp = true;
                        } else {
                            currentFramework.addArgument(words[0], Double.parseDouble(words[1]), Double.parseDouble(words[2]));
                        }
                    } else {
                        // Interpret the data as an edge
                        // If the positions are not provided, use default values
                        if (words.length < 4) {
                            currentFramework.addAttack(words[0], words[1], 300.0, 300.0);
                            requiresCleanUp = true;
                        } else {
                            currentFramework.addAttack(words[0], words[1], Double.parseDouble(words[2]), Double.parseDouble(words[3]));
                        }
                    }
                }
            }
            frameworkGraph.loadFramework(currentFramework);
            if (requiresCleanUp) frameworkGraph.cleanUp();
        } catch (Exception exception) {
            System.out.println(exception);
        }

    }

    public void displayCurrentFileAsHeader() {
        String fileName = currentlyUsedFile.getPath().substring(
                currentlyUsedFile.getPath().lastIndexOf("\\") + 1 );
        String stageName = "Argument Games v1.0 - " + fileName;
        Stage thisStage = (Stage) leftGraphPane.getScene().getWindow();
        thisStage.setTitle(stageName);
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

    @FXML
    public void gameButtonPress() {
        if (gameInProgress) endGame(); else startGame();
    }

    private void startGame() {
        gameInProgress = true;
        Image nextIcon = new Image(getClass().getResourceAsStream("img/End Game.png"));
        gameButtonImageView.setImage(nextIcon);
        gc = new GameController();
        boolean isGrounded = gameTypeChoiceBox.getValue().equals("Grounded");
        gameTypeChoiceBox.setDisable(true);
        buildTreeButton.setDisable(true);
        fileMenu.setDisable(true);
        gc.startGame(frameworkGraph, currentFramework, gameTree, isGrounded, gameLabel);
    }

    private void endGame() {
        gameInProgress = false;
        Image nextIcon = new Image(getClass().getResourceAsStream("img/Start Game.png"));
        gameButtonImageView.setImage(nextIcon);
        gc.endGame();
        gameTypeChoiceBox.setDisable(false);
        buildTreeButton.setDisable(false);
        fileMenu.setDisable(false);
    }

    @FXML
    private void openSettings() {
        SettingsController settingsController = new SettingsController(this);
        settingsController.showWindow();


    }
}