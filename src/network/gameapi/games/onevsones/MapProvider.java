package network.gameapi.games.onevsones;

import network.customevents.TimeEvent;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MapProvider implements Listener {
    public static Map<Location, Boolean> maps = null; // <Center Block> <In Use>
    public static Map<Location, Vector> spawnDistances = null;

    public MapProvider(World world) {
        maps = new HashMap<Location, Boolean>();
        spawnDistances = new HashMap<Location, Vector>();
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
        Location map = null;
        do {
            map = (Location) maps.keySet().toArray()[new Random().nextInt(maps.size())];
        } while(map == null || maps.get(map));

        Bukkit.getLogger().info("");
        Bukkit.getLogger().info("In use = " + maps.get(map) + " (" + teams[0].getPlayers().get(0).getName() + ")");
        Bukkit.getLogger().info("");

        new Battle(map, tournament, ranked, teams);
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
}
