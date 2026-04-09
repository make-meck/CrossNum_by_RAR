package com.example.crossnum;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.io.IOException;
import java.util.List;
import java.util.Random;

class Fraction {
    int numerator;
    int denominator;

    Fraction(int numerator, int denominator){
        this.numerator = numerator;
        this.denominator = denominator;
        simplify();
    }

    public Fraction add(Fraction other){
        int newNum = this.numerator * other.denominator + other.numerator * this.denominator;
        int newDen = this.denominator * other.denominator;
        return new Fraction(newNum, newDen);
    }

    private void simplify(){
        int gcd = gcd(numerator, denominator);
        numerator /= gcd;
        denominator /= gcd;
    }

    private int gcd(int a, int b){
        if(b == 0) return Math.abs(a);
        return gcd(b, a % b);
    }

    @Override
    public String toString(){
        if(denominator == 1) return Integer.toString(numerator);
        return numerator + "/" + denominator;
    }
}

public class MediumPageController {
    @FXML private GridPane puzzleGrid;
    @FXML private Circle toggleCircle;
    @FXML private SVGPath penSVG, eraserSVG;
    @FXML private Button backbuttonMedium;
    @FXML private SVGPath heart1;
    @FXML private SVGPath heart2;
    @FXML private SVGPath heart3;
    @FXML private Label totalHints;
    @FXML private Label totalErasures;
    @FXML private BorderPane mediumPagePane;
    @FXML private Ellipse ellipseMed;
    @FXML private Rectangle medAction;
    @FXML private Button restart;
    @FXML private Button hint;
    @FXML private Button pen;
    @FXML private Button eraser;

    private boolean penMode = true;
    private int lives = 3;
    private int cellsResolved = 0;
    private int hints = 3;
    private int erasures = 0;

    private MediumPageController.Cell[][] gridData = new MediumPageController.Cell[7][7];
    private Label[] currentRowSums = new Label[7];
    private Label[] currentColSums = new Label[7];
    private Label[] targetRowLabels = new Label[7];
    private Label[] targetColLabels = new Label[7];

    private final Image blackEraser = new Image(getClass().getResource("eraser.png").toExternalForm());
    private final Image whiteEraser = new Image(getClass().getResource("white_eraser.png").toExternalForm());
    private final Image whitePen = new Image(getClass().getResource("white_pen.png").toExternalForm());
    private final Image blackPen = new Image(getClass().getResource("pen.png").toExternalForm());

    public class Cell {
        Fraction value;
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
            new GameTheme("Powerpuff", "#FF3E9B", "#F6FFDC",  "#66D0BC", "#FFFFFF", "#FFEABB")
    );

    private int themeIndex = 0;

