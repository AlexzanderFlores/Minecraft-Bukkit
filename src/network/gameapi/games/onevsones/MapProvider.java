package network.gameapi.games.onevsones;

import network.customevents.TimeEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.games.onevsones.events.BattleEndEvent;
import network.player.MessageHandler;
import network.server.util.EventUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.*;

public class MapProvider implements Listener {
    private static Map<Location, Boolean> maps = null; // <Center Block> <In Use>
    private static Map<Location, Vector> spawnDistances = null;
    private static List<String> noMapMessage = null;

    public MapProvider(World world) {
        maps = new HashMap<Location, Boolean>();
        spawnDistances = new HashMap<Location, Vector>();
        noMapMessage = new ArrayList<String>();
        Block mapCheckBlock = null;
        int x = 200;
        final int y = 12;
        int z = -30;
        final int startZ = z;

        while(true) {
            mapCheckBlock = world.getBlockAt(x, y, z);

            if(mapCheckBlock.getType() == Material.AIR) {
                if(z == startZ) {
                    break;
                } else {
                    x += 100;
                    z = startZ;
                }
            } else {
                Location location = mapCheckBlock.getLocation();
                location.setY(0);
                maps.put(location, false);
                z -= 100;
            }
        }

        spawnDistances.put(new Location(world, 200, 0, -30), new Vector(15, 0, 0));
        spawnDistances.put(new Location(world, 300, 0, -30), new Vector(15, 0, 0));
        spawnDistances.put(new Location(world, 400, 0, -30), new Vector(15, 0, 0));
        spawnDistances.put(new Location(world, 500, 0, -30), new Vector(15, 0, 0));
        spawnDistances.put(new Location(world, 600, 0, -30), new Vector(25, 0, 0));
        spawnDistances.put(new Location(world, 700, 0, -30), new Vector(30, 0, 0));
        spawnDistances.put(new Location(world, 800, 0, -30), new Vector(0, 0, -15));
        spawnDistances.put(new Location(world, 900, 0, -30), new Vector(0, 0, -25));
        spawnDistances.put(new Location(world, 1000, 0, -30), new Vector(15, 0, 0)); // Library
        spawnDistances.put(new Location(world, 1100, 0, -30), new Vector(0, 0, -17));

        EventUtil.register(this);
    }

    public MapProvider(boolean tournament, boolean ranked, Team ... teams) {
        // Check if there's at least 1 open map
        boolean openMap = false;
        for(boolean inUse : maps.values()) {
            if(!inUse) {
                openMap = true;
                break;
            }
        }
        if(!openMap) {
            for(Team team : teams) {
                for(Player player : team.getPlayers()) {
                    if(!noMapMessage.contains(player.getName())) {
                        noMapMessage.add(player.getName());
                        MessageHandler.sendMessage(player, "&cThere are no open maps at the moment, please wait.");
                    }
                }
            }
            return;
        }

        noMapMessage.clear();

        // Get a random map from the current open maps
        Location map = null;
        do {
            map = (Location) maps.keySet().toArray()[new Random().nextInt(maps.size())];
        } while(map == null || maps.get(map));

        // Set that map to "in use"
        maps.put(map, true);

        new Battle(map, tournament, ranked, teams);
    }

    public static Vector getSpawnDistance(Location location) {
        return spawnDistances.get(location);
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20) {
            for(Battle battle : BattleHandler.getBattles()) {
                if(battle.incrementTimer() == 5) {
                    battle.start();
                }
            }
        }
    }

    @EventHandler
    public void onBattleEnd(BattleEndEvent event) {
        maps.put(event.getBattle().getTargetLocation(), false);
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        noMapMessage.remove(event.getPlayer().getName());
    }
}
