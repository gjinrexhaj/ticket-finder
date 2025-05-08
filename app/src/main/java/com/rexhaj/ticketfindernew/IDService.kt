package com.rexhaj.ticketfindernew

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


/* EXAMPLE CALL
https://app.ticketmaster.com/discovery/v2/events.json?city='CITYNAME-HERE'&classificationName='CATEGORY'&apikey=APIKEY
 */

interface IDService {

    // "." refers to the base url: https://app.ticketmaster.com/discovery/v2/
    @GET("events.json")
    fun getEventInfo(@Query("id") id: String, @Query("apikey") apikey: String): Call<EventData>
}