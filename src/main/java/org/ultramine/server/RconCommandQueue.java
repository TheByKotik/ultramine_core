package org.ultramine.server;

import java.util.Queue;

import com.google.common.collect.Queues;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.server.MinecraftServer;

@SideOnly(Side.SERVER)
public class RconCommandQueue
{
	private static final RconCommandQueue INSTANCE = new RconCommandQueue();
	public static RconCommandQueue instance()
	{
		return INSTANCE;
	}
	
	private final Queue<CommandRequest> queue = Queues.newConcurrentLinkedQueue();
	private final MinecraftServer server = MinecraftServer.getServer();
	
	private RconCommandQueue()
	{
		FMLCommonHandler.instance().bus().register(this);
	}
	
	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.END)
		{
			for(CommandRequest command; (command = queue.poll()) != null;)
			{
				command.result = server.handleRConCommand(command.cmd);
				synchronized(command)
				{
					command.notifyAll();
				}
			}
		}
	}
	
	public CommandRequest request(String cmd)
	{
		CommandRequest command = new CommandRequest(cmd);
		queue.add(command);
		return command;
	}
	
	@SideOnly(Side.SERVER)
	public static class CommandRequest
	{
		private final String cmd;
		private volatile String result = null;
		
		private CommandRequest(String cmd)
		{
			this.cmd = cmd;
		}
		
		public String await()
		{
			while(result == null)
				synchronized(this){try{this.wait();}catch(InterruptedException e){}}
			return result;
		}
	}
}
