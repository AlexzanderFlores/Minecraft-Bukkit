package network.gameapi.games.onevsones;

import network.customevents.player.MouseClickEvent;
import network.gameapi.SpectatorHandler;
import network.gameapi.games.onevsones.events.BattleRequestEvent;
import network.player.MessageHandler;
import network.server.util.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public class SpectatorHandler1v1s extends SpectatorHandler {
    private static ItemStack item = null;

    public SpectatorHandler1v1s() {
        super();
        saveItems = false;

        item = new ItemCreator(Material.WATCH).setName("&aSpectate").getItemStack();
    }

    public static ItemStack getItem() {
        return item;
    }

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        Player player = event.getPlayer();
        if(getItem().equals(player.getItemInHand())) {
            if(contains(player)) {
                remove(player);
            } else {
                add(player);
            }
            event.setCancelled(true);
        }
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
