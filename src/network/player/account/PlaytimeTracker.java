package network.player.account;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import network.player.MessageHandler;
import network.server.ChatClickHandler;
import network.server.CommandBase;
import network.server.util.EffectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.Network;
import network.Network.Plugins;
import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.player.AsyncPlayerLeaveEvent;
import network.customevents.player.NewPlayerJoinEvent;
import network.customevents.player.PlayerAFKEvent;
import network.customevents.player.PlaytimeLoadedEvent;
import network.customevents.player.timed.PlayerDayOfPlaytimeEvent;
import network.customevents.player.timed.PlayerFirstThirtyMinutesOfPlaytimeEvent;
import network.customevents.player.timed.PlayerHourOfPlaytimeEvent;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;

import static network.server.DB.close;

public class PlaytimeTracker implements Listener {
	public static class Playtime {
		private int days = 0;
		private int hours = 0;
		private int minutes = 0;
		private int seconds = 0;
		private int monthlyDays = 0;
		private int monthlyHours = 0;
		private int monthlyMinutes = 0;
		private int monthlySeconds = 0;
		private int weeklyDays = 0;
		private int weeklyHours = 0;
		private int weeklyMinutes = 0;
		private int weeklySeconds = 0;
		
		public Playtime(UUID uuid) {
			if(DB.PLAYERS_LIFETIME_PLAYTIME.isUUIDSet(uuid)) {
				setPlayTime(uuid, DB.PLAYERS_LIFETIME_PLAYTIME, TimeType.LIFETIME);
			} else {
				setDays(0, TimeType.LIFETIME);
				setHours(0, TimeType.LIFETIME);
				setMinutes(0, TimeType.LIFETIME);
				setSeconds(0, TimeType.LIFETIME);
			}
			String [] keys = new String [] {"uuid", "month"};
			String [] values = new String [] {uuid.toString(), Calendar.getInstance().get(Calendar.MONTH) + ""};
			if(DB.PLAYERS_MONTHLY_PLAYTIME.isKeySet(keys, values)) {
				setPlayTime(uuid, DB.PLAYERS_MONTHLY_PLAYTIME, TimeType.MONTHLY);
			} else {
				setDays(0, TimeType.MONTHLY);
				setHours(0, TimeType.MONTHLY);
				setMinutes(0, TimeType.MONTHLY);
				setSeconds(0, TimeType.MONTHLY);
			}
			keys[1] = "week";
			values[1] = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) + "";
			if(DB.PLAYERS_WEEKLY_PLAYTIME.isKeySet(keys, values)) {
				setPlayTime(uuid, DB.PLAYERS_WEEKLY_PLAYTIME, TimeType.WEEKLY);
			} else {
				setDays(0, TimeType.WEEKLY);
				setHours(0, TimeType.WEEKLY);
				setMinutes(0, TimeType.WEEKLY);
				setSeconds(0, TimeType.WEEKLY);
			}
			Player player = Bukkit.getPlayer(uuid);
			if(player != null) {
				Bukkit.getPluginManager().callEvent(new PlaytimeLoadedEvent(player));
				if(Network.getPlugin() != Plugins.HUB && Network.getMiniGame() == null) {
					int required = 10;
					if(days == 0 && hours == 0 && minutes <= required) {
						Bukkit.getPluginManager().callEvent(new NewPlayerJoinEvent(player));
					}
				}
			}
		}
		
		public void setPlayTime(UUID uuid, DB db, TimeType type) {
			setDays(db.getInt("uuid", uuid.toString(), "days"), type);
			setHours(db.getInt("uuid", uuid.toString(), "hours"), type);
			setMinutes(db.getInt("uuid", uuid.toString(), "minutes"), type);
			setSeconds(db.getInt("uuid", uuid.toString(), "seconds"), type);
		}
		
		public int getDays(TimeType type) {
			return type == TimeType.LIFETIME ? this.days : type == TimeType.MONTHLY ? this.monthlyDays : weeklyDays;
		}
		
