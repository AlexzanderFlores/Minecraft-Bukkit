package network.server.effects.images;

import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.PlayerItemFrameInteractEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import network.server.util.ImageMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.inventivetalent.animatedframes.AnimatedFrame;
import org.inventivetalent.animatedframes.AnimatedFramesPlugin;
import org.inventivetalent.mapmanager.event.MapInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisplayImage implements Listener {
    public enum ImageID {
        HUB_LEFT(new Vector(1679, 5, -1302), new Vector(1683, 8, -1302)),
        HUB_RIGHT(new Vector(1685, 5, -1302), new Vector(1689, 8, -1302)),
        PARKOUR(new Vector(1612, 5, -1306), new Vector(1616, 8, -1306)),
        ENDLESS_PARKOUR(new Vector(1616, 5, -1256), new Vector(1612, 8, -1256)),
        RECENT_CUSTOMER(new Vector(1689, 5, -1260), new Vector(1687, 8, -1260)),
        RECENT_VOTER(new Vector(1685, 5, -1260), new Vector(1683, 8, -1260)),
        RECENT_DISCORD(new Vector(1681, 5, -1260), new Vector(1679, 8, -1260)),
        ONEVSONE_ELO(new Vector(-23, 13, -36), new Vector(-23, 15, -40));

        private Location bottomLeft;
        private Location topRight;

        ImageID(Vector bottomLeft, Vector topRight) {
            this.bottomLeft = bottomLeft.toLocation(Bukkit.getWorlds().get(0));
            this.topRight = topRight.toLocation(Bukkit.getWorlds().get(0));
        }
    }

    private static Map<ImageID, DisplayImage> images = null;

    private ImageID id;
    private Location bottomLeft;
    private Location topRight;
    private String name;
    private String url;
    private UUID uuid;
    private AnimatedFramesPlugin plugin;
    private AnimatedFrame frame;

    DisplayImage(ImageID id) {
        this(id, null);
    }

    public DisplayImage(ImageID id, String url) {
        this.id = id;
        this.bottomLeft = id.bottomLeft;
        this.topRight = id.topRight;
        this.uuid = UUID.randomUUID();

        setUrl(url);

        plugin = (AnimatedFramesPlugin) Bukkit.getPluginManager().getPlugin("AnimatedFrames");

        EventUtil.register(this);

        if(images == null) {
            images = new HashMap<>();
        }

        if(images.containsKey(id)) {
            images.get(id).remove();
        }

        images.put(id, this);
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
        new AsyncDelayedTask(() -> {
            removeFrame();

            new AsyncDelayedTask(() -> {
                ItemFrame firstFrame = ImageMap.getItemFrame(bottomLeft);
                ItemFrame secondFrame = ImageMap.getItemFrame(topRight);

                if(firstFrame == null || secondFrame == null) {
                    return;
                }

                frame = null;
                do {
                    try {
                        frame = plugin.frameManager.createFrame(id.toString(), url, firstFrame, secondFrame);
                    } catch(Exception e) {
                    }
                } while (frame == null);

                plugin.frameManager.writeToFile(frame);
                plugin.frameManager.writeIndexToFile();

                frame.refresh();
                plugin.frameManager.startFrame(frame);

                frame.startCallback = aVoid -> new AsyncDelayedTask(() -> {
                    for(Player player : bottomLeft.getWorld().getPlayers()) {
                        frame.addViewer(player);
                    }
                });

                frame.setPlaying(true);
            }, 20);
        }, 20);
    }

    private void removeFrame() {
        if(plugin.frameManager.doesFrameExist(id.toString())) {
            AnimatedFrame frame = this.plugin.frameManager.getFrame(id.toString());
            plugin.frameManager.stopFrame(frame);

            for(Player player : Bukkit.getOnlinePlayers()) {
                frame.removeViewer(player);
            }

            frame.clearFrames();
            plugin.frameManager.removeFrame(frame);

            frame = null;
            plugin = null;
        }
    }

    private void remove() {
        removeFrame();

        id = null;
        bottomLeft = null;
        topRight = null;
        name = null;
        url = null;
        uuid = null;

        EventUtil.unregister(this);
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
