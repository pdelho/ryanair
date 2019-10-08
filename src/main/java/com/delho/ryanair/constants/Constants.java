package com.delho.ryanair.constants;

public class Constants {

	public static final String ROUTES_API = "https://services-api.ryanair.com/locate/3/routes";
	public static final String SCHEDULES_API_TEST =	"https://services-api.ryanair.com/timtbl/3/schedules/DUB/WRO/years/2019/months/10";
	public static final String SCHEDULES_API_PATTERN = "https://services-api.ryanair.com/timtbl/3/schedules/%s/%s/years/%s/months/%s";
	
	public static final Integer DIRECT_FLIGHT = 0;
	public static final Integer CONNECTION_FLIGHT = 1;
	
	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm";
}
