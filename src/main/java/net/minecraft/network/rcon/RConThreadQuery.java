package net.minecraft.network.rcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.server.MinecraftServer;

@SideOnly(Side.SERVER)
public class RConThreadQuery extends RConThreadBase
{
	private long lastAuthCheckTime;
	private int queryPort;
	private int serverPort;
	private int maxPlayers;
	private String serverMotd;
	private String worldName;
	private DatagramSocket querySocket;
	private byte[] buffer = new byte[1460];
	private DatagramPacket incomingPacket;
	private Map field_72644_p;
	private String queryHostname;
	private String serverHostname;
	private Map queryClients;
	private long time;
	private RConOutputStream output;
	private long lastQueryResponseTime;
	private static final String __OBFID = "CL_00001802";

	public RConThreadQuery(IServer par1IServer)
	{
		super(par1IServer, "Query Listener");
		this.queryPort = par1IServer.getIntProperty("query.port", 0);
		this.serverHostname = par1IServer.getHostname();
		this.serverPort = par1IServer.getPort();
		this.serverMotd = par1IServer.getMotd();
		this.maxPlayers = par1IServer.getMaxPlayers();
		this.worldName = par1IServer.getFolderName();
		this.lastQueryResponseTime = 0L;
		this.queryHostname = "0.0.0.0";

		if (0 != this.serverHostname.length() && !this.queryHostname.equals(this.serverHostname))
		{
			this.queryHostname = this.serverHostname;
		}
		else
		{
			this.serverHostname = "0.0.0.0";

			try
			{
				InetAddress inetaddress = InetAddress.getLocalHost();
				this.queryHostname = inetaddress.getHostAddress();
			}
			catch (UnknownHostException unknownhostexception)
			{
				this.logWarning("Unable to determine local host IP, please set server-ip in \'" + par1IServer.getSettingsFilename() + "\' : " + unknownhostexception.getMessage());
			}
		}

		if (0 == this.queryPort)
		{
			this.queryPort = this.serverPort;
			this.logInfo("Setting default query port to " + this.queryPort);
			par1IServer.setProperty("query.port", Integer.valueOf(this.queryPort));
			par1IServer.setProperty("debug", Boolean.valueOf(false));
			par1IServer.saveProperties();
		}

		this.field_72644_p = new HashMap();
		this.output = new RConOutputStream(1460);
		this.queryClients = new HashMap();
		this.time = (new Date()).getTime();
	}

	private void sendResponsePacket(byte[] par1ArrayOfByte, DatagramPacket par2DatagramPacket) throws IOException
	{
		this.querySocket.send(new DatagramPacket(par1ArrayOfByte, par1ArrayOfByte.length, par2DatagramPacket.getSocketAddress()));
	}

	private boolean parseIncomingPacket(DatagramPacket par1DatagramPacket) throws IOException
	{
		byte[] abyte = par1DatagramPacket.getData();
		int i = par1DatagramPacket.getLength();
		SocketAddress socketaddress = par1DatagramPacket.getSocketAddress();
		this.logDebug("Packet len " + i + " [" + socketaddress + "]");

		if (3 <= i && -2 == abyte[0] && -3 == abyte[1])
		{
			this.logDebug("Packet \'" + RConUtils.getByteAsHexString(abyte[2]) + "\' [" + socketaddress + "]");

			switch (abyte[2])
			{
				case 0:
					if (!this.verifyClientAuth(par1DatagramPacket).booleanValue())
					{
						this.logDebug("Invalid challenge [" + socketaddress + "]");
						return false;
					}
					else if (15 == i)
					{
						this.sendResponsePacket(this.createQueryResponse(par1DatagramPacket), par1DatagramPacket);
						this.logDebug("Rules [" + socketaddress + "]");
					}
					else
					{
						RConOutputStream rconoutputstream = new RConOutputStream(1460);
						rconoutputstream.writeInt(0);
						rconoutputstream.writeByteArray(this.getRequestID(par1DatagramPacket.getSocketAddress()));
						rconoutputstream.writeString(this.serverMotd);
						rconoutputstream.writeString("SMP");
						rconoutputstream.writeString(this.worldName);
						rconoutputstream.writeString(Integer.toString(this.getNumberOfPlayers()));
						rconoutputstream.writeString(Integer.toString(this.maxPlayers));
						rconoutputstream.writeShort((short)this.serverPort);
						rconoutputstream.writeString(this.queryHostname);
						this.sendResponsePacket(rconoutputstream.toByteArray(), par1DatagramPacket);
						this.logDebug("Status [" + socketaddress + "]");
					}
				case 9:
					this.sendAuthChallenge(par1DatagramPacket);
					this.logDebug("Challenge [" + socketaddress + "]");
					return true;
				default:
					return true;
			}
		}
		else
		{
			this.logDebug("Invalid packet [" + socketaddress + "]");
			return false;
		}
	}

