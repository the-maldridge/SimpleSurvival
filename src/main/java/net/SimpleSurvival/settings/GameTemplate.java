package net.SimpleSurvival.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Reid Levenick on 11/9/14.
 */
public class GameTemplate {
    // The people 'lined up' for this game
    private ArrayList<String> competitors = new ArrayList<>();
    // The name of the 'backup' world that this world is going to be initialized from
    private String sourceWorld;
    // The display name of this template, e.g. 'castles' or somesuch
    private String displayName;

    public GameTemplate(String sourceWorld, String displayName) {
        this.sourceWorld = sourceWorld;
        this.displayName = displayName;
    }

    public String getSourceWorld() {
        return sourceWorld;
    }

    public String getDisplayName() {
        return displayName;
    }

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

    public GameSettings createSettings() {
        // Spins off a new GameSettings from GameTemplate to represent a running game
        GameSettings val = new GameSettings(competitors, sourceWorld);
        this.competitors = new ArrayList<>();
        return val;
    }
}
