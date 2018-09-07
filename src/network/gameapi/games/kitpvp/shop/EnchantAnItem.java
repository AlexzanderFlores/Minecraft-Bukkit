package network.gameapi.games.kitpvp.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import network.gameapi.games.kitpvp.events.InventoryViewClickEvent;
import network.player.MessageHandler;
import network.server.util.EffectUtil;

public class EnchantAnItem extends InventoryViewer {
	private static final int price = 20;
	private Random random = null;
	
	public EnchantAnItem() {
		super("Enchant an Item");
		random = new Random();
	}
	
	public static int getPrice() {
		return price;
	}
	
	@EventHandler
	public void onInventoryViewClick(InventoryViewClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			if(coinsHandler.getCoins(player) >= price) {
				if(player.getLevel() > 0) {
					ItemStack item = player.getInventory().getItem(event.getSlot());
					if(item.getEnchantments() == null || item.getEnchantments().isEmpty()) {
						InventoryView view = player.getOpenInventory();
						if(view != null && view.getTitle().equals(name)) {
							item = view.getItem(event.getViewSlot());
						}
					} else {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
						return;
					}
					String type = item.getType().toString();
					List<Enchantment> enchantments = new ArrayList<Enchantment>();
					if(type.contains("SWORD")) {
						enchantments.add(Enchantment.DAMAGE_ALL);
						enchantments.add(Enchantment.KNOCKBACK);
					} else if(type.contains("BOW")) {
						enchantments.add(Enchantment.ARROW_DAMAGE);
						enchantments.add(Enchantment.ARROW_KNOCKBACK);
					} else if(type.contains("HELMET") || type.contains("CHESTPLATE") || type.contains("LEGGINGS") || type.contains("BOOTS")) {
						enchantments.add(Enchantment.PROTECTION_ENVIRONMENTAL);
						enchantments.add(Enchantment.PROTECTION_PROJECTILE);
					}
					if(enchantments.isEmpty()) {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					} else {
						int index = random.nextInt(enchantments.size());
						Bukkit.getLogger().info(type + " " + enchantments.size() + " " + index);
						player.getInventory().getItem(event.getSlot()).addEnchantment(enchantments.get(index), 1);
						enchantments.clear();
						enchantments = null;
						player.setLevel(player.getLevel() - 1);
						coinsHandler.addCoins(player, price * -1);
						EffectUtil.playSound(player, Sound.LEVEL_UP);
					}
				} else {
					MessageHandler.sendMessage(player, "&cYou do not have any levels");
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have enough coins, get more with &a/vote");
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			}
		}
	}
}
