package org.ultramine.server;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class WatchdogThread extends Thread
{
	Logger log = LogManager.getLogger();

	private static WatchdogThread instance;
	private final long timeoutTime;
	private final boolean restart;
	private volatile long lastTick;
	private volatile boolean stopping;

	private WatchdogThread(long timeoutTime, boolean restart)
	{
		super("Spigot Watchdog Thread");
		this.timeoutTime = timeoutTime;
		this.restart = restart;
	}

	public static void doStart(int timeoutTime, boolean restart)
	{
		if(instance == null)
		{
			instance = new WatchdogThread(timeoutTime * 1000L, restart);
			instance.start();
		}
	}
	
	public static void doStart()
	{
		doStart(ConfigurationHandler.getServerConfig().watchdogThread.timeout, ConfigurationHandler.getServerConfig().watchdogThread.restart);
	}

	public static void tick()
	{
		instance.lastTick = System.currentTimeMillis();
	}

	public static void doStop()
	{
		if(instance != null)
		{
			instance.stopping = true;
		}
	}

	@Override
	public void run()
	{
		while(!stopping)
		{
			//
			if(lastTick != 0 && System.currentTimeMillis() > lastTick + timeoutTime)
			{
				log.log(Level.FATAL, "The server has stopped responding!");

				log.log(Level.FATAL, "Current Thread State:");
				ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);

				for(ThreadInfo thread : threads)
				{
					if(thread.getThreadState() != State.WAITING)
					{
						log.log(Level.FATAL, "------------------------------");
						//
						log.log(Level.FATAL, "Current Thread: " + thread.getThreadName());
						log.log(Level.FATAL, "\tPID: " + thread.getThreadId() + " | Suspended: " + thread.isSuspended() + " | Native: " + thread.isInNative() + " | State: "
								+ thread.getThreadState());

						if(thread.getLockedMonitors().length != 0)
						{
							log.log(Level.FATAL, "\tThread is waiting on monitor(s):");

							for(MonitorInfo monitor : thread.getLockedMonitors())
							{
								log.log(Level.FATAL, "\t\tLocked on:" + monitor.getLockedStackFrame());
							}
						}

						log.log(Level.FATAL, "\tStack:");
						//
						StackTraceElement[] stack = thread.getStackTrace();

						for(int line = 0; line < stack.length; line++)
						{
							log.log(Level.FATAL, "\t\t" + stack[line].toString());
						}
					}
				}

				log.log(Level.FATAL, "------------------------------");

				if(restart)
				{
					System.exit(0);
				}

				break;
			}

			try
			{
				sleep(10000);
			} catch (InterruptedException ex)
			{
				// interrupt();
			}
		}
	}
}
