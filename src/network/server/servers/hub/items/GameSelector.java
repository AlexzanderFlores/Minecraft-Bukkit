package network.server.servers.hub.items;

import network.Network.Plugins;
import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.server.DB.Databases;
import network.server.servers.hub.HubBase;
import network.server.servers.hub.HubItemBase;
import network.server.servers.hub.ParkourNPC;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EffectUtil;
import network.server.util.ItemCreator;
import network.server.util.ItemUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSelector extends HubItemBase {
	private static List<String> inMenu = null;
	private static Map<String, Plugins> watching = null;
	private static Map<Plugins, Integer> itemSlots = null;
	private static Map<Plugins, Integer> players = null;
	private static Map<Plugins, String> onlyServer = null;
	private static final int rows = 3;
	private static final int size = 9 * rows;
	private static final String name = "Game Selector";
	
	public GameSelector() {
		super(new ItemCreator(Material.COMPASS).setName("&e" + name), 0);
		inMenu = new ArrayList<>();
		watching = new HashMap<>();
		itemSlots = new HashMap<>();
		players = new HashMap<>();
		onlyServer = new HashMap<>();
	}

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		giveItem(player);
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			EffectUtil.playSound(player, Sound.CHEST_OPEN);
			openMenu(player);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		final Player player = event.getPlayer();
		ItemStack item = event.getItem();
		String title = event.getTitle();

		if(title.equals(ChatColor.stripColor(getName()))) {
			if(item.getType() == Material.DIAMOND_BOOTS) {
				ParkourNPC.openParkourInventory(player);
			} else {
				for(Plugins plugin : Plugins.values()) {
					if(itemSlots.containsKey(plugin) && itemSlots.get(plugin) == event.getSlot()) {
						if(onlyServer.containsKey(plugin)) {
							ProPlugin.sendPlayerToServer(player, onlyServer.get(plugin));
						} else {
							open(player, plugin);
						}
						break;
					}
				}
			}

			event.setCancelled(true);
		} else if(watching.containsKey(player.getName())) {
			if(item.getType() == Material.WOOD_DOOR) {
				openMenu(player);
			} else {
				ProPlugin.sendPlayerToServer(player, ChatColor.stripColor(event.getItemTitle()));
			}

			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			for(Plugins plugin : Plugins.values()) {
				update(plugin);
			}
		}
	}
	
	public static void open(Player player, Plugins plugin) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 6, plugin.getDisplay());
		if(plugin != Plugins.HUB) {
			inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
		}
		player.openInventory(inventory);
		watching.put(player.getName(), plugin);
		update(plugin);
	}

	private static void update(Plugins plugin) {
		new AsyncDelayedTask(() -> {
			if(!Databases.NETWORK.isEnabled()) {
				return;
			}

			ResultSet resultSet = null;
			try {
				List<Integer> priorities = new ArrayList<>();
				List<Integer> serverNumbers = new ArrayList<>();
				List<String> lores = new ArrayList<>();
				List<Integer> playerCounts = new ArrayList<>();
				List<Integer> maxPlayers = new ArrayList<>();
				int limit = 9 * 4;
				resultSet = Databases.NETWORK.getConnection().prepareStatement("SELECT * FROM server_status WHERE game_name = '" + plugin.toString() + "' ORDER BY listed_priority, players DESC, server_number LIMIT " + limit).executeQuery();

				int totalPlayers = 0;

				while(resultSet.next()) {
					priorities.add(resultSet.getInt("listed_priority"));
					serverNumbers.add(resultSet.getInt("server_number"));
					lores.add(resultSet.getString("lore"));
					int playerCount = resultSet.getInt("players");
					playerCounts.add(playerCount);
					maxPlayers.add(resultSet.getInt("max_players"));
					totalPlayers += playerCount;
				}

				players.put(plugin, totalPlayers);
				if(serverNumbers.size() == 1) {
					String server = plugin.getServer() + serverNumbers.get(0);
					onlyServer.put(plugin, server.toLowerCase());
				} else {
					onlyServer.remove(plugin);
				}

				for(String name : watching.keySet()) {
					if(watching.get(name) == plugin) {
						Player player = ProPlugin.getPlayer(name);

						if(player != null) {
							InventoryView inventoryView = player.getOpenInventory();
							String title = inventoryView.getTitle();

							if(title != null && title.equals(plugin.getDisplay())) {
								for(int a = 0; a < serverNumbers.size() && a < inventoryView.getTopInventory().getSize(); ++a) {
									int serverNumber = serverNumbers.get(a);
									String server = "&b" + plugin.getServer() + serverNumber;
									byte data = getWoolColor(priorities.get(a));

									if(plugin == Plugins.HUB) {
										if(HubBase.getHubNumber() == serverNumber) {
											data = (byte) 3;
										} else {
											data = (byte) 5;
										}
									}

									int currentPlayers = playerCounts.get(a);
									int maxPlayerCount = maxPlayers.get(a);
									int percentage = (int) (currentPlayers * 100.0 / maxPlayerCount + 0.5);
									String [] lore;

									if(plugin == Plugins.HUB) {
										lore = new String [] {
											"",
											data == 3 ? "&7You are on this hub" : "&7Click to join &eHub #" + serverNumber,
											"",
											"&e" + currentPlayers + "&8/&e" + maxPlayerCount + " &7(&e" + percentage + "% Full&7)",
											""
										};
									} else {
										 lore = new String [] {
											"",
											"&7Click to play &e" + plugin.getDisplay() + "&7!",
											"",
											"&e" + currentPlayers + "&8/&e" + maxPlayerCount + " &7(&e" + percentage + "% Full&7)",
											""
										};
									}

									inventoryView.setItem(a, new ItemCreator(Material.WOOL, data).setAmount(serverNumber).setName(server).setLores(lore).getItemStack());
								}

								for(int a = serverNumbers.size(); a < (plugin == Plugins.HUB ? ItemUtil.getInventorySize(ProPlugin.getNumberOfHubs()) : 9); ++a) {
									if(inventoryView.getItem(a).getType() == Material.WOOL) {
										inventoryView.setItem(a, new ItemStack(Material.AIR));
									}
								}
							}
						}
					}
				}
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				if(resultSet != null) {
					try {
						resultSet.close();
					} catch(SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getInventory().getTitle().equals(ChatColor.stripColor(getName()))) {
			watching.remove(event.getPlayer().getName());
		} else if(event.getInventory().getTitle().equals(name)) {
			inMenu.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		watching.remove(event.getPlayer().getName());
		inMenu.remove(event.getPlayer().getName());
	}
	
	private void openMenu(Player player) {
		Inventory inventory = Bukkit.createInventory(player, size, ChatColor.stripColor(getName()));

		Plugins plugin = Plugins.KITPVP;
		int playerCount = players.getOrDefault(plugin, 0);
		ItemStack item = new ItemCreator(Material.IRON_SWORD).setName("&b" + plugin.getDisplay()).setLores(new String [] {
				"&ePlayers: &a" + playerCount,
				"",
				"&eFFA Kit PVP with customizable kits",
				""
		}).getItemStack();
		itemSlots.put(plugin, 10);
		inventory.setItem(itemSlots.get(plugin), item);

		plugin = Plugins.ONEVSONE;
		playerCount = players.getOrDefault(plugin, 0);
		item = new ItemCreator(Material.FISHING_ROD).setName("&b" + plugin.getDisplay()).setLores(new String [] {
				"&ePlayers: &a" + playerCount,
				"",
				"&eTest your competitive PVP skills",
				"&eagainst other players",
				""
		}).getItemStack();
		itemSlots.put(plugin, 12);
		inventory.setItem(itemSlots.get(plugin), item);

		plugin = Plugins.UHC;
		playerCount = players.getOrDefault(plugin, 0);
		item = new ItemCreator(Material.GOLDEN_APPLE).setName("&bHosted UHC").setLores(new String [] {
				"&ePlayers: &a" + playerCount,
				"",
				"&eGames are announced here:",
				"&b/discord",
				"&b/socialMedia",
				"",
				""
		}).getItemStack();
		itemSlots.put(plugin, 14);
		inventory.setItem(14, item);

		item = new ItemCreator(Material.DIAMOND_BOOTS).setName("&bParkour").setLores(new String [] {
				"&7Our Unique Parkour Course",
				"",
				"&aEndless Parkour",
				"&eParkour forever, try to",
				"&ebeat your high score!",
				"",
				"&aParkour Course",
				"&eParkour on our custom course",
				"&eWatch out for obstacles though",
				""
		}).getItemStack();
		inventory.setItem(16, item);

		ItemUtil.displayGameGlass(inventory);
		player.openInventory(inventory);
	}
	
	private static byte getWoolColor(int priority) {
		return (byte) (priority == 1 ? 4 : priority == 2 ? 5 : priority == 3 ? 14 : 0);
	}
}
