package network.gameapi.games.uhc;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import network.ProPlugin;
import network.customevents.game.GameStartEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.gameapi.uhc.scenarios.scenarios.CutClean;
import network.player.MessageHandler;
import network.server.CommandBase;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class OptionsHandler implements Listener {
	private static boolean rush = false;
	private static boolean nether = false;
	private static int appleRates = 1;
	private static boolean horses = true;
	private static boolean horseHealing = true;
	private static boolean notchApples = false;
	private static boolean strengthOne = false;
	private static boolean strengthTwo = false;
	private static boolean pearlDamage = true;
	private static boolean absorption = true;
	private static boolean crossTeaming = false;
	private static boolean end = false;
	private static String name = null;
	
	public OptionsHandler() {
		name = "Other Options";
		new CommandBase("appleRates", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					MessageHandler.sendMessage(player, "Apple Rates: &c" + getAppleRates() + "&a%");
				} else if(arguments.length == 1) {
					if(HostHandler.isHost(player.getUniqueId())) {
						try {
							appleRates = Integer.valueOf(arguments[0]);
							if(appleRates <= 0 || appleRates > 100) {
								MessageHandler.sendMessage(player, "&cInvalid Apple Rate! Select 1 - 100");
							} else {
								MessageHandler.alert("Apple Rates are now &c" + appleRates + "&a%");
							}
						} catch(NumberFormatException e) {
							return false;
						}
					} else {
						return false;
					}
				}
				return true;
			}
		};
		new CommandBase("isNetherOn") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "Nether: " + (isNetherEnabled() ? "&eYes" : "&cNo"));
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static boolean isRush() {
		return rush;
	}
	
	public static boolean isNetherEnabled() {
		return nether;
	}
	
	public static int getAppleRates() {
		return appleRates;
	}
	
	public static boolean allowHorses() {
		return horses;
	}
	
	public static boolean allowHorseHealing() {
		return horseHealing;
	}
	
	public static boolean allowNotchApples() {
		return notchApples;
	}
	
	public static boolean allowStrengthOne() {
		return strengthOne;
	}
	
	public static boolean allowStrengthTwo() {
		return strengthTwo;
	}
	
	public static boolean allowPearlDamage() {
		return pearlDamage;
	}
	
	public static boolean getAbsorption() {
		return absorption;
	}
	
	public static void setCrossTeaming(boolean crossTeaming) {
		OptionsHandler.crossTeaming = crossTeaming;
	}
	
	public static boolean getCrossTeaming() {
		return crossTeaming;
	}
	
	public static boolean getEnd() {
		return end;
	}
	
	public static boolean getNether() {
		return nether;
	}
	
	public static void open(Player player) {
		ItemStack enabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.LIME.getData()).setName("&aENABLED").addLore("&fClick the icon above to toggle").getItemStack();
		ItemStack disabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setName("&cDISABLED").addLore("&fClick the icon above to toggle").getItemStack();
		ItemStack back = new ItemCreator(Material.ARROW).setName("&eBack").getItemStack();
		ItemStack finish = new ItemCreator(Material.ARROW).setName("&eFinish Set Up").getItemStack();
		Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
		ItemStack [] items = new ItemStack [] {
			new ItemCreator(Material.DIAMOND_BOOTS).setName("&bRush").getItemStack(),
			new ItemCreator(Material.NETHERRACK).setName("&bNether Enabled").getItemStack(),
			new ItemCreator(Material.APPLE).setName("&bApple Rates").addLore("&4NOTE: &fEdit this with:")
			.addLore("&f/appleRates [percentage]").addLore("").addLore("&aCurrent Rates: &c" + getAppleRates() + "&a%").getItemStack(),
			new ItemCreator(Material.SADDLE).setName("&bHorses").getItemStack(),
			new ItemCreator(Material.BREAD).setName("&bHorse Healing").getItemStack(),
			new ItemCreator(Material.GOLDEN_APPLE, 1).setName("&bNotch Apples").getItemStack(),
			new ItemCreator(Material.DIAMOND_SWORD).setName("&bStrength I").getItemStack(),
			new ItemCreator(Material.DIAMOND_SWORD).setAmount(2).setName("&bStrength II").getItemStack(),
			new ItemCreator(Material.ENDER_PEARL).setName("&bEnder Pearl Damage").getItemStack(),
			new ItemCreator(Material.GOLDEN_APPLE).setName("&bAbsorption").getItemStack(),
			new ItemCreator(Material.ENDER_STONE).setName("&bEnd Enabled").getItemStack()
		};
		boolean [] states = new boolean [] {isRush(), isNetherEnabled(), false, allowHorses(), allowHorseHealing(), allowNotchApples(), allowStrengthOne(),
			allowStrengthTwo(), allowPearlDamage(), getAbsorption(), getEnd()
		};
		int [] slots = new int [] {10, 11, 12, 13, 14, 15, 16, 28, 29, 30, 31};
		for(int a = 0; a < items.length; ++a) {
			inventory.setItem(slots[a], items[a]);
			if(states[a]) {
				inventory.setItem(slots[a] + 9, enabled);
			} else {
				inventory.setItem(slots[a] + 9, disabled);
			}
		}
		inventory.setItem(inventory.getSize() - 9, back);
		inventory.setItem(inventory.getSize() - 1, finish);
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			Material type = item.getType();
			if(type == Material.ARROW) {
				String name = event.getItemTitle();
				if(name.contains("Back")) {
					TeamHandler.open(player);
				} else {
					new TweetHandler();
					player.closeInventory();
				}
			} else if(type == Material.DIAMOND_BOOTS) {
				rush = !rush;
				open(player);
			} else if(type == Material.NETHERRACK) {
				nether = !nether;
				if(nether) {
					strengthOne = true;
					strengthTwo = true;
					WorldHandler.generateNether();
				} else {
					strengthOne = false;
					strengthTwo = false;
				}
				open(player);
			} else if(type == Material.APPLE) {
				open(player);
			} else if(type == Material.SADDLE) {
				horses = !horses;
				if(!horses) {
					horseHealing = false;
				}
				open(player);
			} else if(type == Material.BREAD) {
				if(horses) {
					horseHealing = !horseHealing;
				} else {
					horseHealing = false;
				}
				open(player);
			} else if(type == Material.GOLDEN_APPLE) {
				if(item.getData().getData() == 1) {
					notchApples = !notchApples;
				} else {
					absorption = !absorption;
				}
				open(player);
			} else if(type == Material.DIAMOND_SWORD) {
				if(item.getAmount() == 1) {
					strengthOne = !strengthOne;
				} else if(item.getAmount() == 2) {
					strengthTwo = !strengthTwo;
				}
				if(!nether) {
					strengthOne = false;
					strengthTwo = false;
				}
				open(player);
			} else if(type == Material.ENDER_PEARL) {
				pearlDamage = !pearlDamage;
				open(player);
			} else if(type == Material.ENDER_STONE) {
				end = !end;
				if(end) {
					WorldHandler.generateEnd();
				}
				open(player);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.getEntityType() == EntityType.HORSE && !allowHorses()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if(event.getEntity() instanceof Horse && !allowHorseHealing()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		ItemStack item = event.getCurrentItem();
		if(event.getWhoClicked() instanceof Player && item.getType() == Material.GOLDEN_APPLE && item.getData().getData() == 1 && !allowNotchApples()) {
			Player player = (Player) event.getWhoClicked();
			player.closeInventory();
			MessageHandler.sendMessage(player, "&cYou may not craft that item");
			event.setCurrentItem(new ItemStack(Material.AIR));
			event.setResult(Result.DENY);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(!allowPearlDamage() && event.getCause() == TeleportCause.ENDER_PEARL) {
			event.getPlayer().teleport(event.getTo());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if(!getAbsorption() && event.getItem().getType() == Material.GOLDEN_APPLE) {
			final String name = event.getPlayer().getName();
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(name);
					if(player != null) {
						player.removePotionEffect(PotionEffectType.ABSORPTION);
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		if(ScenarioManager.getActiveScenarios().contains(CutClean.getInstance())) {
			appleRates = 4;
		}
		if(!allowHorses()) {
			for(Entity entity : Bukkit.getWorlds().get(1).getEntities()) {
				if(entity instanceof Horse) {
					entity.remove();
				}
			}
		}
	}
}
