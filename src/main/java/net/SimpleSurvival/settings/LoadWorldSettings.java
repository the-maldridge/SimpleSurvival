package net.SimpleSurvival.settings;

import net.SimpleSurvival.SimpleSurvival;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by maldridge on 11/9/14.
 */
public class LoadWorldSettings {
    private SimpleSurvival plugin;

    public LoadWorldSettings(SimpleSurvival plugin) {
        this.plugin= plugin;
    }

    public WorldSettings getWorldSettings(String worldName) {
        WorldSettings worldSettings = new WorldSettings();

        worldSettings.minPlayers = this.plugin.getConfig().getInt("worlds." + worldName + ".minPlayers");
        worldSettings.maxPlayers = this.plugin.getConfig().getInt("worlds."+worldName+".minPlayers");
        worldSettings.spawns = (ArrayList<Integer[]>)this.plugin.getConfig().getList("worlds."+worldName+".spawns");

        HashMap<String, Double> lootNames = new HashMap<>();
        this.plugin.getConfig().createSection("worlds." + worldName + ".loot", lootNames);
        worldSettings.loot = new HashMap<>();
        for(String key: lootNames.keySet()) {
            worldSettings.loot.put(Material.getMaterial(key), lootNames.get(key));
        }

        worldSettings.breakables = (ArrayList<Material>)this.plugin.getConfig().getList("worlds."+worldName+".breakables");

        return worldSettings;
    }
}
