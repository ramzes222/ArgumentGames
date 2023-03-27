package com.example.argumentgames;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main-view.fxml")));
            Scene scene = new Scene(root);
            stage.setTitle("Argument Games v1.0");
            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/BuildTree.png")));
            stage.getIcons().add(appIcon);

            stage.setScene(scene);
            stage.show();
            stage.setMaximized(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}