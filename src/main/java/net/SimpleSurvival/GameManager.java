package net.SimpleSurvival;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.Location;

import java.util.ArrayList;

import net.SimpleSurvival.SpawnManager;

/**
 * Created by maldridge on 10/21/14.
 */
public class GameManager {
	private SpawnManager spawnManager = new SpawnManager();
	private CommandParser commandParser = new CommandParser();

	private class CommandParser {
		public boolean parseCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(cmd.getName().equalsIgnoreCase("setSpawn")) {
				return setSpawn((Player)sender, cmd, label, args);
			} else if(cmd.getName().equalsIgnoreCase("addSpawn")) {
				return addSpawn((Player)sender, cmd, label, args);
			}
			return false;
		}

		private boolean setSpawn(Player sender, Command cmd, String label, String[] args) {
			if(args.length != 1) return false;

			int spawnNum;
			try {
				spawnNum = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				return false;
			}

			ArrayList<Location> spawnPoints = spawnManager.getSpawnList();

			if(spawnPoints.size() < spawnNum) {
				sender.sendMessage("There are not " + spawnNum + " spawn points yet.");
			} else
			{
				spawnPoints.set(spawnNum, sender.getLocation());
			}
			return true;
		}

		private boolean addSpawn(Player sender, Command cmd, String label, String[] args) {
			if(args.length != 0) return false;

			ArrayList<Location> spawnPoints = spawnManager.getSpawnList();

			spawnPoints.add(sender.getLocation());

			return true;
		}
	}
}
