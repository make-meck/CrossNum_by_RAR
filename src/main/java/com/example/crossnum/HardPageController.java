package com.example.crossnum;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

/*
    The hard mode of the game uses a Backtracking Algorithm with Constraints Propagation
    1. Backtracking - The algorithm recursively tests different values and undoes them when it encounters a dead end.
    2. Checks Validity - It happens before placing a digit, it scans all the cells. If the digit has a similar in the row or column
       it's rejected. If there is no conflict the digit is placed.
    3. Randomizes the digit order each time, making every puzzle unique
    4. Randomizes the layout each new game, giving a different puzzle shape every time

    HOW SCORE SYSTEM WORKS
    Every game has a starting score of 500, if the player has typed the correct number, they will have +10 times combo points.
    If the player has typed the wrong answer their score will be deducted and the combo points will go back to one

*/
public class HardPageController {

    // ══════════════════════════════════════════════════════════════════════
    //  LAYOUT DEFINITION
    //  A pure data class — describes one puzzle shape.
    //  No JavaFX nodes here, just lists of cell keys and run groups.
    //
    //  Cell key format: "col,row"  e.g. "1,1" = column 1, row 1
    //  activeCells — the white playable cells the player fills in
    //  acrossRuns  — horizontal groups (left→right), each must have unique digits
    //  downRuns    — vertical groups (top→bottom), each must have unique digits
    //  runs        — combined list of both, used by the backtracking solver
    // ══════════════════════════════════════════════════════════════════════

    static class LayoutDefinition {
        final String name;
        final Set<String> activeCells;
        final List<List<String>> acrossRuns;
        final List<List<String>> downRuns;
        final List<List<String>> runs;

        LayoutDefinition(String name,
                         Set<String> activeCells,
                         List<List<String>> acrossRuns,
                         List<List<String>> downRuns) {
            this.name        = name;
            this.activeCells = activeCells;
            this.acrossRuns  = acrossRuns;
            this.downRuns    = downRuns;
            this.runs        = new ArrayList<>();
            this.runs.addAll(acrossRuns);
            this.runs.addAll(downRuns);
        }
    }


    //  HELPER BUILDERS — make the LAYOUTS list easier to read

    private static Set<String> cells(String... keys) {
        return new LinkedHashSet<>(Arrays.asList(keys));
    }

    @SafeVarargs
    private static List<List<String>> runs(List<String>... runArrays) {
        return new ArrayList<>(Arrays.asList(runArrays));
    }

    private static List<String> run(String... keys) {
        return Arrays.asList(keys);
    }

    //  LAYOUTS — pool of puzzle shapes picked randomly each new game


    private static final List<LayoutDefinition> LAYOUTS = new ArrayList<>();

