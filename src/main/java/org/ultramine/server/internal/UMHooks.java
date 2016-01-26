package org.ultramine.server.internal;

import java.util.UUID;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.event.WorldEventProxy;
import org.ultramine.server.event.WorldUpdateObject;

import com.mojang.authlib.GameProfile;

public class UMHooks
{
	private static final Logger log = LogManager.getLogger();
	private static final boolean IS_CLIENT = FMLCommonHandler.instance().getSide().isClient();
	
	public static void printStackTrace(Throwable t)
	{
		log.error("Direct Throwable.printStackTrace() call");
		if(Thread.currentThread().getName().equals("Server thread"))
		{
			WorldEventProxy wep = WorldEventProxy.getCurrent();
			if(wep != null)
			{
				int dim = wep.getWorld().provider.dimensionId;
				WorldUpdateObject obj = wep.getUpdateObject();
				switch(obj.getType())
				{
				case BLOCK_EVENT:
					log.error("On block event update [{}]({}, {}, {})", dim, obj.getX(), obj.getY(), obj.getZ());
					break;
				case BLOCK_PENDING:
					log.error("On block pending update [{}]({}, {}, {})", dim, obj.getX(), obj.getY(), obj.getZ());
					break;
				case BLOCK_RANDOM:
					log.error("On block random update [{}]({}, {}, {})", dim, obj.getX(), obj.getY(), obj.getZ());
					break;
				case ENTITY:
					Entity ent = obj.getEntity();
					log.error("On entity update [{}]({}, {}, {}). Entity: {}, Class: {}", dim, ent.posX, ent.posY, ent.posZ, ent, ent.getClass().getName());
					break;
				case ENTITY_WEATHER:
					Entity went = obj.getEntity();
					log.error("On weather entity update [{}]({}, {}, {}). Entity: {}, Class: {}", dim, went.posX, went.posY, went.posZ, went, went.getClass().getName());
					break;
				case PLAYER:
					EntityPlayer player = (EntityPlayer)obj.getEntity();
					log.error("On player packet [{}]({}, {}, {}). Entity: {}", dim, player.posX, player.posY, player.posZ, player);
					break;
				case TILEE_ENTITY:
					TileEntity te = obj.getTileEntity();
					log.error("On TileEntity update [{}]({}, {}, {}). Class: {}", dim, te.xCoord, te.yCoord, te.zCoord, te.getClass().getName());
					break;
				case WEATHER:
					log.error("On weather action at world [{}]", dim);
					break;
				case UNKNOWN:
					log.error("On unknown action at world [{}]", dim);
					break;
				}
			}
			else
			{
				log.error("On unknown action");
			}
		}
		else
		{
			log.error("On unknown action in thread " + Thread.currentThread().getName());
		}

		log.error("Invoked here", new Throwable("stacktrace"));
		log.error("Original stacktrace", t);
	}
	
	public static GameProfile readObjectOwner(NBTTagCompound nbt)
	{
		if(IS_CLIENT)
			return null;
		UUID id = nbt.hasKey("$") ? new UUID(nbt.getLong("$"), nbt.getLong("%")) : null;
		String username = nbt.hasKey("#") ? nbt.getString("#") : null;
		if(id != null || username != null && !username.isEmpty())
			return MinecraftServer.getServer().getConfigurationManager().getDataLoader().internGameProfile(id, username);
		return null;
	}
	
	public static void writeObjectOwner(NBTTagCompound nbt, GameProfile owner)
	{
		if(IS_CLIENT)
			return;
		UUID id = owner.getId();
		String username = owner.getName();
		if(id != null)
		{
			nbt.setLong("$", id.getMostSignificantBits());
			nbt.setLong("%", id.getLeastSignificantBits());
		}
		if(username != null)
			nbt.setString("#", username);
	}

	public static IChatComponent onChatSend(EntityPlayerMP player, IChatComponent msg)
	{
		if(IS_CLIENT)
			return msg;
		if(msg instanceof ChatComponentTranslation)
			return onChatSend(player, (ChatComponentTranslation) msg);
		return msg;
	}

	public static IChatComponent onChatSend(EntityPlayerMP player, ChatComponentTranslation msg)
	{
		String key = msg.getKey();
		Object[] oldArgs = msg.getFormatArgs();
		Object[] newArgs = new Object[oldArgs.length];
		boolean argsChanged = false;
		for(int i = 0; i < oldArgs.length; i++)
		{
			Object o = oldArgs[i];
			Object o1 = o;
			if(o instanceof ChatComponentTranslation)
				o1 = onChatSend(player, (ChatComponentTranslation)o);
			newArgs[i] = o1;
			if(o != o1)
				argsChanged = true;
		}
		if(!argsChanged && !key.startsWith("ultramine.") && !key.startsWith("command.")) //TODO add api for all
			return msg;
		String translated = LanguageRegistry.instance().getStringLocalization(key, player.getTranslator());
		if(translated.isEmpty())
			translated = LanguageRegistry.instance().getStringLocalization(key, "en_US");
		ChatComponentTranslation text = new ChatComponentTranslation(translated.isEmpty() ? key : translated, newArgs);
		text.setChatStyle(msg.getChatStyle());
		return text;
	}
}