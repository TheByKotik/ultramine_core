package org.ultramine.server.event;

import org.ultramine.server.chunk.ChunkHash;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

@SideOnly(Side.SERVER)
public class ServerWorldEventProxy extends WorldEventProxy
{
	private final WorldServer world;
	private final WorldUpdateObject object = new WorldUpdateObject();
	private final TLongList neighborStack = new TLongArrayList();
	
	public ServerWorldEventProxy(WorldServer world)
	{
		this.world = world;
	}
	
	@Override
	public World getWorld()
	{
		return world;
	}
	
	@Override
	public void pushState(WorldUpdateObjectType state)
	{
		object.setType(state);
		current = this;
	}
	
	@Override
	public void popState()
	{
		object.setType(WorldUpdateObjectType.UNKNOWN);
		object.setEntity(null);
		object.setTileEntity(null);
		current = null;
	}
	
	@Override
	public void startEntity(Entity entity)
	{
		object.setEntity(entity);
	}
	
	@Override
	public void startTileEntity(TileEntity tile)
	{
		object.setTileEntity(tile);
	}
	
	@Override
	public void startBlock(Block block, int x, int y, int z)
	{
		object.setBlock(block, x, y, z);
	}
	
	@Override
	public void startNeighbor(int x, int y, int z)
	{
		neighborStack.add(ChunkHash.blockCoordToHash(x, y, z));
	}
	
	@Override
	public void endNeighbor()
	{
		neighborStack.removeAt(neighborStack.size() - 1);
	}
	
	@Override
	public void startInteract(ItemStack stack, Block block, int x, int y, int z)
	{
		object.setInteracting(true);
		object.setInteractBlock(stack, block, x, y, z);
	}
	
	@Override
	public void endInteract()
	{
		object.setInteracting(false);
	}
	
	@Override
	public GameProfile getObjectOwner()
	{
		switch(object.getType())
		{
		case BLOCK_EVENT:
		case BLOCK_PENDING:
		case BLOCK_RANDOM:
			TileEntity te = world.getTileEntity(object.getX(), object.getY(), object.getZ());
			if(te != null)
				return te.getObjectOwner();
			break;
		case ENTITY:
			return object.getEntity().getObjectOwner();
		case ENTITY_WEATHER:
			break;
		case PLAYER:
			return ((EntityPlayer)object.getEntity()).getGameProfile();
		case TILEE_ENTITY:
			return object.getTileEntity().getObjectOwner();
		case UNKNOWN:
			break;
		case WEATHER:
			break;
		}
		
		return null;
	}
	
	@Override
	public WorldUpdateObject getUpdateObject()
	{
		return object;
	}
	
	@Override
	public boolean canChangeBlock(int x, int y, int z, Block block, int meta, int flags)
	{
		if(world.theChunkProviderServer.isGenerating())
			return true;
		if(block != Blocks.air)
		{
			WorldUpdateObjectType type = object.getType();
			if(type == WorldUpdateObjectType.TILEE_ENTITY)
			{
				TileEntity tile = object.getTileEntity();
				if(tile.xCoord == x && tile.yCoord == y && tile.zCoord == z)
					return true;
			}
			else if(type == WorldUpdateObjectType.BLOCK_RANDOM || type == WorldUpdateObjectType.BLOCK_PENDING || type == WorldUpdateObjectType.BLOCK_EVENT)
			{
				if(x == object.getX() && y == object.getY() && z == object.getZ())
					return true;
			}
		}
		
		boolean isNeighbor = neighborStack.size() > 0;
		boolean isNeighborItself = isNeighbor && neighborStack.get(neighborStack.size() - 1) == ChunkHash.blockCoordToHash(x, y, z);
		
		return !MinecraftForge.EVENT_BUS.post(new SetBlockEvent(world, x, y, z, block, meta, object, isNeighbor, isNeighborItself));
	}
}
