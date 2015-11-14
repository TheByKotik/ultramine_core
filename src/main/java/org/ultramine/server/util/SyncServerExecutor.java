package org.ultramine.server.util;

import java.util.Queue;
import java.util.concurrent.Executor;

import com.google.common.collect.Queues;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class SyncServerExecutor implements Executor
{
	private final Queue<Runnable> queue = Queues.newConcurrentLinkedQueue();
	
	public void register()
	{
		FMLCommonHandler.instance().bus().register(this);
	}
	
	public void unregister()
	{
		FMLCommonHandler.instance().bus().unregister(this);
	}

	@Override
	public void execute(Runnable toRun)
	{
		queue.add(toRun);
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.END)
		{
			for(Runnable toRun; (toRun = queue.poll()) != null;)
				toRun.run();
		}
	}
}
