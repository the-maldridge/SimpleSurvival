package net.SimpleSurvival;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by notesbox on 11/23/14.
 */
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
