package org.ultramine.commands.completion;

import cpw.mods.fml.common.eventhandler.Event;
import org.ultramine.commands.completion.CompletionStringParser;

public class RegisterCompletersEvent extends Event
{
	private final CompletionStringParser completionStringParser;

	public RegisterCompletersEvent(CompletionStringParser completionStringParser)
	{
		this.completionStringParser = completionStringParser;
	}

	public CompletionStringParser getCompletionStringParser()
	{
		return completionStringParser;
	}
}
