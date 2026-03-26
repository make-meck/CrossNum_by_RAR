package com.example.crossnum;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class LevelFailedHardController {

    @FXML private Button mainMenuButton;
    @FXML private Button hardRetryButton;

    @FXML
    private void onMainMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("start_page.fxml"));
            Stage stage = (Stage) mainMenuButton.getScene().getWindow();
            Parent root = loader.load();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onHardRetry(ActionEvent event) {
        GameState state = GameState.getInstance();
        state.hasSavedState = false;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hard_page_Improved.fxml"));
            Stage stage = (Stage) hardRetryButton.getScene().getWindow();
            Parent root = loader.load();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}