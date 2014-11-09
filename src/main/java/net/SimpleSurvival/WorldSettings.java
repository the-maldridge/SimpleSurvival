package net.SimpleSurvival;

import org.bukkit.Location;
import org.bukkit.Material;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class WorldSettings {
	/**
	 * Minimum number of players required for this world.
	 */
	public int minPlayers;
	/**
	 * Maximum number of players able to play in this world.
	 */
	public int maxPlayers;
	/**
	 * List of spawn points.
	 */
	public int[][] spawns;
	/**
	 * Hash of loot with item name as key and frequency as the value.
	 */
	public HashMap<Material, Double> loot;
	/**
	 * List of breakable blocks.
	 */
	public ArrayList<Material> breakables;
}
