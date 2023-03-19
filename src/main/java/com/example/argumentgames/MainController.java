package com.example.argumentgames;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.ArrayList;

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

    File currentlyUsedFile;
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

    // Clears the current framework and changes the file path
    public void newData() {

    }

    // Saves the current framework to the current file
    public void saveData() {

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
                loadFromCurrentlyUsedFile();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Only .txt files can be read");
                alert.setContentText("Please select a file with a .txt extension!");
                alert.showAndWait();
            }
        }
    }

    private void saveToCurrentlyUsedFile() {
        if ( currentlyUsedFile == null ) return;
        FileWriter fr = null;
        BufferedWriter bw = null;
        try {
            fr = new FileWriter(currentlyUsedFile);
            bw = new BufferedWriter(fr);
            for(int i = 10; i>0; i--){
                bw.write("abd" + System.getProperty("line.separator"));
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
            Framework loadedFramework = new Framework();
            FileReader fr = new FileReader(currentlyUsedFile);
            BufferedReader br = new BufferedReader(fr);
            String nextLine;
            boolean loadingNodes = true;
            while ((nextLine = br.readLine()) != null) {
                if (nextLine.equals("Nodes:")) loadingNodes = true;
                else if (nextLine.equals("Edges:")) loadingNodes = false;
                else {
                    String[] words = nextLine.split(",");
                    if (loadingNodes) {
                        // Interpret the data as a node
                        loadedFramework.addArgument(words[0], Double.parseDouble(words[1]), Double.parseDouble(words[2]));
                    } else {
                        // Interpret the data as an edge
                        loadedFramework.addAttack(words[0], words[1], Double.parseDouble(words[2]), Double.parseDouble(words[3]));
                    }
                }
            }
            frameworkGraph.loadFramework(loadedFramework);
        } catch (Exception exception) {
            System.out.println(exception);
        }

//        // Set up test framework
//        currentFramework = new Framework();
//        currentFramework.addArgument("a");
//        currentFramework.addArgument("b");
//        currentFramework.addArgument("c");
//        currentFramework.addArgument("d");
//        currentFramework.addAttack("a", "b");
//        currentFramework.addAttack("b", "a");
//        currentFramework.addAttack("a", "c");
//        currentFramework.addAttack("b", "c");
//        currentFramework.addAttack("c", "d");

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