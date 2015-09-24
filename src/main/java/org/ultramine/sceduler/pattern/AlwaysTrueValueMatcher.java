package org.ultramine.sceduler.pattern;

class AlwaysTrueValueMatcher implements IValueMatcher
{
	@Override
	public boolean match(int value)
	{
		return true;
	}
}
