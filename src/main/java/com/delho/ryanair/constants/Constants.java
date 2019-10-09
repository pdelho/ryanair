package com.delho.ryanair.constants;

import java.util.Calendar;

public class Constants {

	public static final String ROUTES_API = "https://services-api.ryanair.com/locate/3/routes";
	public static final String SCHEDULES_API_PATTERN = "https://services-api.ryanair.com/timtbl/3/schedules/%s/%s/years/%s/months/%s";
	
	private static final Calendar calendar = Calendar.getInstance();
	// One month later and zero index , next year?
	private static int offset = calendar.get(Calendar.MONTH)+1 > 11 ? 1 : 0;
	// One month more as API is non zero based
	public static final String SCHEDULES_API_TEST =	String.format(Constants.SCHEDULES_API_PATTERN, "DUB", "WRO", calendar.get(Calendar.YEAR)+offset, (calendar.get(Calendar.MONTH) +1)%12+1);

	public static final Integer DIRECT_FLIGHT = 0;
	public static final Integer CONNECTION_FLIGHT = 1;
	public static final Integer CONNECTION_FLIGHT_HOURS = 2;
	
	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm";
}
