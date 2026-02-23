package com.example.crossnum;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.*;

public class HardPageController {
    @FXML final Map<String,TextField> fieldMap = new LinkedHashMap<>();
    @FXML private Button backbuttonHard;
    @FXML private Button hint;
    @FXML private Label timerLabel;
    @FXML private Timeline timer;
    @FXML private int secondsLeft = 15*60;
    @FXML private TextField tf_c1r1, tf_c2r1, tf_c3r1, tf_c4r1,  tf_c5r1; //all the textfields in row 1
    @FXML private TextField tf_c1r2, tf_c2r2, tf_c3r2, tf_c4r2, tf_c5r2, tf_c6r2; //all the textfields in the 2nd row
    @FXML private TextField tf_c1r3, tf_c2r3, tf_c5r3, tf_c6r3; // textffields in 3rd row
    @FXML private TextField tf_c1r4, tf_c2r4, tf_c5r4, tf_c6r4; //textfields in fourth row
    @FXML private TextField tf_c1r5, tf_c2r5, tf_c3r5, tf_c4r5, tf_c5r5, tf_c6r5; //textfields in 5th row
    @FXML private TextField tf_c2r6, tf_c3r6, tf_c4r6, tf_c5r6, tf_c6r6; //textfields in 6th row


    @FXML
    private void initialize(){
        buildFieldMap();
        startTimer();

    }
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
    // this method is used to make the timer works
    @FXML
    private void startTimer(){
        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    secondsLeft --;
                    int minutes =secondsLeft/60;
                    int seconds = secondsLeft % 60;
                    timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                })
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void buildFieldMap() {
        // row 1
        fieldMap.put("1,1", tf_c1r1); fieldMap.put("2,1", tf_c2r1);
        fieldMap.put("3,1", tf_c3r1); fieldMap.put("4,1", tf_c4r1);
        fieldMap.put("5,1", tf_c5r1);
        // row 2
        fieldMap.put("1,2", tf_c1r2); fieldMap.put("2,2", tf_c2r2);
        fieldMap.put("3,2", tf_c3r2); fieldMap.put("4,2", tf_c4r2);
        fieldMap.put("5,2", tf_c5r2); fieldMap.put("6,2", tf_c6r2);
        // row 3
        fieldMap.put("1,3", tf_c1r3); fieldMap.put("2,3", tf_c2r3);
        fieldMap.put("5,3", tf_c5r3); fieldMap.put("6,3", tf_c6r3);
        // row 4
        fieldMap.put("1,4", tf_c1r4); fieldMap.put("2,4", tf_c2r4);
        fieldMap.put("5,4", tf_c5r4); fieldMap.put("6,4", tf_c6r4);
        // row 5
        fieldMap.put("1,5", tf_c1r5); fieldMap.put("2,5", tf_c2r5);
        fieldMap.put("3,5", tf_c3r5); fieldMap.put("4,5", tf_c4r5);
        fieldMap.put("5,5", tf_c5r5); fieldMap.put("6,5", tf_c6r5);
        // row 6
        fieldMap.put("2,6", tf_c2r6); fieldMap.put("3,6", tf_c3r6);
        fieldMap.put("4,6", tf_c4r6); fieldMap.put("5,6", tf_c5r6);
        fieldMap.put("6,6", tf_c6r6);
    }
}