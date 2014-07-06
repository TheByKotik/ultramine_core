package org.ultramine.commands;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;

import org.ultramine.server.PermissionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandContext
{
	private ICommandSender sender;
	private String[] args;
	private IExtendedCommand command;
	private Map<String, Argument> argumentMap;
	private int lastArgumentNum;
	private String actionName;
	private ICommandHandler actionHandler;

	private CommandContext(IExtendedCommand command, ICommandSender sender, String[] args)
	{
		this.sender = sender;
		this.args = args;
		this.command = command;
		this.argumentMap = new HashMap<String, Argument>(args.length);
		this.actionName = "";
		this.lastArgumentNum = args.length - 1;
	}

	public Argument get(String key)
	{
		if (!argumentMap.containsKey(key))
			throwBadUsage();

		return argumentMap.get(key);
	}

	public Argument get(int num)
	{
		if (num < 0 || num >= args.length)
			throwBadUsage();

		return new Argument(num);
	}

	public boolean contains(String key)
	{
		return argumentMap.containsKey(key);
	}

	public Argument set(String key, String value)
	{
		Argument arg = new Argument(value);
		argumentMap.put(key, arg);
		return arg;
	}

	public String getAction()
	{
		return actionName;
	}

	public boolean actionIs(String action)
	{
		return actionName.equalsIgnoreCase(action);
	}

	public void doAction()
	{
		if (actionHandler != null)
			actionHandler.processCommand(this);
	}

	public ICommandSender getSender()
	{
		return sender;
	}

	public boolean senderIsServer()
	{
		return !(sender instanceof EntityPlayer);
	}

	public EntityPlayerMP getSenderAsPlayer()
	{
		return CommandBase.getCommandSenderAsPlayer(sender);
	}

	public void notifyAdmins(String messageKey, Object... messageArgs)
	{
		CommandBase.func_152373_a(sender, null, messageKey, messageArgs);
	}

	public void checkSenderPermission(String permission)
	{
		checkSenderPermission(permission, "commands.generic.permission");
	}
	
	public void checkSenderPermission(String permission, String msg)
	{
		if (!senderIsServer() && !PermissionHandler.getInstance().has(sender, permission))
			throw new CommandException(msg);
	}

	public void checkSenderPermissionInWorld(String world, String permission)
	{
		if (!senderIsServer() && !PermissionHandler.getInstance().has(world, sender.getCommandSenderName(), permission))
			throw new CommandException("commands.generic.permission");
	}
	
	public void sendMessage(EnumChatFormatting tplColor, EnumChatFormatting argsColor, String msg, Object... args)
	{
		for(int i = 0; i < args.length; i++)
		{
			Object o = args[i];
			if(!(o instanceof IChatComponent))
				args[i] = new ChatComponentText(o.toString()).setChatStyle(new ChatStyle().setColor(argsColor));
		}
		
		ChatComponentTranslation comp = new ChatComponentTranslation(msg, args);
		comp.getChatStyle().setColor(tplColor);
		sender.addChatMessage(comp);
	}
	
	public void sendMessage(EnumChatFormatting argsColor, String msg, Object... args)
	{
		sendMessage(EnumChatFormatting.GOLD, argsColor, msg, args);
	}
	
	public void sendMessage(String msg, Object... args)
	{
		sendMessage(EnumChatFormatting.GOLD, EnumChatFormatting.YELLOW, msg, args);
	}

	public void throwBadUsage()
	{
		throw new WrongUsageException(command.getCommandUsage(sender));
	}
	
	public void failure(String msg)
	{
		throw new CommandException(msg);
	}
	
	public void check(boolean flag, String msg)
	{
		if(!flag)
			throw new CommandException(msg);
	}

	public String[] getArgs()
	{
		return args;
	}

	public IExtendedCommand getCommand()
	{
		return command;
	}

	public class Argument
	{
		private int num;
		private boolean last;
		private String value;

		private Argument(int num)
		{
			this.value = args[num];
			this.num = num;
			this.last = num == lastArgumentNum;
		}

		private Argument(int num, boolean last)
		{
			this.value = args[num];
			this.num = num;
			this.last = last;
		}

		private Argument(String value)
		{
			this.value = value;
			this.num = -1;
			this.last = false;
		}

		private String[] args()
		{
			if (num >= 0)
				return args;
			else
				return new String[] {value};
		}

		private int num()
		{
			return Math.max(num, 0);
		}

		public String asString()
		{
			if (last)
				return CommandBase.func_82360_a(sender, args(), num());
			else
				return value;
		}

		public Argument[] asArray()
		{
			if (num < 0)
				return new Argument[] {this};

			Argument[] result = new Argument[args.length - num];
			for (int i = num; i < args.length; i++)
				result[i-num] = new Argument(i, false);
			return result;
		}

		public int asInt()
		{
			return CommandBase.parseInt(sender, value);
		}

		public int asInt(int minBound)
		{
			return CommandBase.parseIntWithMin(sender, value, minBound);
		}

		public int asInt(int minBound, int maxBound)
		{
			return CommandBase.parseIntBounded(sender, value, minBound, maxBound);
		}

		public double asDouble()
		{
			return CommandBase.parseDouble(sender, value);
		}

		public double asDouble(double minBound)
		{
			return CommandBase.parseDoubleWithMin(sender, value, minBound);
		}

		public double asDouble(double minBound, double maxBound)
		{
			return CommandBase.parseDoubleBounded(sender, value, minBound, maxBound);
		}

		public boolean asBoolean()
		{
			return CommandBase.parseBoolean(sender, value);
		}

		public EntityPlayerMP asPlayer()
		{
			return CommandBase.getPlayer(sender, value);
		}
		
		public WorldServer asWorld()
		{
			WorldServer world = MinecraftServer.getServer().getMultiWorld().getWorldByNameOrID(value);
			if(world == null)
				throw new CommandException("commands.generic.world.invalid", value);
			return world;
		}

		public IChatComponent asChatComponent(boolean emphasizePlayers)
		{
			return CommandBase.func_147176_a(sender, args(), num(), emphasizePlayers);
		}

		public double asCoordinate(double original)
		{
			return CommandBase.func_110666_a(sender, original, value);
		}

		public double asCoordinate(double original, int minBound, int maxBound)
		{
			return CommandBase.func_110665_a(sender, original, value, minBound, maxBound);
		}

		public Item asItem()
		{
			return CommandBase.getItemByText(sender, value);
		}

		public Block asBlock()
		{
			return CommandBase.getBlockByText(sender, value);
		}
	}

	public static class Builder
	{
		private CommandContext context;

		public Builder(IExtendedCommand command, ICommandSender sender, String[] args)
		{
			context = new CommandContext(command, sender, args);
		}

		public Builder resolveArguments(List<String> names)
		{
			context.lastArgumentNum = names.size();
			Map<String, Integer> nameCount = new HashMap<String, Integer>();
			for (int i = 0; i < names.size(); i++)
			{
				String name = names.get(i);

				if (name == null || name.isEmpty())
					continue;

				if (context.argumentMap.containsKey(name))
				{
					Integer count = nameCount.containsKey(name) ? nameCount.get(name) + 1 : 2;
					nameCount.put(name, count);
					name = name + count.toString();
				}

				context.argumentMap.put(name, context.new Argument(i));
			}
			return this;
		}

		public Builder setAction(String actionName, ICommandHandler actionHandler)
		{
			context.actionName = actionName;
			context.actionHandler = actionHandler;
			return this;
		}

		public CommandContext build()
		{
			return context;
		}
	}
}
