package network.server.util;

import java.lang.reflect.Field;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Glow extends Enchantment {
	public Glow(int id) {
		super(id);
	}

	@Override
	public boolean canEnchantItem(ItemStack arg0) {
		return false;
	}

	@Override
	public boolean conflictsWith(Enchantment arg0) {
		return false;
	}

	@Override
	public EnchantmentTarget getItemTarget() {
		return null;
	}

	@Override
	public int getMaxLevel() {
		return 0;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public int getStartLevel() {
		return 0;
	}
	
	public static void register() {
		try {
			Field field = Enchantment.class.getDeclaredField("acceptingNew");
			field.setAccessible(true);
			field.set(null, true);
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			Enchantment.registerEnchantment(new Glow(70));
		} catch(IllegalArgumentException e) {
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ItemStack addGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(new Glow(70), 1, true);
		item.setItemMeta(meta);
		return item;
	}
}
