package network.gameapi.crates;

import network.Network;
import network.Network.Plugins;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PostPlayerJoinEvent;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.SkyWarsShop;
import network.player.CoinsHandler;
import network.player.MessageHandler;
import network.server.ChatClickHandler;
import network.server.servers.hub.items.Features.Rarity;
import network.server.servers.hub.items.features.FeatureItem;
import network.server.servers.hub.items.features.FeatureItem.FeatureType;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class SkyWarsCrate implements Listener {
	private static List<String> delayed = null;
	private static List<FeatureItem> features = null;
	private static final int cost = 40;
	
	public SkyWarsCrate() {
		delayed = new ArrayList<String>();
		features = new ArrayList<FeatureItem>();
		EventUtil.register(this);
	}
	
	public static void addItem(final Player player, final Inventory inventory) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String uuid = player.getUniqueId().toString();
				int month = Calendar.getInstance().get(Calendar.MONTH);
				int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
				String [] lores = null;
				lores = new String [] {
					"",
					"&eGet random kits, cages and more!",
					"",
					"&7Coins:&a " + cost,
					"&7Get one &aFREE &7through &a/vote",
					"",
					"&7Left click to open a crate",
					"&7Right click to purchase a key",
					"",
					"&eKeys owned: &a" + getKeys(player),
					""
				};
				ItemCreator itemCreator = new ItemCreator(Material.CHEST).setName("&b" + getName()).setLores(lores).setGlow(true);
				inventory.setItem(4, itemCreator.getItemStack());
				ItemStack stats = new ItemCreator(Material.PAPER).setName("&bCrate Stats").setLores(new String [] {
					"",
					"&eSky Wars Crate Stats:",
//					"&7Lifetime Sky Wars crates opened: &a" + DB.HUB_LIFETIME_SKY_WARS_CRATES_OPENED.getInt("uuid", uuid, "amount"),
//					"&7Monthly Sky Wars crates opened: &a" + DB.HUB_MONTHLY_SKY_WARS_CRATES_OPENED.getInt(new String [] {"uuid", "month"}, new String [] {uuid, month + ""}, "amount"),
//					"&7Weekly Sky Wars crates opened: &a" + DB.HUB_WEEKLY_SKY_WARS_CRATES_OPENED.getInt(new String [] {"uuid", "week"}, new String [] {uuid, week + ""}, "amount"),
					""
				}).getItemStack();
				inventory.setItem(1, stats);
				inventory.setItem(7, stats);
			}
		});
	}
	
	private static String getName() {
		return "Sky Wars Crate";
	}
	
	private static void updateItem(Player player) {
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(SkyWarsShop.getInstance().getName())) {
			ItemCreator itemCreator = new ItemCreator(player.getOpenInventory().getItem(4));
			int index = -1;
			for(String lore : itemCreator.getLores()) {
				++index;
				if(ChatColor.stripColor(lore).startsWith("Keys")) {
					String [] lores = itemCreator.getLoreArray();
					lores[index] = StringUtil.color("&eKeys owned: &a" + getKeys(player));
					itemCreator.setLores(lores);
					player.getOpenInventory().setItem(4, itemCreator.getItemStack());
					break;
				}
			}
		}
	}
	
	private static int getKeys(Player player) {
		Bukkit.getLogger().info("sky wars crate: get keys");
		return 0;//DB.HUB_SKY_WARS_CRATE_KEYS.getInt("uuid", player.getUniqueId().toString(), "amount");
	}
	
	public static void giveKey(final UUID uuid, final int toAdd) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
