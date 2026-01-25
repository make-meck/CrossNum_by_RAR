package com.example.crossnum;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

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
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onHintClick(){
    }
    @FXML
    protected void onEraserClick(){
    }
    @FXML
    protected void onPenClick(){
    }
}