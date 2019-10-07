package com.delho.ryanair.interconnecting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.delho.ryanair.constants.Constants;
import com.delho.ryanair.interconnecting.model.Route;
import com.delho.ryanair.interconnecting.model.Schedule;

@SpringBootApplication
@ComponentScan("com.delho.ryanair.controllers")
public class App 
{
	private static final Logger LOG = LoggerFactory.getLogger(App.class);
	
    public static void main( String[] args )
    {
    	if(LOG.isDebugEnabled())
    	{
    		RestTemplate restTemplateRoutes = new RestTemplate();
        	ResponseEntity<Route[]> responseEntity = restTemplateRoutes.getForEntity(Constants.ROUTES_API, Route[].class);
        	Route[] routes = responseEntity.getBody();
        	for (Route route: routes)
        	{
        		LOG.debug(route.toString());
        	}
        	
        	RestTemplate restTemplateSchedules = new RestTemplate();
        	ResponseEntity<Schedule> responseEntitySchedules = restTemplateSchedules.getForEntity(Constants.SCHEDULES_API, Schedule.class);
        	Schedule schedule = responseEntitySchedules.getBody();
        	LOG.debug(schedule.toString());
    	}
    		
    	SpringApplication.run(App.class, args);

    }
}
