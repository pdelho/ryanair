### Installation
Got to the root folder and execute

`$ npm clean install`

`$ cd target`

`$ java -jar interconnecting-1.0.0-SNAPSHOT.jar`

Alernatively, you can directly execute the .jar in the root folder, which has already been compiled:

`$ java -jar interconnecting-1.0.0-SNAPSHOT.jar`

### Requirements
- Java 8
- Spring Boot 
- Maven


### Ryanair interconnecting flights tool

This application responses to following request URI with given query parameters:

http://host/context/interconnections?departure={departure}&arrival={arrival}&departureDateTime={departureDateTime}&arrivalDateTime={arrivalDateTime}

where:
- departure - a departure airport IATA code
- departureDateTime - a departure datetime in the departure airport timezone in ISO format
- arrival - an arrival airport IATA code
- arrivalDateTime - an arrival datetime in the arrival airport timezone in ISO format

for example:
http://localhost:8080/somevalidcontext/interconnections?departure=DUB&arrival=WRO&departureDateTime=2018-03-01T07:00&arrivalDateTime=2018-03-03T21:00

**Given:**
The application can consume data from the following two microservices:
- Routes API: https://services-api.ryanair.com/locate/3/routes which returns a list of all available routes based on the airports IATA codes. Please note that only routes with connectingAirport set to null and operator set to RYANAIR should be used.
- Schedules API: https://servicesapi.ryanair.com/timtbl/3/schedules/{departure}/{arrival}/years/{year}/months/{month}
which returns a list of available flights for a given departure airport IATA code, an arrival airport IATA code, a year and a month. For example (https://servicesapi.ryanair.com/timtbl/3/schedules/DUB/WRO/years/2019/months/3)



**Response**
The application returns a list of flights departing from a given departure airport not earlier than the specified departure datetime and arriving to a given arrival airport not later than the specified arrival datetime.

The list should consist of:
- all direct flights if available (for example: DUB - WRO)
- all interconnected flights with a maximum of one stop if available (for example: DUB - STN - WRO)
For interconnected flights the difference between the arrival and the next departure should be 2h or greater

The response follows the pattern:

```json
[
	{
		"stops": 0,
		"legs": [
			{
				"departureAirport": "DUB",
				"arrivalAirport": "WRO",
				"departureDateTime": "2018-03-01T12:40",
				"arrivalDateTime": "2018-03-01T16:40"
			}
		]
	},
	{
		"stops": 1,
		"legs": [
			{
				"departureAirport": "DUB",
				"arrivalAirport": "STN",
				"departureDateTime": "2018-03-01T06:25",
				"arrivalDateTime": "2018-03-01T07:35"
			},
			{
				"departureAirport": "STN",
				"arrivalAirport": "WRO",
				"departureDateTime": "2018-03-01T09:50",
				"arrivalDateTime": "2018-03-01T13:20"
			}
		]
	}
]
```