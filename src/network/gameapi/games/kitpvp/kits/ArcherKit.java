package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.KitPVPShop;
import network.server.servers.hub.items.Features;
import network.server.util.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArcherKit extends KitBase {
    public ArcherKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(Material.BOW).setName("Archer Kit").setLores(new String [] {
                "",
                "&7Price: &a250",
                "",
                "&7Primary: &aWood Sword",
                "&7Secondary: &aBow & Arrow x32",
                "&7Chestplate: &aIron",
                "&7Other Armor: &aChain",
                ""
        }).getItemStack(), getRarity(), 250, 12);

        addItem(Material.WOOD_SWORD);
        addItem(Material.BOW);
        addItem(new ItemStack(Material.ARROW, 32));
        setHelmet(Material.IRON_HELMET);
        setChestplate(Material.CHAINMAIL_CHESTPLATE);
        setLeggings(Material.CHAINMAIL_LEGGINGS);
        setBoots(Material.CHAINMAIL_BOOTS);
    }

    public static Features.Rarity getRarity() {
        return Features.Rarity.COMMON;
    }

    @Override
    public String getPermission() {
        return ChatColor.stripColor(KitPVPShop.getInstance().getPermission() + getName().toLowerCase());
    }

    @Override
    public void execute(Player player) {
        player.getInventory().remove(Material.ARROW);
        super.execute(player);
    }
}