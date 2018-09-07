package network.server.servers.hub.items.features.particles;

import de.slikey.effectlib.util.ParticleEffect;
import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.game.GameStartingEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.*;

@SuppressWarnings("deprecation")
public class ArrowTrails extends FeatureBase {
	private static int max = 21;
	private static Map<String, Integer> owned = null;
	private static Map<String, ArrowTrailParticleTypes> selected = null;
	private static Map<Arrow, ArrowTrailParticleTypes> particles = null;
	private static List<String> settingsChanged = null;
	private static Random random = null;
	
	public enum ArrowTrailParticleTypes {
		CRIT(1, "Crit Arrow Trail", Rarity.COMMON, Material.DIAMOND_SWORD),
		CRIT_MAGIC(2, "Magic Crit Arrow Trail", Rarity.COMMON, new ItemCreator(Material.DIAMOND_SWORD).setGlow(true).getItemStack()),
		SMOKE_NORMAL(3, "Smoke Arrow Trail", Rarity.COMMON, Material.WEB),
		SPELL(4, "Spell Arrow Trail", Rarity.COMMON, new Potion(PotionType.REGEN, 1, true).toItemStack(1)),
		SPELL_INSTANT(5, "Instant Spell Arrow Trail", Rarity.COMMON, Material.BEACON),
		SPELL_MOB(6, "Mob Spell Arrow Trail", Rarity.COMMON, new ItemStack(Material.SKULL_ITEM, 1, (byte) 4)),
		SPELL_MOB_AMBIENT(7, "Ambient Mob Spell Arrow Trail", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 66)),
		SPELL_WITCH(10, "Witch Spell Arrow Trail", Rarity.COMMON, Material.ARROW),
		DRIP_WATER(11, "Drip Water Arrow Trail", Rarity.UNCOMMON, Material.WATER_BUCKET),
		DRIP_LAVA(12, "Drip Lava Arrow Trail", Rarity.UNCOMMON, Material.LAVA_BUCKET),
		VILLAGER_ANGRY(13, "Angry Villager Arrow Trail", Rarity.COMMON, new ItemStack(Material.MONSTER_EGG, 1, (byte) 120)),
		VILLAGER_HAPPY(14, "Happy Villager Arrow Trail", Rarity.COMMON, Material.NETHER_STAR),
		NOTE(15, "Note Arrow Trail", Rarity.UNCOMMON, Material.JUKEBOX),
		FLAME(16, "Flame Arrow Trail", Rarity.RARE, Material.BLAZE_POWDER),
		CLOUD(19, "Cloud Arrow Trail", Rarity.COMMON, Material.FEATHER),
		REDSTONE(20, "Redstone Arrow Trail", Rarity.COMMON, Material.REDSTONE),
		SNOWBALL(21, "Snowball Arrow Trail", Rarity.COMMON, Material.SNOW_BALL),
		SNOW_SHOVEL(22, "Shovel Snow Arrow Trail", Rarity.COMMON, Material.SNOW_BLOCK),
		SLIME(23, "Slime Arrow Trail", Rarity.COMMON, Material.SLIME_BALL),
		HEART(24, "Heart Arrow Trail", Rarity.UNCOMMON, Material.RED_ROSE),
		RANDOM(25, "Random Arrow Trail", Rarity.RARE, Material.ARROW),
		NONE(33, "&cSet No Arrow Trail", Rarity.COMMON, Material.WEB, false)
		
		;
		
		private int slot = 0;
		private String name = null;
		private ItemStack itemStack = null;
		private boolean store = true;
		private Rarity rarity = Rarity.COMMON;
		
		private ArrowTrailParticleTypes(int slot, String name, Rarity rarity, Material material) {
			this(slot, name, rarity, new ItemStack(material));
		}
		
		private ArrowTrailParticleTypes(int slot, String name, Rarity rarity, ItemStack itemStack) {
			this(slot, name, rarity, itemStack, true);
		}
		
