package net.minecraft.network.rcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

@SideOnly(Side.SERVER)
public class RConConsoleSource implements ICommandSender
{
	public static final RConConsoleSource instance = new RConConsoleSource();
	private StringBuffer buffer = new StringBuffer();
	private static final String __OBFID = "CL_00001800";

	public void resetLog()
	{
		this.buffer.setLength(0);
	}

	public String getLogContents()
	{
		return this.buffer.toString();
	}

	public String getCommandSenderName()
	{
		return "Rcon";
	}

	public IChatComponent func_145748_c_()
	{
		return new ChatComponentText(this.getCommandSenderName());
	}

	public void addChatMessage(IChatComponent ichatcomponent)
	{
		this.buffer.append(ichatcomponent.getUnformattedText());
	}

	public boolean canCommandSenderUseCommand(int par1, String par2Str)
	{
		return true;
	}

	public ChunkCoordinates getPlayerCoordinates()
	{
		return new ChunkCoordinates(0, 0, 0);
	}

	public World getEntityWorld()
	{
		return MinecraftServer.getServer().getEntityWorld();
	}
}