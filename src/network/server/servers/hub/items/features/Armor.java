package network.server.servers.hub.items.features;

import network.customevents.TimeEvent;
import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.DB;
import network.server.servers.hub.items.Features;
import network.server.servers.hub.items.Features.Rarity;
import network.server.servers.hub.items.features.FeatureItem.FeatureType;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.ItemCreator;
import network.server.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings("deprecation")
public class Armor extends FeatureBase {
	private static int max = 20;
	private static Map<String, Integer> owned = null;
	private static List<UUID> queue = null;
	private static List<String> settingsChanged = null;
	
	public enum PlayerArmor {
		// Helmets
		DIAMOND_HELMET(0, "Diamond Helmet", Rarity.RARE, Material.DIAMOND_HELMET),
		IRON_HELMET(1, "Iron Helmet", Rarity.RARE, Material.IRON_HELMET),
		CHAIN_HELMET(2, "Chain Helmet", Rarity.UNCOMMON, Material.CHAINMAIL_HELMET),
		GOLD_HELMET(3, "Gold Helmet", Rarity.UNCOMMON, Material.GOLD_HELMET),
		LEATHER_HELMET(4, "Leather Helmet", Rarity.COMMON, Material.LEATHER_HELMET),
		NO_HELMET(8, "&cRemove Helmet", Rarity.COMMON, Material.WEB, false),
		
		// Chestplates
		DIAMOND_CHESTPLATE(9, "Diamond Chestplate", Rarity.RARE, Material.DIAMOND_CHESTPLATE),
		IRON_CHESTPLATE(10, "Iron Chestplate", Rarity.RARE, Material.IRON_CHESTPLATE),
		CHAIN_CHESTPLATE(11, "Chain Chestplate", Rarity.UNCOMMON, Material.CHAINMAIL_CHESTPLATE),
		GOLD_CHESTPLATE(12, "Gold Chestplate", Rarity.UNCOMMON, Material.GOLD_CHESTPLATE),
		LEATHER_CHESTPLATE(13, "Leather Chestplate", Rarity.COMMON, Material.LEATHER_CHESTPLATE),
		NO_CHESTPLATE(17, "&cRemove Chestplate", Rarity.COMMON, Material.WEB, false),
		
		// Leggings
		DIAMOND_LEGGINGS(18, "Diamond Leggings", Rarity.RARE, Material.DIAMOND_LEGGINGS),
		IRON_LEGGINGS(19, "Iron Leggings", Rarity.RARE, Material.IRON_LEGGINGS),
		CHAIN_LEGGINGS(20, "Chain Leggings", Rarity.UNCOMMON, Material.CHAINMAIL_LEGGINGS),
		GOLD_LEGGINGS(21, "Gold Leggings", Rarity.UNCOMMON, Material.GOLD_LEGGINGS),
		LEATHER_LEGGINGS(22, "Leather Leggings", Rarity.COMMON, Material.LEATHER_LEGGINGS),
		NO_LEGGINGS(26, "&cRemove Leggings", Rarity.COMMON, Material.WEB, false),
		
		// Boots
		DIAMOND_BOOTS(27, "Diamond Boots", Rarity.RARE, Material.DIAMOND_BOOTS),
		IRON_BOOTS(28, "Iron Boots", Rarity.RARE, Material.IRON_BOOTS),
		CHAIN_BOOTS(29, "Chain Boots", Rarity.UNCOMMON, Material.CHAINMAIL_BOOTS),
		GOLD_BOOTS(30, "Gold Boots", Rarity.UNCOMMON, Material.GOLD_BOOTS),
		LEATHER_BOOTS(31, "Leather Boots", Rarity.COMMON, Material.LEATHER_BOOTS),
		NO_BOOTS(35, "&cRemove Boots", Rarity.COMMON, Material.WEB, false),
		
		;
		
		private int slot = 0;
		private String name = null;
		private ItemStack itemStack = null;
		private boolean store = true;
		private Rarity rarity = Rarity.COMMON;
		
		private PlayerArmor(int slot, String name, Rarity rarity, Material material) {
			this(slot, name, rarity, new ItemStack(material));
		}
		
		private PlayerArmor(int slot, String name, Rarity rarity, ItemStack itemStack) {
			this(slot, name, rarity, itemStack, true);
		}
		
		private PlayerArmor(int slot, String name, Rarity rarity, Material material, boolean store) {
			this(slot, name, rarity, new ItemStack(material), store);
		}
		
