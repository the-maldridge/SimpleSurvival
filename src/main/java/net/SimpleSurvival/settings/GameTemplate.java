package net.SimpleSurvival.settings;

import net.SimpleSurvival.SimpleSurvival;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Reid Levenick on 11/9/14.
 */
public class GameTemplate {
    private final int minPlayers;
    private final int maxPlayers;
    private final List<List<Integer>> spawns;
    private final HashMap<Material, Double> loot;
    private final List<Material> breakables;
    // The people 'lined up' for this game
    private ArrayList<String> competitors = new ArrayList<>();
    // The name of the 'backup' world that this world is going to be initialized from
    private String sourceWorld;
    // The display name of this template, e.g. 'castles' or somesuch
    private String displayName;

    public GameTemplate(SimpleSurvival plugin, String sourceWorld, String displayName) {
        this.sourceWorld = sourceWorld;
        this.displayName = displayName;
        this.minPlayers = plugin.getConfig().getInt("worlds." + sourceWorld + ".minPlayers");
        this.maxPlayers = plugin.getConfig().getInt("worlds." + sourceWorld + ".minPlayers");

        this.spawns = (List<List<Integer>>) plugin.getConfig().getList("worlds."+sourceWorld+".spawns");

        HashMap<String, Double> lootNames = new HashMap<>();
        plugin.getConfig().createSection("worlds." + sourceWorld + ".loot", lootNames);
        this.loot = new HashMap<>();
        for(String key: lootNames.keySet()) {
            this.loot.put(Material.getMaterial(key), lootNames.get(key));
        }

        List<String> breakableNames = plugin.getConfig().getStringList("worlds." + sourceWorld + ".breakables");
        this.breakables = new ArrayList<Material>();
        for(String breakable: breakableNames) {
            breakables.add(Material.getMaterial(breakable));
        }
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

    public GameSettings createSettings() {
        // Spins off a new GameSettings from GameTemplate to represent a running game
        GameSettings val = new GameSettings(competitors, this);
        this.competitors = new ArrayList<>();
        return val;
    }
}
