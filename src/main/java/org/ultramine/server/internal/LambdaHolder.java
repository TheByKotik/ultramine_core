package org.ultramine.server.internal;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import java.util.function.Predicate;

public class LambdaHolder
{
	public static final Predicate<?> ENTITY_REMOVAL_PREDICATE = o -> ((Entity)o).removeThisTick;
	public static final Predicate<?> TILE_ENTITY_REMOVAL_PREDICATE = o -> ((TileEntity)o).removeThisTick;
}
