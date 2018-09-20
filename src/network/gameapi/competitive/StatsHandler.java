package network.gameapi.competitive;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import network.Network;
import network.ProPlugin;
import network.customevents.game.GameDeathEvent;
import network.customevents.game.GameEndingEvent;
import network.customevents.game.GameKillEvent;
import network.customevents.game.GameLossEvent;
import network.customevents.game.GameWinEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerSpectatorEvent;
import network.customevents.player.StatsChangeEvent;
import network.customevents.player.PlayerSpectatorEvent.SpectatorState;
import network.gameapi.SpectatorHandler;
import network.gameapi.MiniGame.GameStates;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.DoubleUtil;
import network.server.util.EventUtil;
import network.server.util.TimeUtil;

public class StatsHandler implements Listener {
	public static class GameStats {
		private int wins = 0;
		private int losses = 0;
		private int kills = 0;
		private int deaths = 0;
		private int monthlyWins = 0;
		private int monthlyLosses = 0;
		private int monthlyKills = 0;
		private int monthlyDeaths = 0;
		private int weeklyWins = 0;
		private int weeklyLosses = 0;
		private int weeklyKills = 0;
		private int weeklyDeaths = 0;
		
		public GameStats(Player player) {
			String uuid = player.getUniqueId().toString();
			String [] keys = new String [] {"uuid", "date"};
			if(StatTimes.LIFETIME.getDB().isUUIDSet(player.getUniqueId())) {
				wins = StatTimes.LIFETIME.getDB().getInt("uuid", uuid, "wins");
				losses = StatTimes.LIFETIME.getDB().getInt("uuid", uuid, "losses");
				kills = StatTimes.LIFETIME.getDB().getInt("uuid", uuid, "kills");
				deaths = StatTimes.LIFETIME.getDB().getInt("uuid", uuid, "deaths");
			} else {
				StatTimes.LIFETIME.getDB().insert("'" + player.getUniqueId().toString() + "', '0', '0', '0', '0'");
			}
			if(StatTimes.MONTHLY.getDB() != null) {
				String [] values = new String [] {uuid, TimeUtil.getTime().substring(0, 7)};
				if(StatTimes.MONTHLY.getDB().isKeySet(keys, values)) {
					monthlyWins = StatTimes.MONTHLY.getDB().getInt(keys, values, "wins");
					monthlyLosses = StatTimes.MONTHLY.getDB().getInt(keys, values, "losses");
					monthlyKills = StatTimes.MONTHLY.getDB().getInt(keys, values, "kills");
					monthlyDeaths = StatTimes.MONTHLY.getDB().getInt(keys, values, "deaths");
				} else {
					String date = TimeUtil.getTime().substring(0, 7);
					StatTimes.MONTHLY.getDB().insert("'" + uuid + "', '" + date + "', '0', '0', '0', '0'");
				}
			}
			if(StatTimes.WEEKLY.getDB() != null) {
				String date = Calendar.getInstance().get(Calendar.YEAR) + "/" + Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
				String [] values = new String [] {uuid, date};
				if(StatTimes.WEEKLY.getDB().isKeySet(keys, values)) {
					weeklyWins = StatTimes.WEEKLY.getDB().getInt(keys, values, "wins");
					weeklyLosses = StatTimes.WEEKLY.getDB().getInt(keys, values, "losses");
					weeklyKills = StatTimes.WEEKLY.getDB().getInt(keys, values, "kills");
					weeklyDeaths = StatTimes.WEEKLY.getDB().getInt(keys, values, "deaths");
				} else {
					StatTimes.WEEKLY.getDB().insert("'" + uuid + "', '" + date + "', '0', '0', '0', '0'");
				}
			}
			gameStats.put(player.getName(), this);
		}
		
		public int getWins(StatTimes time) {
			return time == StatTimes.MONTHLY ? monthlyWins : time == StatTimes.WEEKLY ? weeklyWins : wins;
		}
		
		public void addWins(StatTimes time) {
			if(time == StatTimes.LIFETIME) {
				++wins;
			} else if(time == StatTimes.MONTHLY) {
				++monthlyWins;
			} else if(time == StatTimes.WEEKLY) {
				++weeklyWins;
			}
		}
		
