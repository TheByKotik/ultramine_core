package org.ultramine.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.commands.completion.CommandCompletionHandler;
import org.ultramine.server.PermissionHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodBasedCommand implements IExtendedCommand
{
	private static final Logger logger = LogManager.getLogger();

	private String name;
	private String usage;
	private String group;
	private String description;

	private Method method;
	private CommandCompletionHandler completionHandler;

	private List<String> aliases = new ArrayList<String>();
	private String[] permissions;
	private boolean isUsableFromServer = true;

	public MethodBasedCommand(String name, String group, Method method)
	{
		this.name = name;
		this.group = group;
		this.method = method;
		this.usage = "command." + name + ".usage";
		this.description = "command." + name + ".description";
	}

	public MethodBasedCommand withAliases(String... aliases)
	{
		this.aliases = Arrays.asList(aliases);
		return this;
	}

	public MethodBasedCommand withCompletionHandler(CommandCompletionHandler completionHandler)
	{
		this.completionHandler = completionHandler;
		return this;
	}

	public MethodBasedCommand withPermissions(String[] permissions)
	{
		this.permissions = permissions;
		return this;
	}

	public MethodBasedCommand withUsableFromServer(boolean isUsableFromServer)
	{
		this.isUsableFromServer = isUsableFromServer;
		return this;
	}

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
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getGroup()
	{
		return group;
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
			if (e.getCause() == null)
			{
				logger.error("Error while invoking method for command " + name, e);
				throw new CommandNotFoundException();
			}
			else if (e.getCause() instanceof CommandException)
				throw (CommandException) e.getCause();
			else
				throw new RuntimeException("Error while invoking method for command " + name, e.getCause());
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender var1)
	{
		return (isUsableFromServer && var1.getCommandSenderName().equals("Server")) || PermissionHandler.getInstance().hasAny(var1, permissions);
	}

	@Override
	public List addTabCompletionOptions(ICommandSender var1, String[] var2)
	{
		if (completionHandler == null)
			return null;

		return completionHandler.getCompletionOptions(var2);
	}

	@Override
	public boolean isUsernameIndex(String[] var1, int var2)
	{
		return completionHandler != null && completionHandler.isUsernameIndex(var2);
	}

	@Override
	public int compareTo(Object o)
	{
		return getCommandName().compareTo(((ICommand)o).getCommandName());
	}
}
