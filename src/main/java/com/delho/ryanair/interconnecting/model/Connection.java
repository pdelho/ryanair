package com.delho.ryanair.interconnecting.model;

import java.util.List;

public class Connection {
	
	private Integer stops;
	private List<Leg> legs;
	
	public Connection(Integer stops, List<Leg> legs) {
		super();
		this.stops = stops;
		this.legs = legs;
	}

	public Integer getStops() {
		return stops;
	}
	
	public void setStops(Integer stops) {
		this.stops = stops;
	}
	
	public List<Leg> getLegs() {
		return legs;
	}
	
	public void setLegs(List<Leg> legs) {
		this.legs = legs;
	}

}
