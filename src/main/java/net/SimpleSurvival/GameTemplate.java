package net.SimpleSurvival;

import net.SimpleSurvival.GameManager;
import net.SimpleSurvival.SimpleSurvival;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by Reid Levenick on 11/9/14.
 */
public class GameTemplate {
    private int minPlayers=1;
    private int maxPlayers;
    private ArrayList<String> competitors = new ArrayList<>();
    private String sourceWorld;
    private String displayName;
    private  List<Integer[]> spawns;
    private  HashMap<Material, Double> loot;
    private  List<Material> breakables;
    private boolean allowHostileMobs;
    private boolean allowAnimals;
    private boolean autoStart;
    private boolean autoWarp;



    public GameTemplate(SimpleSurvival plugin, String sourceWorld, String displayName) {
        this.sourceWorld = sourceWorld;
        this.displayName = displayName;
        this.minPlayers = plugin.getConfig().getInt("worlds." + sourceWorld + ".minPlayers");
        this.maxPlayers = plugin.getConfig().getInt("worlds." + sourceWorld + ".maxPlayers");

        List<String> spawns = plugin.getConfig().getStringList("worlds." + sourceWorld + ".spawns");
        this.spawns = new ArrayList<>();
        for(String point: spawns) {
            String[] xyzStr = point.split(",");
            Integer[] xyz = new Integer[3];
            for(int i = 0; i < 3; ++i) {
                plugin.getLogger().fine("Parsing XYZ spawn for " + sourceWorld + ": " + xyzStr[i].trim());
                xyz[i] = Integer.parseInt(xyzStr[i].trim());
            }
            this.spawns.add(xyz);
        }

        System.out.println("Checking for loot");
        List<Map<?, ?>> lootMaps = (List<Map<?, ?>>)plugin.getConfig().getMapList("worlds." + sourceWorld + ".loot");
        this.loot = new HashMap<>();
        for(Map<?, ?> map: lootMaps) {
            for(Object key: map.keySet()) {
                plugin.getLogger().fine("Loading loot for " + sourceWorld + ": " + (String)key);
                this.loot.put(Material.getMaterial((String)key), (Double)map.get(key));
            }
        }

        List<String> breakableNames = plugin.getConfig().getStringList("worlds." + sourceWorld + ".breakables");
        this.breakables = new ArrayList<Material>();
        for(String breakable: breakableNames) {
            breakables.add(Material.getMaterial(breakable));
        }

        allowHostileMobs = plugin.getConfig().getBoolean("worlds." + sourceWorld + ".mobs");
        allowAnimals = plugin.getConfig().getBoolean("worlds." + sourceWorld + ".animals");
        autoStart = plugin.getConfig().getBoolean("worlds." + sourceWorld + ".autoStart");
        autoWarp = plugin.getConfig().getBoolean("worlds." + sourceWorld + ".autoWarp");
    }
    public GameManager createGame(SimpleSurvival plugin) {
        // Spins off a new GameSettings from GameTemplate to represent a running game
        GameManager val = new GameManager(plugin, this, competitors);
        for(String p: competitors) {
            Bukkit.getPlayer(p).sendMessage("The game is about to begin");
        }
        this.competitors = new ArrayList<>();
        return val;
    }

    public boolean isReady() {
        return competitors.size()>=minPlayers;
    }
    public boolean isFull() {
        return competitors.size()>=maxPlayers;
    }
    public boolean doHostileMobs() { return allowHostileMobs; }
    public boolean doAnimals() { return allowAnimals; }
    public boolean doAutoStart() { return autoStart; }
    public boolean doAutoWarp() { return autoStart; }

    public List<Integer[]> getSpawns() {
        return spawns;
    }
    public HashMap<Material, Double> getLoot() {
        return loot;
    }
    public List<Material> getBreakables() {
        return breakables;
    }
    public String getSourceWorld() {
        return sourceWorld;
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
}
