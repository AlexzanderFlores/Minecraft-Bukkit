package network.staff;

import network.Network;
import network.Network.Plugins;
import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.player.PlayerBanEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.MiniGame;
import network.gameapi.MiniGame.GameStates;
import network.gameapi.SpectatorHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.player.account.PlaytimeTracker;
import network.server.ChatClickHandler;
import network.server.CommandBase;
import network.server.DB;
import network.server.servers.worker.Server;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class ReportHandler implements Listener {
    private static Map<String, List<Integer>> reportIDs = null;
    private Map<Integer, String> openReports = null;
    private int counter = 10;
    private int lastAmount = -1;
    
    public ReportHandler() {
        new CommandBase("report", 1, -1) {
            @Override
            public boolean execute(final CommandSender sender, final String [] arguments) {
                new AsyncDelayedTask(() -> {
                    String option = arguments[0];
                    Player player = null;
                    if(sender instanceof Player) {
                        player = (Player) sender;
                    } else if(!option.equalsIgnoreCase("create") && !option.equalsIgnoreCase("close")) {
                        MessageHandler.sendPlayersOnly(sender);
                        return;
                    }
                    if(option.equalsIgnoreCase("view")) {
                        if(arguments.length == 2 && arguments[1].equalsIgnoreCase("open")) {
                            if(Ranks.isStaff(sender)) {
                                if(openReports == null || openReports.isEmpty()) {
                                    MessageHandler.sendMessage(player, "&cThere are no reports to display currently.");
                                    MessageHandler.sendMessage(player, "&cChecking for more reports in &e" + counter + "&c seconds");
                                } else {
                                    MessageHandler.sendLine(player, "&b");
                                    MessageHandler.sendMessage(player, "&aOpen report IDs: &cCLICK THE ID TO OPEN");
                                    for(int id : openReports.keySet()) {
                                        ChatClickHandler.sendMessageToRunCommand(player, openReports.get(id) + id, "Click to open", "/report view " + id);
                                    }
                                    MessageHandler.sendMessage(player, "&cRed &areport IDs are non-ranked players (More likely to hack)");
                                    MessageHandler.sendMessage(player, "&bBlue &areport IDs are possible server advertisement");
                                    MessageHandler.sendLine(player, "&b");
                                }
                            } else {
                                MessageHandler.sendMessage(player, Ranks.TRIAL.getNoPermission());
                            }
                            return;
                        } else if(arguments.length >= 2) {
                            try {
                                int id = Integer.valueOf(arguments[1]);
                                if(DB.STAFF_REPORTS.isKeySet("id", arguments[1])) {
                                    new ReportData(id, player);
                                } else {
                                    MessageHandler.sendMessage(sender, "&cThere is no report for ID #" + id + " (Deleted or never existed)");
                                }
                                return;
                            } catch(NumberFormatException e) {}
                        }
                        MessageHandler.sendMessage(sender, "&f/report view <report ID>");
                        if(Ranks.isStaff(sender)) {
                            MessageHandler.sendMessage(sender, "&f/report view open");
                        }
                    } else if(option.equalsIgnoreCase("close")) {
                        if(Ranks.isStaff(sender)) {
                            if(arguments.length >= 2) {
                                String id = arguments[1];
                                if(DB.STAFF_REPORTS.isKeySet("id", id)) {
                                    String staffUUID = "CONSOLE";
                                    if(player != null) {
                                        staffUUID = player.getUniqueId().toString();
                                    }
                                    String date = TimeUtil.getTime().substring(0, 7);
                                    String [] keys = new String [] {"uuid", "date_closed"};
                                    String [] values = new String [] {staffUUID, date};
                                    if(DB.STAFF_REPORTS_CLOSED.isKeySet(keys, values)) {
                                        int amount = DB.STAFF_REPORTS_CLOSED.getInt(keys, values, "amount") + 1;
                                        DB.STAFF_REPORTS_CLOSED.updateInt("amount", amount, keys, values);
                                    } else {
                                        DB.STAFF_REPORTS_CLOSED.insert("'" + staffUUID + "', '" + date + "', '1'");
                                    }
                                    DB.STAFF_REPORTS.delete("id", id);
                                    MessageHandler.sendMessage(sender, "You have closed report #" + id);
                                } else {
                                    MessageHandler.sendMessage(sender, "&cThere is no report for ID #" + id + " (Deleted or never existed)");
                                }
                                return;
                            }
                            MessageHandler.sendMessage(sender, "&f/report close <report ID>");
                        } else {
                            MessageHandler.sendMessage(sender, Ranks.TRIAL.getNoPermission());
                        }
                    } else if(option.equalsIgnoreCase("startKicking")) {
                        if(Ranks.isStaff(sender)) {
                            if(arguments.length == 2) {
                                try {
                                    int id = Integer.valueOf(arguments[1]);
                                    MessageHandler.sendLine(player, "&e");
                                    MessageHandler.sendMessage(player, "&aOPTIONS TO KICK");
                                    ChatClickHandler.sendMessageToRunCommand(player, "&cCLICK TO KICK for(SERVER_ADVERTISEMENT)", "Click to kick for SERVER_ADVERTISEMENT", "/report startKicking confirm " + id + " SERVER_ADVERTISEMENT");
                                    MessageHandler.sendMessage(player, "");
                                    ChatClickHandler.sendMessageToRunCommand(player, "&cCLICK TO KICK for(SOCIAL_MEDIA_ADVERTISEMENT)", "Click to kick for SOCIAL_MEDIA_ADVERTISEMENT", "/report startKicking confirm " + id + " SOCIAL_MEDIA_ADVERTISEMENT");
                                    MessageHandler.sendMessage(player, "");
                                    ChatClickHandler.sendMessageToRunCommand(player, "&cCLICK TO KICK for(RACISM)", "Click to kick for RACISM", "/report startKicking confirm " + id + " RACISM");
                                    MessageHandler.sendMessage(player, "");
                                    ChatClickHandler.sendMessageToRunCommand(player, "&cCLICK TO KICK for(DISRESPECT)", "Click to kick for DISRESPECT", "/report startKicking confirm " + id + " DISRESPECT");
                                    MessageHandler.sendMessage(player, "");
                                    ChatClickHandler.sendMessageToRunCommand(player, "&cCLICK TO KICK for(SUICIDE_COMMENTS)", "Click to kick for SUICIDE_COMMENTS", "/report startKicking confirm " + id + " SUICIDE_COMMENTS");
                                    MessageHandler.sendMessage(player, "");
                                    ChatClickHandler.sendMessageToRunCommand(player, "&cCLICK TO KICK for(INAPPROPRIATE_COMMENTS)", "Click to kick for INAPPROPRIATE_COMMENTS", "/report startKicking confirm " + id + " INAPPROPRIATE_COMMENTS");
                                    MessageHandler.sendLine(player, "&e");
                                } catch(NumberFormatException e) {
                                    return;
                                }
                            } else if(arguments.length >= 3) {
                                try {
                                    int id = Integer.valueOf(arguments[2]);
                                    String name = AccountHandler.getName(UUID.fromString(DB.STAFF_REPORTS.getString("id", String.valueOf(id), "reported_uuid")));
                                    String proof = DB.STAFF_REPORTS.getString("id", String.valueOf(id), "proof");
                                    if(name == null) {
                                        MessageHandler.sendMessage(sender, "&cCould not load the name of the player for this report");
                                    } else if(proof == null) {
                                        MessageHandler.sendMessage(sender, "&cCould not load the proof for this report");
                                    } else {
                                        ChatClickHandler.sendMessageToRunCommand(player, "&aCLICK TO KICK FOR " + arguments[3], "Click to kick", "/kick " + name + " " + arguments[3] + " " + proof);
                                    }
                                } catch(NumberFormatException e) {
                                    return;
                                }
                            } else {
                                return;
                            }
                        } else {
                            MessageHandler.sendMessage(player, Ranks.TRIAL.getNoPermission());
                        }
                    } else {
                        Plugins plugin = Network.getPlugin();
                        MiniGame game = Network.getMiniGame();
                        if(plugin == Plugins.HUB) {
                            MessageHandler.sendMessage(player, "&cYou cannot create a report on this server");
                            return;
                        } else if(game != null && game.getGameState() != GameStates.STARTING && game.getGameState() != GameStates.STARTED) {
                            MessageHandler.sendMessage(player, "&cYou cannot create a report before the game starts or is starting");
                            return;
                        }
                        if(arguments.length == 1 || arguments.length == 2) {
                            String name = arguments[0];
                            Player target = ProPlugin.getPlayer(name);
                            if(name.equalsIgnoreCase(sender.getName()) && target != null && !Ranks.OWNER.hasRank(target)) {
                                MessageHandler.sendMessage(player, "&cYou cannot report yourself");
                            } else if(target != null && Ranks.isStaff(target) && !Ranks.OWNER.hasRank(target)) {
                                MessageHandler.sendMessage(player, "&cYou cannot report a staff this way, please report staff here:");
                                MessageHandler.sendMessage(player, "http://forum.promcgames.com/forums/complaints-about-staff.12/");
                            } else {
                                if(player != null && DB.STAFF_REPORTS.isKeySet(new String [] {"uuid", "opened"}, new String [] {player.getUniqueId().toString(), "1"})) {
                                    MessageHandler.sendMessage(player, "&cYou already have an open report deployed.");
                                    return;
                                }
                                UUID uuid = AccountHandler.getUUID(name);
                                if(uuid == null) {
                                    MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
                                } else {
                                    try {
                                        String reasonString = arguments.length == 2 ? arguments[1] : "CHEATING";
                                        ReportReasons reason = ReportReasons.valueOf(reasonString.toUpperCase());
                                        if(reason == ReportReasons.CHAT_FILTER_DETECTION && sender instanceof Player) {
                                            MessageHandler.sendMessage(sender, "&cPlayers cannot create a report for this reason");
                                            return;
                                        }
                                        String proof = (arguments.length == 3 ? arguments[2] : "none");
                                        if(proof.equals("none")) {
                                            if(reason == ReportReasons.CHAT_VIOLATION) {
                                                MessageHandler.sendMessage(sender, "&cYou must attach proof for chat violations");
                                                return;
                                            } else if(target == null) {
                                                MessageHandler.sendMessage(sender, "&cYou must attach proof for reports of offline players");
                                                return;
                                            }
                                        } else {
                                            if(reason != ReportReasons.CHAT_FILTER_DETECTION && !Pattern.compile("http[s]{0,1}://[a-zA-Z0-9\\./\\?=_%&#-+$@'\"\\|,!*]*").matcher(proof).find()) {
                                                MessageHandler.sendMessage(sender, "&cYour proof must be a URL to a FULL SCREEN screen shot");
                                                return;
                                            } else if(proof.contains("gyazo.com")) {
                                                MessageHandler.sendMessage(sender, "&cGyazo links cannot be used as proof");
                                                return;
                                            } else if(reason == ReportReasons.CHEATING || reason == ReportReasons.FAST_BOW) {
                                                if(!proof.toLowerCase().contains("youtube.com/") && !proof.toLowerCase().contains("youtu.be/")) {
                                                    MessageHandler.sendMessage(sender, "&cYour proof must be a youtube URL");
                                                    return;
                                                }
                                                if(proof.contains("&")) {
                                                    proof = proof.split("&")[0];
                                                }
                                            }
                                        }
                                        String reportingUUID = "CONSOLE";
                                        if(player != null) {
                                            reportingUUID = player.getUniqueId().toString();
                                        }
                                        DB.STAFF_REPORTS.insert("'" + reportingUUID + "', '" + uuid.toString() + "', '" + null + "', '" + reason.toString() + "', '" + null + "', '" + null + "', '" + PlaytimeTracker.getPlayTime(target).getDisplay(PlaytimeTracker.TimeType.LIFETIME) + "', '" + proof + "', '" + TimeUtil.getTime() + "', '" + null + "', '" + null + "', '1'");
                                        String staffUUID = "CONSOLE";
                                        if(player != null) {
                                            staffUUID = player.getUniqueId().toString();
                                        }
                                        String [] keys = new String [] {"reported_uuid", "uuid", "opened"};
                                        String [] values = new String [] {target.getUniqueId().toString(), staffUUID, "1"};
                                        int id = DB.STAFF_REPORTS.getInt(keys, values, "id");
                                        MessageHandler.sendMessage(sender, "Your report has been created (ID# " + id + ")! Staff will be notified of it every 10 seconds until it is closed. Thank you for the report!");
                                        if(reason == ReportReasons.CHEATING || reason == ReportReasons.FAST_BOW) {
                                            if(reportIDs == null) {
                                                reportIDs = new HashMap<String, List<Integer>>();
                                            }
                                            List<Integer> ints = reportIDs.get(target.getName());
                                            if(ints == null) {
                                                ints = new ArrayList<Integer>();
                                            }
//                                                ints._add(id);
                                            reportIDs.put(target.getName(), ints);
                                        }
                                    } catch(IllegalArgumentException e) {
                                        MessageHandler.sendMessage(sender, "&c\"" + arguments[1] + "\" is an unknown report reason, use one of the following:");
                                        String reasons = "";
                                        for(ReportReasons reason : ReportReasons.values()) {
                                            reasons += "&a" + reason + "&e, ";
                                        }
                                        MessageHandler.sendMessage(sender, reasons.substring(0, reasons.length() - 2));
                                    }
                                }
                            }
                        } else {
                            MessageHandler.sendMessage(player, "&f/report <name> [proof]");
                        }
                        /*if(arguments.length == 2) {
                            if(arguments[1].equalsIgnoreCase("players")) {
                                MessageHandler.sendLine(player);
                                MessageHandler.sendMessage(player, "Report Commands:");
                                MessageHandler.sendMessage(player, "&bCreate a report report:");
                                MessageHandler.sendMessage(player, "/report <player name> <reason> [proof]");
                                MessageHandler.sendMessage(player, "&bView a report's status:");
                                MessageHandler.sendMessage(player, "/report view <report ID>");
                                MessageHandler.sendLine(player);
                                return;
                            } else if(arguments[1].equalsIgnoreCase("staff")) {
                                MessageHandler.sendLine(player);
                                MessageHandler.sendMessage(player, "Staff Report Commands:");
                                MessageHandler.sendMessage(player, "&bClose a report:");
                                MessageHandler.sendMessage(player, "/report close <report ID>");
                                MessageHandler.sendMessage(player, "&bView open reports:");
                                MessageHandler.sendMessage(player, "/report view open");
                                MessageHandler.sendLine(player);
                                return;
                            }
                        }
                        MessageHandler.sendMessage(player, "You must specify player or staff commands, examples:");
                        MessageHandler.sendMessage(player, "/report commands players");
                        MessageHandler.sendMessage(player, "/report commands staff");*/
                    }
                });
                return true;
            }
        };
        EventUtil.register(this);
        checkForReports();
    }

    private void notifyOfOpenReports(Player player) {
        if(openReports.size() == 1) {
            ChatClickHandler.sendMessageToRunCommand(player, "&cCLICK TO VIEW IT", "Click to view the open report", "/report view open", ChatColor.translateAlternateColorCodes('&', "&eThere is &c1 &eopen report! "));
        } else {
            ChatClickHandler.sendMessageToRunCommand(player, "&cCLICK TO VIEW THEM", "Click to view the open reports", "/report view open", ChatColor.translateAlternateColorCodes('&', "&eThere are &c" + openReports.size() + " &eopen reports! "));
        }
        EffectUtil.playSound(player, Sound.CHICKEN_EGG_POP);
    }

    private void checkForReports() {
        new AsyncDelayedTask(() -> {
            counter = 10;
            if(openReports == null) {
                openReports = new HashMap<>();
            } else {
                openReports.clear();
            }

            ResultSet resultSet = null;
            DB table = DB.STAFF_REPORTS;
            try {
                Connection connection = table.getConnection();
                if(connection != null && !connection.isClosed()) {
                    resultSet = connection.prepareStatement("SELECT id,reported_uuid,reason FROM " + table.getName() + " WHERE opened = '1' LIMIT 10").executeQuery();
                    while(resultSet.next()) {
                        String reason = resultSet.getString("reason");
                        if(reason.equals(ReportReasons.CHAT_FILTER_DETECTION.toString()) || reason.equals(ReportReasons.CHAT_VIOLATION.toString())) {
                            openReports.put(resultSet.getInt("id"), "&b");
                        } else {
                            Ranks rank = AccountHandler.getRank(UUID.fromString(resultSet.getString("reported_uuid")));
                            openReports.put(resultSet.getInt("id"), rank == Ranks.PLAYER ? "&c" : "&e");
                        }
                    }
                }
            } catch(SQLException e) {
                e.printStackTrace();
            } finally {
                DB.close(resultSet);
            }

            if(openReports != null && !openReports.isEmpty()) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(Network.getMiniGame() != null && !SpectatorHandler.contains(player)) {
                        continue;
                    }
                    if(Ranks.isStaff(player)) {
                        notifyOfOpenReports(player);
                    }
                }
            }

            if(Network.getServerName().equalsIgnoreCase("HUB1")) {
                int amount = openReports == null || openReports.isEmpty() ? 0 : openReports.size();
                if(amount != lastAmount) {
                    lastAmount = amount;
                    Server.post("http://167.114.98.199:8081/reports?a=" + amount);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        String name = event.getPlayer().getName();
        if(reportIDs != null && reportIDs.containsKey(name)) {
            if(reportIDs.get(name) != null) {
                for(int id : reportIDs.get(name)) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "report close " + id + " INVALID_REPORT Player has logged off of that server");
                }
                reportIDs.get(name).clear();
            }
            reportIDs.remove(name);
        }
    }

    @EventHandler
    public void onPlayerBan(PlayerBanEvent event) {
        Player player = Bukkit.getPlayer(event.getUUID());
        if(player != null && player.isOnline()) {
            CommandSender staff = event.getStaff();
            if(reportIDs != null && reportIDs.containsKey(player.getName())) {
                if(reportIDs.get(player.getName()) != null) {
                    for(int id : reportIDs.get(player.getName())) {
                        String command = "report close " + id + " PUNISHMENT_ISSUED Player has been banned";
                        if(staff instanceof Player) {
                            Player staffPlayer = (Player) staff;
                            staffPlayer.chat("/" + command);
                        } else {
                            Bukkit.dispatchCommand(staff, command);
                        }
                    }
                    reportIDs.get(player.getName()).clear();
                }
                reportIDs.remove(player.getName());
            }
        }
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20) {
            --counter;
        } else if(ticks == 20 * 10) {
            checkForReports();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if(!Ranks.isStaff(event.getPlayer())) {
            String msg = ChatColor.stripColor(event.getMessage().toLowerCase().replace("!", "").replace(".", "").replace("?", ""));
            String regex = "([h]+[\\W]*[a|4|@|q]+[\\W]*(x|k|ck)+[\\W]*(s)*+(([0|e]+[\\W]*[r]+[\\W]*[s]*)*|([i|1]+[\\W]*[n]+[\\W]*[g]*)))+";
            if(msg.toLowerCase().matches(regex)) {
                for(Player online : Bukkit.getOnlinePlayers()) {
                    if(!event.getPlayer().getName().equals(online.getName())) {
                        event.getRecipients().remove(online);
                    }
                }
                display(event.getPlayer());
            } else {
                for(String word : event.getMessage().split(" ")) {
                    if(word.toLowerCase().matches(regex)) {
                        display(event.getPlayer());
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    private void display(Player player) {
        MessageHandler.sendLine(player);
        MessageHandler.sendMessage(player, "&cWe have an on-server reporting system:");
        MessageHandler.sendMessage(player, "Example: /report <name> CHEATING");
        MessageHandler.sendLine(player);
    }

    public enum ReportReasons {CHEATING, FAST_BOW, CHAT_VIOLATION, CHAT_FILTER_DETECTION}

    private class ReportData {
        public ReportData(int id, Player player) {
            boolean open = true;
            String reporting = null;
            UUID reportedUUID = null;
            String reported = null;
            String playTime = null;
            ReportReasons reason = null;
            String server = null;
            String proof = null;
            ResultSet resultSet = null;
            DB table = DB.STAFF_REPORTS;
            try {
                resultSet = table.getConnection().prepareStatement("SELECT * FROM " + table.getName() + " WHERE id = '" + id + "' LIMIT 1").executeQuery();
                while(resultSet.next()) {
                    reportedUUID = UUID.fromString(resultSet.getString("reported_uuid"));
                    reported = AccountHandler.getName(reportedUUID);
                    String uuid = resultSet.getString("uuid");
                    if(uuid.equals("CONSOLE")) {
                        reporting = "CONSOLE";
                    } else {
                        reporting = AccountHandler.getName(UUID.fromString(uuid));
                    }
                    try {
                        reason = ReportReasons.valueOf(resultSet.getString("reason"));
                        open = resultSet.getBoolean("opened");
                        if(ProPlugin.getPlayer(reported) != null && (reason == ReportReasons.CHEATING || reason == ReportReasons.FAST_BOW)) {
                            if(reportIDs == null) {
                                reportIDs = new HashMap<String, List<Integer>>();
                            }
                            List<Integer> ints = reportIDs.get(reported);
                            if(ints == null) {
                                ints = new ArrayList<Integer>();
                            }
                            if(!ints.contains(id)) {
                                ints.add(id);
                                reportIDs.put(reported, ints);
                            }
                        }
                        int days = DB.PLAYERS_LIFETIME_PLAYTIME.getInt("uuid", reportedUUID.toString(), "days");
                        int hours = DB.PLAYERS_LIFETIME_PLAYTIME.getInt("uuid", reportedUUID.toString(), "hours");
                        int minutes = DB.PLAYERS_LIFETIME_PLAYTIME.getInt("uuid", reportedUUID.toString(), "minutes");
                        int seconds = DB.PLAYERS_LIFETIME_PLAYTIME.getInt("uuid", reportedUUID.toString(), "seconds");
                        playTime = days + "d " + hours + "h " + minutes + "m " + seconds + "s";
                        proof = resultSet.getString("proof");
                    } catch(IllegalArgumentException e) {

                    }
                }
            } catch(SQLException e) {
                e.printStackTrace();
            } finally {
                DB.close(resultSet);
            }
            server = DB.PLAYERS_LOCATIONS.getString("uuid", reportedUUID.toString(), "location");
            if(server == null) {
                server = ChatColor.RED + "OFFLINE";
            }
            if(player != null) {
                MessageHandler.sendLine(player, "&e");
                MessageHandler.sendMessage(player, "&eReport ID #&b" + id + " &eby &b" + reporting + " &efor &b" + reason.toString().replace("_", " "));
                MessageHandler.sendMessage(player, "&eReported: &2" + reported + " &eplaytime: &b" + playTime);
                ChatClickHandler.sendMessageToRunCommand(player, " &cCLICK TO JOIN " + server, "Click to teleport to " + server, "/join " + server, "&eCurrent location: &b" + server);
                if(proof != null && !proof.equals("none")) {
                    MessageHandler.sendMessage(player, "&eProof: &b" + proof.replace("_", " "));
                }
                if(Ranks.isStaff(player)) {
                    if(reason == ReportReasons.CHAT_FILTER_DETECTION && open) {
                        ChatClickHandler.sendMessageToRunCommand(player, " &cCLICK TO KICK", "Click to kick this player", "/report startKicking " + id, "&eStatus: " + (open ? "&aOPEN" : "&cCLOSED"));
                    }
                    ChatClickHandler.sendMessageToRunCommand(player, open ? " &cCLICK TO CLOSE" : " &cCLICK TO RE-CLOSE", "Click to close this report", "/report close " + id, "&eStatus: " + (open ? "&aOPEN" : "&cCLOSED"));
                }
                MessageHandler.sendLine(player, "&e");
            }
        }
    }
}
