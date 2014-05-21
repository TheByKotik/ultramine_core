package org.ultramine.commands;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.IChatComponent;

public class CommandContext
{
	private ICommandSender sender;
	private String[] args;

	public CommandContext(ICommandSender sender, String[] args)
	{
		this.sender = sender;
		this.args = args;
	}

	public ICommandSender getSender()
	{
		return sender;
	}

	public String[] getArgs()
	{
		return args;
	}

	public int getInt(int argNum)
	{
		return CommandBase.parseInt(sender, args[argNum]);
	}

	public int getInt(int argNum, int minBound)
	{
		return CommandBase.parseIntWithMin(sender, args[argNum], minBound);
	}

	public int getInt(int argNum, int minBound, int maxBound)
	{
		return CommandBase.parseIntBounded(sender, args[argNum], minBound, maxBound);
	}

	public double getDouble(int argNum)
	{
		return CommandBase.parseDouble(sender, args[argNum]);
	}

	public double getDouble(int argNum, double minBound)
	{
		return CommandBase.parseDoubleWithMin(sender, args[argNum], minBound);
	}

	public double getDouble(int argNum, double minBound, double maxBound)
	{
		return CommandBase.parseDoubleBounded(sender, args[argNum], minBound, maxBound);
	}

	public boolean getBoolean(int argNum)
	{
		return CommandBase.parseBoolean(sender, args[argNum]);
	}

	public String getString(int argNum)
	{
		return args[argNum];
	}

	public EntityPlayerMP getSenderAsPlayer()
	{
		return CommandBase.getCommandSenderAsPlayer(sender);
	}

	public EntityPlayerMP getPlayer(int argNum)
	{
		return CommandBase.getPlayer(sender, args[argNum]);
	}

	public IChatComponent getChatComponent(int startArgNum, boolean emphasizePlayers)
	{
		return CommandBase.func_147176_a(sender, args, startArgNum, emphasizePlayers);
	}

	public String getJoined(int startArgNum)
	{
		return CommandBase.func_82360_a(sender, args, startArgNum);
	}

	public double getCoordinate(int argNum, double original)
	{
		return CommandBase.func_110666_a(sender, original, args[argNum]);
	}

	public double getCoordinate(int argNum, double original, int minBound, int maxBound)
	{
		return CommandBase.func_110665_a(sender, original, args[argNum], minBound, maxBound);
	}

	public Item getItem(int argNum)
	{
		return CommandBase.getItemByText(sender, args[argNum]);
	}

	public Block getBlock(int argNum)
	{
		return CommandBase.getBlockByText(sender, args[argNum]);
	}

	public void notifyAdmins(String messageKey, Object... messageArgs)
	{
		CommandBase.notifyAdmins(sender, messageKey, messageArgs);
	}
}
