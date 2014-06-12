package org.ultramine.server.util;

import net.minecraft.entity.player.EntityPlayer;

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
	
	public static WarpLocation getFromPlayer(EntityPlayer player)
	{
		WarpLocation s =  new WarpLocation(player.dimension, player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
		s.round();
		return s;
	}
}
