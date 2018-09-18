package network.server.servers.hub;

import com.vexsoftware.votifier.model.VotifierEvent;
import network.Network;
import network.ProPlugin;
import network.player.CoinsHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.CommandDispatcher;
import network.server.DB;
import network.server.servers.hub.crate.Beacon;
import network.server.servers.hub.crate.CrateTypes;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class Voting implements Listener {
    private static List<CoinsHandler> handlers = null;

    Voting() {
        new CommandBase("testVote", 1) {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                Player player = ProPlugin.getPlayer(arguments[0]);
                if(player == null) {
                    MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
                } else {
                    Voting.execute(player.getName());
                }
                return true;
            }
        }.setRequiredRank(AccountHandler.Ranks.OWNER);

        handlers = new ArrayList<CoinsHandler>();
        handlers.add(new CoinsHandler(DB.PLAYERS_COINS_KIT_PVP, Network.Plugins.KITPVP.getData()));
        EventUtil.register(this);
    }

    @EventHandler
    public void onVotifier(VotifierEvent event) {
        execute(event.getVote().getUsername());
    }

    public static void execute(String name) {
        new AsyncDelayedTask(new Runnable() {
            @Override
            public void run() {
                UUID playerUUID = AccountHandler.getUUID(name);
                if(playerUUID != null) {
                    String uuid = playerUUID.toString();

                    int multiplier = increaseStreak(playerUUID, uuid);
                    increaseVotes(uuid);

                    Beacon.giveKey(playerUUID, multiplier, CrateTypes.VOTING);
                    for(CoinsHandler handler : handlers) {
                        handler.addCoins(playerUUID, 20 * multiplier);
                    }

                    giveParkourCheckpoints(playerUUID, uuid, multiplier);

                    CommandDispatcher.sendToGame("ONEVSONE", "addRankedMatches " + name + " 10");
                    CommandDispatcher.sendToAll("say &e" + name + " has voted for perks & advantages. Run &a/vote");
                }
            }
        });
    }

    private static int increaseStreak(UUID playerUUID, String uuid) {
        int streak = 1;
        int multiplier = 1;
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if(DB.PLAYERS_LIFETIME_VOTES.isUUIDSet(playerUUID)) {
            int amount = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "amount") + 1;
            int day = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "day");

            if(day == currentDay - 1) {
                streak = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "streak") + 1;
                if(streak > DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "highest_streak")) {
                    DB.PLAYERS_LIFETIME_VOTES.updateInt("highest_streak", streak, "uuid", uuid);
                }
            } else {
                if(day == currentDay) {
                    streak = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "streak");
                }
            }

            multiplier = streak <= 5 ? 1 : streak <= 10 ? 2 : streak <= 15 ? 3 : streak <= 20 ? 4 : streak <= 25 ? 5 : 6;
            DB.PLAYERS_LIFETIME_VOTES.updateInt("amount", amount, "uuid", uuid);
            DB.PLAYERS_LIFETIME_VOTES.updateInt("day", currentDay, "uuid", uuid);
            DB.PLAYERS_LIFETIME_VOTES.updateInt("streak", streak, "uuid", uuid);
        } else {
            DB.PLAYERS_LIFETIME_VOTES.insert("'" + uuid + "', '1', '" + currentDay + "', '1', '1'");
        }

        return multiplier;
    }

    private static void increaseVotes(String uuid) {
        Calendar calendar = Calendar.getInstance();
        String month = calendar.get(Calendar.MONTH) + "";
        String [] keys = new String [] { "uuid", "month" };
        String [] values = new String [] { uuid, month };

        if(DB.PLAYERS_MONTHLY_VOTES.isKeySet(keys, values)) {
            int amount = DB.PLAYERS_MONTHLY_VOTES.getInt(keys, values, "amount") + 1;
            DB.PLAYERS_MONTHLY_VOTES.updateInt("amount", amount, keys, values);
        } else {
            DB.PLAYERS_MONTHLY_VOTES.insert("'" + uuid + "', '1', '" + month + "'");
        }

        String week = calendar.get(Calendar.WEEK_OF_YEAR) + "";
        keys[1] = "week";
        values[1] = week;

        if(DB.PLAYERS_WEEKLY_VOTES.isKeySet(keys, values)) {
            int amount = DB.PLAYERS_WEEKLY_VOTES.getInt(keys, values, "amount") + 1;
            DB.PLAYERS_WEEKLY_VOTES.updateInt("amount", amount, keys, values);
        } else {
            DB.PLAYERS_WEEKLY_VOTES.insert("'" + uuid + "', '1', '" + week + "'");
        }
    }

    private static void giveParkourCheckpoints(UUID playerUUID, String uuid, int multiplier) {
        int toAdd = 10 * multiplier;

        if(DB.HUB_PARKOUR_CHECKPOINTS.isUUIDSet(playerUUID)) {
            int amount = DB.HUB_PARKOUR_CHECKPOINTS.getInt("uuid", uuid, "amount") + toAdd;
            DB.HUB_PARKOUR_CHECKPOINTS.updateInt("amount", amount, "uuid", uuid);
        } else {
            DB.HUB_PARKOUR_CHECKPOINTS.insert("'" + uuid + "', '" + toAdd + "'");
        }
    }
}
