package org.ultramine.commands.completion;

import java.util.ArrayList;
import java.util.List;

public class CommandCompletionHandler
{
	private List<ArgumentCompleter> completers = new ArrayList<ArgumentCompleter>();
	private int usernameArgIndex = -1;
	private boolean isInfinite = false;

	public List<String> getCompletionOptions(String[] args)
	{
		if (completers.size() == 0)
			return null;

		ArgumentCompleter completer;
		if (completers.size() < args.length)
			completer = isInfinite ? completers.get(completers.size() - 1) : IGNORED;
		else
			completer = completers.get(args.length - 1);

		if (completer != IGNORED)
			return completer.getCompletionOptions(args);
		else
			return null;
	}

	public boolean isUsernameIndex(int checkArgNum)
	{
		return checkArgNum == usernameArgIndex;
	}

	public void addNextArgument(IArgumentCompletionHandler handler, String[] params)
	{
		if (usernameArgIndex == -1 && handler.isUsername())
			usernameArgIndex = completers.size();

		completers.add(new ArgumentCompleter(handler, params));
	}

	public void ignoreNextArgument()
	{
		completers.add(IGNORED);
	}

	public void makeInfinite()
	{
		isInfinite = true;
	}

	private static ArgumentCompleter IGNORED = new ArgumentCompleter(null, null);
	private static class ArgumentCompleter
	{
		private IArgumentCompletionHandler handler;
		private String[] params;

		private ArgumentCompleter(IArgumentCompletionHandler handler, String[] params)
		{
			this.handler = handler;
			this.params = params;
		}

		List<String> getCompletionOptions(String[] args)
		{
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
	}
}
