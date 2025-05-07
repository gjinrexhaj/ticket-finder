package com.rexhaj.ticketfindernew



data class EventData(
    val _embedded: EventList
)

data class EventList(
    val events: List<Event>
)

data class Event(
    val name: String,
    val url: String,
    val id : String,
    val images: List<Images>,
    val dates: Dates,
    val priceRanges: List<Prices>,
    val _embedded: VenueList
)

data class VenueList(
    val venues: List<Venue>
)

data class Venue(
    val name: String,
    val url: String,
    val city: City,
    val address: Address,
    val state: State
)

data class State(
    val name: String,
    val stateCode: String
)

data class Address(
    val line1: String,
    val line2: String,
    val line3: String
)

data class City(
    val name: String
)

data class Images(
    val url: String,
    val width: Int,
    val height: Int
)

data class Dates(
    val start: StartDate
)

data class StartDate(
    val localDate: String,
    val localTime: String,
    val dateTime: String,
    val dateTBD: Boolean,
    val timeTBD: Boolean,
    val noSpecificTime: Boolean
)

data class Prices(
    val min: Number,
    val max: Number
)