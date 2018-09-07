package network.gameapi.games.onevsones;

import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.server.util.EventUtil;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Team implements Listener {
    private DyeColor color = null;
    private Location location = null;
    private Vector distance = null;
    private List<String> players = null;

    public Team(DyeColor color, Location location, Vector distance, Player ...players) {
        this.color = color;
        this.location = location;
        this.distance = distance;
        this.players = new ArrayList<String>();

        for(Player player : players) {
            this.players.add(player.getName());
        }

        EventUtil.register(this);
    }

    public DyeColor getColor() {
        return this.color;
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

    public void teleport(int mult) {
        distance = distance.multiply(mult);
        boolean directionIsX = distance.getX() != 0;
        int distanceToAdd = 0;

        for(Player player : getPlayers()) {
            Vector toAddVector;
            if(directionIsX) {
                toAddVector = new Vector(distanceToAdd, 0, 0);
            } else {
                toAddVector = new Vector(0, 0, distanceToAdd);
            }

            Location loc = location.clone();
            loc.add(toAddVector);

            player.teleport(loc);
            distanceToAdd += 2;
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        players.remove(event.getPlayer().getName());
    }
}
