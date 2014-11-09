package net.SimpleSurvival;

import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maldridge on 11/8/14.
 */
public class GameSettings extends WorldSettings {
    /*
    Which world the game originates in
    */
    public String gameWorld;

    /*
    which world should be displayed as the game world
     */
    public String gameWorldTitle;

    /*
    Players competing
     */
    public ArrayList<String> competitors = new ArrayList<String>();

    /*
    Game Time, default is 15 minutes
     */
    public int timeLimit = 15;
    /*
    Game state, gives an indication of where the game is
     */
    public enum GameState {PRESTART, RUNNING, PAUSED, FINISH}
    public GameState state;

}
