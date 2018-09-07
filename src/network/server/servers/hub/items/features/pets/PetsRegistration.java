package network.server.servers.hub.items.features.pets;

import net.minecraft.server.v1_8_R3.*;
import network.server.servers.hub.items.features.pets.entities.*;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public enum PetsRegistration {
    COW(EntityCow.class, CowPet.class),
    PIG(EntityPig.class, PigPet.class),
    CHICKEN(EntityChicken.class, ChickenPet.class),
    WOLF(EntityWolf.class, WolfPet.class),
    MUSHROOM_COW(EntityMushroomCow.class, MushroomCowPet.class),
    OCELOT(EntityOcelot.class, OcelotPet.class),
    SHEEP(EntitySheep.class, SheepPet.class),
    HORSE(EntityHorse.class, HorsePet.class),
    SLIME(EntitySlime.class, SlimePet.class),
    MAGMA_CUBE(EntityMagmaCube.class, MagmaCubePet.class),
    SQUID(EntitySquid.class, SquidPet.class),
    SNOWMAN(EntitySnowman.class, SnowmanPet.class);

    private Class<? extends EntityInsentient> defaultClass;
    private Class<? extends EntityInsentient> customClass;

    private PetsRegistration(Class<? extends EntityInsentient> defaultClass, Class<? extends EntityInsentient> customClass) {
        this.defaultClass = defaultClass;
        this.customClass = customClass;
    }

    private static Object getPrivateStatic(Class clazz, String f) throws Exception {
        Field field = clazz.getDeclaredField(f);
        field.setAccessible(true);
        return field.get(null);
    }

    public void register() {
        try {
            ((Map) getPrivateStatic(EntityTypes.class, "c")).put(toString(), customClass);
            ((Map) getPrivateStatic(EntityTypes.class, "d")).put(customClass, toString());
            ((Map) getPrivateStatic(EntityTypes.class, "e")).put(Integer.valueOf(EntityType.valueOf(toString()).getTypeId()), customClass);
            ((Map) getPrivateStatic(EntityTypes.class, "f")).put(customClass, Integer.valueOf(EntityType.valueOf(toString()).getTypeId()));
            ((Map) getPrivateStatic(EntityTypes.class, "g")).put(toString(), Integer.valueOf(EntityType.valueOf(toString()).getTypeId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregister() {
        try {
            ((Map) getPrivateStatic(EntityTypes.class, "c")).put(toString(), defaultClass);
            ((Map) getPrivateStatic(EntityTypes.class, "d")).put(defaultClass, toString());
            ((Map) getPrivateStatic(EntityTypes.class, "e")).put(Integer.valueOf(EntityType.valueOf(toString()).getTypeId()), defaultClass);
            ((Map) getPrivateStatic(EntityTypes.class, "f")).put(defaultClass, Integer.valueOf(EntityType.valueOf(toString()).getTypeId()));
            ((Map) getPrivateStatic(EntityTypes.class, "g")).put(toString(), Integer.valueOf(EntityType.valueOf(toString()).getTypeId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
