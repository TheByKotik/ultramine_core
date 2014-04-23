package net.minecraft.util;

import java.util.Iterator;

public class ChatComponentText extends ChatComponentStyle
{
	private final String text;
	private static final String __OBFID = "CL_00001269";

	public ChatComponentText(String p_i45159_1_)
	{
		this.text = p_i45159_1_;
	}

	public String getChatComponentText_TextValue()
	{
		return this.text;
	}

	public String getUnformattedTextForChat()
	{
		return this.text;
	}

	public ChatComponentText createCopy()
	{
		ChatComponentText chatcomponenttext = new ChatComponentText(this.text);
		chatcomponenttext.setChatStyle(this.getChatStyle().createShallowCopy());
		Iterator iterator = this.getSiblings().iterator();

		while (iterator.hasNext())
		{
			IChatComponent ichatcomponent = (IChatComponent)iterator.next();
			chatcomponenttext.appendSibling(ichatcomponent.createCopy());
		}

		return chatcomponenttext;
	}

	public boolean equals(Object par1Obj)
	{
		if (this == par1Obj)
		{
			return true;
		}
		else if (!(par1Obj instanceof ChatComponentText))
		{
			return false;
		}
		else
		{
			ChatComponentText chatcomponenttext = (ChatComponentText)par1Obj;
			return this.text.equals(chatcomponenttext.getChatComponentText_TextValue()) && super.equals(par1Obj);
		}
	}

	public String toString()
	{
		return "TextComponent{text=\'" + this.text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getChatStyle() + '}';
	}
}