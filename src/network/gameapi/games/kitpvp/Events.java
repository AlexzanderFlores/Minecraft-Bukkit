package network.gameapi.games.kitpvp;

import network.Network.Plugins;
import network.customevents.game.GameKillEvent;
import network.customevents.player.AsyncPostPlayerJoinEvent;
import network.gameapi.kit.KitBase;
import network.player.CoinsHandler;
import network.player.LevelGiver;
import network.player.MessageHandler;
import network.server.util.EventUtil;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.KITPVP.getData());
		if(coinsHandler != null) {
			coinsHandler.getCoins(player);
			if(coinsHandler.isNewPlayer(player)) {
				coinsHandler.addCoins(player, 100, "&7(To help you get started)");
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Arrow) {
			event.getEntity().remove();
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		new LevelGiver(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(player.getKiller() != null) {
			Player killer = player.getKiller();
			MessageHandler.sendMessage(player, event.getDeathMessage());
			MessageHandler.sendMessage(killer, event.getDeathMessage());
		}
		event.setDeathMessage(null);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		for(KitBase kit : KitBase.getKits()) {
			if(kit.has(player)) {
				kit.execute(player);
				break;
			}
		}
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		event.setYield(0);
		event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		InventoryType type = event.getInventory().getType();
		if(type == InventoryType.ENCHANTING || type == InventoryType.HOPPER || type == InventoryType.BEACON) {
			event.setCancelled(true);
		}
	}
}
