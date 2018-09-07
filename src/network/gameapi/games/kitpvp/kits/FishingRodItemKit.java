package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.server.util.ItemCreator;
import org.bukkit.Material;

// TODO: Make this require 1 weekly vote
public class FishingRodItemKit extends KitBase {
    private static final int PRICE = 2;
    private static final int INVENTORY_LIMIT = 1;

    public FishingRodItemKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(Material.FISHING_ROD).setName("Fishing Rod").setLores(new String [] {
                "",
                "&7Price: &a" + PRICE,
                "&7Inventory Limit: &a" + INVENTORY_LIMIT,
                ""
        }).getItemStack(), null, PRICE, 15, INVENTORY_LIMIT);
    }

    @Override
    public String getPermission() {
        return null;
    }
}