    static {

        // LAYOUT 1 — Original ───────────────────────────────────────────
        LAYOUTS.add(new LayoutDefinition(
                "Layout1",
                cells("1,1","2,1","3,1","4,1","5,1",
                        "1,2","2,2","3,2","4,2","5,2","6,2",
                        "1,3","2,3",
                        "5,3","6,3",
                        "1,4","2,4",
                        "5,4","6,4",
                        "1,5","2,5","3,5","4,5","5,5","6,5",
                        "2,6","3,6","4,6","5,6","6,6"),
                runs(   //across
                        run("1,1","2,1","3,1","4,1","5,1"),
                        run("1,2","2,2","3,2","4,2","5,2","6,2"),
                        run("1,3","2,3"),
                        run("5,3","6,3"),
                        run("1,4","2,4"),
                        run("5,4","6,4"),
                        run("1,5","2,5","3,5","4,5","5,5","6,5"),
                        run("2,6","3,6","4,6","5,6","6,6")
                ),
                runs( // down
                        run("1,1","1,2","1,3","1,4","1,5"),
                        run("2,1","2,2","2,3","2,4","2,5","2,6"),
                        run("3,1","3,2"),
                        run("3,5","3,6"),
                        run("4,1","4,2"),
                        run("4,5","4,6"),
                        run("5,1","5,2","5,3","5,4","5,5","5,6"),
                        run("6,2","6,3","6,4","6,5","6,6")
                )
        ));

        // LAYOUT 2 — Staircase
        LAYOUTS.add(new LayoutDefinition(
                "Layout2",
                cells("1,1","2,1","3,1",
                        "1,2","2,2","3,2","4,2",
                        "2,3","3,3","4,3","5,3",
                        "3,4","4,4","5,4","6,4",
                        "4,5","5,5","6,5",
                        "5,6","6,6"),
                runs(
                        run("1,1","2,1","3,1"),
                        run("1,2","2,2","3,2","4,2"),
                        run("2,3","3,3","4,3","5,3"),
                        run("3,4","4,4","5,4","6,4"),
                        run("4,5","5,5","6,5"),
                        run("5,6","6,6")
                ),
                runs(
                        run("1,1","1,2"),
                        run("2,1","2,2","2,3"),
                        run("3,1","3,2","3,3","3,4"),
                        run("4,2","4,3","4,4","4,5"),
                        run("5,3","5,4","5,5","5,6"),
                        run("6,4","6,5","6,6")
                )
        ));

        // LAYOUT 3 — Windows
        LAYOUTS.add(new LayoutDefinition(
                "Layout3",
                cells("1,1","2,1","3,1",
                        "1,2","2,2","3,2",
                        "1,3","2,3","3,3",
                        "4,4","5,4","6,4",
                        "4,5","5,5","6,5",
                        "4,6","5,6","6,6"),
                runs(
                        run("1,1","2,1","3,1"),
                        run("1,2","2,2","3,2"),
                        run("1,3","2,3","3,3"),
                        run("4,4","5,4","6,4"),
                        run("4,5","5,5","6,5"),
                        run("4,6","5,6","6,6")
                ),
                runs(
                        run("1,1","1,2","1,3"),
                        run("2,1","2,2","2,3"),
                        run("3,1","3,2","3,3"),
                        run("4,4","4,5","4,6"),
                        run("5,4","5,5","5,6"),
                        run("6,4","6,5","6,6")
                )
        ));
        LAYOUTS.add(
                new LayoutDefinition(
                        "layout4",
                        cells(
                                "2,2", "3,2", "4,2", "5,2",
                                        "2,3", "3,3", "4,3", "5,3", "6,3",
                                        "2,4", "3,4", "4,4",  "5,4", "6,4",
                                        "2,5", "3,5", "4,5", "5,5", "6,5",
                                        "2,6", "3,6", "4,6", "5,6", "6,6"),
                        runs(
                                run("2,2", "3,2", "4,2", "5,2"),
                                run( "2,3", "3,3", "4,3", "5,3", "6,3"),
                                run("2,4", "3,4", "4,4",  "5,4", "6,4"),
                                run(  "2,5", "3,5", "4,5", "5,5", "6,5"),
                                run( "2,6", "3,6", "4,6", "5,6", "6,6")
                        ),
                        runs(
                                run("2,2", "2,3", "2,4", "2,5", "2,6"),
                                run("3,2", "3,3", "3,4", "3,5", "3,6"),
                                run("4,2", "4,3", "4,4", "4,5", "4,6"),
                                run("5,2", "5,3","5,4","5,5", "5,6"),
                                run("6,3","6,4","6,5", "6,6")

                        )

                ));

        LAYOUTS.add(
                new LayoutDefinition(
                        "Layout5",
                        cells("1,1", "2,1", "3,1", "4,1", "5,1", "6,1",
                                "1,2", "2,2", "4,2", "5,2", "6,2",
                                "1,3", "2,3", "4,3", "6,3",
                                "1,4", "2,4","3,4","4,4", "5,4", "6,4",
                                "1,5", "2,5", "4,5", "5,5", "6,5",
                                "1,6", "2,6", "4,6", "5,6"),

                        runs(
                                run("1,1", "2,1", "3,1", "4,1", "5,1", "6,1"),
                                run( "1,2", "2,2"),
                                run("1,3", "2,3"),
                                run("4,2", "5,2", "6,2"),
                                run(  "1,4", "2,4","3,4","4,4", "5,4", "6,4"),
                                run( "1,5", "2,5"),
                                run( "4,5", "5,5", "6,5"),
                                run(  "1,6", "2,6"),
                                run("4,6", "5,6")
                        ),
                        runs(
                                run("1,1", "1,2", "1,3", "1,4", "1,5", "1,6"),
                                run("2,1","2,2","2,3", "2,4", "2,5", "2,6"),
                                run("4,1", "4,2", "4,3", "4,4", "4,5", "4,6"),
                                run("5,1", "5,2"),
                                run("5,4","5,5","5,6"),
                                run("6,1", "6,2", "6,3", "6,4", "6,5")
                        )

                ));

    }