    @FXML
    public void initialize() {
        toggleCircle.setTranslateX(75);
        penSVG.setStroke(Color.BLACK);
        eraserSVG.setStroke(Color.WHITE);

        GameState state = GameState.getInstance();
        themeIndex = GameState.getInstance().getMediumSavedTheme();

        if (state.hasMediumSavedState) {

            if (state.mediumRetryMode) {
                // Step 1: initialize all cells
                for (int r = 0; r <= 6; r++) {
                    for (int c = 0; c <= 6; c++) {
                        gridData[r][c] = new MediumPageController.Cell();
                    }
                }

                // Step 2: restore cell data
                for (int r = 1; r <= 6; r++) {
                    for (int c = 1; c <= 6; c++) {
                        gridData[r][c].value = new Fraction(
                                state.mediumCellValuesNumerator[r][c],
                                state.mediumCellValuesDenominator[r][c]
                        );
                        gridData[r][c].isSolution = state.mediumCellIsSolution[r][c];
                    }
                }

                // Step 3: recalculate row/col sums
                for (int r = 1; r <= 6; r++) {
                    Fraction rowSum = new Fraction(0, 1);
                    for (int c = 1; c <= 6; c++) {
                        if (gridData[r][c].isSolution)
                            rowSum = rowSum.add(gridData[r][c].value);
                    }
                    gridData[r][0] = new MediumPageController.Cell();
                    gridData[r][0].value = rowSum;
                }
                for (int c = 1; c <= 6; c++) {
                    Fraction colSum = new Fraction(0, 1);
                    for (int r = 1; r <= 6; r++) {
                        if (gridData[r][c].isSolution)
                            colSum = colSum.add(gridData[r][c].value);
                    }
                    gridData[0][c] = new MediumPageController.Cell();
                    gridData[0][c].value = colSum;
                }

                // In retry mode all cells are unresolved, so erasures = set to original possible erasure
                erasures = countErasures();

                if (lives <= 2) heart1.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
                if (lives <= 1) heart2.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
                if (lives <= 0) heart3.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");

            } else {
                // Restore saved state — initialize gridData FIRST, then compute from it
                lives = state.mediumLives;
                hints = state.mediumHints;
                cellsResolved = state.mediumCellsResolved;
                totalHints.setText(String.valueOf(hints));

                // Step 1: initialize all cells
                for (int r = 0; r <= 6; r++) {
                    for (int c = 0; c <= 6; c++) {
                        gridData[r][c] = new MediumPageController.Cell();
                    }
                }

                // Step 2: restore cell data from saved state
                for (int r = 1; r <= 6; r++) {
                    for (int c = 1; c <= 6; c++) {
                        gridData[r][c].value = new Fraction(
                                state.mediumCellValuesNumerator[r][c],
                                state.mediumCellValuesDenominator[r][c]
                        );
                        gridData[r][c].isSolution = state.mediumCellIsSolution[r][c];
                        gridData[r][c].isResolved = state.mediumCellIsResolved[r][c];
                        gridData[r][c].wasHinted = state.mediumCellWasHinted[r][c]; // FIX 3
                    }
                }

                // Step 3: recalculate row/col sums
                for (int r = 1; r <= 6; r++) {
                    Fraction rowSum = new Fraction(0, 1);
                    for (int c = 1; c <= 6; c++) {
                        if (gridData[r][c].isSolution) {
                            Fraction temp = rowSum.add(gridData[r][c].value);
                            if (temp.numerator <= 99) rowSum = temp;
                            else gridData[r][c].isSolution = false;
                        }
                    }
                    gridData[r][0] = new Cell();
                    gridData[r][0].value = rowSum;
                }
                for (int c = 1; c <= 6; c++) {
                    Fraction colSum = new Fraction(0, 1);
                    for (int r = 1; r <= 6; r++) {
                        if (gridData[r][c].isSolution) {
                            Fraction temp = colSum.add(gridData[r][c].value);
                            if (temp.numerator <= 99) colSum = temp;
                            else gridData[r][c].isSolution = false;
                        }
                    }
                    gridData[0][c] = new MediumPageController.Cell();
                    gridData[0][c].value = colSum;
                }

                // Step 4: recompute remaining erasures now that gridData is fully populated
                int erasuresDone = 0;
                for (int rr = 1; rr <= 6; rr++)
                    for (int cc = 1; cc <= 6; cc++)
                        if (!gridData[rr][cc].isSolution && gridData[rr][cc].isResolved) erasuresDone++;
                erasures = countErasures() - erasuresDone;  // FIX 1

                if (lives <= 2) heart1.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
                if (lives <= 1) heart2.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
                if (lives <= 0) heart3.setStyle("-fx-fill:#c31515; -fx-opacity: 0;");
            }

        } else {
            generatePuzzle();
            erasures = countErasures();  // counts number of possible erasures
        }

        totalErasures.setText(String.valueOf(erasures));   // display the erasure number
        totalErasures.setAlignment(Pos.CENTER);

        populateGridUI();
        applyTheme();

        // save state of the running sums for when returning from the menu
        if (state.hasMediumSavedState && !state.mediumRetryMode) {
            restoreRunningSums();
        }
    }

    // counts all the false cells or possible erasure per round
    private int countErasures() {
        int count = 0;
        for (int r = 1; r <= 6; r++)
            for (int c = 1; c <= 6; c++)
                if (!gridData[r][c].isSolution) count++;
        return count;
    }

