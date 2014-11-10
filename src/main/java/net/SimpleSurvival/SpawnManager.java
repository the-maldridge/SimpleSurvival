package net.SimpleSurvival;

import net.SimpleSurvival.settings.GameSettings;
import net.SimpleSurvival.settings.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;

/**
 * Created by maldridge on 10/21/14.
 */
public class SpawnManager {
    public boolean sendPlayersToSpawn(GameSettings currentGame) {
        for (int i = 0; i < currentGame.competitors.size(); i++) {
            int x = currentGame.spawns.get(i)[0];
            int y = currentGame.spawns.get(i)[1];
            int z = currentGame.spawns.get(i)[2];
            World w = Bukkit.getWorld(currentGame.gameWorld);
            Location nextSpawn = new Location(w, x, y, z);
            Bukkit.getPlayer(currentGame.competitors.get(i)).teleport(nextSpawn);
        }
        return true;
    }

	public boolean addSpawn(WorldSettings currentWorld, Location loc) {
		Integer[] currentLoc = new Integer[3];
		currentLoc[0] = (int)loc.getX();
		currentLoc[1] = (int)loc.getY();
		currentLoc[2] = (int)loc.getZ();

		currentWorld.spawns.add(currentLoc);

		return true;
	}

	public boolean setSpawn(WorldSettings currentWorld, int index, Location loc) {
		Integer[] currentLoc = new Integer[3];
		currentLoc[0] = (int)loc.getX();
		currentLoc[1] = (int)loc.getY();
		currentLoc[2] = (int)loc.getZ();

		currentWorld.spawns.set(index, currentLoc);

		return true;
	}
}
