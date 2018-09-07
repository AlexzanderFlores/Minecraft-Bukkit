package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.server.util.ItemCreator;
import org.bukkit.Material;

public class ArrowItemKit extends KitBase {
    private static final int PRICE = 4;
    private static final int INVENTORY_LIMIT = 32;
    private static final int QUANTITY = 4;

    public ArrowItemKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(Material.ARROW).setName("Arrow x" + QUANTITY).setLores(new String [] {
                "",
                "&7Price: &a" + PRICE,
                "&7Inventory Limit: &a" + INVENTORY_LIMIT,
                ""
        }).getItemStack(), null, PRICE, 14, INVENTORY_LIMIT, QUANTITY);
    }

    @Override
    public String getPermission() {
        return null;
    }
}