package network.gameapi.games.onevsones;

import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.server.util.EventUtil;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class Team implements Listener {
    private DyeColor color = null;
    private OneVsOneKit kit = null;
    private List<String> players = null;

    public Team(DyeColor color, OneVsOneKit kit, Player ...players) {
        this.color = color;
        this.kit = kit;
        this.players = new ArrayList<String>();

        for(Player player : players) {
            this.players.add(player.getName());
        }

        EventUtil.register(this);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public OneVsOneKit getKit() {
        return this.kit;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<Player>();
        for(String name : this.players) {
            Player player = ProPlugin.getPlayer(name);
            if(player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public boolean isInTeam(Player player) {
        return this.players.contains(player.getName());
    }

    public boolean removePlayer(Player player) {
        if(players.size() <= 1) {
            return true;
        }
        players.remove(player.getName());
        return false;
    }

    public void teleport(Location location) {
        for(Player player : getPlayers()) {
            player.teleport(location);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        removePlayer(event.getPlayer());
    }
}
