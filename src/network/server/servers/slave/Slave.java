package network.server.servers.slave;

import network.ProPlugin;

public class Slave extends ProPlugin {
	public Slave() {
		super("Slave");
		new Voting();
	}
}