    // SCORE CONSTANTS
    private static final int base_score = 500;
    private static final int point_per_correct = 10;
    private static final int penalty_wrong = 20;
    private static final int score_floor = 0;


    //  INSTANCE FIELDS
    private final Map<String, TextField> fieldMap = new LinkedHashMap<>();
    private final Map<String, Integer>   solution  = new HashMap<>();
    private final Set<String>            correctCells = new HashSet<>();
    private final Map<String, Boolean>   cellWasCorrect = new HashMap<>();

    // The currently active layout — set in initialize() and onRestartClick()
    private LayoutDefinition currentLayout;
    private String prevLayoutName = null;
    //Used for the score system
    private int currentScore = base_score;
    private int comboCount = 1;
    private Timeline timer;
    private int secondsLeft = 15 * 60;
    private int hintsLeft   = 3;

    // ── FXML injections — only structural elements remain ─────────────────
    // (TextFields and sum Labels are now created in Java by buildGrid())
    @FXML private Label scoreLabel;
    @FXML private Button    backbuttonHard;
    @FXML private Button    hint;
    @FXML private Button    restartButton;
    @FXML private Label     timerLabel;
    @FXML private Label     hintLabel;
    @FXML private GridPane  hardPagePane;
    @FXML private BorderPane hardLevelPage;
    @FXML private Ellipse scoreEllipse;
    @FXML private Ellipse hardEllipse;
    @FXML private Circle restartCirle;
    @FXML private Circle backCircle;
    @FXML private Circle hintCircle;



    //THEMES
    record GameTheme(
            String name,
            String blackCell, //cell background of the clue
            String whitecell, //textfield cell
            String pageBackground,
            String labelText, //score, timer, and hint label color
            String buttonBase //base color for buttons

    ) {}
    private static final List<GameTheme> THEMES = List.of(
            new GameTheme("Forest",   "#2d532c", "#ffffff", "#BBD0BB", "#ffffff", "#3a7a39"),
            new GameTheme("Ocean",   "#1a3a5c", "#e8f4fd", "#81A6C6", "#cce7ff", "#1e5080"),
            new GameTheme("Sunset",  "#7a2d00", "#fff3e0", "#FF8C00", "#ffd8a8", "#b84500"),
            new GameTheme("Amethyst","#3d1a6e", "#f3eaff", "#B95E82", "#dbb8ff", "#6a2fbf"),
            new GameTheme("Slate",   "#2e3f50", "#ecf0f1", "#BFC9D1", "#bdc3c7", "#3d5166"),
            new GameTheme("Royal", "#4B0082", "#FFFFFF", "#FF7F00","#FFFFFF", "#FF007F" ),
            new GameTheme("Powerpuff", "#FF3E9B", "#F6FFDC",  "#66D0BC", "#FFFFFF", "#FFEABB")
    );
    private int themeIndex = 0; // this tracks which theme is active


    //  INITIALIZE

