package com.delho.ryanair.interconnecting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {
	
	private Integer month;
	private Day[] days;
	
	public Integer getMonth() {
		return month;
	}
	
	public void setMonth(Integer month) {
		this.month = month;
	}
	
	public Day[] getDays() {
		return days;
	}
	
	public void setDays(Day[]  days) {
		this.days = days;
	}
	
	@Override
	public String toString() {
		StringBuilder schedules = new StringBuilder("Schedules for month " + month +":\n");
		for (Day day: days)
		{
			schedules.append('\t' + day.toString() + '\n');
		}
		return schedules.toString();
	}
	
	

}
