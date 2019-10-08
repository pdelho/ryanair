package com.delho.ryanair.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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



@RestController
public class InterconnectingFlightsController {
	
	private static final Logger LOG = LoggerFactory.getLogger(InterconnectingFlightsController.class);
	
	private Utils utils = new Utils();
	
	@RequestMapping(value = "/routes")
	public ResponseEntity<List<Route>> getRoutes() 
	{
		final RestTemplate restTemplateRoutes = new RestTemplate();
		final ResponseEntity<Route[]> responseEntity = restTemplateRoutes.getForEntity(Constants.ROUTES_API, Route[].class);
		final List<Route> routes = Arrays.asList(responseEntity.getBody());
		return new ResponseEntity<List<Route>>(routes, HttpStatus.OK);
	}
	
	protected List<Route> getRoutes(String API) 
	{
		final RestTemplate restTemplateRoutes = new RestTemplate();
		final ResponseEntity<Route[]> responseEntity = restTemplateRoutes.getForEntity(API, Route[].class);
		return Arrays.asList(responseEntity.getBody());
	}
	
	@RequestMapping(value = "/schedule")
	public ResponseEntity<Schedule> getSchedule() 
	{
		final RestTemplate restTemplateRoutes = new RestTemplate();
		final ResponseEntity<Schedule> responseEntity = restTemplateRoutes.getForEntity(Constants.SCHEDULES_API_TEST, Schedule.class);
		final Schedule schedule = responseEntity.getBody();
		return new ResponseEntity<Schedule>(schedule, HttpStatus.OK);
	}
	
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
	
	protected Schedule getSchedule(final String scheduleAPI)
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
	
    @RequestMapping("/interconnections")
    public ResponseEntity<List<Connection>> interconnections(
    		@RequestParam(value="departure", required=false, defaultValue="DUB") final String departure,
    		@RequestParam(value="arrival", required=false, defaultValue="WRO") final String arrival,
    		@RequestParam(value="departureDateTime", required=false) @DateTimeFormat(pattern=Constants.DATE_PATTERN) final Date departureDateTime,
    		@RequestParam(value="arrivalDateTime", required=false) @DateTimeFormat(pattern=Constants.DATE_PATTERN) final Date arrivalDateTime) {
    	
    	// We will return a list of connections
    	List <Connection> availableConnections = new ArrayList<Connection>();
    	
    	// Use calendars instead of Date as some methods are deprecated
    	final Calendar departureDate = Calendar.getInstance();
    	departureDate.setTime(departureDateTime);
    	final Integer departingYear = departureDate.get(Calendar.YEAR);
    	// Month is 0-index based
    	final Integer departingMonth = departureDate.get(Calendar.MONTH)+1;
		
		final Calendar arrivalDate = Calendar.getInstance();
		arrivalDate.setTime(arrivalDateTime);
		//		final Integer arrivingYear = arrivalDate.get(Calendar.YEAR);
		//    	// Month is 0-index based
		//    	final Integer arrivingMonth = arrivalDate.get(Calendar.MONTH)+1;
		
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
				LOG.info("There is a possible direct connection between {} and {}", departingRoute.getAirportFrom(), arrival);
				LOG.info("Checking schedules...");
				this.setDirecteFlights(availableConnections, departingRoute, arrival, departingYear, departingMonth, departureDate, arrivalDate);
			}
			
