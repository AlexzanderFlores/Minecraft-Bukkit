package network.gameapi.games.onevsones.kits;

import network.gameapi.games.onevsones.BattleHandler;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.Potion.Tier;

import network.server.util.ItemCreator;

import org.bukkit.potion.PotionType;

@SuppressWarnings("deprecation")
public class NoDebuff extends OneVsOneKit {
    public NoDebuff() {
        super("No Debuff", new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(1));
        setArmor(new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).addEnchantment(Enchantment.DURABILITY, 3).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).addEnchantment(Enchantment.DURABILITY, 3).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).addEnchantment(Enchantment.DURABILITY, 3).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).addEnchantment(Enchantment.DURABILITY, 3).addEnchantment(Enchantment.PROTECTION_FALL, 4).getItemStack());
        setItem(0, new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).addEnchantment(Enchantment.FIRE_ASPECT, 2).addEnchantment(Enchantment.DURABILITY, 3).getItemStack());
        setItem(1, new ItemCreator(Material.ENDER_PEARL).setAmount(16).getItemStack());
        setItem(9, new ItemStack(Material.ARROW, 64));
        Potion health = new Potion(PotionType.INSTANT_HEAL, 1, true);
        health.setTier(Tier.TWO);
        for(int a = 2; a <= 5; ++a) {
            setItem(a, health.toItemStack(1));
        }
        setItem(6, new ItemStack(Material.COOKED_BEEF, 64));
        Potion fireResistance = new Potion(PotionType.FIRE_RESISTANCE, 1, false);
        fireResistance.setHasExtendedDuration(true);
        setItem(7, fireResistance.toItemStack(1));
        Potion speed = new Potion(PotionType.SPEED, 1, false);
        speed.setTier(Tier.TWO);
        for(int slot : new int [] {8, 33, 34, 35}) {
            setItem(slot, speed.toItemStack(1));
        }
        for(int slot : new int [] {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32}) {
            setItem(slot, health.toItemStack(1));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(BattleHandler.isInBattle(player) && hasKit(player)) {
                event.setCancelled(false);
            }
        }
    }
}
