package network.gameapi.games.kitpvp.killstreaks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import network.gameapi.games.kitpvp.SpawnHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class SnowballFight extends Killstreak implements Listener {
	private static SnowballFight instance = null;
	
	public SnowballFight() {
		super(new ItemCreator(Material.SNOW_BALL).setName("Snowball Fight").getItemStack());
		instance = this;
		EventUtil.register(this);
	}
	
	public static SnowballFight getInstance() {
		if(instance == null) {
			new SnowballFight();
		}
		return instance;
	}
	
	@Override
	public void execute(Player player) {
		player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 5));
		MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened \"&e" + getName() + "&6\" from the killstreak selector");
		MessageHandler.sendMessage(player, "&b" + getName() + ": &aGet 3 snowballs that give players slowness");
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(player.getItemInHand().getType() == Material.SNOW_BALL && SpawnHandler.isAtSpawn(player)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Snowball && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 0));
		}
	}
}