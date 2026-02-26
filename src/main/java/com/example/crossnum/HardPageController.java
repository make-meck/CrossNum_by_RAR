package com.example.crossnum;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.*;
import java.util.Arrays;
import java.util.Collections;

/*
    The hard mode of the game uses a Backtracking Algorithm with Constraints Propagation
    1. Backtracking - The algorithm recursively test different values and undoes them when it encounters a dead end.
    2. Checks Validity - It happens before placing a digit, it scans all the cells. If the digit has similar in the row or column
        its rejected. If there is no conflict the digit is placed.
    3. Randomizes the digit order each time, making every puzzle unique
 */
public class HardPageController {
    private final Map<String, TextField> fieldMap = new LinkedHashMap<>();
    private final Map<String, Integer> solution = new HashMap<>();

    //This defines which cells belongs together in a row or column grouo, so the group can have unique digits and display its sum

    private static final List<List<String>> RUNS = Arrays.asList(
            // ACROSS
            Arrays.asList("1,1", "2,1", "3,1", "4,1", "5,1"),
            Arrays.asList("1,2", "2,2", "3,2", "4,2", "5,2", "6,2"),
            Arrays.asList("1,3", "2,3"),
            Arrays.asList("5,3", "6,3"),
            Arrays.asList("1,4", "2,4"),
            Arrays.asList("5,4", "6,4"),
            Arrays.asList("1,5", "2,5", "3,5", "4,5", "5,5", "6,5"),
            Arrays.asList("2,6", "3,6", "4,6", "5,6", "6,6"),
            // DOWN
            Arrays.asList("1,1", "1,2", "1,3", "1,4", "1,5"),
            Arrays.asList("2,1", "2,2", "2,3", "2,4", "2,5", "2,6"),
            Arrays.asList("3,1", "3,2"),
            Arrays.asList("3,5", "3,6"),
            Arrays.asList("4,1", "4,2"),
            Arrays.asList("4,5", "4,6"),
            Arrays.asList("5,1", "5,2", "5,3", "5,4", "5,5", "5,6"),
            Arrays.asList("6,2", "6,3"),
            Arrays.asList("6,4", "6,5", "6,6")
    );

    private Timeline timer;
    private int secondsLeft = 8 * 60;
    private int hintsLeft = 3;

    @FXML
    private Button backbuttonHard;
    @FXML
    private Button hint;
    @FXML
    private Button restartButton;
    @FXML
    private Label timerLabel;
    @FXML
    private Label hintLabel;
    @FXML
    private TextField tf_r1c1, tf_r1c2, tf_r1c3, tf_r1c4, tf_r1c5; //All textfields in row1
    @FXML
    private TextField tf_r2c1, tf_r2c2, tf_r2c3, tf_r2c4, tf_r2c5, tf_r2c6; //All textfields in row 2
    @FXML
    private TextField tf_r3c1, tf_r3c2, tf_r3c5, tf_r3c6; //All Textfields in row3
    @FXML
    private TextField tf_r4c1, tf_r4c2, tf_r4c5, tf_r4c6; //All textfields in row4
    @FXML
    private TextField tf_r5c1, tf_r5c2, tf_r5c3, tf_r5c4, tf_r5c5, tf_r5c6;  //All textfields in row5
    @FXML
    private TextField tf_r6c2, tf_r6c3, tf_r6c4, tf_r6c5, tf_r6c6; //All textfields in row6

    //Labels for displaying the sum
    @FXML
    private Label lbl_r1c0_across, lbl_r2c0_across, lbl_r3c0_across, lbl_r4c0_across, lbl_r5c0_across; //All label in column 0
    @FXML
    private Label lbl_r0c1_down, lbl_r0c2_down, lbl_r0c3_down, lbl_r0c4_down, lbl_r0c5_down; //All labels in row 0
    @FXML
    private Label lbl_r6c1_across; //Label for row 1 column 6
    @FXML
    private Label lbl_r3c4_across; //Label for row 4, column 4
    @FXML
    private Label lbl_r4c3_down, lbl_c4r4_down; //Label for row 4
    @FXML
    private Label lbl_r4c4_across, lbl_r4c4_down;
    @FXML
    private Label lbl_r1c6_down;


