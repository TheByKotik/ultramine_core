package net.minecraft.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;

public class AABBPool
{
	private final int maxNumCleans;
	private final int numEntriesToRemove;
	private final List listAABB = new ArrayList();
	private int nextPoolIndex;
	private int maxPoolIndex;
	private int numCleans;
	private static final String __OBFID = "CL_00000609";

	public AABBPool(int par1, int par2)
	{
		this.maxNumCleans = par1;
		this.numEntriesToRemove = par2;
	}

	public AxisAlignedBB getAABB(double par1, double par3, double par5, double par7, double par9, double par11)
	{
		AxisAlignedBB axisalignedbb;

		if (this.nextPoolIndex >= this.listAABB.size())
		{
			axisalignedbb = new AxisAlignedBB(par1, par3, par5, par7, par9, par11);
			this.listAABB.add(axisalignedbb);
		}
		else
		{
			axisalignedbb = (AxisAlignedBB)this.listAABB.get(this.nextPoolIndex);
			axisalignedbb.setBounds(par1, par3, par5, par7, par9, par11);
		}

		++this.nextPoolIndex;
		return axisalignedbb;
	}

	public void cleanPool()
	{
		if (this.nextPoolIndex > this.maxPoolIndex)
		{
			this.maxPoolIndex = this.nextPoolIndex;
		}

		if (this.numCleans++ == this.maxNumCleans)
		{
			int i = Math.max(this.maxPoolIndex, this.listAABB.size() - this.numEntriesToRemove);

			while (this.listAABB.size() > i)
			{
				this.listAABB.remove(i);
			}

			this.maxPoolIndex = 0;
			this.numCleans = 0;
		}

		this.nextPoolIndex = 0;
	}

	@SideOnly(Side.CLIENT)
	public void clearPool()
	{
		this.nextPoolIndex = 0;
		this.listAABB.clear();
	}

	public int getlistAABBsize()
	{
		return this.listAABB.size();
	}

	public int getnextPoolIndex()
	{
		return this.nextPoolIndex;
	}
}