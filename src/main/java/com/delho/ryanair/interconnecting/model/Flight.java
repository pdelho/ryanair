package com.delho.ryanair.interconnecting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {

	private String number;
	private String departureTime;
	private String arrivalTime;
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getDepartureTime() {
		return departureTime;
	}
	
	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}
	
	public String getArrivalTime() {
		return arrivalTime;
	}
	
	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	
	@Override
	public String toString() {
		return "Flight{" +
				"Number= '" + number + '\'' +
				",Departing time= '" + departureTime.toString() + '\'' +
				",Arrival time= '" + arrivalTime.toString() + '\'' +
				'}';
	}
}
