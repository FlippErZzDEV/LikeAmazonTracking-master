package net.thellay.likeamazontracking.commands;

import net.thellay.likeamazontracking.LikeAmazonTracking;
import net.thellay.likeamazontracking.db.DataBaseHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Lat_Command implements CommandExecutor {

    private final LikeAmazonTracking pl;
    private final DataBaseHook dbHook;

    public Lat_Command(LikeAmazonTracking pl, DataBaseHook dbHook) {
        this.pl = pl;
        this.dbHook = dbHook;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length<1) {
            sendHelpMessage(sender);
            return false;
        }
        Bukkit.getScheduler().runTaskAsynchronously(pl,() -> {
            try {
                switch (args[0]) {
                    case "info" -> {
                        //if (sender instanceof Player player) {
                        //    player.sendMessage("Current Session: "+ dbHook.getSession(dbHook.getCurrentSessionID(player)));
                        //    player.sendMessage("All Sessions: " + dbHook.getPlayerSessions(player.getUniqueId()));
                        //}
                        boolean all = sender.hasPermission("staticservice.buildserver.lat.session.info.all");
                        boolean own = all || sender.hasPermission("staticservice.buildserver.lat.session.info.own");
                        boolean current = sender.hasPermission("staticservice.buildserver.lat.session.info.current");

                        //if Someone have permissions
                        if(current) {
                            if(args.length == 1) {
                                if(sender instanceof Player player) {
                                    sendCurrentSession(sender);
                                    if(own) sendSessionList(sender, player.getUniqueId(), 25,1);
                                }
                                return;
                            } else {
                                UUID uuid = getUUIDFromString(args[1]);
                                if(uuid== null) {
                                    sender.sendMessage("§6" + args[1] + "§7 - §eis not an listed player");
                                    return;
                                }
                                if(((sender instanceof Player player) && player.getUniqueId()==uuid)|| all) {
                                    sendSessionList(sender, uuid, 25,1);
                                }
                                return;
                            }
                        }

                    }
                    case "§7session" -> {

                    }
                    case "§7reason" -> {

                    }
                }
                sendHelpMessage(sender);
            } catch (Exception e) {
                sendHelpMessage(sender);
                e.printStackTrace();
            }
        });
        return true;
    }

    private UUID getUUIDFromString(@NotNull String string) {
        if(string.length() < 20) {
            OfflinePlayer p;
            p = Bukkit.getOfflinePlayerIfCached(string);
            if(p!= null) return p.getUniqueId();
        } else try {
            return UUID.fromString(string);
        } catch (Exception ignored) {}
        return null;
    }

    private void sendSessionList(CommandSender sender, UUID user, int size, int site) throws SQLException {
        sendSessionList(sender, dbHook.getPlayerSessions(user));
    }


    private void sendCurrentSession(@NotNull CommandSender sender) throws SQLException {
        if(sender instanceof Player player)  {
            player.sendMessage("§6Current Session: "+ dbHook.getSession(dbHook.getCurrentSessionID(player)).infoText());
        }
    }

    SimpleDateFormat justDate = new SimpleDateFormat("dd-MM-yyyy", Locale.GERMANY);

    private void sendSessionList(@NotNull CommandSender sender, List<DataBaseHook.Session> sessions) {
        StringBuilder builder = new StringBuilder();
        for (DataBaseHook.Session session : sessions) {
            builder.append("§7\nSession: ").append(session.ID()).
                    append("§7, User: ").append(Bukkit.getOfflinePlayer(session.user()).getName()).
                    append("§7, Date: ").append(justDate.format(session.start())).
                    append("§7, Time: ").append(session.durationString());
        }
        sender.sendMessage(String.valueOf(builder));
        sender.sendMessage("§6Total Time Of Counted Sessions: " + totalSessionTime(sessions));
        //player.sendMessage("All Sessions: " + dbHook.getPlayerSessions(player.getUniqueId()));
    }

    private String totalSessionTime(List<DataBaseHook.Session> sessions) {
        long totalTime = 0;
        for (DataBaseHook.Session session : sessions) {
            totalTime += session.durationMilis();
        }
        Duration duration = Duration.ofMillis(totalTime);
        return DataBaseHook.Session.durationString(duration);
    }

    public void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(helpMessage(sender));
    }

    public String helpMessage(CommandSender sender) {
        StringBuilder stringBuilder = new StringBuilder();
        if(sender.hasPermission("staticservice.buildserver.lat.session.info")) {
            stringBuilder.append(
                    "§7\n/lat info");
        }
        if(sender.hasPermission("staticservice.buildserver.lat.session.info.own") || sender.hasPermission("likeamazontracking.session.info.all")) {
            stringBuilder.append(
                    "§7\n/lat session <ID|\"current\">" +
                    "§7\n/lat session list <player> <side>");
        }
        if(sender.hasPermission("staticservice.buildserver.lat.session.changereason.all") || sender.hasPermission("likeamazontracking.session.changereason.own")) {
            stringBuilder.append("§7\n/lat session <ID> setreason <ReasonID>");
        } else if(sender.hasPermission("staticservice.buildserver.lat.session.changereason.current")) {
            stringBuilder.append("§7\n/lat session current setreason <ReasonID>");
        }

        if(sender.hasPermission("staticservice.buildserver.lat.reason.info")) {
            stringBuilder.append("§7\n/lat reason list <side>");
        }

        if (sender.hasPermission("staticservice.buildserver.lat.reason.prepare.text.all")) {
            stringBuilder.append("§7\n/lat reason <ReasonID> (all) text <text>");
        } else if(sender.hasPermission("staticservice.buildserver.lat.reason.prepare.text.own")) {
            stringBuilder.append("§7\n/lat reason <ReasonID> (whenPlayerIsMaintainer) text <text");
        }

        if(sender.hasPermission("staticservice.buildserver.lat.reason.prepare.maintainer")){
            stringBuilder.append("§7\n/lat reason <ReasonID> setMaintainer <Player|UUID>");
        }

        if(sender.hasPermission("staticservice.buildserver.lat.reason.create")) {
            stringBuilder.append("§7\n/lat reason new");
        }

        return stringBuilder.toString();
    }

}

