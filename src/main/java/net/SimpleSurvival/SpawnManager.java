package net.SimpleSurvival;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import java.util.ArrayList;

/**
 * Created by maldridge on 10/21/14.
 */
public class SpawnManager {
	private ArrayList<Location> spawnPointList = new ArrayList<Location>();

	ArrayList<Location> getSpawnList() {
		return spawnPointList;
	}
}
