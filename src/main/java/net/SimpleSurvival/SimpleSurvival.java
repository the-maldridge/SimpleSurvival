package net.SimpleSurvival;

import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
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
    // The keys of this hashmap are the source names
	HashMap<String, GameTemplate> gameTemplates = new HashMap<String, GameTemplate>();
    ArrayList<GameManager> runningGames = new ArrayList<>();
    WorldManager worldManager = new WorldManager(this);
    String homeworld = "world";

    public SimpleSurvival() {
        String[] worlds = this.getDataFolder().list();
        for(String world: worlds) {
            if ((new File(this.getDataFolder(), world).isDirectory())) {
                this.getLogger().info("Found template world " + world);
                gameTemplates.put(world, new GameTemplate(this, world, world));
            }
        }
        homeworld = this.getConfig().getString("lobby");
    }

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
        this.saveDefaultConfig();
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            SimpleSurvival plugin = SimpleSurvival.this;
            @Override
            public void run() {
                for (GameTemplate game : this.plugin.gameTemplates.values()) {
                    if (game.isReady()) {
                        // FIXME: Btw I changed this maldridge, look at it because I'm going to forget
                        // Get the settings for the world
                        GameManager manager = game.createGame(this.plugin);
                        // Copy the world data into the running worlds
                        this.plugin.worldManager.newWorldFromTemplate(manager.getWorld(), manager.getWorldUUID());
                        // Load the world into memory
                        this.plugin.getServer().createWorld(new WorldCreator(manager.getWorldUUID()));
                        // Send the players there
                        manager.sendPlayersToSpawn();
                        // Add the world to the running games
                        System.out.println("world loaded, about to start");
                        this.plugin.runningGames.add(manager);
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
				return true;
			}

			String player = ((Player) sender).getName();

			// NOTE: Assumes that games do not have a space in the name
			if (args.length != 2) {
				return false;
			}

			boolean isRegistering;

			if(args[0].equalsIgnoreCase("register")) {
				isRegistering = true;
			} else if(args[0].equalsIgnoreCase("unregister")) {
				isRegistering = false;
			} else {
				return false;
			}

			String gameName = args[1];

			if (!gameTemplates.containsKey(gameName)) {
				sender.sendMessage("Could not find the game " + gameName);
				return true;
			}

			GameTemplate game = gameTemplates.get(gameName);

			if(isRegistering) {
				if (game.hasCompetitor(player)) {
					sender.sendMessage("You are already registered for that game");
					return true;
				}

				for (String key : gameTemplates.keySet()) {
					if (gameTemplates.get(key).hasCompetitor(player)) {
						sender.sendMessage("You are already in the game " + key);
						return true;
					}
				}

                if(game.isFull()) {
                    sender.sendMessage("That game is full");
                    return true;
                }

				// Finally, there are no problems, so we can add the player to the list
				game.addCompetitor(player);
				sender.sendMessage("Successfully registered");
				return true;
			} else {
				if (!game.hasCompetitor(player)) {
					sender.sendMessage("You aren't registered for that game");
					return true;
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
		}
		return false;
	}

	private boolean addSpawn(Player player) {
		String worldName = player.getLocation().getWorld().getName();
		boolean success = gameTemplates.get(worldName).addSpawn(player.getLocation());
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

		if(gameTemplates.containsKey(worldName)) {
			GameTemplate game = gameTemplates.get(worldName);
			if(spawnNum < game.getSpawns().size()) {
				boolean success = game.setSpawn(spawnNum, player.getLocation());
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
}