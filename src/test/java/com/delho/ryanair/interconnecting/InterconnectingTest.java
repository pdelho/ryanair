package com.delho.ryanair.interconnecting;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import com.delho.ryanair.constants.Constants;
import com.delho.ryanair.controllers.InterconnectingFlightsController;
import com.delho.ryanair.interconnecting.model.Day;
import com.delho.ryanair.interconnecting.model.Flight;
import com.delho.ryanair.interconnecting.model.Leg;
import com.delho.ryanair.interconnecting.model.Route;
import com.delho.ryanair.interconnecting.model.Schedule;
import com.delho.ryanair.utils.Utils;

public class InterconnectingTest {
	
	private static InterconnectingFlightsController controller = new InterconnectingFlightsController();
	private static Utils utils = new Utils();
	
	private static final Integer OCTOBER = 10;

	private static List<Route> allRoutes = controller.getRoutes(Constants.ROUTES_API);
	private static Schedule scheduleDUBWRO = controller.getSchedule(Constants.SCHEDULES_API_TEST);
	private static Schedule customSchedule = generateSchedule();
	
	
	private static Schedule generateSchedule()
	{
		Schedule testSchedule = new Schedule();
		// Assume 2019, October 1st and 2nd 2019. 2 days, 2 flights per day
		testSchedule.setMonth(OCTOBER);
		Day[] days = new Day[3];
		
		// Day 1
		Day dayOne = new Day();
		dayOne.setDay(1);
		Flight[] flightsDayOne = new Flight[2];
		// Flight 11 and 12
		Flight dayOneFlightOne = new Flight();
		dayOneFlightOne.setDepartureTime("9:00");
		dayOneFlightOne.setArrivalTime("11:00");
		flightsDayOne[0] = dayOneFlightOne;
		Flight dayOneFlightTwo = new Flight();
		dayOneFlightTwo.setDepartureTime("14:00");
		dayOneFlightTwo.setArrivalTime("16:00");
		flightsDayOne[1] = dayOneFlightTwo;
		dayOne.setFlights(flightsDayOne);
		days[0] = dayOne;
		
		// Day 2
		Day dayTwo = new Day();
		dayTwo.setDay(2);
		Flight[] flightsDayTwo = new Flight[2];
		// Flight 22 and 21
		Flight dayTwoFlightOne = new Flight();
		dayTwoFlightOne.setDepartureTime("9:00");
		dayTwoFlightOne.setArrivalTime("11:00");
		flightsDayTwo[0] = dayTwoFlightOne;
		Flight dayTwoFlightTwo = new Flight();
		dayTwoFlightTwo.setDepartureTime("14:00");
		dayTwoFlightTwo.setArrivalTime("16:00");
		flightsDayTwo[1] = dayTwoFlightTwo;
		dayTwo.setFlights(flightsDayTwo);
		days[1] = dayTwo;
		
		testSchedule.setDays(days);
		return testSchedule;
	}
	
	
	@Test
	public void workingRoutesAPI() {
		assertNotNull(allRoutes);
		assertTrue(allRoutes.size()>0);
	}
	
	@Test
	public void workingScheduleAPI() {
		assertNotNull(scheduleDUBWRO);
		assertNotNull(scheduleDUBWRO.getDays());
		assertTrue(scheduleDUBWRO.getDays()[0].getDay()>=1);
		assertTrue(scheduleDUBWRO.getDays()[0].getDay()<=31);
		assertNotNull(scheduleDUBWRO.getDays()[0].getFlights());
		assertNotNull(scheduleDUBWRO.getDays()[0].getFlights()[0].getArrivalTime());
		
	}
	
	@Test
	public void directFlightsCheck() {
		
		Calendar departureDate = utils.getCalendarFromFlightDate(2019, OCTOBER, 1, "10:00", Constants.DIRECT_FLIGHT);
		Calendar arrivalDate = utils.getCalendarFromFlightDate(2019, OCTOBER, 2, "13:00", Constants.DIRECT_FLIGHT);
		Integer departingYear = 2019;
		Integer departingMonth = OCTOBER;
		Day one = customSchedule.getDays()[0];
		Flight oneOne = one.getFlights()[0];
		Flight oneTwo = one.getFlights()[1];
		Day two = customSchedule.getDays()[1];
		Flight twoOne = two.getFlights()[0];
		Flight twoTwo = two.getFlights()[1];
		List<Calendar> departingArrivalFlightDates= new ArrayList<Calendar>();
		// Too early
		assertFalse(controller.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate, one, oneOne, departingArrivalFlightDates, true, null));
		// OK
		assertTrue(controller.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate, one, oneTwo, departingArrivalFlightDates, true, null));
		// OK
		assertTrue(controller.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate, two, twoOne, departingArrivalFlightDates, true, null));
		// Too late
		assertFalse(controller.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate, two, twoTwo, departingArrivalFlightDates, true, null));

	}
	
	@Test
	public void interconnectedFlightsCheck() {
		
		Calendar departureDate = utils.getCalendarFromFlightDate(2019, OCTOBER, 1, "10:00", Constants.DIRECT_FLIGHT);
		Calendar arrivalDate = utils.getCalendarFromFlightDate(2019, OCTOBER, 2, "13:00", Constants.DIRECT_FLIGHT);
		Integer departingYear = 2019;
		Integer departingMonth = OCTOBER;
		Day one = customSchedule.getDays()[0];
		Flight oneOne = one.getFlights()[0];
		Flight oneTwo = one.getFlights()[1];
		Day two = customSchedule.getDays()[1];
		Flight twoOne = two.getFlights()[0];
		Flight twoTwo = two.getFlights()[1];
		List<Calendar> departingArrivalFlightDates= new ArrayList<Calendar>();
		
		// Leg one, we are coming from another place
		Leg leg = new Leg();
		leg.setArrivalDateTime("2019-10-01T13:00");
		
		// Too early
		assertFalse(controller.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate, one, oneOne, departingArrivalFlightDates, false, leg));
		// One hour to transfer is not enough to take the flight 2
		assertFalse(controller.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate, one, oneTwo, departingArrivalFlightDates, false, leg));
		// OK
		assertTrue(controller.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate, two, twoOne, departingArrivalFlightDates, false, leg));
		// Too late
		assertFalse(controller.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate, two, twoTwo, departingArrivalFlightDates, false, leg));

	}
	
	

}
