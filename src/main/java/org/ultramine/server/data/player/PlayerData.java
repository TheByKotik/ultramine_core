package org.ultramine.server.data.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.authlib.GameProfile;

public class PlayerData
{
	private final GameProfile profile;
	private final Map<Class<? extends PlayerDataExtension>, PlayerDataExtension> data = new HashMap<Class<? extends PlayerDataExtension>, PlayerDataExtension>();
	private final PlayerCoreData coreData;

	public PlayerData(GameProfile profile, List<PlayerDataExtension> list)
	{
		this.profile = profile;
		for(PlayerDataExtension o : list)
			data.put(o.getClass(), o);
		coreData = get(PlayerCoreData.class);
	}
	
	public GameProfile getProfile()
	{
		return profile;
	}

	public <T> T get(Class<T> clazz)
	{
		return clazz.cast(data.get(clazz));
	}

	public PlayerCoreData core()
	{
		return coreData;
	}
}
