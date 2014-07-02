package org.ultramine.server.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class WarpLocation
{
	public int dimension;
	public double x;
	public double y;
	public double z;
	public float yaw;
	public float pitch;
	public double randomRadius;
	
	public WarpLocation(int dimension, double x, double y, double z, float yaw, float pitch, double randomRadius)
	{
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.randomRadius = randomRadius;
	}
	
	public WarpLocation(int dimension, double x, double y, double z, float yaw, float pitch)
	{
		this(dimension, x, y, z, yaw, pitch, 0);
	}
	
	public WarpLocation(int dimension, double x, double y, double z)
	{
		this(dimension, x, y, z, 0, 0);
	}
	
	public void round()
	{
		x = (double)Math.round(x*100)/100.0;
		y = (double)Math.round(y*100)/100.0;
		z = (double)Math.round(z*100)/100.0;
		
		yaw = (float)Math.round(yaw*100)/100.0F;
		pitch = (float)Math.round(pitch*100)/100.0F;
	}
	
	public boolean equals(WarpLocation loc)
	{
		if(this == loc) return true;
		return
				Math.abs(x - loc.x) < 0.1 &&
				Math.abs(y - loc.y) < 0.1 &&
				Math.abs(z - loc.z) < 0.1;
	}
	
	public NBTTagCompound toNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setDouble("x", x);
		nbt.setDouble("y", y);
		nbt.setDouble("z", z);
		if(dimension != 0)
			nbt.setInteger("d", dimension);
		if(yaw != 0F)
			nbt.setFloat("w", yaw);
		if(pitch != 0F)
			nbt.setFloat("p", pitch);
		if(randomRadius != 0d)
			nbt.setDouble("r", randomRadius);
		return nbt;
	}
	
	public static WarpLocation getFromNBT(NBTTagCompound nbt)
	{
		double x = nbt.getDouble("x");
		double y = nbt.getDouble("y");
		double z = nbt.getDouble("z");
		int dimension = nbt.hasKey("d") ? nbt.getInteger("d") : 0;
		float yaw = nbt.hasKey("w") ? nbt.getFloat("w") : 0F;
		float pitch = nbt.hasKey("p") ? nbt.getFloat("p") : 0F;
		int randomRadius = nbt.hasKey("r") ? nbt.getInteger("r") : 0;
		
		return new WarpLocation(dimension, x, y, z, yaw, pitch, randomRadius);
	}
	
	public static WarpLocation getFromPlayer(EntityPlayer player)
	{
		WarpLocation s =  new WarpLocation(player.dimension, player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
		s.round();
		return s;
	}
}
