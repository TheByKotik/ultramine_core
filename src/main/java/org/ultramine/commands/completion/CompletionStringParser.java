package org.ultramine.commands.completion;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompletionStringParser
{
	private static final Pattern argumentPattern = Pattern.compile("<\\s*([^<>\\s]*)\\s*([^<>]*)>");
	private Map<String, IArgumentCompletionHandler> handlers = new HashMap<String, IArgumentCompletionHandler>();

	public CommandCompletionHandler parse(String completionString)
	{
		CommandCompletionHandler result = new CommandCompletionHandler();
		Matcher matcher = argumentPattern.matcher(completionString);

		while (matcher.find())
		{
			String handlerName = matcher.group(1);

			if (handlerName.isEmpty() || !handlers.containsKey(handlerName))
			{
				result.ignoreNextArgument();
			}
			else
			{
				String[] params = StringUtils.split(matcher.group(2));
				result.addNextArgument(handlers.get(handlerName), params);
			}
		}

		if (completionString.endsWith("..."))
			result.makeInfinite();

		return result;
	}

	public void registerHandler(String name, IArgumentCompletionHandler handler)
	{
		handlers.put(name, handler);
	}

	public void registerHandlers(Class<?> cls)
	{
		for (Method handler : cls.getMethods())
		{
			if (handler.isAnnotationPresent(ArgumentCompleter.class) && Modifier.isStatic(handler.getModifiers()))
			{
				ArgumentCompleter data = handler.getAnnotation(ArgumentCompleter.class);
				registerHandler(data.value(), new WrappedHandler(handler, data.isUsername()));
			}
		}
	}

	private static class WrappedHandler implements IArgumentCompletionHandler
	{
		private Method method;
		private boolean isUsername;

		private WrappedHandler(Method method, boolean isUsername)
		{
			this.method = method;
			this.isUsername = isUsername;
		}

		@Override
		public List<String> handleCompletion(String val, String[] args)
		{
			try
			{
				return (List<String>) method.invoke(null, val, args);
			}
			catch (IllegalAccessException ignored)
			{
			}
			catch (InvocationTargetException ignored)
			{
			}

			return null;
		}

		@Override
		public boolean isUsername()
		{
			return isUsername;
		}
	}
}
