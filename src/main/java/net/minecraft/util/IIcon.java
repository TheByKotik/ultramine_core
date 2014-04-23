package net.minecraft.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IIcon
{
	@SideOnly(Side.CLIENT)
	int getIconWidth();

	@SideOnly(Side.CLIENT)
	int getIconHeight();

	@SideOnly(Side.CLIENT)
	float getMinU();

	@SideOnly(Side.CLIENT)
	float getMaxU();

	@SideOnly(Side.CLIENT)
	float getInterpolatedU(double var1);

	@SideOnly(Side.CLIENT)
	float getMinV();

	@SideOnly(Side.CLIENT)
	float getMaxV();

	@SideOnly(Side.CLIENT)
	float getInterpolatedV(double var1);

	@SideOnly(Side.CLIENT)
	String getIconName();
}