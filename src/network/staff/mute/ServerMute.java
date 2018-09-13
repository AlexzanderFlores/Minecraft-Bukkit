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
    private ServerMute instance;

    public ServerMute() {
        instance = this;

        new CommandBase("serverMute") {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                if(EventUtil.isListener(instance)) {
                    EventUtil.unregister(instance);
                    MessageHandler.alert("Server Mute has been toggled &coff.");
                } else {
                    EventUtil.register(instance);
                    MessageHandler.alert("Server Mute has been toggled &aon.");
                }
                return true;
            }
        }.enableDelay(5).setRequiredRank(AccountHandler.Ranks.SENIOR_STAFF);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if(!AccountHandler.Ranks.TRIAL.hasRank(event.getPlayer())) {
            MessageHandler.sendMessage(event.getPlayer(), "&cThis server has been temporarily muted by a staff member.");
            event.setCancelled(true);
        }
    }
}
