package com.delho.ryanair.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
		final RestTemplate restTemplateRoutes = new RestTemplate();
		final ResponseEntity<Schedule> responseEntity = restTemplateRoutes.getForEntity(scheduleAPI, Schedule.class);
		return responseEntity.getBody();
	}
	
    @RequestMapping("/interconnections")
    public ResponseEntity<List<Connection>> interconnections(
    		@RequestParam(value="departure", required=false, defaultValue="DUB") final String departure,
    		@RequestParam(value="arrival", required=false, defaultValue="WRO") final String arrival,
    		@RequestParam(value="departureDateTime", required=false) @DateTimeFormat(pattern=Constants.DATE_PATTERN) final Date departureDateTime,
    		@RequestParam(value="arrivalDateTime", required=false) @DateTimeFormat(pattern=Constants.DATE_PATTERN) final Date arrivalDateTime) {
    	
    	// We will return a list of connections
    	List <Connection> availableConnections = new ArrayList<Connection>();
    	List <Leg> directConnections = new ArrayList<Leg>();
    	List <Leg> interConnections = new ArrayList<Leg>();
    	
    	// Use calendars instead of Date as some methods are deprecated
    	final Calendar departureDate = Calendar.getInstance();
    	departureDate.setTime(departureDateTime);
    	final Integer departingYear = departureDate.get(Calendar.YEAR);
    	// Month is 0-index based
    	final Integer departingMonth = departureDate.get(Calendar.MONTH)+1;
		
		final Calendar arrivalDate = Calendar.getInstance();
		arrivalDate.setTime(arrivalDateTime);
		final Integer arrivingYear = arrivalDate.get(Calendar.YEAR);
    	// Month is 0-index based
    	final Integer arrivingMonth = arrivalDate.get(Calendar.MONTH)+1;
		
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
				this.getFlights(directConnections, departingRoute, arrival, departingYear, departingMonth, departureDate, arrivalDate, true);
			}
			
			// For each arriving route, if its departure is equal to departure's arrival
			for (Route arrivingRoute: arrivingRoutes)
			{
				if (arrivingRoute.getAirportFrom().equals(departingRoute.getAirportTo()))
				{
					LOG.info("There is a possible interconnection between {}-{}-{}", departingRoute.getAirportFrom(), departingRoute.getAirportTo(), arrivingRoute.getAirportTo());
					
					
				}
			}
		}
		
		// Set direct flights if any
		if (!directConnections.isEmpty() && directConnections!= null)
		{
			for (Leg leg: directConnections)
			{
				Connection connection = new Connection(Constants.DIRECT_FLIGHT, leg);
				availableConnections.add(connection);
			}		
		}
    	
    	return new ResponseEntity<List<Connection>>(availableConnections, HttpStatus.OK);
    }
    
    protected void getFlights (List <Leg> directConnections, final Route departingRoute, final String arrival, 
    		final Integer departingYear, final Integer departingMonth, final Calendar departureDate, final Calendar arrivalDate,
    		Boolean isDirect)
    {
    	final String scheduleAPI = String.format(Constants.SCHEDULES_API_PATTERN, departingRoute.getAirportFrom(), arrival, departingYear, departingMonth);
		final Schedule schedule =  this.getSchedule(scheduleAPI);
		for (Day day: schedule.getDays())
		{
			for (Flight flight : day.getFlights())
			{
				Calendar departingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getDepartureTime());
				// FIXME Check if day or month changes
				Calendar arrivingDateFlight = utils.getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getArrivalTime());
				// Departing date must be after and arrival date must be before
				if (departingDateFlight.after(departureDate) && arrivingDateFlight.before(arrivalDate))
				{
					LOG.info("Flight departing at {} and arriving at {} available", utils.getFormatedCalendar(departingDateFlight), utils.getFormatedCalendar(arrivingDateFlight));
					Leg leg = new Leg();
					leg.setDepartureAirport(departingRoute.getAirportFrom());
					leg.setArrivalAirport(arrival);
					leg.setDepartureDateTime(utils.getFormatedCalendar(departingDateFlight));
					leg.setArrivalDateTime(utils.getFormatedCalendar(arrivingDateFlight));
					directConnections.add(leg);
				}
			}					
		}	
    }
}
