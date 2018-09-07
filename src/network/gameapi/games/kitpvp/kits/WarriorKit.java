package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.KitPVPShop;
import network.server.servers.hub.items.Features;
import network.server.util.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class WarriorKit extends KitBase {
    public WarriorKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(Material.IRON_HELMET).setName("Warrior Kit").setLores(new String [] {
                "",
                "&7Price: &a250",
                "",
                "&7Primary: &aStone Sword",
                "&7Helmet: &aIron",
                "&7Other Armor: &aChain",
                ""
        }).getItemStack(), getRarity(), 250, 11);

        addItem(Material.STONE_SWORD);
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
}
