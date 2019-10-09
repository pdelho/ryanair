package com.delho.ryanair.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.config.ResourceNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.delho.ryanair.constants.Constants;
import com.delho.ryanair.interconnecting.enumeration.Operator;
import com.delho.ryanair.interconnecting.model.Connection;
import com.delho.ryanair.interconnecting.model.Day;
import com.delho.ryanair.interconnecting.model.Flight;
import com.delho.ryanair.interconnecting.model.Leg;
import com.delho.ryanair.interconnecting.model.Route;
import com.delho.ryanair.interconnecting.model.Schedule;
import com.delho.ryanair.utils.Utils;


/**
 * Controller to map interconnecting flights
 * @author Pablo del Hoyo pablodelhoyo@gmail.com
 * @version 1.0
 *
 */
@RestController
public class InterconnectingFlightsController {
	
	/**
	 * The LOG constant
	 */
	private static final Logger LOG = LoggerFactory.getLogger(InterconnectingFlightsController.class);
	
	/**
	 * The utils class
	 */
	private final Utils utils = new Utils();
	
	/**
	 * Map all routes request
	 * @return all routes
	 */
	@RequestMapping(value = "/routes")
	public ResponseEntity<List<Route>> getRoutes() 
	{
		final RestTemplate restTemplateRoutes = new RestTemplate();
		final ResponseEntity<Route[]> responseEntity = restTemplateRoutes.getForEntity(Constants.ROUTES_API, Route[].class);
		final List<Route> routes = Arrays.asList(responseEntity.getBody());
		return new ResponseEntity<List<Route>>(routes, HttpStatus.OK);
	}
	
	/**
	 * Provides a List of routes given the API
	 * @param API string containing the routes API
	 * @return a list of routes
	 */
	public List<Route> getRoutes(String API) 
	{
		final RestTemplate restTemplateRoutes = new RestTemplate();
		final ResponseEntity<Route[]> responseEntity = restTemplateRoutes.getForEntity(API, Route[].class);
		return Arrays.asList(responseEntity.getBody());
	}
	
	/**
	 * Maps a single route request for testing purpose
	 * @return a schedule with the flights from WRO to DUB in October 2019
	 */
	@RequestMapping(value = "/schedule")
	public ResponseEntity<Schedule> getSchedule() 
	{
		final RestTemplate restTemplateRoutes = new RestTemplate();
		final ResponseEntity<Schedule> responseEntity = restTemplateRoutes.getForEntity(Constants.SCHEDULES_API_TEST, Schedule.class);
		final Schedule schedule = responseEntity.getBody();
		return new ResponseEntity<Schedule>(schedule, HttpStatus.OK);
	}
	
	/**
	 * Maps a schedule request
	 * @param departure string of the departing airport (DUB)
	 * @param arrival string of the arriving airport (WRO)
	 * @param year string representing a year (2019)
	 * @param month string representing a month (1 for January)
	 * @return a schedule containing the days and the flights for the given parameters
	 */
	@RequestMapping(value = "/schedules/{departure}/{arrival}/years/{year}/months/{month}")
	public ResponseEntity<Schedule> getSchedule(
			@PathVariable(value="departure", required=true) final String departure,
			@PathVariable(value="arrival", required=true) final String arrival,
			@PathVariable(value="year", required=true) final String year,
			@PathVariable(value="month", required=true) final String month) 
	{
		final RestTemplate restTemplateRoutes = new RestTemplate();
		final ResponseEntity<Schedule> responseEntity = restTemplateRoutes.getForEntity(Constants.SCHEDULES_API_TEST, Schedule.class);
		final Schedule schedule = responseEntity.getBody();
		return new ResponseEntity<>(schedule, HttpStatus.OK);
	}
	
	/**
	 * Return a schedule given a schedule API
	 * @param scheduleAPI string of the scheduleAPI (https://services-api.ryanair.com/timtbl/3/schedules/DUB/WRO/years/2019/months/10)
	 * @return the schedule or null if something went wrong
	 */
	public Schedule getSchedule(final String scheduleAPI)
	{
		try
		{
			final RestTemplate restTemplateRoutes = new RestTemplate();
			final ResponseEntity<Schedule> responseEntity = restTemplateRoutes.getForEntity(scheduleAPI, Schedule.class);
			return responseEntity.getBody();
		}
		catch (ResourceNotFoundException | HttpClientErrorException e)
		{
			LOG.error(e.getMessage());
			return null;
		}
		
	}
	
