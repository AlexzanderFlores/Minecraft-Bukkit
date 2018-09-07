package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.server.util.ItemCreator;
import org.bukkit.Material;

public class GoldenAppleItemKit extends KitBase {
    private static final int PRICE = 15;
    private static final int INVENTORY_LIMIT = 3;

    public GoldenAppleItemKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(Material.GOLDEN_APPLE).setName("Golden Apple").setLores(new String [] {
                "",
                "&7Price: &a" + PRICE,
                "&7Inventory Limit: &a" + INVENTORY_LIMIT,
                ""
        }).getItemStack(), null, PRICE, 24, INVENTORY_LIMIT);
    }

    @Override
    public String getPermission() {
        return null;
    }
}