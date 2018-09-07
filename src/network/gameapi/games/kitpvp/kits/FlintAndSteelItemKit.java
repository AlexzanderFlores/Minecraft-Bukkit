package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.server.util.ItemCreator;
import org.bukkit.Material;

public class FlintAndSteelItemKit extends KitBase {
    private static final int PRICE = 2;
    private static final int INVENTORY_LIMIT = 1;

    public FlintAndSteelItemKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(Material.FLINT_AND_STEEL).setName("Flint and Steel").setLores(new String [] {
                "",
                "&7Price: &a" + PRICE,
                "&7Inventory Limit: &a" + INVENTORY_LIMIT,
                "&7Item Uses: &a2",
                "&7Fire Lasts: &a5s",
                ""
        }).getItemStack(), null, PRICE, 16, INVENTORY_LIMIT);
    }

    @Override
    public String getPermission() {
        return null;
    }
}