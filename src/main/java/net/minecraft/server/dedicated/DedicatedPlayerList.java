package net.minecraft.server.dedicated;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

import net.minecraft.server.management.ServerConfigurationManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.permission.MinecraftPermissions;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.PermissionHandler;

@SideOnly(Side.SERVER)
public class DedicatedPlayerList extends ServerConfigurationManager
{
	private static final Logger field_164439_d = LogManager.getLogger();
	private File opsList;
	private File whiteList;
	private static final String __OBFID = "CL_00001783";

	public DedicatedPlayerList(DedicatedServer par1DedicatedServer)
	{
		super(par1DedicatedServer);
		this.opsList = par1DedicatedServer.getFile("ops.txt");
		this.whiteList = par1DedicatedServer.getFile("white-list.txt");
		this.viewDistance = ConfigurationHandler.getWorldsConfig().global.chunkLoading.viewDistance;
		this.maxPlayers = ConfigurationHandler.getServerConfig().vanilla.maxPlayers;
		this.setWhiteListEnabled(ConfigurationHandler.getServerConfig().vanilla.whiteList);

		if (!par1DedicatedServer.isSinglePlayer())
		{
			this.getBannedPlayers().setListActive(true);
			this.getBannedIPs().setListActive(true);
		}

		this.getBannedPlayers().loadBanList();
		this.getBannedPlayers().saveToFileWithHeader();
		this.getBannedIPs().loadBanList();
		this.getBannedIPs().saveToFileWithHeader();
		this.readWhiteList();

		if (!this.whiteList.exists())
		{
			this.saveWhiteList();
		}
	}

	public void setWhiteListEnabled(boolean par1)
	{
		super.setWhiteListEnabled(par1);
		ConfigurationHandler.getServerConfig().vanilla.whiteList = par1;
		this.getServerInstance().saveProperties();
	}

	public void addOp(String par1Str)
	{
		super.addOp(par1Str);
		PermissionHandler.getInstance().save();
	}

	public void removeOp(String par1Str)
	{
		super.removeOp(par1Str);
		PermissionHandler.getInstance().save();
	}

	public void removeFromWhitelist(String par1Str)
	{
		super.removeFromWhitelist(par1Str);
		this.saveWhiteList();
	}

	public void addToWhiteList(String par1Str)
	{
		super.addToWhiteList(par1Str);
		this.saveWhiteList();
	}

	public void loadWhiteList()
	{
		this.readWhiteList();
	}

	private void loadOpsList()
	{
		try
		{
			this.getOps().clear();
			BufferedReader bufferedreader = new BufferedReader(new FileReader(this.opsList));
			String s = "";

			while ((s = bufferedreader.readLine()) != null)
			{
				this.getOps().add(s.trim().toLowerCase());
			}

			bufferedreader.close();
		}
		catch (Exception exception)
		{
			field_164439_d.warn("Failed to load operators list: " + exception);
		}
	}

	private void saveOpsList()
	{
		try
		{
			PrintWriter printwriter = new PrintWriter(new FileWriter(this.opsList, false));
			Iterator iterator = this.getOps().iterator();

			while (iterator.hasNext())
			{
				String s = (String)iterator.next();
				printwriter.println(s);
			}

			printwriter.close();
		}
		catch (Exception exception)
		{
			field_164439_d.warn("Failed to save operators list: " + exception);
		}
	}

	private void readWhiteList()
	{
		try
		{
			this.getWhiteListedPlayers().clear();
			BufferedReader bufferedreader = new BufferedReader(new FileReader(this.whiteList));
			String s = "";

			while ((s = bufferedreader.readLine()) != null)
			{
				this.getWhiteListedPlayers().add(s.trim().toLowerCase());
			}

			bufferedreader.close();
		}
		catch (Exception exception)
		{
			field_164439_d.warn("Failed to load white-list: " + exception);
		}
	}

	private void saveWhiteList()
	{
		try
		{
			PrintWriter printwriter = new PrintWriter(new FileWriter(this.whiteList, false));
			Iterator iterator = this.getWhiteListedPlayers().iterator();

			while (iterator.hasNext())
			{
				String s = (String)iterator.next();
				printwriter.println(s);
			}

			printwriter.close();
		}
		catch (Exception exception)
		{
			field_164439_d.warn("Failed to save white-list: " + exception);
		}
	}

	public boolean isAllowedToLogin(String par1Str)
	{
		par1Str = par1Str.trim().toLowerCase();
		return !this.isWhiteListEnabled()
				|| this.getWhiteListedPlayers().contains(par1Str)
				|| PermissionHandler.getInstance().hasGlobally(par1Str, MinecraftPermissions.IGNORE_WHITE_LIST);
	}

	public DedicatedServer getServerInstance()
	{
		return (DedicatedServer)super.getServerInstance();
	}
}