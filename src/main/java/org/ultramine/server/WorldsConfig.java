package org.ultramine.server;

import java.util.HashMap;
import java.util.Map;

public class WorldsConfig
{
	public WorldConfig global = new WorldConfig();
	public Map<String, WorldConfig> worlds = new HashMap<String, WorldConfig>();
	
	public static class WorldConfig
	{
		public int dimension;
		public Generation generation;
		public MobSpawn mobSpawn;
		public Settings settings;
		public ChunkLoading chunkLoading;
		
		public static class Generation
		{
			public String seed;
			public int providerID = 0;
			public String levelType = "DEFAULT";
			public String generatorSettings = "";
			public boolean generateStructures = true;
		}
		
		public static class MobSpawn
		{
			public boolean spawnAnimals = true;
			public boolean spawnMonsters = true;
			public boolean spawnNPCs = true;
		}
		
		public static class Settings
		{
			public String difficulty = "1";
			public boolean pvp = true;
			public int maxBuildHeight = 256;
			public WorldTime time = WorldTime.NORMAL;
			public Weather weather = Weather.NORMAL;
			
			public enum WorldTime
			{
				NORMAL, DAY, NIGHT, FIXED
			}
			
			public enum Weather
			{
				NORMAL, NONE, RAIN, THUNDER
			}
		}
		
		public static class ChunkLoading
		{
			public int viewDistance  = 10;
			public int chunkUpdateRadius = 7;
			public int chunkCacheSize;
			public boolean enableChunkLoaders = true;
		}
	}
}
