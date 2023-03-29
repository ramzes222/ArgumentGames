package com.example.argumentgames;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;


public class StartGameController {
    MainController mainController;
    private Stage startGameStage;

    @FXML
    Button startGameButton, cancelButton;
    @FXML
    ChoiceBox gameTypeChoiceBox, rulesetChoiceBox;
    @FXML
    CheckBox computerPlayerCheckbox;

    public StartGameController(MainController mainController) {
        this.mainController = mainController;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("start-game-view.fxml"));
            loader.setController(this);
            Parent startGameRoot = loader.load();
            Scene startGameScene = new Scene(startGameRoot, 350, 400);
            this.startGameStage = new Stage();
            startGameStage.setScene(startGameScene);
            startGameStage.setTitle("Start Game");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        startGameButton.setOnAction(e -> {
            boolean isGrounded = gameTypeChoiceBox.getValue().equals("Grounded");
            boolean isBaseRuleset = rulesetChoiceBox.getValue().equals("Base");
            startGameStage.hide();
            mainController.startGame(computerPlayerCheckbox.isSelected(), isGrounded, isBaseRuleset);
        });
        cancelButton.setOnAction(e-> {
            startGameStage.hide();
        });
    }

    public void showWindow() {
        startGameStage.show();
    }
}
