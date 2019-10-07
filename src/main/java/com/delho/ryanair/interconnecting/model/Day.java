package com.delho.ryanair.interconnecting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Day {
	
	private Integer day;
	private Flight[] flights;
	
	public Integer getDay() {
		return day;
	}
	
	public void setDay(Integer day) {
		this.day = day;
	}
	
	public Flight[] getFlights() {
		return flights;
	}
	
	public void setFlights(Flight[] flights) {
		this.flights = flights;
	}
	
	@Override
	public String toString() {
		StringBuilder days = new StringBuilder("Flights for day " + day +":\n");
		for (Flight flight: flights)
		{
			days.append("\t\t" + flight.toString() + '\n');
		}
		return days.toString();
	}
	
	

}
