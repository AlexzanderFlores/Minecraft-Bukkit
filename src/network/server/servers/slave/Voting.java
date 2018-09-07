package network.server.servers.slave;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.VotifierEvent;

import network.Network.Plugins;
import network.player.CoinsHandler;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.CommandDispatcher;
import network.server.DB;
import network.server.servers.hub.crate.Beacon;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;

public class Voting implements Listener {
	private static List<CoinsHandler> handlers = null;
	
	public Voting() {
//		new CommandBase("test", 1) {
//			@Override
//			public boolean execute(CommandSender sender, String [] arguments) {
//				Voting.execute(arguments[0]);
//				return true;
//			}
//		};
		handlers = new ArrayList<CoinsHandler>();
		handlers.add(new CoinsHandler(DB.PLAYERS_COINS_KIT_PVP, Plugins.KITPVP.getData()));
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onVotifier(VotifierEvent event) {
		execute(event.getVote().getUsername());
	}
	
	public static void execute(final String name) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				UUID playerUUID = AccountHandler.getUUID(name);
				if(playerUUID != null) {
					Bukkit.getLogger().info("voting: update lifetime votes");
					String uuid = playerUUID.toString();
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
							} else {
								streak = 1;
							}
						}
						multiplier = streak <= 5 ? 1 : streak <= 10 ? 2 : streak <= 15 ? 3 : streak <= 20 ? 4 : streak <= 25 ? 5 : 6;
						DB.PLAYERS_LIFETIME_VOTES.updateInt("amount", amount, "uuid", uuid);
						DB.PLAYERS_LIFETIME_VOTES.updateInt("day", currentDay, "uuid", uuid);
						DB.PLAYERS_LIFETIME_VOTES.updateInt("streak", streak, "uuid", uuid);
					} else {
						DB.PLAYERS_LIFETIME_VOTES.insert("'" + uuid + "', '1', '" + currentDay + "', '1', '1'");
					}
					Bukkit.getLogger().info("voting: update monthly votes");
					Calendar calendar = Calendar.getInstance();
					String month = calendar.get(Calendar.MONTH) + "";
					String [] keys = new String [] {"uuid", "month"};
					String [] values = new String [] {uuid, month};
					if(DB.PLAYERS_MONTHLY_VOTES.isKeySet(keys, values)) {
						int amount = DB.PLAYERS_MONTHLY_VOTES.getInt(keys, values, "amount") + 1;
						DB.PLAYERS_MONTHLY_VOTES.updateInt("amount", amount, keys, values);
					} else {
						DB.PLAYERS_MONTHLY_VOTES.insert("'" + uuid + "', '1', '" + month + "'");
					}
					Bukkit.getLogger().info("voting: update weekly votes");
					String week = calendar.get(Calendar.WEEK_OF_YEAR) + "";
					keys[1] = "week";
					values[1] = week;
					if(DB.PLAYERS_WEEKLY_VOTES.isKeySet(keys, values)) {
						int amount = DB.PLAYERS_WEEKLY_VOTES.getInt(keys, values, "amount") + 1;
						DB.PLAYERS_WEEKLY_VOTES.updateInt("amount", amount, keys, values);
					} else {
						DB.PLAYERS_WEEKLY_VOTES.insert("'" + uuid + "', '1', '" + week + "'");
					}
					Beacon.giveKey(playerUUID, 1 * multiplier, "voting");
					for(CoinsHandler handler : handlers) {
						Bukkit.getLogger().info("Giving 20 coins for " + handler.getPluginData());
						handler.addCoins(playerUUID, 20 * multiplier);
					}
					/*Bukkit.getLogger().info("voting: giving 3 sky wars loot passes");
					int toAdd = 3 * multiplier;
					if(DB.PLAYERS_SKY_WARS_LOOT_PASSES.isUUIDSet(playerUUID)) {
						int amount = DB.PLAYERS_SKY_WARS_LOOT_PASSES.getInt("uuid", uuid, "amount") + toAdd;
						DB.PLAYERS_SKY_WARS_LOOT_PASSES.updateInt("amount", amount, "uuid", uuid);
					} else {
						DB.PLAYERS_SKY_WARS_LOOT_PASSES.insert("'" + uuid + "', '" + toAdd + "'");
					}
					Bukkit.getLogger().info("voting: giving key fragment");
					KeyFragments.give(playerUUID, 1 * multiplier);
					Bukkit.getLogger().info("voting: giving pvp battles auto respawn passes");
					toAdd = 15 * multiplier;
					if(DB.PLAYERS_DOMINATION_AUTO_RESPAWN.isUUIDSet(playerUUID)) {
						int amount = DB.PLAYERS_DOMINATION_AUTO_RESPAWN.getInt("uuid", uuid, "amount") + toAdd;
						DB.PLAYERS_DOMINATION_AUTO_RESPAWN.updateInt("amount", amount, "uuid", uuid);
					} else {
						DB.PLAYERS_DOMINATION_AUTO_RESPAWN.insert("'" + uuid + "', '" + toAdd + "'");
					}
					Bukkit.getLogger().info("voting: giving speed uhc rescatter passes");
					toAdd = 1 * multiplier;
					if(DB.PLAYERS_SPEED_UHC_RESCATTER.isUUIDSet(playerUUID)) {
						int amount = DB.PLAYERS_SPEED_UHC_RESCATTER.getInt("uuid", uuid, "amount") + toAdd;
						DB.PLAYERS_SPEED_UHC_RESCATTER.updateInt("amount", amount, "uuid", uuid);
					} else {
						DB.PLAYERS_SPEED_UHC_RESCATTER.insert("'" + uuid + "', '" + toAdd + "'");
					}*/
					Bukkit.getLogger().info("voting: giving parkour course checkpoints");
					int toAdd = 10 * multiplier;
					if(DB.HUB_PARKOUR_CHECKPOINTS.isUUIDSet(playerUUID)) {
						int amount = DB.HUB_PARKOUR_CHECKPOINTS.getInt("uuid", uuid, "amount") + toAdd;
						DB.HUB_PARKOUR_CHECKPOINTS.updateInt("amount", amount, "uuid", uuid);
					} else {
						DB.HUB_PARKOUR_CHECKPOINTS.insert("'" + uuid + "', '" + toAdd + "'");
					}
					Bukkit.getLogger().info("voting: giving exp");
					CommandDispatcher.sendToGame("ONEVSONE", "addRankedMatches " + name + " 10");
					Bukkit.getLogger().info("voting: +10 ranked matches");
					CommandDispatcher.sendToAll("say &e" + name + " has voted for advantages. Run command &a/vote");
				}
			}
		});
	}
}
