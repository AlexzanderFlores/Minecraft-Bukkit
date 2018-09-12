package network.gameapi.games.onevsones;

import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.games.onevsones.events.QuitCommandEvent;
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.player.MessageHandler;
import network.server.CommandBase;
import network.server.util.ConfigurationUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HotBarEditor implements Listener {
    private static String name = null;
    private static ItemStack item = null;
    private static List<String> players = null;

    public HotBarEditor() {
        name = "Hot Bar Editor";
        item = new ItemCreator(Material.NAME_TAG).setName("&a" + name).getItemStack();
        players = new ArrayList<String>();

        new CommandBase("saveHotBar", true) {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                Player player = (Player) sender;

                if(players.contains(player.getName())) {
                    OneVsOneKit kit = OneVsOneKit.getPlayersKit(player);
                    ConfigurationUtil config = new ConfigurationUtil(getPath(player, kit));

                    for(int a = 0; a < 36; ++a) {
                        ItemStack item = player.getInventory().getItem(a);
                        if(item != null) {
                            String enchantmentString = "";
                            Map<Enchantment, Integer> enchantments = item.getEnchantments();

                            for(Enchantment enchantment : enchantments.keySet()) {
                                enchantmentString += enchantment.getName() + ":" + enchantments.get(enchantment) + ",";
                            }
                            if(!enchantmentString.equalsIgnoreCase("")) {
                                // Add spacer and remove final comma
                                enchantmentString = "|" + enchantmentString.substring(0, enchantmentString.length() - 1);
                            }

                            String line = "";

                            if(item.getType() == Material.POTION) {
                                Potion potion = Potion.fromItemStack(item);
                                //POTION:1:type:level:splash:tier:extended_duration
                                line = item.getType().toString() + ":" + item.getAmount() + ":" + potion.getType().toString() + ":" + potion.getLevel() + ":" + potion.isSplash() + ":" + potion.getTier().name() + ":" + potion.hasExtendedDuration() + enchantmentString;
                            } else {
                                line = item.getType().toString() + ":" + item.getAmount() + ":" + item.getData().getData() + enchantmentString;
                            }

                            config.getConfig().set(String.valueOf(a), line);
                        }
                    }

                    if(config.save()) {
                        MessageHandler.sendMessage(player, "Your hot bar was saved successfully!");
                    } else {
                        MessageHandler.sendMessage(player, "&cThere was an error while saving your hot bar, please report this");
                    }

                    players.remove(player.getName());
                    LobbyHandler.spawn(player);
                } else {
                    MessageHandler.sendMessage(player, "&cYou must be editing your hot bar setup to use this command");
                }

                return true;
            }
        };

        EventUtil.register(this);
    }

    public static ItemStack getItem() {
        return item;
    }

    public static String getPath(Player player, OneVsOneKit kit) {
        return "./plugins/Core/hotbars/" + player.getUniqueId() + "-" + kit.getName().replace(" ", "-") + ".yml";
    }

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        Player player = event.getPlayer();

        if(player.getItemInHand().equals(item)) {
            Inventory inventory = LobbyHandler.getKitSelectorInventory(player, name, false);
            player.openInventory(inventory);

            players.add(player.getName());
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if(event.getTitle().equalsIgnoreCase(name)) {
            Player player = event.getPlayer();

            OneVsOneKit kit = OneVsOneKit.getKit(event.getItem());
            kit.give(player);

            MessageHandler.sendMessage(player, "");
            MessageHandler.sendMessage(player, "Edit your item locations and then run &b/saveHotBar");
            MessageHandler.sendMessage(player, "To ignore changes simply run &b/quit");
            MessageHandler.sendMessage(player, "");

            player.closeInventory();
            player.getInventory().setHeldItemSlot(0);
            players.add(player.getName());

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getInventory().getTitle().equalsIgnoreCase(name)) {
            players.remove(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onQuitCommand(QuitCommandEvent event) {
        Player player = event.getPlayer();

        if(players.contains(player.getName())) {
            players.remove(player.getName());
            MessageHandler.sendMessage(player, "&cHot Bar changed not saved");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        players.remove(event.getPlayer().getName());
    }
}
