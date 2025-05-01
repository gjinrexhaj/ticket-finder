package com.rexhaj.ticketfindernew

data class User(
    val username: String? = null,
    val email: String? = null,
    val id: Number? = null,
    val favorites: EventList? = null
)