		public void setDays(int days, TimeType type) {
			if(type == TimeType.LIFETIME) {
				this.days = days;
			} else if(type == TimeType.MONTHLY) {
				this.monthlyDays = days;
			} else if(type == TimeType.WEEKLY) {
				this.weeklyDays = days;
			}
		}
		
		public int getHours(TimeType type) {
			return type == TimeType.LIFETIME ? this.hours : type == TimeType.MONTHLY ? this.monthlyHours : this.weeklyHours;
		}
		
		public void setHours(int hours, TimeType type) {
			if(type == TimeType.LIFETIME) {
				this.hours = hours;
			} else if(type == TimeType.MONTHLY) {
				this.monthlyHours = hours;
			} else if(type == TimeType.WEEKLY) {
				this.weeklyHours = hours;
			}
		}
		
		public int getMinutes(TimeType type) {
			return type == TimeType.LIFETIME ? this.minutes : type == TimeType.MONTHLY ? this.monthlyMinutes : this.weeklyMinutes;
		}
		
		public void setMinutes(int minutes, TimeType type) {
			if(type == TimeType.LIFETIME) {
				this.minutes = minutes;
			} else if(type == TimeType.MONTHLY) {
				this.monthlyMinutes = minutes;
			} else if(type == TimeType.WEEKLY) {
				this.weeklyMinutes = minutes;
			}
		}
		
		public int getSeconds(TimeType type) {
			return type == TimeType.LIFETIME ? this.seconds : type == TimeType.MONTHLY ? this.monthlySeconds : this.weeklySeconds;
		}
		
		public int setSeconds(int seconds, TimeType type) {
			if(type == TimeType.LIFETIME) {
				return this.seconds = seconds;
			} else if(type == TimeType.MONTHLY) {
				return this.monthlySeconds = seconds;
			} else {
				return this.weeklySeconds = seconds;
			}
		}
		
		public String getString(TimeType type) {
			return getDays(type) + "/" + getHours(type) + "/" + getMinutes(type) + "/" + getSeconds(type);
		}
		
		public String getDisplay(TimeType type) {
			return getDays(type) + "d " + getHours(type) + "h " + getMinutes(type) + "m";
		}
		
