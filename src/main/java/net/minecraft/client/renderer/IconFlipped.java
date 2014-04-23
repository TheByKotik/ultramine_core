package net.minecraft.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

@SideOnly(Side.CLIENT)
public class IconFlipped implements IIcon
{
	private final IIcon baseIcon;
	private final boolean flipU;
	private final boolean flipV;
	private static final String __OBFID = "CL_00001511";

	public IconFlipped(IIcon par1Icon, boolean par2, boolean par3)
	{
		this.baseIcon = par1Icon;
		this.flipU = par2;
		this.flipV = par3;
	}

	public int getIconWidth()
	{
		return this.baseIcon.getIconWidth();
	}

	public int getIconHeight()
	{
		return this.baseIcon.getIconHeight();
	}

	public float getMinU()
	{
		return this.flipU ? this.baseIcon.getMaxU() : this.baseIcon.getMinU();
	}

	public float getMaxU()
	{
		return this.flipU ? this.baseIcon.getMinU() : this.baseIcon.getMaxU();
	}

	public float getInterpolatedU(double par1)
	{
		float f = this.getMaxU() - this.getMinU();
		return this.getMinU() + f * ((float)par1 / 16.0F);
	}

	public float getMinV()
	{
		return this.flipV ? this.baseIcon.getMinV() : this.baseIcon.getMinV();
	}

	public float getMaxV()
	{
		return this.flipV ? this.baseIcon.getMinV() : this.baseIcon.getMaxV();
	}

	public float getInterpolatedV(double par1)
	{
		float f = this.getMaxV() - this.getMinV();
		return this.getMinV() + f * ((float)par1 / 16.0F);
	}

	public String getIconName()
	{
		return this.baseIcon.getIconName();
	}
}