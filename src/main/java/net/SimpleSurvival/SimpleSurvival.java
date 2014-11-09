package net.SimpleSurvival;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.World;
import org.bukkit.Server;

import java.util.HashMap;

/**
 * Created by maldridge on 11/8/14.
 */
public class SimpleSurvival extends JavaPlugin {
	CommandParser commandParser = new CommandParser();
	SpawnManager spawnManager = new SpawnManager();
	HashMap<String, WorldSettings> worldSettings = new HashMap<String, WorldSettings>();

	public SimpleSurvival() {
		for(World world : getServer().getWorlds()) {
			worldSettings.put(world.getName(), new WorldSettings());
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return commandParser.parseCommand(sender, cmd, label, args);
	}

	private class CommandParser {
		public boolean parseCommand(CommandSender sender, Command cmd, String label, String[] args) {
			String cmdName = cmd.getName();
			if(cmdName.equalsIgnoreCase("addSpawn")) {
				return addSpawn((Player)sender);
			} else if(cmdName.equalsIgnoreCase("changeSpawn")) {
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

			if(worldSettings.containsKey(worldName)) {
				WorldSettings settings = worldSettings.get(worldName);
				if(spawnNum < settings.spawns.size()) {
					return spawnManager.setSpawn(settings, spawnNum, player.getLocation());
				} else {
					player.sendMessage("Not enough spawn points already exist.");
					return true;
				}
			} else {
				player.sendMessage("World settings do not exist.");
				return true;
			}
		}
	}
}
