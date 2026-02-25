package com.example.crossnum;
import java.util.HashMap;
import java.util.Map;
public class GameState {
    private static GameState instance;

    //It uses Map to save the progress of the game, such as the solution, the typed values, and style
    public Map<String, Integer> solution = new HashMap<>();
    public Map<String, String> fieldValues = new HashMap<>();
    public Map<String, String> fieldStyles= new HashMap<>();
    public int secondsLeft = 15*60;
    public int hintsLeft = 3;
    public boolean hasSavedState= false;

    public static GameState getInstance(){
        if(instance==null){
            instance= new GameState();
        }
        return instance;
    }
}
