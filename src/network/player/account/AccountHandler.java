package network.player.account;

import network.Network;
import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerRankChangeEvent;
import network.player.MessageHandler;
import network.server.CommandBase;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import network.server.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccountHandler implements Listener {
	public enum Ranks {
		PLAYER(ChatColor.GRAY, ""),
		PRO(ChatColor.YELLOW, "[Pro]"),
		PRO_PLUS(ChatColor.GREEN, "[Pro&b+&a]"),
		YOUTUBER(ChatColor.LIGHT_PURPLE, "[YT]"),
		TRIAL(ChatColor.DARK_AQUA, "[Trial]"),
		STAFF(ChatColor.DARK_GREEN, "[Staff]"),
		SENIOR_STAFF(ChatColor.BLUE, "[Sr. Staff]"),
		OWNER(ChatColor.RED, "[Owner]");
		
		private ChatColor color = null;
		private String prefix = null;
		
		private Ranks(ChatColor color, String prefix) {
			this.color = color;
			this.prefix = color + ChatColor.translateAlternateColorCodes('&', prefix) + (prefix.equals("") ? "" : " " + ChatColor.WHITE);
		}
		
		public ChatColor getColor() {
			return this.color;
		}
		
		public String getPrefix() {
			return this.prefix;
		}
		
		public String getNoPermission() {
			return "&cTo use this you must have " + getPrefix();
		}
		
		public String getPermission() {
			return "rank." + toString().toLowerCase().replace("_", ".");
		}
		
		public boolean hasRank(CommandSender sender) {
			return getRank(sender).isAboveRank(this);
		}
		
		public boolean isAboveRank(Ranks rank) {
			return ordinal() >= rank.ordinal();
		}
		
		public static boolean isStaff(CommandSender sender) {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				return Ranks.TRIAL.hasRank(player);
			} else {
				return true;
			}
		}
		
		public static int getVotes(Player player) {
			Ranks rank = AccountHandler.getRank(player);
			return rank.ordinal() + 1 >= 5 ? 5 : rank.ordinal() + 1;
		}
	}
	
	private static Map<String, Ranks> ranks = null;
	
	public AccountHandler() {
		ranks = new HashMap<String, Ranks>();
		new CommandBase("setRank", 2, 3, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
						} else {
							try {
								Ranks rank = Ranks.valueOf(arguments[1].toUpperCase());
								Player target = ProPlugin.getPlayer(arguments[0]);
								if(target != null) {
									if(arguments.length == 3) {
										if(rank.hasRank(target)) {
											MessageHandler.sendMessage(sender, "&c" + target.getName() + " already has that rank");
											return;
										}
									}
									updateRank(target, rank);
								}
								setRank(uuid, rank);
								MessageHandler.sendMessage(sender, arguments[0] + " has been set to " + rank.getPrefix());
							} catch(IllegalArgumentException e) {
								MessageHandler.sendMessage(sender, "&cUnknown rank! Please use one of the following:");
								for(Ranks rank : Ranks.values()) {
									MessageHandler.sendMessage(sender, rank.toString() + " &a- " + rank.getPrefix());
								}
							}
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	public static String getPrefix(CommandSender sender) {
		return getPrefix(sender, true);
	}
	
	public static String getPrefix(CommandSender sender, boolean realPrefix) {
		return getPrefix(sender, realPrefix, false);
	}
	
	public static String getPrefix(CommandSender sender, boolean realPrefix, boolean ignoreClan) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			return realPrefix ? AccountHandler.getRank(player).getPrefix() + player.getName() : getRank(player).getPrefix() + player.getName();
		} else {
			return Ranks.OWNER.getPrefix() + sender.getName();
		}
	}
	
	public static String getPrefix(String name) {
		return getPrefix(name, false);
	}
	
	public static String getPrefix(String name, boolean realPrefix) {
		Player player = ProPlugin.getPlayer(name);
		if(player == null) {
			return null;
		} else {
			return getPrefix(player, realPrefix);
		}
	}
	
	public static String getPrefix(UUID uuid) {
		return getPrefix(uuid, false);
	}
	
	public static String getPrefix(UUID uuid, boolean realPrefix) {
		Player player = Bukkit.getPlayer(uuid);
		if(player == null) {
			Ranks rank = getRank(uuid);
			return rank.getPrefix() + getName(uuid);
		} else {
			return getPrefix(player, realPrefix);
		}
	}
	
	public static void updateRank(Player player, Ranks rank) {
		if(rank == Ranks.PLAYER) {
			ranks.remove(player.getName());
		} else {
			ranks.put(player.getName(), rank);
		}
		Bukkit.getPluginManager().callEvent(new PlayerRankChangeEvent(player, rank));
	}
	
	public static void setRank(Player player, Ranks rank) {
		setRank(player, rank, false);
	}
	
	public static void setRank(Player player, Ranks rank, boolean updateDatabase) {
		updateRank(player, rank);
		if(updateDatabase) {
			setRank(player.getUniqueId(), rank);
		}
	}
	
	public static void setRank(final UUID uuid, final Ranks rank) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				DB.PLAYERS_ACCOUNTS.updateString("rank", rank.toString(), "uuid", uuid.toString());
			}
		});
	}
	
	public static Ranks getRank(CommandSender sender) {
		return sender instanceof Player ? ranks.containsKey(sender.getName()) ? ranks.get(sender.getName()) : Ranks.PLAYER : Ranks.OWNER;
	}
	
	public static Ranks getRank(String name) {
		return ranks.containsKey(name) ? ranks.get(name) : Ranks.PLAYER;
	}
	
	public static Ranks getRank(UUID uuid) {
		try {
			return Ranks.PLAYER;//Ranks.valueOf(DB.PLAYERS_ACCOUNTS.getString("uuid", uuid.toString(), "rank"));
		} catch(NullPointerException e) {
			return Ranks.PLAYER;
		}
	}
	
	public static String getAddress(Player player) {
		return player.getAddress().getAddress().getHostAddress();
	}
	
	public static String getAddress(UUID uuid) {
		return DB.PLAYERS_ACCOUNTS.getString("uuid", uuid.toString(), "address");
	}
	
	public static UUID getUUID(String name) {
		try {
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				return player.getUniqueId();
			}
			return UUID.fromString(DB.PLAYERS_ACCOUNTS.getString("name", name, "uuid"));
		} catch(Exception e) {
			return null;
		}
	}
	
	public static UUID getUUIDFromIP(String address) {
		return UUID.fromString(DB.PLAYERS_ACCOUNTS.getString("address", address, "uuid"));
	}
	
	public static String getName(UUID uuid) {
		try {
			return DB.PLAYERS_ACCOUNTS.getString("uuid", uuid.toString(), "name");
		} catch(NullPointerException e) {
			return null;
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPrePlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		String address = event.getAddress().getHostAddress();
		if(DB.PLAYERS_ACCOUNTS.isUUIDSet(player.getUniqueId())) {
			Ranks rank = Ranks.valueOf(DB.PLAYERS_ACCOUNTS.getString("uuid", player.getUniqueId().toString(), "rank"));
			setRank(player, rank);
			if(Network.getPlugin() == Network.Plugins.HUB) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String uuid = player.getUniqueId().toString();
						String currentAddress = DB.PLAYERS_ACCOUNTS.getString("uuid", uuid, "address");
						if(!currentAddress.equals(address) || !DB.PLAYERS_IP_ADDRESSES.isKeySet("uuid", uuid)) {
							DB.PLAYERS_IP_ADDRESSES.insert("'" + uuid + "', '" + address + "', '" + TimeUtil.getTime() + "'");
						}
						DB.PLAYERS_ACCOUNTS.updateString("name", player.getName(), "uuid", uuid);
						DB.PLAYERS_ACCOUNTS.updateString("address", address, "uuid", uuid);
					}
				});
			}
		} else if(Network.getPlugin() == Network.Plugins.HUB) {
			Ranks startingRank = Ranks.PLAYER;
			setRank(player, startingRank);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					String uuid = player.getUniqueId().toString();
					String name = player.getName();
					String rank = startingRank.toString();
					DB.PLAYERS_ACCOUNTS.insert("'" + uuid + "', '" + name + "', '" + address + "', '" + rank + "', '" + TimeUtil.getTime().substring(0, 10) + "'");
					DB.PLAYERS_IP_ADDRESSES.insert("'" + uuid + "', '" + address + "', '" + TimeUtil.getTime() + "'");
				}
			});
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPostPlayerLogin(PlayerLoginEvent event) {
		if(event.getResult() == Result.KICK_FULL || event.getResult() == Result.KICK_WHITELIST || event.getResult() == Result.KICK_OTHER) {
			ranks.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		final String name = event.getPlayer().getName();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				ranks.remove(name);
			}
		});
	}
}
