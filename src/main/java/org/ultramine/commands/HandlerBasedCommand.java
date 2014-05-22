package org.ultramine.commands;

import net.minecraft.command.ICommandSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.commands.completion.CommandCompletionHandler;
import org.ultramine.server.PermissionHandler;

import java.util.Arrays;
import java.util.List;

public class HandlerBasedCommand implements IExtendedCommand
{
	private static final Logger logger = LogManager.getLogger();

	private String name;
	private String usage;
	private String group;
	private String description;

	private ICommandHandler handler;
	private CommandCompletionHandler completionHandler;

	private List<String> aliases;
	private String[] permissions;
	private boolean isUsableFromServer = true;

	public HandlerBasedCommand(String name, String group, ICommandHandler handler)
	{
		this.name = name;
		this.group = group;
		this.handler = handler;
		this.usage = "command." + name + ".usage";
		this.description = "command." + name + ".description";
	}

	public HandlerBasedCommand withAliases(String... aliases)
	{
		this.aliases = Arrays.asList(aliases);
		return this;
	}

	public HandlerBasedCommand withCompletionHandler(CommandCompletionHandler completionHandler)
	{
		this.completionHandler = completionHandler;
		return this;
	}

	public HandlerBasedCommand withPermissions(String[] permissions)
	{
		this.permissions = permissions;
		return this;
	}

	public HandlerBasedCommand withUsableFromServer(boolean isUsableFromServer)
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
		handler.processCommand(var1, var2);
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
		if (o instanceof IExtendedCommand)
		{
			int result = getGroup().compareTo(((IExtendedCommand) o).getGroup());
			if (result == 0)
				result = getCommandName().compareTo(((IExtendedCommand) o).getCommandName());

			return result;
		}
		return -1;
	}
}
