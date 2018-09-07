package network.gameapi.games.uhcskywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.customevents.TimeEvent;
import network.customevents.game.GameDeathEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerOpenNewChestEvent;
import network.gameapi.MiniGame.GameStates;
import network.gameapi.SpectatorHandler;
import network.player.TitleDisplayer;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;

public class ChestHandler implements Listener {
	private static List<Block> oppenedChests = null;
	private static Map<ItemStack, Rarity> possibleItems = null;
	private List<Material> giveOnce = null;
	private Map<String, Integer> chestsOpened = null;
	private Map<String, List<Material>> alreadyGotten = null;
	public enum Rarity {COMMON, UNCOMMON, RARE}
	private enum ArmorSlot {HELMET, CHESTPLATE, LEGGINGS, BOOTS}
	private static int restockCounter = 60 * 5;
	
	public ChestHandler() {
		oppenedChests = new ArrayList<Block>();
		possibleItems = new HashMap<ItemStack, Rarity>();
		giveOnce = new ArrayList<Material>();
		chestsOpened = new HashMap<String, Integer>();
		alreadyGotten = new HashMap<String, List<Material>>();
		
		giveOnce.add(Material.COOKED_BEEF);
		giveOnce.add(Material.GRILLED_PORK);
		giveOnce.add(Material.STONE_SWORD);
		giveOnce.add(Material.STONE_PICKAXE);
		giveOnce.add(Material.FISHING_ROD);
		giveOnce.add(Material.LAVA_BUCKET);
		giveOnce.add(Material.IRON_SWORD);
		giveOnce.add(Material.IRON_PICKAXE);
		giveOnce.add(Material.IRON_AXE);
		giveOnce.add(Material.DIAMOND_SWORD);
		giveOnce.add(Material.DIAMOND_PICKAXE);
		giveOnce.add(Material.DIAMOND_AXE);
		
		addItem(Material.COOKED_BEEF, Rarity.COMMON);
		addItem(Material.GRILLED_PORK, Rarity.COMMON);
		addItem(Material.STONE_SWORD, Rarity.COMMON);
		addItem(Material.IRON_SWORD, Rarity.COMMON);
		addItem(Material.DIAMOND_AXE, Rarity.COMMON);
		addItem(Material.STONE_PICKAXE, Rarity.COMMON);
		addItem(Material.WOOD, 32, Rarity.COMMON);
		addItem(Material.EXP_BOTTLE, 5, Rarity.COMMON);
		addItem(Material.EXP_BOTTLE, 10, Rarity.COMMON);
		addItem(Material.COBBLESTONE, 32, Rarity.COMMON);
		addItem(Material.IRON_BOOTS, Rarity.COMMON);
		addItem(Material.IRON_LEGGINGS, Rarity.COMMON);
		addItem(Material.IRON_HELMET, Rarity.COMMON);
		
		addItem(Material.GOLD_INGOT, 4, Rarity.UNCOMMON);
		addItem(Material.FISHING_ROD, Rarity.UNCOMMON);
		addItem(Material.WATER_BUCKET, Rarity.UNCOMMON);
		addItem(Material.LAVA_BUCKET, Rarity.UNCOMMON);
		addItem(Material.EGG, 16, Rarity.UNCOMMON);
		addItem(Material.SNOW_BALL, 16, Rarity.UNCOMMON);
		addItem(Material.ARROW, Rarity.UNCOMMON);
		addItem(Material.BOW, Rarity.UNCOMMON);
		addItem(Material.IRON_PICKAXE, Rarity.UNCOMMON);
		addItem(Material.IRON_CHESTPLATE, Rarity.UNCOMMON);
		addItem(Material.DIAMOND_BOOTS, Rarity.UNCOMMON);
		addItem(Material.DIAMOND_HELMET, Rarity.UNCOMMON);

		addItem(Material.DIAMOND_PICKAXE, Rarity.RARE);
		addItem(Material.FLINT_AND_STEEL, Rarity.RARE);
		addItem(Material.GOLDEN_APPLE, Rarity.RARE);
		addItem(Material.DIAMOND_SWORD, Rarity.RARE);
		addItem(Material.ENDER_PEARL, Rarity.RARE);
		addItem(Material.DIAMOND_LEGGINGS, Rarity.RARE);
		addItem(Material.DIAMOND_CHESTPLATE, Rarity.RARE);
		addItem(Material.ENCHANTMENT_TABLE, Rarity.RARE);
		addItem(Material.ANVIL, Rarity.RARE);
		
		EventUtil.register(this);
	}
	
	public static void restock(Block block) {
		oppenedChests.remove(block);
	}
	
	public static void addItem(Material material, Rarity rarity) {
		addItem(material, 1, rarity);
	}
	
	public static void addItem(Material material, int amount, Rarity rarity) {
		addItem(material, amount, 0, rarity);
	}
	
	public static void addItem(Material material, int amount, int data, Rarity rarity) {
		addItem(new ItemStack(material, amount, (byte) data), rarity);
	}
	
	public static void addItem(ItemStack itemStack, Rarity rarity) {
		possibleItems.put(itemStack, rarity);
	}
	
	private boolean isArmor(ItemStack item) {
		String type = item.getType().toString();
		return type.contains("HELMET") || type.contains("CHESTPLATE") || type.contains("LEGGINGS") || type.contains("BOOTS");
	}
	
