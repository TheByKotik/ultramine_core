package net.minecraft.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.world.storage.WorldInfo;

public final class WorldSettings
{
	private final long seed;
	private final WorldSettings.GameType theGameType;
	private final boolean mapFeaturesEnabled;
	private final boolean hardcoreEnabled;
	private final WorldType terrainType;
	private boolean commandsAllowed;
	private boolean bonusChestEnabled;
	private String field_82751_h;
	private static final String __OBFID = "CL_00000147";

	public WorldSettings(long par1, WorldSettings.GameType par3EnumGameType, boolean par4, boolean par5, WorldType par6WorldType)
	{
		this.field_82751_h = "";
		this.seed = par1;
		this.theGameType = par3EnumGameType;
		this.mapFeaturesEnabled = par4;
		this.hardcoreEnabled = par5;
		this.terrainType = par6WorldType;
	}

	public WorldSettings(WorldInfo par1WorldInfo)
	{
		this(par1WorldInfo.getSeed(), par1WorldInfo.getGameType(), par1WorldInfo.isMapFeaturesEnabled(), par1WorldInfo.isHardcoreModeEnabled(), par1WorldInfo.getTerrainType());
	}

	public WorldSettings enableBonusChest()
	{
		this.bonusChestEnabled = true;
		return this;
	}

	public WorldSettings func_82750_a(String par1Str)
	{
		this.field_82751_h = par1Str;
		return this;
	}

	@SideOnly(Side.CLIENT)
	public WorldSettings enableCommands()
	{
		this.commandsAllowed = true;
		return this;
	}

	public boolean isBonusChestEnabled()
	{
		return this.bonusChestEnabled;
	}

	public long getSeed()
	{
		return this.seed;
	}

	public WorldSettings.GameType getGameType()
	{
		return this.theGameType;
	}

	public boolean getHardcoreEnabled()
	{
		return this.hardcoreEnabled;
	}

	public boolean isMapFeaturesEnabled()
	{
		return this.mapFeaturesEnabled;
	}

	public WorldType getTerrainType()
	{
		return this.terrainType;
	}

	public boolean areCommandsAllowed()
	{
		return this.commandsAllowed;
	}

	public static WorldSettings.GameType getGameTypeById(int par0)
	{
		return WorldSettings.GameType.getByID(par0);
	}

	public String func_82749_j()
	{
		return this.field_82751_h;
	}

	public static enum GameType
	{
		NOT_SET(-1, ""),
		SURVIVAL(0, "survival"),
		CREATIVE(1, "creative"),
		ADVENTURE(2, "adventure");
		int id;
		String name;

		private static final String __OBFID = "CL_00000148";

		private GameType(int par3, String par4Str)
		{
			this.id = par3;
			this.name = par4Str;
		}

		public int getID()
		{
			return this.id;
		}

		public String getName()
		{
			return this.name;
		}

		public void configurePlayerCapabilities(PlayerCapabilities par1PlayerCapabilities)
		{
			if (this == CREATIVE)
			{
				par1PlayerCapabilities.allowFlying = true;
				par1PlayerCapabilities.isCreativeMode = true;
				par1PlayerCapabilities.disableDamage = true;
			}
			else
			{
				par1PlayerCapabilities.allowFlying = false;
				par1PlayerCapabilities.isCreativeMode = false;
				par1PlayerCapabilities.disableDamage = false;
				par1PlayerCapabilities.isFlying = false;
			}

			par1PlayerCapabilities.allowEdit = !this.isAdventure();
		}

		public boolean isAdventure()
		{
			return this == ADVENTURE;
		}

		public boolean isCreative()
		{
			return this == CREATIVE;
		}

		@SideOnly(Side.CLIENT)
		public boolean isSurvivalOrAdventure()
		{
			return this == SURVIVAL || this == ADVENTURE;
		}

		public static WorldSettings.GameType getByID(int par0)
		{
			WorldSettings.GameType[] agametype = values();
			int j = agametype.length;

			for (int k = 0; k < j; ++k)
			{
				WorldSettings.GameType gametype = agametype[k];

				if (gametype.id == par0)
				{
					return gametype;
				}
			}

			return SURVIVAL;
		}

		@SideOnly(Side.CLIENT)
		public static WorldSettings.GameType getByName(String par0Str)
		{
			WorldSettings.GameType[] agametype = values();
			int i = agametype.length;

			for (int j = 0; j < i; ++j)
			{
				WorldSettings.GameType gametype = agametype[j];

				if (gametype.name.equals(par0Str))
				{
					return gametype;
				}
			}

			return SURVIVAL;
		}
	}
}