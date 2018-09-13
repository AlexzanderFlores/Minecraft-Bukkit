package network.staff;

import network.player.account.AccountHandler;
import network.server.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

class ClearChat {
    ClearChat() {
        new CommandBase("clearChat") {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                for(int a = 0; a < 100; ++a) {
                    Bukkit.broadcastMessage("");
                }
                return true;
            }
        }.setRequiredRank(AccountHandler.Ranks.STAFF);
    }
}
