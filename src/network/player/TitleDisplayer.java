package network.player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import network.ProPlugin;
import network.server.util.StringUtil;
import network.server.util.TextConverter;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TitleDisplayer {
    private String name = null;
    private IChatBaseComponent title = null;
    private IChatBaseComponent subTitle = null;
    private int fadeIn = 20;
    private int stay = 20;
    private int fadeOut = 20;

    public TitleDisplayer(Player player, String title) {
        this(player, title, null);
    }

    public TitleDisplayer(Player player, String title, String subTitle) {
        this.name = player.getName();
        setTitle(title);
        if(subTitle != null) {
            setSubTitle(subTitle);
        }
    }

    public TitleDisplayer setTitle(String title) {
        this.title = IChatBaseComponent.ChatSerializer.a(TextConverter.convert(StringUtil.color(title)));
        return this;
    }

    public TitleDisplayer setSubTitle(String subTitle) {
        this.subTitle = IChatBaseComponent.ChatSerializer.a(TextConverter.convert(StringUtil.color(subTitle)));
        return this;
    }

    public TitleDisplayer setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    public TitleDisplayer setStay(int stay) {
        this.stay = stay;
        return this;
    }

    public TitleDisplayer setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

    public void display() {
        Player player = ProPlugin.getPlayer(name);
        if(player != null) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
            if(title != null) {
                craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, title));
                title = null;
            }
            if(subTitle != null) {
                craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subTitle));
                subTitle = null;
            }
            name = null;
        }
    }
}
