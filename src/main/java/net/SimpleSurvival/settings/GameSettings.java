package net.SimpleSurvival.settings;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by maldridge on 11/8/14.
 */
public class GameSettings {
    // so we can get things like the loot table and the breakables list and the spawn positions later
    // and the fancy display name if oyu're into that kind of thing
    private GameTemplate staticSettings;

    public String getRunningWorld() {
        return runningWorld;
    }

    private String runningWorld;

    public ArrayList<String> getCompetitors() {
        return competitors;
    }

    public GameSettings(ArrayList<String> competitors, GameTemplate staticSettings) {
        this.competitors = competitors;
        this.staticSettings = staticSettings;
    }

    /*
    Players competing
     */
    private ArrayList<String> competitors = new ArrayList<String>();

    /*
    Game state, gives an indication of where the game is
     */
    public enum GameState {
        RUNNING, PAUSED, FINISH
    }

    public List<List<Integer>> getSpawns() {
        return this.staticSettings.getSpawns();
    }

    private GameState state;

    public GameState getState() {
        return state;
    }


}