	/**
	 * Return a list of flights departing from a given departure airport not
		earlier than the specified departure datetime and arriving to a given arrival airport not
		later than the specified arrival datetime.
		The list consist of:
			- all direct flights if available (for example: DUB - WRO)
			- all interconnected flights with a maximum of one stop if available (for example:DUB - STN - WRO)
				For interconnected flights the difference between the arrival and the next departure
				must be 2h or greater
	 * @param departure a departure airport IATA code
	 * @param arrival an arrival airport IATA cod
	 * @param departureDateTime a departure datetime in the departure airport timezone in ISO format
	 * @param arrivalDateTime an arrival datetime in the departure airport timezone in ISO format
	 * @return the list of flights fulfilling the conditions. Empty otherwise
	 */
    @RequestMapping("/interconnections")
    public ResponseEntity<PriorityQueue<Connection>> interconnections(
    		@RequestParam(value="departure", required=false, defaultValue="DUB") final String departure,
    		@RequestParam(value="arrival", required=false, defaultValue="WRO") final String arrival,
    		@RequestParam(value="departureDateTime", required=false) @DateTimeFormat(pattern=Constants.DATE_PATTERN) final Date departureDateTime,
    		@RequestParam(value="arrivalDateTime", required=false) @DateTimeFormat(pattern=Constants.DATE_PATTERN) final Date arrivalDateTime) {
    	
    	// We will return a list of connections ordered by number of stops
    	Comparator<Connection> stopsSorter = Comparator.comparing(Connection::getStops);
    	PriorityQueue <Connection> availableConnections = new PriorityQueue<Connection>(stopsSorter);
    	
    	// Get all routes
		List<Route> allRoutes = this.getRoutes(Constants.ROUTES_API);
		// Filter Ryanair only
		allRoutes = allRoutes.stream()               
                .filter(route -> Operator.RYANAIR.toString().equals(route.getOperator()))     
                .collect(Collectors.toList());
		
		// Get all routes departing from departure
		List<Route> departingRoutes = allRoutes.stream()               
                .filter(route -> departure.equals(route.getAirportFrom()))     
                .collect(Collectors.toList());
		
		// Get all routes arriving to arrival
		List<Route> arrivingRoutes = allRoutes.stream()               
                .filter(route -> arrival.equals(route.getAirportTo()))     
                .collect(Collectors.toList());
		
		// For each departing route
		for (Route departingRoute: departingRoutes)
		{
			// If its destination is equal to arrival, possible direct connection
			if (arrival.equals(departingRoute.getAirportTo()))
			{
				LOG.debug("There is a possible direct connection between {} and {}", departingRoute.getAirportFrom(), arrival);
				LOG.debug("Checking schedules...");
				this.setDirectFlights(availableConnections, departingRoute.getAirportFrom(), arrival, departureDateTime, arrivalDateTime, true);
			}
			
			// For each arriving route, if its departure is equal to departure's arrival, possible interconnection
			for (Route arrivingRoute: arrivingRoutes)
			{
				if (departingRoute.getAirportTo().equals(arrivingRoute.getAirportFrom()))
				{
					// Structure:
					// Airport 1 - (Flight1) - Airport 2 - (Flight 2) - Airport 3
					LOG.debug("There is a possible interconnection between {}-{}-{}", departingRoute.getAirportFrom(), departingRoute.getAirportTo(), arrivingRoute.getAirportTo());
					LOG.debug("Checking schedules...");
					
					// List of available flights for leg 1, we will have to iterate them
					PriorityQueue <Connection> legsOneList = new PriorityQueue<Connection>(stopsSorter);
					
					// Flight 1
					// Get schedules for flight1 (direct from airport 1 to airport 2) and save them into listLegOne
					this.setDirectFlights(legsOneList, departingRoute.getAirportFrom(), departingRoute.getAirportTo(), departureDateTime, arrivalDateTime, false);

					// Flight 2
					// Get schedules for flight2 and check if the interconnection is possible with flight1. If so, save it into availableConnections
					this.setInterconnectedFlights(availableConnections, legsOneList, arrivingRoute.getAirportFrom(), arrivingRoute.getAirportTo(), departureDateTime, arrivalDateTime);
				}
			}
		}
		
		if (availableConnections.isEmpty() || availableConnections == null) {
            LOG.debug("No flights matching your requirements");;
            return new ResponseEntity<PriorityQueue<Connection>>(HttpStatus.NOT_FOUND);
        }
		
    	return new ResponseEntity<PriorityQueue<Connection>>(availableConnections, HttpStatus.OK);
    }
    
