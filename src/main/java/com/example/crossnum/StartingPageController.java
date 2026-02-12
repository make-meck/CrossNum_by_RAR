package com.example.crossnum;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class StartingPageController {

    @FXML private StackPane main_stack;
    @FXML private StackPane level_pane;
    @FXML private Button help_button;
    @FXML private Button backbutton;
    @FXML private StackPane mechanics_pane;
    @FXML private Button mechanics_easybutton;
    @FXML private Button mechanics_mediumbutton;
    @FXML private Button mechanics_hardbutton;

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
    private void handleLevel(String levelFileName) throws IOException {
        FXMLLoader levelLoader = new FXMLLoader(getClass().getResource(levelFileName));
        Stage stage = (Stage) level_pane.getScene().getWindow();
        Parent root = levelLoader.load();
        stage.getScene().setRoot(root);
    }

    @FXML
    private void easyLevel() throws IOException {
        handleLevel("easy_page.fxml");
    }

    @FXML
    private void mediumLevel() throws IOException {
        handleLevel("medium_page.fxml");
    }

    @FXML
    private void hardLevel() throws IOException {
        handleLevel("hard_page.fxml");
    }

    @FXML
    private void howToPlay() throws IOException {
        FXMLLoader helpLoader = new FXMLLoader(getClass().getResource("mechanics_page.fxml"));
        Stage stage = (Stage) help_button.getScene().getWindow();
        Parent root = helpLoader.load();
        stage.getScene().setRoot(root);
    }

    @FXML
    private void backButton() throws IOException {
        FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("start_page.fxml"));
        Stage stage = (Stage) backbutton.getScene().getWindow();
        Parent root = backbuttonLoader.load();
        stage.getScene().setRoot(root);
    }

    @FXML
    private void easyMechanics() throws IOException {
        FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("mechanics_page.fxml"));
        Stage stage = (Stage) mechanics_easybutton.getScene().getWindow();
        Parent root = backbuttonLoader.load();
        stage.getScene().setRoot(root);
    }

    @FXML
    private void mediumMechanics() throws IOException {
        FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("mechanics_medium.fxml"));
        Stage stage = (Stage) mechanics_mediumbutton.getScene().getWindow();
        Parent root = backbuttonLoader.load();
        stage.getScene().setRoot(root);
    }

    @FXML
    private void hardMechanics() throws IOException {
        FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("mechanics_hard.fxml"));
        Stage stage = (Stage) mechanics_hardbutton.getScene().getWindow();
        Parent root = backbuttonLoader.load();
        stage.getScene().setRoot(root);
    }

}