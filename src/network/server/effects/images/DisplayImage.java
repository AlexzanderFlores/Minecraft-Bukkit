package network.server.effects.images;

import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import network.server.util.ImageMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.inventivetalent.animatedframes.AnimatedFrame;
import org.inventivetalent.animatedframes.AnimatedFramesPlugin;
import org.inventivetalent.animatedframes.Callback;

import java.util.UUID;

public class DisplayImage implements Listener {
    private String name;
    private Location bottomLeft;
    private Location topRight;
    private String url;
    private UUID uuid;
    private AnimatedFramesPlugin plugin;
    private AnimatedFrame frame;

    public DisplayImage(String name, Location bottomLeft, Location topRight) {
        this(name, bottomLeft, topRight, null);
    }

    public DisplayImage(String name, Location bottomLeft, Location topRight, String url) {
        this.name = name;
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.url = url;
        this.uuid = UUID.randomUUID();

        plugin = (AnimatedFramesPlugin) Bukkit.getPluginManager().getPlugin("AnimatedFrames");

        EventUtil.register(this);
    }

    protected String getUrl() {
        return this.url;
    }

    protected DisplayImage setUrl(String url) {
        this.url = url;
        return this;
    }

    protected String getUuid() {
        return this.uuid.toString();
    }

    public void display() {
        new AsyncDelayedTask(new Runnable() {
            @Override
            public void run() {
                removeFrame();

                new AsyncDelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        ItemFrame firstFrame = ImageMap.getItemFrame(bottomLeft);
                        ItemFrame secondFrame = ImageMap.getItemFrame(topRight);

                        if(firstFrame == null || secondFrame == null) {
                            return;
                        }

                        frame = null;
                        do {
                            try {
                                frame = plugin.frameManager.createFrame(name, url, firstFrame, secondFrame);
                            } catch(Exception e) {}
                        } while(frame == null);

                        plugin.frameManager.writeToFile(frame);
                        plugin.frameManager.writeIndexToFile();

                        frame.refresh();
                        plugin.frameManager.startFrame(frame);

                        frame.startCallback = new Callback<Void>() {
                            @Override
                            public void call(Void aVoid) {
                                new AsyncDelayedTask(new Runnable() {
                                    @Override
                                    public void run() {
                                        for(Player player : bottomLeft.getWorld().getPlayers()) {
                                            frame.addViewer(player);
                                        }
                                    }
                                });
                            }
                        };

                        frame.setPlaying(true);
                    }
                }, 20);
            }
        }, 20);
    }

    private void removeFrame() {
        if(plugin.frameManager.doesFrameExist(name)) {
            AnimatedFrame frame = this.plugin.frameManager.getFrame(name);
            plugin.frameManager.stopFrame(frame);

            new DelayedTask(new Runnable() {
                @Override
                public void run() {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        frame.removeViewer(player);
                    }

                    frame.clearFrames();
                    plugin.frameManager.removeFrame(frame);
                }
            });
        }
    }

    @EventHandler
    public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
        if(frame != null) {
            frame.addViewer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        if(frame != null) {
            frame.removeViewer(event.getPlayer());
        }
    }
}
