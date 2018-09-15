package network.player;

import network.Network;
import network.Network.Plugins;
import network.ProPlugin;
import network.customevents.game.GameKillEvent;
import network.customevents.game.GameWinEvent;
import network.customevents.player.AsyncPlayerLeaveEvent;
import network.customevents.player.CoinGiveEvent;
import network.customevents.player.CoinUpdateEvent;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CoinsHandler implements Listener {
	private static Map<String, CoinsHandler> handlers = new HashMap<String, CoinsHandler>();
	private static int killCoins = 0;
	private static int winCoins = 0;
	private DB table = null;
	//private Plugins plugin = null;
	private Map<String, Integer> coins = null;
	private List<String> newPlayer = null;
	//private boolean boosterEnabled = false;
	private String pluginData = null;
	
	public CoinsHandler(DB table, String pluginData) {
		if(!handlers.containsKey(pluginData)) {
			this.pluginData = pluginData;
			this.table = table;
			//this.plugin = plugin;
			coins = new HashMap<String, Integer>();
			newPlayer = new ArrayList<String>();
			handlers.put(pluginData, this);
			new CommandBase("addCoins", 3) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					String name = arguments[0];
					String game = arguments[1];
					int amount = Integer.valueOf(arguments[2]);
					Plugins plugin = null;
					try {
						plugin = Plugins.valueOf(game);
					} catch(Exception e) {
						for(Plugins plugins : Plugins.values()) {
							MessageHandler.sendMessage(sender, plugins.toString());
						}
						return true;
					}
					String pluginData = plugin.getData();
					if(handlers.containsKey(pluginData)) {
						Player player = ProPlugin.getPlayer(name);
						if(player == null) {
							MessageHandler.sendMessage(sender, "&c" + name + " is not online");
						} else {
							CoinsHandler handler = handlers.get(pluginData);
							handler.addCoins(player, amount);
						}
					} else {
						MessageHandler.sendMessage(sender, "&cThere is no handler for " + plugin.toString());
					}
					return true;
				}
			}.setRequiredRank(Ranks.OWNER);
			new CommandBase("coins", 0, 1) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					if(Network.getPlugin() == Plugins.HUB) {
						MessageHandler.sendMessage(sender, "&cYou can only use this command in-game");
					} else {
						Player target = null;
						if(arguments.length == 0) {
							if(sender instanceof Player) {
								target = (Player) sender;
							} else {
								MessageHandler.sendPlayersOnly(sender);
								return true;
							}
						} else if(arguments.length == 1) {
							if(Ranks.PRO.hasRank(sender)) {
								target = ProPlugin.getPlayer(arguments[0]);
								if(target == null) {
									MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
									return true;
								}
							} else {
								MessageHandler.sendMessage(sender, Ranks.PRO.getNoPermission());
								return true;
							}
						}
						Plugins plugin = Network.getPlugin();
						CoinsHandler coinsHandler = getCoinsHandler(plugin.getData());
						if(coinsHandler == null) {
							MessageHandler.sendMessage(sender, "&cError on loading the coins handler for \"" + plugin.getData() + "\"");
						} else {
							MessageHandler.sendMessage(sender, AccountHandler.getPrefix(target) + " &ehas &b" + coinsHandler.getCoins(target) + " &ecoins in " + plugin.getDisplay());
						}
					}
					return true;
				}
			};
			EventUtil.register(this);
		}
	}
	
	public static CoinsHandler getCoinsHandler(String pluginData) {
		return handlers.get(pluginData);
	}
	
	public static int getWinCoins() {
		return winCoins;
	}
	
	public static void setWinCoins(int winCoins) {
		CoinsHandler.winCoins = winCoins;
	}
	
	public static int getKillCoins() {
		return killCoins;
	}
	
	public static void setKillCoins(int killCoins) {
		CoinsHandler.killCoins = killCoins;
	}
	
	public String getPluginData() {
		return pluginData;
	}
	
	public int getCoins(Player player) {
		if(!coins.containsKey(player.getName())) {
			if(!table.isUUIDSet(player.getUniqueId()) && !newPlayer.contains(player.getName())) {
				newPlayer.add(player.getName());
			}
			coins.put(player.getName(), table.getInt("uuid", player.getUniqueId().toString(), "coins"));
		}
		return coins.get(player.getName());
	}
	
	public void addCoins(Player player, int amount) {
		addCoins(player, amount, null);
	}
	
	public void addCoins(Player player, int amount, String message) {
		if(amount == 0) {
			return;
		}
		CoinGiveEvent event = new CoinGiveEvent(player, amount);
		Bukkit.getPluginManager().callEvent(event);
		amount = event.getAmount();
		if(amount > 0) {
			if(Ranks.PRO_PLUS.hasRank(player)) {
				amount *= 3;
			} else if(Ranks.PRO.hasRank(player)) {
				amount *= 2;
			}
		}
		String msg = (amount >= 0 ? "&b+" : "&c") + amount + " Coin" + (amount == 1 ? "" : "s");
		if(message != null) {
			msg += " " + message;
		}
		MessageHandler.sendMessage(player, msg);
		/*if(!Ranks.PRO.hasRank(player)) {
			MessageHandler.sendMessage(player, RankAds.getAlerts()[0]);
		}*/
		if(coins.containsKey(player.getName())) {
			amount += coins.get(player.getName());
		}
		coins.put(player.getName(), amount);
		Bukkit.getPluginManager().callEvent(new CoinUpdateEvent(player));
	}
	
	public void addCoins(UUID uuid, int toAdd) {
		if(table.isUUIDSet(uuid)) {
			int coins = table.getInt("uuid", uuid.toString(), "coins") + toAdd;
			table.updateInt("coins", coins, "uuid", uuid.toString());
		} else {
			table.insert("'" + uuid.toString() + "', '" + toAdd + "'");
		}
	}
	
	public boolean isNewPlayer(Player player) {
		return newPlayer != null && newPlayer.contains(player.getName());
	}
	
	public ItemStack getItemStack(Player player) {
		return new ItemCreator(Material.GOLD_INGOT).setName("&7Coins: &a" + getCoins(player)).setLores(new String [] {
			"",
			"&eRanks get &cx2 &ecoins &a/buy",
			"&eGet more coins daily &a/vote",
			""
		}).getItemStack();
	}
	
	/*@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 5) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					
				}
			});
		}
	}*/
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		if(killCoins > 0) {
			addCoins(event.getPlayer(), killCoins);
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(winCoins > 0) {
			if(event.getTeam() == null) {
				addCoins(event.getPlayer(), winCoins);
			} else {
				for(OfflinePlayer offlinePlayer : event.getTeam().getPlayers()) {
					if(offlinePlayer.isOnline()) {
						Player player = (Player) offlinePlayer;
						addCoins(player, winCoins);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(coins.containsKey(name)) {
			UUID uuid = event.getUUID();
			int amount = coins.get(name);
			if(table.isUUIDSet(uuid)) {
				if(amount <= 0) {
					table.delete("uuid", uuid.toString());
				} else {
					table.updateInt("coins", amount, "uuid", uuid.toString());
				}
			} else if(amount > 0) {
				table.insert("'" + uuid.toString() + "', '" + amount + "'");
			}
			coins.remove(name);
		}
		newPlayer.remove(name);
	}
}
