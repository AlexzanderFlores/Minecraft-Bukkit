package network.server.servers.hub.parkours;

import network.ProPlugin;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.util.EventUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Parkour implements Listener {
    public Parkour() {
        new CommandBase("checkpointFromVoting", 2) {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                Player player = ProPlugin.getPlayer(arguments[0]);
                if(player != null) {
                    // TODO: Add arguments[1] worth of checkpoints to this player
                }
                return true;
            }
        }.setRequiredRank(AccountHandler.Ranks.OWNER);

        EventUtil.register(this);
    }
}
