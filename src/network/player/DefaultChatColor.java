package network.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.StringUtil;

public class DefaultChatColor implements Listener {
	private String name = null;
	private Map<String, String> colors = null;
	
	public DefaultChatColor() {
		name = "Select DefaultKit Chat Color";
		colors = new HashMap<String, String>();
		new CommandBase("defaultChatColor", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Inventory inventory = Bukkit.createInventory(player, 9 * 2, name);
				int [] slots = new int [] {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16};
				int [] data = new int [] {15, 11, 13, 3, 14, 10, 1, 8, 7, 9, 5, 3, 6, 2, 4, 0};
				for(int a = 0; a < slots.length && a < data.length; ++a) {
					String color = getColor(a);
					inventory.setItem(slots[a], new ItemCreator(Material.STAINED_GLASS, data[a])
						 .setName("&" + color + "Click for this color")
						 .addLore("&aColor Code: " + color)
						 .addLore("&aExample: " + AccountHandler.getRank(player).getPrefix() + StringUtil.color("&" + color + player.getName()))
						 .getItemStack());
				}
				player.openInventory(inventory);
				return true;
			}
		}.setRequiredRank(Ranks.PRO_PLUS);
		EventUtil.register(this);
	}
	
	private String getColor(int a) {
		if(a <= 9) {
			return String.valueOf(a);
		} else if(a == 10) {
			return "a";
		} else if(a == 11) {
			return "b";
		} else if(a == 12) {
			return "c";
		} else if(a == 13) {
			return "d";
		} else if(a == 14) {
			return "e";
		} else if(a == 15) {
			return "f";
		} else {
			return "";
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			final String color = ChatColor.stripColor(event.getItem().getItemMeta().getLore().get(0)).replace("Color Code: ", "");
			colors.put(player.getName(), color);
			MessageHandler.sendMessage(player, "You selected color code&" + color + " " + color);
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					if(color.equals("f")) {
						DB.PLAYERS_CHAT_COLOR.deleteUUID(uuid);
					} else {
						if(DB.PLAYERS_CHAT_COLOR.isUUIDSet(uuid)) {
							DB.PLAYERS_CHAT_COLOR.updateString("color", color, "uuid", uuid.toString());
						} else {
							DB.PLAYERS_CHAT_COLOR.insert("'" + uuid.toString() + "', '" + color + "'");
						}
					}
				}
			});
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(Ranks.PRO_PLUS.hasRank(player)) {
			if(!colors.containsKey(player.getName())) {
				if(DB.PLAYERS_CHAT_COLOR.isUUIDSet(player.getUniqueId())) {
					colors.put(player.getName(), DB.PLAYERS_CHAT_COLOR.getString("uuid", player.getUniqueId().toString(), "color"));
				} else {
					colors.put(player.getName(), "f");
				}
			}
			String format = event.getFormat();
			String prefix = format.split(":")[0] + ": ";
			String message = StringUtil.color("&" + colors.get(player.getName())) + format.replace(prefix, "");
			event.setFormat(prefix + message);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		colors.remove(event.getPlayer().getName());
	}
}