		public int getLosses(StatTimes time) {
			return time == StatTimes.MONTHLY ? monthlyLosses : time == StatTimes.WEEKLY ? weeklyLosses : losses;
		}
		
		public void addLosses(StatTimes time) {
			if(time == StatTimes.LIFETIME) {
				++losses;
			} else if(time == StatTimes.MONTHLY) {
				++monthlyLosses;
			} else if(time == StatTimes.WEEKLY) {
				++weeklyLosses;
			}
		}
		
		public int getKills(StatTimes time) {
			return time == StatTimes.MONTHLY ? monthlyKills : time == StatTimes.WEEKLY ? weeklyKills : kills;
		}
		
		public void addKills(StatTimes time) {
			if(time == StatTimes.LIFETIME) {
				++kills;
			} else if(time == StatTimes.MONTHLY) {
				++monthlyKills;
			} else if(time == StatTimes.WEEKLY) {
				++weeklyKills;
			}
		}
		
		public int getDeaths(StatTimes time) {
			return time == StatTimes.MONTHLY ? monthlyDeaths : time == StatTimes.WEEKLY ? weeklyDeaths : deaths;
		}
		
		public void addDeaths(StatTimes time) {
			if(time == StatTimes.LIFETIME) {
				++deaths;
			} else if(time == StatTimes.MONTHLY) {
				++monthlyDeaths;
			} else if(time == StatTimes.WEEKLY) {
				++weeklyDeaths;
			}
		}
		
		public void removeDeath() {
			--deaths;
			--monthlyDeaths;
			--weeklyDeaths;
		}
	}
	
	private static Map<String, GameStats> gameStats = null;
	private static Map<String, String> combatTagged = null;
	private static List<UUID> queue = null;
	private static List<Network.Plugins> ignoreWinsLosses = null;
	public static enum StatTypes {RANK, WINS, LOSSES, KILLS, DEATHS};
	public static enum StatTimes {
		LIFETIME, MONTHLY, WEEKLY;
		
		private DB db = null;
		
		public DB getDB() {
			return this.db;
		}
		
		public void setDB(DB db) {
			this.db = db;
		}
	};
	private static DB elo = null;
	private static String wins = null;
	private static String losses = null;
	private static String kills = null;
	private static String deaths = null;
	private static boolean enabled = false;
	private static boolean saveOnQuit = true;
	private static boolean viewOnly = false;
	
	public StatsHandler(DB table) {
		this(table, null, null);
	}
	
