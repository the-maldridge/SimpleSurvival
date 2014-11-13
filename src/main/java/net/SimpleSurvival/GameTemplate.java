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

    public boolean isReady() {
        return competitors.size()>=minPlayers;
    }

    public boolean isFull() {
        return competitors.size()>=maxPlayers;
    }

    public List<Integer[]> getSpawns() {
        return spawns;
    }

    public HashMap<Material, Double> getLoot() {
        return loot;
    }

    public List<Material> getBreakables() {
        return breakables;
    }

    private  List<Integer[]> spawns;
    private  HashMap<Material, Double> loot;
    private  List<Material> breakables;

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
        this.maxPlayers = plugin.getConfig().getInt("worlds." + sourceWorld + ".maxPlayers");

        List<String> spawns = plugin.getConfig().getStringList("worlds." + sourceWorld + ".spawns");
        this.spawns = new ArrayList<>();
        for(String point: spawns) {
            String[] xyzStr = point.split(",");
            Integer[] xyz = new Integer[3];
            for(int i = 0; i < 3; ++i) {
                System.out.println("Parsing XYZ: " + xyzStr[i].trim());
                xyz[i] = Integer.parseInt(xyzStr[i].trim());
            }
            this.spawns.add(xyz);
        }

        System.out.println("Checking for loot");
        List<Map<?, ?>> lootMaps = (List<Map<?, ?>>)plugin.getConfig().getMapList("worlds." + sourceWorld + ".loot");
        this.loot = new HashMap<>();
        for(Map<?, ?> map: lootMaps) {
			for(Object key: map.keySet()) {
				System.out.println("Found loot " + (String)key);
				this.loot.put(Material.getMaterial((String)key), (Double)map.get(key));
			}
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

    public GameManager createGame(SimpleSurvival plugin) {
        // Spins off a new GameSettings from GameTemplate to represent a running game
        GameManager val = new GameManager(plugin, this, competitors);
        for(String p: competitors) {
            Bukkit.getPlayer(p).sendMessage("The game is about to begin");
        }
        this.competitors = new ArrayList<>();
        return val;
    }
    //TODO write back to the disk
    public boolean addSpawn(Location loc) {
        Integer[] currentLoc = new Integer[3];
        currentLoc[0] = (int)loc.getX();
        currentLoc[1] = (int)loc.getY();
        currentLoc[2] = (int)loc.getZ();

        spawns.add(currentLoc);

        return true;
    }

    public boolean setSpawn(int index, Location loc) {
        Integer[] currentLoc = new Integer[3];
        currentLoc[0] = (int)loc.getX();
        currentLoc[1] = (int)loc.getY();
        currentLoc[2] = (int)loc.getZ();

        spawns.set(index, currentLoc);

        return true;
    }

}
