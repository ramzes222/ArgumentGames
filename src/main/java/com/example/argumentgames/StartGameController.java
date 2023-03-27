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
    ChoiceBox gameTypeChoiceBox;
    @FXML
    CheckBox computerPlayerCheckbox;

    public StartGameController(MainController mainController) {
        this.mainController = mainController;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("start-game-view.fxml"));
            loader.setController(this);
            Parent startGameRoot = loader.load();
            Scene startGameScene = new Scene(startGameRoot, 500, 300);
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
            startGameStage.hide();
            mainController.startGame(computerPlayerCheckbox.isSelected(), isGrounded);
        });
        cancelButton.setOnAction(e-> {
            startGameStage.hide();
        });
    }

    public void showWindow() {
        startGameStage.show();
    }
}