    // restores running sums when going back from the menu
    private void restoreRunningSums() {
        for (int r = 1; r <= 6; r++) {
            Fraction currentSum = new Fraction(0, 1);
            for (int c = 1; c <= 6; c++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution)
                    currentSum = currentSum.add(gridData[r][c].value);
            }
            if (currentRowSums[r] != null) {
                boolean matches = currentSum.numerator == gridData[r][0].value.numerator
                        && currentSum.denominator == gridData[r][0].value.denominator;
                currentRowSums[r].setText(currentSum.toString());
                if (matches && currentSum.numerator != 0) {
                    currentRowSums[r].setTextFill(Color.web("#00bf63"));
                    targetRowLabels[r].setTextFill(Color.web("#00bf63"));
                } else {
                    currentRowSums[r].setTextFill(Color.web("#e0e0e0"));
                    targetRowLabels[r].setTextFill(Color.WHITE);
                }
            }
        }
        for (int c = 1; c <= 6; c++) {
            Fraction currentSum = new Fraction(0, 1);
            for (int r = 1; r <= 6; r++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution)
                    currentSum = currentSum.add(gridData[r][c].value);
            }
            if (currentColSums[c] != null) {
                boolean matches = currentSum.numerator == gridData[0][c].value.numerator
                        && currentSum.denominator == gridData[0][c].value.denominator;
                currentColSums[c].setText(currentSum.toString());
                if (matches && currentSum.numerator != 0) {
                    currentColSums[c].setTextFill(Color.web("#00bf63"));
                    targetColLabels[c].setTextFill(Color.web("#00bf63"));
                } else {
                    currentColSums[c].setTextFill(Color.web("#e0e0e0"));
                    targetColLabels[c].setTextFill(Color.WHITE);
                }
            }
        }
    }

    private void generatePuzzle() {
        Random rand = new Random();
        boolean hasZeroSum;

        do {
            hasZeroSum = false;

            for (int r = 1; r <= 6; r++) {
                for (int c = 1; c <= 6; c++) {
                    gridData[r][c] = new MediumPageController.Cell();
                    boolean isFraction = rand.nextInt(4) == 0;

                    if (isFraction) {
                        int denom;
                        int choice = rand.nextInt(3);
                        if (choice == 0) denom = 2;
                        else if (choice == 1) denom = 4;
                        else denom = 3;
                        int numer = 1 + rand.nextInt(denom - 1);
                        gridData[r][c].value = new Fraction(numer, denom);
                    } else {
                        gridData[r][c].value = new Fraction(1 + rand.nextInt(9), 1);
                    }
                    gridData[r][c].isSolution = rand.nextBoolean();
                }
            }

            for (int r = 1; r <= 6; r++) {
                Fraction rowSum = new Fraction(0, 1);
                for (int c = 1; c <= 6; c++) {
                    if (gridData[r][c].isSolution)
                        rowSum = rowSum.add(gridData[r][c].value);
                }
                if (rowSum.numerator == 0) hasZeroSum = true;
                gridData[r][0] = new Cell();
                gridData[r][0].value = rowSum;
            }

            for (int c = 1; c <= 6; c++) {
                Fraction colSum = new Fraction(0, 1);
                for (int r = 1; r <= 6; r++) {
                    if (gridData[r][c].isSolution)
                        colSum = colSum.add(gridData[r][c].value);
                }
                if (colSum.numerator == 0) hasZeroSum = true;
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

                Label label = new Label(gridData[row][col].value.toString());
                label.setFont(Font.font("Arial", FontWeight.BOLD, 15));

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

                    // restore the yellow circle from the use of hints when coming from the main menu
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

    private void handleCellClick(StackPane pane, Label label, int row, int col) {
        MediumPageController.Cell cell = gridData[row][col];

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
            if (!cell.isSolution) {
                if (erasures <= 0) return;
                erasures--;
                totalErasures.setText(String.valueOf(erasures));

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
        for (int r = 1; r <= 6; r++) {
            Fraction currentSum = new Fraction(0, 1);
            for (int c = 1; c <= 6; c++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution)
                    currentSum = currentSum.add(gridData[r][c].value);
            }
            if (currentRowSums[r] != null) {
                boolean sumMatches = currentSum.numerator == gridData[r][0].value.numerator
                        && currentSum.denominator == gridData[r][0].value.denominator;
                currentRowSums[r].setText(currentSum.toString());
                if (sumMatches && currentSum.numerator != 0) {
                    currentRowSums[r].setTextFill(Color.web("#00bf63"));
                    targetRowLabels[r].setTextFill(Color.web("#00bf63"));
                } else {
                    currentRowSums[r].setTextFill(Color.web("#e0e0e0"));
                    targetRowLabels[r].setTextFill(Color.WHITE);
                }
            }
        }

        for (int c = 1; c <= 6; c++) {
            Fraction currentSum = new Fraction(0, 1);
            for (int r = 1; r <= 6; r++) {
                if (gridData[r][c].isResolved && gridData[r][c].isSolution)
                    currentSum = currentSum.add(gridData[r][c].value);
            }
            if (currentColSums[c] != null) {
                boolean sumMatches = currentSum.numerator == gridData[0][c].value.numerator
                        && currentSum.denominator == gridData[0][c].value.denominator;
                currentColSums[c].setText(currentSum.toString());
                if (sumMatches && currentSum.numerator != 0) {
                    currentColSums[c].setTextFill(Color.web("#00bf63"));
                    targetColLabels[c].setTextFill(Color.web("#00bf63"));
                } else {
                    currentColSums[c].setTextFill(Color.web("#e0e0e0"));
                    targetColLabels[c].setTextFill(Color.WHITE);
                }
            }
        }
    }

    private void deductLife() {
        lives--;

        if (lives == 2) animateHeartLoss(heart1);
        else if (lives == 1) animateHeartLoss(heart2);
        else if (lives == 0) animateHeartLoss(heart3);

        if (lives <= 0) {
            saveGameToState();
            try {
                FXMLLoader levelSuccessLoader = new FXMLLoader(getClass().getResource("medium_level_failed.fxml"));
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
        if (cellsResolved == 36) {
            saveGameToState();

            PauseTransition sfxDelay = new PauseTransition(Duration.millis(400));
            sfxDelay.setOnFinished(e-> SettingsController.playSuccessSound());
            sfxDelay.play();
            PauseTransition delay = new PauseTransition(Duration.millis(1500));
            delay.setOnFinished(e -> {
                try {
                    FXMLLoader levelSuccessLoader = new FXMLLoader(getClass().getResource("level_accomplishment_medium.fxml"));
                    Stage stage = (Stage) puzzleGrid.getScene().getWindow();
                    Parent root = levelSuccessLoader.load();

                    AchievementEasyController stats = levelSuccessLoader.getController();
                    stats.setStars(lives);

                    stage.getScene().setRoot(root);
                    SettingsController.setupGlobalClickSounds(stage.getScene());
                } catch (IOException f) {
                    f.printStackTrace();
                }
            });
            delay.play();
        }
    }

    @FXML
    protected void onHintClick() {
        if (hints <= 0) return;
        hints--;
        totalHints.setText(String.valueOf(hints));

        java.util.List<int[]> unresolvedCells = new java.util.ArrayList<>();
        for (int r = 1; r <= 6; r++) {
            for (int c = 1; c <= 6; c++) {
                if (!gridData[r][c].isResolved)
                    unresolvedCells.add(new int[]{r, c});
            }
        }

        if (unresolvedCells.isEmpty()) return;

        Random rand = new Random();
        int[] chosen = unresolvedCells.get(rand.nextInt(unresolvedCells.size()));
        int row = chosen[0], col = chosen[1];
        MediumPageController.Cell cell = gridData[row][col];

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
    private void backbutton(ActionEvent event) {
        GameState state = GameState.getInstance();
        state.mediumLives = lives;
        state.mediumHints = hints;
        state.mediumCellsResolved = cellsResolved;
        state.hasMediumSavedState = true;
        state.mediumRetryMode = false;

        for (int r = 1; r <= 6; r++) {
            for (int c = 1; c <= 6; c++) {
                state.mediumCellValuesNumerator[r][c] = gridData[r][c].value.numerator;
                state.mediumCellValuesDenominator[r][c] = gridData[r][c].value.denominator;
                state.mediumCellIsSolution[r][c] = gridData[r][c].isSolution;
                state.mediumCellIsResolved[r][c] = gridData[r][c].isResolved;
                state.mediumCellWasHinted[r][c] = gridData[r][c].wasHinted;
            }
        }

        try {
            FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("start_page.fxml"));
            Stage stage = (Stage) backbuttonMedium.getScene().getWindow();
            Parent root = backbuttonLoader.load();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveGameToState() {
        GameState state = GameState.getInstance();
        state.hasMediumSavedState = true;
        state.mediumRetryMode = true;

        for (int r = 1; r <= 6; r++) {
            for (int c = 1; c <= 6; c++) {
                state.mediumCellValuesNumerator[r][c] = gridData[r][c].value.numerator;
                state.mediumCellValuesDenominator[r][c] = gridData[r][c].value.denominator;
                state.mediumCellIsSolution[r][c] = gridData[r][c].isSolution;
                state.mediumCellIsResolved[r][c] = gridData[r][c].isResolved;
                state.mediumCellWasHinted[r][c] = gridData[r][c].wasHinted;
            }
        }
    }

    private void applyTheme() {
        themeIndex = themeIndex % THEMES.size();
        GameTheme t = THEMES.get(themeIndex);

        mediumPagePane.setStyle("-fx-background-color: " + t.pageBackground() + ";");

        if (backbuttonMedium != null) backbuttonMedium.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 40;");
        if (restart != null)          restart.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 40;");
        if (hint != null)             hint.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 35;");

        javafx.scene.paint.Color accent = javafx.scene.paint.Color.web(t.blackCell());
        if (ellipseMed != null) ellipseMed.setFill(accent);
        if (medAction != null)  medAction.setFill(accent);

        if (pen != null)    pen.setStyle("-fx-background-color: transparent;");
        if (eraser != null) eraser.setStyle("-fx-background-color: transparent;");

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