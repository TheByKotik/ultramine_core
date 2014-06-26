package net.minecraft.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3Pool;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public interface IBlockAccess
{
	Block getBlock(int var1, int var2, int var3);

	TileEntity getTileEntity(int var1, int var2, int var3);

	@SideOnly(Side.CLIENT)
	int getLightBrightnessForSkyBlocks(int var1, int var2, int var3, int var4);

	int getBlockMetadata(int var1, int var2, int var3);

	boolean isAirBlock(int var1, int var2, int var3);

	@SideOnly(Side.CLIENT)
	BiomeGenBase getBiomeGenForCoords(int var1, int var2);

	@SideOnly(Side.CLIENT)
	int getHeight();

	@SideOnly(Side.CLIENT)
	boolean extendedLevelsInChunkCache();

	@Deprecated /* gone in 1.7.10, use direct access to Vec3.createVectorHelper instead */
	Vec3Pool getWorldVec3Pool();

	int isBlockProvidingPowerTo(int var1, int var2, int var3, int var4);

	/**
	 * FORGE: isSideSolid, pulled up from {@link World}
	 *
	 * @param x X coord
	 * @param y Y coord
	 * @param z Z coord
	 * @param side Side
	 * @param _default default return value
	 * @return if the block is solid on the side
	 */
	boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default);
}