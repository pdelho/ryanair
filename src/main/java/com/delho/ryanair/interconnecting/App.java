package com.delho.ryanair.interconnecting;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.delho.ryanair.interconnecting.model.Route;
import com.delho.ryanair.interconnecting.model.Schedule;

public class App 
{
	// private static final Logger LOG = LoggerFactory.getLogger(App.class);
	
	private static final String ROUTES_API = "https://services-api.ryanair.com/locate/3/routes";
	private static final String SCHEDULES_API ="https://services-api.ryanair.com/timtbl/3/schedules/DUB/MAD/years/2019/months/10";
	
	
    public static void main( String[] args )
    {
    	RestTemplate restTemplateRoutes = new RestTemplate();
    	ResponseEntity<Route[]> responseEntity = restTemplateRoutes.getForEntity(ROUTES_API, Route[].class);
    	Route[] routes = responseEntity.getBody();
    	for (Route route: routes)
    	{
    		System.out.println(route.toString());
    	}
    	
    	RestTemplate restTemplateSchedules = new RestTemplate();
    	ResponseEntity<Schedule> responseEntitySchedules = restTemplateSchedules.getForEntity(SCHEDULES_API, Schedule.class);
    	Schedule schedule = responseEntitySchedules.getBody();
    	System.out.println(schedule);

    }
}