	private byte[] createQueryResponse(DatagramPacket par1DatagramPacket) throws IOException
	{
		long i = MinecraftServer.getSystemTimeMillis();

		if (i < this.lastQueryResponseTime + 5000L)
		{
			byte[] abyte = this.output.toByteArray();
			byte[] abyte1 = this.getRequestID(par1DatagramPacket.getSocketAddress());
			abyte[1] = abyte1[0];
			abyte[2] = abyte1[1];
			abyte[3] = abyte1[2];
			abyte[4] = abyte1[3];
			return abyte;
		}
		else
		{
			this.lastQueryResponseTime = i;
			this.output.reset();
			this.output.writeInt(0);
			this.output.writeByteArray(this.getRequestID(par1DatagramPacket.getSocketAddress()));
			this.output.writeString("splitnum");
			this.output.writeInt(128);
			this.output.writeInt(0);
			this.output.writeString("hostname");
			this.output.writeString(this.serverMotd);
			this.output.writeString("gametype");
			this.output.writeString("SMP");
			this.output.writeString("game_id");
			this.output.writeString("MINECRAFT");
			this.output.writeString("version");
			this.output.writeString(this.server.getMinecraftVersion());
			this.output.writeString("plugins");
			this.output.writeString(this.server.getPlugins());
			this.output.writeString("map");
			this.output.writeString(this.worldName);
			this.output.writeString("numplayers");
			this.output.writeString("" + this.getNumberOfPlayers());
			this.output.writeString("maxplayers");
			this.output.writeString("" + this.maxPlayers);
			this.output.writeString("hostport");
			this.output.writeString("" + this.serverPort);
			this.output.writeString("hostip");
			this.output.writeString(this.queryHostname);
			this.output.writeInt(0);
			this.output.writeInt(1);
			this.output.writeString("player_");
			this.output.writeInt(0);
			String[] astring = this.server.getAllUsernames();
			String[] astring1 = astring;
			int j = astring.length;

			for (int k = 0; k < j; ++k)
			{
				String s = astring1[k];
				this.output.writeString(s);
			}

			this.output.writeInt(0);
			return this.output.toByteArray();
		}
	}

	private byte[] getRequestID(SocketAddress par1SocketAddress)
	{
		return ((RConThreadQuery.Auth)this.queryClients.get(par1SocketAddress)).getRequestId();
	}

	private Boolean verifyClientAuth(DatagramPacket par1DatagramPacket)
	{
		SocketAddress socketaddress = par1DatagramPacket.getSocketAddress();

		if (!this.queryClients.containsKey(socketaddress))
		{
			return Boolean.valueOf(false);
		}
		else
		{
			byte[] abyte = par1DatagramPacket.getData();
			return ((RConThreadQuery.Auth)this.queryClients.get(socketaddress)).getRandomChallenge() != RConUtils.getBytesAsBEint(abyte, 7, par1DatagramPacket.getLength()) ? Boolean.valueOf(false) : Boolean.valueOf(true);
		}
	}

	private void sendAuthChallenge(DatagramPacket par1DatagramPacket) throws IOException
	{
		RConThreadQuery.Auth auth = new RConThreadQuery.Auth(par1DatagramPacket);
		this.queryClients.put(par1DatagramPacket.getSocketAddress(), auth);
		this.sendResponsePacket(auth.getChallengeValue(), par1DatagramPacket);
	}

