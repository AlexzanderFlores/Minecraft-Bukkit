package network.gameapi.games.kitpvp.shop;

import network.gameapi.shops.KitPVPShop;
import npc.NPCEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Shop implements Listener {

	public Shop(double x, double y, double z) {
		World world = Bukkit.getWorlds().get(0);

		new NPCEntity(EntityType.ZOMBIE, "&eKits & Shop", new Location(world, x, y, z), world.getSpawnLocation()) {
			@Override
			public void onInteract(Player player) {
				KitPVPShop.getInstance().openShop(player);
			}
		};
	}
}
