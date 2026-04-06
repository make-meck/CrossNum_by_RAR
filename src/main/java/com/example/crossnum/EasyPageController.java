package com.example.crossnum;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class EasyPageController {
    @FXML private BorderPane easyPagePane;
    @FXML private GridPane puzzleGrid;
    @FXML private Label welcomeText;
    @FXML private Button hint, eraser, pen, back, restart;
    @FXML private Circle toggleCircle;
    @FXML private SVGPath penSVG, eraserSVG;
    @FXML private Button backbuttonEasy;
    @FXML private SVGPath heart1;
    @FXML private SVGPath heart2;
    @FXML private SVGPath heart3;
    @FXML private Label totalHints;
    @FXML private Rectangle actionRectangle;
    @FXML private Ellipse easyEllipse;

    private boolean penMode = true;
    private int lives = 3;
    private int cellsResolved = 0; // level success determinant
    private int hints = 3;

    // The puzzle grid (array) that holds the random generated logical data
    private Cell[][] gridData = new Cell[5][5];
    // Arrays to hold the current sums per row and column during a round
    private Label[] currentRowSums = new Label[5];
    private Label[] currentColSums = new Label[5];
    // Arrays to hold the target sums per row and column
    private Label[] targetRowLabels = new Label[5];
    private Label[] targetColLabels = new Label[5];

    private final Image blackEraser = new Image(getClass().getResource("eraser.png").toExternalForm());
    private final Image whiteEraser = new Image(getClass().getResource("white_eraser.png").toExternalForm());
    private final Image whitePen = new Image(getClass().getResource("white_pen.png").toExternalForm());
    private final Image blackPen = new Image(getClass().getResource("pen.png").toExternalForm());

    // inner class that holds the logical state of each grid/box
    // for puzzle numbers, boolean masking values, or puzzle state (if already solved or not)
    public class Cell {
        int value;
        boolean isSolution;
        boolean isResolved = false;
    }
    record GameTheme(
            String name,
            String blackCell,
            String whiteCell,
            String pageBackground,
            String labelText,
            String buttonBase
    ) {}

    private static final List<GameTheme> THEMES = List.of(
            new GameTheme("Forest",   "#2d532c", "#ffffff", "#d0e8d0", "#ffffff", "#3a7a39"),
            new GameTheme("Ocean",   "#1a3a5c", "#e8f4fd", "#81A6C6", "#cce7ff", "#1e5080"),
            new GameTheme("Sunset",  "#7a2d00", "#fff3e0", "#FF8C00", "#ffd8a8", "#b84500"),
            new GameTheme("Amethyst","#3d1a6e", "#f3eaff", "#B95E82", "#dbb8ff", "#6a2fbf"),
            new GameTheme("Slate",   "#2e3f50", "#ecf0f1", "#BFC9D1", "#bdc3c7", "#3d5166"),
            new GameTheme("Royal", "#4B0082", "#FFFFFF", "#FF7F00","#FFFFFF", "#FF007F" ),
            new GameTheme("Powerpuff", "#FF3E9B", "#F6FFDC",  "#66D0BC", "#FFFFFF", "#FFEABB")
    );

    private int themeIndex = 0;

    @FXML
    public void initialize() {
        toggleCircle.setTranslateX(75);
        penSVG.setStroke(Color.BLACK);
        eraserSVG.setStroke(Color.WHITE);
        // penMode is initialized as true so set the UI as penMode too
        //Once the player has opened the easy mode, the saved game state will display in the screen
        GameState state = GameState.getInstance();
        themeIndex = GameState.getInstance().EasySavedTheme;
        if(state.hasEasySavedState) {

            if (state.easyRetryMode) {
                for(int r = 0; r<=4; r++){
                    for (int c= 0; c<=4; c++){
                        gridData[r][c] = new Cell(); //It will create new cell objects at each position, without this, cells are null and could cause a crash
                    }
                }

                // After intializing the cells, it will restore the cells data from saved state
                for(int r =1; r<= 4; r++){  //loops rows 1 to 4 and skips row 0, since it is used for getting the sums
                    for(int c= 1; c<=4 ; c++){  //same as the previous line
                        gridData[r][c].value = state.easyCellValues[r][c]; //restores the number
                        gridData[r][c].isSolution = state.easyCellIsSolution[r][c]; //restore if it is part fo the solution
                    }
                }

                //Recalculates the sum of the rows
                for (int r = 1; r <= 4; r++) { //it loops each row
                    int rowSum = 0;
                    for (int c = 1; c <= 4; c++) { //loop each colum in this row
                        if (gridData[r][c].isSolution) //if the cell is a solution
                            rowSum += gridData[r][c].value; //the value of the cell will be added to the row sum
                    }
                    gridData[r][0] = new Cell(); //creates a new cells at column 0 (the green sum box)
                    gridData[r][0].value = rowSum; //it stores the calculated sum in the box
                }
                //recalculates the sum of the columns
                for (int c = 1; c <= 4; c++) { //loop each column
                    int colSum = 0;
                    for (int r = 1; r <= 4; r++) { //loop each row in this column
                        if (gridData[r][c].isSolution) colSum += gridData[r][c].value; //if the cell is a part of the solution and add its value col sum
                    }
                    gridData[0][c] = new Cell(); //creates a new cells at row 0
                    gridData[0][c].value = colSum; //store the calculated sum in the cell

                }

                //this will return the hearts
                if (lives <= 2) heart1.setStyle("-fx-fill:#808080");
                if (lives <= 1) heart2.setStyle("-fx-fill:#808080");
                if (lives <= 0) heart3.setStyle("-fx-fill:#808080");

            }

            else {
                //Restores the saved state, such as the lives and hints
                lives = state.easyLives; //The saved lives will be restored
                hints = state.easyHints; // the number of hints available will be restored
                cellsResolved = state.easyCellsResolved; //this will determine how many cells have been restored
                totalHints.setText(String.valueOf(hints));

                //First, it will intialize all the cells first
                for (int r = 0; r <= 4; r++) {
                    for (int c = 0; c <= 4; c++) {
                        gridData[r][c] = new Cell(); //It will create new cell objects at each position, without this, cells are null and could cause a crash
                    }
                }

                // After intializing the cells, it will restore the cells data from saved state
                for (int r = 1; r <= 4; r++) {  //loops rows 1 to 4 and skips row 0, since it is used for getting the sums
                    for (int c = 1; c <= 4; c++) {  //same as the previous line
                        gridData[r][c].value = state.easyCellValues[r][c]; //restores the number
                        gridData[r][c].isSolution = state.easyCellIsSolution[r][c]; //restore if it is part fo the solution
                        gridData[r][c].isResolved = state.easyCellIsResolved[r][c]; //restores if player has already solved it
                    }
                }

                //Recalculates the sum of the rows
                for (int r = 1; r <= 4; r++) { //it loops each row
                    int rowSum = 0;
                    for (int c = 1; c <= 4; c++) { //loop each colum in this row
                        if (gridData[r][c].isSolution) //if the cell is a solution
                            rowSum += gridData[r][c].value; //the value of the cell will be added to the row sum
                    }
                    gridData[r][0] = new Cell(); //creates a new cells at column 0 (the green sum box)
                    gridData[r][0].value = rowSum; //it stores the calculated sum in the box
                }
                //recalculates the sum of the columns
                for (int c = 1; c <= 4; c++) { //loop each column
                    int colSum = 0;
                    for (int r = 1; r <= 4; r++) { //loop each row in this column
                        if (gridData[r][c].isSolution)
                            colSum += gridData[r][c].value; //if the cell is a part of the solution and add its value col sum
                    }
                    gridData[0][c] = new Cell(); //creates a new cells at row 0
                    gridData[0][c].value = colSum; //store the calculated sum in the cell

                }

                //this will return the hearts
                if (lives <= 2) heart1.setStyle("-fx-fill:#808080");
                if (lives <= 1) heart2.setStyle("-fx-fill:#808080");
                if (lives <= 0) heart3.setStyle("-fx-fill:#808080");
            }

        }
        else{
            generatePuzzle();
        }

        populateGridUI();
        applyTheme();
    }

    private void generatePuzzle() {
        Random rand = new Random();
        boolean hasZeroSum;

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

        do {
            hasZeroSum = false; // Resets the flag for each puzzle generation attempt

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

                // If a row sum is 0, set hasZeroSum as true so the puzzle regenerates
                if (rowSum == 0) {
                    hasZeroSum = true;
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

                // If a column sum is 0, set hasZeroSum as true so the puzzle regenerates
                if (colSum == 0) {
                    hasZeroSum = true;
                }

                gridData[0][c] = new Cell(); // The zero on first index indicates the leftmost green box
                gridData[0][c].value = colSum;
            }

        } while (hasZeroSum); // Keep generating puzzle to prevent zero sums
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

                // This clear the existing children before adding new ones, this is important in restoring data
                pane.getChildren().clear();

                // style for displaying numbers
                Label label = new Label(String.valueOf(gridData[row][col].value));
                label.setFont(Font.font("Arial", FontWeight.BOLD, 25));

                // Populate the green boxes with the row sums and column sums from gridData array
                if (row == 0 || col == 0) {
                    label.setTextFill(Color.WHITE);
                    pane.getChildren().add(label);

                    // Store the label in the array
                    if (col == 0) targetRowLabels[row] = label;
                    if (row == 0) targetColLabels[col] = label;

                    // for displaying the current sums based on the encircled numbers per row and column
                    // style of how it'll be displayed
                    Label currentSumLabel = new Label("0");
                    currentSumLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    currentSumLabel.setTextFill(Color.web("#e0e0e0"));
                    StackPane.setAlignment(currentSumLabel, Pos.TOP_LEFT);
                    StackPane.setMargin(currentSumLabel, new Insets(3));
                    // the actual population
                    pane.getChildren().add(currentSumLabel);

                    // Store references to update the sum
                    if (col == 0) currentRowSums[row] = currentSumLabel;
                    if (row == 0) currentColSums[col] = currentSumLabel;
                }
                // Populate the green boxes with the numbers stored in gridData array
                else {
                    label.setTextFill(Color.BLACK);
                    pane.getChildren().add(label);


                    //restores visual state if cell was already resolved
                    if(gridData[row][col].isResolved){
                        if(gridData[row][col].isSolution){
                            drawCircle(pane, "Normal");
                        }
                        else{
                            label.setText("");
                        }
                    }
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
                SettingsController.playCorrectSound();
                label.setTextFill(Color.web("#00bf63"));
                drawCircle(pane, "Normal");
                cell.isResolved = true;
                cellsResolved++;
                updateRunningSums();
                checkWinCondition();
            }
            // The lives will decrease if the wrong box is clicked
            else {
                label.setTextFill(Color.web("#c82121"));
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
                label.setTextFill(Color.web("#c82121"));
                deductLife();
            }
        }
    }

    // The method that adds circle when using pen mode
    private void drawCircle(StackPane pane, String mode) {

        Circle circle = new Circle(25);
        circle.setFill(Color.TRANSPARENT);

        if (mode == "Hint") circle.setStroke(Color.web("#f1dd2b"));
        else circle.setStroke(Color.web("#00bf63"));

        circle.setStrokeWidth(4);

        circle.setOpacity(0);
        circle.setScaleX(0.5);
        circle.setScaleY(0.5);

        pane.getChildren().add(circle);
        FadeTransition fade = new FadeTransition(Duration.millis(200), circle);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(150), circle);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.play();

    }

    // This method calculates and updates the current sums per row and column
    private void updateRunningSums() {
        // for row current sums
        for (int r = 1; r <= 4; r++) {
            int currentSum = 0;
            for (int c = 1; c <= 4; c++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution) {
                    currentSum += gridData[r][c].value;
                }
            }
            if (currentRowSums[r] != null) {
                currentRowSums[r].setText(String.valueOf(currentSum));
                // will change the color when the target sum is matched
                if (currentSum == gridData[r][0].value && currentSum != 0) {
                    currentRowSums[r].setTextFill(Color.web("#00bf63"));
                    targetRowLabels[r].setTextFill(Color.web("#00bf63"));
                } else {
                    currentRowSums[r].setText(String.valueOf(currentSum));
                    currentRowSums[r].setTextFill(Color.web("#e0e0e0"));
                    targetRowLabels[r].setTextFill(Color.WHITE);
                }
            }
        }

        // for current column sums
        for (int c = 1; c <= 4; c++) {
            int currentSum = 0;
            for (int r = 1; r <= 4; r++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution) {
                    currentSum += gridData[r][c].value;
                }
            }
            if (currentColSums[c] != null) {
                currentColSums[c].setText(String.valueOf(currentSum));
                // will change the color when the target sum is matched
                if (currentSum == gridData[0][c].value && currentSum != 0) {
                    currentColSums[c].setTextFill(Color.web("#00bf63"));
                    targetColLabels[c].setTextFill(Color.web("#00bf63"));
                } else {
                    currentColSums[c].setText(String.valueOf(currentSum));
                    currentColSums[c].setTextFill(Color.web("#e0e0e0"));
                    targetColLabels[c].setTextFill(Color.WHITE);
                }
            }
        }
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
            saveGameToState();
            try {
                FXMLLoader levelSuccessLoader = new FXMLLoader(getClass().getResource("level_failed.fxml"));
                Stage stage = (Stage) puzzleGrid.getScene().getWindow();
                Parent root = levelSuccessLoader.load();
                stage.getScene().setRoot(root);
                SettingsController.setupGlobalClickSounds(stage.getScene());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Game Over!");
        }
    }

    // Will check if all numbers with true value are encircled and all numbers with false value are erased
    private void checkWinCondition() {
        if (cellsResolved == 16) {
            saveGameToState();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("level_accomplishment.fxml"));
                Parent root = loader.load();

                AchievementEasyController ac = loader.getController();
                ac.setStars(lives);

                Stage stage = (Stage) backbuttonEasy.getScene().getWindow();
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
        TranslateTransition move = new TranslateTransition(Duration.millis(100), toggleCircle);

        if (penMode) {
            move.setToX(75);

            StrokeTransition penStroke = new StrokeTransition(Duration.millis(200), penSVG, Color.WHITE, Color.BLACK);
            StrokeTransition eraserStroke = new StrokeTransition(Duration.millis(200), eraserSVG, Color.BLACK, Color.WHITE);

            penStroke.play();
            eraserStroke.play();

        } else {
            move.setToX(0);

            StrokeTransition penStroke = new StrokeTransition(Duration.millis(200), penSVG, Color.BLACK, Color.WHITE);
            StrokeTransition eraserStroke = new StrokeTransition(Duration.millis(200), eraserSVG, Color.WHITE, Color.BLACK);

            penStroke.play();
            eraserStroke.play();
        }

        move.play();
    }

    @FXML
    protected void onHintClick() {
        // Checks number of hints. If there's no hint left, return. Otherwise, decrement by one.
        if (hints <= 0) return;
        hints--;
        totalHints.setText(String.valueOf(hints)); // Displays the updated number of hints

        // Gathers all the unresolved cells.
        java.util.List<int[]> unresolvedCells = new java.util.ArrayList<>();
        for (int r = 1; r <= 4; r++) {
            for (int c = 1; c <= 4; c++) {
                if (!gridData[r][c].isResolved) {
                    unresolvedCells.add(new int[]{r, c}); // Creates an array holding two values --> the indices of the unresolved cell
                }
            }
        }

        // Exit if all cells are already resolved
        if (unresolvedCells.isEmpty()) return;

        // Chooses a random cell from the unresolvedCells list
        Random rand = new Random();
        int[] chosen = unresolvedCells.get(rand.nextInt(unresolvedCells.size()));
        int row = chosen[0], col = chosen[1];
        Cell cell = gridData[row][col];

        // Loops over the grid cells and find the cell pane that matches the random hint cell's indices
        for (Node node : puzzleGrid.getChildren()) {
            if (node instanceof StackPane pane) {
                Integer r = GridPane.getRowIndex(pane);
                Integer c = GridPane.getColumnIndex(pane);
                int paneRow = (r == null) ? 0 : r;
                int paneCol = (c == null) ? 0 : c;

                if (paneRow == row && paneCol == col) {
                    Label label = null;
                    for (Node child : pane.getChildren()) {
                        if (child instanceof Label) {
                            label = (Label) child;
                            break;
                        }
                    }

                    if (cell.isSolution) {
                        drawCircle(pane, "Hint");
                    } else {
                        if (label != null) label.setText("");
                    }

                    cell.isResolved = true;
                    cellsResolved++;
                    updateRunningSums();
                    checkWinCondition();
                    break;
                }
            }
        }
    }

    @FXML
    private void onRestartClick() {
        // Lives will also be reset
        lives = 3;
        cellsResolved = 0;

        heart1.setStyle("-fx-fill: #c82121;");
        heart2.setStyle("-fx-fill: #c82121;");
        heart3.setStyle("-fx-fill: #c82121;");

        // Resets boxes' logic
        for (Node node : puzzleGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane pane = (StackPane) node;
                Integer r = GridPane.getRowIndex(pane);
                Integer c = GridPane.getColumnIndex(pane);
                int row = (r == null) ? 0 : r;
                int col = (c == null) ? 0 : c;

                // lop-left empty
                if (row == 0 && col == 0) continue;

                // reset white boxes' logical data
                if (row > 0 && col > 0) {
                    gridData[row][col].isResolved = false;
                }

                // erases everything contained in the grid
                pane.getChildren().clear();
            }
        }

        populateGridUI();
        applyTheme();
    }

    @FXML
    private void backbutton(ActionEvent event) {
        //This will save the state of the game
        GameState state = GameState.getInstance();
        state.easyLives= lives;
        state.easyHints = hints;
        state.easyCellsResolved = cellsResolved;
        state.hasEasySavedState= true;
        state.easyRetryMode = false;

        //This will save the data in the grid

        for(int r = 1; r<=4; r++){
            for(int c = 1; c<=4; c++){
                state.easyCellValues[r][c] = gridData[r][c].value;
                state.easyCellIsSolution[r][c]= gridData[r][c].isSolution;
                state.easyCellIsResolved[r][c] = gridData[r][c].isResolved;
            }
        }
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

    // Helper method to save the current logical data of the round
    private void saveGameToState() {
        GameState state = GameState.getInstance();
        state.hasEasySavedState = true;
        state.easyRetryMode = true;

        for (int r = 1; r <= 4; r++) {
            for (int c = 1; c <= 4; c++) {
                state.easyCellValues[r][c] = gridData[r][c].value;
                state.easyCellIsSolution[r][c] = gridData[r][c].isSolution;
                state.easyCellIsResolved[r][c] = gridData[r][c].isResolved;
            }
        }
    }

    //Applying themes in the easy page

    private void applyTheme() {
        GameTheme t = THEMES.get(themeIndex);

        // Page background
        easyPagePane.setStyle("-fx-background-color: " + t.pageBackground() + ";");

        // Back and restart buttons (keep their radius)
        if (backbuttonEasy != null) backbuttonEasy.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 40;");
        if (restart != null)        restart.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 40;");
        if (hint != null)           hint.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 35;");

        //ellipse and rectangle
        javafx.scene.paint.Color accent = javafx.scene.paint.Color.web(t.blackCell());
        if (easyEllipse != null) easyEllipse.setFill(accent);
        if(actionRectangle != null) actionRectangle.setFill(accent);


        // Pen and eraser stay transparent
        if (pen != null)   pen.setStyle("-fx-background-color: transparent;");
        if (eraser != null) eraser.setStyle("-fx-background-color: transparent;");

        // Labels
        if (welcomeText != null) welcomeText.setStyle("-fx-text-fill: " + t.labelText() + ";");

        // Grid cells
        for (Node node : puzzleGrid.getChildren()) {
            if (node instanceof StackPane pane) {
                Integer r = GridPane.getRowIndex(pane);
                Integer c = GridPane.getColumnIndex(pane);
                int row = (r == null) ? 0 : r;
                int col = (c == null) ? 0 : c;

                if (row == 0 && col == 0) continue;

                if (row == 0 || col == 0) {
                    pane.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 10;");
                } else {
                    pane.setStyle("-fx-background-color: " + t.whiteCell() + "; -fx-background-radius: 10;");
                }
            }
        }
    }
}