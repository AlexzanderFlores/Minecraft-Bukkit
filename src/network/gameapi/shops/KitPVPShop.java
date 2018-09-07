package network.gameapi.shops;

import network.Network;
import network.customevents.player.InventoryItemClickEvent;
import network.gameapi.SpectatorHandler;
import network.gameapi.games.kitpvp.kits.*;
import network.gameapi.kit.KitBase;
import network.server.DB;
import network.server.util.EffectUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;

public class KitPVPShop extends ShopBase {
    private static KitPVPShop instance = null;

    public KitPVPShop() {
        super("Shop - Kit PVP", "kit.kit_pvp", DB.PLAYERS_COINS_KIT_PVP, Network.Plugins.KITPVP, 1, 40);
        instance = this;

        new DefaultKit();
        new WarriorKit();
        new ArcherKit();
        new TankKit();
        new VoterKit();

        new ArrowItemKit();
        new FishingRodItemKit();
        new FlintAndSteelItemKit();
        new HealthPotionItemKit();
        new GoldenAppleItemKit();
    }

    public static KitPVPShop getInstance() {
        if(instance == null) {
            new KitPVPShop();
        }
        return instance;
    }

    @Override
    public void openShop(Player player, int page) {
        if(SpectatorHandler.contains(player)) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(player, 9 * 5, getName());

        for(KitBase kit : KitBase.getKits()) {
            if(kit.getPluginData().equals(Network.Plugins.KITPVP.getData())) {
                inventory.setItem(kit.getSlot(), kit.getIcon(player));
            }
        }

//        inventory.setItem(25, new ItemCreator(Material.ENCHANTMENT_TABLE).setName("&bEnchant an Item").setLores(new String [] {
//                "", "&7Price: &a30", "&7Costs &a1 Level", "",
//                "&7Possible Enchantments:", "&bProtection 1", "&bSharpness 1", "", "&cEnchantments reset on death.", ""
//        }).getItemStack());

        player.openInventory(inventory);
        super.openShop(player, page);
    }

    @Override
    public void updateInfoItem(Player player) {
    }

    @Override
    public void updateInfoItem(Player player, Inventory inventory) {
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if(event.getTitle().equals(getName())) {
            event.setCancelled(true);
            Player player = event.getPlayer();

            for(KitBase kit : KitBase.getKits()) {
                String name = ChatColor.stripColor(event.getItemTitle());
                if(kit.getPluginData().equals(Network.Plugins.KITPVP.getData()) && name.startsWith(kit.getName()) && kit.getSlot() == event.getSlot()) {
                    if(!kit.use(player)) {
                        EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
                    }
                }
            }
        }
    }
}
