package org.ultramine.economy;

import org.ultramine.server.data.player.PlayerData;

import net.minecraft.server.MinecraftServer;

import com.mojang.authlib.GameProfile;

public class Accounts
{
	public static Account getPlayer(GameProfile profile)
	{
		PlayerData data = MinecraftServer.getServer().getConfigurationManager().getDataLoader().getPlayerData(profile);
		return data == null ? null : data.core().getAccount();
	}
	
	public static Account getPlayer(String username)
	{
		PlayerData data = MinecraftServer.getServer().getConfigurationManager().getDataLoader().getPlayerData(username);
		return data == null ? null : data.core().getAccount();
	}
}
