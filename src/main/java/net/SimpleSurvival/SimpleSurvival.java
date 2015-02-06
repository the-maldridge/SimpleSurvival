package net.SimpleSurvival;

import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
	ArrayList<GameManager> warpable = new ArrayList<>();
	ArrayList<GameManager> startable = new ArrayList<>();
	ArrayList<int[]> spawns = new ArrayList<int[]>();

	WorldManager worldManager = new WorldManager(this);

	public SimpleSurvival() {
		String[] worlds = this.getDataFolder().list();
		for (String world : worlds) {
			if ((new File(this.getDataFolder(), world).isDirectory())) {
				this.getLogger().info("Found template world " + world);
				gameTemplates.put(world, new GameTemplate(this, world, world));
			}
		}
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			SimpleSurvival plugin = SimpleSurvival.this;

			@Override
			public void run() {
				for (GameTemplate game : this.plugin.gameTemplates.values()) {
					if (game.isReady()) {
						// Get the settings for the world
						GameManager manager = game.createGame(this.plugin);
						// Copy the world data into the running worlds
						this.plugin.worldManager.newWorldFromTemplate(manager);
						// Send the players there
						if (manager.doAutoWarp()) {
							manager.sendPlayersToSpawn();
							// start the game
							if (manager.doAutoStart()) {
								manager.start();
							} else {
								this.plugin.startable.add(manager);
							}
							this.plugin.runningGames.add(manager);

						} else {
							this.plugin.getLogger().info(manager.toString() + " ready for manual warp");
							this.plugin.warpable.add(manager);
						}
					}
				}

				for (int i = 0; i < this.plugin.runningGames.size(); i++) {
					if (this.plugin.runningGames.get(i).getState() == GameManager.GameState.FINISHED) {
						this.plugin.runningGames.remove(i--).end(false);
					}
				}
			}
		}, 0L, 200L);
	}

	@Override
	public void onDisable() {
		endAllGames();
	}

	public void endAllGames() {
		for(GameManager game: runningGames) {
			game.end(true);
		}
		runningGames.clear();
		for(GameManager game: warpable) {
			game.end(true);
		}
		warpable.clear();
		for(GameManager game: startable) {
			game.end(true);
		}
		startable.clear();
	}
	public void addSpawn(Player player) {
		int[] loc = new int[3];
		loc[0] = (int)player.getLocation().getX();
		loc[1] = (int)player.getLocation().getY();
		loc[2] = (int)player.getLocation().getZ();
		spawns.add(loc);
	}

	public void dumpSpawns(Player player) {
		for(int[] loc: spawns) {
			player.sendMessage("- "+loc[0]+","+loc[1]+","+loc[2]);
		}

		ArrayList<String> spawnString = new ArrayList<>();
		for(int[] loc: spawns) {
			spawnString.add(loc[0]+","+loc[1]+","+loc[2]);
		}
		this.getConfig().set("worlds."+player.getWorld().toString()+".spawns", spawnString);
		try {
			this.getConfig().save("config.yml");
		} catch(IOException e) {
			this.getLogger().severe("Could not write config section" + e.toString());
		}
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt) {
		// Make sure players are not registered for events that haven't fired
		String player = evt.getPlayer().getName();

		for (GameTemplate game : gameTemplates.values()) {
			if (game.hasCompetitor(player)) {
				game.removeCompetitor(player);
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String cmdName = cmd.getName();
		if (cmdName.equalsIgnoreCase("survival")) {
			// Add the player to the list for the game they are attempting to enter
			if (!(sender instanceof Player)) {
				sender.sendMessage("It is assumed that only Players may register for a game");
				return true;
			}
			String player = ((Player) sender).getName();

			// NOTE: Assumes that games do not have a space in the name
			if (args.length != 2) {
				sender.sendMessage(gameTemplates.keySet().toString());
				return true;
			}


			String gameName = args[1];
			if (!gameTemplates.containsKey(gameName)) {
				sender.sendMessage("Could not find the game " + gameName);
				return true;
			}

			GameTemplate game = gameTemplates.get(gameName);
			if (args[0].equalsIgnoreCase("register")) {
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

				if (game.isFull()) {
					sender.sendMessage("That game is full");
					return true;
				}

				// Finally, there are no problems, so we can add the player to the list
				game.addCompetitor(player);
				sender.sendMessage("Successfully registered");
				return true;
			}

			if(args[0].equalsIgnoreCase("unregister")) {
				if (!game.hasCompetitor(player)) {
					sender.sendMessage("You aren't registered for that game");
					return true;
				}

				game.removeCompetitor(player);
				sender.sendMessage("Successfully unregistered");
				return true;
			}
		} else if (cmdName.equalsIgnoreCase("warp")) {
			if (args.length == 0) {
				sender.sendMessage("Warpable games: " + warpable.toString());
			} else if (args.length == 1) {
				for (GameManager game : warpable) {
					if ((game.getWorld()+"-"+game.getWorldUUID().substring(0,4)).equalsIgnoreCase(args[0])) {
						game.sendPlayersToSpawn();
						warpable.remove(game);
						if(game.doAutoStart()) {
							runningGames.add(game);
						} else {
							startable.add(game);
						}
						return true;
					}
				}
				sender.sendMessage("The game you entered wasn't found.");
				return true;
			} else {
				return false;
			}
		} else if (cmdName.equalsIgnoreCase("start")) {
			if (args.length == 0) {
				sender.sendMessage("Startable games: " + startable.toString());
			} else if (args.length == 1) {
				for (GameManager game : startable) {
					if ((game.getWorld()+"-"+game.getWorldUUID().substring(0,4)).equalsIgnoreCase(args[0])) {
						game.start();
						startable.remove(game);
						runningGames.add(game);
						return true;
					}
				}
				sender.sendMessage("The game you entered wasn't startable.");
			} else {
				return false;
			}
		} else if(cmdName.equalsIgnoreCase("endall")) {
			endAllGames();
			return true;
		} else if(cmdName.equalsIgnoreCase("addspawn")) {
			addSpawn((Player)sender);
			return true;
		} else if(cmdName.equalsIgnoreCase("dumpspawns")) {
			dumpSpawns((Player)sender);
			return true;
		} else if(cmdName.equalsIgnoreCase("clearspawns")) {
			spawns = new ArrayList<>();
			return true;
		}
		//if we've made it here no command handler could fire
		return false;
	}
}