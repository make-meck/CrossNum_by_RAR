package com.example.crossnum;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class AchievementEasyController {

        @FXML
        private Group star1;
        @FXML private Group star2;
        @FXML private Group star3;
        @FXML private Button mainMenuButton;
        @FXML private Button easyNextButton;
        @FXML private Button mediumNextButton;
        @FXML private Button retryButton;
        @FXML private Button mediumRetryButton;
        @FXML private Label remarkLabel;


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
        //Animation for the Stars and the label
        private void playStarStampAnimation(List<Group> stars, int earnedStars) {
            for (int i = 0; i < stars.size(); i++) {
                Group star = stars.get(i);
                boolean earned = i < earnedStars;

                star.setScaleX(0);
                star.setScaleY(0);
                star.setOpacity(0);


                PauseTransition delay = new PauseTransition(Duration.millis(i * 2000));
                delay.setOnFinished(e -> {
                    playSound("star_shine.wav");

                    PauseTransition restoreMusic = new PauseTransition(Duration.millis(800));
                    restoreMusic.setOnFinished(ev -> SettingsController.restoreAudio());
                    restoreMusic.play();
                } ); // ← plays on each star stamp

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

        //it determines the number of stars the player can have
        public void setStars(int lives) {

            int stars;
            if (lives == 3) stars = 3;
            else if (lives == 2 ) stars = 2;
            else stars = 1;

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
        private void onEasyNext(ActionEvent event) {
            try {
                GameState.getInstance().hasEasySavedState= false;
                GameState.getInstance().EasySavedTheme = new Random().nextInt(7);
                FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("easy_page.fxml"));
                Stage stage = (Stage) easyNextButton.getScene().getWindow();
                Parent root = backbuttonLoader.load();
                stage.getScene().setRoot(root);
                SettingsController.setupGlobalClickSounds(stage.getScene());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @FXML
        private void onMediumNext(ActionEvent event) {
            try {
                GameState.getInstance().hasMediumSavedState= false;
                GameState.getInstance().MediumSavedTheme= new Random().nextInt(7);
                FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("medium_page.fxml"));
                Stage stage = (Stage) mediumNextButton.getScene().getWindow();
                Parent root = backbuttonLoader.load();
                stage.getScene().setRoot(root);
                SettingsController.setupGlobalClickSounds(stage.getScene());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @FXML
        private void onRetry (ActionEvent event) {
            GameState state = GameState.getInstance();
            // indicates that the user failed and still has the saved puzzle
            state.hasEasySavedState = true;

            try {
                FXMLLoader retryLoader = new FXMLLoader(getClass().getResource("easy_page.fxml"));
                Stage stage = (Stage) retryButton.getScene().getWindow();
                Parent root = retryLoader.load();
                stage.getScene().setRoot(root);
                SettingsController.setupGlobalClickSounds(stage.getScene());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @FXML
        private void onMediumRetry (ActionEvent event) {
            GameState state = GameState.getInstance();
            // indicates that the user failed and still has the saved puzzle
            state.hasMediumSavedState = true;

            try {
                FXMLLoader retryLoader = new FXMLLoader(getClass().getResource("medium_page.fxml"));
                Stage stage = (Stage) mediumRetryButton.getScene().getWindow();
                Parent root = retryLoader.load();
                stage.getScene().setRoot(root);
                SettingsController.setupGlobalClickSounds(stage.getScene());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

