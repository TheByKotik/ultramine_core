package org.ultramine.commands.basic;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import static net.minecraft.util.EnumChatFormatting.*;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.MultiWorld;
import org.ultramine.server.Teleporter;

public class TechCommands
{
	@Command(
			name = "id",
			group = "technical",
			permissions = {"command.id"},
			syntax = {"<%id>"}
	)
	public static void id(CommandContext ctx)
	{
		Item item = ctx.get("id").asItem();
		ctx.sendMessage("ID: %s", Item.itemRegistry.getNameForObject(item));
		ctx.sendMessage("Internal ID: %s", Item.itemRegistry.getIDForObject(item));
		ctx.sendMessage("Unlocalized name: %s", new ChatComponentText(item.getUnlocalizedName()));
		ctx.sendMessage("Localized name: %s", new ChatComponentTranslation(item.getUnlocalizedName()+".name"));
		ctx.sendMessage("Is block: %s", item instanceof ItemBlock);
		ctx.sendMessage("Class name: %s", item.getClass().getName());
	}
	
	@Command(
			name = "uptime",
			aliases = {"ticks", "lagometer"},
			group = "technical",
			permissions = {"command.uptime"}
	)
	public static void uptime(CommandContext ctx)
	{
		double tps = Math.round(ctx.getServer().currentTPS*10)/10d;
		double downtime = ctx.getServer().currentWait/1000/1000d;
		int load = (int)Math.round((50-downtime)/50*100);
		int uptime = (int)((System.currentTimeMillis() - ctx.getServer().startTime)/1000);
		ctx.sendMessage(DARK_GREEN, "command.uptime.msg.up", String.format("%dd %dh %dm %ds", uptime/(60*60*24), uptime/(60*60)%24, uptime/60%60, uptime%60));
		ctx.sendMessage(load > 100 ? RED : DARK_GREEN, "command.uptime.msg.load", Integer.toString(load).concat("%"));
		ctx.sendMessage(tps < 15 ? RED : DARK_GREEN, "command.uptime.msg.tps",  Double.toString(tps),
				Integer.toString((int)(tps/20*100)).concat("%"));
	}
	
	@Command(
			name = "debuginfo",
			group = "technical",
			permissions = {"command.debuginfo"},
			syntax = {
					"",
					"[chunk]",
					"<world>",
					"<player>"
			}
	)
	public static void debuginfo(CommandContext ctx)
	{
		if(ctx.getAction().equals("chunk"))
		{
			EntityPlayerMP player = ctx.getSenderAsPlayer();
			Chunk chunk = player.worldObj.getChunkFromChunkCoords(player.chunkCoordX, player.chunkCoordZ);
			ctx.sendMessage("Chunk: %s %s", chunk.xPosition, chunk.zPosition);
			ctx.sendMessage("EntityLiving: %s", chunk.getEntityCount());
			ctx.sendMessage("EntityMonster: %s", chunk.getEntityCountByType(EnumCreatureType.monster));
			ctx.sendMessage("EntityAnimal: %s", chunk.getEntityCountByType(EnumCreatureType.creature));
			ctx.sendMessage("EntityAmbient: %s", chunk.getEntityCountByType(EnumCreatureType.ambient));
			ctx.sendMessage("EntityWater: %s", chunk.getEntityCountByType(EnumCreatureType.waterCreature));
		}
		else if(ctx.contains("world") || ctx.getArgs().length == 0)
		{
			WorldServer world = ctx.contains("world") ? ctx.get("world").asWorld() : ctx.getSenderAsPlayer().getServerForPlayer();
			ctx.sendMessage("World: %s, Dimension: %s", world.getWorldInfo().getWorldName(), world.provider.dimensionId);
			ctx.sendMessage("Chunks loaded:  %s", world.theChunkProviderServer.getLoadedChunkCount());
			ctx.sendMessage("Chunks active:  %s", world.getActiveChunkSetSize());
			ctx.sendMessage("Chunks for unload:  %s", world.theChunkProviderServer.chunksToUnload.size());
			ctx.sendMessage("Players: %s", world.playerEntities.size());
			ctx.sendMessage("Entities:  %s", world.loadedEntityList.size());
			ctx.sendMessage("TileEntities:  %s", world.loadedTileEntityList.size());
		}
		else
		{
			EntityPlayerMP player = ctx.get("player").asPlayer();
			ctx.sendMessage("Username: %s", player.getCommandSenderName());
			ctx.sendMessage("UUID: %s", player.getGameProfile().getId());
			ctx.sendMessage("Chunk send rate: %s", player.getChunkMgr().getRate());
			ctx.sendMessage("View distance: %s", player.getRenderDistance());
		}
	}
	
	@Command(
			name = "memstat",
			group = "technical",
			permissions = {"command.memstat"}
	)
	public static void memstat(CommandContext ctx)
	{
		ctx.sendMessage("Heap max: %sm", Runtime.getRuntime().maxMemory() >> 20);
		ctx.sendMessage("Heap total: %sm", Runtime.getRuntime().totalMemory() >> 20);
		ctx.sendMessage("Heap free: %sm", Runtime.getRuntime().freeMemory() >> 20);
	}
	
	@Command(
			name = "multiworld",
			aliases = {"mw", "mv"},
			group = "technical",
			permissions = {"command.multiworld"},
			syntax = {
					"[list]",
					"[load unload goto] <%world>" //No world validation
			}
	)
	public static void multiworld(CommandContext ctx)
	{
		MultiWorld mw = ctx.getServer().getMultiWorld();
		
		if(ctx.getAction().equals("list"))
		{
			ctx.sendMessage("command.warplist.head=Warp list:");
			for(int dim : DimensionManager.getStaticDimensionIDs())
			{
				String name = mw.getNameByID(dim);
				ctx.sendMessage(GOLD, "    - %s - %s - %s", dim, name != null ? name : "unnamed", mw.getWorldByID(dim) != null ? "loaded" : "unloaded");
			}
			return;
		}
		
		int dim = ctx.get("world").asInt();

		if(!DimensionManager.isDimensionRegistered(dim))
			ctx.failure("command.multiworld.notregistered");
		
		if(ctx.getAction().equals("load"))
		{
			if(mw.getWorldByID(dim) != null)
				ctx.failure("command.multiworld.alreadyloaded");
			
			DimensionManager.initDimension(dim);
			ctx.sendMessage("command.multiworld.load");
		}
		else if(ctx.getAction().equals("unload"))
		{
			if(mw.getWorldByID(dim) == null)
				ctx.failure("command.multiworld.notloaded");
			
			DimensionManager.unloadWorld(dim);
			ctx.sendMessage("command.multiworld.unload");
		}
		else if(ctx.getAction().equals("goto"))
		{
			WorldServer world = mw.getWorldByID(dim);
			if(world == null)
				ctx.failure("command.multiworld.notloaded");
			
			Teleporter.tpNow(ctx.getSenderAsPlayer(), dim, world.getWorldInfo().getSpawnX(), world.getWorldInfo().getSpawnY(), world.getWorldInfo().getSpawnZ());
		}
	}
}
