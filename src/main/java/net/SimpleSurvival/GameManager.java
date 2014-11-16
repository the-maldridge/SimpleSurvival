package net.SimpleSurvival;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
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
        this.plugin.getLogger().info("Warping the following players to  " + this.staticSettings.getSourceWorld() + ": "+ competitors.toString());
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

    @Override
    public String toString() {return this.getWorld()+"-"+this.getWorldUUID().substring(0,4);}
    public String getWorld() {
        return staticSettings.getSourceWorld();
    }

    public String getWorldUUID() {
        return worldUUID;
    }

    public boolean doAnimals() { return staticSettings.doAnimals(); }
    public boolean doHostileMobs() { return staticSettings.doHostileMobs(); }
    public boolean doAutoWarp() { return staticSettings.doAutoWarp(); }
    public boolean doAutoStart() { return staticSettings.doAutoStart(); }

    public void sendPlayersToSpawn() {
        for (int i = 0; i < competitors.size(); i++) {
            int x = staticSettings.getSpawns().get(i)[0];
            int y = staticSettings.getSpawns().get(i)[1];
            int z = staticSettings.getSpawns().get(i)[2];
            World w = Bukkit.getWorld(worldUUID);
            Location nextSpawn = new Location(w, x, y, z);
            Player player = Bukkit.getPlayer(competitors.get(i));
            player.teleport(nextSpawn);
            setCompetitorMode(player);
        }
    }

    public void start() {
        if(state == GameState.BEFORE_GAME) {
            new GameStarter(this.plugin, this, 15).runTaskTimer(this.plugin, 0, 20);
            state = GameState.STARTING;
        }
    }
    public void end(boolean silent) {
        if (silent) {
            for (Player p : Bukkit.getWorld(worldUUID).getPlayers()) {
                p.sendMessage("This world is being unloaded");
            }

            for(Player p : Bukkit.getWorld(worldUUID).getPlayers()) {
                p.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
            }

            plugin.worldManager.destroyWorld(worldUUID);
            unregisterListeners();
        } else {
            BukkitTask countDownTimer = new GameEnder(this.plugin, this, 10).runTaskTimer(this.plugin, 0, 20);
        }
    }
    public void announceWinner() {
        for(Player p: Bukkit.getWorld(worldUUID).getPlayers()) {
            if(competitors.size()>0) {
                p.sendMessage(competitors.get(0) + " has won the round!");
            }
        }
        if(competitors.size()>0) {
            this.plugin.getLogger().info(competitors.get(0) + " has won the round.");
        } else {
            this.plugin.getLogger().info("A serious error has occurred, winners list of size 0: " + competitors.toString());
        }
    }


    private void dropPlayerInventory(Player player) {
        for (ItemStack i : player.getInventory().getContents()) {
            if (i != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), i);
                player.getInventory().remove(i);
            }
        }
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[] {null, null, null, null});
    }

    private void setSpectatorMode(String player) {
        Player p = Bukkit.getPlayer(player);
        p.setGameMode(GameMode.ADVENTURE);
        p.setAllowFlight(true);
        p.setCanPickupItems(false);
        for(Player pl : p.getWorld().getPlayers()) {
            pl.hidePlayer(p);
        }
    }
    private void setCompetitorMode(Player player) {
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        player.setHealth(20);
        player.setLevel(0);
        player.getInventory().clear();
    }

    public void unregisterListeners() {
        PlayerMoveEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        InventoryOpenEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
        PlayerPickupItemEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().getWorld().getName() == worldUUID) {
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
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity().getWorld().getName() == worldUUID) {
            if ((event.getEntity() instanceof Player)) {
                if(competitors.contains(((Player)event.getEntity()).getPlayer().getName())) {
                    if ((event.getDamager() instanceof Player) && (competitors.contains(((Player) event.getDamager()).getName()))) {
                        //damage caused by PvP
                        Player victim = ((Player) event.getEntity()).getPlayer();
                        Player killer = ((Player) event.getDamager()).getPlayer();
                        if (victim.getHealth() - event.getDamage() <= 0) {
                            //Player is dead
                            event.setCancelled(true);
                            competitors.remove(competitors.indexOf(victim.getName()));
                            setSpectatorMode(victim.getName());
                            dropPlayerInventory(victim);
                            for (Player pl : Bukkit.getWorld(worldUUID).getPlayers()) {
                                pl.sendMessage(ChatColor.RED + "[DEATH]" + ChatColor.BOLD + victim.getName() + " was killed by " + ChatColor.BOLD + killer.getName());
                            }
                            //if there is only one competitor left, set the game state to finished
                            if (competitors.size() <= 1) {
                                announceWinner();
                                state = GameState.FINISHED;
                            }
                        }

                    } else {
						event.setCancelled(true);
					}
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity().getWorld().getName() == worldUUID) {
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                if (event.getEntity() instanceof Player) {
                    Player victim = ((Player) event.getEntity()).getPlayer();
                    if (victim.getHealth() - event.getDamage() <= 0) {
                        //Player is dead
                        event.setCancelled(true);
                        competitors.remove(victim.getName());
                        dropPlayerInventory(victim);
                        setSpectatorMode(victim.getName());
                        for (Player pl : Bukkit.getWorld(worldUUID).getPlayers()) {
                            pl.sendMessage(ChatColor.RED + "[DEATH]" + ChatColor.BOLD + victim.getName() + " was killed by " + ChatColor.BOLD + event.getCause().toString());
                        }
                        //if there is only one competitor left, set the game state to finished
                        if (competitors.size() <= 1) {
                            announceWinner();
                            state = GameState.FINISHED;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent inventoryOpenEvent) {
        if (inventoryOpenEvent.getPlayer().getWorld().getName() == worldUUID) {
            String player = inventoryOpenEvent.getPlayer().getName();
            Inventory inventory = inventoryOpenEvent.getInventory();
            InventoryHolder holder = inventory.getHolder();
            if (holder instanceof Chest || holder instanceof DoubleChest) {
                if (spectators.contains(player)) {
                    inventoryOpenEvent.setCancelled(true);
                    return;
                }

                if (!openedChests.contains(holder)) {
                    openedChests.add(holder);
                    for (Map.Entry<Material, Double> lootEntry : staticSettings.getLoot().entrySet()) {
                        if (Math.random() * 100 < lootEntry.getValue()) {
                            inventory.addItem(new ItemStack(lootEntry.getKey()));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent breakEvent) {
        if(!this.staticSettings.getBreakables().contains(breakEvent.getBlock())) {
            breakEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if(!competitors.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
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

        for (Player p : Bukkit.getWorld(this.currentGame.getWorldUUID()).getPlayers()) {
            p.sendMessage("The world will close in " + countdown + " seconds!");
        }
    }

    @Override
    public void run() {
        countdown--;
        if (countdown <= 0) {
            for (Player p : Bukkit.getWorld(currentGame.getWorldUUID()).getPlayers()) {
                p.sendMessage("World Closing...");
            }
            for (Player p : Bukkit.getWorld(currentGame.getWorldUUID()).getPlayers()) {
                p.setGameMode(GameMode.SURVIVAL);
                p.setAllowFlight(false);
                p.getInventory().clear();
                for(Player player : Bukkit.getWorld(currentGame.getWorldUUID()).getPlayers()) {
                    p.showPlayer(player);
                }
            }

            for(Player p : Bukkit.getWorld(currentGame.getWorldUUID()).getPlayers()) {
                p.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
            }

            plugin.worldManager.destroyWorld(currentGame.getWorldUUID());
			currentGame.unregisterListeners();
            this.cancel();
        }

    }
}
