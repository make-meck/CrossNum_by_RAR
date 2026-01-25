package com.example.crossnum;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class StartingPage {

    @FXML private StackPane main_stack;
    @FXML private BorderPane root_pane;
    @FXML private Button play_button;
    @FXML private Button help_button;
    @FXML private Button easy_button;
    @FXML private Button medium_button;
    @FXML private Button hard_button;

    @FXML
    private void playClick() throws IOException {
        FXMLLoader playLoader = new FXMLLoader(getClass().getResource("level_selection.fxml"));
        Parent overlay = playLoader.load();

        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                main_stack.getChildren().remove(overlay);
            }
        });

        main_stack.getChildren().add(overlay);
        StackPane.setAlignment(overlay, Pos.BOTTOM_CENTER);
    }

    @FXML
    private void howToPlay() throws IOException {
        FXMLLoader helpLoader = new FXMLLoader(getClass().getResource("mechanics_page.fxml"));
        Stage stage = (Stage) help_button.getScene().getWindow();
        Parent root = helpLoader.load();
        stage.getScene().setRoot(root);
    }
}