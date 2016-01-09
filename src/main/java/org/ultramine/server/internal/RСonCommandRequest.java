package org.ultramine.server.internal;

import net.minecraft.server.MinecraftServer;

import java.util.function.Supplier;

public class RСonCommandRequest implements Supplier<String>
{
	private final String command;

	public RСonCommandRequest(String command)
	{
		this.command = command;
	}

	@Override
	public String get()
	{
		return MinecraftServer.getServer().handleRConCommand(command);
	}
}
