package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class PathEntity
{
	private final PathPoint[] points;
	private int currentPathIndex;
	private int pathLength;
	private static final String __OBFID = "CL_00000575";

	public PathEntity(PathPoint[] par1ArrayOfPathPoint)
	{
		this.points = par1ArrayOfPathPoint;
		this.pathLength = par1ArrayOfPathPoint.length;
	}

	public void incrementPathIndex()
	{
		++this.currentPathIndex;
	}

	public boolean isFinished()
	{
		return this.currentPathIndex >= this.pathLength;
	}

	public PathPoint getFinalPathPoint()
	{
		return this.pathLength > 0 ? this.points[this.pathLength - 1] : null;
	}

	public PathPoint getPathPointFromIndex(int par1)
	{
		return this.points[par1];
	}

	public int getCurrentPathLength()
	{
		return this.pathLength;
	}

	public void setCurrentPathLength(int par1)
	{
		this.pathLength = par1;
	}

	public int getCurrentPathIndex()
	{
		return this.currentPathIndex;
	}

	public void setCurrentPathIndex(int par1)
	{
		this.currentPathIndex = par1;
	}

	public Vec3 getVectorFromIndex(Entity par1Entity, int par2)
	{
		double d0 = (double)this.points[par2].xCoord + (double)((int)(par1Entity.width + 1.0F)) * 0.5D;
		double d1 = (double)this.points[par2].yCoord;
		double d2 = (double)this.points[par2].zCoord + (double)((int)(par1Entity.width + 1.0F)) * 0.5D;
		return par1Entity.worldObj.getWorldVec3Pool().getVecFromPool(d0, d1, d2);
	}

	public Vec3 getPosition(Entity par1Entity)
	{
		return this.getVectorFromIndex(par1Entity, this.currentPathIndex);
	}

	public boolean isSamePath(PathEntity par1PathEntity)
	{
		if (par1PathEntity == null)
		{
			return false;
		}
		else if (par1PathEntity.points.length != this.points.length)
		{
			return false;
		}
		else
		{
			for (int i = 0; i < this.points.length; ++i)
			{
				if (this.points[i].xCoord != par1PathEntity.points[i].xCoord || this.points[i].yCoord != par1PathEntity.points[i].yCoord || this.points[i].zCoord != par1PathEntity.points[i].zCoord)
				{
					return false;
				}
			}

			return true;
		}
	}

	public boolean isDestinationSame(Vec3 par1Vec3)
	{
		PathPoint pathpoint = this.getFinalPathPoint();
		return pathpoint == null ? false : pathpoint.xCoord == (int)par1Vec3.xCoord && pathpoint.zCoord == (int)par1Vec3.zCoord;
	}
}