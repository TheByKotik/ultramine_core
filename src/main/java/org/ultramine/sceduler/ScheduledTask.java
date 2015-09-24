package org.ultramine.sceduler;

import org.ultramine.sceduler.pattern.SchedulingPattern;

public abstract class ScheduledTask
{
	protected final Scheduler sceduler;
	protected final SchedulingPattern pattern;
	protected final Runnable task;
	
	protected ScheduledTask(Scheduler sceduler, SchedulingPattern pattern, Runnable task)
	{
		this.sceduler = sceduler;
		this.pattern = pattern;
		this.task = task;
	}
	
	public boolean cancel()
	{
		return sceduler.cancelTask(this);
	}
	
	public boolean canRun(long millis)
	{
		return pattern.match(millis);
	}
	
	abstract void launch();
}