    @FXML
    private void initialize() {
        GameState state = GameState.getInstance();
        themeIndex = state.hardSavedTheme;
        if (state.hasSavedState) {
            // Restore the saved layout, solution, and timer
            currentLayout = findLayoutByName(state.savedLayoutName);
            if (currentLayout == null) currentLayout = randomLayout();

            solution.putAll(state.hardSolution);
            secondsLeft = state.secondsLeft;
            hintsLeft   = state.hintsLeft;
            currentScore = state.savedScore;
            comboCount = state.savedCombo;
            prevLayoutName = state.prevLayoutName;

            // Build the grid first so fieldMap is populated
            buildGrid();
            applyTheme();
            clearFieldsForPlayer();

            // Then restore what the player had typed and cell colours
            for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
                String key = entry.getKey();
                String savedText = state.hardFieldValues.getOrDefault(key, "");
                String savedStyle = state.hardFieldValues.getOrDefault(key, "");
                entry.getValue().setText(state.hardFieldValues.getOrDefault(key, ""));
                entry.getValue().setStyle(state.hardFieldStyles.getOrDefault(key, ""));

                //Rebuild correctCells/cells was correct from saved values
                if(!savedText.isEmpty()){
                    boolean wasRight = savedText.equals(
                            String.valueOf(solution.getOrDefault(key, -999)));
                    cellWasCorrect.put(key,wasRight);
                    if(wasRight) correctCells.add(key);
                }
            }

            int minutes = secondsLeft / 60;
            int seconds = secondsLeft % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

        } else {
            // Fresh game — pick a layout, build the grid, then solve it
            currentLayout = randomLayout();
            generateSolution(); // generateSolution() reads from fieldMap
            buildGrid();        // buildGrid() populates fieldMap
            applyTheme();
            clearFieldsForPlayer();
            currentScore= base_score;
            comboCount = 1;
            secondsLeft = 15*60;
            timerLabel.setText("15:00");
        }