		private PlayerArmor(int slot, String name, Rarity rarity, ItemStack itemStack, boolean store) {
			this.slot = slot;
			this.name = name;
			this.rarity = rarity;
			this.itemStack = itemStack;
			this.store = store;
			if(store) {
				new FeatureItem(getName(), getItemStack(), getRarity(), FeatureType.REWARD_CRATE);
			}
		}
		
		public int getSlot() {
			return this.slot;
		}
		
		public String getName() {
			return this.name;
		}
		
		public ItemStack getItemStack() {
			return this.itemStack;
		}
		
		public Rarity getRarity() {
			return this.rarity;
		}
		
		public boolean owns(Player player) {
			InventoryView inventoryView = opened(player);
			if(inventoryView != null) {
				return store && inventoryView.getItem(getSlot()).getType() != Material.INK_SACK;
			}
			return store && DB.HUB_ARMOR.isKeySet(new String [] {"uuid", "name"}, new String [] {player.getUniqueId().toString(), toString()});
		}
		
		public void give(Player player) {
			if(store) {
				String [] keys = new String [] {"uuid", "name"};
				String [] values = new String [] {player.getUniqueId().toString(), toString()};
				if(owns(player) || DB.HUB_ARMOR.isKeySet(keys, values)) {
					int owned = DB.HUB_ARMOR.getInt(keys, values, "amount_owned");
					DB.HUB_ARMOR.updateInt("amount_owned", owned + 1, keys, values);
					Bukkit.getLogger().info("armor: give more");
				} else {
					String time = TimeUtil.getTime().substring(0, 10);
					DB.HUB_ARMOR.insert("'" + player.getUniqueId().toString() + "', '" + toString() + "', '" + getType() + "', '0', '1', '" + time + "'");
					Bukkit.getLogger().info("armor: give");
				}
				owned.remove(player.getName());
			}
		}
		
		public ItemStack getItem(Player player, String action) {
			ItemCreator item = null;
			boolean own = owns(player);
			if(!store || own) {
				item = new ItemCreator(getItemStack()).setName("&b" + getName());
				if(own) {
					String [] keys = new String [] {"uuid", "name"};
					String [] values = new String [] {player.getUniqueId().toString(), toString()};
					int owned = DB.HUB_ARMOR.getInt(keys, values, "amount_owned");
					item.setLores(new String [] {
						"",
						"&7Status: &eUnlocked",
						"&7You own &e" + owned + " &7of these",
						"&7Unlocked on &e" + DB.HUB_ARMOR.getString(keys, values, "unlocked_time"),
						"&7Rarity: &e" + getRarity().getName(),
						""
					});
					Bukkit.getLogger().info("armor: getItem");
				}
			} else {
				item = new ItemCreator(new ItemStack(Material.INK_SACK, 1, (byte) 8)).setName("&b" + getName());
				item.setLores(new String [] {
					"",
					"&7Status: &cLocked",
					"&7Unlock in: &e" + action,
					"&7Rarity: &e" + getRarity().getName(),
					"",
				});
			}
			return item.getItemStack();
		}
		
		public void decrenentAmount(Player player) {
			String [] keys = new String [] {"uuid", "name"};
			String [] values = new String [] {player.getUniqueId().toString(), toString()};
			int amount = DB.HUB_ARMOR.getInt(keys, values, "amount_owned") - 1;
			if(amount <= 0) {
				DB.HUB_ARMOR.delete(keys, values);
			} else {
				DB.HUB_ARMOR.updateInt("amount_owned", amount, keys, values);
			}
			owned.remove(player.getName());
		}
		
		public String getType() {
			return toString().split("_")[1];
		}
		
		public void equipArmor(Player player) {
			String type = getType();
			if(type.equals("HELMET")) {
				player.getInventory().setHelmet(this.itemStack);
			} else if(type.equals("CHESTPLATE")) {
				player.getInventory().setChestplate(this.itemStack);
			} else if(type.equals("LEGGINGS")) {
				player.getInventory().setLeggings(this.itemStack);
			} else if(type.equals("BOOTS")) {
				player.getInventory().setBoots(this.itemStack);
			}
		}
	}
	
	public Armor() {
		super(getInvName(), 14, new ItemStack(Material.IRON_CHESTPLATE), null, new String [] {
			"",
			"&7Wear the coolest armor around!",
			"",
			"&7Owned: &eXX&8/&e" + max + " &7(&eYY%&7)",
			"&7Collect from: &eZZ",
			""
		});
		owned = new HashMap<String, Integer>();
		queue = new ArrayList<UUID>();
		settingsChanged = new ArrayList<String>();
		PlayerArmor.values();
	}
	
	private static String getInvName() {
		return "Armor";
	}
	
