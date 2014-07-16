package org.ultramine.server.util;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.common.base.Function;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class TwoStepsExecutor
{
	private final Executor exec;
	private final Queue<CallbackDataStruct<?>> queue = Queues.newConcurrentLinkedQueue();
	
	public TwoStepsExecutor(Executor exec)
	{
		this.exec = exec;
	}
	
	public TwoStepsExecutor(String threadNameFormat)
	{
		this(Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(threadNameFormat).setDaemon(true).build()));
	}
	
	public void register()
	{
		FMLCommonHandler.instance().bus().register(this);
	}
	
	public <P, R> void execute(final Function<P, R> async, final Function<R, Void> sync)
	{
		execute(null, async, sync);
	}
	
	public <P, R> void execute(final P param, final Function<P, R> async, final Function<R, Void> sync)
	{
		exec.execute(new Runnable()
		{
			@Override
			public void run()
			{
				R ret = async.apply(param);
				queue.add(new CallbackDataStruct<R>(sync, ret));
			}
		});
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			for(CallbackDataStruct<?> toCall; (toCall = queue.poll()) != null;)
				toCall.call();
		}
	}
	
	protected void finalize()
	{
		try
		{
			FMLCommonHandler.instance().bus().unregister(this);
		} catch(Throwable ignored){}
	}
	
	private static class CallbackDataStruct<T>
	{
		private Function<T, Void> callback;
		private T param;
		
		public CallbackDataStruct(Function<T, Void> callback, T param)
		{
			this.callback = callback;
			this.param = param;
		}
		
		public void call()
		{
			callback.apply(param);
		}
	}
}