	public StatsHandler(DB lifetime, DB monthly, DB weekly) {
		ignoreWinsLosses = new ArrayList<>();
		ignoreWinsLosses.add(Network.Plugins.KITPVP);
		ignoreWinsLosses.add(Network.Plugins.ONEVSONE);
		StatTimes.LIFETIME.setDB(lifetime);
		StatTimes.MONTHLY.setDB(monthly);
		StatTimes.WEEKLY.setDB(weekly);
		if(lifetime == null) {
			new CommandBase("stats", -1) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					MessageHandler.sendMessage(sender, "&cYou can only use this command on a game server");
					return true;
				}
			};
		} else {
			enabled = true;
			queue = new ArrayList<>();
			wins = "Wins";
			losses = "Losses";
			kills = "Kills";
			deaths = "Deaths";
			Bukkit.getScheduler().runTaskTimerAsynchronously(Network.getInstance(), new Runnable() {
				@Override
				public void run() {
					if(queue != null && !queue.isEmpty()) {
						UUID uuid = queue.get(0);
						queue.remove(0);
						Player player = Bukkit.getPlayer(uuid);
						if(player != null) {
							save(player);
						}
					}
				}
			}, 20, 20);

			new CommandBase("stats", 0, 1) {
				@Override
				public boolean execute(CommandSender sender, String[] arguments) {
					String name;
					if(arguments.length == 0) {
						if(sender instanceof Player) {
							Player player = (Player) sender;
							name = player.getName();
						} else {
							MessageHandler.sendUnknownCommand(sender);
							return true;
						}
					} else if(Ranks.PRO_PLUS.hasRank(sender)) {
						name = arguments[0];
					} else {
						MessageHandler.sendMessage(sender, Ranks.PRO_PLUS.getNoPermission());
						return true;
					}
					Player player = ProPlugin.getPlayer(name);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + name + " is not online");
					} else {
						loadStats(player);
						MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player, false) + "'s Statistics:");
						MessageHandler.sendMessage(sender, "Key: &cLifetime Stats &7/ &bMonthly Stats &7/ &aWeekly Stats");
						if(!gameStats.containsKey(player.getName())) {
							loadStats(player);
						}
						GameStats stats = gameStats.get(player.getName());
						if(!ignoreWinsLosses.contains(Network.getPlugin())) {
							MessageHandler.sendMessage(sender, "&e" + wins + ": &c" + stats.getWins(StatTimes.LIFETIME) + " &7/ &b" + stats.getWins(StatTimes.MONTHLY) + " &7/ &a" + stats.getWins(StatTimes.WEEKLY));
							MessageHandler.sendMessage(sender, "&e" + losses + ": &c" + stats.getLosses(StatTimes.LIFETIME) + " &7/ &b" + stats.getLosses(StatTimes.MONTHLY) + " &7/ &a" + stats.getLosses(StatTimes.WEEKLY));
						}
						MessageHandler.sendMessage(sender, "&e" + kills + ": &c" + stats.getKills(StatTimes.LIFETIME) + " &7/ &b" + stats.getKills(StatTimes.MONTHLY) + " &7/ &a" + stats.getKills(StatTimes.WEEKLY));
						MessageHandler.sendMessage(sender, "&e" + deaths + ": &c" + stats.getDeaths(StatTimes.LIFETIME) + " &7/ &b" + stats.getDeaths(StatTimes.MONTHLY) + " &7/ &a" + stats.getDeaths(StatTimes.WEEKLY));
						double kills = (double) gameStats.get(player.getName()).getKills(StatTimes.LIFETIME);
						double deaths = (double) gameStats.get(player.getName()).getDeaths(StatTimes.LIFETIME);
						double monthlyKills = (double) gameStats.get(player.getName()).getKills(StatTimes.MONTHLY);
						double monthlyDeaths = (double) gameStats.get(player.getName()).getDeaths(StatTimes.MONTHLY);
						double weeklyKills = (double) gameStats.get(player.getName()).getKills(StatTimes.WEEKLY);
						double weeklyDeaths = (double) gameStats.get(player.getName()).getDeaths(StatTimes.WEEKLY);
						double kdr = (kills == 0 || deaths == 0 ? 0 : DoubleUtil.round(kills / deaths, 2));
						double monthlyKdr = (monthlyKills == 0 || monthlyDeaths == 0 ? 0 : DoubleUtil.round(monthlyKills / monthlyDeaths, 2));
						double weeklyKdr = (weeklyKills == 0 || weeklyDeaths == 0 ? 0 : DoubleUtil.round(weeklyKills / weeklyDeaths, 2));
						MessageHandler.sendMessage(sender, "&eKDR: &c" + kdr + " &7/ &b" + monthlyKdr + " &7/ &a" + weeklyKdr);
					}
					return true;
				}
			};

			new StatRanking();
			EventUtil.register(this);
		}
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void setEloDB(DB elo, int starting) {
		if(StatsHandler.elo == null) {
			new EloHandler(starting);
		}
		StatsHandler.elo = elo;
	}
	
	public static DB getEloDB() {
		return elo;
	}
	
	public static boolean getSaveOnQuit() {
		return saveOnQuit;
	}
	
	public static boolean getViewOnly() {
		return viewOnly;
	}
	
	public static void setSaveOnQuit(boolean saveOnQuit) {
		StatsHandler.saveOnQuit = saveOnQuit;
	}
	
	public static void loadStats(Player player) {
		if(gameStats == null) {
			gameStats = new HashMap<String, GameStats>();
		}
		if(!gameStats.containsKey(player.getName())) {
			new GameStats(ProPlugin.getPlayer(player.getName()));
		}
	}
	
	public static void setViewOnly(boolean viewOnly) {
		StatsHandler.viewOnly = viewOnly;
	}
	
	public static void setWinsString(String wins) {
		StatsHandler.wins = wins;
	}
	
	public static void setLossesString(String losses) {
		StatsHandler.losses = losses;
	}
	
	public static void setKillsString(String kills) {
		StatsHandler.kills = kills;
	}
	
	public static void setDeathsString(String deaths) {
		StatsHandler.deaths = deaths;
	}
	
	public static int getWins(Player player, StatTimes time) {
		loadStats(player);
		return gameStats.get(player.getName()).getWins(time);
	}
	
	public static int getLosses(Player player, StatTimes time) {
		loadStats(player);
		return gameStats.get(player.getName()).getLosses(time);
	}
	
	public static int getKills(Player player, StatTimes time) {
		loadStats(player);
		return gameStats.get(player.getName()).getKills(time);
	}
	
	public static int getDeaths(Player player, StatTimes time) {
		loadStats(player);
		return gameStats.get(player.getName()).getDeaths(time);
	}
	
	private static boolean canEditStats(Player player) {
		if(viewOnly) {
			return false;
		}
		StatsChangeEvent event = new StatsChangeEvent(player);
		Bukkit.getPluginManager().callEvent(event);
		return !event.isCancelled();
	}
	
	public static void addWin(Player player) {
		if(!canEditStats(player) || player == null || !player.isOnline()) {
			return;
		}
		loadStats(player);
		for(StatTimes time : StatTimes.values()) {
			gameStats.get(player.getName()).addWins(time);
		}
		if(!queue.contains(player.getUniqueId())) {
			queue.add(player.getUniqueId());
		}
	}
	
	public static void addLoss(Player player) {
		if(!canEditStats(player) || player == null || !player.isOnline()) {
			return;
		}
		loadStats(player);
		for(StatTimes time : StatTimes.values()) {
			gameStats.get(player.getName()).addLosses(time);
		}
		if(!queue.contains(player.getUniqueId())) {
			queue.add(player.getUniqueId());
		}
	}
	
	public static void addKill(Player player) {
		if(!canEditStats(player) || player == null || !player.isOnline()) {
			return;
		}
		loadStats(player);
		for(StatTimes time : StatTimes.values()) {
			gameStats.get(player.getName()).addKills(time);
		}
		if(!queue.contains(player.getUniqueId())) {
			queue.add(player.getUniqueId());
		}
	}
	
	public static void addDeath(Player player) {
		if(!canEditStats(player) || player == null || !player.isOnline()) {
			return;
		}
		loadStats(player);
		for(StatTimes time : StatTimes.values()) {
			gameStats.get(player.getName()).addDeaths(time);
		}
		if(!queue.contains(player.getUniqueId())) {
			queue.add(player.getUniqueId());
		}
	}
	
	public static void removeDeath(Player player) {
		if(!canEditStats(player) || player == null || !player.isOnline()) {
			return;
		}
		loadStats(player);
		gameStats.get(player.getName()).removeDeath();
		if(!queue.contains(player.getUniqueId())) {
			queue.add(player.getUniqueId());
		}
	}
	
	public static void save(Player player) {
		GameStats stats = gameStats.get(player.getName());
		if(stats == null) {
			return;
		}
		String uuid = player.getUniqueId().toString();
		for(StatTimes time : StatTimes.values()) {
			if(time == StatTimes.LIFETIME && time.getDB() != null) {
				time.getDB().updateInt("wins", stats.getWins(StatTimes.LIFETIME), "uuid", uuid);
				time.getDB().updateInt("losses", stats.getLosses(StatTimes.LIFETIME), "uuid", uuid);
				time.getDB().updateInt("kills", stats.getKills(StatTimes.LIFETIME), "uuid", uuid);
				time.getDB().updateInt("deaths", stats.getDeaths(StatTimes.LIFETIME), "uuid", uuid);
			} else if(time == StatTimes.MONTHLY && time.getDB() != null) {
				String [] keys = new String [] {"uuid", "date"};
				String [] values = new String [] {uuid, TimeUtil.getTime().substring(0, 7)};
				time.getDB().updateInt("wins", stats.getWins(StatTimes.MONTHLY), keys, values);
				time.getDB().updateInt("losses", stats.getLosses(StatTimes.MONTHLY), keys, values);
				time.getDB().updateInt("kills", stats.getKills(StatTimes.MONTHLY), keys, values);
				time.getDB().updateInt("deaths", stats.getDeaths(StatTimes.MONTHLY), keys, values);
			} else if(time == StatTimes.WEEKLY && time.getDB() != null) {
				String [] keys = new String [] {"uuid", "date"};
				String [] values = new String [] {uuid, Calendar.getInstance().get(Calendar.YEAR) + "/" + Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)};
				time.getDB().updateInt("wins", stats.getWins(StatTimes.WEEKLY), keys, values);
				time.getDB().updateInt("losses", stats.getLosses(StatTimes.WEEKLY), keys, values);
				time.getDB().updateInt("kills", stats.getKills(StatTimes.WEEKLY), keys, values);
				time.getDB().updateInt("deaths", stats.getDeaths(StatTimes.WEEKLY), keys, values);
			}
		}
		gameStats.remove(player.getName());
	}
	
	public static List<String> getTop10(StatTimes time) {
		if(time.getDB() == null) {
			return new ArrayList<String>();
		}
		List<String> stats = new ArrayList<String>();
		ResultSet resultSet = null;
		try {
			String query = "SELECT uuid, kills FROM " + time.getDB().getName() + " ";
			if(time == StatTimes.MONTHLY) {
				String month = TimeUtil.getTime().substring(0, 7);
				query += "WHERE date = '" + month + "' ";
			} else if(time == StatTimes.WEEKLY) {
				query += "WHERE date = '" + Calendar.getInstance().get(Calendar.YEAR) + "/" + Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) + "' ";
			}
			query += "ORDER BY kills DESC LIMIT 10";
			resultSet = time.getDB().getConnection().prepareStatement(query).executeQuery();
			int counter = 0;
			while(resultSet.next()) {
				String name = AccountHandler.getName(UUID.fromString(resultSet.getString("uuid")));
				int kills = resultSet.getInt("kills");
				stats.add("&e" + ++counter + ". &b" + name + " &c" + kills + " kill" + (kills == 1 ? "" : "s"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			DB.close(resultSet);
		}
		return stats;
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(event.getPlayer() != null) {
			addWin(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onGameLoss(GameLossEvent event) {
		if(event.getPlayer() != null) {
			addLoss(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		addKill(event.getPlayer());
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		addDeath(event.getPlayer());
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : Bukkit.getOnlinePlayers()) {
					save(player);
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile)) {
			Player attacker = null;
			if(event.getDamager() instanceof Player) {
				attacker = (Player) event.getDamager();
			} else if(event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(projectile.getShooter() instanceof Player) {
					attacker = (Player) projectile.getShooter();
				}
			}
			if(attacker != null && !SpectatorHandler.contains(attacker)) {
				final Player player = (Player) event.getEntity();
				if(!SpectatorHandler.contains(player)) {
					if(combatTagged == null) {
						combatTagged = new HashMap<String, String>();
					}
					combatTagged.put(player.getName(), attacker.getName());
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							combatTagged.remove(player.getName());
						}
					}, 20 * 5);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpectatorStart(PlayerSpectatorEvent event) {
		if(event.getState() == SpectatorState.STARTING && combatTagged != null) {
			combatTagged.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(!SpectatorHandler.contains(player) && combatTagged != null && combatTagged.containsKey(player.getName())) {
			if(Network.getMiniGame() != null && Network.getMiniGame().getGameState() != GameStates.STARTED) {
				return;
			}
			Player attacker = ProPlugin.getPlayer(combatTagged.get(player.getName()));
			if(attacker != null) {
				addKill(attacker);
				MessageHandler.sendMessage(attacker, "Given 1 kill due to " + player.getName() + " combat logging");
			}
			addLoss(player);
			addDeath(player);
			combatTagged.remove(player.getName());
		}
		if(gameStats != null && gameStats.containsKey(player.getName())) {
			if(saveOnQuit) {
				save(player);
			}
			gameStats.remove(player.getName());
		}
	}
}
