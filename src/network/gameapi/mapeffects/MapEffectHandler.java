package network.gameapi.mapeffects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

public class MapEffectHandler {
    private static List<MapEffectsBase> effects = null;

    public MapEffectHandler(World world) {
        if(effects != null) {
        	for(MapEffectsBase effect : effects) {
            	if(effect.getName() != null && effect.getName().equals(world.getName())) {
                	effect.execute(world);
                    break;
                }
            }
            effects.clear();
            effects = null;
        }
    }

    public static void addEffect(MapEffectsBase effect) {
        if(effects == null) {
            effects = new ArrayList<MapEffectsBase>();
        }
        effects.add(effect);
    }
}
