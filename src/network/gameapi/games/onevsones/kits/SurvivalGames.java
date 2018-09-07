package network.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SurvivalGames extends OneVsOneKit {
    public SurvivalGames() {
        super("Survival Games", Material.STONE_SWORD);
        setArmor(Material.GOLD_HELMET);
        setArmor(Material.CHAINMAIL_CHESTPLATE);
        setArmor(Material.IRON_LEGGINGS);
        setArmor(Material.CHAINMAIL_BOOTS);
        setItem(0, Material.STONE_SWORD);
        setItem(1, Material.FISHING_ROD);
        setItem(2, Material.FLINT_AND_STEEL);
        setItem(3, Material.BOW);
        setItem(4, Material.GOLDEN_APPLE);
        setItem(8, new ItemStack(Material.ARROW, 6));
    }
}
