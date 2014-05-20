package org.ultramine.commands.completion;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class DefaultCompleters
{
	@ArgumentCompleter(value = "player", isUsername = true)
	public static List<String> player(String val, String[] args)
	{
		return getListOfStringsMatchingWord(val, MinecraftServer.getServer().getAllUsernames());
	}

	@ArgumentCompleter("item")
	public static List<String> item(String val, String[] args)
	{
		return getListOfStringsFromIterableMatchingWord(val, Item.itemRegistry.getKeys());
	}

	@ArgumentCompleter("block")
	public static List<String> block(String val, String[] args)
	{
		return getListOfStringsFromIterableMatchingWord(val, Block.blockRegistry.getKeys());
	}

	@ArgumentCompleter("entity")
	public static List<String> entity(String val, String[] args)
	{
		return getListOfStringsFromIterableMatchingWord(val, EntityList.func_151515_b());
	}

	@ArgumentCompleter("list")
	public static List<String> list(String val, String[] args)
	{
		return getListOfStringsMatchingWord(val, args);
	}

	public static List<String> getListOfStringsMatchingWord(String filter, String[] strings)
	{
		List<String> result = new ArrayList<String>();

		for (String str : strings)
		{
			if (CommandBase.doesStringStartWith(filter, str))
				result.add(str);
		}

		return result;
	}

	public static List<String> getListOfStringsFromIterableMatchingWord(String filter, Iterable<String> iterable)
	{
		List<String> result = new ArrayList<String>();

		for (String str : iterable)
		{
			if (CommandBase.doesStringStartWith(filter, str))
				result.add(str);
		}

		return result;
	}
}
