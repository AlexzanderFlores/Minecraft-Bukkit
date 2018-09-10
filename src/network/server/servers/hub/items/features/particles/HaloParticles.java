package network.server.servers.hub.items.features.particles;

import de.slikey.effectlib.util.ParticleEffect;
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
import network.server.util.CircleUtil;
import network.server.util.ItemCreator;
import network.server.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings("deprecation")
public class HaloParticles extends FeatureBase {
	private static int max = 21;
	private static Map<String, Integer> owned = null;
	private static Map<String, HaloParticleTypes> selected = null;
	private static Map<String, CircleUtil> circles = null;
	private static List<UUID> queue = null;
	private static List<String> settingsChanged = null;
	private static Random random = null;
	
	public enum HaloParticleTypes {
		CRIT(1, "Crit Halo Particles", Rarity.COMMON, Material.DIAMOND_SWORD),
		CRIT_MAGIC(2, "Magic Crit Halo Particles", Rarity.COMMON, new ItemCreator(Material.DIAMOND_SWORD).setGlow(true).getItemStack()),
		SMOKE_NORMAL(3, "Smoke Halo Particles", Rarity.COMMON, Material.WEB),
		SPELL(4, "Spell Halo Particles", Rarity.COMMON, new Potion(PotionType.REGEN, 1, true).toItemStack(1)),
		SPELL_INSTANT(5, "Instant Spell Halo Particles", Rarity.COMMON, Material.BEACON),
		SPELL_MOB(6, "Mob Spell Halo Particles", Rarity.COMMON, new ItemStack(Material.SKULL_ITEM, 1, (byte) 4)),
		SPELL_MOB_AMBIENT(7, "Ambient Mob Spell Halo Particles", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 66)),
		SPELL_WITCH(10, "Witch Spell Halo Particles", Rarity.COMMON, Material.DIAMOND_HELMET),
		DRIP_WATER(11, "Drip Water Halo Particles", Rarity.UNCOMMON, Material.WATER_BUCKET),
		DRIP_LAVA(12, "Drip Lava Halo Particles", Rarity.UNCOMMON, Material.LAVA_BUCKET),
		VILLAGER_ANGRY(13, "Angry Villager Halo Particles", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 120)),
		VILLAGER_HAPPY(14, "Happy Villager Halo Particles", Rarity.COMMON, Material.NETHER_STAR),
		NOTE(15, "Note Halo Particles", Rarity.UNCOMMON, Material.JUKEBOX),
		FLAME(16, "Flame Halo Particles", Rarity.RARE, Material.BLAZE_POWDER),
		CLOUD(19, "Cloud Halo Particles", Rarity.COMMON, Material.FEATHER),
		REDSTONE(20, "Redstone Halo Particles", Rarity.COMMON, Material.REDSTONE),
		SNOWBALL(21, "Snowball Halo Particles", Rarity.COMMON, Material.SNOW_BALL),
		SNOW_SHOVEL(22, "Shovel Snow Halo Particles", Rarity.COMMON, Material.SNOW_BLOCK),
		SLIME(23, "Slime Halo Particles", Rarity.COMMON, Material.SLIME_BALL),
		HEART(24, "Heart Halo Particles", Rarity.UNCOMMON, Material.RED_ROSE),
		FIREWORKS_SPARK(25, "Firework Spark Halo Particles", Rarity.RARE, Material.FIREWORK),
		NONE(33, "&cSet No Halo Particles", Rarity.COMMON, Material.WEB, false);
		
		private int slot = 0;
		private String name = null;
		private ItemStack itemStack = null;
		private boolean store = true;
		private Rarity rarity = Rarity.COMMON;
		
		private HaloParticleTypes(int slot, String name, Rarity rarity, Material material) {
			this(slot, name, rarity, new ItemStack(material));
		}
		
		private HaloParticleTypes(int slot, String name, Rarity rarity, ItemStack itemStack) {
			this(slot, name, rarity, itemStack, true);
		}
		
		private HaloParticleTypes(int slot, String name, Rarity rarity, Material material, boolean store) {
			this(slot, name, rarity, new ItemStack(material), store);
		}
		
		private HaloParticleTypes(int slot, String name, Rarity rarity, ItemStack itemStack, boolean store) {
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
			if(Ranks.VIP.hasRank(player)) {
				if(this == FIREWORKS_SPARK) {
					String [] keys = new String [] {"uuid", "name"};
					String [] values = new String [] {player.getUniqueId().toString(), toString()};
					if(!DB.HUB_HALO_PARTICLES.isKeySet(keys, values)) {
						String time = TimeUtil.getTime().substring(0, 10);
						Bukkit.getLogger().info("halo particles: give");
						DB.HUB_HALO_PARTICLES.insert("'" + player.getUniqueId().toString() + "', '" + toString() + "', '0', '1', '" + time + "'");
					}
					return true;
				}
			}
			InventoryView inventoryView = opened(player);
			if(inventoryView != null) {
				return store && inventoryView.getItem(getSlot()).getType() != Material.INK_SACK;
			}
			return store && DB.HUB_HALO_PARTICLES.isKeySet(new String [] {"uuid", "name"}, new String [] {player.getUniqueId().toString(), toString()});
		}
		
		public void give(Player player) {
			if(store) {
				String [] keys = new String [] {"uuid", "name"};
				String [] values = new String [] {player.getUniqueId().toString(), toString()};
				if(owns(player) || DB.HUB_HALO_PARTICLES.isKeySet(keys, values)) {
					Bukkit.getLogger().info("halo particles: give more");
					int owned = DB.HUB_HALO_PARTICLES.getInt(keys, values, "amount_owned");
					DB.HUB_HALO_PARTICLES.updateInt("amount_owned", owned + 1, keys, values);
				} else {
					String time = TimeUtil.getTime().substring(0, 10);
					Bukkit.getLogger().info("halo particles: give");
					DB.HUB_HALO_PARTICLES.insert("'" + player.getUniqueId().toString() + "', '" + toString() + "', '0', '1', '" + time + "'");
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
					int owned = DB.HUB_HALO_PARTICLES.getInt(keys, values, "amount_owned");
					item.setLores(new String [] {
						"",
						"&7Status: &eUnlocked",
						"&7You own &e" + owned + " &7of these",
						"&7Unlocked on &e" + DB.HUB_HALO_PARTICLES.getString(keys, values, "unlocked_time"),
						"&7Rarity: &e" + getRarity().getName(),
						""
					});
					if(this == FIREWORKS_SPARK) {
						item.addLore("&7Requires " + Ranks.VIP.getPrefix());
						item.addLore("");
					}
					Bukkit.getLogger().info("halo particles: getItem");
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
				if(this == FIREWORKS_SPARK) {
					item.addLore("&7Requires " + Ranks.VIP.getPrefix());
					item.addLore("");
				}
			}
			return item.getItemStack();
		}
		
		public void decrenentAmount(Player player) {
			String [] keys = new String [] {"uuid", "name"};
			String [] values = new String [] {player.getUniqueId().toString(), toString()};
			int amount = DB.HUB_HALO_PARTICLES.getInt(keys, values, "amount_owned") - 1;
			if(amount <= 0) {
				DB.HUB_HALO_PARTICLES.delete(keys, values);
			} else {
				DB.HUB_HALO_PARTICLES.updateInt("amount_owned", amount, keys, values);
			}
			owned.remove(player.getName());
		}
		
		public void display(Location location) {
			ParticleEffect effect = null;
			try {
				effect = ParticleEffect.valueOf(toString());
			} catch(IllegalArgumentException e) {
				effect = ParticleEffect.valueOf(HaloParticleTypes.values()[random.nextInt(HaloParticleTypes.values().length)].toString());
			}
			effect.display(0, 0, 0, 0, 1, location, 20);
		}
	}
	
	public HaloParticles() {
		super(getInvName(), 10, new ItemStack(Material.DIAMOND_HELMET), null, new String [] {
			"",
			"&7Show off with these cool",
			"&7particles above your head",
			"",
			"&7Owned: &eXX&8/&e" + max + " &7(&eYY%&7)",
			"&7Collect from: &eZZ",
			""
		});
		owned = new HashMap<String, Integer>();
		selected = new HashMap<String, HaloParticleTypes>();
		circles = new HashMap<String, CircleUtil>();
		queue = new ArrayList<UUID>();
		settingsChanged = new ArrayList<String>();
		random = new Random();
		HaloParticleTypes.values();
	}
	
	private static String getInvName() {
		return "Halo Particles";
	}
	
	private static InventoryView opened(Player player) {
		InventoryView inventoryView = player.getOpenInventory();
		return inventoryView != null && inventoryView.getTitle().equals(getInvName()) ? inventoryView : null;
	}
	
	private HaloParticleTypes getParticles(int slot) {
		for(HaloParticleTypes particle : HaloParticleTypes.values()) {
			if(particle.getSlot() == slot) {
				return particle;
			}
		}
		return null;
	}
	
	private String getSetting() {
		return "load_halo_particles_on_join";
	}
	
	private ItemStack getSettingItem(Player player) {
		return getSettingItem(player, -1);
	}
	
	private ItemStack getSettingItem(Player player, int state) {
		if(state == -1) {
			state = DB.PLAYERS_SETTINGS.getInt(new String [] {"uuid", "setting"}, new String [] {player.getUniqueId().toString(), getSetting()}, "state");
			Bukkit.getLogger().info("halo particles: getSettingItem");
		}
		ItemCreator loadItem = null;
		if(state == 1) {
			loadItem = new ItemCreator(Material.EMERALD_BLOCK).setName("&bLoad Halo Particles on Join: &eOn");
		} else {
			loadItem = new ItemCreator(Material.REDSTONE_BLOCK).setName("&bLoad Halo Particles on Join: &cOff");
		}
		loadItem.setLores(new String [] {
			"",
			"&7This toggles weather or not your halo",
			"&7particles will load when you join a hub",
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
			final int displayState = player.getOpenInventory().getItem(29).getType() == Material.EMERALD_BLOCK ? 1 : 0;
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "setting"};
					String [] values = new String [] {uuid.toString(), getSetting()};
					if(DB.PLAYERS_SETTINGS.isKeySet(keys, values)) {
						if(displayState == 0) {
							DB.PLAYERS_SETTINGS.delete(keys, values);
						} else {
							DB.PLAYERS_SETTINGS.updateInt("state", displayState, keys, values);
						}
					} else if(displayState > 0) {
						DB.PLAYERS_SETTINGS.insert("'" + uuid.toString() + "', '" + getSetting() + "', '" + displayState + "'");
					}
					Bukkit.getLogger().info("halo particles: save settings");
				}
			});
		}
	}
	
	private void select(Player player) {
		final HaloParticleTypes type = selected.get(player.getName());
		if(type != null) {
			final UUID uuid = player.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					DB.HUB_HALO_PARTICLES.updateInt("active", 0, "uuid", uuid.toString());
					DB.HUB_HALO_PARTICLES.updateInt("active", 1, new String [] {"uuid", "name"}, new String [] {uuid.toString(), type.toString()});
					Bukkit.getLogger().info("halo particles: selected");
				}
			});
		}
		saveSetting(player);
	}
	
	private void cancel(Player player) {
		if(circles.containsKey(player.getName())) {
			circles.get(player.getName()).delete();
			circles.remove(player.getName());
		}
	}
	
	private void enable(Player player, final HaloParticleTypes type) {
		cancel(player);
		selected.put(player.getName(), type);
		circles.put(player.getName(), new CircleUtil(player, .85, 6) {
			@Override
			public void run(Vector vector, Location location) {
				ParticleEffect.valueOf(type.toString()).display(location.add(0, 2.20, 0), 20);
			}
		});
	}
	
	@Override
	public int getOwned(Player player) {
		if(!owned.containsKey(player.getName())) {
			Bukkit.getLogger().info("halo particles: getOwned");
			owned.put(player.getName(), DB.HUB_HALO_PARTICLES.getSize("uuid", player.getUniqueId().toString()));
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
				for(HaloParticleTypes particle : HaloParticleTypes.values()) {
					inventory.setItem(particle.getSlot(), particle.getItem(player, getAction()));
				}
				inventory.setItem(29, getSettingItem(player));
				inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&cBack").getItemStack());
				player.openInventory(inventory);
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getInvName())) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.EMERALD_BLOCK || item.getType() == Material.REDSTONE_BLOCK) {
				if(Ranks.VIP.hasRank(player)) {
					if(event.getSlot() == 29) {
						int newState = item.getType() == Material.EMERALD_BLOCK ? 0 : 1;
						player.getOpenInventory().setItem(event.getSlot(), getSettingItem(player, newState));
					}
					if(!settingsChanged.contains(player.getName())) {
						settingsChanged.add(player.getName());
					}
				} else {
					MessageHandler.sendMessage(player, Ranks.VIP.getNoPermission());
				}
			} else if(item.getType() == Material.WEB) {
				String name = event.getItemTitle();
				if(selected.containsKey(player.getName())) {
					final UUID uuid = player.getUniqueId();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.HUB_HALO_PARTICLES.updateInt("active", 0, "uuid", uuid.toString());
							Bukkit.getLogger().info("halo particles: set none");
						}
					});
					cancel(player);
					selected.remove(player.getName());
					MessageHandler.sendMessage(player, "You have set " + name);
				}
			} else if(item.getType() == Material.INK_SACK) {
				displayLocked(player);
			} else if(item.getType() == Material.WOOD_DOOR) {
				Features.open(player);
			} else {
				String name = event.getItemTitle();
				final HaloParticleTypes type = getParticles(event.getSlot());
				if(type != null && !(selected.containsKey(player.getName()) && selected.get(player.getName()) == type)) {
					if(type == HaloParticleTypes.FIREWORKS_SPARK && !Ranks.VIP.hasRank(player)) {
						MessageHandler.sendMessage(player, "&cThis Halo Particle requires at least " + Ranks.VIP.getPrefix());
					} else {
						enable(player, type);
						MessageHandler.sendMessage(player, "You have enabled &e" + ChatColor.stripColor(name));
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
			Bukkit.getLogger().info("halo particles: queue");
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
							for(String haloName : DB.HUB_HALO_PARTICLES.getAllStrings("name", new String [] {"uuid", "active"}, new String [] {uuid.toString(), "1"})) {
								Bukkit.getLogger().info("halo particles: " + player.getName() + " queue");
								HaloParticleTypes type = HaloParticleTypes.valueOf(haloName);
								if(type == null) {
									DB.HUB_HALO_PARTICLES.delete(new String [] {"uuid", "name"}, new String [] {uuid.toString(), haloName});
								} else {
									enable(player, type);
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
		selected.remove(event.getPlayer().getName());
		queue.remove(event.getPlayer().getUniqueId());
		select(event.getPlayer());
		cancel(event.getPlayer());
	}
}
