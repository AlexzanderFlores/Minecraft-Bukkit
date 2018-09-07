package network.gameapi.games.onevsones;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import network.ProPlugin;
import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.AsyncPlayerLeaveEvent;
import network.gameapi.games.onevsones.events.BattleStartEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.TimeUtil;

public class RankedHandler implements Listener {
	private static Map<String, Integer> matches = null;
	private static String name = null;
	
	public RankedHandler() {
		name = "Ranked Selector";
		matches = new HashMap<String, Integer>();
		new CommandBase("addRankedMatches", 2) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					int amount = Integer.valueOf(arguments[1]);
					matches.put(name, matches.get(name) + amount);
					MessageHandler.sendMessage(player, "+" + amount + " Ranked matches from voting (" + matches.get(name) + " total)");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	public static int getMatches(Player player) {
		return matches == null || !matches.containsKey(player.getName()) ? 0 : matches.get(player.getName());
	}
	
	public static void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
		inventory.setItem(11, new ItemCreator(Material.STAINED_GLASS_PANE, 5).setName("&aRanked Queue").getItemStack());
		inventory.setItem(13, new ItemCreator(Material.STAINED_GLASS_PANE, 14).setName("&aUnranked Queue").getItemStack());
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		int daily = 20;
		int amount = daily;
		String date = TimeUtil.getTime().substring(0, 10);
		if(DB.PLAYERS_ONE_VS_ONE_RANKED_MATCHES.isUUIDSet(uuid)) {
			amount = DB.PLAYERS_ONE_VS_ONE_RANKED_MATCHES.getInt("uuid", uuid.toString(), "amount");
			if(!DB.PLAYERS_ONE_VS_ONE_RANKED_MATCHES.getString("uuid", uuid.toString(), "date").equals(date)) {
				amount += daily;
				MessageHandler.sendMessage(player, "+" + daily + " Daily ranked matches, get more with &b/vote");
				DB.PLAYERS_ONE_VS_ONE_RANKED_MATCHES.updateString("date", date, "uuid", uuid.toString());
			}
		} else {
			DB.PLAYERS_ONE_VS_ONE_RANKED_MATCHES.insert("'" + uuid.toString() + "', '" + daily + "', '" + date + "'");
			MessageHandler.sendMessage(player, "+" + daily + " Daily ranked matches, get more with &b/vote");
		}
		matches.put(player.getName(), amount);
	}
	
	@EventHandler
	public void onBattleStart(BattleStartEvent event) {
		if(event.getBattle().isRanked()) {
			for(Player player : event.getBattle().getPlayers()) {
				if(!Ranks.VIP.hasRank(player)) {
					int amount = getMatches(player);
					matches.put(player.getName(), --amount);
					MessageHandler.sendMessage(player, "You now have &e" + amount + " &xranked matches left");
					MessageHandler.sendMessage(player, "Get more with " + Ranks.VIP.getPrefix() + "&xor voting &b/vote");
				}
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(matches.containsKey(name)) {
			int amount = matches.get(name);
			if(amount < 0) {
				amount = 0;
			}
			DB.PLAYERS_ONE_VS_ONE_RANKED_MATCHES.updateInt("amount", amount, "uuid", event.getUUID().toString());
			matches.remove(name);
		}
	}
}
