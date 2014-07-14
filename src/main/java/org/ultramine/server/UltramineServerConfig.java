package org.ultramine.server;

import java.util.LinkedHashMap;
import java.util.Map;

public class UltramineServerConfig
{
	public WatchdogThreadConfig	watchdogThread = new WatchdogThreadConfig();
	public Teleportation teleportation = new Teleportation();
	public SpawnLocations spawnLocations = new SpawnLocations();
	public VanillaConfig		vanilla = new VanillaConfig();
	public Map<String, Database> databases;
	public SQLServerStorage inSQLServerStorage = new SQLServerStorage();

	public static class WatchdogThreadConfig
	{
		public int timeout = 120;
		public boolean restart = true;
	}
	
	public static class Teleportation
	{
		public int cooldown = 60;
		public int delay = 5;
	}
	
	public static class SpawnLocations
	{
		public String firstSpawn = "spawn";
		public String deathSpawn = "spawn";
		public boolean respawnOnBed = true;
	}
	
	public static class Database
	{
		public String url; //jdbc:mysql://localhost:3306/databasename
		public String username;
		public String password;
		public int maxConnections;
	}
	
	public static class SQLServerStorage
	{
		public boolean enabled = false;
		public String database = "global";
		public String tablePrefix = "mc_";
	}
	
	
	
	public static class VanillaConfig
	{
		public boolean	enableQuery					= false;
		public int		queryPort					= 25565;
		public boolean	allowFlight					= false;
		public boolean	announcePlayerAchievements	= true;
		public int		serverPort					= 25565;
		public boolean	enableRcon					= false;
		public int		rconPort					= 0;
		public String	rconPassword				= "";
		public boolean	forceGamemode				= false;
		public String	serverIp					= "";
		public boolean	whiteList					= false;
		public boolean	snooperEnabled				= true;
		public boolean	hardcore					= false;
		public boolean	onlineMode					= true;
		public String	resourcePack				= "";
		public boolean	enableCommandBlock			= false;
		public int		playerIdleTimeout			= 0;
		public int		gamemode					= 0;
		public int		maxPlayers					= 20;
		public String	motd						= "A Minecraft Server";

		public Map<String, Object> 	unresolved = new LinkedHashMap<String, Object>();
	}
}
