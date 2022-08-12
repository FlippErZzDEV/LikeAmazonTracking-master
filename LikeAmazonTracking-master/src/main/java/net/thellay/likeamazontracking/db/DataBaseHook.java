package net.thellay.likeamazontracking.db;

import net.thellay.likeamazontracking.LikeAmazonTracking;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;


public class DataBaseHook {

    private Connection conn;
    private final LikeAmazonTracking pl;
    private final Logger log;
    private final HashMap<UUID,Integer> sessionStarts = new HashMap<>();

    public DataBaseHook(LikeAmazonTracking plugin) {
        pl = plugin;
        log = pl.getLogger();
        plugin.saveResource("session.db",false);
        tryConnect();
    }

    public void tryConnect() {

        File dataFolder = pl.getDataFolder();

        try {
            // db parameters
            String url = "jdbc:sqlite:" + dataFolder.getPath()+File.separator+"session.db";
            // create a connection to the database
            //log.info(url);
            conn = DriverManager.getConnection(url);

            log.info("Connection to SQLite has been established.");

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS \"Session\" (\n" +
                    "\t\"ID\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t\"UUID\"\tTEXT NOT NULL,\n" +
                    "\t\"Start\"\tINTEGER,\n" +
                    "\t\"End\"\tINTEGER,\n" +
                    "\t\"ReasonID\"\tINTEGER NOT NULL\n" +
                    ")").executeUpdate();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS \"Reason\" (\n" +
                    "\t\"ReasonID\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t\"PrimaryUUID\"\tTEXT,\n" +
                    "\t\"Text\"\tTEXT\n" +
                    ")").executeUpdate();
            conn.prepareStatement("INSERT INTO Reason (ReasonID,Text) VALUES (0, 'Keine Angabe') ON CONFLICT DO NOTHING").executeUpdate();