		void addSecond(Player player) {
			if(++seconds >= 60) {
				seconds = 0;
				++minutes;
				if(minutes == 30 && hours == 0 && days == 0) {
					Bukkit.getPluginManager().callEvent(new PlayerFirstThirtyMinutesOfPlaytimeEvent(player));
				}

				if(minutes % 30 == 0) {
					Bukkit.getPluginManager().callEvent(new PlayerHourOfPlaytimeEvent(player));
				}

				if(minutes >= 60) {
					minutes = 0;
					Bukkit.getPluginManager().callEvent(new PlayerHourOfPlaytimeEvent(player));
					++hours;
					if(hours >= 24) {
						hours = 0;
						Bukkit.getPluginManager().callEvent(new PlayerDayOfPlaytimeEvent(player));
						++days;
					}
				}
			}

			if(++monthlySeconds >= 60) {
				monthlySeconds = 0;
				if(++monthlyMinutes >= 60) {
					monthlyMinutes = 0;
					if(++monthlyHours >= 24) {
						monthlyHours = 0;
						++monthlyDays;
					}
				}
			}

			if(++weeklySeconds >= 60) {
				weeklySeconds = 0;
				if(++weeklyMinutes >= 60) {
					weeklyMinutes = 0;
					if(++weeklyHours >= 24) {
						weeklyHours = 0;
						++weeklyDays;
					}
				}
			}
		}
	}
	
	private static Map<String, Playtime> playtime = null;
	private static List<String> queue = null;
	private List<String> afk = null;
	public enum TimeType {LIFETIME, MONTHLY, WEEKLY}
	
	public PlaytimeTracker() {
		playtime = new HashMap<>();
		queue = new ArrayList<>();
		EventUtil.register(this);

		new CommandBase("test", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				test(player);
				return true;
			}
		}.setRequiredRank(AccountHandler.Ranks.STAFF);
	}
	
	public static Playtime getPlayTime(Player player) {
		return getPlayTime(player.getName());
	}
	
	public static Playtime getPlayTime(String name) {
		if(!playtime.containsKey(name)) {
			new AsyncDelayedTask(() -> {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					playtime.put(player.getName(), new Playtime(player.getUniqueId()));
				}
			});

			return null;
		}
		return playtime.get(name);
	}
	
	public static List<String> getTop5(TimeType timeType) {
		DB db = timeType == TimeType.LIFETIME ? DB.PLAYERS_LIFETIME_PLAYTIME : timeType == TimeType.MONTHLY ? DB.PLAYERS_MONTHLY_PLAYTIME : DB.PLAYERS_WEEKLY_PLAYTIME;
		List<String> names = new ArrayList<>();
		String key = null;
		int value = 0;

		if(timeType == TimeType.MONTHLY) {
			key = "month";
			value = Calendar.getInstance().get(Calendar.MONTH);
		} else if(timeType == TimeType.WEEKLY) {
			key = "week";
			value = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
		}

		List<String> allUUIDs;
		if(key == null) {
			allUUIDs = db.getOrdered("days DESC,hours DESC,minutes DESC,seconds", "uuid", 5, true);
		} else {
			allUUIDs = db.getOrdered("days DESC,hours DESC,minutes DESC,seconds", "uuid", key, String.valueOf(value), 5, true);
		}

		for(String uuidString : allUUIDs) {
			UUID uuid = UUID.fromString(uuidString);
			names.add(AccountHandler.getName(uuid));
		}

		Bukkit.getLogger().info("playtime: get top 5 " + timeType.toString());
		return names;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if((afk != null && afk.contains(player.getName())) || player.getVehicle() != null || player.getTicksLived() <= 40) {
					continue;
				}

				if(playtime.containsKey(player.getName())) {
					Playtime playtime = getPlayTime(player);
					if(playtime != null) {
						playtime.addSecond(player);
					}
				} else if(!queue.contains(player.getName())) {
					queue.add(player.getName());
				}
			}

			if(!queue.isEmpty()) {
				String name = queue.get(0);
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					getPlayTime(player);
				}
				queue.remove(0);
			}
		}
	}
	
	@EventHandler
	public void onPlayerAFKEvent(PlayerAFKEvent event) {
		if(event.getAFK()) {
			if(afk == null) {
				afk = new ArrayList<>();
			}
			if(!afk.contains(event.getPlayer().getName())) {
				afk.add(event.getPlayer().getName());
			}
		} else if(afk != null && afk.contains(event.getPlayer().getName())) {
			afk.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getUUID();
		String name = event.getName();

		if(playtime.containsKey(name)) {
			Playtime time = playtime.get(name);
			int days = time.getDays(TimeType.LIFETIME);
			int hours = time.getHours(TimeType.LIFETIME);
			int minutes = time.getMinutes(TimeType.LIFETIME);
			int seconds = time.getSeconds(TimeType.LIFETIME);
			if(DB.PLAYERS_LIFETIME_PLAYTIME.isUUIDSet(uuid)) {
				DB.PLAYERS_LIFETIME_PLAYTIME.updateInt("days", days, "uuid", uuid.toString());
				DB.PLAYERS_LIFETIME_PLAYTIME.updateInt("hours", hours, "uuid", uuid.toString());
				DB.PLAYERS_LIFETIME_PLAYTIME.updateInt("minutes", minutes, "uuid", uuid.toString());
				DB.PLAYERS_LIFETIME_PLAYTIME.updateInt("seconds", seconds, "uuid", uuid.toString());
			} else {
				DB.PLAYERS_LIFETIME_PLAYTIME.insert("'" + uuid.toString() + "', '" + days + "', '" + hours + "', '" + minutes + "', '" + seconds + "'");
			}

			days = time.getDays(TimeType.MONTHLY);
			hours = time.getHours(TimeType.MONTHLY);
			minutes = time.getMinutes(TimeType.MONTHLY);
			seconds = time.getSeconds(TimeType.MONTHLY);
			String month = Calendar.getInstance().get(Calendar.MONTH) + "";
			String [] keys = new String [] {"uuid", "month"};
			String [] values = new String [] {uuid.toString(), month};
			if(DB.PLAYERS_MONTHLY_PLAYTIME.isKeySet(keys, values)) {
				DB.PLAYERS_MONTHLY_PLAYTIME.updateInt("days", days, "uuid", uuid.toString());
				DB.PLAYERS_MONTHLY_PLAYTIME.updateInt("hours", hours, "uuid", uuid.toString());
				DB.PLAYERS_MONTHLY_PLAYTIME.updateInt("minutes", minutes, "uuid", uuid.toString());
				DB.PLAYERS_MONTHLY_PLAYTIME.updateInt("seconds", seconds, "uuid", uuid.toString());
			} else {
				DB.PLAYERS_MONTHLY_PLAYTIME.insert("'" + uuid.toString() + "', '" + days + "', '" + hours + "', '" + minutes + "', '" + seconds + "', '" + month + "'");
			}

			days = time.getDays(TimeType.WEEKLY);
			hours = time.getHours(TimeType.WEEKLY);
			minutes = time.getMinutes(TimeType.WEEKLY);
			seconds = time.getSeconds(TimeType.WEEKLY);
			String week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) + "";
			keys[1] = "week";
			values[1] = week;
			if(DB.PLAYERS_WEEKLY_PLAYTIME.isKeySet(keys, values)) {
				DB.PLAYERS_WEEKLY_PLAYTIME.updateInt("days", days, "uuid", uuid.toString());
				DB.PLAYERS_WEEKLY_PLAYTIME.updateInt("hours", hours, "uuid", uuid.toString());
				DB.PLAYERS_WEEKLY_PLAYTIME.updateInt("minutes", minutes, "uuid", uuid.toString());
				DB.PLAYERS_WEEKLY_PLAYTIME.updateInt("seconds", seconds, "uuid", uuid.toString());
			} else {
				DB.PLAYERS_WEEKLY_PLAYTIME.insert("'" + uuid.toString() + "', '" + days + "', '" + hours + "', '" + minutes + "', '" + seconds + "', '" + week + "'");
			}
			playtime.remove(name);
		}

		if(afk != null && afk.contains(name)) {
			afk.remove(name);
		}

		queue.remove(name);
	}

	@EventHandler
	public void onPlayerHourOfPlaytime(PlayerHourOfPlaytimeEvent event) {
		test(event.getPlayer());
	}

	public static void test(Player player) {
		if(AccountHandler.Ranks.STAFF.hasRank(player) && DB.Databases.PLAYERS.isEnabled()) {
			new AsyncDelayedTask(() -> {
				UUID uuid = player.getUniqueId();

				boolean remind = false;

				if(DB.PLAYERS_RECENT_VOTER.isUUIDSet(uuid)) {
					PreparedStatement statement = null;
					ResultSet resultSet = null;
					try {
						String query = "SELECT COUNT(id) FROM " + DB.PLAYERS_RECENT_VOTER.getName() + " WHERE uuid = '" + uuid + "' AND date < NOW() - INTERVAL 1 DAY LIMIT 1;";
						statement = DB.Databases.PLAYERS.getConnection().prepareStatement(query);
						resultSet = statement.executeQuery();
						if(resultSet.next()) {
							remind = resultSet.getInt("COUNT(id)") == 0;
						}
					} catch(SQLException e) {
						e.printStackTrace();
					} finally {
						close(statement, resultSet);
					}
				} else {
					remind = true;
				}

				if(remind) {
					MessageHandler.sendLine(player);
					MessageHandler.sendMessage(player, "Hey " + player.getName() + "!");
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendMessage(player, "You haven't &bvoted&x in the last 24 hours");
					MessageHandler.sendMessage(player, "Voting gives &bin game advantages&x & only takes a few seconds");
					MessageHandler.sendMessage(player, "");
					ChatClickHandler.sendMessageToRunCommand(player, " &c[CLICK HERE]", "Click", "/vote", "&eRun &b/vote&e or");
					MessageHandler.sendLine(player);
					EffectUtil.playSound(player, Sound.LEVEL_UP);
				}
			});
		}
	}
}
