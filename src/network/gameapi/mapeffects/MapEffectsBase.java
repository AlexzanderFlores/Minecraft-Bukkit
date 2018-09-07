package network.gameapi.mapeffects;

import org.bukkit.World;

public abstract class MapEffectsBase {
    private String name = null;

    public MapEffectsBase(String name) {
        this.name = name;
        MapEffectHandler.addEffect(this);
    }

    public String getName() {
        return this.name;
    }

    public abstract void execute(World world);
}
