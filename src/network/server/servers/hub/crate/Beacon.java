package network.server.servers.hub.crate;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import network.customevents.TimeEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.ChatClickHandler;
import network.server.DB;
import network.server.servers.hub.items.Features.Rarity;
import network.server.servers.hub.items.features.FeatureItem;
import network.server.servers.hub.items.features.FeatureItem.FeatureType;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.Particles.ParticleTypes;
import network.server.util.StringUtil;
import network.server.util.TimeUtil;
import npc.NPCEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings("deprecation")
public class Beacon implements Listener {
	private Random random = null;
	private Block glass = null;
	private final String originalName;
	private CrateTypes type = null;
	private Location hologramLocation = null;
	private Hologram hologram = null;
	private NPCEntity npc = null;
	private int counter = 0;
	private boolean running = false;
	private boolean displaying = false;
	private List<FeatureItem> items = null;
	private List<String> delayed = null;
	private static final int delay = 2;
	private static String keyFragmentName = null;
	private static String threeKeys = null;
	
	public Beacon(String originalName, CrateTypes type, Block glass, Vector standOffset) {
		random = new Random();
		this.glass = glass;
		this.originalName = originalName;
		this.type = type;
		this.hologramLocation = glass.getLocation().add(standOffset);
		hologram = HologramAPI.createHologram(hologramLocation, getName());
		hologram.spawn();
		delayed = new ArrayList<String>();
		setWood();
		keyFragmentName = "Key Fragment";
		threeKeys = CrateTypes.VOTING.getDisplay() + " Key x3";
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				items = FeatureItem.getItems(FeatureType.REWARD_CRATE);
				items.add(new FeatureItem(getKeyFragmentName(), new ItemStack(Material.TRIPWIRE_HOOK), Rarity.UNCOMMON, FeatureType.REWARD_CRATE));
				items.add(new FeatureItem(getThreeKeys(), new ItemStack(Material.TRIPWIRE_HOOK), Rarity.RARE, FeatureType.REWARD_CRATE));
			}
		});
		EventUtil.register(this);
	}
	
	public static String getKeyFragmentName() {
		return keyFragmentName;
	}
	
	public static String getThreeKeys() {
		return threeKeys;
	}
	
	public CrateTypes getType() {
		return type;
	}
	
	public static void giveKey(UUID uuid, int toAdd, CrateTypes type) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String [] keys = new String [] { "uuid", "type" };
				String [] values = new String [] { uuid.toString(), type.getName() };

				if(DB.HUB_CRATE_KEYS.isKeySet(keys, values)) {
					int amount = DB.HUB_CRATE_KEYS.getInt(keys, values, "amount") + toAdd;
					DB.HUB_CRATE_KEYS.updateInt("amount", amount, keys, values);
				} else {
					DB.HUB_CRATE_KEYS.insert("'" + uuid + "', '" + type.getName() + "', '" + toAdd + "'");
				}
				Bukkit.getLogger().info(type.getName() + ": give player key");
			}
		});
	}
	
	private void setWood() {
		glass.setType(Material.WOOD);
		glass.setData((byte) 3);
	}
	
	private void activate(final Player player) {
		if(delayed.contains(player.getName())) {
			return;
		} else {
			delayed.add(player.getName());
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(player.getName());
				}
			}, 20 * delay);
		}
		String [] keys = new String [] { "uuid", "type" };
		String [] values = new String [] { player.getUniqueId().toString(), type.getName() };
		if(DB.HUB_CRATE_KEYS.getInt(keys, values, "amount") <= 0) {
			if(type == CrateTypes.VOTING) {
				ChatClickHandler.sendMessageToRunCommand(player, "&aClick here", "Click to vote", "/vote", "&cYou don't have any &b" + type.getDisplay() + " Keys&c! Get some by voting ");
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have any &b" + type.getDisplay() + " Keys&c! Get some on Buycraft: &cComing Soon");
			}
			return;
		}
		if(npc != null) {
			npc.remove();
		}
		npc = new NPCEntity(EntityType.SILVERFISH, null, glass.getLocation().add(1.25, 1.40, 0.5)) {
			@Override
			public void onInteract(Player arg0) {
				
			}
		};
		npc.setSpawnZombie(false);
		LivingEntity livingEntity = (LivingEntity) npc.getLivingEntity();
		livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100));
		running = true;
		glass.setType(Material.STAINED_GLASS);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				FeatureItem item = null;
				int chance = random.nextInt(100) + 1;
				Rarity rarity = type == CrateTypes.PREMIUM ? Rarity.RARE : chance <= 10 ? Rarity.RARE : chance <= 35 ? Rarity.UNCOMMON : Rarity.COMMON;
				do {
					item = items.get(random.nextInt(items.size()));
				} while(item.getRarity() != rarity);
				setItem(item);
				setWood();
				displaying = true;
				if(player.isOnline()) {
					item.give(player);
					String log = item.getName();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							String uuid = player.getUniqueId().toString();
							int owned = DB.HUB_CRATE_KEYS.getInt(keys, values, "amount") - 1;
							if(owned <= 0) {
								DB.HUB_CRATE_KEYS.delete(keys, values);
								owned = 0;
							} else {
								DB.HUB_CRATE_KEYS.updateInt("amount", owned, keys, values);
							}
							Bukkit.getLogger().info(type + ": update key amount");
							MessageHandler.sendMessage(player, "You now have &e" + owned + " &x" + CrateTypes.VOTING.getDisplay() + " Crate key" + (owned == 1 ? "" : "s") + " left");
							if(DB.HUB_LIFETIME_CRATES_OPENED.isUUIDSet(player.getUniqueId())) {
								int amount = DB.HUB_LIFETIME_CRATES_OPENED.getInt(keys, values, "amount") + 1;
								DB.HUB_LIFETIME_CRATES_OPENED.updateInt("amount", amount, keys, values);
							} else {
								DB.HUB_LIFETIME_CRATES_OPENED.insert("'" + player.getUniqueId().toString() + "', '" + type + "', '" + 1 + "'");
							}
							Bukkit.getLogger().info(type + ": update lifetime crates used");
							Calendar calendar = Calendar.getInstance();
							String month = calendar.get(Calendar.MONTH) + "";
							String [] keys = new String [] {"uuid", "type", "month"};
							String [] values = new String [] {uuid, type.getName(), month};
							if(DB.HUB_MONTHLY_CRATES_OPENED.isKeySet(keys, values)) {
								int amount = DB.HUB_MONTHLY_CRATES_OPENED.getInt(keys, values, "amount") + 1;
								DB.HUB_MONTHLY_CRATES_OPENED.updateInt("amount", amount, keys, values);
							} else {
								DB.HUB_MONTHLY_CRATES_OPENED.insert("'" + uuid + "', '" + type + "', '1', '" + month + "'");
							}
							Bukkit.getLogger().info(type + ": update monthly crates used");
							String week = calendar.get(Calendar.WEEK_OF_YEAR) + "";
							keys[2] = "week";
							values[2] = week;
							if(DB.HUB_WEEKLY_CRATES_OPENED.isKeySet(keys, values)) {
								int amount = DB.HUB_WEEKLY_CRATES_OPENED.getInt(keys, values, "amount") + 1;
								DB.HUB_WEEKLY_CRATES_OPENED.updateInt("amount", amount, keys, values);
							} else {
								DB.HUB_WEEKLY_CRATES_OPENED.insert("'" + uuid + "', '" + type + "', '1', '" + week + "'");
							}
							Bukkit.getLogger().info(type + ": update weekly crates used");
							DB.HUB_CRATE_LOGS.insert("'" + player.getUniqueId().toString() + "', '" + type + "', '" + log + "', '" + TimeUtil.getTime() + "'");
							Bukkit.getLogger().info(type + ": update log");
						}
					});
					String rareString = "&8(" + item.getRarity().getName() + "&8)";
					MessageHandler.sendMessage(player, "&6You opened &c" + item.getName() + " " + rareString);
					if(item.getRarity() == Rarity.RARE) {
						MessageHandler.alert(AccountHandler.getRank(player).getColor() + player.getName() + " &xOpened &c" + item.getName() + " " + rareString);
					}
				}
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						counter = 0;
						hologram.setText(getName());
						if(npc != null) {
							if(npc.getLivingEntity().getPassenger() != null) {
								npc.getLivingEntity().getPassenger().remove();
							}
							npc.remove();
							npc = null;
						}
						running = false;
						displaying = false;
					}
				}, 20 * 5);
			}
		}, 20 * 7 + 10);
	}
	
	private String getName() {
		if(counter > 0) {
			return StringUtil.color("&e" + originalName.substring(counter, originalName.length() - counter)).replace("&", "");
		}
		return StringUtil.color("&e" + originalName);
	}
	
	private void setItem() {
		setItem(null);
	}
	
	private void setItem(FeatureItem featureItem) {
		if(npc.getLivingEntity().getPassenger() == null) {
			if(featureItem == null) {
				featureItem = items.get(random.nextInt(items.size()));
			}
			Item item = npc.getLivingEntity().getWorld().dropItemNaturally(npc.getLivingEntity().getLocation(), featureItem.getItemStack());
			npc.getLivingEntity().setPassenger(item);
		} else {
			Item item = (Item) npc.getLivingEntity().getPassenger();
			ItemStack itemStack = null;
			if(featureItem == null) {
				do {
					featureItem = items.get(random.nextInt(items.size()));
					itemStack = featureItem.getItemStack();
				} while(itemStack.equals(item.getItemStack()));
			} else {
				itemStack = featureItem.getItemStack();
			}
			item.setItemStack(itemStack);
		}
		hologram.setText(StringUtil.color("&b" + featureItem.getName()));
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 2) {
			if(running && !displaying) {
				if(counter <= 12) {
					hologram.setText(getName());
					++counter;
				}
			}
		} else if(ticks == 5) {
			if(running && !displaying) {
				glass.setData((byte) random.nextInt(15));
				EffectUtil.playSound(random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2, glass.getLocation());
				ParticleTypes.FIREWORK_SPARK.display(glass.getLocation().add(0, 2, 0));
				if(counter <= 12) {
					hologram.setText(getName());
					++counter;
				}
				if(counter > 12) {
					setItem();
				}
			}
		} else if(ticks == 20) {
			if(running && !displaying) {
				glass.setData((byte) random.nextInt(15));
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST) {
			Block block = event.getClickedBlock();
			Location loc = block.getLocation();
			if(loc.getBlockX() == glass.getX() && loc.getBlockY() - 1 == glass.getY() && loc.getBlockZ() == glass.getZ() && !running) {
				activate(event.getPlayer());
			}
		}
	}
}