            log.info("DataBasesNowImplemented");

        } catch (SQLException e) {
            log.warning(e.getMessage());
        }
    }



    public int startSessionNow(Player player) throws SQLException {
        UUID uuid = player.getUniqueId();
        long time = System.currentTimeMillis();
        PreparedStatement stm = conn.prepareStatement("INSERT INTO Session (UUID, Start, ReasonID)  VALUES (?,CURRENT_TIMESTAMP,?);\n");
        stm.setString(1,player.getUniqueId().toString());
        stm.setInt(2,0);
        stm.execute();
        int i = conn.prepareStatement("SELECT max(ID) FROM Session;").executeQuery().getInt(1);
        sessionStarts.put(player.getUniqueId(), i);
        return i;
    }

    public void endSession(UUID uuid) throws SQLException {
        int i = sessionStarts.get(uuid);
        sessionStarts.remove(uuid);
        PreparedStatement stm = conn.prepareStatement("UPDATE Session SET End=CURRENT_TIMESTAMP WHERE ID="+i);
        stm.executeUpdate();
    }

    public void endAllSessions() throws SQLException {
        PreparedStatement stm = conn.prepareStatement("UPDATE Session SET End=CURRENT_TIMESTAMP WHERE ID=?");
        sessionStarts.forEach((uuid, integer) -> {
            try {
                stm.setInt(1,integer);
                stm.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        sessionStarts.clear();
    }

    public void setCurrentReason(Player player, int reasonID) throws SQLException {
        setReason(sessionStarts.get(player.getUniqueId()),reasonID);
    }

    public void setReason(int sessionID, int reasonID) throws SQLException {
        conn.prepareStatement("UPDATE Session SET ReasonID="+reasonID+ " WHERE ID="+sessionID).executeUpdate();
    }

    public int makeReason(String text) throws SQLException {
        PreparedStatement stm = conn.prepareStatement("INSERT INTO Reason (Text) VALUES (?)");
        stm.setString(1,text);
        stm.executeUpdate();
        int i = conn.prepareStatement("SELECT max(ReasonID) FROM Reason;").executeQuery().getInt(1);
        return i;
    }

    public void updateReasonText(int reasonID, String text) throws SQLException {
        PreparedStatement stm = conn.prepareStatement("UPDATE Reason SET Text=? WHERE ReasonID="+reasonID);
        stm.setInt(1,reasonID);
        stm.executeUpdate();
    }

    public void updateReasonMaintainer(int reasonID, UUID maintainer) throws SQLException {
        conn.prepareStatement("UPDATE Reason SET PrimaryUUID=" + maintainer + " WHERE ReasonID="+reasonID);
    }



    private ResultSet getPlayerSessionsResult(UUID uuid) throws SQLException {
        return conn.prepareStatement("SELECT ID, UUID, Start, End, Reason.ReasonID, Text FROM Session, Reason WHERE Session.UUID='"+uuid+"' AND Reason.ReasonID=Session.ReasonID").executeQuery();
    }

    public List<Session> getPlayerSessions(UUID uuid) throws SQLException {
        ResultSet set = getPlayerSessionsResult(uuid);
        ArrayList<Session> sessions = new ArrayList<Session>();
       // set.beforeFirst();
        while (set.next()) {
            sessions.add(new Session(
                    set.getInt("ID"),
                    UUID.fromString(set.getString("UUID")),
                    set.getDate("Start"),
                    set.getDate("End"),
                    set.getInt("ReasonID"),
                    set.getString("Text")));
        }
        return sessions;
    }


    private ResultSet getSessionResult(int ID) throws SQLException {
        return conn.prepareStatement("SELECT ID, UUID, Start, End, Reason.ReasonID, Text FROM Session, Reason WHERE Session.ID="+ID+" AND Reason.ReasonID=Session.ReasonID").executeQuery();
    }

    public Session getSession(int ID) throws SQLException {
        ResultSet set = getSessionResult(ID);
        return new Session(
                set.getInt("ID"),
                UUID.fromString(set.getString("UUID")),
                set.getDate("Start"),
                set.getDate("End"),
                set.getInt("ReasonID"),
                set.getString("Text"));
    }


    /*
    public String getPlayerSessionsAsString(UUID uuid) throws SQLException {
        ResultSet set = getPlayerSessions(uuid);
        StringBuilder stringBuilder = new StringBuilder();
        while (set.next()) {
            stringBuilder
                    .append("\n")
                    .append("SessionID: ")
                    .append(set.getInt("ID"))
                    .append(" SessionStart: ")
                    .append(set.getDate("Start"))
                    .append(" SessionEnd: ")
                    .append(set.getDate("End"))
                    .append(" SessionReason: ")
                    .append(set.getString("Text"));
        }
        return stringBuilder.toString();
    }
    */


    public int getCurrentSessionID(Player player) {
        return sessionStarts.get(player.getUniqueId());
    }

    public record Session(int ID, UUID user, Date start, Date end, int reasonID, String reasonTxt) {


        public Session {
            //fix the time difference
            start.setTime((2*60*60*1000)+start.getTime());
            if(end!=null) end.setTime((2*60*60*1000)+end.getTime());
        }

        @Override
        public String toString() {
            return (end==null) ?
                    "Session{" +
                    "ID=" + ID +
                    ", user=" + user +
                    ", start=" + start.toGMTString() +
                    ", end=null" +
                    ", reasonID=" + reasonID +
                    ", reasonTxt='" + reasonTxt + '\'' +
                    '}' :
                    "Session{" +
                    "ID=" + ID +
                    ", user=" + user +
                    ", start=" + start.toGMTString() +
                    ", end=" + end.toGMTString() +
                    ", reasonID=" + reasonID +
                    ", reasonTxt='" + reasonTxt + '\'' +
                    '}';
        }

        private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm",Locale.GERMAN);

        public String infoText() {
            //DateFormat.getDateInstance(DateFormat.AM_PM_FIELD, Locale.GERMANY);

            return
                    "SessionID: " + ID +
                    "\nSelectedJob: (ID:" + reasonID +") - " + reasonTxt +
                    "\nSessionStart: " + formatter.format(start) +
                    "\nSessionLength: " + durationString();

        }

        public Duration duration() {
            return Duration.ofMillis(durationMilis());
        }

        public long durationMilis() {
            //System.out.println((System.currentTimeMillis() + " " +(2*60*60*1000) + " " + start.getTime()));
            return end==null ? (System.currentTimeMillis() - start.getTime()) : end.getTime()-start.getTime();
        }

        public String durationString() {
            return durationString(duration());
        }
        //static public String durationString(Duration duration) {
        //    return duration.toMinutesPart()<10 ? duration.toHours() + ":" + duration.toMinutesPart() : duration.toHours() + ":0" + duration.toMinutesPart();
        //}

        static public String durationString(Duration duration) {
            return duration.toHours() + "H " + duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s";
        }

    }
}
