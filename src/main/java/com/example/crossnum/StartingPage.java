package com.example.crossnum;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;

public class StartingPage {

    @FXML
    private Button play_button;

    @FXML
    private Button help_button;

    @FXML
    private Rectangle bg_shadow;

    @FXML
    private Rectangle level_container;

    @FXML
    private Button easy_button;

    @FXML
    private Button medium_button;

    @FXML
    private Button hard_button;

    @FXML
    private void playClick() {
        bg_shadow.setVisible(true);
        level_container.setVisible(true);
        easy_button.setVisible(true);
        medium_button.setVisible(true);
        hard_button.setVisible(true);
    }
}
