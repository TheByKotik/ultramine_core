package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPlayerInfo
{
	public final String name;
	private final String nameinLowerCase;
	public int responseTime;
	private static final String __OBFID = "CL_00000888";

	public GuiPlayerInfo(String par1Str)
	{
		this.name = par1Str;
		this.nameinLowerCase = par1Str.toLowerCase();
	}
}