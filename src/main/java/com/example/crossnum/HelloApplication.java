package com.example.crossnum;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("start_page.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

        scene.getStylesheets().add(
                getClass().getResource("style.css").toExternalForm()
        );

        SettingsController.initMusic();

        stage.setTitle("CROSSNUM");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}