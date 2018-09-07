package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.KitPVPShop;
import network.server.servers.hub.items.Features;
import network.server.util.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class TankKit extends KitBase {
    public TankKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(Material.IRON_CHESTPLATE).setName("Tank Kit").setLores(new String [] {
                "",
                "&7Price: &a350",
                "",
                "&7Primary: &aStone Sword",
                "&7Chestplate: &aIron",
                "&7Other Armor: &aChain",
                ""
        }).getItemStack(), getRarity(), 350, 19);

        addItem(Material.STONE_SWORD);
        setHelmet(Material.CHAINMAIL_HELMET);
        setChestplate(Material.IRON_CHESTPLATE);
        setLeggings(Material.CHAINMAIL_LEGGINGS);
        setBoots(Material.CHAINMAIL_BOOTS);
    }

    public static Features.Rarity getRarity() {
        return Features.Rarity.UNCOMMON;
    }

    @Override
    public String getPermission() {
        return ChatColor.stripColor(KitPVPShop.getInstance().getPermission() + getName().toLowerCase());
    }
}