		private ArrowTrailParticleTypes(int slot, String name, Rarity rarity, Material material, boolean store) {
			this(slot, name, rarity, new ItemStack(material), store);
		}
		
		private ArrowTrailParticleTypes(int slot, String name, Rarity rarity, ItemStack itemStack, boolean store) {
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
			return store && DB.PLAYERS_ARROW_TRAILS.isKeySet(new String [] {"uuid", "name"}, new String [] {player.getUniqueId().toString(), toString()});
		}
		
		public void give(Player player) {
			if(store) {
				String [] keys = new String [] {"uuid", "name"};
				String [] values = new String [] {player.getUniqueId().toString(), toString()};
				if(owns(player) || DB.PLAYERS_ARROW_TRAILS.isKeySet(keys, values)) {
					Bukkit.getLogger().info("arrow trails: give more");
					int owned = DB.PLAYERS_ARROW_TRAILS.getInt(keys, values, "amount_owned");
					DB.PLAYERS_ARROW_TRAILS.updateInt("amount_owned", owned + 1, keys, values);
				} else {
					String time = TimeUtil.getTime().substring(0, 10);
					Bukkit.getLogger().info("arrow trails: give");
					DB.PLAYERS_ARROW_TRAILS.insert("'" + player.getUniqueId().toString() + "', '" + toString() + "', '0', '1', '" + time + "'");
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
					int owned = DB.PLAYERS_ARROW_TRAILS.getInt(keys, values, "amount_owned");
					item.setLores(new String [] {
						"",
						"&7Status: &eUnlocked",
						"&7You own &e" + owned + " &7of these",
						"&7Unlocked on &e" + DB.PLAYERS_ARROW_TRAILS.getString(keys, values, "unlocked_time"),
						"&7Rarity: &e" + getRarity().getName(),
						""
					});
					Bukkit.getLogger().info("arrow trails: getItem");
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
			int amount = DB.PLAYERS_ARROW_TRAILS.getInt(keys, values, "amount_owned") - 1;
			if(amount <= 0) {
				DB.PLAYERS_ARROW_TRAILS.delete(keys, values);
			} else {
				DB.PLAYERS_ARROW_TRAILS.updateInt("amount_owned", amount, keys, values);
			}
			owned.remove(player.getName());
		}
		
		public void display(Location location) {
			ParticleEffect effect = null;
			try {
				effect = ParticleEffect.valueOf(toString());
			} catch(IllegalArgumentException e) {
				effect = ParticleEffect.valueOf(ArrowTrailParticleTypes.values()[random.nextInt(ArrowTrailParticleTypes.values().length)].toString());
			}
			effect.display(0, 0, 0, 0, 1, location, 20);
		}
	}
	
	public ArrowTrails() {
		super(getInvName(), 16, new ItemStack(Material.ARROW), null, new String [] {
			"",
			"&7Keep track of your arrows",
			"&7with these cool particles",
			"",
			"&7Owned: &eXX&8/&e" + max + " &7(&eYY%&7)",
			"&7Collect from: &eZZ",
			""
		});
		owned = new HashMap<String, Integer>();
		selected = new HashMap<String, ArrowTrailParticleTypes>();
		particles = new HashMap<Arrow, ArrowTrailParticleTypes>();
		settingsChanged = new ArrayList<String>();
		random = new Random();
		ArrowTrailParticleTypes.values();
	}
	
	private static String getInvName() {
		return "Arrow Trails";
	}
	
	private static InventoryView opened(Player player) {
		InventoryView inventoryView = player.getOpenInventory();
		return inventoryView != null && inventoryView.getTitle().equals(getInvName()) ? inventoryView : null;
	}
	
	private void load(Player player) {
		Bukkit.getLogger().info("arrow trails: load");
		selected.put(player.getName(), ArrowTrailParticleTypes.valueOf(DB.PLAYERS_ARROW_TRAILS.getString(new String [] {"uuid", "active"}, new String [] {player.getUniqueId().toString(), "1"}, "name")));
	}
	
	private ArrowTrailParticleTypes getParticles(int slot) {
		for(ArrowTrailParticleTypes particle : ArrowTrailParticleTypes.values()) {
			if(particle.getSlot() == slot) {
				return particle;
			}
		}
		return null;
	}
	
	private String getDisplaySetting() {
		return "display_arrow_trails_to_others";
	}
	
	private ItemStack getDisplaySettingItem(Player player) {
		return getDisplaySettingItem(player, -1);
	}
	
	private ItemStack getDisplaySettingItem(Player player, int state) {
		if(state == -1) {
			Bukkit.getLogger().info("arrow trails: getDisplaySettings");
			state = DB.PLAYERS_SETTINGS.getInt(new String [] {"uuid", "setting"}, new String [] {player.getUniqueId().toString(), getDisplaySetting()}, "state");
		}
		ItemCreator loadItem = null;
		if(state == 1) {
			loadItem = new ItemCreator(Material.EMERALD_BLOCK).setName("&bDisplay Trails to Others: &eOn");
		} else {
			loadItem = new ItemCreator(Material.REDSTONE_BLOCK).setName("&bDisplay Trails to Others: &cOff");
		}
		loadItem.setLores(new String [] {
			"",
			"&7This toggles wheather or not your",
			"&7trail particles will be displayed",
			"&7to other players. Useful for staying",
			"&7hidden but still using your perks!",
			"",
			"&c(COMING SOON)",
			"",
		});
		return loadItem.getItemStack();
	}
	
	private String getViewSetting() {
		return "view_arrow_trails_from_others";
	}
	
	private ItemStack getViewSettingItem(Player player) {
		return getViewSettingItem(player, -1);
	}
	
	private ItemStack getViewSettingItem(Player player, int state) {
		if(state == -1) {
			Bukkit.getLogger().info("arrow trails: getViewSettings");
			state = DB.PLAYERS_SETTINGS.getInt(new String [] {"uuid", "setting"}, new String [] {player.getUniqueId().toString(), getViewSetting()}, "state");
		}
		ItemCreator loadItem = null;
		if(state == 0) {
			loadItem = new ItemCreator(Material.EMERALD_BLOCK).setName("&bView Trails from Others: &eOn");
		} else {
			loadItem = new ItemCreator(Material.REDSTONE_BLOCK).setName("&bView Trails from Others: &cOff");
		}
		loadItem.setLores(new String [] {
			"",
			"&7This toggles wheather or not you",
			"&7will see trail particles from other",
			"&7players. If they annoy you then you",
			"&7can disable them",
			"",
			"&c(COMING SOON)",
			"",
		});
		return loadItem.getItemStack();
	}
	
	private void saveSetting(Player player) {
		if(settingsChanged.contains(player.getName())) {
			settingsChanged.remove(player.getName());
			final UUID uuid = player.getUniqueId();
			final int displayState = player.getOpenInventory().getItem(29).getType() == Material.EMERALD_BLOCK ? 1 : 0;
			final int viewState = player.getOpenInventory().getItem(31).getType() == Material.EMERALD_BLOCK ? 0 : 1;
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "setting"};
					String [] values = new String [] {uuid.toString(), getDisplaySetting()};
					if(DB.PLAYERS_SETTINGS.isKeySet(keys, values)) {
						if(displayState == 0) {
							DB.PLAYERS_SETTINGS.delete(keys, values);
						} else {
							DB.PLAYERS_SETTINGS.updateInt("state", displayState, keys, values);
						}
					} else if(displayState > 0) {
						DB.PLAYERS_SETTINGS.insert("'" + uuid.toString() + "', '" + getDisplaySetting() + "', '" + displayState + "'");
					}
					values[1] = getViewSetting();
					if(DB.PLAYERS_SETTINGS.isKeySet(keys, values)) {
						if(viewState == 0) {
							DB.PLAYERS_SETTINGS.delete(keys, values);
						} else {
							DB.PLAYERS_SETTINGS.updateInt("state", viewState, keys, values);
						}
					} else if(viewState > 0) {
						DB.PLAYERS_SETTINGS.insert("'" + uuid.toString() + "', '" + getViewSetting() + "', '" + viewState + "'");
					}
					Bukkit.getLogger().info("arrow trails: save settings");
				}
			});
		}
	}
	
	private void select(Player player) {
		final ArrowTrailParticleTypes type = selected.get(player.getName());
		if(type != null) {
			UUID uuid = player.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					DB.PLAYERS_ARROW_TRAILS.updateInt("active", 0, "uuid", uuid.toString());
					DB.PLAYERS_ARROW_TRAILS.updateInt("active", 1, new String [] {"uuid", "name"}, new String [] {uuid.toString(), type.toString()});
					Bukkit.getLogger().info("arrow trails: selected");
				}
			});
		}
		saveSetting(player);
	}
	
	@Override
	public int getOwned(Player player) {
		if(!owned.containsKey(player.getName())) {
			Bukkit.getLogger().info("arrow trails: getOwned");
			owned.put(player.getName(), DB.PLAYERS_ARROW_TRAILS.getSize("uuid", player.getUniqueId().toString()));
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
				for(ArrowTrailParticleTypes particle : ArrowTrailParticleTypes.values()) {
					inventory.setItem(particle.getSlot(), particle.getItem(player, getAction()));
				}
				inventory.setItem(29, getDisplaySettingItem(player));
				inventory.setItem(31, getViewSettingItem(player));
				inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&cBack").getItemStack());
				player.openInventory(inventory);
			}
		});
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					load(player);
				}
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getInvName())) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.EMERALD_BLOCK || item.getType() == Material.REDSTONE_BLOCK) {
				if(event.getSlot() == 29) {
					int newState = item.getType() == Material.EMERALD_BLOCK ? 0 : 1;
					player.getOpenInventory().setItem(event.getSlot(), getDisplaySettingItem(player, newState));
				} else {
					int newState = item.getType() == Material.EMERALD_BLOCK ? 1 : 0;
					player.getOpenInventory().setItem(event.getSlot(), getViewSettingItem(player, newState));
				}
				if(!settingsChanged.contains(player.getName())) {
					settingsChanged.add(player.getName());
				}
			} else if(item.getType() == Material.WEB) {
				String name = event.getItemTitle();
				if(selected.containsKey(player.getName())) {
					UUID uuid = player.getUniqueId();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.PLAYERS_ARROW_TRAILS.updateInt("active", 0, "uuid", uuid.toString());
							Bukkit.getLogger().info("arrow trails: set none");
						}
					});
					selected.remove(player.getName());
					MessageHandler.sendMessage(player, "You have set " + name);
				}
			} else if(item.getType() == Material.INK_SACK) {
				displayLocked(player);
			} else if(item.getType() == Material.WOOD_DOOR) {
				Features.open(player);
			} else {
				String name = event.getItemTitle();
				final ArrowTrailParticleTypes type = getParticles(event.getSlot());
				if(type != null && !(selected.containsKey(player.getName()) && selected.get(player.getName()) == type)) {
					selected.put(player.getName(), type);
					MessageHandler.sendMessage(player, "You have enabled &e" + ChatColor.stripColor(name));
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			if(selected.containsKey(player.getName())) {
				Arrow arrow = (Arrow) event.getProjectile();
				particles.put(arrow, selected.get(player.getName()));
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long time = event.getTicks();
		if(time == 1) {
			Iterator<Arrow> iterator = particles.keySet().iterator();
			while(iterator.hasNext()) {
				Arrow arrow = iterator.next();
				if(arrow == null || arrow.isDead() || arrow.isOnGround()) {
					iterator.remove();
				} else {
					particles.get(arrow).display(arrow.getLocation());
				}
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getEntity();
			particles.remove(arrow);
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
		select(event.getPlayer());
	}
}
