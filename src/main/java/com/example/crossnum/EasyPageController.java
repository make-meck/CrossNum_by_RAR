package com.example.crossnum;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Random;

public class EasyPageController {

    @FXML private GridPane puzzleGrid;
    @FXML private Label welcomeText;
    @FXML private Button hint, eraser, pen, back, restart;
    @FXML private Circle toggleCircle;
    @FXML private ImageView penImage, eraserImage;
    @FXML private Button backbuttonEasy;
    @FXML private SVGPath heart1;
    @FXML private SVGPath heart2;
    @FXML private SVGPath heart3;

    private boolean penMode = true;
    private int lives = 3;
    private int cellsResolved = 0; // level success determinant

    // The puzzle grid (array) that holds the random generated logical data
    private Cell[][] gridData = new Cell[5][5];

    private final Image blackEraser = new Image(getClass().getResource("eraser.png").toExternalForm());
    private final Image whiteEraser = new Image(getClass().getResource("white_eraser.png").toExternalForm());
    private final Image whitePen = new Image(getClass().getResource("white_pen.png").toExternalForm());
    private final Image blackPen = new Image(getClass().getResource("pen.png").toExternalForm());

    // Inner class that holds the logical state of each grid/box
    // for puzzle numbers, boolean masking values, or puzzle state (if already solved or not)
    private class Cell {
        int value;
        boolean isSolution;
        boolean isResolved = false;
    }

    @FXML
    public void initialize() {
        // penMode is initialized as true so set the UI as penMode too
        toggleCircle.setTranslateX(75);
        penImage.setImage(blackPen);
        eraserImage.setImage(whiteEraser);

        generatePuzzle();
        populateGridUI();
    }

    private void generatePuzzle() {
        Random rand = new Random();

        /*
            Forward Propagation with Boolean Masking Algorithm:
            1. With forward propagation algorithm, it will first generate random numbers that will be displayed
               on the gridPane. By gridPane, it is the white boxes only and not yet the green box with the
               sums per row and column. Consider it as the first layer of the grid.
            2. Boolean masking will be done: it is like flipping a coin per grid (randomly) to determine
               its boolean value (true/false). Consider it the invisible second layer on top of the first.
            3. Then, sums will be determined per row and column. The code will first calculate the row sums
               based on the boolean value determined per grid and number. A true value will retrieve the number
               from the first layer and add it to another determined true value in the same row. While false
               value will be ignored and will not be added in the calculated row sum. The same calculation will
               be done per column. After every row calculation and column calculation, the computed sum and then
               be displayed in the green box.
        */

        // (1)(2) Loop that will generate random numbers and boolean values (per indices) and store it in the array.
        for (int r = 1; r <= 4; r++) {
            for (int c = 1; c <= 4; c++) {
                gridData[r][c] = new Cell(); // Constructor
                gridData[r][c].value = rand.nextInt(9) + 1; // random numbers from 1 to 9
                gridData[r][c].isSolution = rand.nextBoolean(); // random boolean values (true/false)
            }
        }

        // (3) Loop that will calculate the row sums based on boolean mask
        // true will add, false will not add
        for (int r = 1; r <= 4; r++) {
            int rowSum = 0;
            for (int c = 1; c <= 4; c++) {
                if (gridData[r][c].isSolution) rowSum += gridData[r][c].value;
            }
            gridData[r][0] = new Cell(); // The zero on second index indicates the green box above
            gridData[r][0].value = rowSum;
        }

        // (3) Loop that will calculate the column sums based on boolean mask
        // true will add, false will not add
        for (int c = 1; c <= 4; c++) {
            int colSum = 0;
            for (int r = 1; r <= 4; r++) {
                if (gridData[r][c].isSolution) colSum += gridData[r][c].value;
            }
            gridData[0][c] = new Cell(); // The zero on first index indicates the leftmost green box
            gridData[0][c].value = colSum;
        }
    }

