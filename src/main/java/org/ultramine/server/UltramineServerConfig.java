package org.ultramine.server;

public class UltramineServerConfig
{
	public WatchdogThreadConfig	watchdogThread = new WatchdogThreadConfig();
	public VanillaConfig		vanilla = new VanillaConfig();
	
	
	public static class WatchdogThreadConfig
	{
		public int timeout = 120;
		public boolean restart = true;
	}
	
	public static class VanillaConfig
	{
		public String	generatorSettings			= "";
		public int		opPermissionLevel			= 4;
		public boolean	allowNether					= true;
		public String	levelName					= "world";
		public boolean	enableQuery					= false;
		public int		queryPort					= 25565;
		public boolean	allowFlight					= false;
		public boolean	announcePlayerAchievements	= true;
		public int		serverPort					= 25565;
		public String	levelType					= "DEFAULT";
		public boolean	enableRcon					= false;
		public int		rconPort					= 0;
		public String	rconPassword				= "";
		public boolean	forceGamemode				= false;
		public String	levelSeed					= "";
		public String	serverIp					= "";
		public int		maxBuildHeight				= 256;
		public boolean	spawnNPCs					= true;
		public boolean	whiteList					= false;
		public boolean	spawnAnimals				= true;
		public boolean	snooperEnabled				= true;
		public boolean	hardcore					= false;
		public boolean	onlineMode					= true;
		public String	resourcePack				= "";
		public boolean	pvp							= true;
		public int		difficulty					= 1;
		public boolean	enableCommandBlock			= false;
		public int		playerIdleTimeout			= 0;
		public int		gamemode					= 0;
		public int		maxPlayers					= 20;
		public boolean	spawnMonsters				= true;
		public int		viewDistance				= 10;
		public boolean	generateStructures			= true;
		public String	motd						= "A Minecraft Server";
	}
}
