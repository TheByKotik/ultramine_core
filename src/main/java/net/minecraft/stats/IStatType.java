package net.minecraft.stats;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IStatType
{
	@SideOnly(Side.CLIENT)
	String format(int var1);
}