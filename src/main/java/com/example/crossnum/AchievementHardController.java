package com.example.crossnum;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class AchievementHardController {

    @FXML private Group star1;
    @FXML private Group star2;
    @FXML private Group star3;
    @FXML private Button mainMenuButton;
    @FXML private Button nextButton;


    //it determines the number of stars the player can have
    public void setStats(int secondsLeft, int timeTaken) {

        int stars;
        if (secondsLeft > 8 * 60) stars = 3;
        else if (secondsLeft > 4 * 60) stars = 2;
        else if( secondsLeft >= 1 *60) stars= 1;
        else stars = 0;

        // Dim stars that weren't earned
        List<Group> allStars = List.of(star1, star2, star3);
        for (int i = 0; i < allStars.size(); i++) {
            if (i >= stars) {
                allStars.get(i).setOpacity(0.25); // dimmed
            }
        }
    }

    @FXML
    private void onMainMenu(ActionEvent event) {
        try {
            FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("start_page.fxml"));
            Stage stage = (Stage) mainMenuButton.getScene().getWindow();
            Parent root = backbuttonLoader.load();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onNext(ActionEvent event) {
        try {
            GameState.getInstance().hasSavedState= false;
            FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("hard_page.fxml"));
            Stage stage = (Stage) nextButton.getScene().getWindow();
            Parent root = backbuttonLoader.load();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}