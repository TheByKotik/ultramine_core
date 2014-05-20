package org.ultramine.commands.completion;

import java.util.List;

public interface IArgumentCompletionHandler
{
	List<String> handleCompletion(String val, String[] args);
	boolean isUsername();
}