    private void populateGridUI() {
        // Loop through the GridPane's children to populate with the logical data generated
        for (Node node : puzzleGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane pane = (StackPane) node;

                // Get row and column indices
                Integer r = GridPane.getRowIndex(pane);
                Integer c = GridPane.getColumnIndex(pane);
                int row = (r == null) ? 0 : r; // as GridPane returns null for index 0 sometimes
                int col = (c == null) ? 0 : c;

                // No UI and data for the top-left corner
                if (row == 0 && col == 0) continue; // [0,0]

                // style for displaying numbers
                Label label = new Label(String.valueOf(gridData[row][col].value));
                label.setFont(Font.font("Arial", FontWeight.BOLD, 25));

                // Populate the green boxes with the row sums and column sums from gridData array
                if (row == 0 || col == 0) {
                    label.setTextFill(Color.WHITE);
                    pane.getChildren().add(label);
                }
                // Populate the green boxes with the numbers stored in gridData array
                else {
                    label.setTextFill(Color.BLACK);
                    pane.getChildren().add(label);

                    // passes arguments every box clicked for pen and eraser gameplay
                    int finalRow = row;
                    int finalCol = col;
                    pane.setOnMouseClicked(e -> handleCellClick(pane, label, finalRow, finalCol));
                }
            }
        }
    }

    private void handleCellClick(StackPane pane, Label label, int row, int col) {
        Cell cell = gridData[row][col];

        if (cell.isResolved || lives <= 0) return;

        if (penMode) {
            // Using pen
            // The pane will contain circle if correct box is clicked
            if (cell.isSolution) {
                drawCircle(pane);
                cell.isResolved = true;
                cellsResolved++;
                checkWinCondition();
            }
            // The lives will decrease if the wrong box is clicked
            else {
                deductLife();
            }
        }
        else {
            // Using eraser
            if (!cell.isSolution) {
                // The pane will erase the number if the correct box is clicked
                label.setText(""); // replaces the number with space to remove it
                cell.isResolved = true;
                cellsResolved++;
                checkWinCondition();
            }
            // The lives will decrease if the wrong box is clicked
            else {
                deductLife();
            }
        }
    }

    // The method that adds circle when using pen mode
    private void drawCircle(StackPane pane) {
        Circle circle = new Circle(25);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#365d35"));
        circle.setStrokeWidth(4);
        pane.getChildren().add(circle);
    }

    // Handles the UI of hearts that will serve as lives per round
    private void deductLife() {
        lives--;

        if (lives == 2) {
            heart1.setStyle("-fx-fill: #808080;");
        } else if (lives == 1) {
            heart2.setStyle("-fx-fill: #808080;");
        } else if (lives == 0) {
            heart3.setStyle("-fx-fill: #808080;");
        }

        if (lives <= 0) {
            System.out.println("Game Over!");
        }
    }

    // Will check if all numbers with true value are encircled and all numbers with false value are erased
    private void checkWinCondition() {
        if (cellsResolved == 16) {
            try {
                FXMLLoader levelSuccessLoader = new FXMLLoader(getClass().getResource("level_accomplishment.fxml"));
                Stage stage = (Stage) puzzleGrid.getScene().getWindow();
                Parent root = levelSuccessLoader.load();
                stage.getScene().setRoot(root);
                SettingsController.setupGlobalClickSounds(stage.getScene());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onPenClick() {
        penMode = true;
        updateToggle();
    }

    @FXML
    private void onEraserClick() {
        penMode = false;
        updateToggle();
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

    @FXML
    protected void onHintClick() {
        // Optional: Add hint logic here later
    }

    @FXML
    private void backbutton(ActionEvent event) {
        try {
            FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("start_page.fxml"));
            Stage stage = (Stage) backbuttonEasy.getScene().getWindow();
            Parent root = backbuttonLoader.load();
            stage.getScene().setRoot(root);
            // SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}