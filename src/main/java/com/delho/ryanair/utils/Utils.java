package com.delho.ryanair.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.delho.ryanair.constants.Constants;

public class Utils {
	
	public Calendar getCalendarFromFlightDate(final Integer year, final Integer month, final Integer day, final String dateString, final Integer interconnectionHours)
	{
		final String[] time = dateString.split ( ":" );
		final int hour = Integer.parseInt ( time[0].trim() );
		final int minute = Integer.parseInt ( time[1].trim() );
		
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		// Zero index based
		calendar.set(Calendar.MONTH, month-1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour + interconnectionHours);
		calendar.set(Calendar.MINUTE, minute);
		return calendar;

	}
	
	public String getFormatedCalendar(final Calendar calendar)
	{
		final Date date = calendar.getTime();  
		final DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_PATTERN);  
		return dateFormat.format(date);
	}

}
