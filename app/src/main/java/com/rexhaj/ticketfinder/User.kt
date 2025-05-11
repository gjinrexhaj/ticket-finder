package com.rexhaj.ticketfinder

data class User(
    val email: String? = null,
    val uid: String? = null,
    val favorites: MutableList<String>? = null
)
