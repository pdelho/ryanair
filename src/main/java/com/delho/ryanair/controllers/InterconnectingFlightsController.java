package com.delho.ryanair.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.delho.ryanair.constants.Constants;
import com.delho.ryanair.interconnecting.model.Route;



@RestController
public class InterconnectingFlightsController {

	private static final String template = "Hello, %s!";
	
	@RequestMapping(value = "/routes")
	public ResponseEntity<Route[]> getRoutes() 
	{
		RestTemplate restTemplateRoutes = new RestTemplate();
		ResponseEntity<Route[]> responseEntity = restTemplateRoutes.getForEntity(Constants.ROUTES_API, Route[].class);
		Route[] routes = responseEntity.getBody();
		return new ResponseEntity<>(routes, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/flights")
	public ResponseEntity<Route[]> getFlights() 
	{
		RestTemplate restTemplateRoutes = new RestTemplate();
		ResponseEntity<Route[]> responseEntity = restTemplateRoutes.getForEntity(Constants.ROUTES_API, Route[].class);
		Route[] routes = responseEntity.getBody();
		return new ResponseEntity<>(routes, HttpStatus.OK);
	}
	
	

    @RequestMapping("/interconnections")
    public ResponseEntity<List<Route>> interconnections(@RequestParam(value="departure", defaultValue="MAD") String departure) {
    	
    	RestTemplate restTemplateRoutes = new RestTemplate();
		ResponseEntity<Route[]> responseEntity = restTemplateRoutes.getForEntity(Constants.ROUTES_API, Route[].class);
		List<Route> routes = Arrays.asList(responseEntity.getBody());
    	List<Route> result = routes.stream()                // convert list to stream
                .filter(route -> departure.equals(route.getAirportFrom()))     
                .collect(Collectors.toList()); 
    	
    	return new ResponseEntity<List<Route>>(result, HttpStatus.OK);
    }
}
