package network.server.servers.worker;

import network.ProPlugin;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.CommandDispatcher;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class Worker extends ProPlugin {
	public Worker() {
		super("Worker");

		new CommandBase("purchase", 2, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				String product = arguments[1];

                try {
                    UUID uuid = AccountHandler.getUUID(name);
                    AccountHandler.Ranks ranks = AccountHandler.Ranks.valueOf(product.toUpperCase());
                    AccountHandler.setRank(uuid, ranks);
                } catch(Exception e) {}

				CommandDispatcher.sendToAll("purchase " + name + " " + product);

				Server.post("http://167.114.98.199:8081/recent-customer?n=" + name + "&p=" + product);
				return true;
			}
		}.setRequiredRank(AccountHandler.Ranks.OWNER);

		new Server();
		new Voting();
	}
}