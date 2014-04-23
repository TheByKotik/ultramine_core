package net.minecraft.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptManager
{
	private static final String __OBFID = "CL_00001483";

	@SideOnly(Side.CLIENT)
	public static SecretKey createNewSharedKey()
	{
		try
		{
			KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
			keygenerator.init(128);
			return keygenerator.generateKey();
		}
		catch (NoSuchAlgorithmException nosuchalgorithmexception)
		{
			throw new Error(nosuchalgorithmexception);
		}
	}

	public static KeyPair createNewKeyPair()
	{
		try
		{
			KeyPairGenerator keypairgenerator = KeyPairGenerator.getInstance("RSA");
			keypairgenerator.initialize(1024);
			return keypairgenerator.generateKeyPair();
		}
		catch (NoSuchAlgorithmException nosuchalgorithmexception)
		{
			nosuchalgorithmexception.printStackTrace();
			System.err.println("Key pair generation failed!");
			return null;
		}
	}

	public static byte[] getServerIdHash(String par0Str, PublicKey par1PublicKey, SecretKey par2SecretKey)
	{
		try
		{
			return digestOperation("SHA-1", new byte[][] {par0Str.getBytes("ISO_8859_1"), par2SecretKey.getEncoded(), par1PublicKey.getEncoded()});
		}
		catch (UnsupportedEncodingException unsupportedencodingexception)
		{
			unsupportedencodingexception.printStackTrace();
			return null;
		}
	}

	private static byte[] digestOperation(String par0Str, byte[] ... par1ArrayOfByte)
	{
		try
		{
			MessageDigest messagedigest = MessageDigest.getInstance(par0Str);
			byte[][] abyte1 = par1ArrayOfByte;
			int i = par1ArrayOfByte.length;

			for (int j = 0; j < i; ++j)
			{
				byte[] abyte2 = abyte1[j];
				messagedigest.update(abyte2);
			}

			return messagedigest.digest();
		}
		catch (NoSuchAlgorithmException nosuchalgorithmexception)
		{
			nosuchalgorithmexception.printStackTrace();
			return null;
		}
	}

	public static PublicKey decodePublicKey(byte[] par0ArrayOfByte)
	{
		try
		{
			X509EncodedKeySpec x509encodedkeyspec = new X509EncodedKeySpec(par0ArrayOfByte);
			KeyFactory keyfactory = KeyFactory.getInstance("RSA");
			return keyfactory.generatePublic(x509encodedkeyspec);
		}
		catch (NoSuchAlgorithmException nosuchalgorithmexception)
		{
			;
		}
		catch (InvalidKeySpecException invalidkeyspecexception)
		{
			;
		}

		System.err.println("Public key reconstitute failed!");
		return null;
	}

	public static SecretKey decryptSharedKey(PrivateKey par0PrivateKey, byte[] par1ArrayOfByte)
	{
		return new SecretKeySpec(decryptData(par0PrivateKey, par1ArrayOfByte), "AES");
	}

	@SideOnly(Side.CLIENT)
	public static byte[] encryptData(Key par0Key, byte[] par1ArrayOfByte)
	{
		return cipherOperation(1, par0Key, par1ArrayOfByte);
	}

	public static byte[] decryptData(Key par0Key, byte[] par1ArrayOfByte)
	{
		return cipherOperation(2, par0Key, par1ArrayOfByte);
	}

	private static byte[] cipherOperation(int par0, Key par1Key, byte[] par2ArrayOfByte)
	{
		try
		{
			return createTheCipherInstance(par0, par1Key.getAlgorithm(), par1Key).doFinal(par2ArrayOfByte);
		}
		catch (IllegalBlockSizeException illegalblocksizeexception)
		{
			illegalblocksizeexception.printStackTrace();
		}
		catch (BadPaddingException badpaddingexception)
		{
			badpaddingexception.printStackTrace();
		}

		System.err.println("Cipher data failed!");
		return null;
	}

	private static Cipher createTheCipherInstance(int par0, String par1Str, Key par2Key)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(par1Str);
			cipher.init(par0, par2Key);
			return cipher;
		}
		catch (InvalidKeyException invalidkeyexception)
		{
			invalidkeyexception.printStackTrace();
		}
		catch (NoSuchAlgorithmException nosuchalgorithmexception)
		{
			nosuchalgorithmexception.printStackTrace();
		}
		catch (NoSuchPaddingException nosuchpaddingexception)
		{
			nosuchpaddingexception.printStackTrace();
		}

		System.err.println("Cipher creation failed!");
		return null;
	}

	public static Cipher func_151229_a(int p_151229_0_, Key p_151229_1_)
	{
		try
		{
			Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
			cipher.init(p_151229_0_, p_151229_1_, new IvParameterSpec(p_151229_1_.getEncoded()));
			return cipher;
		}
		catch (GeneralSecurityException generalsecurityexception)
		{
			throw new RuntimeException(generalsecurityexception);
		}
	}
}