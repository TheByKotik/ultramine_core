package net.minecraft.world.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.util.IProgressUpdate;

public interface ISaveFormat
{
	ISaveHandler getSaveLoader(String var1, boolean var2);

	@SideOnly(Side.CLIENT)
	List getSaveList() throws AnvilConverterException;

	void flushCache();

	@SideOnly(Side.CLIENT)
	WorldInfo getWorldInfo(String var1);

	boolean deleteWorldDirectory(String var1);

	@SideOnly(Side.CLIENT)
	void renameWorld(String var1, String var2);

	boolean isOldMapFormat(String var1);

	boolean convertMapFormat(String var1, IProgressUpdate var2);

	@SideOnly(Side.CLIENT)
	boolean canLoadWorld(String var1);
}