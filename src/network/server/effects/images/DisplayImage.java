package network.server.effects.images;

import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.PlayerItemFrameInteractEvent;
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
import org.inventivetalent.mapmanager.event.MapInteractEvent;

import java.util.UUID;

public class DisplayImage implements Listener {
    public enum ImageID {
        HUB_LEFT,
        HUB_RIGHT,
        RECENT_CUSTOMER,
        RECENT_VOTER,
        RECENT_DISCORD
    }

    private ImageID id;
    private Location bottomLeft;
    private Location topRight;
    private String name;
    private String url;
    private UUID uuid;
    private AnimatedFramesPlugin plugin;
    private AnimatedFrame frame;

    DisplayImage(ImageID id, Location bottomLeft, Location topRight) {
        this(id, bottomLeft, topRight, null);
    }

    public DisplayImage(ImageID id, Location bottomLeft, Location topRight, String url) {
        this.id = id;
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.uuid = UUID.randomUUID();

        setUrl(url);

        plugin = (AnimatedFramesPlugin) Bukkit.getPluginManager().getPlugin("AnimatedFrames");

        EventUtil.register(this);
    }

    String getUrl() {
        return this.url;
    }

    void setUrl(String url) {
        this.name = url;
        this.url = Bukkit.getWorldContainer().getPath() + "/plugins/Core/media/" + url + ".png";
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
                                frame = plugin.frameManager.createFrame(id.toString(), url, firstFrame, secondFrame);
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
        if(plugin.frameManager.doesFrameExist(id.toString())) {
            AnimatedFrame frame = this.plugin.frameManager.getFrame(id.toString());
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

    @EventHandler
    public void onMapInteract(MapInteractEvent event) {
        if(frame != null) {
            for(UUID [] uuidArray : frame.getItemFrameUUIDs()) {
                for(UUID uuid : uuidArray) {
                    if(uuid.equals(event.getItemFrame().getUniqueId())) {
                        PlayerItemFrameInteractEvent playerItemFrameInteractEvent = new PlayerItemFrameInteractEvent(event.getPlayer(), id, name);
                        Bukkit.getPluginManager().callEvent(playerItemFrameInteractEvent);
                        return;
                    }
                }
            }
        }
    }
}
