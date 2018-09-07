package network.gameapi.games.kitpvp.killstreaks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class PoisonBow extends Killstreak implements Listener {
	private static PoisonBow instance = null;
	private List<Entity> poisonArrows = null;
	private Map<String, Integer> uses = null;
	private int maxUses = 3;
	
	public PoisonBow() {
		super(new ItemCreator(Material.BOW).setName("Poison Bow").getItemStack());
		instance = this;
		poisonArrows = new ArrayList<Entity>();
		uses = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	public static PoisonBow getInstance() {
		if(instance == null) {
			new PoisonBow();
		}
		return instance;
	}
	
	@Override
	public void execute(Player player) {
		player.getInventory().addItem(new ItemCreator(Material.BOW).setName(getName()).addEnchantment(Enchantment.ARROW_INFINITE).getItemStack());
		if(!player.getInventory().contains(Material.ARROW)) {
			player.getInventory().setItem(9, new ItemStack(Material.ARROW));
		}
		uses.put(player.getName(), 0);
		MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened \"&e" + getName() + "&6\" from the killstreak selector");
		MessageHandler.sendMessage(player, "&b" + getName() + ": &aGet a bow that shoots poison arrows, &e" + maxUses + " &auses");
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if(poisonArrows.contains(arrow)) {
				Bukkit.getLogger().info("A");
				Player player = (Player) event.getEntity();
				player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 7 + 10, 0));
				poisonArrows.remove(arrow);
			}
		}
	}
	
	//@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		poisonArrows.remove(event.getEntity());
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(event.getEntity() instanceof Player && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			ItemStack item = player.getItemInHand();
			if(item != null && item.getItemMeta().getDisplayName() != null) {
				String name = item.getItemMeta().getDisplayName();
				if(name != null && name.equals(getName())) {
					Arrow arrow = (Arrow) event.getProjectile();
					poisonArrows.add(arrow);
					int use = 0;
					if(uses.containsKey(player.getName())) {
						use = uses.get(player.getName());
					}
					if(++use >= maxUses) {
						player.setItemInHand(new ItemStack(Material.AIR));
						uses.put(player.getName(), 0);
					} else {
						uses.put(player.getName(), use);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		uses.remove(event.getPlayer().getName());
	}
}