        updateScoreDisplay();
        startTimer();
        updateHintButton();
    }

        // For the theme
        private void applyTheme(){
            GameTheme t= THEMES.get(themeIndex);
            /*
            // Page background — image takes priority over color
            if (t.backgroundImage() != null) {
            String url = getClass().getResource(t.backgroundImage()).toExternalForm();
             hardLevelPage.setStyle(
              "-fx-background-image: url('" + url + "');" +
             "-fx-background-size: cover;" +
              "-fx-background-position: center;"
                 );
            } else {
             hardLevelPage.setStyle("-fx-background-color: " + t.pageBackground() + ";");
               }
             */

            hardLevelPage.setStyle("-fx-background-color: " + t.pageBackground() + ";");

            javafx.scene.paint.Color accent = javafx.scene.paint.Color.web(t.blackCell());
            if (scoreEllipse != null) scoreEllipse.setFill(accent);
            if (hardEllipse  != null) hardEllipse.setFill(accent);
            if (backCircle   != null) backCircle.setFill(accent);
            if (restartCirle != null) restartCirle.setFill(accent);
            if (hintCircle   != null) hintCircle.setFill(accent);

            for (Label lbl : List.of(scoreLabel, timerLabel)) {
                if (lbl != null) lbl.setStyle("-fx-text-fill: " + t.labelText() + ";");
            }

            for (Button btn : List.of(backbuttonHard, hint, restartButton)) {
                if (btn != null) btn.setStyle("-fx-background-color: transparent;");
            }

            for (javafx.scene.Node node : hardPagePane.getChildren()) {
                if (node instanceof StackPane sp) {
                    String bg = sp.getStyle();
                    if (bg.contains("#ffffff") || bg.contains("#fff")) {
                        sp.setStyle("-fx-background-color: " + t.whitecell() + "; -fx-background-radius: 10;");
                    } else if (!bg.contains("transparent")) {

                        sp.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 10;");
                    }
                }
            }
        }

    //  BUILD GRID — creates all cells dynamically from currentLayout

    private void buildGrid() {
        hardPagePane.getChildren().clear();
        fieldMap.clear();

        // Work out which black cells carry ACROSS labels (cell to the LEFT of run start)
        // and which carry DOWN labels (cell ABOVE run start)
        Map<String, Integer> acrossLabelMap = new LinkedHashMap<>();
        Map<String, Integer> downLabelMap   = new LinkedHashMap<>();

        for (List<String> run : currentLayout.acrossRuns) {
            if (run.isEmpty()) continue;
            String first = run.get(0);
            String labelCell = (col(first) - 1) + "," + row(first);
            int sum = run.stream().mapToInt(c -> solution.getOrDefault(c, 0)).sum();
            acrossLabelMap.put(labelCell, sum);
        }

        for (List<String> run : currentLayout.downRuns) {
            if (run.isEmpty()) continue;
            String first = run.get(0);
            String labelCell = col(first) + "," + (row(first) - 1);
            int sum = run.stream().mapToInt(c -> solution.getOrDefault(c, 0)).sum();
            downLabelMap.put(labelCell, sum);
        }

        // Paint every cell in the 7×7 grid
        for (int r = 0; r <= 6; r++) {
            for (int c = 0; c <= 6; c++) {
                String key      = c + "," + r;
                boolean isActive  = currentLayout.activeCells.contains(key);
                boolean hasAcross = acrossLabelMap.containsKey(key);
                boolean hasDown   = downLabelMap.containsKey(key);

                if (isActive) {
                    addWhiteCell(c, r, key);
                } else if (hasAcross || hasDown) {
                    addBlackClueCell(c, r,
                            hasAcross ? acrossLabelMap.get(key) : null,
                            hasDown   ? downLabelMap.get(key)   : null);
                } else {
                    addEmptyCell(c, r);
                }
            }
        }
/*
        //for debugging
        for (List<String> run : currentLayout.acrossRuns) {
            if (run.isEmpty()) continue;
            String first = run.get(0);
            String labelCell = (col(first) - 1) + "," + row(first);
            int sum = run.stream().mapToInt(c -> solution.getOrDefault(c, 0)).sum();
            // DEBUG
            System.out.println("Run: " + run + " labelCell: " + labelCell + " sum: " + sum);
            acrossLabelMap.put(labelCell, sum);
            }
   */

        }

    //Cell builders

    // White playable TextField cell
    private void addWhiteCell(int col, int row, String key) {
        GameTheme t= THEMES.get(themeIndex);
        StackPane pane = new StackPane();
        pane.setStyle("-fx-background-color: " + t.whitecell() + "; -fx-background-radius: 10;");
        pane.setPrefSize(200, 150);

        TextField tf = new TextField();
        tf.setFont(Font.font("Arial Bold", 25));
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setMaxHeight(Double.MAX_VALUE);
        pane.getChildren().add(tf);

        fieldMap.put(key, tf);
        placeInGrid(pane, col, row);
    }

    private void addBlackClueCell(int col, int row, Integer across, Integer down) {
        GameTheme t = THEMES.get(themeIndex);

        GridPane inner = new GridPane();
        inner.setStyle("-fx-background-color:" + t.blackCell()+ " ; -fx-background-radius: 10;");
        inner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // 2x2 inner grid: top-left=down label, bottom-right=across label
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        inner.getColumnConstraints().addAll(c1, c2);

        RowConstraints r1 = new RowConstraints();
        r1.setPercentHeight(50);
        RowConstraints r2 = new RowConstraints();
        r2.setPercentHeight(50);
        inner.getRowConstraints().addAll(r1, r2);

        // Down label — top-left cell of inner grid
        if (down != null) {
            Label lbl = new Label(String.valueOf(down));
            lbl.setTextFill(Color.WHITE);
            lbl.setFont(Font.font("Arial Bold", 14));
            lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setColumnIndex(lbl, 0);
            GridPane.setRowIndex(lbl, 1);
            GridPane.setHalignment(lbl, javafx.geometry.HPos.LEFT);
            GridPane.setValignment(lbl, javafx.geometry.VPos.BOTTOM);
            javafx.geometry.Insets margin = new javafx.geometry.Insets(0, 0, 3, 4);
            GridPane.setMargin(lbl, margin);
            inner.getChildren().add(lbl);
        }

        // Across label — bottom-right cell of inner grid
        if (across != null) {
            Label lbl = new Label(String.valueOf(across));
            lbl.setTextFill(Color.WHITE);
            lbl.setFont(Font.font("Arial Bold", 14));
            lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setColumnIndex(lbl, 1);
            GridPane.setRowIndex(lbl, 0);
            GridPane.setHalignment(lbl, javafx.geometry.HPos.RIGHT);
            GridPane.setValignment(lbl, javafx.geometry.VPos.TOP);
            javafx.geometry.Insets margin = new javafx.geometry.Insets(3, 4, 0, 0);
            GridPane.setMargin(lbl, margin);
            inner.getChildren().add(lbl);
        }

        // Diagonal line drawn on top using a Canvas that fills the cell
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas();
        // Bind canvas size to the inner GridPane size
        canvas.widthProperty().bind(inner.widthProperty());
        canvas.heightProperty().bind(inner.heightProperty());
        canvas.setMouseTransparent(true);

        // Redraw line whenever size changes
        javafx.beans.value.ChangeListener<Number> redraw = (obs, oldVal, newVal) -> {
            double w = canvas.getWidth();
            double h = canvas.getHeight();
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, w, h);
            gc.setStroke(javafx.scene.paint.Color.WHITE);
            gc.setLineWidth(1.5);
            gc.strokeLine(4, 4, w - 4, h - 4);
        };
        canvas.widthProperty().addListener(redraw);
        canvas.heightProperty().addListener(redraw);

        StackPane pane = new StackPane(inner, canvas);
        pane.setStyle("-fx-background-color: " + t.blackCell() + "; -fx-background-radius: 10;");

        GridPane.setColumnIndex(pane, col);
        GridPane.setRowIndex(pane, row);
        GridPane.setHgrow(pane, Priority.SOMETIMES);
        GridPane.setVgrow(pane, Priority.SOMETIMES);
        hardPagePane.getChildren().add(pane);
    }


    // Transparent filler for unused corners
    private void addEmptyCell(int col, int row) {
        StackPane pane = new StackPane();
        pane.setStyle("-fx-background-color: transparent;");
        placeInGrid(pane, col, row);
    }

    // Places a StackPane into the GridPane at the given column and row
    private void placeInGrid(StackPane pane, int col, int row) {
        GridPane.setColumnIndex(pane, col);
        GridPane.setRowIndex(pane, row);
        GridPane.setHgrow(pane, Priority.SOMETIMES);
        GridPane.setVgrow(pane, Priority.SOMETIMES);
        hardPagePane.getChildren().add(pane);
    }

    //  GAME LOGIC

    private void clearFieldsForPlayer() {
        correctCells.clear();
        cellWasCorrect.clear();

        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            String    key = entry.getKey();
            TextField tf  = entry.getValue();
            tf.clear();

            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                // ── Input validation ──────────────────────────────────────
                if (!newVal.matches("[1-9]?")) { tf.setText(oldVal); return; }
                if (newVal.length() > 1)       { tf.setText(newVal.substring(newVal.length() - 1)); return; }

                boolean prevCorrect = Boolean.TRUE.equals(cellWasCorrect.get(key));

                // ── Cell cleared ──────────────────────────────────────────
                if (newVal.isEmpty()) {
                    if (prevCorrect) correctCells.remove(key);
                    cellWasCorrect.remove(key);
                    tf.setStyle("-fx-background-color: " + THEMES.get(themeIndex).whitecell() + ";");
                    updateScoreDisplay();
                    return;
                }

                int     entered   = Integer.parseInt(newVal);
                int     expected  = solution.getOrDefault(key, -999);
                boolean isCorrect = (entered == expected);

                if (isCorrect) {
                    // ── Correct answer ────────────────────────────────────
                    if (!prevCorrect) {
                        // Award points only when transitioning to correct
                        int earned = point_per_correct * comboCount;
                        currentScore += earned;
                        comboCount++;
                        correctCells.add(key);
                    }
                    cellWasCorrect.put(key, true);
                    tf.setStyle("-fx-text-fill: #00bf63;" +
                            "-fx-background-color:" + THEMES.get(themeIndex).whitecell() + ";" +
                            "-fx-border-radius:0px;" +
                            "-fx-border-color:transparent;");
                    updateScoreDisplay();
                    checkIfAllCorrect();

                } else {
                    // ── Wrong answer ──────────────────────────────────────
                    if (prevCorrect) correctCells.remove(key);
                    currentScore = Math.max(score_floor, currentScore - penalty_wrong);
                    comboCount   = 1;
                    cellWasCorrect.put(key, false);
                    tf.setStyle("-fx-text-fill: #c82121;" +
                            "-fx-background-color:" + THEMES.get(themeIndex).whitecell() + ";");
                    updateScoreDisplay();
                }
            });
        }
    }


    private void checkIfAllCorrect() {
        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            String input = entry.getValue().getText().trim();
            if (input.isEmpty() || Integer.parseInt(input) != solution.getOrDefault(entry.getKey(), -999))
                return;
        }
        timer.stop();
        for (TextField tf : fieldMap.values()) tf.setEditable(false);
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> levelAchievement());
        delay.play();
    }

    private void generateSolution() {
        solution.clear();
        List<String> cells = new ArrayList<>(currentLayout.activeCells);
        boolean solved = backtrack(cells, 0);
        if (!solved) {
            System.out.println("Backtracking failed, retrying...");
            generateSolution();
        }
        /*
        // DEBUG
        System.out.println("Solution size: " + solution.size());
        System.out.println("Solution: " + solution);
        */

    }

    private boolean backtrack(List<String> cells, int index) {
        if (index == cells.size()) return true;
        String key = cells.get(index);
        List<Integer> digits = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        Collections.shuffle(digits);
        for (int digit : digits) {
            if (isValid(key, digit)) {
                solution.put(key, digit);
                if (backtrack(cells, index + 1)) return true;
                solution.remove(key);
            }
        }
        return false;
    }

    private boolean isValid(String key, int digit) {
        for (List<String> run : currentLayout.runs) {
            if (!run.contains(key)) continue;
            for (String other : run) {
                if (!other.equals(key) && solution.getOrDefault(other, -1) == digit)
                    return false;
            }
        }
        return true;
    }

    // Score Display
    private void updateScoreDisplay(){
        if(scoreLabel == null) return;
        if(comboCount >2) {
            scoreLabel.setText(currentScore + "  🔥x" + comboCount);
        }else{
            scoreLabel.setText(String.valueOf(currentScore));
        }
    }

    // Hint

    @FXML
    protected void onHintClick() {
        if (hintsLeft <= 0) return;

        List<Map.Entry<String, TextField>> emptyCells = new ArrayList<>();
        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            String input = entry.getValue().getText().trim();
            if (input.isEmpty() ||
                    Integer.parseInt(input) != solution.getOrDefault(entry.getKey(), -999))
                emptyCells.add(entry);
        }
        if (emptyCells.isEmpty()) return;

        Collections.shuffle(emptyCells);
        Map.Entry<String, TextField> chosen = emptyCells.get(0);
        TextField tf  = chosen.getValue();
        String    key = chosen.getKey();

        // Hints are neutral — no score change, no combo effect
        tf.setText(String.valueOf(solution.get(key)));
        tf.setStyle("-fx-font-size:25px; -fx-text-fill: #f1dd2b;");
        tf.setEditable(false);

        correctCells.add(key);
        cellWasCorrect.put(key, true);

        hintsLeft--;
        updateHintButton();
        checkIfAllCorrect();
    }

    private void updateHintButton(){
        hintLabel.setText(String.valueOf(hintsLeft));
        if (hintsLeft <= 0) {
            hint.setDisable(true);
            hint.setOpacity(0.4);
        }
    }

    //  Restart

    @FXML
    protected void onRestartClick() {
        themeIndex = (themeIndex + 1) % THEMES.size(); // ← advance
        GameState.getInstance().hardSavedTheme = themeIndex; // ← persist

        currentLayout = randomLayout();
        generateSolution(); // fills solution FIRST
        buildGrid();        // builds grid with correct sums
        applyTheme();
        clearFieldsForPlayer();

        currentScore = base_score;
        comboCount =1;
        updateScoreDisplay();

        timer.stop();
        secondsLeft = 15 * 60;
        timerLabel.setText("15:00");
        startTimer();

        hintsLeft = 3;
        hint.setDisable(false);
        hint.setOpacity(1.0);
        updateHintButton();


        GameState.getInstance().hasSavedState = false;
    }

    // ── Timer ─────────────────────────────────────────────────────────────

    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsLeft--;
            int m = secondsLeft / 60, s = secondsLeft % 60;
            timerLabel.setText(String.format("%02d:%02d", m, s));
            if (secondsLeft <= 0 || currentScore == 0) { timer.stop(); gameFailed(); }  // if the player has a score of 0 and time runn out it will be game failed
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void levelAchievement() {
        themeIndex = (themeIndex + 1 ) % THEMES.size();
        GameState.getInstance().hardSavedTheme = themeIndex;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("level_accomplishment_hard.fxml"));
            Parent root = loader.load();
            AchievementHardController ac = loader.getController();
            int timeTaken = (15 * 60) - secondsLeft;
            ac.setStats(secondsLeft, timeTaken,currentScore);
            Stage stage = (Stage) backbuttonHard.getScene().getWindow();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void gameFailed() {
        themeIndex = (themeIndex + 1) % THEMES.size();
        GameState state = GameState.getInstance();
        state.hardSolution    = new HashMap<>(solution);
        state.secondsLeft     = secondsLeft; // ← save real value, not 15*60
        state.hintsLeft       = hintsLeft;
        state.hasSavedState   = true;
        state.savedLayoutName = currentLayout.name;
        state.savedScore      = currentScore;
        state.savedCombo      = comboCount;
        state.hardSavedTheme      = themeIndex;
        state.prevLayoutName = prevLayoutName;

        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            state.hardFieldValues.put(entry.getKey(), entry.getValue().getText());
            state.hardFieldStyles.put(entry.getKey(), entry.getValue().getStyle());
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("level_failed_hard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backbuttonHard.getScene().getWindow();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void backbutton(ActionEvent event) {
        GameState state = GameState.getInstance();
        state.hardSolution    = new HashMap<>(solution);
        state.secondsLeft     = secondsLeft;
        state.hintsLeft       = hintsLeft;
        state.hasSavedState   = true;
        state.savedLayoutName = currentLayout.name;
        state.savedCombo      =comboCount;
        state.savedScore      = currentScore;
        state.hardSavedTheme      = themeIndex;
        state.prevLayoutName  = prevLayoutName;

        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            state.hardFieldValues.put(entry.getKey(), entry.getValue().getText());
            state.hardFieldStyles.put(entry.getKey(), entry.getValue().getStyle());
        }
        timer.stop();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("start_page.fxml"));
            Stage stage = (Stage) backbuttonHard.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Layout utilities ──────────────────────────────────────────────────

    private LayoutDefinition randomLayout() {
        List<LayoutDefinition> available = new ArrayList<>(LAYOUTS);

       //This wwill eliminate displaying the same layout from the previous round
        if (prevLayoutName != null && available.size() > 1) {
            available.removeIf(l -> l.name.equals(prevLayoutName));
        }

        LayoutDefinition chosen = available.get(new Random().nextInt(available.size()));
        prevLayoutName = chosen.name;
        return chosen;
    }

    private LayoutDefinition findLayoutByName(String name) {
        if (name == null) return null;
        return LAYOUTS.stream()
                .filter(l -> l.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    // Extracts the column number from a "col,row" key
    private int col(String key) { return Integer.parseInt(key.split(",")[0]); }

    // Extracts the row number from a "col,row" key
    private int row(String key) { return Integer.parseInt(key.split(",")[1]); }
}