package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.KitPVPShop;
import network.server.servers.hub.items.Features;
import network.server.util.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class VoterKit extends KitBase {
    public VoterKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(Material.IRON_CHESTPLATE).setName("Voter Kit").setLores(new String [] {
                "",
                "&7Price: &a500",
                "&7Required Monthly Votes: &a5",
                "",
                "&7Primary: &aStone Sword",
                "&7Helmet & Chestplate: &aIron",
                "&7Leggings & Boots: &aChain",
                ""
        }).getItemStack(), getRarity(), 500, 20);

        addItem(Material.STONE_SWORD);
        setHelmet(Material.IRON_HELMET);
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