	private ArmorSlot getArmorSlot(ItemStack item) {
		String [] split = item.getType().toString().split("_");
		if(split.length == 2) {
			return ArmorSlot.valueOf(split[1]);
		} else {
			return null;
		}
	}
	
	private Material getArmor(ArmorSlot armorSlot, Material type) {
		String typeName = type.toString().split("_")[0];
		return Material.valueOf(typeName + "_" + armorSlot);
	}
	
	public static int getRestockCounter() {
		return restockCounter;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && Network.getMiniGame().getGameState() == GameStates.STARTED) {
			if(--restockCounter <= 0) {
				TimeEvent.getHandlerList().unregister(this);
				oppenedChests.clear();
				for(Player player : Bukkit.getOnlinePlayers()) {
					new TitleDisplayer(player, "&eChests Restocked").display();
					EffectUtil.playSound(player, Sound.CHEST_OPEN);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(oppenedChests != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && !SpectatorHandler.contains(event.getPlayer())) {
			Block block = event.getClickedBlock();
			if((block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) && !oppenedChests.contains(block)) {
				Player player = event.getPlayer();
				Chest chest = (Chest) block.getState();
				oppenedChests.add(block);
				chest.getInventory().clear();
				Random random = new Random();
				int numberOfTimes = random.nextInt(5) + 2;
				List<ItemStack> items = new ArrayList<ItemStack>(possibleItems.keySet());
				List<Material> gotten = new ArrayList<Material>();
				for(int a = 0; a < numberOfTimes; ++a) {
					ItemStack itemStack = null;
					Material type = null;
					int chance = random.nextInt(100) + 1;
					Rarity rarity = chance <= 10 ? Rarity.RARE : chance <= 35 ? Rarity.UNCOMMON : Rarity.COMMON;
					do {
						do {
							do {
								itemStack = items.get(random.nextInt(possibleItems.size()));
							} while(possibleItems.get(itemStack) != rarity);
							if(isArmor(itemStack)) {
								for(ItemStack armor : player.getInventory().getArmorContents()) {
									if(armor == null || armor.getType() == Material.AIR) {
										if(random.nextBoolean()) {
											type = Material.IRON_HELMET;
										} else {
											type = Material.DIAMOND_HELMET;
										}
										ArmorSlot armorSlot = getArmorSlot(armor);
										if(armorSlot != null) {
											itemStack.setType(getArmor(armorSlot, type));
										}
									}
								}
							}
							type = itemStack.getType();
						} while(gotten.contains(type));
						if((itemStack.getEnchantments() == null || itemStack.getEnchantments().isEmpty()) && giveOnce.contains(type)) {
							List<Material> got = alreadyGotten.get(player.getName());
							if(got == null) {
								got = new ArrayList<Material>();
								alreadyGotten.put(player.getName(), got);
							}
							if(!got.contains(type)) {
								got.add(type);
								alreadyGotten.put(player.getName(), got);
								break;
							}
						} else {
							break;
						}
					} while(true);
					if(itemStack.getAmount() == 1 && (type == Material.ARROW || type.isEdible())) {
						if(type == Material.ARROW) {
							itemStack = new ItemStack(type, random.nextInt(10) + 10);
							if(!chest.getInventory().contains(Material.BOW)) {
								chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.BOW));
							}
						} else if(type != Material.GOLDEN_APPLE) {
							itemStack = new ItemStack(type, random.nextInt(5) + 5);
						}
					} else if(type == Material.ENDER_PEARL) {
						itemStack = new ItemStack(type, random.nextInt(3) + 1);
					}
					gotten.add(type);
					if(itemStack.getType() == Material.BOW && !chest.getInventory().contains(Material.ARROW)) {
						chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.ARROW, random.nextInt(10) + 10));
					}
					int slot = 0;
					do {
						slot = random.nextInt(chest.getInventory().getSize());
					} while(chest.getInventory().getItem(slot) != null);
					chest.getInventory().setItem(slot, itemStack);
				}
				items.clear();
				items = null;
				gotten.clear();
				gotten = null;
				int counter = 0;
				if(chestsOpened.containsKey(player.getName())) {
					counter = chestsOpened.get(player.getName());
				}
				chestsOpened.put(player.getName(), ++counter);
				if(counter == 1) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.STONE_PICKAXE));
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.STONE_SWORD));
				} else if(counter == 2) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.WOOD, 32));
				} else if(counter == 3) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.SNOW_BALL, 16));
				}
				Bukkit.getPluginManager().callEvent(new PlayerOpenNewChestEvent(player, chest));
			}
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Material type = event.getEntity().getItemStack().getType();
		if(type == Material.CHEST || type == Material.TRAPPED_CHEST) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Material type = event.getBlock().getType();
		if(type == Material.CHEST || type == Material.TRAPPED_CHEST) {
			Player player = event.getPlayer();
			player.setItemInHand(new ItemStack(Material.AIR));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		remove(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
	
	private void remove(Player player) {
		chestsOpened.remove(player.getName());
		if(alreadyGotten.containsKey(player.getName())) {
			alreadyGotten.get(player.getName()).clear();
			alreadyGotten.remove(player.getName());
		}
	}
}