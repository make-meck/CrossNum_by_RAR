package com.example.crossnum;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class HelloController {
    @FXML
    private Label welcomeText;
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
    private boolean penMode = true;

    private final Image blackEraser =
            new Image(getClass().getResource("eraser.png").toExternalForm());
    private final Image whiteEraser =
            new Image(getClass().getResource("white_eraser.png").toExternalForm());
    private final Image whitePen =
            new Image(getClass().getResource("white_pen.png").toExternalForm());
    private final Image blackPen =
            new Image(getClass().getResource("pen.png").toExternalForm());


    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

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
