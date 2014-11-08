package org.ultramine.server.util;

public class BasicTypeFormatter
{
	public static String formatTime(long timeMills)
	{
		return formatTime(timeMills, false);
	}
	
	public static String formatTime(long timeMills, boolean genitive)
	{
		return formatTime(timeMills, genitive, true);
	}
	
	public static String formatTime(long timeMills, boolean genitive, boolean printSec)
	{
		int seconds = (int) (timeMills / 1000) % 60 ;
		int minutes = (int) (timeMills / (60000)) % 60;
		int hours   = (int) (timeMills / (3600000)) % 24;
		int days	= (int) (timeMills / (86400000));
		
		String dayN;
		int daydd = days % 10;
		if(daydd == 0 || days > 4 && days < 21)
			dayN = "дней";
		else if(daydd == 1)
			dayN = "день";
		else //if(daydd > 1 && daydd < 5)
			dayN = "дня";
		
		String hourN;
		int hourdd = hours % 10;
		if(hourdd == 0 || hours > 4 && hours < 21)
			hourN = "часов";
		else if(hourdd == 1)
			hourN = "час";
		else //if(hourdd > 1 && hourdd < 5)
			hourN = "часа";
		
		String minN;
		int mindd = minutes % 10;
		if(hourdd == 0 || minutes > 4 && minutes < 21)
			minN = "минут";
		else if(mindd == 1)
			minN = genitive ? "минуту" : "минута";
		else //if(mindd > 1 && mindd < 5)
			minN = "минуты";
		
		String secN;
		int secdd = seconds % 10;
		if(secdd == 0 || seconds > 4 && seconds < 21)
			secN = "секунд";
		else if(secdd == 1)
			secN = genitive ? "секунду" : "секунда";
		else //if(secdd > 1 && secdd < 5)
			secN = "секунды";
		
		return
				(days > 0 ? days + " " + dayN + " " : "") +
				(hours > 0 ? hours + " " + hourN + " " : "") +
				(minutes > 0 ? minutes + " " + minN + " " : "") +
				(printSec && (seconds != 0 || minutes == 0 && hours == 0 && days == 0) ? seconds + " " + secN : "");
	}
}
