package com.example.crossnum;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class HardPageController {

    @FXML
    private Button backbuttonHard;
    @FXML
    private Button hint;


    @FXML
    private void backbutton(ActionEvent event) {
        try {
            FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("start_page.fxml"));
            Stage stage = (Stage) backbuttonHard.getScene().getWindow();
            Parent root = backbuttonLoader.load();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    protected void onHintClick() {

    }
}