    /**
     * Store into a given list of connections the direct flights given a departure and arrival, 
		which departure later than a given date and and arrive earlier than a given date
     * @param availableConnections list of connections which stores the available flights
	 * @param departure a departure airport IATA code
	 * @param arrival an arrival airport IATA cod
	 * @param departureDateTime a departure datetime in the departure airport timezone in ISO format
	 * @param arrivalDateTime an arrival datetime in the departure airport timezone in ISO format
     * @param isOneLeg true if it is a direct flight (for logging purpose)
     */
    protected void setDirectFlights (final PriorityQueue<Connection> availableConnections, final String departure, final String arrival,
    		final Date departureDateTime, final Date arrivalDateTime, final Boolean isOneLeg)
    {
    	// Use calendars instead of Date as some methods are deprecated
    	final Calendar departureDate = Calendar.getInstance();
    	departureDate.setTime(departureDateTime);
    	final Integer departingYear = departureDate.get(Calendar.YEAR);
    	// Month is 0-index based
    	final Integer departingMonth = departureDate.get(Calendar.MONTH)+1;
		
		final Calendar arrivalDate = Calendar.getInstance();
		arrivalDate.setTime(arrivalDateTime);
    	
		// Get the schedule
    	final String scheduleAPI = String.format(Constants.SCHEDULES_API_PATTERN, departure, arrival, departingYear, departingMonth);
		final Schedule schedule =  this.getSchedule(scheduleAPI);
		if (schedule != null)
		{
			for (Day day: schedule.getDays())
			{
				for (Flight flight : day.getFlights())
				{
					// For each flight of each day, check if schedule is correct (true as is a direct flight and null first leg)
					List<Calendar> departingArrivalFlightDates= new ArrayList<Calendar>();
					if (this.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate,
							day, flight, departingArrivalFlightDates,
							true, null))
					{
						// If so, set the leg, add it to legs, and store connection
						final List<Leg> legs = new ArrayList<Leg>();
						final Leg leg = new Leg();
						leg.setDepartureAirport(departure);
						leg.setArrivalAirport(arrival);
						leg.setDepartureDateTime(utils.getFormatedCalendar(departingArrivalFlightDates.get(0)));
						leg.setArrivalDateTime(utils.getFormatedCalendar(departingArrivalFlightDates.get(1)));
						legs.add(leg);
						final Connection connection = new Connection(Constants.DIRECT_FLIGHT, legs);
						if (isOneLeg)
						{
							LOG.info("Direct flight departing at {} and arriving at {} available", utils.getFormatedCalendar(departingArrivalFlightDates.get(0)), utils.getFormatedCalendar(departingArrivalFlightDates.get(1)));
						}
						else
						{
							LOG.debug("First flight departing at {} and arriving at {} available", utils.getFormatedCalendar(departingArrivalFlightDates.get(0)), utils.getFormatedCalendar(departingArrivalFlightDates.get(1)));
						}
						
						availableConnections.add(connection);
					}
					else
					{
						LOG.debug("Flight departing at {} and arriving at {} is not possible", utils.getFormatedCalendar(departingArrivalFlightDates.get(0)), utils.getFormatedCalendar(departingArrivalFlightDates.get(1)));
					}
				}					
			}
		}
			
    }
    
    /**
     * Store into a given list of connections the interconnected flights given a departure and arrival of the second airport, 
		which departure later than a given date and and arrive earlier than a given date.
		And also a list of first flights, checking if the interconnection is possible
     * @param availableConnections list of connections which stores the available flights
     * @param legsOneConnections list of first flight which may provide a possible interconnection to final destination
     * @param departure of the second flight
     * @param arrival of the second flight
	 * @param departureDateTime a departure datetime in the departure airport timezone in ISO format
	 * @param arrivalDateTime an arrival datetime in the departure airport timezone in ISO format
     */
    protected void setInterconnectedFlights (final PriorityQueue<Connection> availableConnections, final PriorityQueue<Connection> legsOneConnections, final String departure, final String arrival, 
    		final Date departureDateTime, final Date arrivalDateTime)
    {
    	
    	// Use calendars instead of Date as some methods are deprecated
    	final Calendar departureDate = Calendar.getInstance();
    	departureDate.setTime(departureDateTime);
    	final Integer departingYear = departureDate.get(Calendar.YEAR);
    	// Month is 0-index based
    	final Integer departingMonth = departureDate.get(Calendar.MONTH)+1;
		
		final Calendar arrivalDate = Calendar.getInstance();
		arrivalDate.setTime(arrivalDateTime);
    	
    	// For each first leg available
		final Iterator<Connection> legsOne = legsOneConnections.iterator();
		while(legsOne.hasNext())
		{
			Connection connectionOne = legsOne.next(); 
			for (Leg legOne : connectionOne.getLegs())
			{
				// Get schedules for flight 2 (airport 2 or leg 1 arrival to airport 3)
				final String scheduleTwoAPI = String.format(Constants.SCHEDULES_API_PATTERN, departure, arrival, departingYear, departingMonth);
				final Schedule scheduleTwo =  this.getSchedule(scheduleTwoAPI);
				if (scheduleTwo != null)
				{
					for (Day day: scheduleTwo.getDays())
					{
						for (Flight flight : day.getFlights())
						{
							// For each flight of each day, check if schedule is correct (false as is a direct flight)
							List<Calendar> departingArrivalFlightDates= new ArrayList<Calendar>();			
							if (this.isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate, 
									day, flight, departingArrivalFlightDates, 
									false, legOne))
							{
								Leg legTwo = new Leg();
								legTwo.setDepartureAirport(departure);
								legTwo.setArrivalAirport(arrival);
								legTwo.setDepartureDateTime(utils.getFormatedCalendar(departingArrivalFlightDates.get(0)));
								legTwo.setArrivalDateTime(utils.getFormatedCalendar(departingArrivalFlightDates.get(1)));
								// Add leg one and leg two as an interconnected flight
								LOG.debug("Second flight departing at {} and arriving at {} available", utils.getFormatedCalendar(departingArrivalFlightDates.get(0)), utils.getFormatedCalendar(departingArrivalFlightDates.get(1)));
								final List<Leg> legs = new ArrayList<Leg>();
								legs.add(legOne);
								legs.add(legTwo);
								LOG.info("First flight depating at {} from {} and arriving at {} to {} available",
										legOne.getDepartureDateTime(), legOne.getDepartureAirport(), legOne.getArrivalDateTime(), legOne.getArrivalAirport());
								LOG.info("Second flight depating at {} from {} and arriving at {} to {} available",
										legTwo.getDepartureDateTime(), legTwo.getDepartureAirport(), legTwo.getArrivalDateTime(), legTwo.getArrivalAirport());
								Connection connection = new Connection (Constants.CONNECTION_FLIGHT, legs);
								availableConnections.add(connection);
								
							}
							else
							{
								LOG.debug("Flight departing at {} and arriving at {} is not possible", utils.getFormatedCalendar(departingArrivalFlightDates.get(0)), utils.getFormatedCalendar(departingArrivalFlightDates.get(1)));
							}
						}					
					}
				}
			}
		}
    }
    
    /**
     * Provides if a connection is possible or not
     	If direct flight
     		true if departing date of flight after than given departing date
     		and arrival date of flight before than given date
     	If is interconnected flight
     		true if departing date of flight after than given departing date
     		and arrival date of flight before than given date
     		AND (arriving date of first leg plus time for interconnection) before than arrivalDateFlight
     * @param departingYear year of the departure desired
     * @param departingMonth month of the departure desired
     * @param departureDate calendar of the departure desired
     * @param arrivalDate calendar of the arrival desired
     * @param day day of the flight
     * @param flight to be checked
     * @param departingArrivalFlightDates the dates of the departing and the arrival of the flights (to be stored)
     * @param isDirect boolean to indicate if the flight is direct
     * @param legOne first leg used to take into account the time needed for interconnection
     * @return boolean indicating whether a connection is possible or not
     */
    public Boolean isScheduleAvailable (final Integer departingYear, final Integer departingMonth, Calendar departureDate, Calendar arrivalDate,
    		final Day day, final Flight flight,	List<Calendar> departingArrivalFlightDates, 
    		final Boolean isDirect, final Leg legOne)
    {
    	final Calendar departingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getDepartureTime(), Constants.DIRECT_FLIGHT);
		// FIXME Check if day or month changes
		final Calendar arrivingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getArrivalTime(), Constants.DIRECT_FLIGHT);
		departingArrivalFlightDates.add(departingDateFlight);
		departingArrivalFlightDates.add(arrivingDateFlight);
		
		if (isDirect)
		{
			// Departing date must be after and arrival date must be before
			return departingDateFlight.after(departureDate) && arrivingDateFlight.before(arrivalDate);
		}
		else
		{
			// Warning we have to add two hours to leg one arrival time and ensure that is before departing time
			final String legOneArrivalWithInterconnectionString = legOne.getArrivalDateTime();
			Date legOneArrivalWithInterconnectionDate = new Date();
			try 
			{
				legOneArrivalWithInterconnectionDate = new SimpleDateFormat(Constants.DATE_PATTERN).parse(legOneArrivalWithInterconnectionString);
			} 
			catch (final ParseException e) 
			{
				LOG.error(e.getMessage());
			} 
	    	final Calendar legOneArrivalWithInterconnection = Calendar.getInstance();
	    	legOneArrivalWithInterconnection.setTime(legOneArrivalWithInterconnectionDate);
	    	// Increase two hours
	    	legOneArrivalWithInterconnection.add(Calendar.HOUR_OF_DAY, Constants.CONNECTION_FLIGHT_HOURS);
	    	
	    	// Departing date must be after and arrival date must be before
	    	// AND departing date with interconnection before that arrivalDateFlight
	    	return departingDateFlight.after(departureDate) && arrivingDateFlight.before(arrivalDate)
			&& legOneArrivalWithInterconnection.before(departingDateFlight);
			
		}
    }
}
