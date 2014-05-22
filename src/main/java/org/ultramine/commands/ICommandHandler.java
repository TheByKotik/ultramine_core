package org.ultramine.commands;

import net.minecraft.command.ICommandSender;

public interface ICommandHandler
{
	public void processCommand(ICommandSender var1, String[] var2);
	public void setCommand(IExtendedCommand command);
}
