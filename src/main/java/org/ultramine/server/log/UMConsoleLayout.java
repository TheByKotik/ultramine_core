package org.ultramine.server.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.helpers.Charsets;
import org.apache.logging.log4j.core.helpers.Constants;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

@Plugin(name = "UMConsoleLayout", category = "Core", elementType = "layout", printObject = false)
public class UMConsoleLayout extends AbstractStringLayout
{
	private static final boolean useUMConsole = Boolean.parseBoolean(System.getProperty("org.ultramine.server.umconsole"));
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	protected UMConsoleLayout(Charset charset)
	{
		super(charset);
	}

	@Override
	public String toSerializable(LogEvent event)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append('[');
		sb.append(dateFormat.format(event.getMillis()));
		sb.append("] ");
		
		sb.append('[');
		Level level = event.getLevel();
		if(useUMConsole)
		{
			if(level == Level.WARN)
				sb.append("\u00A7e");
			else if(level == Level.ERROR)
				sb.append("\u00A7c");
			else if(level == Level.FATAL)
				sb.append("\u00A74");
		}
		String levelS = event.getLevel().toString();
		int llen = levelS.length();
		if(llen > 4)
			levelS = levelS.substring(0, 4);
		sb.append(levelS);
		if(llen < 4)
			sb.append(' ');
		if(useUMConsole && (level == Level.WARN || level == Level.ERROR || level == Level.FATAL))
			sb.append("\u00A7r");
		sb.append("] ");
		
		String msg = event.getMessage().getFormattedMessage();
		if(useUMConsole)
		{
			sb.append(msg);
		}
		else
		{
			for(int i = 0, s = msg.length(); i < s; i++)
			{
				char c = msg.charAt(i);
				if(c == '\u00a7')
				{
					i++;
					continue;
				}
				sb.append(c);
			}
		}
		
		Throwable t = event.getThrown();
		if(t != null)
		{
			StringWriter w = new StringWriter();
			t.printStackTrace(new PrintWriter(w));
			sb.append(Constants.LINE_SEP);
			sb.append(w.toString());
		}
		
		sb.append(Constants.LINE_SEP);
		
		return sb.toString();
	}

	@Override
	public Map<String, String> getContentFormat()
	{
		return Collections.emptyMap();
	}
	
	@PluginFactory
	public static UMConsoleLayout createLayout(@PluginAttribute("charset") final String charsetName)
	{
		return new UMConsoleLayout(Charsets.getSupportedCharset(charsetName));
	}
}