//				if(DB.HUB_SKY_WARS_CRATE_KEYS.isUUIDSet(uuid)) {
//					int amount = DB.HUB_SKY_WARS_CRATE_KEYS.getInt("uuid", uuid.toString(), "amount") + toAdd;
//					DB.HUB_SKY_WARS_CRATE_KEYS.updateInt("amount", amount, "uuid", uuid.toString());
//				} else {
//					DB.HUB_SKY_WARS_CRATE_KEYS.insert("'" + uuid.toString() + "', '" + toAdd + "'");
//				}
				Player player = Bukkit.getPlayer(uuid);
				if(player != null) {
					updateItem(player);
				}
				Bukkit.getLogger().info("sky wars crate: give keys");
			}
		});
	}
	
	private void populateFeatures() {
		if(features.isEmpty()) {
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getPluginData().equals(Plugins.UHCSW.getData())) {
					features.add(new FeatureItem(kit.getName(), kit.getIcon(), kit.getKitRarity(), FeatureType.SKY_WARS));
				}
			}
			features.add(new FeatureItem("15 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.COMMON, FeatureType.SKY_WARS));
			features.add(new FeatureItem("25 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.COMMON, FeatureType.SKY_WARS));
			features.add(new FeatureItem("35 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.UNCOMMON, FeatureType.SKY_WARS));
			features.add(new FeatureItem("45 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.UNCOMMON, FeatureType.SKY_WARS));
			features.add(new FeatureItem("60 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.RARE, FeatureType.SKY_WARS));
			features.add(new FeatureItem("80 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.RARE, FeatureType.SKY_WARS));
			features.add(new FeatureItem("Crate Key x3", new ItemCreator(Material.TRIPWIRE_HOOK).setGlow(true).getItemStack(), Rarity.RARE, FeatureType.SKY_WARS));
		}
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		if(Network.getMiniGame() == null) {
			PostPlayerJoinEvent.getHandlerList().unregister(this);
		} else {
			final Player player = event.getPlayer();
			if(CoinsHandler.getCoinsHandler(Plugins.UHCSW.getData()).getCoins(player) >= cost) {
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						EffectUtil.playSound(player, Sound.LEVEL_UP);
						MessageHandler.sendMessage(player, "&a[TIP] &xYou have enough coins to buy a &bSky Wars Crate");
						ChatClickHandler.sendMessageToRunCommand(player, " &bChest", "Click to Open Shop", "/shop", "&bSky Wars Crates &eare found in the shop, click the");
					}
				}, 20 * 2);
			}
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(event.getItemTitle() != null && ChatColor.stripColor(event.getItemTitle()).equals(getName())) {
			if(event.getClickType() == ClickType.LEFT) {
				if(delayed.contains(player.getName())) {
					EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				} else {
					final String name = player.getName();
					delayed.add(name);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(name);								
						}
					}, 20 * 2);
					if(getKeys(player) > 0) {
						populateFeatures();
//						new CrateBase(player, Plugins.UHCSW, SkyWarsCrate.getDisplay(), features).setLifetime(DB.HUB_LIFETIME_SKY_WARS_CRATES_OPENED).setMonthly(DB.HUB_MONTHLY_SKY_WARS_CRATES_OPENED).setWeekly(DB.HUB_WEEKLY_SKY_WARS_CRATES_OPENED);
					} else {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					}
				}
			} else if(event.getClickType() == ClickType.MIDDLE) {
				//TODO: Display item options and rarities
				populateFeatures();
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			} else if(event.getClickType() == ClickType.RIGHT) {
				CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.UHCSW.getData());
				int coins = coinsHandler.getCoins(player);
				if(coins >= cost) {
					coinsHandler.addCoins(player, cost * -1);
					giveKey(player.getUniqueId(), 1);
					EffectUtil.playSound(player, Sound.LEVEL_UP);
				} else {
					EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCrateFinished(CrateFinishedEvent event) {
		if(event.getPlugin() == Plugins.UHCSW) {
			Player player = event.getPlayer();
			giveKey(player.getUniqueId(), -1);
			FeatureItem won = event.getItemWon();
			String name = won.getName();
			if(won.getItemStack().getType() == Material.GOLD_INGOT) {
				int coins = Integer.valueOf(name.split(" ")[0]);
				CoinsHandler handler = CoinsHandler.getCoinsHandler(event.getPlugin().getData());
				handler.addCoins(player, coins);
				return;
			} else if(won.getItemStack().getType() == Material.TRIPWIRE_HOOK) {
				giveKey(player.getUniqueId(), 3);
				return;
			}
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getPluginData().equals(event.getPlugin().getData()) && name.equals(kit.getName())) {
					kit.giveKit(player);
					return;
				}
			}
		}
	}
}