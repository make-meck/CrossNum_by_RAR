package com.example.crossnum;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class StartingPage {

    // 1. Inject the StackPane
    @FXML
    private StackPane main_stack;

    @FXML
    private BorderPane root_pane;

    @FXML
    private Button play_button;

    @FXML
    private void playClick() throws IOException {
        FXMLLoader playLoader = new FXMLLoader(getClass().getResource("level_selection.fxml"));
        Parent overlay = playLoader.load();
        main_stack.getChildren().add(overlay);
        StackPane.setAlignment(overlay, Pos.BOTTOM_CENTER);
    }
}