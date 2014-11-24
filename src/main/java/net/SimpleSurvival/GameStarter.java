package net.SimpleSurvival;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by notesbox on 11/23/14.
 */
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
