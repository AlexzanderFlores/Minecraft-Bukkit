package network.gameapi.games.kitpvp.killstreaks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Killstreak {
	private static List<Killstreak> killstreaks = null;
	private String name = null;
	private ItemStack itemStack = null;
	
	public Killstreak(ItemStack itemStack) {
		this.itemStack = itemStack;
		name = itemStack.getItemMeta().getDisplayName();
		if(killstreaks == null) {
			killstreaks = new ArrayList<Killstreak>();
		}
		killstreaks.add(this);
	}
	
	public static List<Killstreak> getKillstreaks() {
		return killstreaks;
	}
	
	public String getName() {
		return name;
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public abstract void execute(Player player);
}