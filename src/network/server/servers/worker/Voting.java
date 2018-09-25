package network.server.servers.worker;

import com.vexsoftware.votifier.model.VotifierEvent;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.CommandDispatcher;
import network.server.DB;
import network.server.servers.hub.crate.Beacon;
import network.server.servers.hub.crate.CrateTypes;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

public class Voting implements Listener {
    Voting() {
        new CommandBase("testVote", 1) {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                Voting.execute(arguments[0]);
                return true;
            }
        }.setRequiredRank(AccountHandler.Ranks.OWNER);

        EventUtil.register(this);
    }

    @EventHandler
    public void onVotifier(VotifierEvent event) {
        execute(event.getVote().getUsername());
    }

    public static void execute(String name) {
        new AsyncDelayedTask(() -> {
            UUID playerUUID = AccountHandler.getUUID(name);
            if(playerUUID != null) {
                String uuid = playerUUID.toString();

                DB.PLAYERS_RECENT_VOTER.insert("'" + uuid + "', '" + new Timestamp(Calendar.getInstance().getTime().getTime()) + "'");

                int multiplier = increaseStreak(playerUUID, uuid);
                increaseVotes(uuid);

                int amount = 5 * multiplier;

                if(DB.PLAYERS_VOTE_PASSES.isUUIDSet(playerUUID)) {
                    int passes = DB.PLAYERS_VOTE_PASSES.getInt("uuid", uuid, "vote_passes");
                    DB.PLAYERS_VOTE_PASSES.updateInt("vote_passes", passes + amount, "uuid", uuid);
                } else {
                    DB.PLAYERS_VOTE_PASSES.insert("'" + uuid + "', '" + amount + "'");
                }

//                Beacon.giveKey(playerUUID, multiplier, CrateTypes.VOTING);
//                giveParkourCheckpoints(playerUUID, uuid, multiplier);

//                CommandDispatcher.sendToGame("ONEVSONE", "addRankedMatches " + name + " " + (10 * multiplier));
//                CommandDispatcher.sendToGame("KITPVP", "addCoins " + name + " KITPVP " + (20 * multiplier));
//                CommandDispatcher.sendToGame("HUB", "checkpointFromVoting " + name + " " + (10 * multiplier));
                CommandDispatcher.sendToAll("say &e" + name + " has voted for perks & advantages. Run &a/vote");
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

            multiplier = streak <= 10 ? 1 : streak <= 20 ? 2 : 3;
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

//    private static void giveParkourCheckpoints(UUID playerUUID, String uuid, int multiplier) {
//        int toAdd = 10 * multiplier;
//
//        if(DB.HUB_PARKOUR_CHECKPOINTS.isUUIDSet(playerUUID)) {
//            int amount = DB.HUB_PARKOUR_CHECKPOINTS.getInt("uuid", uuid, "amount") + toAdd;
//            DB.HUB_PARKOUR_CHECKPOINTS.updateInt("amount", amount, "uuid", uuid);
//        } else {
//            DB.HUB_PARKOUR_CHECKPOINTS.insert("'" + uuid + "', '" + toAdd + "'");
//        }
//    }
}
