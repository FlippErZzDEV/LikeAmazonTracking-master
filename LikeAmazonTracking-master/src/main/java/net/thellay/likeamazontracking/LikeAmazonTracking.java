package net.thellay.likeamazontracking;

import net.thellay.likeamazontracking.commands.Lat_Command;
import net.thellay.likeamazontracking.db.DataBaseHook;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class LikeAmazonTracking extends JavaPlugin {

    private static LikeAmazonTracking biligesPlugin;

    public static LikeAmazonTracking getPlugin() {

        return biligesPlugin;
    }

    public DataBaseHook dbHook;

    @Override
    public void onEnable() {
        biligesPlugin = this;
        //saveDefaultConfig();

        dbHook = new DataBaseHook(this);
        new JoinListener(this,dbHook);
        getPlugin().getCommand("lat").setExecutor(new Lat_Command(this,dbHook));
    }

    @Override
    public void onDisable() {
        try {
            dbHook.endAllSessions();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
