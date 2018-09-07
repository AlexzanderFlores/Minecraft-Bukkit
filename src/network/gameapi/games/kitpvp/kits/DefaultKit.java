package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.KitPVPShop;
import network.server.util.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DefaultKit extends KitBase {
    public DefaultKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(Material.STONE_SWORD).setName("Default Kit").setLores(new String [] {
                "",
                "&7Price: &a0",
                "",
                "&7Primary: &aStone Sword",
                "&7Armor: &aChain",
                ""
        }).getItemStack(), null, 0, 10);

        addItem(Material.STONE_SWORD);
        setHelmet(Material.CHAINMAIL_HELMET);
        setChestplate(Material.CHAINMAIL_CHESTPLATE);
        setLeggings(Material.CHAINMAIL_LEGGINGS);
        setBoots(Material.CHAINMAIL_BOOTS);
    }

    @Override
    public String getPermission() {
        return ChatColor.stripColor(KitPVPShop.getInstance().getPermission() + getName().toLowerCase());
    }

    @Override
    public boolean owns(Player player) {
        return true;
    }
}
