package network.gameapi.games.kitpvp.kits;

import network.Network;
import network.gameapi.kit.KitBase;
import network.server.util.ItemCreator;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class HealthPotionItemKit extends KitBase {
    private static final int PRICE = 15;
    private static final int INVENTORY_LIMIT = 3;

    public HealthPotionItemKit() {
        super(Network.Plugins.KITPVP, new ItemCreator(new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(1)).setName("Health Potion").setLores(new String [] {
                "",
                "&7Price: &a" + PRICE,
                "&7Inventory Limit: &a" + INVENTORY_LIMIT,
                ""
        }).getItemStack(), null, PRICE, 23, INVENTORY_LIMIT);
    }

    @Override
    public String getPermission() {
        return null;
    }
}