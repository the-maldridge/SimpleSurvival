package net.SimpleSurvival;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import java.util.ArrayList;

/**
 * Created by maldridge on 10/21/14.
 */
public class SpawnManager {
    public boolean sendPlayersToSpawn(GameSettings currentGame) {
        for (int i = 0; i < currentGame.competitors.size(); i++) {
            int x = currentGame.spawns[i][1];
            int y = currentGame.spawns[i][2];
            int z = currentGame.spawns[i][3];
            World w = Bukkit.getWorld(currentGame.gameWorld);
            Location nextSpawn = new Location(w, x, y, z);
            Bukkit.getPlayer(currentGame.competitors.get(i)).teleport(nextSpawn);
        }
        return true;
    }
}