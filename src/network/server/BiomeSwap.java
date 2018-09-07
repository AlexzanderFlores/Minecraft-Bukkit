package network.server;

import net.minecraft.server.v1_8_R3.BiomeBase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiomeSwap {
	public enum Biome {
		OCEAN(0), PLAINS(1), DESERT(2), EXTREME_HILLS(3), FOREST(4), TAIGA(5), SWAMPLAND(6), RIVER(7), HELL(8), SKY(9), FROZEN_OCEAN(10), FROZEN_RIVER(11),
		ICE_PLAINS(12), ICE_MOUNTAINS(13), MUSHROOM_ISLAND(14), MUSHROOM_SHORE(15), BEACH(16), DESERT_HILLS(17), FOREST_HILLS(18), TAIGA_HILLS(19),
		SMALL_MOUNTAINS(20), JUNGLE(21), JUNGLE_HILLS(22), JUNGLE_EDGE(23), DEEP_OCEAN(24), STONE_BEACH(25), COLD_BEACH(26), BIRCH_FOREST(27),
		BIRCH_FOREST_HILLS(28), ROOFED_FOREST(29), COLD_TAIGA(30), COLD_TAIGA_HILLS(31), MEGA_TAIGA(32), MEGA_TAIGA_HILLS(33), EXTREME_HILLS_PLUS(34),
		SAVANNA(35), SAVANNA_PLATEAU(36), MESA(37), MESA_PLATEAU_F(38), MESA_PLATEAU(39);

		private final int id;

		private Biome(int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}
	}
	
	public static void setUpUHC() {
		List<Biome> badBiomes = new ArrayList<Biome>();
		badBiomes.add(Biome.DEEP_OCEAN);
		badBiomes.add(Biome.OCEAN);
		badBiomes.add(Biome.JUNGLE);
		badBiomes.add(Biome.JUNGLE_EDGE);
		badBiomes.add(Biome.JUNGLE_HILLS);
		badBiomes.add(Biome.MESA);
		badBiomes.add(Biome.MESA_PLATEAU);
		badBiomes.add(Biome.MESA_PLATEAU_F);
		List<Biome> goodBiomes = new ArrayList<Biome>();
		goodBiomes.add(Biome.PLAINS);
		goodBiomes.add(Biome.FOREST);
		goodBiomes.add(Biome.FOREST_HILLS);
		goodBiomes.add(Biome.SWAMPLAND);
		Random random = new Random();
		for(Biome biome : badBiomes) {
			Biome target = null;
			do {
				target = goodBiomes.get(random.nextInt(goodBiomes.size()));
			} while(badBiomes.contains(target));
			swap(biome, target);
		}
		badBiomes.clear();
		badBiomes = null;
		goodBiomes.clear();
		goodBiomes = null;
	}
	
	public static void swap(Biome from, Biome to) {
		try {
			Field field = BiomeBase.class.getDeclaredField("biomes");
			field.setAccessible(true);
			BiomeBase [] biomes = (BiomeBase []) field.get(null);
			biomes[from.getId()] = biomes[to.getId()];
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
