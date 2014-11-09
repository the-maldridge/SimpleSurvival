package net.SimpleSurvival;


import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by maldridge on 10/21/14.
 */
public class GameManager {
    GameSettings currentGame;

    public GameManager(GameSettings currentGame) {
        this.currentGame = currentGame;
    }

    public void gameStateFreeze(PlayerMoveEvent p) {
        if((p != null) && (p instanceof Player)) {
            if (currentGame.competitors.contains(((Player) p).getName())) {
                if (currentGame.state == GameSettings.GameState.PRESTART || currentGame.state == GameSettings.GameState.PAUSED) {
                    p.setCancelled(true);
                }
            }
        }
    }
}
