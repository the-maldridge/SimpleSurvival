package net.SimpleSurvival;

import net.SimpleSurvival.settings.GameSettings;
import net.SimpleSurvival.settings.GameTemplate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashMap;

// NOTE: It should be noticed that "settings to begin a game" and "settings for a currently running game"
// should *probably* be split, so that GameSettings doesn't have to interact with relatively static and relatively
// active data at the same time, so we can store an array of running games and an array of possible ways to run a game

/**
 * Created by maldridge on 11/8/14.
 */
public class SimpleSurvival extends JavaPlugin {
	// Map of games on particular worlds; Contains information about running/waiting games
	// TODO: Save game settings and load them in a useful manner
	// TODO: Remove players from the list of competitors when they disconnect
	HashMap<String, GameTemplate> gameTemplates = new HashMap<String, GameTemplate>();
    ArrayList<GameSettings> runningGames = new ArrayList<>();


	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt) {
		// Make sure players are not registered for events that haven't fired
		String player = evt.getPlayer().getName();

		for(GameTemplate game: gameTemplates.values()) {
			if(game.hasCompetitor(player)) {
				game.removeCompetitor(player);
			}
		}
	}

    public void onEnable() {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for(GameTemplate game: gameTemplates.values()) {
                    if(game.isReady()) {
                            runningGames.add(game.createSettings());
                    }
                }
            }
        }, 0L, 200L);
    }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String cmdName = cmd.getName();
		if(cmdName.equalsIgnoreCase("survival")) {
			// Add the player to the list for the game they are attempting to enter
			if (!(sender instanceof Player)) {
				sender.sendMessage("It is assumed that only Players may register for a game");
				return false;
			}

			String player = ((Player) sender).getName();

			// NOTE: Assumes that games do not have a space in the name
			if (args.length != 2) {
				sender.sendMessage("Game un/registration takes two arguments, in the form");
				sender.sendMessage("  register <name>");
				sender.sendMessage("unregister <name>");
				return false;
			}

			boolean isRegistering;

			if(args[1].equalsIgnoreCase("register")) {
				isRegistering = true;
			} else if(args[1].equalsIgnoreCase("unregister")) {
				isRegistering = false;
			} else {
				sender.sendMessage("Game registration takes 'register' or 'unregister' as the first argument");
				return false;
			}

			String gameName = args[2];

			if (!gameTemplates.containsKey(gameName)) {
				sender.sendMessage("Could not find the game " + gameName);
				return false;
			}

			GameTemplate game = gameTemplates.get(gameName);

			if(isRegistering) {
				if (game.hasCompetitor(player)) {
					sender.sendMessage("You are already registered for that game");
					return false;
				}

				for (String key : gameTemplates.keySet()) {
					if (gameTemplates.get(key).hasCompetitor(player)) {
						sender.sendMessage("You are already in the game " + key);
						return false;
					}
				}

				// Finally, there are no problems, so we can add the player to the list
				game.addCompetitor(player);
				sender.sendMessage("Successfully registered");
				return true;
			} else {
				if (!game.hasCompetitor(player)) {
					sender.sendMessage("You aren't registered for that game");
					return false;
				}

				game.removeCompetitor(player);
				sender.sendMessage("Successfully unregistered");
				return true;
			}
		} else if(cmdName.equalsIgnoreCase("addSpawn")) {
			// TODO: Add permission check
			return addSpawn((Player)sender);
		} else if(cmdName.equalsIgnoreCase("changeSpawn")) {
			// TODO: Add permission check
			if (args.length != 1) return false;
			return setSpawn((Player)sender, args[0]);
		} else if(cmdName.equalsIgnoreCase("getSpawns")) {
			// TODO: Add permission check
			return getSpawns((Player)sender);
		}
		return false;
	}

	private boolean addSpawn(Player player) {
		String worldName = player.getLocation().getWorld().getName();
		boolean success = spawnManager.addSpawn(worldSettings.get(worldName), player.getLocation());
		if(success) {
			player.sendMessage("Successfully added spawn.");
		}
		return success;
	}

	private boolean setSpawn(Player player, String arg) {
		String worldName = player.getLocation().getWorld().getName();
		int spawnNum;
		try {
			spawnNum = Integer.valueOf(arg);
			--spawnNum;
		} catch(NumberFormatException e) {
			return false;
		}

		if(worldSettings.containsKey(worldName)) {
			WorldSettings settings = worldSettings.get(worldName);
			if(spawnNum < settings.spawns.size()) {
				boolean success = spawnManager.setSpawn(settings, spawnNum, player.getLocation());
				if(success) {
					player.sendMessage("Spawn " + (spawnNum + 1) + " successfully changed.");
				}
				return success;
			} else {
				player.sendMessage("Not enough spawn points already exist.");
				return true;
			}
		} else {
			player.sendMessage("World settings do not exist.");
			return true;
		}
	}

	private boolean getSpawns(Player player) {
		String worldName = player.getLocation().getWorld().getName();
		WorldSettings settings = worldSettings.get(worldName);
		int i = 1;
		for(Integer[] spawn : settings.spawns) {
			player.sendMessage("Spawn " + i++ + ":");
			player.sendMessage("   x: " + spawn[0]);
			player.sendMessage("   y: " + spawn[1]);
			player.sendMessage("   z: " + spawn[2]);
		}
		return true;
	}
}
