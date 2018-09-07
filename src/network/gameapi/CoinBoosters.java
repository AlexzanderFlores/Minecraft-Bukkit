package network.gameapi;

import network.Network;
import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.CoinGiveEvent;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoinBoosters implements Listener {
	private Map<String, Integer> boosters = null;
	private ItemStack item = null;
	private String user = null;
	private final String command = "Get boosters with &6/booster";
	
	public CoinBoosters() {
		boosters = new HashMap<String, Integer>();
		item = new ItemCreator(Material.DIAMOND).setName("&eClick to Enable x2 Coin Booster").setGlow(true).getItemStack();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		if(Network.getMiniGame().getUseCoinBoosters() && Network.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			UUID uuid = player.getUniqueId();
			String [] keys = new String [] {"uuid", "game_name"};
			String [] values = new String [] {uuid.toString(), Network.getPlugin().getData()};
			boosters.put(player.getName(), DB.PLAYERS_COIN_BOOSTERS.getInt(keys, values, "amount"));
			player.getInventory().addItem(item);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		final Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(this.item.equals(item)) {
			int amount = boosters.get(player.getName());
			if(amount > 0) {
				if(user == null) {
					user = AccountHandler.getPrefix(player);
					MessageHandler.alert(user + " &xhas enabled a x2 coin booster for " + Network.getPlugin().getDisplay() + "! " + command);
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							int amount = boosters.get(player.getName()) - 1;
							String [] keys = new String [] {"uuid", "game_name"};
							String [] values = new String [] {player.getUniqueId().toString(), Network.getPlugin().getData()};
							if(amount <= 0) {
								DB.PLAYERS_COIN_BOOSTERS.delete(keys, values);
							} else {
								DB.PLAYERS_COIN_BOOSTERS.updateInt("amount", amount, keys, values);
							}
						}
					});
				} else {
					MessageHandler.sendMessage(player, user + " &chas already enabled a coin booster for " + Network.getPlugin().getDisplay() + ". " + command);
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have any coin boosters! " + command);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCoinGive(CoinGiveEvent event) {
		if(user != null) {
			MessageHandler.sendMessage(event.getPlayer(), user + " &xhas an active x2 Coins booster! " + command);
			event.setAmount(event.getAmount() * 2);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		boosters.remove(event.getPlayer().getName());
	}
}
