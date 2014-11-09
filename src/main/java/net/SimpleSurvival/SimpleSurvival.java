package net.SimpleSurvival;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

/**
 * Created by maldridge on 11/8/14.
 */
public class SimpleSurvival extends JavaPlugin {
	SpawnManager spawnManager = new SpawnManager();
	HashMap<String, WorldSettings> worldSettings = new HashMap<String, WorldSettings>();

	private class CommandParser {
		public boolean parseCommand(CommandSender sender, Command cmd, String label, String[] args) {
			String cmdName = cmd.getName();
			if(cmdName.equalsIgnoreCase("addSpawn")) {
				return addSpawn((Player)sender);
			} else if(cmdName.equalsIgnoreCase("setSpawn")) {
				return setSpawn((Player)sender, args[0]);
			}
			return false;
		}

		private boolean addSpawn(Player player) {
			String worldName = player.getLocation().getWorld().getName();
			return spawnManager.addSpawn(worldSettings.get(worldName), player.getLocation());
		}

		private boolean setSpawn(Player player, String arg) {
			String worldName = player.getLocation().getWorld().getName();
			int spawnNum;
			try {
				spawnNum = Integer.valueOf(arg);
			} catch(NumberFormatException e) {
				return false;
			}
			return spawnManager.setSpawn(worldSettings.get(worldName), spawnNum, player.getLocation());
		}
	}
}
