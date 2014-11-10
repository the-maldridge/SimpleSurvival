package net.SimpleSurvival;

import net.SimpleSurvival.settings.GameSettings;
import org.bukkit.entity.Player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.ArrayList;


/**
 * Created by maldridge on 10/21/14.
 */
public class GameManager implements Listener {
    GameSettings currentGame;
    ArrayList<String> killers;
    ArrayList<String> spectators = new ArrayList<String>();


    public GameManager(GameSettings currentGame) {
        this.currentGame = currentGame;
        this.killers = new ArrayList<String>(currentGame.getCompetitors());
    }

    public void gameStateFreeze(PlayerMoveEvent p) {
        if((p != null) && (p instanceof Player)) {
            if (currentGame.getCompetitors().contains(((Player) p).getName())) {
                if (currentGame.getState() == GameSettings.GameState.PAUSED) {
                    p.setCancelled(true);
                }
            }
        }
    }

    public void playerDeath(PlayerDeathEvent p) {
        if((p != null) && (p instanceof Player)) {
            Player player = (Player)p;
            Player killer = player.getKiller();

            spectators.add(killers.remove(killers.indexOf(killer.getName())));
            setSpectatorMode();
            for(Player pl: player.getWorld().getPlayers()) {
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

    public void chestOpen(PlayerInteractEvent playerInteractEvent) {
        if (spectators.contains(playerInteractEvent.getPlayer().getName())) {
            if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block b = playerInteractEvent.getClickedBlock();
                if (b.getType() == Material.CHEST) {
                    playerInteractEvent.setCancelled(true);

                }

            }

        }
    }
}
