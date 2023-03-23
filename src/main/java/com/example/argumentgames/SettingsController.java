package com.example.argumentgames;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Set;

public class SettingsController {

    Stage settingsStage;
    MainController mainController;
    @FXML
    CheckBox savePositionToFile;
    @FXML
    ColorPicker attackControlColor, attackArrowColor, argumentBaseColor, selectionColor, proponentArgColor, opponentArgColor,
            attackedArgColor, attackingArgColor;

    public SettingsController(MainController mainController) {
        this.mainController = mainController;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("settings-view.fxml"));
            loader.setController(this);
            Parent settingsRoot = loader.load();
            Scene settingsScene = new Scene(settingsRoot, 600, 400);
            settingsStage = new Stage();
            settingsStage.setScene(settingsScene);
            settingsStage.setTitle("Settings");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
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
