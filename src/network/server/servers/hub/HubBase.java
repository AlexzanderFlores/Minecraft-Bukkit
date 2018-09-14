package network.server.servers.hub;

import network.Network;
import network.ProPlugin;
import network.player.LevelHandler;
import network.player.TeamScoreboardHandler;
import network.server.CommandBase;
import network.server.DailyRewards;
import network.server.ServerLogger;
import network.server.effects.images.DisplaySkin;
import network.server.servers.hub.crate.CrateTypes;
import network.server.servers.hub.crate.KeyFragments;
import network.server.servers.hub.items.Features;
import network.server.servers.hub.items.GameSelector;
import network.server.servers.hub.items.HubSelector;
import network.server.servers.hub.items.Profile;
import network.server.servers.hub.parkours.EndlessParkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.*;
import java.util.List;

public class HubBase extends ProPlugin {
	private static int hubNumber = 0;
	private boolean hasPlayerJoined = false;
	
	public HubBase(String name) {
		super(name);
		addGroup("24/7");
		addGroup("hub");
		setAllowItemSpawning(true);
		setAllowInventoryClicking(true);
		resetWorld(12250);
		hubNumber = Integer.valueOf(Network.getServerName().replaceAll("[^\\d.]", ""));
		LevelHandler.enable();
		World world = Bukkit.getWorlds().get(0);

		CrateTypes.VOTING.setBeacon(world.getBlockAt(1651, 6, -1278), new Vector(0.85, 2.5, 0.5));
		CrateTypes.PREMIUM.setBeacon(world.getBlockAt(1651, 6, -1284), new Vector(0.85, 2.5, 0.5));

		new Events();
		new KeyFragments();
		new GameSelector();
		new Features();
		new Profile();
		new HubSelector();
		new ServerLogger();
		new DailyRewards(new Location(world, 1658, 5, -1277));
		new TeamScoreboardHandler();
		new ParkourNPC();
		new EndlessParkour();
		new Flag();

		if(hubNumber == 1) {
			new Server();
		}

		Map<String, Location []> locations = new HashMap<String, Location []>();

		locations.put("RecentCustomer", new Location [] {
				new Location(world, 1689, 5, -1260),
				new Location(world, 1687, 8, -1260)
		});

		locations.put("RecentVoter", new Location [] {
				new Location(world, 1685, 5, -1260),
				new Location(world, 1683, 8, -1260)
		});

		locations.put("RecentlyJoinedDiscord", new Location [] {
				new Location(world, 1681, 5, -1260),
				new Location(world, 1679, 8, -1260),
		});

		List<UUID> uuids = Arrays.asList(
				UUID.fromString("11603007-81b5-45fe-b17e-91ea8972143d"),
				UUID.fromString("6330b959-8daa-4cb0-9112-cf28f5185384"),
				UUID.fromString("10924f26-1c86-4025-8f9d-8b3a86b83810")
		);

		new DisplaySkin(
				"RecentCustomer",
				locations,
				uuids.get(new Random().nextInt(uuids.size())),
				new Color(0x312117)
		).display();

		new DisplaySkin(
				"RecentVoter",
				locations,
				uuids.get(new Random().nextInt(uuids.size())),
				new Color(0x312117)
		).display();

		new DisplaySkin(
				"RecentlyJoinedDiscord",
				locations,
				uuids.get(new Random().nextInt(uuids.size())),
				new Color(0x312117)
		).display();
	}
	
	public static int getHubNumber() {
		return hubNumber;
	}
	
//	@Override
//	public void disable() {
//		String container = Bukkit.getWorldContainer().getPath();
//		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
//		FileHandler.delete(new File(container + "/spawn"));
//		FileHandler.copyFolder(new File("/root/resources/maps/hub"), new File(container + "/spawn"));
//		super.disable();
//	}
}