    @FXML
    private void initialize() {
        buildFieldMap();

        GameState state = GameState.getInstance();
        //Restore saved state
        if (state.hasSavedState) {
            solution.putAll(state.hardSolution);
            secondsLeft = state.secondsLeft;
            hintsLeft = state.hintsLeft;

            displaySums();
            clearFieldsForPlayer();

            //Restore the field values and styles
            for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
                String key = entry.getKey();
                TextField tf = entry.getValue();
                tf.setText(state.hardFieldValues.getOrDefault(key, ""));
                tf.setStyle(state.hardFieldStyles.getOrDefault(key, ""));
            }

            //This will update the timeLabel as the state of the game is saved
            int minutes = secondsLeft / 60;
            int seconds = secondsLeft % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        } else {
            //If the condition is not satisfied it will generate a new game
            generateSolution();
            displaySums();
            clearFieldsForPlayer();
        }
        startTimer();
        updateHintButton();


    }


    @FXML
    protected void onHintClick() {
        if (hintsLeft <= 0) return;

        // Collect all unfilled or wrong cells
        List<Map.Entry<String, TextField>> emptyCells = new ArrayList<>();
        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            String input = entry.getValue().getText().trim();
            if (input.isEmpty() || Integer.parseInt(input) != solution.get(entry.getKey())) {
                emptyCells.add(entry);
            }
        }

        if (emptyCells.isEmpty()) return;

        // Pick a random unfilled cell and reveal it
        Collections.shuffle(emptyCells);
        Map.Entry<String, TextField> chosen = emptyCells.get(0);
        TextField tf = chosen.getValue();
        tf.setText(String.valueOf(solution.get(chosen.getKey())));
        tf.setStyle("-fx-font-size:25px;" +
                "-fx-text-fill: #f1dd2b;"); // yellow
        tf.setEditable(false);

        hintsLeft--;
        updateHintButton();
        checkIfAllCorrect();
    }

    private void updateHintButton() {
        hintLabel.setText(String.valueOf(hintsLeft));
        if (hintsLeft < 0) {
            hint.setDisable(true);
            hint.setOpacity(0.4);
        }
    }

    @FXML
    protected void onRestartClick() {
        // Generates a new solution
        solution.clear();
        generateSolution();

        // Recalculate sums
        displaySums();

        //  Reset all fields
        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            TextField tf = entry.getValue();
            tf.setEditable(true);
            tf.clear();
            tf.setStyle(" ");
        }

        //  Reset timer
        timer.stop();
        secondsLeft = 15 * 60;
        timerLabel.setText("15:00");
        startTimer();

        // Reset hints
        hintsLeft = 3;
        hint.setDisable(false);
        hint.setOpacity(1.0);

        updateHintButton();
        GameState.getInstance().hasSavedState = false;
    }


    // this method is used to make the timer works
    private void startTimer() {
        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    secondsLeft--;
                    int minutes = secondsLeft / 60;
                    int seconds = secondsLeft % 60;
                    timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                })
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();





    }

    /*This method servers as the map who coordinates to the corresponsing textfield.
    e.g., "1,3" is for the textfield found in row 1 column 3
    for the coordinates of the cell it follows the format column by row.
    */
    private void buildFieldMap() {
        // row 1
        fieldMap.put("1,1", tf_r1c1);
        fieldMap.put("2,1", tf_r1c2);
        fieldMap.put("3,1", tf_r1c3);
        fieldMap.put("4,1", tf_r1c4);
        fieldMap.put("5,1", tf_r1c5);
        // row 2
        fieldMap.put("1,2", tf_r2c1);
        fieldMap.put("2,2", tf_r2c2);
        fieldMap.put("3,2", tf_r2c3);
        fieldMap.put("4,2", tf_r2c4);
        fieldMap.put("5,2", tf_r2c5);
        fieldMap.put("6,2", tf_r2c6);
        // row 3
        fieldMap.put("1,3", tf_r3c1);
        fieldMap.put("2,3", tf_r3c2);
        fieldMap.put("5,3", tf_r3c5);
        fieldMap.put("6,3", tf_r3c6);
        // row 4
        fieldMap.put("1,4", tf_r4c1);
        fieldMap.put("2,4", tf_r4c2);
        fieldMap.put("5,4", tf_r4c5);
        fieldMap.put("6,4", tf_r4c6);
        // row 5
        fieldMap.put("1,5", tf_r5c1);
        fieldMap.put("2,5", tf_r5c2);
        fieldMap.put("3,5", tf_r5c3);
        fieldMap.put("4,5", tf_r5c4);
        fieldMap.put("5,5", tf_r5c5);
        fieldMap.put("6,5", tf_r5c6);
        // row 6
        fieldMap.put("2,6", tf_r6c2);
        fieldMap.put("3,6", tf_r6c3);
        fieldMap.put("4,6", tf_r6c4);
        fieldMap.put("5,6", tf_r6c5);
        fieldMap.put("6,6", tf_r6c6);

    }

    //This method happens once the solution is generated, and it calculated the sum of each run and displays it on the corresponding label
    private void displaySums() {
        Map<List<String>, Label> runLabelMap = new LinkedHashMap<>();


        runLabelMap.put(Arrays.asList("1,1", "2,1", "3,1", "4,1", "5,1"), lbl_r1c0_across);
        runLabelMap.put(Arrays.asList("1,2", "2,2", "3,2", "4,2", "5,2", "6,2"), lbl_r2c0_across);
        runLabelMap.put(Arrays.asList("1,3", "2,3"), lbl_r3c0_across);
        runLabelMap.put(Arrays.asList("5,3", "6,3"), lbl_r3c4_across);
        runLabelMap.put(Arrays.asList("1,4", "2,4"), lbl_r4c0_across);
        runLabelMap.put(Arrays.asList("5,4", "6,4"), lbl_r4c4_across);
        runLabelMap.put(Arrays.asList("1,5", "2,5", "3,5", "4,5", "5,5", "6,5"), lbl_r5c0_across);
        runLabelMap.put(Arrays.asList("2,6", "3,6", "4,6", "5,6", "6,6"), lbl_r6c1_across);


        runLabelMap.put(Arrays.asList("1,1", "1,2", "1,3", "1,4", "1,5"), lbl_r0c1_down);
        runLabelMap.put(Arrays.asList("2,1", "2,2", "2,3", "2,4", "2,5", "2,6"), lbl_r0c2_down);
        runLabelMap.put(Arrays.asList("3,1", "3,2"), lbl_r0c3_down);
        runLabelMap.put(Arrays.asList("3,5", "3,6"), lbl_r4c3_down);
        runLabelMap.put(Arrays.asList("4,1", "4,2"), lbl_r0c4_down);
        runLabelMap.put(Arrays.asList("4,5", "4,6"), lbl_r4c4_down);
        runLabelMap.put(Arrays.asList("5,1", "5,2", "5,3", "5,4", "5,5", "5,6"), lbl_r0c5_down);
        runLabelMap.put(Arrays.asList("6,2", "6,3", "6,4", "6,5", "6,6"), lbl_r1c6_down);

        for (Map.Entry<List<String>, Label> entry : runLabelMap.entrySet()) {
            int sum = 0;
            for (String cell : entry.getKey()) {
                sum += solution.get(cell);
            }
            if (entry.getValue() != null) {
                entry.getValue().setText(String.valueOf(sum));
            }
        }
    }

    //This method as the listener for the player's input. It restricts inputs to a single digit 1-9,
    //the digit will turn green if correct, red if wrong
    //triggers a win check after every correct entry
    private void clearFieldsForPlayer() {
        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            TextField tf = entry.getValue();
            tf.clear();

            //This sets up a listener that fires every time the text in the field changes
            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                // Only allow single digit 1–9
                if (!newVal.matches("[1-9]?")) {
                    tf.setText(oldVal);
                    return;
                }
                //This acts as the second checkpoint to only allows single digits
                if (newVal.length() > 1) {
                    tf.setText(newVal.substring(newVal.length() - 1));
                    return;
                }


                if (newVal.isEmpty()) {
                    tf.setStyle("-fx-background-color: #fff;");
                } else if (Integer.parseInt(newVal) == solution.get(key)) {
                    tf.setStyle("-fx-text-fill: #00bf63;" +
                            "-fx-background-color:#fff;" +
                            "-fx-border-radius:0px;" +
                            "-fx-border-color:transparent;"); // green
                    checkIfAllCorrect();
                } else {
                    tf.setStyle("-fx-text-fill: #c82121;"); // red
                }
            });
        }
    }

    //This method checks whether the player has correctly filled in the entire puzzle
    private void checkIfAllCorrect() {
        //It goes through evey cell
        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            //As it goes to every cell, it grabs the players input and checks it the field is empty or not a solution.
            String input = entry.getValue().getText().trim();

            //If either is true for even one cell the method immediately returns- meaning the puzzle is not complete yet.
            if (input.isEmpty() || Integer.parseInt(input) != solution.get(entry.getKey())) {
                return;
            }
        }
        //If all cells are correntm the timer will stop and call the levelachievement() method
        timer.stop();
        levelAchievement();

    }

    //This method allows to go to another fxml, where it shows the performance of the player for the previous game.
    private void levelAchievement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("level_accomplishment_hard.fxml"));
            Parent root = loader.load();

            AchievementHardController ac = loader.getController();
            int timeTaken = (15 * 60) - secondsLeft;
            ac.setStats(secondsLeft, timeTaken);

            Stage stage = (Stage) backbuttonHard.getScene().getWindow();
             stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //This method prepares the list of cells, to support the process of backtrack
    private void generateSolution() {
        //It clears all the previous stored solution
        solution.clear();

        //Gets all the cells keys from the fieldMap and puts them into a list.
        List<String> cells = new ArrayList<>(fieldMap.keySet());

        //it begins with the first cell in the list.
        // Backtrack will recursively fill in each cell with a valid digit until the entire puzzle is solved
        backtrack(cells, 0);
    }

    private boolean backtrack(List<String> cells, int index) {
        //If index has reached the end of the cell list, it means every cell has been successfully filled.
        // It return true to signal that the puzzle is complete
        if (index == cells.size())
            return true;

        //it grabs the currect cell to woek on for example ("1,1", or "2,3").
        String key = cells.get(index);

        //the algorithm tries digits in a different order each time
        List<Integer> digits = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

        Collections.shuffle(digits); // random order each time

        /*
        This is the core loop. For each digit it:

        1. Checks if the digit is valid for this cell (no conflict in its row/column group)
        2. If valid, places it in the solution
        3. Recursively moves to the next cell (index + 1)
        4. If the recursive call eventually returns true, the whole thing succeeds and bubbles true back up
        5.  If it returns false (dead end), it removes the digit and tries the next one — this is the "backtracking" part
        6. If all 9 digits fail, it returns false, telling the previous call to backtrack as well
         */
        for (int digit : digits) {
            if (isValid(key, digit)) {
                solution.put(key, digit);
                if (backtrack(cells, index + 1)) return true;
                solution.remove(key);
            }
        }
        return false;
    }

    //This method make sure that there is no duplicate within the same row or column group
    private boolean isValid(String key, int digit) {
        //Goes through every run.
        for (List<String> run : RUNS) {
            //if the current cell doesn't belong to that run, it will be skipped and moves to the next one
            if (!run.contains(key))
                continue;
            //If a matching digit is found in the same run, it returns false- the number is not a part of the solution
            for (String other : run) {
                if (!other.equals(key) && solution.getOrDefault(other, -1) == digit)
                    return false;
            }
        }
        return true;
    }


   /* private void gameFailed() {
        saveGameToState();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hard_level_failed.fxml"));
            Parent root = loader.load();

            AchievementHardController ac = loader.getController();

            Stage stage = (Stage) backbuttonHard.getScene().getWindow();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
    //This method is responsible for saving the game state and the action of the back button
    @FXML
    private void backbutton(ActionEvent event) {

        //This section will save the current state of the game
        GameState state = GameState.getInstance();
        state.hardSolution = new HashMap<>(solution);
        state.secondsLeft = secondsLeft;
        state.hintsLeft = hintsLeft;
        state.hasSavedState = true;

        //this will save the fields' value and style
        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            state.hardFieldValues.put(entry.getKey(), entry.getValue().getText());
            state.hardFieldStyles.put(entry.getKey(), entry.getValue().getStyle());
        }
        timer.stop();
        try {
            FXMLLoader backbuttonLoader = new FXMLLoader(getClass().getResource("start_page.fxml"));
            Stage stage = (Stage) backbuttonHard.getScene().getWindow();
            Parent root = backbuttonLoader.load();
            stage.getScene().setRoot(root);
            SettingsController.setupGlobalClickSounds(stage.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to save the current logical data of the round
    private void saveGameToState() {
        GameState state = GameState.getInstance();
        //Restore saved state
        if (state.hasSavedState) {
            displaySums();
            clearFieldsForPlayer();

            //Restore the field values and styles
            for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
                String key = entry.getKey();
                TextField tf = entry.getValue();
                tf.setText(state.hardFieldValues.getOrDefault(key, ""));
                tf.setStyle(state.hardFieldStyles.getOrDefault(key, ""));
            }
        }

    }


}
