package network.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import network.gameapi.uhc.GoldenHead;
import network.server.util.ItemCreator;

public class BuildUHC extends OneVsOneKit {
    public BuildUHC() {
        super("Build UHC", Material.GOLDEN_APPLE);
        setArmor(new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
        setItem(0, new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 3).getItemStack());
        setItem(1, Material.FISHING_ROD);
        setItem(2, new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_DAMAGE, 3).getItemStack());
        setItem(3, GoldenHead.get(3));
        setItem(4, new ItemStack(Material.GOLDEN_APPLE, 6));
        setItem(5, Material.LAVA_BUCKET);
        setItem(6, Material.WATER_BUCKET);
        setItem(7, Material.DIAMOND_PICKAXE);
        setItem(8, new ItemStack(Material.COBBLESTONE, 64));
        setItem(9, new ItemStack(Material.ARROW, 40));
        setItem(10, new ItemStack(Material.LAVA_BUCKET));
        setItem(11, new ItemStack(Material.WATER_BUCKET));
        setItem(12, new ItemStack(Material.COBBLESTONE, 64));
    }
}
