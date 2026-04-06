package com.example.crossnum;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class AchievementHardController {

    @FXML private Group star1;
    @FXML private Group star2;
    @FXML private Group star3;
    @FXML private Button mainMenuButton;
    @FXML private Button nextButton;
    @FXML private Button hardRetryButton;
    @FXML private Label remarkLabel;
    @FXML private Label scoreLabel;


    private void playSound(String filename) {
        try {
            var resource = getClass().getResource("/audio/" + filename);
            if (resource == null) { System.out.println("NOT FOUND"); return; }
            AudioClip clip = new AudioClip(resource.toExternalForm());
            clip.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void playStarStampAnimation(List<Group> stars, int earnedStars) {
        for (int i = 0; i < stars.size(); i++) {
            Group star = stars.get(i);
            boolean earned = i < earnedStars;

            star.setScaleX(0);
            star.setScaleY(0);
            star.setOpacity(0);

            PauseTransition delay = new PauseTransition(Duration.millis(i * 2000));
            delay.setOnFinished(e -> playSound("star_shine.wav")); // ← plays on each star stamp

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), star);
            scaleUp.setFromX(0);
            scaleUp.setFromY(0);
            scaleUp.setToX(1.2);
            scaleUp.setToY(1.2);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), star);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(earned ? 1.0 : 0.25);

            ParallelTransition stamp = new ParallelTransition(scaleUp, fadeIn);

            ScaleTransition settle = new ScaleTransition(Duration.millis(100), star);
            settle.setFromX(1.2);
            settle.setFromY(1.2);
            settle.setToX(1.0);
            settle.setToY(1.0);

            RotateTransition wobble = new RotateTransition(Duration.millis(80), star);
            wobble.setFromAngle(-8);
            wobble.setToAngle(8);
            wobble.setCycleCount(2);
            wobble.setAutoReverse(true);
            wobble.setOnFinished(e -> star.setRotate(0));

            SequentialTransition starAnim;
            if (earned) {
                starAnim = new SequentialTransition(delay, stamp, settle, wobble);
            } else {
                starAnim = new SequentialTransition(delay, stamp, settle);
            }

            starAnim.play();
        }
    }

    private void playLabelStampAnimation(Label label) {
        label.setScaleX(3.0);
        label.setScaleY(3.0);
        label.setOpacity(0);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), label);
        scaleDown.setFromX(3.0);
        scaleDown.setFromY(3.0);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), label);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition bounce = new ScaleTransition(Duration.millis(100), label);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.1);
        bounce.setToY(1.1);

        ScaleTransition settle = new ScaleTransition(Duration.millis(100), label);
        settle.setFromX(1.1);
        settle.setFromY(1.1);
        settle.setToX(1.0);
        settle.setToY(1.0);

        ParallelTransition stamp = new ParallelTransition(scaleDown, fadeIn);
        SequentialTransition full = new SequentialTransition(stamp, bounce, settle);
        full.play();
    }

    //it displays the scroe and the stars the player has accumulated 
    public void setStats(int secondsLeft, int timeTaken, int accuScore) {

        int stars;
        if (secondsLeft > 8 * 60) stars = 3;
        else if (secondsLeft > 4 * 60) stars = 2;
        else if( secondsLeft >= 1 *60) stars= 1;
        else stars = 0;

        // Dim stars that weren't earned
        List<Group> allStars = List.of(star1, star2, star3);
        // animation for the stars
        playStarStampAnimation(allStars, stars);

        PauseTransition labelDelay = new PauseTransition(Duration.millis(allStars.size() * 400));
        labelDelay.setOnFinished(e -> {
            remarkLabel.setText(stars == 3 ? "EXCELLENT" :
                    stars == 2 ? "GREAT JOB" :
                            stars == 1 ? "GOOD EFFORT" : "TRY AGAIN");
            playLabelStampAnimation(remarkLabel); // your label stamp from before
        });
        labelDelay.play();
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + accuScore);
        }
    }

    @FXML
    private void onMainMenu(ActionEvent event) {
        GameState state = GameState.getInstance();
        state.hasSavedState   = false;
        state.hardSolution    = new HashMap<>();
        state.hardFieldValues = new HashMap<>();
        state.hardFieldStyles = new HashMap<>();
        state.secondsLeft     = 15 * 60;
        state.hintsLeft       = 3;
        state.savedScore      = 500;
        state.savedCombo      = 1;
        state.savedLayoutName = null;


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
    private void onNext(ActionEvent event) {
        try {
            GameState.getInstance().hasSavedState= false;
            FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("hard_page_Improved.fxml"));
            Stage stage = (Stage) nextButton.getScene().getWindow();
            Parent root = backbuttonLoader.load();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onHardRetry(ActionEvent event) {
        GameState state = GameState.getInstance();
        state.hasSavedState   = true;
        state.hardFieldValues = new HashMap<>();
        state.hardFieldStyles = new HashMap<>();
        state.secondsLeft     = 15 * 60;
        state.hintsLeft       = 3;
        state.savedScore      = 500;
        state.savedCombo      = 1;
        state.hardPrevLayoutName = null;

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