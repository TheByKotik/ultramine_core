package org.ultramine.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.event.WorldEventProxy;
import org.ultramine.server.event.WorldUpdateObject;

public class UMHooks
{
	private static final Logger log = LogManager.getLogger();
	
	public static void printStackTrace(Throwable t)
	{
		log.warn("Direct Throwable.printStackTrace() call");
		WorldEventProxy wep = WorldEventProxy.getCurrent();
		if(wep != null)
		{
			int dim = wep.getWorld().provider.dimensionId;
			WorldUpdateObject obj = wep.getUpdateObject();
			switch(obj.getType())
			{
			case BLOCK_EVENT:
				log.warn("On block event update [{}]({}, {}, {})", dim, obj.getX(), obj.getY(), obj.getZ());
				break;
			case BLOCK_PENDING:
				log.warn("On block pending update [{}]({}, {}, {})", dim, obj.getX(), obj.getY(), obj.getZ());
				break;
			case BLOCK_RANDOM:
				log.warn("On block random update [{}]({}, {}, {})", dim, obj.getX(), obj.getY(), obj.getZ());
				break;
			case ENTITY:
				Entity ent = obj.getEntity();
				log.warn("On entity update [{}]({}, {}, {}). Entity: {}, Class: {}", dim, ent.posX, ent.posY, ent.posZ, ent, ent.getClass().getName());
				break;
			case ENTITY_WEATHER:
				Entity went = obj.getEntity();
				log.warn("On weather entity update [{}]({}, {}, {}). Entity: {}, Class: {}", dim, went.posX, went.posY, went.posZ, went, went.getClass().getName());
				break;
			case PLAYER:
				EntityPlayer player = (EntityPlayer)obj.getEntity();
				log.warn("On player packet [{}]({}, {}, {}). Entity: {}", dim, player.posX, player.posY, player.posZ, player);
				break;
			case TILEE_ENTITY:
				TileEntity te = obj.getTileEntity();
				log.warn("On TileEntity update [{}]({}, {}, {}). Class: {}", dim, te.xCoord, te.yCoord, te.zCoord, te.getClass().getName());
				break;
			case WEATHER:
				log.warn("On weather action at world [{}]", dim);
				break;
			case UNKNOWN:
				log.warn("On unknown action at world [{}]", dim);
				break;
			}
		}
		else
		{
			log.warn("On unknown action");
		}

		log.warn("Invoked here", new Throwable("stacktrace"));
		log.warn("Original stacktrace", t);
	}
}
