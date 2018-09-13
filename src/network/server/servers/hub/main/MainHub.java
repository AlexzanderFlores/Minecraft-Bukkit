package network.server.servers.hub.main;

import network.Network;
import network.server.servers.hub.HubBase;

public class MainHub extends HubBase {
	public MainHub() {
		super("MainHub");
		addGroup("mainhub");
//		new MainHubTop5();
	}
	
	public static int getHubNumber() {
		return Integer.valueOf(Network.getServerName().toLowerCase().replace("hub", ""));
	}
}
