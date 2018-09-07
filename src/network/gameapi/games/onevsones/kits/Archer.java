package network.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import network.server.util.ItemCreator;

public class Archer extends OneVsOneKit {
    public Archer() {
        super("Archer", Material.BOW);
        setArmor(Material.LEATHER_HELMET);
        setArmor(Material.CHAINMAIL_CHESTPLATE);
        setArmor(Material.GOLD_LEGGINGS);
        setArmor(Material.LEATHER_BOOTS);
        setItem(0, new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE).getItemStack());
        setItem(8, Material.ARROW);
    }
}
