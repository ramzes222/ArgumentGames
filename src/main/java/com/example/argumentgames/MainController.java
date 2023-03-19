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

    // Save the current framework to currently used file
    public void saveToCurrentlyUsedFile() {
        if ( currentlyUsedFile == null ) {
            // No file loaded- prompt user to Save As
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No file is loaded!");
            alert.setContentText("Please select a file to save to first!");
            alert.showAndWait();
            saveAsFileData();
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
            while ((nextLine = br.readLine()) != null) {
                if (nextLine.equals("Nodes:")) loadingNodes = true;
                else if (nextLine.equals("Edges:")) loadingNodes = false;
                else {
                    String[] words = nextLine.split(",");
                    if (loadingNodes) {
                        // Interpret the data as a node
                        currentFramework.addArgument(words[0], Double.parseDouble(words[1]), Double.parseDouble(words[2]));
                    } else {
                        // Interpret the data as an edge
                        currentFramework.addAttack(words[0], words[1], Double.parseDouble(words[2]), Double.parseDouble(words[3]));
                    }
                }
            }
            frameworkGraph.loadFramework(currentFramework);
        } catch (Exception exception) {
            System.out.println(exception);
        }

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