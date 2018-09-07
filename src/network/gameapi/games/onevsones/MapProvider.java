package network.gameapi.games.onevsones;

import network.customevents.TimeEvent;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MapProvider implements Listener {
//    public static Map<Integer, List<Location>> maps = null; // <map number> <target Locations>
    public static Map<Location, Boolean> maps = null; // <Center Block> <In Use>
    public static Map<Location, Vector> spawnDistances = null;
    private static int numberOfMaps = 0;

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
                Bukkit.getLogger().info(location.toString());
                ++numberOfMaps;
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
        spawnDistances.put(new Location(world, 1000, 0, -30), new Vector(20, 0, 0));
        spawnDistances.put(new Location(world, 1100, 0, -30), new Vector(20, 0, -17));

        for(Location location : spawnDistances.keySet()) {
            Bukkit.getLogger().info(location.toString() + " - " + spawnDistances.get(location).toString());
        }

//        do {
//        	mapCheckBlock = world.getBlockAt(382, 4, z);
//        	do {
//                mapCheckBlock = mapCheckBlock.getRelative(118, 0, 0);
//                if(mapCheckBlock.getType() != Material.AIR) {
//                	List<Location> locations = maps.get(counter);
//                	if(locations == null) {
//                		locations = new ArrayList<Location>();
//                	}
//                	locations.add(new Location(world, mapCheckBlock.getX(), 4, z));
//                	maps.put(counter, locations);
//                    numberOfMaps++;
//                }
//            } while(mapCheckBlock.getType() != Material.AIR);
//        	++counter;
//        	z -= 100;
//        	mapCheckBlock = world.getBlockAt(500, 4, z);
//        } while(mapCheckBlock.getType() != Material.AIR);
        Bukkit.getLogger().info("Maps found: " + numberOfMaps);
        EventUtil.register(this);
    }

    public MapProvider(Player playerOne, Player playerTwo, World world, boolean tournament, boolean ranked) {
        Location map = null;
        do {
            map = (Location) maps.keySet().toArray()[new Random().nextInt(maps.size())];
        } while(maps.get(map));

        new Battle(map, playerOne, playerTwo, tournament, ranked);

//    	int error = -1;
//        int map = new Random().nextInt(maps.size());
//        Location location = null;
//        if(maps.containsKey(map)) {
//        	List<Location> locations = maps.get(map);
//        	if(locations != null && !locations.isEmpty()) {
//        		location = locations.get(0);
//        		locations.remove(0);
//        		maps.put(map, locations);
//                new Battle(map, location, playerOne, playerTwo, tournament, ranked);
//        	} else {
//        		error = 1;
//        	}
//        } else {
//        	error = 2;
//        }
//        if(error > 0) {
//        	String loc = location == null ? "null" : location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
//        	String message = "&cThere was an error with map provider, please report this (" + loc + ", " + map + ", " + error + ")";
//        	MessageHandler.sendMessage(playerOne, message);
//        	MessageHandler.sendMessage(playerTwo, message);
//        }
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20) {
            for(Battle battle : BattleHandler.getBattles()) {
                battle.incrementTimer();
                if(battle.getTimer() == 5) {
                    battle.start();
                }
            }
        }
    }
}
