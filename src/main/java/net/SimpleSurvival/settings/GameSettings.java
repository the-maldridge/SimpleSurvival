package net.SimpleSurvival.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by maldridge on 11/8/14.
 */
public class GameSettings extends WorldSettings {
    /*
    Which world the game originates in
    */
    private String world;
    public String getWorld() {
        return world;
    }

    /*
    which world should be displayed as the game world
     */
    private String title;
    public String getTitle() {
        return title;
    }

    /*
    Players competing
     */
    private ArrayList<String> competitors = new ArrayList<String>();
    public void addCompetitor(String competitor) {
        competitors.add(competitor);
        Collections.sort(competitors);
    }
    public boolean hasCompetitor(String competitor) {
        return Collections.binarySearch(competitors, competitor) >= 0;
    }
    public void removeCompetitor(String competitor) {
        competitors.remove(competitor);
    }
    public List<String> getCompetitors() {
        return (List<String>)Collections.unmodifiableList(competitors);
    }

    /*
    Game state, gives an indication of where the game is
     */
    public enum GameState {PRESTART, RUNNING, PAUSED, FINISH}
    private GameState state;
    public GameState getState() {
        return state;
    }
    public void setState(GameState state) {
        this.state = state;
    }
}
