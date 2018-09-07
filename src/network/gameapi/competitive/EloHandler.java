package network.gameapi.competitive;

import network.Network;
import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.AsyncPlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;

public class EloHandler implements Listener {
	private static Map<String, Integer> elo = null;
	private static List<UUID> queue = null;
	private static DB db = null;
	private static int starting = 0;
	
	public EloHandler(int starting) {
		this(null, starting);
	}
	
	public EloHandler(DB db, int starting) {
		elo = new HashMap<String, Integer>();
		queue = new ArrayList<UUID>();
		EloHandler.db = db;
		EloHandler.starting = starting;
		new CommandBase("elo", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String target = null;
				if(arguments.length == 0) {
					target = sender.getName();
				} else if(Ranks.VIP_PLUS.hasRank(sender)) {
					target = arguments[0];
				} else {
					MessageHandler.sendMessage(sender, Ranks.VIP_PLUS.getNoPermission());
					return true;
				}
				if(elo.containsKey(target)) {
					MessageHandler.sendMessage(sender, target + " has an elo of &e" + elo.get(target));
				} else {
					MessageHandler.sendMessage(sender, "&c" + target + " has no elo logged on this server");
				}
				return true;
			}
		}.setRequiredRank(Ranks.VIP);
		EventUtil.register(this);
		Bukkit.getScheduler().runTaskTimerAsynchronously(Network.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(queue != null && !queue.isEmpty()) {
					UUID uuid = queue.get(0);
					queue.remove(0);
					Player player = Bukkit.getPlayer(uuid);
					if(player != null && elo.containsKey(player.getName())) {
						DB db = EloHandler.db == null ? StatsHandler.getEloDB() : EloHandler.db;
						int amount = elo.get(player.getName());
						if(db.isUUIDSet(uuid)) {
							db.updateInt("elo", amount, "uuid", uuid.toString());
						} else {
							db.insert("'" + uuid.toString() + "', '" + amount + "'");
						}
					}
				}
			}
		}, 20, 20);
	}
	
	public static void calculateWin(Player winner, Player loser, int display) {
		if(winner == null || loser == null) {
			return;
		}
		int elo1 = getElo(loser);
		int elo2 = getElo(winner);
		int K = 32;
		int diff = elo1 - elo2;
		double percentage = 1 / (1 + Math.pow(10, diff / 400));
		int amount = (int) Math.round(K * (1 - percentage));
		if(amount < 1) {
			amount = 1;
		}
		int winnerResult = add(winner, amount);
		int loserResult = add(loser, -amount);
		if(display > 0) { // 0 = don't display, 1 = display change, 2 = display change + new value
			String newWinner = AccountHandler.getPrefix(winner) + (display == 2 ? " &6" + winnerResult : "") + " &a(+" + amount + ")";
			String newLoser = AccountHandler.getPrefix(loser) + (display == 2 ? " &6" + loserResult : "") + " &c(" + amount * -1 + ")";
			for(Player player : new Player [] {winner, loser}) {
				MessageHandler.sendLine(player);
				MessageHandler.sendMessage(player, newWinner);
				MessageHandler.sendMessage(player, newLoser);
				MessageHandler.sendLine(player);
			}
		}
		queue.add(winner.getUniqueId());
		queue.add(loser.getUniqueId());
	}
	
	public static int getElo(Player player) {
		return elo == null || !elo.containsKey(player.getName()) ? 0 : elo.get(player.getName());
	}
	
	public static int add(Player player, int amount) {
		elo.put(player.getName(), getElo(player) + amount);
		return elo.get(player.getName());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		UUID uuid = player.getUniqueId();
		DB db = EloHandler.db == null ? StatsHandler.getEloDB() : EloHandler.db;
		if(db != null) {
			if(db.isUUIDSet(uuid)) {
				elo.put(name, db.getInt("uuid", uuid.toString(), "elo"));
			} else {
				elo.put(name, starting);
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getUUID();
		String name = event.getName();
		if(elo.containsKey(name)) {
			DB db = EloHandler.db == null ? StatsHandler.getEloDB() : EloHandler.db;
			int amount = elo.get(name);
			if(db.isUUIDSet(uuid)) {
				db.updateInt("elo", amount, "uuid", uuid.toString());
			} else {
				db.insert("'" + uuid.toString() + "', '" + amount + "'");
			}
			elo.remove(name);
		}
	}
}