	private static InventoryView opened(Player player) {
		InventoryView inventoryView = player.getOpenInventory();
		return inventoryView != null && inventoryView.getTitle().equals(getInvName()) ? inventoryView : null;
	}
	
	private PlayerArmor getPlayerArmor(int slot) {
		for(PlayerArmor armor : PlayerArmor.values()) {
			if(armor.getSlot() == slot) {
				return armor;
			}
		}
		return null;
	}
	
	private PlayerArmor getPlayerArmor(ItemStack itemStack) {
		if(itemStack != null) {
			for(PlayerArmor armor : PlayerArmor.values()) {
				if(armor.getItemStack().getType() == itemStack.getType() && armor.getItemStack().getData().getData() == itemStack.getData().getData()) {
					return armor;
				}
			}
		}
		return null;
	}
	
	private String getSetting() {
		return "load_armor_on_join";
	}
	
	private ItemStack getSettingItem(Player player) {
		return getSettingItem(player, -1);
	}
	
	private ItemStack getSettingItem(Player player, int state) {
		if(state == -1) {
			state = DB.PLAYERS_SETTINGS.getInt(new String [] {"uuid", "setting"}, new String [] {player.getUniqueId().toString(), getSetting()}, "state");
			Bukkit.getLogger().info("armor: getSettingItem");
		}
		ItemCreator loadItem = null;
		if(state == 1) {
			loadItem = new ItemCreator(Material.EMERALD_BLOCK).setName("&bLoad Armor on Join: &eOn");
		} else {
			loadItem = new ItemCreator(Material.REDSTONE_BLOCK).setName("&bLoad Armor on Join: &cOff");
		}
		loadItem.setLores(new String [] {
			"",
			"&7This toggles wheather or not your",
			"&7armor will load when you join a hub",
			"",
			"&7This is for " + Ranks.VIP.getPrefix() + "&7rank and above only to",
			"&7prevent lag and improve server performance",
			"",
		});
		return loadItem.getItemStack();
	}
	
