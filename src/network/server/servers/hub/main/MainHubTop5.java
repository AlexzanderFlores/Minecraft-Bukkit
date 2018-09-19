package network.server.servers.hub.main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.inventivegames.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import network.customevents.TimeEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.PlaytimeTracker;
import network.player.account.PlaytimeTracker.TimeType;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.StringUtil;
import npc.NPCEntity;

@SuppressWarnings("unused")
public class MainHubTop5 implements Listener {
	private static List<Hologram> stands = null;
	private static Hologram display = null;
	private static Hologram bar = null;
	private static int counter = 0;
	private static int infoCounter = 0;
	private static final int delay = 5;
	private static boolean delayed = false;
	private static String name = null;
	private static Information last = null;
	
	private enum Information {
		LIFETIME_PLAYTIME("Lifetime Playtime", 10, Material.WATCH, 3, new Runnable() {
			@Override
			public void run() {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						setNames(PlaytimeTracker.getTop5(TimeType.LIFETIME));
						setDisplay("Lifetime Playtime");
						counter = 0;
						last = Information.LIFETIME_PLAYTIME;
					}
				});
			}
		}),
		MONTHLY_PLAYTIME("Monthly Playtime", 11, Material.WATCH, 2, new Runnable() {
			@Override
			public void run() {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						setNames(PlaytimeTracker.getTop5(TimeType.MONTHLY));
						setDisplay("Monthly Playtime");
						counter = 0;
						last = Information.MONTHLY_PLAYTIME;
					}
				});
			}
		}),
		WEEKLY_PLAYTIME("Weekly Playtime", 12, Material.WATCH, new Runnable() {
			@Override
			public void run() {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						setNames(PlaytimeTracker.getTop5(TimeType.WEEKLY));
						setDisplay("Weekly Playtime");
						counter = 0;
						last = Information.WEEKLY_PLAYTIME;
					}
				});
			}
		}),
		LIFETIME_VOTES("Lifetime Votes", 14, Material.NAME_TAG, 3, new Runnable() {
			@Override
			public void run() {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						List<String> names = new ArrayList<String>();
						for(String uuidString : DB.PLAYERS_LIFETIME_VOTES.getOrdered("amount", "uuid", 5, true)) {
							UUID uuid = UUID.fromString(uuidString);
							names.add(AccountHandler.getName(uuid));
						}
						setNames(names);
						names.clear();
						names = null;
						setDisplay("Lifetime Votes");
						counter = 0;
						last = Information.LIFETIME_VOTES;
					}
				});
			}
		}),
		MONTHLY_VOTES("Monthly Votes", 15, Material.NAME_TAG, 2, new Runnable() {
			@Override
			public void run() {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						List<String> names = new ArrayList<String>();
						for(String uuidString : DB.PLAYERS_MONTHLY_VOTES.getOrdered("amount", "uuid", 5, true)) {
							UUID uuid = UUID.fromString(uuidString);
							names.add(AccountHandler.getName(uuid));
						}
						setNames(names);
						names.clear();
						names = null;
						setDisplay("Monthly Votes");
						counter = 0;
						last = Information.MONTHLY_VOTES;
					}
				});
			}
		}),
		WEEKLY_VOTES("Weekly Votes", 16, Material.NAME_TAG, new Runnable() {
			public void run() {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						List<String> names = new ArrayList<String>();
						for(String uuidString : DB.PLAYERS_WEEKLY_VOTES.getOrdered("amount", "uuid", 5, true)) {
							UUID uuid = UUID.fromString(uuidString);
							names.add(AccountHandler.getName(uuid));
						}
						setNames(names);
						names.clear();
						names = null;
						setDisplay("Weekly Votes");
						counter = 0;
						last = Information.WEEKLY_VOTES;
					}
				});
			}
		})
		
		;
		
		private String name = null;
		private ItemStack itemStack = null;
		private Runnable runnable = null;
		private int slot = 0;
		
		private Information(String name, int slot, Material material, Runnable runnable) {
			this(name, slot, new ItemStack(material), runnable);
		}
		
		private Information(String name, int slot, Material material, int amount, Runnable runnable) {
			this(name, slot, new ItemStack(material, amount), runnable);
		}
		
		private Information(String name, int slot, ItemStack itemStack, Runnable runnable) {
			this.name = name;
			this.itemStack = itemStack;
			this.runnable = runnable;
			this.slot = slot;
		}
		
		public ItemStack getItemStack() {
			return new ItemCreator(this.itemStack.clone()).setName(StringUtil.color("&b" + this.name)).getItemStack();
		}
		
		public int getSlot() {
			return this.slot;
		}
		
		public void run() {
			this.runnable.run();
		}
	}
	
	public MainHubTop5() {
		stands = new ArrayList<Hologram>();
		World world = Bukkit.getWorlds().get(0);
		double x = 1714.5;
		double [] y = new double [] {7, 6, 6, 5, 5};
		double [] z = new double [] {-1280.5, -1281.5, -1279.5, -1282.5, -1278.5};
		String [] places = new String [] {"&91st", "&c2nd", "&f3rd", "&e4th", "&25th"};
		Color [] colors = new Color [] {Color.fromRGB(20, 90, 150), Color.fromRGB(227, 50, 50), Color.fromRGB(255, 255, 255), Color.fromRGB(225, 220, 25), Color.fromRGB(20, 150, 60)};
		for(int a = 0; a < y.length; ++a) {
			Location location = new Location(world, x, y[a], z[a], -270.0f, 0.0f);
			/*ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
			armorStand.setBasePlate(false);
			armorStand.setHelmet(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_HELMET), colors[a]));
			armorStand.setChestplate(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), colors[a]));
			armorStand.setLeggings(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), colors[a]));
			armorStand.setBoots(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_BOOTS), colors[a]));
			stands._add(armorStand);
			armorStand = (ArmorStand) world.spawnEntity(location._add(0, 0.3, 0), EntityType.ARMOR_STAND);
			armorStand.setGravity(false);
			armorStand.setVisible(false);
			armorStand.setCustomName(StringUtil.color(places[a]));
			armorStand.setCustomNameVisible(true);*/
		}
		Location location = new Location(world, 1711.5, 4, -1280.5, -270.0f, 0.0f);
		/*display = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
		display.setGravity(false);
		display.setVisible(false);
		bar = (ArmorStand) world.spawnEntity(location._add(0, -0.35, 0), EntityType.ARMOR_STAND);
		bar.setGravity(false);
		bar.setVisible(false);*/
		setNext();
		name = "Select Top 5 Type";
		new NPCEntity(EntityType.VILLAGER, "&e&k" + name, new Location(world, 1712.5, 5, -1283.5)) {
			@Override
			public void onInteract(Player player) {
				EffectUtil.playSound(player, Sound.VILLAGER_IDLE);
				MessageHandler.sendMessage(player, "&cComing soon");
				/*Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
				for(Information info : Information.values()) {
					inventory.setItem(info.getSlot(), info.getItemStack());
				}
				player.openInventory(inventory);*/
			}
		};
		EventUtil.register(this);
	}
	
	private void setNext() {
		Information.values()[infoCounter].run();
		counter = 0;
		if(++infoCounter >= Information.values().length) {
			infoCounter = 0;
		}
	}
	
	public static void setDisplay(String name) {
		/*display.setCustomName(StringUtil.color(name));
		display.setCustomNameVisible(true);*/
	}
	
	public static void setNames(String [] names) {
		List<String> nameList = new ArrayList<String>();
		for(String name : names) {
			nameList.add(name);
		}
		setNames(nameList);
		nameList.clear();
		nameList = null;
	}
	
	public static void setNames(List<String> names) {
		if(names.isEmpty() || names.size() < 5) {
			for(int a = names.size(); a <= 5; ++a) {
				names.add("None");
			}
		}
		for(int a = 0; a < 5 && a < names.size(); ++a) {
			/*stands.get(a).setCustomName(StringUtil.color(names.get(a)));
			stands.get(a).setCustomNameVisible(true);
			stands.get(a).setHelmet(ItemUtil.getSkull(names.get(a)));*/
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(!Bukkit.getOnlinePlayers().isEmpty()) {
				if(counter >= 10) {
					setNext();
				}
				String text = "&4";
				int size = 0;
				for(int a = 0; a <= counter; ++a, ++size) {
					text += "|";
				}
				text += "&7";
				for(; size < 10; ++size) {
					text += "|";
				}
				/*bar.setCustomName(StringUtil.color(text));
				bar.setCustomNameVisible(true);*/
				++counter;
			}
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			if(delayed) {
				MessageHandler.sendMessage(player, "&cCan only be used once every &e" + delay + " &cseconds");
			} else {
				delayed = true;
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						delayed = false;
					}
				}, 20 * delay);
				ItemStack itemStack = event.getItem();
				for(Information info : Information.values()) {
					if(info.getItemStack().equals(itemStack)) {
						if(info == last) {
							MessageHandler.sendMessage(player, "&cThis &eTop 5 &cis being displayed already");
						} else {
							info.run();
						}
						break;
					}
				}
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
