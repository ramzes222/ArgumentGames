package com.example.argumentgames;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class SettingsController {

    private Stage settingsStage;
    MainController mainController;
    @FXML
    CheckBox savePositionToFile, playAgainstComputer;
    @FXML
    ColorPicker attackControlColor, attackArrowColor, argumentBaseColor, selectionColor, proponentArgColor, opponentArgColor,
            attackedArgColor, attackingArgColor;
    @FXML
    Button saveButton, restoreDefaultsButton, cancelButton;

    private HashMap<String, Node> stringToNode = new HashMap<>();
    public SettingsController(MainController mainController) {
        this.mainController = mainController;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("settings-view.fxml"));
            loader.setController(this);
            Parent settingsRoot = loader.load();
            Scene settingsScene = new Scene(settingsRoot, 600, 400);
            this.settingsStage = new Stage();
            settingsStage.setScene(settingsScene);
            settingsStage.setTitle("Settings");
            // Load saved settings
            loadFromFile();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {

        saveButton.setOnAction(e -> {saveToFile(); settingsStage.hide(); mainController.loadSettings();});
        restoreDefaultsButton.setOnAction(e -> restoreDefaults());
        cancelButton.setOnAction(e-> {
            // Check if any changes were made
            if (isCurrentDifferentFromSavedFile()) {
                // Ask to confirm with a dialog
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Cancel changes");
                alert.setHeaderText("Are you sure you want to exit?");
                alert.setContentText("All current changes will be lost!");
                Optional<ButtonType> response = alert.showAndWait();

                if (response.get() == ButtonType.OK) { settingsStage.hide(); }
            } else {settingsStage.hide(); }
        });

        // Setup the HashMap
        stringToNode.put("savePositionToFile", savePositionToFile);
        stringToNode.put("argumentBaseColor", argumentBaseColor);
        stringToNode.put("selectionColor", selectionColor);
        stringToNode.put("attackArrowColor", attackArrowColor);
        stringToNode.put("attackControlColor", attackControlColor);
        stringToNode.put("proponentArgColor", proponentArgColor);
        stringToNode.put("opponentArgColor", opponentArgColor);
        stringToNode.put("attackingArgColor", attackingArgColor);
        stringToNode.put("attackedArgColor", attackedArgColor);
    }

    // Returns true if any of the current loaded values is different from the settings file
    private boolean isCurrentDifferentFromSavedFile() {
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
                        ColorPicker setPicker = (ColorPicker) stringToNode.get(words[0]);
                        if (!setPicker.getValue().equals(readColor)) return true;
                    } else {
                        // Read Boolean
                        CheckBox setCheckbox = (CheckBox) stringToNode.get(words[0]);
                        if (setCheckbox.isSelected() != (boolean) Boolean.parseBoolean(words[1])) return true;
                    }
                }
            } catch (Exception exception) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private void loadFromFile() {
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
                        ColorPicker setPicker = (ColorPicker) stringToNode.get(words[0]);
                        setPicker.setValue(readColor);
                    } else {
                        // Read Boolean
                        CheckBox setCheckbox = (CheckBox) stringToNode.get(words[0]);
                        setCheckbox.setSelected(Boolean.parseBoolean(words[1]));
                    }
                }
            } catch (Exception exception) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Cannot read settings file");
                alert.setContentText("The saved settings cannot be read. Restoring default settings... \n\nError: " + exception.toString());
                alert.showAndWait();
                restoreDefaults();
                saveToFile();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cannot read settings file");
            alert.setContentText("The saved settings cannot be read. Restoring default settings...\n\nError: File cannot be read.");
            alert.showAndWait();
            restoreDefaults();
            saveToFile();
        }
    }

    // Saves the current settings to a local file
    private void saveToFile() {
        File settingsFile = new File("settings.txt");
        FileWriter fr = null;
        BufferedWriter bw = null;
        try {
            fr = new FileWriter(settingsFile);
            bw = new BufferedWriter(fr);
            // Start writing Settings
            // savePositionToFile
            bw.write("savePositionToFile=" + savePositionToFile.isSelected() + System.getProperty("line.separator"));
            // playAgainstComputer
            //bw.write("playAgainstComputer=" + playAgainstComputer.isSelected() + System.getProperty("line.separator"));


            // argumentBaseColor
            bw.write("argumentBaseColor=" + argumentBaseColor.getValue().toString() + System.getProperty("line.separator"));
            // selectionColor
            bw.write("selectionColor=" + selectionColor.getValue().toString() + System.getProperty("line.separator"));
            // attackArrowColor
            bw.write("attackArrowColor=" + attackArrowColor.getValue().toString() + System.getProperty("line.separator"));
            // attackControlColor
            bw.write("attackControlColor=" + attackControlColor.getValue().toString() + System.getProperty("line.separator"));
            // proponentArgColor
            bw.write("proponentArgColor=" + proponentArgColor.getValue().toString() + System.getProperty("line.separator"));
            // opponentArgColor
            bw.write("opponentArgColor=" + opponentArgColor.getValue().toString() + System.getProperty("line.separator"));
            // attackingArgColor
            bw.write("attackingArgColor=" + attackingArgColor.getValue().toString() + System.getProperty("line.separator"));
            // attackedArgColor
            bw.write("attackedArgColor=" + attackedArgColor.getValue().toString() + System.getProperty("line.separator"));
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

    private void restoreDefaults() {
        savePositionToFile.setSelected(true);

        argumentBaseColor.setValue(Color.CORNSILK);
        selectionColor.setValue(Color.YELLOW);
        attackArrowColor.setValue(Color.FORESTGREEN);
        attackControlColor.setValue(Color.ALICEBLUE);
        proponentArgColor.setValue(Color.CORNSILK);
        opponentArgColor.setValue(Color.MOCCASIN);
        attackingArgColor.setValue(Color.LIGHTCORAL);
        attackedArgColor.setValue(Color.PEACHPUFF);
    }

    public void showWindow() {
        settingsStage.show();
    }
}
