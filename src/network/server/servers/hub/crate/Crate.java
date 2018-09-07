package network.server.servers.hub.crate;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Crate {
	private static Beacon voting = null;
	private static Beacon superCrate = null;
	
	public Crate() {
		World world = Bukkit.getWorlds().get(0);
		voting = new Beacon("Voting Crate&8 (&7Click&8)", "voting", world.getBlockAt(1651, 6, -1278), new Vector(0.85, 2.5, 0.5));
		superCrate = new Beacon("Super Crate&8 (&7Click&8)", "super", world.getBlockAt(1651, 6, -1284), new Vector(0.85, 2.5, 0.5));
	}
	
	public static Beacon getVoting() {
		return voting;
	}
	
	public static Beacon getSuperCrate() {
		return superCrate;
	}
}
