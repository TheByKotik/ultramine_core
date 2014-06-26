package net.minecraft.world.chunk.storage;

import java.io.File;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraft.world.storage.WorldInfo;

public class AnvilSaveHandler extends SaveHandler
{
	private static final String __OBFID = "CL_00000581";

	public AnvilSaveHandler(File par1File, String par2Str, boolean par3)
	{
		super(par1File, par2Str, par3);
	}

	public IChunkLoader getChunkLoader(WorldProvider par1WorldProvider)
	{
		File file1 = this.getWorldDirectory();
		File file2;

		if (!isSingleStorage && par1WorldProvider.getSaveFolder() != null)
		{
			file2 = new File(file1, par1WorldProvider.getSaveFolder());
			file2.mkdirs();
			return new AnvilChunkLoader(file2);
		}
		else
		{
			return new AnvilChunkLoader(file1);
		}
	}

	public void saveWorldInfoWithPlayer(WorldInfo par1WorldInfo, NBTTagCompound par2NBTTagCompound)
	{
		par1WorldInfo.setSaveVersion(19133);
		super.saveWorldInfoWithPlayer(par1WorldInfo, par2NBTTagCompound);
	}

	public void flush()
	{
		try
		{
			ThreadedFileIOBase.threadedIOInstance.waitForFinish();
		}
		catch (InterruptedException interruptedexception)
		{
			interruptedexception.printStackTrace();
		}

		RegionFileCache.clearRegionFileReferences();
	}
	
	/* ======================================== ULTRAMINE START =====================================*/
	
	private boolean isSingleStorage;
	
	public void setSingleStorage()
	{
		isSingleStorage = true;
	}
}