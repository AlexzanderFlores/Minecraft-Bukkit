package network.gameapi.games.uhcskywars;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import network.ProPlugin;
import network.customevents.game.GameStartEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.TitleDisplayer;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;

public class LootPassHandler implements Listener {
	private List<String> canUsePass = null;
	private List<String> usedLootPass = null;
	
	public LootPassHandler() {
		canUsePass = new ArrayList<String>();
		usedLootPass = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					if(DB.PLAYERS_SKY_WARS_LOOT_PASSES.isUUIDSet(player.getUniqueId())) {
						canUsePass.add(player.getName());
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if(block.getType() == Material.CHEST) {
			Player player = event.getPlayer();
			if(usedLootPass.contains(player.getName())) {
				new TitleDisplayer(player, "&cYou can only use &e1 &cpass per game").display();
			} else {
				if(canUsePass.contains(player.getName())) {
					canUsePass.remove(player.getName());
					usedLootPass.add(player.getName());
					ChestHandler.restock(block);
					new TitleDisplayer(player, "&bRestocked Chest", "&cGet more with &a/vote").display();
					final UUID uuid = player.getUniqueId();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							int amount = DB.PLAYERS_SKY_WARS_LOOT_PASSES.getInt("uuid", uuid.toString(), "amount") - 1;
							if(amount <= 0) {
								DB.PLAYERS_SKY_WARS_LOOT_PASSES.deleteUUID(uuid);
							} else {
								DB.PLAYERS_SKY_WARS_LOOT_PASSES.updateInt("amount", amount, "uuid", uuid.toString());
							}
						}
					});
				} else {
					new TitleDisplayer(player, "&cYou are out of restocks", "&cGet more with &a/vote").display();
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		canUsePass.remove(event.getPlayer().getName());
		usedLootPass.remove(event.getPlayer().getName());
	}
}