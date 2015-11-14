package org.ultramine.sceduler;

import org.ultramine.sceduler.pattern.SchedulingPattern;
import org.ultramine.server.util.GlobalExecutors;

public class ScheduledAsyncTask extends ScheduledTask
{
	ScheduledAsyncTask(Scheduler sceduler, SchedulingPattern pattern, Runnable task)
	{
		super(sceduler, pattern, task);
	}

	@Override
	void launch()
	{
		GlobalExecutors.cachedIO().execute(task);
	}
}