	private void cleanQueryClientsMap()
	{
		if (this.running)
		{
			long i = MinecraftServer.getSystemTimeMillis();

			if (i >= this.lastAuthCheckTime + 30000L)
			{
				this.lastAuthCheckTime = i;
				Iterator iterator = this.queryClients.entrySet().iterator();

				while (iterator.hasNext())
				{
					Entry entry = (Entry)iterator.next();

					if (((RConThreadQuery.Auth)entry.getValue()).hasExpired(i).booleanValue())
					{
						iterator.remove();
					}
				}
			}
		}
	}

	public void run()
	{
		this.logInfo("Query running on " + this.serverHostname + ":" + this.queryPort);
		this.lastAuthCheckTime = MinecraftServer.getSystemTimeMillis();
		this.incomingPacket = new DatagramPacket(this.buffer, this.buffer.length);

		try
		{
			while (this.running)
			{
				try
				{
					this.querySocket.receive(this.incomingPacket);
					this.cleanQueryClientsMap();
					this.parseIncomingPacket(this.incomingPacket);
				}
				catch (SocketTimeoutException sockettimeoutexception)
				{
					this.cleanQueryClientsMap();
				}
				catch (PortUnreachableException portunreachableexception)
				{
					;
				}
				catch (IOException ioexception)
				{
					this.stopWithException(ioexception);
				}
			}
		}
		finally
		{
			this.closeAllSockets();
		}
	}

	public void startThread()
	{
		if (!this.running)
		{
			if (0 < this.queryPort && 65535 >= this.queryPort)
			{
				if (this.initQuerySystem())
				{
					super.startThread();
				}
			}
			else
			{
				this.logWarning("Invalid query port " + this.queryPort + " found in \'" + this.server.getSettingsFilename() + "\' (queries disabled)");
			}
		}
	}

	private void stopWithException(Exception par1Exception)
	{
		if (this.running)
		{
			this.logWarning("Unexpected exception, buggy JRE? (" + par1Exception.toString() + ")");

			if (!this.initQuerySystem())
			{
				this.logSevere("Failed to recover from buggy JRE, shutting down!");
				this.running = false;
			}
		}
	}

	private boolean initQuerySystem()
	{
		try
		{
			this.querySocket = new DatagramSocket(this.queryPort, InetAddress.getByName(this.serverHostname));
			this.registerSocket(this.querySocket);
			this.querySocket.setSoTimeout(500);
			return true;
		}
		catch (SocketException socketexception)
		{
			this.logWarning("Unable to initialise query system on " + this.serverHostname + ":" + this.queryPort + " (Socket): " + socketexception.getMessage());
		}
		catch (UnknownHostException unknownhostexception)
		{
			this.logWarning("Unable to initialise query system on " + this.serverHostname + ":" + this.queryPort + " (Unknown Host): " + unknownhostexception.getMessage());
		}
		catch (Exception exception)
		{
			this.logWarning("Unable to initialise query system on " + this.serverHostname + ":" + this.queryPort + " (E): " + exception.getMessage());
		}

		return false;
	}

	@SideOnly(Side.SERVER)
	class Auth
	{
		private long timestamp = (new Date()).getTime();
		private int randomChallenge;
		private byte[] requestId;
		private byte[] challengeValue;
		private String requestIdAsString;
		private static final String __OBFID = "CL_00001803";

		public Auth(DatagramPacket par2DatagramPacket)
		{
			byte[] abyte = par2DatagramPacket.getData();
			this.requestId = new byte[4];
			this.requestId[0] = abyte[3];
			this.requestId[1] = abyte[4];
			this.requestId[2] = abyte[5];
			this.requestId[3] = abyte[6];
			this.requestIdAsString = new String(this.requestId);
			this.randomChallenge = (new Random()).nextInt(16777216);
			this.challengeValue = String.format("\t%s%d\u0000", new Object[] {this.requestIdAsString, Integer.valueOf(this.randomChallenge)}).getBytes();
		}

		public Boolean hasExpired(long par1)
		{
			return Boolean.valueOf(this.timestamp < par1);
		}

		public int getRandomChallenge()
		{
			return this.randomChallenge;
		}

		public byte[] getChallengeValue()
		{
			return this.challengeValue;
		}

		public byte[] getRequestId()
		{
			return this.requestId;
		}
	}
}