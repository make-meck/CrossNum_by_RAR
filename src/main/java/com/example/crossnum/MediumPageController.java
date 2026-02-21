package com.example.crossnum;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class MediumPageController {
    @FXML
    Button hint;
    @FXML
    Button eraser;
    @FXML
    Button pen;
    @FXML
    Circle toggleCircle;
    @FXML
    ImageView penImage;
    @FXML
    ImageView eraserImage;
    @FXML
    Button back;
    @FXML
    Button restart;
    private boolean penMode = true;
    @FXML private Button backbuttonMedium;

    @FXML
    private void backbutton(ActionEvent event) {
        try {
            FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("start_page.fxml"));
            Stage stage = (Stage) backbuttonMedium.getScene().getWindow();
            Parent root = backbuttonLoader.load();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Image blackEraser =
            new Image(getClass().getResource("eraser.png").toExternalForm());
    private final Image whiteEraser =
            new Image(getClass().getResource("white_eraser.png").toExternalForm());
    private final Image whitePen =
            new Image(getClass().getResource("white_pen.png").toExternalForm());
    private final Image blackPen =
            new Image(getClass().getResource("pen.png").toExternalForm());



    @FXML
    protected void onHintClick(){
    }

    @FXML
    private void onPenClick() {
        penMode = true;
        updateToggle();
        System.out.println("PEN");
    }

    @FXML
    private void onEraserClick() {
        penMode = false;
        updateToggle();
        System.out.println("ERASER");
    }

    private void updateToggle() {
        if (penMode) {
            toggleCircle.setTranslateX(75);
            penImage.setImage(blackPen);
            eraserImage.setImage(whiteEraser);
        } else {
            toggleCircle.setTranslateX(0);
            eraserImage.setImage(blackEraser);
            penImage.setImage(whitePen);
        }
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), toggleCircle);
        tt.setToX(penMode ? 75 : 0);
        tt.play();
    }
}

