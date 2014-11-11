package net.SimpleSurvival;

import net.SimpleSurvival.settings.GameSettings;
import org.bukkit.*;
import org.bukkit.entity.Player;

import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Map;


/**
 * Created by maldridge on 10/21/14.
 */
public class GameManager implements Listener {
    private final SimpleSurvival plugin;
    GameSettings currentGame;

    public GameManager(SimpleSurvival plugin, GameSettings currentGame) {
        this.currentGame = currentGame;
        this.plugin = plugin;
        System.out.println("players passed to us " + currentGame.getCompetitors().toString());
        sendPlayersToSpawn();
        this.plugin.getServer().getPluginManager().registerEvents(new GameEvents(this.plugin, this.currentGame), this.plugin);
        BukkitTask countDownTimer = new GameStarter(this.plugin, this.currentGame, 15).runTaskTimer(this.plugin, 0, 20);
    }



    public boolean sendPlayersToSpawn() {
        System.out.println(currentGame.getCompetitors().size());
        for (int i = 0; i < currentGame.getCompetitors().size(); i++) {
            int x = currentGame.getSpawns().get(i)[0];
            int y = currentGame.getSpawns().get(i)[1];
            int z = currentGame.getSpawns().get(i)[2];
            World w = Bukkit.getWorld(currentGame.getWorldUUID());
            Location nextSpawn = new Location(w, x, y, z);
            System.out.println("Teleporting " + currentGame.getCompetitors().get(i) + " to " + currentGame.getWorldUUID());
            Bukkit.getPlayer(currentGame.getCompetitors().get(i)).teleport(nextSpawn);
        }
        return true;
    }
}

class GameEvents implements Listener {
    private final GameSettings currentGame;
    SimpleSurvival plugin;
    ArrayList<String> spectators = new ArrayList<String>();
    private ArrayList<InventoryHolder> openedChests = new ArrayList<InventoryHolder>();

    public GameEvents(SimpleSurvival plugin, GameSettings currentGame) {
        this.plugin = plugin;
        this.currentGame = currentGame;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (currentGame.getCompetitors().contains(event.getPlayer().getName())) {
            if (currentGame.getState() == GameSettings.GameState.PAUSED) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent p) {
        if ((p != null) && (p instanceof Player)) {
            Player player = (Player) p;
            Player killer = player.getKiller();


            spectators.add(currentGame.getCompetitors().remove(currentGame.getCompetitors().indexOf(killer.getName())));
            setSpectatorMode();
            for (Player pl : player.getWorld().getPlayers()) {
                pl.sendMessage(ChatColor.RED + "[DEATH]" + ChatColor.BOLD + player.getName() + " was killed by " + ChatColor.BOLD + killer.getName());
            }
        }
    }

    private void setSpectatorMode() {
        for(int i=0; i<spectators.size(); i++) {
            Player p = Bukkit.getPlayer(spectators.get(i));
            p.setGameMode(GameMode.ADVENTURE);
            p.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent inventoryOpenEvent) {
        String player = inventoryOpenEvent.getPlayer().getName();
        Inventory inventory = inventoryOpenEvent.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof Chest || holder instanceof DoubleChest) { 
            if (spectators.contains(player)) {
                inventoryOpenEvent.setCancelled(true);
                return;
            } else {
                System.out.println("player check passed");
            }

            if (!openedChests.contains(holder)) {
                for (Map.Entry<Material, Double> lootEntry : currentGame.getLoot().entrySet()) {
                    System.out.println("Checking loot entry " + lootEntry.toString());
                    if (Math.random() * 100 < lootEntry.getValue()) {
                        System.out.println("lootchance passed");
                        inventory.addItem(new ItemStack(lootEntry.getKey()));
                    } else {
                        System.out.println("insufficient loot chance");
                    }
                }
            } else {
                System.out.println("seen this chest before");
            }
        } else {
            System.out.println("not a chest");
        }
    }
}

class GameStarter extends BukkitRunnable {

    private final SimpleSurvival plugin;
    private GameSettings currentGame;
    private int countdown;

    public GameStarter(SimpleSurvival plugin, GameSettings currentGame, int countdown) {
        this.plugin = plugin;
        this.currentGame = currentGame;
        this.countdown = countdown;
    }

    @Override
    public void run() {
        for(int i=0; i<currentGame.getCompetitors().size(); i++) {
            Player p = Bukkit.getPlayer(currentGame.getCompetitors().get(i));
            p.sendMessage("The game will begin in " + countdown + " seconds!");
        }
        countdown--;
        if(countdown<=0) {
            for(int i=0; i<currentGame.getCompetitors().size(); i++) {
                Player p = Bukkit.getPlayer(currentGame.getCompetitors().get(i));
                p.sendMessage("The game has begun!");
            }
            currentGame.setState(GameSettings.GameState.RUNNING);
            this.cancel();
        }
    }
}
