package net.SimpleSurvival;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;


/**
 * Created by maldridge on 10/21/14.
 */
public class GameManager implements Listener {
    private final String worldUUID = UUID.randomUUID().toString();
    private final SimpleSurvival plugin;
    ArrayList<String> spectators = new ArrayList<String>();
    public enum GameState {BEFORE_GAME, STARTING, RUNNING, PAUSED, FINISHED}
    private GameState state = GameState.BEFORE_GAME;
    private ArrayList<InventoryHolder> openedChests = new ArrayList<InventoryHolder>();
    private GameTemplate staticSettings;
    private ArrayList<String> competitors;

    public GameManager(SimpleSurvival plugin, GameTemplate staticSettings, ArrayList<String> competitors) {
        this.plugin = plugin;
        this.staticSettings = staticSettings;
        this.competitors = competitors;
        this.plugin.getLogger().info("Players given to us " + competitors.toString());
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public ArrayList<String> getCompetitors() {
        return competitors;
    }

    public List<Integer[]> getSpawns() {
        return this.staticSettings.getSpawns();
    }

    public HashMap<Material, Double> getLoot() {
        return this.staticSettings.getLoot();
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public String getWorld() {
        return staticSettings.getSourceWorld();
    }

    public String getWorldUUID() {
        return worldUUID;
    }

    public boolean sendPlayersToSpawn() {
        for (int i = 0; i < competitors.size(); i++) {
            int x = staticSettings.getSpawns().get(i)[0];
            int y = staticSettings.getSpawns().get(i)[1];
            int z = staticSettings.getSpawns().get(i)[2];
            World w = Bukkit.getWorld(worldUUID);
            Location nextSpawn = new Location(w, x, y, z);
            Player player = Bukkit.getPlayer(competitors.get(i));
            player.teleport(nextSpawn);
            player.setAllowFlight(false);
            player.setGameMode(GameMode.SURVIVAL);
            player.setFoodLevel(20);
            player.setHealth(20);
            player.setLevel(0);
            player.getInventory().clear();
        }

        if(state == GameState.BEFORE_GAME) {
            new GameStarter(this.plugin, this, 15).runTaskTimer(this.plugin, 0, 20);
            state = GameState.STARTING;
        }

        return true;
    }

    public void end() {
        BukkitTask countDownTimer = new GameEnder(this.plugin, this, 10).runTaskTimer(this.plugin, 0, 20);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (competitors.contains(event.getPlayer().getName())) {
            if (state != GameState.RUNNING) {
                Vector to = event.getTo().toVector();
                Vector from = event.getFrom().toVector();
                Player player = event.getPlayer();
                if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
                    player.teleport(event.getFrom());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Player) {
                //damage caused by PvP
                Player victim = ((Player) event.getEntity()).getPlayer();
                Player killer = ((Player) event.getDamager()).getPlayer();
                if (victim.getHealth() - event.getDamage() <= 0) {
                    //Player is dead
                    spectators.add(competitors.remove(competitors.indexOf(victim.getName())));
                    setSpectatorMode(victim.getName());
                    for (Player pl : Bukkit.getWorld(worldUUID).getPlayers()) {
                        pl.sendMessage(ChatColor.RED + "[DEATH]" + ChatColor.BOLD + pl.getName() + " was killed by " + ChatColor.BOLD + killer.getName());
                    }
                }
            }
            event.setCancelled(true);

            //if there is only one competitor left, set the game state to finished
            if (competitors.size() <= 1) {
                state = GameState.FINISHED;
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = ((Player) event.getEntity()).getPlayer();
            if (victim.getHealth() - event.getDamage() <= 0) {
                //Player is dead
                setSpectatorMode(victim.getName());
                for (Player pl : Bukkit.getWorld(worldUUID).getPlayers()) {
                    pl.sendMessage(ChatColor.RED + "[DEATH]" + ChatColor.BOLD + pl.getName() + " was killed by " + ChatColor.BOLD + event.getCause().toString());
                }
            }
            event.setCancelled(true);

            //if there is only one competitor left, set the game state to finished
            if (competitors.size() <= 1) {
                state = GameState.FINISHED;
            }
        }
    }

    private void setSpectatorMode(String player) {
        Player p = Bukkit.getPlayer(player);
        p.setGameMode(GameMode.ADVENTURE);
        p.setAllowFlight(true);
        p.setCanPickupItems(false);
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
                openedChests.add(holder);
                for (Map.Entry<Material, Double> lootEntry : staticSettings.getLoot().entrySet()) {
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
    private GameManager currentGame;
    private int countdown;

    public GameStarter(SimpleSurvival plugin, GameManager currentGame, int countdown) {
        this.plugin = plugin;
        this.currentGame = currentGame;
        this.countdown = countdown;
    }

    @Override
    public void run() {
        for (Player p: Bukkit.getWorld(currentGame.getWorldUUID()).getPlayers()) {
            p.sendMessage("The game will begin in " + countdown + " seconds!");
        }
        countdown--;
        if (countdown <= 0) {
            for (Player p: Bukkit.getWorld(currentGame.getWorldUUID()).getPlayers()) {
                p.sendMessage("The game has begun!");
            }
            currentGame.setState(GameManager.GameState.RUNNING);
            this.cancel();
        }
    }
}

class GameEnder extends BukkitRunnable {

    private final SimpleSurvival plugin;
    private GameManager currentGame;
    private int countdown;

    public GameEnder(SimpleSurvival plugin, GameManager currentGame, int countdown) {
        this.plugin = plugin;
        this.currentGame = currentGame;
        this.countdown = countdown;

        for (Player p : Bukkit.getWorld(currentGame.getWorldUUID()).getPlayers()) {
            p.sendMessage("The world will close in " + countdown + " seconds!");
        }
    }

    @Override
    public void run() {
        countdown--;
        if (countdown <= 0) {
            for (Player p : Bukkit.getWorld(currentGame.getWorldUUID()).getPlayers()) {
                p.sendMessage("Server Closing...");
            }
            for (Player p : Bukkit.getWorld(currentGame.getWorldUUID()).getPlayers()) {
                p.teleport(new Location(Bukkit.getServer().getWorlds().get(0), 0, 0, 0));
            }
            plugin.worldManager.destroyWorld(currentGame.getWorldUUID());
            this.cancel();
        }
    }
}
