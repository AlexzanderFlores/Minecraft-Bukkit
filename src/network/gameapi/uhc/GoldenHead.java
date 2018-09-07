package network.gameapi.uhc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import network.server.util.EventUtil;
import network.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class GoldenHead implements Listener {
	private static ItemStack item = null;
	private static String name = null;
	
	public GoldenHead() {
		name = ChatColor.LIGHT_PURPLE + "Golden Head";
		item = new ItemCreator(Material.GOLDEN_APPLE).setName(name).getItemStack();
		ShapedRecipe ultraApple = new ShapedRecipe(new ItemCreator(Material.GOLDEN_APPLE).setName(name).setGlow(true).getItemStack());
        ultraApple.shape("012", "345", "678");
        ultraApple.setIngredient('0', Material.GOLD_INGOT);
        ultraApple.setIngredient('1', Material.GOLD_INGOT);
        ultraApple.setIngredient('2', Material.GOLD_INGOT);
        ultraApple.setIngredient('3', Material.GOLD_INGOT);
        ultraApple.setIngredient('4', Material.SKULL_ITEM, 3);
        ultraApple.setIngredient('5', Material.GOLD_INGOT);
        ultraApple.setIngredient('6', Material.GOLD_INGOT);
        ultraApple.setIngredient('7', Material.GOLD_INGOT);
        ultraApple.setIngredient('8', Material.GOLD_INGOT);
        Bukkit.getServer().addRecipe(ultraApple);
		EventUtil.register(this);
	}
	
	public static ItemStack get() {
		return get(1);
	}
	
	public static ItemStack get(int amount) {
		return new ItemCreator(item.clone()).setAmount(amount).getItemStack();
	}
	
	public static String getName() {
		return name;
	}
	
	@EventHandler
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
		ItemStack item = event.getItem();
		if(item != null && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null) {
			if(item.getItemMeta().getDisplayName().equals(name)) {
				Player player = event.getPlayer();
				double newHealth = event.getPlayer().getHealth() + 4.0d;
				player.setHealth(newHealth > player.getMaxHealth() ? player.getMaxHealth() : newHealth);
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60, 0));
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 8, 1));
			}
		}
	}
}
