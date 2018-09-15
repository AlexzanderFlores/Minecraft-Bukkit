package network.gameapi.games.onevsones.kits;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.gameapi.games.onevsones.HotBarEditor;
import network.gameapi.games.onevsones.OnevsOnes;
import network.gameapi.games.onevsones.events.BattleStartEvent;
import network.gameapi.games.onevsones.events.QueueEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.tasks.DelayedTask;
import network.server.util.ConfigurationUtil;
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

import network.ProPlugin;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.ItemCreator;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

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

        for(OneVsOneKit kit : playersKits.values()) {
            if(kit.getName().equals(getName())) {
                ++counter;
            }
        }
        return counter;
    }

    public boolean hasKit(Player player) {
        OneVsOneKit kit = getPlayersKit(player);
        if(kit != null && kit.getName().equalsIgnoreCase(getName())) {
            return true;
        }
        return false;
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
        String name = player.getName();
        OneVsOneKit kit = this;

        new AsyncDelayedTask(new Runnable() {
            @Override
            public void run() {
                Player player = ProPlugin.getPlayer(name);
                if(player != null) {
                    player.getInventory().clear();
                    String path = HotBarEditor.getPath(player, kit);
                    File file = new File(path);

                    if(file.exists()) {
                        ConfigurationUtil config = new ConfigurationUtil(path);
                        for(String key : config.getConfig().getKeys(false)) {
                            ItemStack itemStack = null;

                            int slot = Integer.valueOf(key);
                            String item = config.getConfig().getString(key);

                            String [] split = item.split("\\|");
                            String enchantments = split.length > 1 ? split[1] : "";
                            split = split[0].split(":");

                            Material material = Material.valueOf(split[0]);
                            int amount = Integer.valueOf(split[1]);

                            if(material == Material.POTION) {
                                PotionType type = PotionType.valueOf(split[2]);
                                int level = Integer.valueOf(split[3]);
                                boolean splash = Boolean.valueOf(split[4]);
                                Potion.Tier tier = Potion.Tier.valueOf(split[5]);
                                boolean extended = Boolean.valueOf(split[6]);

                                Potion potion = new Potion(type, level);
                                potion.setSplash(splash);
                                potion.setTier(tier);
                                if(extended) {
                                    potion.setHasExtendedDuration(extended);
                                }

                                itemStack = potion.toItemStack(amount);
                            } else {
                                byte data = Byte.valueOf(split[2]);
                                itemStack = new ItemStack(material, amount, data);

                                if(!enchantments.equalsIgnoreCase("")) {
                                    for(String enchantment : enchantments.split(",")) {
                                        split = enchantment.split(":");
                                        itemStack.addEnchantment(Enchantment.getByName(split[0]), Integer.valueOf(split[1]));
                                    }
                                }
                            }

                            player.getInventory().setItem(slot, itemStack);

                            // Set the armor
                            for(int a = 36; a <= 39; ++a) {
                                player.getInventory().setItem(a, items.get(a));
                            }
                        }
                    } else {
                        for(int slot : items.keySet()) {
                            player.getInventory().setItem(slot, items.get(slot));
                        }
                    }
                    player.updateInventory();
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

            if(AccountHandler.Ranks.PRO.hasRank(player)) {
                // Add them to the queue instantly if they're a ranked player
                addToQueue(player, teamSize);
            } else {
                // Wait 5 seconds to add them to the queue if they're a default player
                MessageHandler.sendMessage(player, "&a[TIP] " + AccountHandler.Ranks.PRO.getPrefix() + "&cPerk: &e5x faster queuing time &b/buy");
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
    public void onBattleStart(BattleStartEvent event) {
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
