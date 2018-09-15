package network.server.servers.hub.items.features.pets;

import network.customevents.TimeEvent;
import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.DB;
import network.server.servers.hub.items.Features;
import network.server.servers.hub.items.Features.Rarity;
import network.server.servers.hub.items.features.FeatureBase;
import network.server.servers.hub.items.features.FeatureItem;
import network.server.servers.hub.items.features.FeatureItem.FeatureType;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.ItemCreator;
import network.server.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings("deprecation")
public class Pets extends FeatureBase {
	private static int max = 42;
	private static Map<String, Integer> owned = null;
	private static Map<String, Integer> pages = null;
	private static Map<String, PetTypes> pets = null;
	private static List<UUID> queue = null;
	private static List<String> settingsChanged = null;
	
	public enum PetTypes {
		// 1st page
		CHICKEN(10, 1, "Chicken Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 93)),
		COW(11, 1, "Cow Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 92)),
		PIG(12, 1, "Pig Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 90)),
		MUSHROOM_COW(13, 1, "Mushroom Cow Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 96)),
		WOLF(14, 1, "Wolf Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 95)),
		MAGMA_CUBE(15, 1, "Magma Cube Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 62)),
		SLIME(16, 1, "Slime Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 55)),
		OCELOT(19, 1, "Ocelot Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 98)),
		RED_OCELOT(20, 1, "Red Ocelot Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 92)),
		BLACK_OCELOT(21, 1, "Black Ocelot Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 92)),
		SIAMESE_OCELOT(22, 1, "Siamese Ocelot Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 92)),
		BLAZE(23, 1, "Blaze Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 61)),
		SHEEP(24, 1, "Cow Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 91)),
		RAINBOW_SHEEP(25, 1, "Rainbow Sheep Pet", Rarity.RARE, Material.WOOL),
		CREEPER(28, 1, "Creeper Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 50)),
		POWERED_CREEPER(29, 1, "Powered Creeper Pet", Rarity.RARE, new ItemStack(Material.MONSTER_EGG, 1, (byte) 50)),
		SILVERFISH(30, 1, "Silverfish Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 60)),
		SKELETON(31, 1, "Skeleton Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 51)),
		WITCH(32, 1, "Witch Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 66)),
		BAT(33, 1, "Bat Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 65)),
		ENDERMAN(34, 1, "Enderman Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 58)),
		
		// 2nd page
		SPIDER(10, 2, "Spider Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 52)),
		CAVE_SPIDER(11, 2, "Cave Spider Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 59)),
		HORSE(12, 2, "Horse Pet", Rarity.RARE, new ItemStack(Material.MONSTER_EGG, 1, (byte) 100)),
		ZOMBIE_HORSE(13, 2, "Zombie Horse Pet", Rarity.RARE, new ItemStack(Material.SKULL_ITEM, 1, (byte) 2)),
		SKELETON_HORSE(14, 2, "Skeleton Horse Pet", Rarity.RARE, new ItemStack(Material.SKULL_ITEM, 1, (byte) 0)),
		BLACKSMITH_VILLAGER(15, 2, "Blacksmith Villager Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 120)),
		BUTCHER_VILLAGER(16, 2, "Butcher Villager Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 120)),
		FARMER_VILLAGER(19, 2, "Farmer Villager Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 120)),
		LIBRARIAN_VILLAGER(20, 2, "Librarian Villager Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 120)),
		PRIEST_VILLAGER(21, 2, "Priest Villager Pet", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 120)),
		ZOMBIE_PIGAN(22, 2, "Zombie Pigman Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 57)),
		ZOMBIE(23, 2, "Zombie Pet", Rarity.UNCOMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 54)),
		
		;
		
		private int slot = 0;
		private int page = 1;
		private String name = null;
		private ItemStack itemStack = null;
		private boolean store = true;
		private Rarity rarity = Rarity.COMMON;
		
		private PetTypes(int slot, int page, String name, Rarity rarity, Material material) {
			this(slot, page, name, rarity, new ItemStack(material));
		}
		
		private PetTypes(int slot, int page, String name, Rarity rarity, ItemStack itemStack) {
			this(slot, page, name, rarity, itemStack, true);
		}
		
		private PetTypes(int slot, int page, String name, Rarity rarity, Material material, boolean store) {
			this(slot, page, name, rarity, new ItemStack(material), store);
		}
		
		private PetTypes(int slot, int page, String name, Rarity rarity, ItemStack itemStack, boolean store) {
			this.slot = slot;
			this.page = page;
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
		
		public int getPage() {
			return this.page;
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
			return store && DB.HUB_PETS.isKeySet(new String [] {"uuid", "name"}, new String [] {player.getUniqueId().toString(), toString()});
		}
		
		public void give(Player player) {
			if(store) {
				String [] keys = new String [] {"uuid", "name"};
				String [] values = new String [] {player.getUniqueId().toString(), toString()};
				if(owns(player) || DB.HUB_PETS.isKeySet(keys, values)) {
					int owned = DB.HUB_PETS.getInt(keys, values, "amount_owned");
					DB.HUB_PETS.updateInt("amount_owned", owned + 1, keys, values);
					Bukkit.getLogger().info("pets: give more");
				} else {
					String time = TimeUtil.getTime().substring(0, 10);
					DB.HUB_PETS.insert("'" + player.getUniqueId().toString() + "', '" + toString() + "', 'none', '0', '1', '" + time + "'");
					Bukkit.getLogger().info("pets: give");
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
					int owned = DB.HUB_PETS.getInt(keys, values, "amount_owned");
					item.setLores(new String [] {
						"",
						"&7Status: &eUnlocked",
						"&7You own &e" + owned + " &7of these",
						"&7Unlocked on &e" + DB.HUB_PETS.getString(keys, values, "unlocked_time"),
						"&7Rarity: &e" + getRarity().getName(),
						""
					});
					Bukkit.getLogger().info("pets: getItem");
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
			int amount = DB.HUB_PETS.getInt(keys, values, "amount_owned") - 1;
			if(amount <= 0) {
				DB.HUB_PETS.delete(keys, values);
			} else {
				DB.HUB_PETS.updateInt("amount_owned", amount, keys, values);
			}
			owned.remove(player.getName());
		}
	}
	
	public Pets() {
		super("Pets", 12, new ItemStack(Material.BONE), null, new String [] {
			"",
			"&7Love pets? We have one of",
			"&7the largest collections around!",
			"",
			"&7Owned: &eXX&8/&e" + max + " &7(&eYY%&7)",
			"&7Collect from: &eZZ",
			""
		});
		owned = new HashMap<String, Integer>();
		pages = new HashMap<String, Integer>();
		pets = new HashMap<String, PetTypes>();
		queue = new ArrayList<UUID>();
		settingsChanged = new ArrayList<String>();
		PetTypes.values();
	}
	
	private static String getInvName() {
		return "Pets";
	}
	
	private static InventoryView opened(Player player) {
		InventoryView inventoryView = player.getOpenInventory();
		return inventoryView != null && inventoryView.getTitle().equals(getInvName()) ? inventoryView : null;
	}
	
	private String getSetting() {
		return "load_pet_on_join";
	}
	
	private ItemStack getSettingItem(Player player) {
		return getSettingItem(player, -1);
	}
	
	private ItemStack getSettingItem(Player player, int state) {
		if(state == -1) {
			state = DB.PLAYERS_SETTINGS.getInt(new String [] {"uuid", "setting"}, new String [] {player.getUniqueId().toString(), getSetting()}, "state");
			Bukkit.getLogger().info("pets: getSettingItem");
		}
		ItemCreator loadItem = null;
		if(state == 1) {
			loadItem = new ItemCreator(Material.EMERALD_BLOCK).setName("&bLoad Pet on Join: &eOn");
		} else {
			loadItem = new ItemCreator(Material.REDSTONE_BLOCK).setName("&bLoad Pet on Join: &cOff");
		}
		loadItem.setLores(new String [] {
			"",
			"&7This toggles wheather or not your",
			"&7pet will load when you join a hub",
			"",
			"&7This is for " + Ranks.PRO.getPrefix() + "&7rank and above only to",
			"&7prevent lag and improve server performance",
			"",
		});
		return loadItem.getItemStack();
	}
	
	private void saveSetting(Player player) {
		if(settingsChanged.contains(player.getName())) {
			settingsChanged.remove(player.getName());
			final UUID uuid = player.getUniqueId();
			final int state = player.getOpenInventory().getItem(player.getOpenInventory().getTopInventory().getSize() - 7).getType() == Material.EMERALD_BLOCK ? 1 : 0;
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
					Bukkit.getLogger().info("pets: save settings");
				}
			});
		}
	}
	
	private void select(Player player) {
		final UUID uuid = player.getUniqueId();
		final PetTypes type = pets.get(player.getName());
		if(type != null) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					DB.HUB_PETS.updateInt("active", 0, "uuid", uuid.toString());
					DB.HUB_PETS.updateInt("active", 1, new String [] {"uuid", "name"}, new String [] {uuid.toString(), type.toString()});
					Bukkit.getLogger().info("pets: selected");
				}
			});
		}
		saveSetting(player);
	}
	
	@Override
	public int getOwned(Player player) {
		if(!owned.containsKey(player.getName())) {
			owned.put(player.getName(), DB.HUB_PETS.getSize("uuid", player.getUniqueId().toString()));
			Bukkit.getLogger().info("pets: getOwned");
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
				int page = 1;
				if(pages.containsKey(player.getName())) {
					page = pages.get(player.getName());
				} else {
					pages.put(player.getName(), page);
				}
				if(page > 1) {
					inventory.setItem(0, new ItemCreator(Material.ARROW).setName("&bPage #" + (page - 1)).getItemStack());
				}
				if(page < 2) {
					inventory.setItem(8, new ItemCreator(Material.ARROW).setName("&bPage #" + (page + 1)).getItemStack());
				}
				for(PetTypes type : PetTypes.values()) {
					if(type.getPage() == page) {
						inventory.setItem(type.getSlot(), type.getItem(player, "Reward Crates"));
					}
				}
				inventory.setItem(inventory.getSize() - 7, getSettingItem(player));
				inventory.setItem(inventory.getSize() - 3, new ItemCreator(Material.WOOD_DOOR).setName("&cBack").getItemStack());
				player.openInventory(inventory);
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getInvName())) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			ItemStack item = event.getItem();
			if(item.getType() == Material.EMERALD_BLOCK || item.getType() == Material.REDSTONE_BLOCK) {
				if(Ranks.PRO.hasRank(player)) {
					int newState = item.getType() == Material.EMERALD_BLOCK ? 0 : 1;
					player.getOpenInventory().setItem(player.getOpenInventory().getTopInventory().getSize() - 7, getSettingItem(player, newState));
					if(!settingsChanged.contains(player.getName())) {
						settingsChanged.add(player.getName());
					}
				} else {
					MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
				}
			} else if(item.getType() == Material.INK_SACK && item.getData().getData() == 8) {
				displayLocked(player);
			} else if(item.getType() == Material.WOOD_DOOR) {
				Features.open(player);
			} else if(item.getType() == Material.ARROW) {
				player.closeInventory();
				int page = 1;
				if(pages.containsKey(player.getName())) {
					page = pages.get(player.getName());
				}
				if(event.getSlot() == 8) {
					pages.put(player.getName(), ++page);
				}
				if(event.getSlot() == 0) {
					pages.put(player.getName(), --page);
				}
				display(player);
			} else {
				player.closeInventory();
				MessageHandler.sendMessage(player, "&cPets are still under development. Your unlocked pets will still be there once they are released!");
				//String type = item.getType().toString().toUpperCase();
				//String name = ChatColor.stripColor(event.getItemTitle());
				/*if(item.getType() == Material.BARRIER) {
					boolean update = false;
					final PetTypes petType = pets.get(player.getDisplay());
					if(petType != null && update) {
						player.updateInventory();
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								DB.HUB_PETS.updateInt("active", 0, new String [] {"uuid", "name"}, new String [] {uuid.toString(), petType.getDisplay()});
								Bukkit.getLogger().info("pets: set none");
							}
						});
						MessageHandler.sendMessage(player, "You have set " + petType.getDisplay());
					}
				} else {
					boolean update = false;
					final PetTypes petType = pets.get(player.getDisplay());
					if(petType != null && update) {
						player.updateInventory();
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								DB.HUB_PETS.updateInt("active", 1, new String [] {"uuid", "name"}, new String [] {uuid.toString(), petType.getDisplay()});
								Bukkit.getLogger().info("pets: selected");
							}
						});
						MessageHandler.sendMessage(player, "You have selected &e" + ChatColor.stripColor(petType.getDisplay()));
					}
				}*/
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if(Ranks.PRO.hasRank(player) && DB.PLAYERS_SETTINGS.isKeySet(new String [] {"uuid", "setting", "state"}, new String [] {uuid.toString(), getSetting(), "1"})) {
			Bukkit.getLogger().info("pets: queue");
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
							for(String petName : DB.HUB_PETS.getAllStrings("name", new String [] {"uuid", "active"}, new String [] {uuid.toString(), "1"})) {
								Bukkit.getLogger().info("pets: " + player.getName() + " queue");
								PetTypes type = PetTypes.valueOf(petName);
								if(type == null) {
									DB.HUB_PETS.delete(new String [] {"uuid", "name"}, new String [] {uuid.toString(), petName});
								} else {
									//TODO: Spawn
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
