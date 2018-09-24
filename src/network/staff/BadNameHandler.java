package network.staff;

import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.DB;
import network.server.GeneralEvents;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class BadNameHandler implements Listener {
    private List<String> marked;
    private List<String> checked;

    public BadNameHandler() {
        marked = new ArrayList<>();
        checked = new ArrayList<>();

        new CommandBase("badName", 1, true) {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                UUID uuid = AccountHandler.getUUID(arguments[0]);
                Player target = Bukkit.getPlayer(uuid);
                String name;
                if(target == null) {
                    name = arguments[0];
                } else {
                    name = target.getName();
                }

                if(uuid == null) {
                    MessageHandler.sendMessage(sender, "&cThat player has not logged in or has already changed their name.");
                } else {
                    new AsyncDelayedTask(() -> {
                        Player staff = (Player) sender;

                        if(DB.STAFF_BAD_NAMES.isUUIDSet(uuid)) {
                            DB.STAFF_BAD_NAMES.deleteUUID(uuid);
                            MessageHandler.sendMessage(staff, name + " has been &cunmarked &xas having a bad name.");
                            marked.remove(name);
                            checked.add(name);
                            if(target != null) {
                                GeneralEvents.colorPlayerTab(target);
                            }
                        } else {
                            DB.STAFF_BAD_NAMES.insert("'" + uuid.toString() + "', '" + staff.getUniqueId().toString() + "', '" + name + "'");
                            MessageHandler.sendMessage(staff, name + " has been &cmarked &xas having a bad name.");
                            marked.add(name);
                            checked.add(name);
                            if(target != null) {
                                block(target); // To set tab name
                            }
                        }
                    });
                }
                return true;
            }
        }.setRequiredRank(AccountHandler.Ranks.TRIAL);

        EventUtil.register(this);
    }

    private boolean block(Player player) {
        String name = player.getName();

        if(!checked.contains(name)) {
            checked.add(name);
            if(DB.STAFF_BAD_NAMES.isUUIDSet(player.getUniqueId())) {
                marked.add(name);
            }
        }

        boolean isMarked = marked.contains(player.getName());
        if(isMarked) {
            player.setPlayerListName(ChatColor.GRAY + "Bad Name");
        }

        return isMarked;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(block(player)) {
            MessageHandler.sendMessage(player, "&cYou cannot send chat messages due to your IGN breaking our rules. After you change your name please contact a staff member.");
            event.setCancelled(true);
        }

        for(String name : marked) {
            if(event.getMessage().toLowerCase().contains(name.toLowerCase())) {
                event.getRecipients().clear();
                event.getRecipients().add(player);
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
        block(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        marked.remove(event.getPlayer().getName());
        checked.remove(event.getPlayer().getName());
    }
}
