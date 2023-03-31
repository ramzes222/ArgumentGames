package com.example.argumentgames;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public class MainController {
    @FXML
    RadioButton leftSelectButton, leftMoveButton, leftPanButton, rightSelectButton, rightMoveButton, rightPanButton;
    @FXML
    Button leftAddNodeButton, leftAddEdgeButton, leftCleanupButton, gameButton, leftDeleteButton, passButton;
    @FXML
    Pane leftGraphPane, rightGraphPane;
    @FXML
    Label gameLabel;

    @FXML
    ImageView gameButtonImageView;
    @FXML
    Menu fileMenu, editMenu;
    @FXML
    MenuItem prefGameTreeButton, groundGameTreeButton, turnToDungButton;

    Framework currentFramework;

    Graph frameworkGraph;
    TreeGraph gameTree;

    GameController gc;
    boolean gameInProgress;
    private final HashMap<String, Color> colorLookup = new HashMap<>();
    private final HashMap<String, Boolean> booleanLookup = new HashMap<>();
    File currentlyUsedFile;

    public MainController() {
    }

    public void initialize() {
        // Set up the two Graphs
        currentFramework = new Framework();
        frameworkGraph = new Graph(leftGraphPane, leftSelectButton, leftMoveButton, leftPanButton, leftAddNodeButton, leftAddEdgeButton, leftDeleteButton, leftCleanupButton, colorLookup, booleanLookup);
        frameworkGraph.loadFramework(currentFramework);
        gameTree = new TreeGraph(rightGraphPane, rightSelectButton, rightMoveButton, rightPanButton, colorLookup);

        // Construct the menu Bar
        gameLabel.prefWidthProperty().bind(rightGraphPane.widthProperty());
        gameLabel.toFront();

        // Load the settings
        restoreDefaults();
        loadSettings();

        // Set menu item functionality
        prefGameTreeButton.setOnAction(e-> buildTreeFromFramework(false));
        groundGameTreeButton.setOnAction(e-> buildTreeFromFramework(true));
    }

    public void loadSettings() {
        File settingsFile = new File("settings.txt");
        if ( settingsFile.exists() && settingsFile.canRead() ) {
            // Load the file
            try {
                FileReader fr = new FileReader(settingsFile);
                BufferedReader br = new BufferedReader(fr);
                String nextLine;
                while ((nextLine = br.readLine()) != null) {
                    String[] words = nextLine.split("=");
                    if (words[1].length() == 10) {
                        // Read color
                        Color readColor = Color.valueOf(words[1]);
                        String key = words[0];
                        colorLookup.put(key, readColor);
                    } else {
                        // Read Boolean
                        Boolean readBool = Boolean.valueOf(words[1]);
                        String key = words[0];
                        booleanLookup.put(key, readBool);
                    }
                }
            } catch (Exception exception) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Cannot read settings file");
                alert.setContentText("The saved settings cannot be read. Using default settings... \n\nError: " + exception);
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cannot read settings file");
            alert.setContentText("The saved settings cannot be read. Using default settings...\n\nError: File cannot be read.");
            alert.showAndWait();
        }
        frameworkGraph.reloadColors();
        gameTree.reloadColors();
    }

    private void restoreDefaults() {
        booleanLookup.put("savePositionToFile",true);
        booleanLookup.put("allowMetaArguments",false);

        colorLookup.put("argumentBaseColor", Color.CORNSILK);
        colorLookup.put("selectionColor", Color.YELLOW);
        colorLookup.put("attackArrowColor", Color.FORESTGREEN);
        colorLookup.put("attackControlColor", Color.ALICEBLUE);
        colorLookup.put("proponentArgColor", Color.CORNSILK);
        colorLookup.put("opponentArgColor", Color.MOCCASIN);
        colorLookup.put("attackingArgColor", Color.LIGHTCORAL);
        colorLookup.put("attackedArgColor", Color.PEACHPUFF);
        colorLookup.put("computerSelectableColor", Color.ORANGERED);
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
        boolean savePos = booleanLookup.get("savePositionToFile");
        if ( currentlyUsedFile == null ) {
            // No file loaded - prompt user to Save As
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No file is loaded!");
            alert.setContentText("Please select a file to save to first!");
            alert.showAndWait();
            saveAsFileData();
            return;
        }
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
                    arg.prefX = Math.round(c.getLayoutX());
                    arg.prefY = Math.round(c.getLayoutY()); }
                if (savePos) {
                    bw.write(arg.getName() + "," + arg.prefX + "," + arg.prefY + System.getProperty("line.separator"));
                } else {
                    bw.write(arg.getName() + System.getProperty("line.separator"));
                }
            }
            // Write Edges
            bw.write("Edges:" + System.getProperty("line.separator"));
            for (FrameworkAttack att : currentFramework.getAttacks()) {
                GraphArrow a = frameworkGraph.getGArrow(att.getFrom().getName(), att.getTo().getName());
                if (a!=null) {
                    att.prefControlX = Math.round(a.getControlX());
                    att.prefControlY = Math.round(a.getControlY()); }
                if (savePos) {
                    bw.write(att.getFrom().getName() + "," + att.getTo().getName() + "," + att.prefControlX + "," + att.prefControlY + System.getProperty("line.separator"));
                } else {
                    bw.write(att.getFrom().getName() + "," + att.getTo().getName() + System.getProperty("line.separator"));
                }

            }
        } catch (IOException ex){
            ex.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                    fr.close();
                }
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
            System.out.println("Error: " + exception);
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
    public void buildTreeFromFramework(boolean isGrounded) {
        String rootName = frameworkGraph.getSelectedArgumentName();
        if (rootName==null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Please select an argument first");
            alert.setContentText("To generate a game tree, first select an argument. The game will be rooted at that argument");
            alert.showAndWait();
        } else {
            FrameworkArgument rootOfTree = currentFramework.getArgumentByName(rootName);
            TreeArgument gameTreeRoot = currentFramework.buildGameTree(rootOfTree, isGrounded);
            gameTree.buildTree(gameTreeRoot);
        }
    }

    @FXML
    public void gameButtonPress() {
        if (gameInProgress) endGame(); else {
            if (frameworkGraph.getSelectedArgumentName() != null) {
                StartGameController startGameController = new StartGameController(this);
                startGameController.showWindow();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Please select an argument first");
                alert.setContentText("To start an argument game, first select an argument. The game will be rooted at that argument");
                alert.showAndWait();
            }
        }
    }

    // Starts an argument game rooted in the currently selected framework argument.
    // Applies provided arguments to the game.
    public void startGame(boolean isComputerPlaying, boolean isGrounded, boolean isBaseRuleset) {
        if (frameworkGraph.getSelectedArgumentName() != null) {
            buildTreeFromFramework(isGrounded);
            gameInProgress = true;
            Image nextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/End Game.png")));
            gameButtonImageView.setImage(nextIcon);
            gc = new GameController();
            fileMenu.setDisable(true);
            editMenu.setDisable(true);
            gc.startGame(frameworkGraph, currentFramework, gameTree, isGrounded, isComputerPlaying, isBaseRuleset, gameLabel, passButton, colorLookup);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Please select an argument first");
            alert.setContentText("To start an argument game, first select an argument. The game will be rooted at that argument");
            alert.showAndWait();
        }
    }

    // Ends the currently ongoing game
    private void endGame() {
        gameInProgress = false;
        Image nextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/Start Game.png")));
        gameButtonImageView.setImage(nextIcon);
        gc.endGame();
        fileMenu.setDisable(false);
        editMenu.setDisable(false);
        passButton.setVisible(false);
    }

    // Opens the settings window
    @FXML
    private void openSettings() {
        SettingsController settingsController = new SettingsController(this);
        settingsController.showWindow();
    }

    // Modify the framework to turn it into a Dung framework
    @FXML
    private void turnToDung() {
        if (!currentFramework.isMeta()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No changes made");
            alert.setContentText("No meta attacks currently exist, the current framework is already a Dung framework. Nothing was changed.");
            alert.showAndWait();
            return;
        }
        Framework newDung = new Framework();
        newDung.generateFromMeta(currentFramework);
        currentFramework = newDung;
        frameworkGraph.loadFramework(currentFramework);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Success");
        alert.setContentText("Successfully turned the meta framework into a Dung Framework");
        alert.showAndWait();
    }
}