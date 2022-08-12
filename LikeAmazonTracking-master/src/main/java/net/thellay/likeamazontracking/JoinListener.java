package net.thellay.likeamazontracking;

import net.thellay.likeamazontracking.db.DataBaseHook;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class JoinListener implements Listener {

    DataBaseHook hook;
    LikeAmazonTracking pl;

    public JoinListener(LikeAmazonTracking pl, DataBaseHook hook) {
        Bukkit.getPluginManager().registerEvents(this,pl);
        this.hook = hook;
        this.pl = pl;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(pl,() -> {
            try {
                hook.startSessionNow(event.getPlayer());
                event.getPlayer().sendMessage("§eDeine Session wurde gestartet");
                event.getPlayer().sendMessage("§7Deine Sessions: " + hook.getSession(hook.getCurrentSessionID(event.getPlayer())).infoText());
            } catch (SQLException e) {
                e.printStackTrace();
                event.getPlayer().sendMessage("§cSession konnte nicht gestartet werden");
            }
        });
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(pl,() -> {
            try {
                hook.endSession(event.getPlayer().getUniqueId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


}
