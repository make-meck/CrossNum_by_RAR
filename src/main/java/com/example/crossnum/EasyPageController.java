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
    @FXML private Label totalErasures;
    @FXML private Rectangle actionRectangle;
    @FXML private Ellipse easyEllipse;

    private boolean penMode = true;
    private int lives = 3;
    private int cellsResolved = 0;
    private int hints = 3;
    private int erasures = 0;

    private Cell[][] gridData = new Cell[5][5];
    private Label[] currentRowSums = new Label[5];
    private Label[] currentColSums = new Label[5];
    private Label[] targetRowLabels = new Label[5];
    private Label[] targetColLabels = new Label[5];

    private final Image blackEraser = new Image(getClass().getResource("eraser.png").toExternalForm());
    private final Image whiteEraser = new Image(getClass().getResource("white_eraser.png").toExternalForm());
    private final Image whitePen = new Image(getClass().getResource("white_pen.png").toExternalForm());
    private final Image blackPen = new Image(getClass().getResource("pen.png").toExternalForm());

    public class Cell {
        int value;
        boolean isSolution;
        boolean isResolved = false;
        boolean wasHinted = false;
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

        GameState state = GameState.getInstance();
        themeIndex = GameState.getInstance().EasySavedTheme;

        if (state.hasEasySavedState) {

            if (state.easyRetryMode) {
                for (int r = 0; r <= 4; r++) {
                    for (int c = 0; c <= 4; c++) {
                        gridData[r][c] = new Cell();
                    }
                }

                for (int r = 1; r <= 4; r++) {
                    for (int c = 1; c <= 4; c++) {
                        gridData[r][c].value = state.easyCellValues[r][c];
                        gridData[r][c].isSolution = state.easyCellIsSolution[r][c];
                    }
                }

                for (int r = 1; r <= 4; r++) {
                    int rowSum = 0;
                    for (int c = 1; c <= 4; c++) {
                        if (gridData[r][c].isSolution) rowSum += gridData[r][c].value;
                    }
                    gridData[r][0] = new Cell();
                    gridData[r][0].value = rowSum;
                }

                for (int c = 1; c <= 4; c++) {
                    int colSum = 0;
                    for (int r = 1; r <= 4; r++) {
                        if (gridData[r][c].isSolution) colSum += gridData[r][c].value;
                    }
                    gridData[0][c] = new Cell();
                    gridData[0][c].value = colSum;
                }

                // In retry mode all cells are unresolved, so erasures = set to original possible erasure
                erasures = countErasures();

                if (lives <= 2) heart1.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
                if (lives <= 1) heart2.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
                if (lives <= 0) heart3.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");

            } else {
                // Restore saved state — initialize gridData FIRST, then compute from it
                lives = state.easyLives;
                hints = state.easyHints;
                cellsResolved = state.easyCellsResolved;
                totalHints.setText(String.valueOf(hints));

                // Step 1: initialize all cells
                for (int r = 0; r <= 4; r++) {
                    for (int c = 0; c <= 4; c++) {
                        gridData[r][c] = new Cell();
                    }
                }

                // Step 2: restore cell data from saved state
                for (int r = 1; r <= 4; r++) {
                    for (int c = 1; c <= 4; c++) {
                        gridData[r][c].value = state.easyCellValues[r][c];
                        gridData[r][c].isSolution = state.easyCellIsSolution[r][c];
                        gridData[r][c].isResolved = state.easyCellIsResolved[r][c];
                        gridData[r][c].wasHinted = state.easyCellWasHinted[r][c];
                    }
                }

                // Step 3: recalculate row/col sums
                for (int r = 1; r <= 4; r++) {
                    int rowSum = 0;
                    for (int c = 1; c <= 4; c++) {
                        if (gridData[r][c].isSolution) rowSum += gridData[r][c].value;
                    }
                    gridData[r][0] = new Cell();
                    gridData[r][0].value = rowSum;
                }

                for (int c = 1; c <= 4; c++) {
                    int colSum = 0;
                    for (int r = 1; r <= 4; r++) {
                        if (gridData[r][c].isSolution) colSum += gridData[r][c].value;
                    }
                    gridData[0][c] = new Cell();
                    gridData[0][c].value = colSum;
                }

                // Step 4: recompute remaining erasures now that gridData is fully populated
                int erasuresDone = 0;
                for (int rr = 1; rr <= 4; rr++)
                    for (int cc = 1; cc <= 4; cc++)
                        if (!gridData[rr][cc].isSolution && gridData[rr][cc].isResolved) erasuresDone++;
                erasures = countErasures() - erasuresDone;

                if (lives <= 2) heart1.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
                if (lives <= 1) heart2.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
                if (lives <= 0) heart3.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
            }

        } else {
            generatePuzzle();
            erasures = countErasures();   // counts number of possible erasures
        }

        totalErasures.setText(String.valueOf(erasures));

        populateGridUI();
        applyTheme();

        // save state of the running sums for when returning from the menu
        if (state.hasEasySavedState && !state.easyRetryMode) {
            restoreRunningSums();
        }
    }

    // counts all the false cells or possible erasure per round
    private int countErasures() {
        int count = 0;
        for (int r = 1; r <= 4; r++) {
            for (int c = 1; c <= 4; c++) {
                if (!gridData[r][c].isSolution) count++;
            }
        }
        return count;
    }

    private void generatePuzzle() {
        Random rand = new Random();
        boolean hasZeroSum;

        do {
            hasZeroSum = false;

            for (int r = 1; r <= 4; r++) {
                for (int c = 1; c <= 4; c++) {
                    gridData[r][c] = new Cell();
                    gridData[r][c].value = rand.nextInt(9) + 1;
                    gridData[r][c].isSolution = rand.nextBoolean();
                }
            }

            for (int r = 1; r <= 4; r++) {
                int rowSum = 0;
                for (int c = 1; c <= 4; c++) {
                    if (gridData[r][c].isSolution) rowSum += gridData[r][c].value;
                }
                if (rowSum == 0) hasZeroSum = true;
                gridData[r][0] = new Cell();
                gridData[r][0].value = rowSum;
            }

            for (int c = 1; c <= 4; c++) {
                int colSum = 0;
                for (int r = 1; r <= 4; r++) {
                    if (gridData[r][c].isSolution) colSum += gridData[r][c].value;
                }
                if (colSum == 0) hasZeroSum = true;
                gridData[0][c] = new Cell();
                gridData[0][c].value = colSum;
            }

        } while (hasZeroSum);
    }

    private void populateGridUI() {
        for (Node node : puzzleGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane pane = (StackPane) node;

                Integer r = GridPane.getRowIndex(pane);
                Integer c = GridPane.getColumnIndex(pane);
                int row = (r == null) ? 0 : r;
                int col = (c == null) ? 0 : c;

                if (row == 0 && col == 0) continue;

                pane.getChildren().clear();

                Label label = new Label(String.valueOf(gridData[row][col].value));
                label.setFont(Font.font("Arial", FontWeight.BOLD, 25));

                if (row == 0 || col == 0) {
                    label.setTextFill(Color.WHITE);
                    pane.getChildren().add(label);

                    if (col == 0) targetRowLabels[row] = label;
                    if (row == 0) targetColLabels[col] = label;

                    Label currentSumLabel = new Label("0");
                    currentSumLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    currentSumLabel.setTextFill(Color.web("#e0e0e0"));
                    StackPane.setAlignment(currentSumLabel, Pos.TOP_LEFT);
                    StackPane.setMargin(currentSumLabel, new Insets(3));
                    pane.getChildren().add(currentSumLabel);

                    if (col == 0) currentRowSums[row] = currentSumLabel;
                    if (row == 0) currentColSums[col] = currentSumLabel;

                } else {
                    label.setTextFill(Color.BLACK);
                    pane.getChildren().add(label);

                    // FIX 3: use wasHinted to restore the correct circle color
                    if (gridData[row][col].isResolved) {
                        if (gridData[row][col].isSolution) {
                            String circleMode = gridData[row][col].wasHinted ? "Hint" : "Normal";
                            drawCircle(pane, circleMode);
                        } else {
                            label.setText("");
                        }
                    }

                    int finalRow = row;
                    int finalCol = col;
                    pane.setOnMouseClicked(e -> handleCellClick(pane, label, finalRow, finalCol));
                }
            }
        }
    }

    // restores running sums when going back from the menu
    private void restoreRunningSums() {
        for (int r = 1; r <= 4; r++) {
            int currentSum = 0;
            for (int c = 1; c <= 4; c++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution) {
                    currentSum += gridData[r][c].value;
                }
            }
            if (currentRowSums[r] != null) {
                currentRowSums[r].setText(String.valueOf(currentSum));
                if (currentSum == gridData[r][0].value && currentSum != 0) {
                    currentRowSums[r].setTextFill(Color.web("#00bf63"));
                    targetRowLabels[r].setTextFill(Color.web("#00bf63"));
                } else {
                    currentRowSums[r].setTextFill(Color.web("#e0e0e0"));
                    targetRowLabels[r].setTextFill(Color.WHITE);
                }
            }
        }

        for (int c = 1; c <= 4; c++) {
            int currentSum = 0;
            for (int r = 1; r <= 4; r++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution) {
                    currentSum += gridData[r][c].value;
                }
            }
            if (currentColSums[c] != null) {
                currentColSums[c].setText(String.valueOf(currentSum));
                if (currentSum == gridData[0][c].value && currentSum != 0) {
                    currentColSums[c].setTextFill(Color.web("#00bf63"));
                    targetColLabels[c].setTextFill(Color.web("#00bf63"));
                } else {
                    currentColSums[c].setTextFill(Color.web("#e0e0e0"));
                    targetColLabels[c].setTextFill(Color.WHITE);
                }
            }
        }
    }

    private void handleCellClick(StackPane pane, Label label, int row, int col) {
        Cell cell = gridData[row][col];

        if (cell.isResolved || lives <= 0) return;

        if (penMode) {
            if (cell.isSolution) {
                SettingsController.playCorrectSound();
                label.setTextFill(Color.web("#00bf63"));
                drawCircle(pane, "Normal");
                cell.isResolved = true;
                cellsResolved++;
                updateRunningSums();
                checkWinCondition();
            } else {
                label.setTextFill(Color.web("#c82121"));
                deductLife();
            }
        } else {
            // checks erasures before allowing erasing function
            if (!cell.isSolution) {
                if (erasures <= 0) return;   // no erasures left will block action
                erasures--;
                totalErasures.setText(String.valueOf(erasures)); // update display

                SettingsController.playEraseSound();
                animateErase(label);
                cell.isResolved = true;
                cellsResolved++;
                checkWinCondition();
            } else {
                label.setTextFill(Color.web("#c82121"));
                deductLife();
            }
        }
    }

    private void drawCircle(StackPane pane, String mode) {
        Circle circle = new Circle(25);
        circle.setFill(Color.TRANSPARENT);

        if ("Hint".equals(mode)) circle.setStroke(Color.web("#f1dd2b"));
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

    private void updateRunningSums() {
        for (int r = 1; r <= 4; r++) {
            int currentSum = 0;
            for (int c = 1; c <= 4; c++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution) {
                    currentSum += gridData[r][c].value;
                }
            }
            if (currentRowSums[r] != null) {
                currentRowSums[r].setText(String.valueOf(currentSum));
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

        for (int c = 1; c <= 4; c++) {
            int currentSum = 0;
            for (int r = 1; r <= 4; r++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution) {
                    currentSum += gridData[r][c].value;
                }
            }
            if (currentColSums[c] != null) {
                currentColSums[c].setText(String.valueOf(currentSum));
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

    private void deductLife() {
        lives--;

        if (lives == 2) {
            animateHeartLoss(heart1);
        } else if (lives == 1) {
            animateHeartLoss(heart2);
        } else if (lives == 0) {
            animateHeartLoss(heart3);
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
        }
    }

    private void animateHeartLoss(SVGPath heart) {
        TranslateTransition fall = new TranslateTransition(Duration.millis(300), heart);
        fall.setByY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(300), heart);
        fade.setToValue(0);

        ParallelTransition animation = new ParallelTransition(fall, fade);

        animation.setOnFinished(e -> {
            heart.setStyle("-fx-fill: #c31515;");
            heart.setOpacity(0);
            heart.setTranslateY(0);
        });

        animation.play();
    }

    private void animateErase(Label label) {
        FadeTransition fade = new FadeTransition(Duration.millis(170), label);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(170), label);
        scale.setToX(0.7);
        scale.setToY(0.7);

        ParallelTransition animation = new ParallelTransition(fade, scale);

        animation.setOnFinished(e -> {
            label.setText("");
            label.setOpacity(1);
            label.setScaleX(1);
            label.setScaleY(1);
        });

        animation.play();
    }

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
        if (hints <= 0) return;
        hints--;
        totalHints.setText(String.valueOf(hints));

        java.util.List<int[]> unresolvedCells = new java.util.ArrayList<>();
        for (int r = 1; r <= 4; r++) {
            for (int c = 1; c <= 4; c++) {
                if (!gridData[r][c].isResolved) {
                    unresolvedCells.add(new int[]{r, c});
                }
            }
        }

        if (unresolvedCells.isEmpty()) return;

        Random rand = new Random();
        int[] chosen = unresolvedCells.get(rand.nextInt(unresolvedCells.size()));
        int row = chosen[0], col = chosen[1];
        Cell cell = gridData[row][col];

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
                        cell.wasHinted = true;
                    } else {
                        if (label != null) animateErase(label);
                        erasures--;
                        totalErasures.setText(String.valueOf(erasures));
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
        lives = 3;
        cellsResolved = 0;
        hints = 3;
        totalHints.setText(String.valueOf(hints));
        erasures = countErasures();
        totalErasures.setText(String.valueOf(erasures));

        heart1.setStyle("-fx-fill: #c82121;");
        heart1.setOpacity(1);
        heart1.setTranslateY(0);

        heart2.setStyle("-fx-fill: #c82121;");
        heart2.setOpacity(1);
        heart2.setTranslateY(0);

        heart3.setStyle("-fx-fill: #c82121;");
        heart3.setOpacity(1);
        heart3.setTranslateY(0);

        for (Node node : puzzleGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane pane = (StackPane) node;
                Integer r = GridPane.getRowIndex(pane);
                Integer c = GridPane.getColumnIndex(pane);
                int row = (r == null) ? 0 : r;
                int col = (c == null) ? 0 : c;

                if (row == 0 && col == 0) continue;

                if (row > 0 && col > 0) {
                    gridData[row][col].isResolved = false;
                    gridData[row][col].wasHinted = false;
                }

                pane.getChildren().clear();
            }
        }

        populateGridUI();
        applyTheme();
    }

    @FXML
    private void backbutton(ActionEvent event) {
        GameState state = GameState.getInstance();
        state.easyLives = lives;
        state.easyHints = hints;
        state.easyCellsResolved = cellsResolved;
        state.hasEasySavedState = true;
        state.easyRetryMode = false;

        for (int r = 1; r <= 4; r++) {
            for (int c = 1; c <= 4; c++) {
                state.easyCellValues[r][c] = gridData[r][c].value;
                state.easyCellIsSolution[r][c] = gridData[r][c].isSolution;
                state.easyCellIsResolved[r][c] = gridData[r][c].isResolved;
                state.easyCellWasHinted[r][c] = gridData[r][c].wasHinted;
            }
        }

        try {
            FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("start_page.fxml"));
            Stage stage = (Stage) backbuttonEasy.getScene().getWindow();
            Parent root = backbuttonLoader.load();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveGameToState() {
        GameState state = GameState.getInstance();
        state.hasEasySavedState = true;
        state.easyRetryMode = true;

        for (int r = 1; r <= 4; r++) {
            for (int c = 1; c <= 4; c++) {
                state.easyCellValues[r][c] = gridData[r][c].value;
                state.easyCellIsSolution[r][c] = gridData[r][c].isSolution;
                state.easyCellIsResolved[r][c] = gridData[r][c].isResolved;
                state.easyCellWasHinted[r][c] = gridData[r][c].wasHinted;
            }
        }
    }

    private void applyTheme() {
        GameTheme t = THEMES.get(themeIndex);

        easyPagePane.setStyle("-fx-background-color: " + t.pageBackground() + ";");

        if (backbuttonEasy != null) backbuttonEasy.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 40;");
        if (restart != null)        restart.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 40;");
        if (hint != null)           hint.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 35;");

        javafx.scene.paint.Color accent = javafx.scene.paint.Color.web(t.blackCell());
        if (easyEllipse != null)     easyEllipse.setFill(accent);
        if (actionRectangle != null) actionRectangle.setFill(accent);

        if (pen != null)    pen.setStyle("-fx-background-color: transparent;");
        if (eraser != null) eraser.setStyle("-fx-background-color: transparent;");

        if (welcomeText != null) welcomeText.setStyle("-fx-text-fill: " + t.labelText() + ";");

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