			// For each arriving route, if its departure is equal to departure's arrival, possible interconnection
			for (Route arrivingRoute: arrivingRoutes)
			{
				if (arrivingRoute.getAirportFrom().equals(departingRoute.getAirportTo()))
				{
					// Structure:
					// Airport 1 - (Flight1) - Airport 2 - (Flight 2) - Airport 3
					LOG.info("There is a possible interconnection between {}-{}-{}", departingRoute.getAirportFrom(), departingRoute.getAirportTo(), arrivingRoute.getAirportTo());
					LOG.info("Checking schedules...");
					
					// Flight 1
					// Get schedules for flight1 (airport 1 to airport 2)
					final String scheduleOneAPI = String.format(Constants.SCHEDULES_API_PATTERN, departingRoute.getAirportFrom(), departingRoute.getAirportTo(), departingYear, departingMonth);
					final Schedule scheduleOne =  this.getSchedule(scheduleOneAPI);
					if (scheduleOne != null)
					{
						// List of available flights for leg 1, we will have to iterate them
						List <Leg> legsOne = new ArrayList<Leg>();
						for (Day day: scheduleOne.getDays())
						{
							for (Flight flight : day.getFlights())
							{
								final Calendar departingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getDepartureTime(), Constants.DIRECT_FLIGHT);
								// FIXME Check if day or month changes
								final Calendar arrivingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getArrivalTime(), Constants.DIRECT_FLIGHT);
								// Departing date must be after and arrival date must be before
								if (departingDateFlight.after(departureDate) && arrivingDateFlight.before(arrivalDate))
								{
									LOG.info("First flight departing at {} and arriving at {} available", utils.getFormatedCalendar(departingDateFlight), utils.getFormatedCalendar(arrivingDateFlight));
									Leg legOne = new Leg();
									legOne.setDepartureAirport(departingRoute.getAirportFrom());
									legOne.setArrivalAirport(departingRoute.getAirportTo());
									legOne.setDepartureDateTime(utils.getFormatedCalendar(departingDateFlight));
									legOne.setArrivalDateTime(utils.getFormatedCalendar(arrivingDateFlight));
									legsOne.add(legOne);
								}
								else
								{
									LOG.debug("Flight departing at {} and arriving at {} is not possible", utils.getFormatedCalendar(departingDateFlight), utils.getFormatedCalendar(arrivingDateFlight));
								}
							}					
						}
						
						// For each first leg available 
						for (Leg legOne : legsOne)
						{
							// Get schedules for flight 2 (airport 2 or leg 1 arrival to airport 3)
							final String scheduleTwoAPI = String.format(Constants.SCHEDULES_API_PATTERN, arrivingRoute.getAirportFrom(), arrivingRoute.getAirportTo(), departingYear, departingMonth);
							final Schedule scheduleTwo =  this.getSchedule(scheduleTwoAPI);
							if (scheduleTwo != null)
							{
								for (Day day: scheduleTwo.getDays())
								{
									for (Flight flight : day.getFlights())
									{
										final Calendar departingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getDepartureTime(), Constants.DIRECT_FLIGHT);
										// FIXME Check if day or month changes
										final Calendar arrivingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getArrivalTime(), Constants.DIRECT_FLIGHT);
										
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
								    	legOneArrivalWithInterconnection.set(Calendar.HOUR_OF_DAY, legOneArrivalWithInterconnection.get(Calendar.HOUR_OF_DAY) + Constants.CONNECTION_FLIGHT_HOURS);
								    	
										
										// Departing date must be after and arrival date must be before
										// AND departing date with interconnection before that arrivalDateFlight
										if (departingDateFlight.after(departureDate) && arrivingDateFlight.before(arrivalDate)
												&& legOneArrivalWithInterconnection.before(departingDateFlight))
										{
											LOG.info("Second flight departing at {} and arriving at {} available", utils.getFormatedCalendar(departingDateFlight), utils.getFormatedCalendar(arrivingDateFlight));
											Leg legTwo = new Leg();
											legTwo.setDepartureAirport(arrivingRoute.getAirportFrom());
											legTwo.setArrivalAirport(arrivingRoute.getAirportTo());
											legTwo.setDepartureDateTime(utils.getFormatedCalendar(departingDateFlight));
											legTwo.setArrivalDateTime(utils.getFormatedCalendar(arrivingDateFlight));
											// Add leg one and leg two as an interconnected flight
											final List<Leg> legs = new ArrayList<Leg>();
											legs.add(legOne);
											legs.add(legTwo);
											Connection connection = new Connection (Constants.CONNECTION_FLIGHT, legs);
											availableConnections.add(connection);
											
										}
										else
										{
											LOG.debug("Flight departing at {} and arriving at {} is not possible", utils.getFormatedCalendar(departingDateFlight), utils.getFormatedCalendar(arrivingDateFlight));
										}
									}					
								}
							}
						}
						
					}			
				}
			}
		}
		
		// Set interconnected flights if any
    	
    	return new ResponseEntity<List<Connection>>(availableConnections, HttpStatus.OK);
    }
    
    protected void setDirecteFlights (final List<Connection> connections, final Route departingRoute, final String arrival, 
    		final Integer departingYear, final Integer departingMonth, final Calendar departureDate, final Calendar arrivalDate)
    {
    	final String scheduleAPI = String.format(Constants.SCHEDULES_API_PATTERN, departingRoute.getAirportFrom(), arrival, departingYear, departingMonth);
		final Schedule schedule =  this.getSchedule(scheduleAPI);
		if (schedule != null)
		{
			for (Day day: schedule.getDays())
			{
				for (Flight flight : day.getFlights())
				{
					final Calendar departingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getDepartureTime(), Constants.DIRECT_FLIGHT);
					// FIXME Check if day or month changes
					final Calendar arrivingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getArrivalTime(), Constants.DIRECT_FLIGHT);
					// Departing date must be after and arrival date must be before
					if (departingDateFlight.after(departureDate) && arrivingDateFlight.before(arrivalDate))
					{
						LOG.info("Flight departing at {} and arriving at {} available", utils.getFormatedCalendar(departingDateFlight), utils.getFormatedCalendar(arrivingDateFlight));
						final List<Leg> legs = new ArrayList<Leg>();
						final Leg leg = new Leg();
						leg.setDepartureAirport(departingRoute.getAirportFrom());
						leg.setArrivalAirport(arrival);
						leg.setDepartureDateTime(utils.getFormatedCalendar(departingDateFlight));
						leg.setArrivalDateTime(utils.getFormatedCalendar(arrivingDateFlight));
						legs.add(leg);
						final Connection connection = new Connection(Constants.DIRECT_FLIGHT, legs);
						connections.add(connection);
					}
					else
					{
						LOG.debug("Flight departing at {} and arriving at {} is not possible", utils.getFormatedCalendar(departingDateFlight), utils.getFormatedCalendar(arrivingDateFlight));
					}
				}					
			}
		}
			
    }
}
