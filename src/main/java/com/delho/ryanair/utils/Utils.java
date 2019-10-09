package com.delho.ryanair.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.delho.ryanair.constants.Constants;

public class Utils {
	
	/**
	 * Generates a calendar given some parameters of the flight
	 * @param year the year of the calendar (2019)
	 * @param month the month of the calendar (1 for January, 12 for December, will take into account that is zero index based)
	 * @param day the day of the month (25)
	 * @param dateString a string indicating the hour and minutes with the pattern HH:mm (14:30)
	 * @param interconnectionHours offset in hours to be added to the hours is it is a interconnecting flight (typically 2)
	 * @return the Calendar with all the parameters given
	 */
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
	
	/**
	 * Format a calendar into a string with the format yyyy-MM-dd'T'HH:mm
	 * @param calendar the calendar to be formated
	 * @return string of the formated calendar
	 */
	public String getFormatedCalendar(final Calendar calendar)
	{
		final Date date = calendar.getTime();  
		final DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_PATTERN);  
		return dateFormat.format(date);
	}

}
