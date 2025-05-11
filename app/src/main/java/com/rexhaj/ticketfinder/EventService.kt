package com.rexhaj.ticketfinder

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


/* EXAMPLE CALL
https://app.ticketmaster.com/discovery/v2/events.json?city='CITYNAME-HERE'&classificationName='CATEGORY'&apikey=APIKEY
 */

interface EventService {

    // "." refers to the base url: https://app.ticketmaster.com/discovery/v2/
    @GET("events.json")
    fun getEventInfo(@Query("city") city: String, @Query("classificationName") category: String,
                     @Query("sort") sort: String, @Query("apikey") apikey: String): Call<EventData>
}