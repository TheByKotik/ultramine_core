package org.ultramine.commands.completion;

import net.minecraft.command.CommandBase;
import java.util.ArrayList;
import java.util.List;

public class CommandCompletionHandler
{
	private static ICompleter IGNORED = new ICompleter.ArgumentCompleter(null, null);

	private List<ICompleter> completers = new ArrayList<ICompleter>();
	private List<String> names = new ArrayList<String>();
	private int usernameArgIndex = -1;
	private boolean isInfinite = false;

	public List<String> getCompletionOptions(String[] args)
	{
		if (completers.size() == 0)
			return null;

		ICompleter completer;
		if (completers.size() < args.length)
			completer = isInfinite ? completers.get(completers.size() - 1) : IGNORED;
		else
			completer = completers.get(args.length - 1);

		return completer.getCompletionOptions(args);
	}

	public boolean isUsernameIndex(int checkArgNum)
	{
		return checkArgNum == usernameArgIndex;
	}

	public boolean match(boolean strict, String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (i == completers.size())
				return isInfinite;

			if (!completers.get(i).match(args[i]))
				return false;
		}
		return !strict || args.length == completers.size();
	}

	public List<String> getNames()
	{
		return names;
	}

	public void addNextArgument(String name, IArgumentCompletionHandler handler, String[] params)
	{
		if (usernameArgIndex == -1 && handler.isUsername())
			usernameArgIndex = completers.size();

		completers.add(new ICompleter.ArgumentCompleter(handler, params));
		names.add(name);
	}

	public void addNextActionArgument(String... actions)
	{
		completers.add(new ICompleter.ActionCompleter(actions));
		names.add("action");
	}

	public void ignoreNextArgument(String name)
	{
		completers.add(IGNORED);
		names.add(name);
	}

	public void makeInfinite()
	{
		isInfinite = true;
	}

	private static interface ICompleter
	{
		List<String> getCompletionOptions(String[] args);
		boolean match(String val);

		static class ArgumentCompleter implements ICompleter
		{
			private IArgumentCompletionHandler handler;
			private String[] params;

			private ArgumentCompleter(IArgumentCompletionHandler handler, String[] params)
			{
				this.handler = handler;
				this.params = params;
			}

			@Override
			public List<String> getCompletionOptions(String[] args)
			{
				if (handler == null || params == null)
					return null;

				String[] params = new String[this.params.length];
				for (int i = 0; i < this.params.length; i++)
				{
					String param = this.params[i];
					if (param.startsWith("&"))
					{
						try
						{
							int argNum = Integer.valueOf(param.substring(1));
							params[i] = args[argNum];
						}
						catch (Exception ignored)
						{
							params[i] = param;
						}
					}
					else
						params[i] = param;
				}

				return handler.handleCompletion(args[args.length - 1], params);
			}

			@Override
			public boolean match(String val)
			{
				return true;
			}
		}

		static class ActionCompleter implements ICompleter
		{
			private String[] actions;

			private ActionCompleter(String[] actions)
			{
				this.actions = actions;
			}

			@Override
			public List<String> getCompletionOptions(String[] args)
			{
				List<String> result = new ArrayList<String>();
				String val = args[args.length - 1];
				for (String action : actions)
				{
					if (CommandBase.doesStringStartWith(val, action))
						result.add(action);
				}
				return result;
			}

			@Override
			public boolean match(String val)
			{
				for (String action : actions)
				{
					if (action.equalsIgnoreCase(val))
						return true;
				}
				return false;
			}
		}
	}
}
