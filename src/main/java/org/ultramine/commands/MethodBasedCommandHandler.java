package org.ultramine.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodBasedCommandHandler implements ICommandHandler
{
	private static final Logger logger = LogManager.getLogger();
	private Method method;

	public MethodBasedCommandHandler(Method method)
	{
		this.method = method;
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
			logger.error("Error while executing command from method", e);
			throw new CommandNotFoundException();
		}
		catch (InvocationTargetException e)
		{
			if (e.getCause() == null)
			{
				logger.error("Error while executing command from method", e);
				throw new CommandNotFoundException();
			}
			else if (e.getCause() instanceof CommandException)
				throw (CommandException) e.getCause();
			else
				throw new RuntimeException("Error while executing command from method", e.getCause());
		}
	}
}
