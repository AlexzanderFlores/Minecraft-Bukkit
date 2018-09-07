package network.gameapi.games.onevsones;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.gameapi.uhc.GoldenHead;
import network.player.MessageHandler;
import network.server.tasks.DelayedTask;
import network.server.util.ConfigurationUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class HotbarEditor implements Listener {
	private static String name = null;
	private static ItemStack item = null;
    private static Map<String, OneVsOneKit> kits = null;
    private static String path = null;

    public HotbarEditor() {
    	name = "Hotbar Editor";
    	item = new ItemCreator(Material.NAME_TAG).setName("&a" + name).getItemStack();
        kits = new HashMap<String, OneVsOneKit>();
        path = "/root/resources/1v1/hotbars/%/";
        EventUtil.register(this);
    }
    
    public static ItemStack getItem() {
    	return item;
    }

    public static void open(Player player, OneVsOneKit kit) {
    	if(QueueHandler.isInQueue(player) || QueueHandler.isWaitingForMap(player)) {
            QueueHandler.remove(player);
            MessageHandler.sendMessage(player, "&cYou have been removed from the queue");
        }
    	PrivateBattleHandler.removeAllInvitesFromPlayer(player);
    	Inventory inventory = Bukkit.createInventory(player, 9 * 5, "Edit " + kit.getName());
    	Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>(kit.getItems());
    	for(int slot : items.keySet()) {
    		if(slot < 9 * 4) {
    			inventory.setItem(slot, items.get(slot));
    		}
        }
    	inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WEB).setName("&eSave Kit Change").getItemStack());
		player.openInventory(inventory);
		items.clear();
		items = null;
		kits.put(player.getName(), kit);
    }
    
    public static boolean load(Player player, OneVsOneKit kit) {
    	File file = new File(path.replace("%", player.getName()) + "/" + kit.getName().replace(" ", "") + ".yml");
    	if(file.exists()) {
    		ConfigurationUtil config = new ConfigurationUtil(file.getAbsolutePath());
        	for(String slot : config.getConfig().getKeys(false)) {
        		String input = config.getConfig().getString(slot);
        		if(!input.equals("null")) {
        			String [] split = input.split(":");
            		int id = Integer.valueOf(split[0]);
            		byte data = Byte.valueOf(split[1]);
            		int amount = Integer.valueOf(split[2]);
            		String potionName = String.valueOf(split[3]);
            		int potionLevel = Integer.valueOf(split[4]);
            		boolean potionSplash = split[5].equals("1") ? true : false;
            		Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
            		if(split.length > 5) {
            			for(int a = 6; a < split.length; a += 2) {
            				Enchantment enchantment = Enchantment.getByName(split[a]);
            				int level = Integer.valueOf(split[a + 1]);
            				enchants.put(enchantment, level);
            			}
            		}
            		ItemCreator itemCreator = new ItemCreator(Material.getMaterial(id), data).setAmount(amount);
            		for(Enchantment enchantment : enchants.keySet()) {
            			itemCreator.addEnchantment(enchantment, enchants.get(enchantment));
            		}
            		if(data == -1) {
            			data = 0;
            			itemCreator.setName(GoldenHead.getName());
            		}
            		ItemStack itemStack = itemCreator.getItemStack();
            		if(!potionName.equals("NULL")) {
            			Potion potion = new Potion(PotionType.valueOf(potionName), potionLevel, potionSplash);
            			potion.apply(itemStack);
            		}
            		player.getInventory().setItem(Integer.valueOf(slot), itemStack);
        		}
        	}
        	Map<Integer, ItemStack> items = kit.getItems();
        	for(int a = 36; a <= 39; ++a) {
        		player.getInventory().setItem(a, items.get(a));
        	}
        	return true;
    	} else {
    		return false;
    	}
    }
    
    public static String getPath() {
    	return path;
    }

    private String getItemName(ItemStack item) {
        if(item != null) {
            int id = item.getTypeId();
            byte data = item.getData().getData();
            if(item.getType() == Material.GOLDEN_APPLE && (item.getAmount() == 3 || item.getAmount() == 1)) {
            	data = -1;
            }
            int amount = item.getAmount();
            String name = id + ":" + data + ":" + amount;
            if(item.getType() == Material.POTION) {
                Potion potion = Potion.fromItemStack(item);
                name += ":" + potion.getType().toString() + ":" + potion.getLevel() + ":" + (potion.isSplash() ? 1 : 0);
            } else {
                name += ":NULL:0:0";
            }
            Map<Enchantment, Integer> enchants = item.getEnchantments();
            for(Enchantment enchantment : enchants.keySet()) {
                name += ":" + enchantment.getName() + ":" + enchants.get(enchantment);
            }
            return name;
        }
        return "null";
    }
    
    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
    	Player player = event.getPlayer();
    	ItemStack item = player.getItemInHand();
    	if(item != null && item.equals(HotbarEditor.item)) {
    		player.openInventory(LobbyHandler.getKitSelectorInventory(player, name, false));
    		event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
    	Player player = event.getPlayer();
    	if(event.getInventory().getName().equals(name)) {
    		OneVsOneKit kit = OneVsOneKit.getKit(event.getItem());
    		if(kit == null) {
    			MessageHandler.sendMessage(player, "&cAn error occured when selecting kit, please try again");
    		} else {
    			HotbarEditor.open(player, kit);
    		}
    		event.setCancelled(true);
    	} else if(kits.containsKey(player.getName())) {
    		if(event.getItem().getType() == Material.WEB) {
    			String name = kits.get(player.getName()).getName();
        		File dir = new File(path.replace("%", player.getName()));
        		dir.mkdirs();
    			dir.mkdir();
                File file = new File(dir, name.replace(" ", "") + ".yml");
                if(file.exists()) {
                    MessageHandler.sendMessage(player, "&cDeleting your old hot bar set up");
                    file.delete();
                }
                try {
                	file.createNewFile();
                } catch(IOException e) {
                	e.printStackTrace();
                	MessageHandler.sendMessage(player, "&cError: &7" + e.getMessage());
                	return;
                }
                ConfigurationUtil config = new ConfigurationUtil(file.getAbsolutePath());
                Inventory inventory = event.getInventory();
                for(int a = 0; a < inventory.getSize() - 9; ++a) {
                    ItemStack item = inventory.getContents()[a];
                    if(item != null && item.getType() != Material.AIR) {
                        config.getConfig().set(a + "", getItemName(item));
                    }
                }
                config.save();
                MessageHandler.sendMessage(player, "Saving your hot bar set up for kit \"&e" + kits.get(player.getName()).getName() + "&x\"");
                event.setCancelled(true);
        		player.closeInventory();
    		}
    	}
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    	if(event.getWhoClicked() instanceof Player) {
    		Player player = (Player) event.getWhoClicked();
    		if(kits.containsKey(player.getName()) && event.getRawSlot() != event.getSlot()) {
    			MessageHandler.sendMessage(player, "&cYou cannot place items in the bottom inventory");
    			event.setCancelled(true);
    		}
    	}
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
    	if(event.getPlayer() instanceof Player) {
    		final Player player = (Player) event.getPlayer();
    		if(kits.containsKey(player.getName())) {
    			new DelayedTask(new Runnable() {
					@Override
					public void run() {
		    			LobbyHandler.giveItems(player);
					}
				});
    			kits.remove(player.getName());
    		}
    	}
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        kits.remove(event.getPlayer().getName());
    }
}
