package network.server.servers.hub.items.features;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import network.customevents.player.PlayerLeaveEvent;

public class Gadgets extends FeatureBase {
	private static int max = 0;
	private static Map<String, Integer> owned = null;
	
	public Gadgets() {
		super("Gadgets", 24, new ItemStack(Material.ENDER_PEARL), null, new String [] {
			"",
			"&cUnder development",
			"",
			"&7&mHave fun with the best gadets around",
			"",
			"&7&mOwned: &e&mXX&8&m/&e&m" + max + " &7&m(&e&mYY%&7&m)",
			"&7&mCollect from: &e&mZZ",
			""
		});
		owned = new HashMap<String, Integer>();
	}
	
	@Override
	public int getOwned(Player player) {
		if(!owned.containsKey(player.getName())) {
			owned.put(player.getName(), 0);
		}
		return owned.get(player.getName());
	}
	
	@Override
	public int getMax() {
		return max;
	}
	
	@Override
	public void display(Player player) {
		
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		owned.remove(event.getPlayer().getName());
	}
}
