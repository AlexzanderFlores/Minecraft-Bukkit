package network.gameapi;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.ProPlugin;
import network.customevents.player.MouseClickEvent;
import network.gameapi.MiniGame.GameStates;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class LeaveItem implements Listener {
	private ItemStack leaveItem = null;
	
	public LeaveItem() {
		leaveItem = new ItemCreator(Material.WOOD_DOOR).setName("&aReturn to Hub").getItemStack();
		EventUtil.register(this);
	}
	
	private boolean isWaiting() {
		GameStates gameState = Network.getMiniGame().getGameState();
		return gameState == GameStates.WAITING || gameState == GameStates.VOTING;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(isWaiting()) {
			event.getPlayer().getInventory().setItem(8, leaveItem);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(isWaiting()) {
			Player player = event.getPlayer();
			ItemStack item = player.getItemInHand();
			if(item != null && item.equals(leaveItem)) {
				ProPlugin.sendPlayerToServer(player, "hub");
			}
		}
	}
}
