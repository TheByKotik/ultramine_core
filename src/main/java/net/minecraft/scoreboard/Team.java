package net.minecraft.scoreboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class Team
{
	private static final String __OBFID = "CL_00000621";

	public boolean isSameTeam(Team par1Team)
	{
		return par1Team == null ? false : this == par1Team;
	}

	public abstract String getRegisteredName();

	public abstract String formatString(String var1);

	@SideOnly(Side.CLIENT)
	public abstract boolean func_98297_h();

	public abstract boolean getAllowFriendlyFire();
}