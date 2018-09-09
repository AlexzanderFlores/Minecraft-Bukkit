package network.gameapi.games.onevsones.kits;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.gameapi.games.onevsones.OnevsOnes;
import network.gameapi.games.onevsones.events.BattleEndEvent;
import network.gameapi.games.onevsones.events.QueueEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import network.ProPlugin;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.ConfigurationUtil;
import network.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class OneVsOneKit implements Listener {
    private static List<OneVsOneKit> kits = null;
    private static Map<String, OneVsOneKit> playersKits = null;
    private String name = null;
    private ItemStack icon = null;
    private Map<Integer, ItemStack> items = null;
    private Map<Integer, List<String>> queue = null; // <team size> <list of player names>

    public OneVsOneKit(String name, Material icon) {
        this(name, new ItemStack(icon));
    }

    public OneVsOneKit(String name, ItemStack icon) {
        this.name = name;

        ItemCreator itemCreator = new ItemCreator(icon).setAmount(0).setName("&e" + name);
        if(!icon.getType().toString().contains("SWORD")) {
        	itemCreator.addLore("");
        }
        this.icon = itemCreator.getItemStack();

        items = new HashMap<Integer, ItemStack>();

        queue = new HashMap<Integer, List<String>>();
        for(int teamSize : OnevsOnes.getTeamSizes()) {
            queue.put(teamSize, new ArrayList<String>());
        }

        if(kits == null) {
            kits = new ArrayList<OneVsOneKit>();
        }
        kits.add(this);

        if(playersKits == null) {
        	playersKits = new HashMap<String, OneVsOneKit>();
        }

        EventUtil.register(this);
    }

    public static List<OneVsOneKit> getKits() {
        return kits;
    }

    public static OneVsOneKit getKit(ItemStack item) {
        return getKit(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
    }

    public static OneVsOneKit getKit(String name) {
        for(OneVsOneKit kit : getKits()) {
            if(kit.getName().equals(name)) {
                return kit;
            }
        }
        return null;
    }

    public static void givePlayersKit(Player player, OneVsOneKit kit) {
    	playersKits.put(player.getName(), kit);
    }
    
    public static OneVsOneKit getPlayersKit(Player player) {
    	return playersKits == null ? null : playersKits.get(player.getName());
    }

    public static void removePlayerKit(Player player) {
        removePlayerKit(player.getName());
    }

    public static void removePlayerKit(String name) {
        if(playersKits != null) {
            playersKits.remove(name);
        }
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }
    
    public Map<Integer, ItemStack> getItems() {
    	return items;
    }

    public int getUsers() {
        int counter = 0;
        if(playersKits == null) {
            return counter;
        }

        for(String name : playersKits.keySet()) {
            Bukkit.getLogger().info(name + ": " + playersKits.get(name).getName());
        }

        for(OneVsOneKit kit : playersKits.values()) {
            if(kit.getName().equals(getName())) {
                ++counter;
            }
        }
        return counter;
    }

    public void setArmor(Material armor) {
        setArmor(new ItemStack(armor));
    }

    public void setArmor(ItemStack armor) {
        ArmorSlot type = null;
        String name = armor.getType().toString().split("_")[1];
        if(name.equals("HELMET")) {
            type = ArmorSlot.HELMET;
        } else if(name.equals("CHESTPLATE")) {
            type = ArmorSlot.CHESTPLATE;
        } else if(name.equals("LEGGINGS")) {
            type = ArmorSlot.LEGGINGS;
        } else if(name.equals("BOOTS")) {
            type = ArmorSlot.BOOTS;
        }
        setItem(type.getSlot(), armor);
    }

    public void setItem(int slot, Material item) {
        setItem(slot, new ItemStack(item));
    }

    public void setItem(int slot, ItemStack item) {
        items.put(slot, item);
    }
    
    public void preview(Player player) {
    	Inventory inventory = Bukkit.createInventory(player, 9 * 5, "Preview of " + getName());
    	for(int slot : items.keySet()) {
    		inventory.setItem(slot, items.get(slot));
        }
    	inventory.setItem(inventory.getSize() - 1, new ItemCreator(Material.WOOD_DOOR).setGlow(true).setName("&bBack").getItemStack());
    	//inventory.setItem(inventory.getSize() - 3, new ItemCreator(Material.NAME_TAG).setGlow(true).setName("&aEdit Kit").setLores(new String [] {"", "&7Edit your hotbar layout", ""}).getItemStack());
    	player.openInventory(inventory);
    }

    public void give(Player player) {
        give(player, true);
    }

    public void give(Player player, boolean setInMemory) {
        final String name = player.getName();
        final String kitName = getName().replace(" ", "_");
        new AsyncDelayedTask(new Runnable() {
            @Override
            public void run() {
                Player player = ProPlugin.getPlayer(name);
                if(player != null) {
                    player.getInventory().clear();
//                    boolean hotbarSetup = false;
//                    String path = HotbarEditor.getPath().replace("%", name);
//                    File file = new File(path, kitName);
//                	if(file.exists()) {
//                		hotbarSetup = true;
//                        ConfigurationUtil config = new ConfigurationUtil(path);
//                        for(String key : config.getConfig().getKeys(false)) {
//                            String item = config.getConfig().getString(key);
//                            String [] itemData = item.split(":");
//                            int id = Integer.valueOf(itemData[0]);
//                            byte data = Byte.valueOf(itemData[1]);
//                            int amount = Integer.valueOf(itemData[2]);
//                            String typeName = itemData[3];
//                            if(typeName.equals("NULL")) {
//                                ItemStack itemStack = new ItemStack(id, amount, data);
//                                if(itemData.length > 6) {
//                                    for(int a = 6; a < itemData.length; ++a) {
//                                        if(a % 2 == 0) {
//                                            Enchantment enchant = Enchantment.getByName(itemData[a]);
//                                            int level = Integer.valueOf(itemData[a + 1]);
//                                            itemStack.addEnchantment(enchant, level);
//                                        }
//                                    }
//                                }
//                                player.getInventory().setItem(Integer.valueOf(key), itemStack);
//                            } else {
//                                PotionType type = PotionType.valueOf(typeName);
//                                int level = Integer.valueOf(itemData[4]);
//                                boolean splash = itemData[5].equals("1");
//                                player.getInventory().setItem(Integer.valueOf(key), new Potion(type, level, splash).toItemStack(amount));
//                            }
//                        }
//                	}
//                    if(!hotbarSetup) {
                        for(int slot : items.keySet()) {
                            player.getInventory().setItem(slot, items.get(slot));
                        }
                        player.updateInventory();
//                    }
                }
            }
        });
        if(setInMemory) {
            playersKits.put(player.getName(), this);
        }
    }

    public List<String> getQueue(int teamSize) {
        return this.queue.get(teamSize);
    }

    public void addToQueue(Player player, int teamSize) {
        queue.get(teamSize).add(player.getName());
    }

    public void removeFromQueue(String name) {
        for(int teamSize : OnevsOnes.getTeamSizes()) {
            this.queue.get(teamSize).remove(name);
        }

        removePlayerKit(name);
    }

    @EventHandler
    public void onQueue(QueueEvent event) {
        QueueEvent.QueueAction action = event.getAction();
        int teamSize = event.getTeamSize(); // 1v1s = 1, 2v2s = 2, etc.
        Player player = event.getPlayer();
        String name = player.getName();

        if(action == QueueEvent.QueueAction.REMOVE) {
            // If they are being removed from the queue
            removeFromQueue(name);
        } else if(event.getKit().getName().equalsIgnoreCase(getName()) && action == QueueEvent.QueueAction.ADD) {
            // If they are being added to the queue for this kit

            // Give them the kit items
            give(player);

            if(AccountHandler.Ranks.VIP.hasRank(player)) {
                // Add them to the queue instantly if they're a ranked player
                addToQueue(player, teamSize);
            } else {
                // Wait 5 seconds to add them to the queue if they're a default player
                MessageHandler.sendMessage(player, "&a&l[TIP] " + AccountHandler.Ranks.VIP.getPrefix() + "&cPerk: &e5x faster queuing time &b/buy");
                new DelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        if(player.isOnline()) {
                            addToQueue(player, teamSize);
                        }
                    }
                }, 20 * 5);
            }
        }
    }

    @EventHandler
    public void onBattleEnd(BattleEndEvent event) {
        for(Player player : event.getBattle().getPlayers()) {
            removePlayerKit(player);
        }
    }

    public enum ArmorSlot {
        HELMET(39), CHESTPLATE(38), LEGGINGS(37), BOOTS(36);

        private int slot = 0;

        private ArmorSlot(int slot) {
            this.slot = slot;
        }

        public int getSlot() {
            return slot;
        }
    }
}
