package org.ultramine.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

import java.util.List;

public class VanillaCommandWrapper implements IExtendedCommand
{
	private ICommand wrappedCommand;

	public VanillaCommandWrapper(ICommand wrappedCommand)
	{
		this.wrappedCommand = wrappedCommand;
	}

	@Override
	public String getCommandName()
	{
		return wrappedCommand.getCommandName();
	}

	@Override
	public String getCommandUsage(ICommandSender var1)
	{
		return wrappedCommand.getCommandUsage(var1);
	}

	@Override
	public List getCommandAliases()
	{
		return wrappedCommand.getCommandAliases();
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2)
	{
		wrappedCommand.processCommand(var1, var2);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender var1)
	{
		return wrappedCommand.canCommandSenderUseCommand(var1);
	}

	@Override
	public List addTabCompletionOptions(ICommandSender var1, String[] var2)
	{
		return wrappedCommand.addTabCompletionOptions(var1, var2);
	}

	@Override
	public boolean isUsernameIndex(String[] var1, int var2)
	{
		return wrappedCommand.isUsernameIndex(var1, var2);
	}

	@Override
	public int compareTo(Object o)
	{
		return wrappedCommand.compareTo(o);
	}
}
