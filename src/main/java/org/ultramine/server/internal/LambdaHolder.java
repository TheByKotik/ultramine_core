package org.ultramine.server.internal;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.openhft.koloboke.collect.map.ShortObjMap;
import net.openhft.koloboke.collect.map.hash.HashShortObjMaps;

import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Java 8 features like lambdas must not be used in net.minecraft classes, so all lambdas used there, located here*/
public class LambdaHolder
{
	public static final Predicate<?> ENTITY_REMOVAL_PREDICATE = o -> ((Entity)o).removeThisTick;
	public static final Predicate<?> TILE_ENTITY_REMOVAL_PREDICATE = o -> ((TileEntity)o).removeThisTick;

	private static final Supplier<TreeSet<?>> NEW_TREE_SET = TreeSet::new;
	private static final Supplier<ShortObjMap<?>> NEW_SHORT_OBJ_MAP = HashShortObjMaps::newMutableMap;

	@SuppressWarnings("unchecked")
	public static <T> Supplier<TreeSet<T>> newTreeSet()
	{
		return (Supplier<TreeSet<T>>) (Object) NEW_TREE_SET;
	}

	@SuppressWarnings("unchecked")
	public static <T> Supplier<ShortObjMap<T>> newShortObjMap()
	{
		return (Supplier<ShortObjMap<T>>) (Object) NEW_SHORT_OBJ_MAP;
	}
}
