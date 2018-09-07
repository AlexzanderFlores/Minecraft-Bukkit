package network.gameapi.uhc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.customevents.game.GameDeathEvent;
import network.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class SkullPikeUtil implements Listener {
	public SkullPikeUtil() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player player = event.getPlayer();
        Location location = player.getLocation();
        location.getBlock().setType(Material.NETHER_FENCE);
        Block block = location.getBlock().getRelative(0, 1, 0);
        block.setType(Material.SKULL);
        block.setData((byte) 1);
        if(block.getType() == Material.SKULL) {
        	Skull skull = (Skull) block.getState();
            skull.setSkullType(SkullType.PLAYER);
            skull.setOwner(player.getName());
            skull.update();
        }
	}
}