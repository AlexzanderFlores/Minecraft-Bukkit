package network.gameapi.crates;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import network.Network.Plugins;
import network.customevents.TimeEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.server.DB;
import network.server.servers.hub.items.Features.Rarity;
import network.server.servers.hub.items.features.FeatureItem;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class CrateBase implements Listener {
	private int [] slots = null;
	private Random random = new Random();
	private Player player = null;
	private Plugins plugin = null;
	private String  title = null;
	private List<FeatureItem> features = null;
	private int glassSpeed = 2;
	private int tickSpeed = 2;
	private int start = 20;
	private int end = 25;
	private int counter = 0;
	private boolean displaying = false;
	private DB lifetime = null;
	private DB monthly = null;
	private DB weekly = null;
	private FeatureItem item = null;
	
	public CrateBase(Player player, Plugins plugin, String title, List<FeatureItem> features) {
		slots = new int [] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
		random = new Random();
		this.player = player;
		this.plugin = plugin;
		this.title = title;
		this.features = features;
		Inventory inventory = Bukkit.createInventory(player, 9 * 5, title);
		inventory.setItem(13, new ItemCreator(Material.HOPPER).setName(" ").getItemStack());
		inventory.setItem(31, new ItemCreator(Material.LONG_GRASS, 2).setName(" ").getItemStack());
		player.openInventory(inventory);
		EventUtil.register(this);
	}
	
	private void remove() {
		HandlerList.unregisterAll(this);
		slots = null;
		random = null;
		InventoryView inventoryView = player.getOpenInventory();
		if(inventoryView.getTitle().equals(title)) {
			player.closeInventory();
		}
		player = null;
		title = null;
		features = null;
		lifetime = null;
		monthly = null;
		weekly = null;
	}
	
	public List<FeatureItem> getFeatures() {
		return features;
	}
	
	public DB getLifetime() {
		return lifetime;
	}
	
	public CrateBase setLifetime(DB lifetime) {
		this.lifetime = lifetime;
		return this;
	}
	
	public DB getMonthly() {
		return monthly;
	}
	
	public CrateBase setMonthly(DB monthly) {
		this.monthly = monthly;
		return this;
	}
	
	public DB getWeekly() {
		return weekly;
	}
	
	public CrateBase setWeekly(DB weekly) {
		this.weekly = weekly;
		return this;
	}
	
	private void placeGlass() {
		InventoryView inventoryView = player.getOpenInventory();
		if(inventoryView != null && inventoryView.getTitle().equals(title)) {
			byte data = 0;
			if(displaying) {
				do {
					data = (byte) random.nextInt(15);
				} while(data == 8);
			}
			for(int slot : slots) {
				if(!displaying) {
					do {
						data = (byte) random.nextInt(15);
					} while(data == 8);
				}
				inventoryView.setItem(slot, new ItemCreator(Material.STAINED_GLASS_PANE, data).setGlow(true).setName(" ").getItemStack());
			}
		} else {
			remove();
		}
	}
	
	private void placeItems(boolean last) {
		EffectUtil.playSound(player, Sound.NOTE_PIANO);
		InventoryView inventoryView = player.getOpenInventory();
		if(inventoryView != null && inventoryView.getTitle().equals(title)) {
			for(int a = start; a < end; ++a) {
				FeatureItem feature = null;
				if(a == 22 && last) {
					int chance = random.nextInt(100) + 1;
					Rarity rarity = chance <= 10 ? Rarity.RARE : chance <= 35 ? Rarity.UNCOMMON : Rarity.COMMON;
					do {
						feature = features.get(random.nextInt(features.size()));
					} while(feature.getRarity() != rarity);
					item = feature;
				} else {
					feature = features.get(random.nextInt(features.size()));
				}
				inventoryView.setItem(a, new ItemCreator(feature.getItemStack()).setLores(new String [] {"", "&7Rarity: " + feature.getRarity().getName(), ""}).getItemStack());
			}
		}
	}
	
	private void win(InventoryView inventoryView) {
		Bukkit.getPluginManager().callEvent(new CrateFinishedEvent(player, plugin, item));
		if(lifetime == null || monthly == null || weekly == null) {
			remove();
		} else {
			final UUID uuid = player.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					if(lifetime != null) {
						if(lifetime.isUUIDSet(uuid)) {
							int amount = lifetime.getInt("uuid", uuid.toString(), "amount") + 1;
							lifetime.updateInt("amount", amount, "uuid", uuid.toString());
						} else {
							lifetime.insert("'" + uuid.toString() + "', '1'");
						}
					}
					Calendar calendar = Calendar.getInstance();
					if(monthly != null) {
						int month = calendar.get(Calendar.MONTH);
						String [] keys = new String [] {"uuid", "month"};
						String [] values = new String [] {uuid.toString(), month + ""};
						if(monthly.isKeySet(keys, values)) {
							int amount = monthly.getInt(keys, values, "amount") + 1;
							monthly.updateInt("amount", amount, keys, values);
						} else {
							monthly.insert("'" + uuid.toString() + "', '1', '" + month + "'");
						}
					}
					if(weekly != null) {
						int week = calendar.get(Calendar.WEEK_OF_YEAR);
						String [] keys = new String [] {"uuid", "week"};
						String [] values = new String [] {uuid.toString(), week + ""};
						if(weekly.isKeySet(keys, values)) {
							int amount = weekly.getInt(keys, values, "amount") + 1;
							weekly.updateInt("amount", amount, keys, values);
						} else {
							weekly.insert("'" + uuid.toString() + "', '1', '" + week + "'");
						}
					}
					remove();
				}
			});
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(!displaying) {
			if(ticks == glassSpeed) {
				placeGlass();
			}
			if(ticks == tickSpeed) {
				placeItems(false);
			}
			if(ticks == 20) {
				if(counter == 3) {
					tickSpeed = 7;
				} else if(counter == 5) {
					tickSpeed = 14;
				} else if(counter > 7) {
					placeItems(true);
					InventoryView inventoryView = player.getOpenInventory();
					inventoryView.setItem(13, new ItemStack(Material.AIR));
					inventoryView.setItem(31, new ItemStack(Material.AIR));
					displaying = true;
					counter = -1;
				}
				++counter;
			}
		} else {
			if(ticks == 5) {
				placeGlass();
			} else if(ticks == 20) {
				InventoryView inventoryView = player.getOpenInventory();
				if(inventoryView != null && inventoryView.getTitle().equals(title)) {
					if(counter == 1) {
						inventoryView.setItem(20, new ItemStack(Material.AIR));
						inventoryView.setItem(24, new ItemStack(Material.AIR));
						EffectUtil.playSound(player, random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2);
					} else if(counter == 2) {
						inventoryView.setItem(21, new ItemStack(Material.AIR));
						inventoryView.setItem(23, new ItemStack(Material.AIR));
						EffectUtil.playSound(player, random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2);
					} else if(counter == 3) {
						EffectUtil.playSound(player, Sound.LEVEL_UP);
					} else if(counter == 5) {
						win(inventoryView);
					}
					++counter;
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getPlayer().getName().equals(this.player.getName())) {
			InventoryView inventoryView = player.getOpenInventory();
			if(inventoryView != null && inventoryView.getTitle().equals(title)) {
				if(displaying) {
					win(inventoryView);
				} else {
					remove();
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(event.getPlayer().getName().equals(player.getName())) {
			remove();
		}
	}
}