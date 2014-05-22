package org.ultramine.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;
import org.ultramine.commands.completion.CompletionStringParser;
import org.ultramine.commands.completion.RegisterCompletersEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class CommandRegistry
{
	private Map<String, IExtendedCommand> commandMap;
	private SortedSet<IExtendedCommand> registeredCommands;
	private CompletionStringParser completionStringParser;

	public CommandRegistry()
	{
		commandMap = new HashMap<String, IExtendedCommand>();
		registeredCommands = new TreeSet<IExtendedCommand>();
		completionStringParser = new CompletionStringParser();

		MinecraftForge.EVENT_BUS.post(new RegisterCompletersEvent(completionStringParser));
	}

	public IExtendedCommand registerCommand(IExtendedCommand command)
	{
		List<String> aliases = command.getCommandAliases();
		commandMap.put(command.getCommandName(), command);
		registeredCommands.add(command);

		if (aliases != null)
		{
			for (String alias : aliases)
			{
				IExtendedCommand cmd = commandMap.get(alias);
				if (cmd == null || !cmd.getCommandName().equals(alias))
					commandMap.put(alias, command);
			}
		}

		return command;
	}

	public IExtendedCommand registerVanillaCommand(ICommand command)
	{
		return registerCommand(new VanillaCommandWrapper(command));
	}

	public void registerCommands(Class<?> cls)
	{
		for (Method method : cls.getMethods())
		{
			if (method.isAnnotationPresent(Command.class) && Modifier.isStatic(method.getModifiers()))
			{
				Command data = method.getAnnotation(Command.class);
				ICommandHandler handler = new MethodBasedCommandHandler(method);

				IExtendedCommand cmd = new HandlerBasedCommand(data.name(), data.group(), handler)
						.withAliases(data.aliases())
						.withPermissions(data.permissions())
						.withUsableFromServer(data.isUsableFromServer())
						.withCompletionHandler(completionStringParser.parse(data.completion()));

				registerCommand(cmd);
			}
		}
	}

	public IExtendedCommand get(String name)
	{
		return commandMap.get(name);
	}

	public Map<String, IExtendedCommand> getCommandMap()
	{
		return commandMap;
	}

	public List<String> filterPossibleCommandsNames(ICommandSender sender, String filter)
	{
		List<String> result = new ArrayList<String>();

		for (Map.Entry<String, IExtendedCommand> entry : commandMap.entrySet())
		{
			if (CommandBase.doesStringStartWith(filter, entry.getKey()) && entry.getValue().canCommandSenderUseCommand(sender))
				result.add(entry.getKey());
		}

		return result;
	}

	public List<IExtendedCommand> getPossibleCommands(ICommandSender sender)
	{
		List<IExtendedCommand> result = new ArrayList<IExtendedCommand>();

		for (IExtendedCommand command : registeredCommands)
		{
			if (command.canCommandSenderUseCommand(sender))
				result.add(command);
		}

		return result;
	}

	public CompletionStringParser getCompletionStringParser()
	{
		return completionStringParser;
	}
}
