package network.gameapi.games.kitpvp.killstreaks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class ExplosiveBow extends Killstreak implements Listener {
	private static ExplosiveBow instance = null;
	private List<Entity> explosiveArrows = null;
	private Map<String, Integer> uses = null;
	private int maxUses = 3;
	
	public ExplosiveBow() {
		super(new ItemCreator(Material.BOW).setName("Explosive Bow").getItemStack());
		instance = this;
		explosiveArrows = new ArrayList<Entity>();
		uses = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	public static ExplosiveBow getInstance() {
		if(instance == null) {
			new ExplosiveBow();
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
		MessageHandler.sendMessage(player, "&b" + getName() + ": &aGet a bow that shoots explosive arrows, 3 uses");
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if(explosiveArrows.contains(arrow)) {
				Player player = (Player) event.getEntity();
				player.getWorld().createExplosion(arrow.getLocation(), 2.0f);
				explosiveArrows.remove(arrow);
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(explosiveArrows.contains(event.getEntity())) {
			Projectile projectile = event.getEntity();
			projectile.getWorld().createExplosion(projectile.getLocation(), 2.0f);
			explosiveArrows.remove(event.getEntity());
		}
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(event.getEntity() instanceof Player && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			ItemStack item = player.getItemInHand();
			if(item != null && item.getItemMeta() != null) {
				String name = item.getItemMeta().getDisplayName();
				if(name != null && name.equals(getName())) {
					explosiveArrows.add(event.getProjectile());
					int use = 0;
					if(uses.containsKey(player.getName())) {
						use = uses.get(player.getName());
					}
					if(++use >= maxUses) {
						player.setItemInHand(new ItemStack(Material.AIR));
						uses.put(player.getName(), use);
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