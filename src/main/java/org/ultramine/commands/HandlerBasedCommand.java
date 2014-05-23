package org.ultramine.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.commands.completion.CommandCompletionHandler;
import org.ultramine.server.PermissionHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerBasedCommand implements IExtendedCommand
{
	private static final Logger logger = LogManager.getLogger();

	private String name;
	private String usage;
	private String group;
	private String description;

	private ICommandHandler handler;
	private List<CommandCompletionHandler> completionHandlers;
	private Map<String, ICommandHandler> actionHandlers;

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
		this.completionHandlers = new ArrayList<CommandCompletionHandler>();
		this.actionHandlers = new HashMap<String, ICommandHandler>();
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
		CommandContext.Builder builder = new CommandContext.Builder(this, var1, var2);

		if (completionHandlers.size() > 0)
		{
			CommandCompletionHandler completionHandler = findCompletionHandler(var2);
			if (completionHandler == null)
				throw new WrongUsageException(usage);

			builder.setArgumentsNames(completionHandler.getNames());
			if (!builder.getActionName().isEmpty())
				builder.setActionHandler(actionHandlers.get(builder.getActionName()));
		}

		handler.processCommand(builder.build());
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender var1)
	{
		return (isUsableFromServer && var1.getCommandSenderName().equals("Server")) || PermissionHandler.getInstance().hasAny(var1, permissions);
	}

	@Override
	public List addTabCompletionOptions(ICommandSender var1, String[] var2)
	{
		if (completionHandlers.size() == 0)
			return null;

		List<String> result = null;
		String[] tail = ArrayUtils.remove(var2, var2.length - 1);

		for (CommandCompletionHandler completionHandler : completionHandlers)
		{
			if (completionHandler.match(false, tail))
				if (result == null)
					result = completionHandler.getCompletionOptions(var2);
				else
					result.addAll(completionHandler.getCompletionOptions(var2));
		}
		return result;
	}

	@Override
	public boolean isUsernameIndex(String[] var1, int var2)
	{
		CommandCompletionHandler completionHandler = findCompletionHandler(var1);
		return completionHandler != null && completionHandler.isUsernameIndex(var2);
	}

	private CommandCompletionHandler findCompletionHandler(String[] args)
	{
		if (completionHandlers.size() == 0)
			return null;

		for (CommandCompletionHandler completionHandler : completionHandlers)
		{
			if (completionHandler.match(true, args))
				return completionHandler;
		}
		return null;
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

	public static class Builder
	{
		private HandlerBasedCommand command;

		public Builder(String name, String group, ICommandHandler handler)
		{
			command = new HandlerBasedCommand(name, group, handler);
		}

		public String getName()
		{
			return command.name;
		}

		public Builder setAliases(String... aliases)
		{
			command.aliases = Arrays.asList(aliases);
			return this;
		}

		public Builder addCompletionHandlers(CommandCompletionHandler completionHandler)
		{
			command.completionHandlers.add(completionHandler);
			return this;
		}

		public Builder setPermissions(String... permissions)
		{
			command.permissions = permissions;
			return this;
		}

		public Builder setUsableFromServer(boolean isUsableFromServer)
		{
			command.isUsableFromServer = isUsableFromServer;
			return this;
		}

		public Builder addAction(String name, ICommandHandler action)
		{
			command.actionHandlers.put(name, action);
			return this;
		}

		public HandlerBasedCommand build()
		{
			return command;
		}
	}
}
