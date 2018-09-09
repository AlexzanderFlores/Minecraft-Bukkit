package network.gameapi.games.onevsones;

import network.gameapi.SpectatorHandler;
import network.gameapi.games.onevsones.events.BattleRequestEvent;
import network.player.MessageHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;

public class SpectatorHandler1v1s extends SpectatorHandler {
    public SpectatorHandler1v1s(World world, Location target) {
        super();
        createNPC(new Location(world, 8.5, 13, -43.5), target);
        saveItems = false;
    }

    @EventHandler
    public void onBattleRequest(BattleRequestEvent event) {
        if(contains(event.getPlayerOne())) {
            MessageHandler.sendMessage(event.getPlayerOne(), "&cCannot send request while spectating");
            event.setCancelled(true);
        } else if(contains(event.getPlayerTwo())) {
            MessageHandler.sendMessage(event.getPlayerOne(), "&cCannot send request to a spectator");
            event.setCancelled(true);
        }
    }
}
