package network.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import network.gameapi.uhc.GoldenHead;
import network.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class SpeedUHC extends OneVsOneKit {
    public SpeedUHC() {
        super("Speed UHC", Material.DIAMOND_PICKAXE);
        setArmor(new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
        setItem(0, new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DURABILITY).getItemStack());
        setItem(1, Material.FLINT_AND_STEEL);
        setItem(2, GoldenHead.get());
        setItem(3, new ItemStack(Material.GOLDEN_APPLE, 2));
        Potion regen = new Potion(PotionType.REGEN, 2, true);
        setItem(4, regen.toItemStack(1));
        Potion swift = new Potion(PotionType.SPEED, 2, true);
        setItem(5, swift.toItemStack(1));
        setItem(6, swift.toItemStack(1));
        setItem(7, new ItemStack(Material.COBBLESTONE, 32));
        setItem(8, new ItemStack(Material.DIAMOND_PICKAXE));
    }
}
