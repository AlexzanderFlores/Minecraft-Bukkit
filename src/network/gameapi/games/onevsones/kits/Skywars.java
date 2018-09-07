package network.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import network.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class Skywars extends OneVsOneKit {
    public Skywars() {
        super("Sky Wars", Material.SNOW_BALL);
        setArmor(new ItemCreator(Material.GOLD_HELMET).addEnchantment(Enchantment.PROTECTION_PROJECTILE).getItemStack());
        setArmor(new ItemCreator(Material.CHAINMAIL_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
        setArmor(new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
        setArmor(new ItemCreator(Material.CHAINMAIL_BOOTS).addEnchantment(Enchantment.PROTECTION_PROJECTILE).getItemStack());
        setItem(0, new ItemCreator(Material.STONE_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).getItemStack());
        setItem(1, Material.FISHING_ROD);
        setItem(2, Material.FLINT_AND_STEEL);
        setItem(3, new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_DAMAGE).getItemStack());
        setItem(4, new ItemStack(Material.GOLDEN_APPLE, 3));
        setItem(6, new ItemStack(Material.COBBLESTONE, 32));
        setItem(7, new ItemStack(Material.DIAMOND_PICKAXE));
        Potion fire = new Potion(PotionType.REGEN, 1, false);
        setItem(5, fire.toItemStack(1));
        setItem(8, new ItemStack(Material.ARROW, 12));
    }
}
