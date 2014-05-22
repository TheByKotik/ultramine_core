package org.ultramine.commands;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.IChatComponent;
import org.ultramine.server.PermissionHandler;

import java.util.Arrays;

public class CommandContext
{
	private ICommandSender sender;
	private String[] args;
	private IExtendedCommand command;

	public CommandContext(IExtendedCommand command, ICommandSender sender, String[] args)
	{
		this.sender = sender;
		this.args = args;
		this.command = command;
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
		return CommandBase.parseInt(sender, getString(argNum));
	}

	public int getInt(int argNum, int minBound)
	{
		return CommandBase.parseIntWithMin(sender, getString(argNum), minBound);
	}

	public int getInt(int argNum, int minBound, int maxBound)
	{
		return CommandBase.parseIntBounded(sender, getString(argNum), minBound, maxBound);
	}

	public double getDouble(int argNum)
	{
		return CommandBase.parseDouble(sender, getString(argNum));
	}

	public double getDouble(int argNum, double minBound)
	{
		return CommandBase.parseDoubleWithMin(sender, getString(argNum), minBound);
	}

	public double getDouble(int argNum, double minBound, double maxBound)
	{
		return CommandBase.parseDoubleBounded(sender, getString(argNum), minBound, maxBound);
	}

	public boolean getBoolean(int argNum)
	{
		return CommandBase.parseBoolean(sender, getString(argNum));
	}

	public String getString(int argNum)
	{
		try
		{
			return args[argNum];
		}
		catch (IndexOutOfBoundsException ignored)
		{
			throwBadUsage();
		}
		return null;
	}

	public EntityPlayerMP getSenderAsPlayer()
	{
		return CommandBase.getCommandSenderAsPlayer(sender);
	}

	public EntityPlayerMP getPlayer(int argNum)
	{
		return CommandBase.getPlayer(sender, getString(argNum));
	}

	public IChatComponent getChatComponent(int startArgNum, boolean emphasizePlayers)
	{
		return CommandBase.func_147176_a(sender, args, startArgNum, emphasizePlayers);
	}

	public String getJoined(int startArgNum)
	{
		return CommandBase.func_82360_a(sender, args, startArgNum);
	}

	public String[] getLast(int startArgNum)
	{
		try
		{
			return Arrays.copyOfRange(args, startArgNum, args.length);
		}
		catch (IllegalArgumentException ignored)
		{
			throwBadUsage();
		}
		return new String[0];
	}

	public double getCoordinate(int argNum, double original)
	{
		return CommandBase.func_110666_a(sender, original, getString(argNum));
	}

	public double getCoordinate(int argNum, double original, int minBound, int maxBound)
	{
		return CommandBase.func_110665_a(sender, original, getString(argNum), minBound, maxBound);
	}

	public Item getItem(int argNum)
	{
		return CommandBase.getItemByText(sender, getString(argNum));
	}

	public Block getBlock(int argNum)
	{
		return CommandBase.getBlockByText(sender, getString(argNum));
	}

	public void notifyAdmins(String messageKey, Object... messageArgs)
	{
		CommandBase.notifyAdmins(sender, messageKey, messageArgs);
	}

	public void checkSenderPermission(String permission)
	{
		if (!senderIsServer() && !PermissionHandler.getInstance().has(sender, permission))
			throw new CommandException("commands.generic.permission");
	}

	public void checkSenderPermissionInWorld(String world, String permission)
	{
		if (!senderIsServer() && !PermissionHandler.getInstance().has(world, sender.getCommandSenderName(), permission))
			throw new CommandException("commands.generic.permission");
	}

	public boolean senderIsServer()
	{
		return sender.getCommandSenderName().equals("Server");
	}

	public void throwBadUsage()
	{
		throw new WrongUsageException(command.getCommandUsage(sender));
	}

	public int argsCount()
	{
		return args.length;
	}
}
