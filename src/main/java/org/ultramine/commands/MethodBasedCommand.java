package org.ultramine.commands;

import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.commands.completion.CommandCompletionHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MethodBasedCommand implements IExtendedCommand
{
	private static Logger logger = LogManager.getLogger();

	private String name;
	private String usage;
	private List<String> aliases;
	private Method method;
	private CommandCompletionHandler completionHandler;

	@Override
	public String getCommandName()
	{
		return name;
	}

	@Override
	public String getCommandUsage(ICommandSender var1)
	{
		return usage;
	}

	@Override
	public List getCommandAliases()
	{
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2)
	{
		try
		{
			method.invoke(null, new CommandContext(var1, var2));
		}
		catch (IllegalAccessException e)
		{
			logger.error("Error while invoking method for command " + name, e);
			throw new CommandNotFoundException();
		}
		catch (InvocationTargetException e)
		{
			logger.error("Error while invoking method for command " + name, e);
			throw new CommandNotFoundException();
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender var1)
	{
		return false;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender var1, String[] var2)
	{
		return completionHandler.getCompletionOptions(var2);
	}

	@Override
	public boolean isUsernameIndex(String[] var1, int var2)
	{
		return completionHandler.isUsernameIndex(var2);
	}

	@Override
	public int compareTo(Object o)
	{
		return getCommandName().compareTo(((ICommand)o).getCommandName());
	}
}
