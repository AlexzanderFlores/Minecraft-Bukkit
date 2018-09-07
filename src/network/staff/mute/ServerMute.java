package network.staff.mute;

import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.util.EventUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ServerMute implements Listener {
    private boolean muted = false;

    public ServerMute() {
        new CommandBase("serverMute") {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                muted = !muted;
                MessageHandler.alert("Server Mute has been toggled.");
                return true;
            }
        }.setRequiredRank(AccountHandler.Ranks.SENIOR_STAFF);

        EventUtil.register(this);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if(muted && !AccountHandler.Ranks.TRIAL.hasRank(event.getPlayer())) {
            MessageHandler.sendMessage(event.getPlayer(), "&cThis server has been temporarily muted by a staff member.");
            event.setCancelled(true);
        }
    }
}
