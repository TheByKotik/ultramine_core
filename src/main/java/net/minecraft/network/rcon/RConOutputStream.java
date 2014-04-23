package net.minecraft.network.rcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SideOnly(Side.SERVER)
public class RConOutputStream
{
	private ByteArrayOutputStream byteArrayOutput;
	private DataOutputStream output;
	private static final String __OBFID = "CL_00001798";

	public RConOutputStream(int par1)
	{
		this.byteArrayOutput = new ByteArrayOutputStream(par1);
		this.output = new DataOutputStream(this.byteArrayOutput);
	}

	public void writeByteArray(byte[] par1ArrayOfByte) throws IOException
	{
		this.output.write(par1ArrayOfByte, 0, par1ArrayOfByte.length);
	}

	public void writeString(String par1Str) throws IOException
	{
		this.output.writeBytes(par1Str);
		this.output.write(0);
	}

	public void writeInt(int par1) throws IOException
	{
		this.output.write(par1);
	}

	public void writeShort(short par1) throws IOException
	{
		this.output.writeShort(Short.reverseBytes(par1));
	}

	public byte[] toByteArray()
	{
		return this.byteArrayOutput.toByteArray();
	}

	public void reset()
	{
		this.byteArrayOutput.reset();
	}
}