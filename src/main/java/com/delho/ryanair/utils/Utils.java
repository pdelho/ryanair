package com.delho.ryanair.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.delho.ryanair.constants.Constants;

public class Utils {
	
	public Calendar getCalendarFromFlightDate(Integer year, Integer month, Integer day, String dateString)
	{
		String[] time = dateString.split ( ":" );
		int hour = Integer.parseInt ( time[0].trim() );
		int minute = Integer.parseInt ( time[1].trim() );
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month-1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		return calendar;

	}
	
	public String getFormatedCalendar(Calendar calendar)
	{
		Date date = calendar.getTime();  
		DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_PATTERN);  
		return dateFormat.format(date);
	}

}
