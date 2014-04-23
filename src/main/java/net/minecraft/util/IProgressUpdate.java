package net.minecraft.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IProgressUpdate
{
	void displayProgressMessage(String var1);

	@SideOnly(Side.CLIENT)
	void resetProgressAndMessage(String var1);

	void resetProgresAndWorkingMessage(String var1);

	void setLoadingProgress(int var1);

	@SideOnly(Side.CLIENT)
	void func_146586_a();
}