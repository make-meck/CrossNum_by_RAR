package com.example.crossnum;
import java.util.HashMap;
import java.util.Map;
public class GameState {
    private static GameState instance;

    //It uses Map to save the progress of the game, such as the solution, the typed values, and style

    //This is for the saving the state of the easy mode
    public int[][] easyCellValues = new int[5][5];
    public boolean[][] easyCellIsSolution = new boolean[5][5];
    public boolean[][] easyCellIsResolved = new boolean[5][5];
    public int[] easyRowsSums = new int[5];
    public int[] easyColSums = new int[5];
    public int easyLives= 3;
    public int easyHints= 3;
    public int easyCellsResolved = 0;
    public boolean hasEasySavedState = false;
    public boolean easyRetryMode = false;

    //This is for the saving the state of the easy mode
    public int[][] mediumCellValues = new int[7][7];
    public boolean[][] mediumCellIsSolution = new boolean[7][7];
    public boolean[][] mediumCellIsResolved = new boolean[7][7];
    public int[] mediumRowsSums = new int[7];
    public int[] mediumColSums = new int[7];
    public int mediumLives= 3;
    public int mediumHints= 3;
    public int mediumCellsResolved = 0;
    public boolean hasMediumSavedState = false;
    public boolean mediumRetryMode = false;

    //This is for saving the state of the hard mode
    public Map<String, Integer> hardSolution = new HashMap<>();
    public Map<String, String> hardFieldValues = new HashMap<>();
    public Map<String, String> hardFieldStyles= new HashMap<>();
    public int secondsLeft = 15*60;
    public int hintsLeft = 3;
    public boolean hasSavedState= false;
    public boolean hardRetryMode = false;


    public static GameState getInstance(){
        if(instance==null){
            instance= new GameState();
        }
        return instance;
    }
}
