package org.ultramine.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AsyncIOUtils
{
	private static final Logger log = LogManager.getLogger();
	
	public static void writeString(final File file, final String data)
	{
		GlobalExecutors.writingIOExecutor().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					FileUtils.writeStringToFile(file, data, Charsets.UTF_8);
				}
				catch(IOException e)
				{
					log.warn("Failed to write file: "+file.getAbsolutePath(), e);
				}
			}
		});
	}
	
	public static void writeBytes(final File file, final byte[] data)
	{
		GlobalExecutors.writingIOExecutor().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					FileUtils.writeByteArrayToFile(file, data);
				}
				catch(IOException e)
				{
					log.warn("Failed to write file: "+file.getAbsolutePath(), e);
				}
			}
		});
	}
	
	public static void safeWriteNBT(final File file, final NBTTagCompound nbt)
	{
		GlobalExecutors.writingIOExecutor().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					File file1 = new File(file.getParentFile(), file.getName()+".tmp");
					CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(file1));

					if (file.exists())
						file.delete();
					file1.renameTo(file);
				}
				catch(IOException e)
				{
					log.warn("Failed to write file: "+file.getAbsolutePath(), e);
				}
			}
		});
	}
}
