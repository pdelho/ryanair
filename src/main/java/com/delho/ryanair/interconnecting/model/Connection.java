package com.delho.ryanair.interconnecting.model;

public class Connection {
	
	private Integer stops;
	private Leg leg;
	
	public Connection(Integer stops, Leg leg) {
		super();
		this.stops = stops;
		this.leg = leg;
	}

	public Integer getStops() {
		return stops;
	}
	
	public void setStops(Integer stops) {
		this.stops = stops;
	}
	
	public Leg getLeg() {
		return leg;
	}
	
	public void setLegs(Leg leg) {
		this.leg = leg;
	}

}
