package network.gameapi.kit;

import network.Network;
import network.Network.Plugins;
import network.customevents.player.AsyncPlayerLeaveEvent;
import network.customevents.player.AsyncPostPlayerJoinEvent;
import network.player.MessageHandler;
import network.server.DB;
import network.server.util.EventUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultKit implements Listener {
	private static Map<String, KitBase> defaultKits = null;
	
	public DefaultKit() {
		if(Network.getPlugin() != Plugins.HUB) {
			defaultKits = new HashMap<String, KitBase>();
			EventUtil.register(this);
		}
	}
	
	public static void setDefaultKit(Player player, KitBase kit) {
		if(defaultKits == null) {
			new DefaultKit();
		}
		defaultKits.put(player.getName(), kit);
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		if(Network.getMiniGame() != null && !Network.getMiniGame().getJoiningPreGame()) {
			return;
		}
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		boolean selected = false;
		for(String kitName : DB.PLAYERS_DEFAULT_KITS.getAllStrings("kit", new String [] {"uuid", "game"}, new String [] {uuid.toString(), Network.getPlugin().getData()})) {
			selected = true;
			KitBase kit = null;
			for(KitBase kitBase : KitBase.getKits()) {
				if(kitBase.getName().equals(kitName)) {
					kit = kitBase;
					break;
				}
			}
			if(kit != null) {
				kit.use(player, true);
			}
		}
		if(!selected) {
			MessageHandler.sendMessage(player, "&cNo default kit selected, get them in the &bshop");
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(defaultKits.containsKey(name)) {
			KitBase kit = defaultKits.get(name);
			UUID uuid = event.getUUID();
			String game = kit.getPluginData();
			String [] keys = new String [] {"uuid", "game", "type"};
			String [] values = new String [] {uuid.toString(), game, kit.getKitType()};
			if(DB.PLAYERS_DEFAULT_KITS.isKeySet(keys, values)) {
				DB.PLAYERS_DEFAULT_KITS.updateString("kit", kit.getName(), keys, values);
			} else {
				DB.PLAYERS_DEFAULT_KITS.insert("'" + uuid.toString() + "', '" + game + "', '" + kit.getKitType() + "', '" + kit.getName() + "'");
			}
			defaultKits.remove(name);
		}
	}
}
