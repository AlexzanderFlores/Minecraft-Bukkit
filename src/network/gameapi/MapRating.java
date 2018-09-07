package network.gameapi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.customevents.game.GameEndingEvent;
import network.customevents.game.GiveMapRatingItemEvent;
import network.customevents.player.AsyncPlayerLeaveEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.PlayerSpectatorEvent;
import network.customevents.player.PlayerSpectatorEvent.SpectatorState;
import network.player.TitleDisplayer;
import network.server.DB;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.UnicodeUtil;

public class MapRating implements Listener {
	private String name = null;
	private ItemStack ratingItem = null;
	private Map<String, Integer> ratings = null;
	private int slot = 0;
	
	public MapRating() {
		this(6);
	}
	
	public MapRating(int slot) {
		name = "Rate Map";
		ratingItem = new ItemCreator(Material.NETHER_STAR).setName("&a" + name).getItemStack();
		ratings = new HashMap<String, Integer>();
		this.slot = slot;
		EventUtil.register(this);
	}
	
	private void give(Player player) {
		GiveMapRatingItemEvent giveEvent = new GiveMapRatingItemEvent(player);
		Bukkit.getPluginManager().callEvent(giveEvent);
		if(!giveEvent.isCancelled()) {
			player.getInventory().setItem(slot, ratingItem);
		}
	}
	
	@EventHandler
	public void onPlayerSpectate(PlayerSpectatorEvent event) {
		if(event.getState() == SpectatorState.ADDED) {
			give(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		 new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : Bukkit.getOnlinePlayers()) {
					give(player);
					SpectatorHandler.giveUtilItems(player);
				}
			}
		}, 20);
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item != null && item.equals(ratingItem)) {
			String fullStar = " " + UnicodeUtil.getUnicode("2726");
			String emptyStar = " &7" + UnicodeUtil.getUnicode("2727");
			Inventory inventory = Bukkit.createInventory(player, 9, name);
			inventory.setItem(0, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + emptyStar + emptyStar + emptyStar + emptyStar).getItemStack());
			inventory.setItem(2, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + emptyStar + emptyStar + emptyStar).setAmount(2).getItemStack());
			inventory.setItem(4, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + fullStar + emptyStar + emptyStar).setAmount(3).getItemStack());
			inventory.setItem(6, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + fullStar + fullStar + emptyStar).setAmount(4).getItemStack());
			inventory.setItem(8, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + fullStar + fullStar + fullStar).setAmount(5).getItemStack());
			player.openInventory(inventory);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			ratings.put(player.getName(), event.getItem().getAmount());
			new TitleDisplayer(player, "&bMap Rated!", "&bThank you for your input").display();
			player.closeInventory();
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(ratings.containsKey(name)) {
			UUID uuid = event.getUUID();
			String game = Network.getPlugin().getData();
			MiniGame miniGame = Network.getMiniGame();
			String map = miniGame.getMap().getName();
			int rating = ratings.get(name);
			String [] keys = new String [] {"uuid", "game", "map"};
			String [] values = new String [] {uuid.toString(), game, map};
			if(DB.NETWORK_MAP_RATINGS.isKeySet(keys, values)) {
				DB.NETWORK_MAP_RATINGS.updateInt("rating", rating, keys, values);
			} else {
				DB.NETWORK_MAP_RATINGS.insert("'" + uuid.toString() + "', '" + game + "', '" + map + "', '" + rating + "'");
			}
			ratings.remove(name);
		}
	}
}