	private void saveSetting(Player player) {
		if(settingsChanged.contains(player.getName())) {
			settingsChanged.remove(player.getName());
			final UUID uuid = player.getUniqueId();
			final int state = player.getOpenInventory().getItem(player.getOpenInventory().getTopInventory().getSize() - 9).getType() == Material.EMERALD_BLOCK ? 1 : 0;
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "setting"};
					String [] values = new String [] {uuid.toString(), getSetting()};
					if(DB.PLAYERS_SETTINGS.isKeySet(keys, values)) {
						if(state == 0) {
							DB.PLAYERS_SETTINGS.delete(keys, values);
						} else {
							DB.PLAYERS_SETTINGS.updateInt("state", state, keys, values);
						}
					} else if(state > 0) {
						DB.PLAYERS_SETTINGS.insert("'" + uuid.toString() + "', '" + getSetting() + "', '" + state + "'");
					}
					Bukkit.getLogger().info("armor: save settings");
				}
			});
		}
	}
	
	private void select(Player player) {
		final UUID uuid = player.getUniqueId();
		for(ItemStack itemStack : player.getInventory().getArmorContents()) {
			final PlayerArmor armor = getPlayerArmor(itemStack);
			if(armor != null) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String armorType = armor.getType();
						DB.HUB_ARMOR.updateInt("active", 0, new String [] {"uuid", "type"}, new String [] {uuid.toString(), armorType});
						DB.HUB_ARMOR.updateInt("active", 1, new String [] {"uuid", "name"}, new String [] {uuid.toString(), armor.toString()});
						Bukkit.getLogger().info("armor: selected");
					}
				});
			}
		}
		saveSetting(player);
	}
	
	@Override
	public int getOwned(Player player) {
		if(!owned.containsKey(player.getName())) {
			owned.put(player.getName(), DB.HUB_ARMOR.getSize("uuid", player.getUniqueId().toString()));
			Bukkit.getLogger().info("armor: getOwned");
		}
		return owned.get(player.getName());
	}
	
	@Override
	public int getMax() {
		return max;
	}
	
	@Override
	public void display(final Player player) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Inventory inventory = Bukkit.createInventory(player, 9 * 6, getName());
				for(PlayerArmor armor : PlayerArmor.values()) {
					inventory.setItem(armor.getSlot(), armor.getItem(player, getAction()));
				}
				inventory.setItem(inventory.getSize() - 9, getSettingItem(player));
				inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&cBack").getItemStack());
				player.openInventory(inventory);
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getInvName())) {
			Player player = event.getPlayer();
			UUID uuid = player.getUniqueId();
			ItemStack item = event.getItem();
			if(item.getType() == Material.EMERALD_BLOCK || item.getType() == Material.REDSTONE_BLOCK) {
				if(Ranks.VIP.hasRank(player)) {
					int newState = item.getType() == Material.EMERALD_BLOCK ? 0 : 1;
					player.getOpenInventory().setItem(player.getOpenInventory().getTopInventory().getSize() - 9, getSettingItem(player, newState));
					if(!settingsChanged.contains(player.getName())) {
						settingsChanged.add(player.getName());
					}
				} else {
					MessageHandler.sendMessage(player, Ranks.VIP.getNoPermission());
				}
			} else if(item.getType() == Material.INK_SACK && item.getData().getData() == 8) {
				displayLocked(player);
			} else if(item.getType() == Material.WOOD_DOOR) {
				Features.open(player);
			} else {
				String type = item.getType().toString().toUpperCase();
				String name = ChatColor.stripColor(event.getItemTitle());
				if(item.getType() == Material.WEB) {
					boolean update = false;
					if(name.contains("Helmet")) {
						if(player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() != Material.AIR) {
							player.getInventory().setHelmet(new ItemStack(Material.AIR));
							update = true;
						}
					} else if(name.contains("Chestplate")) {
						if(player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() != Material.AIR) {
							player.getInventory().setChestplate(new ItemStack(Material.AIR));
							update = true;
						}
					} else if(name.contains("Leggings")) {
						if(player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() != Material.AIR) {
							player.getInventory().setLeggings(new ItemStack(Material.AIR));
							update = true;
						}
					} else if(name.contains("Boots")) {
						if(player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() != Material.AIR) {
							player.getInventory().setBoots(new ItemStack(Material.AIR));
							update = true;
						}
					}
					final PlayerArmor armor = getPlayerArmor(event.getSlot());
					if(armor != null && update) {
						player.updateInventory();
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								String armorType = armor.getType();
								DB.HUB_ARMOR.updateInt("active", 0, new String [] {"uuid", "type"}, new String [] {uuid.toString(), armorType});
								Bukkit.getLogger().info("armor: set none");
							}
						});
						MessageHandler.sendMessage(player, "You have set " + armor.getName());
					}
				} else {
					boolean update = false;
					if(type.contains("HELMET")) {
						if(player.getInventory().getHelmet() == null || player.getInventory().getHelmet().getType() != item.getType()) {
							player.getInventory().setHelmet(item);
							update = true;
						}
					} else if(type.contains("CHESTPLATE")) {
						if(player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType() != item.getType()) {
							player.getInventory().setChestplate(item);
							update = true;
						}
					} else if(type.contains("LEGGINGS")) {
						if(player.getInventory().getLeggings() == null || player.getInventory().getLeggings().getType() != item.getType()) {
							player.getInventory().setLeggings(item);
							update = true;
						}
					} else if(type.contains("BOOTS")) {
						if(player.getInventory().getBoots() == null || player.getInventory().getBoots().getType() != item.getType()) {
							player.getInventory().setBoots(item);
							update = true;
						}
					}
					final PlayerArmor armor = getPlayerArmor(event.getSlot());
					if(armor != null && update) {
						player.updateInventory();
						MessageHandler.sendMessage(player, "You have selected &e" + ChatColor.stripColor(armor.getName()));
					}
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if(Ranks.VIP.hasRank(player) && DB.PLAYERS_SETTINGS.isKeySet(new String [] {"uuid", "setting", "state"}, new String [] {uuid.toString(), getSetting(), "1"})) {
			Bukkit.getLogger().info("armor: queue");
			queue.add(uuid);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 10) {
			if(queue != null && !queue.isEmpty()) {
				final UUID uuid = queue.get(0);
				queue.remove(0);
				Player player = Bukkit.getPlayer(uuid);
				if(player != null) {
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							for(String armorName : DB.HUB_ARMOR.getAllStrings("name", new String [] {"uuid", "active"}, new String [] {uuid.toString(), "1"})) {
								Bukkit.getLogger().info("armor: " + player.getName() + " queue");
								PlayerArmor armor = PlayerArmor.valueOf(armorName);
								if(armor == null) {
									DB.HUB_ARMOR.delete(new String [] {"uuid", "name"}, new String [] {uuid.toString(), armorName});
								} else {
									armor.equipArmor(player);
								}
							}
						}
					});
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getPlayer() instanceof Player && event.getInventory().getTitle().equals(getName())) {
			Player player = (Player) event.getPlayer();
			select(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		owned.remove(event.getPlayer().getName());
		queue.remove(event.getPlayer().getUniqueId());
		select(event.getPlayer());
	}
}
