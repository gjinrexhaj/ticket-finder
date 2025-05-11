package com.rexhaj.ticketfinder.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rexhaj.ticketfinder.Event
import com.rexhaj.ticketfinder.R


private const val TAG = "RecyclerAdapter"

val user = FirebaseAuth.getInstance()

class RecyclerAdapter(private val events: List<Event>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


    // Inner class
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Provides reference to views present in a row item

        val eventImage = itemView.findViewById<ImageView>(R.id.rv_imageView_eventImage)
        val eventName = itemView.findViewById<TextView>(R.id.rv_textView_eventName)
        val venueNameAndCity = itemView.findViewById<TextView>(R.id.rv_textView_venueNameAndCity)
        val venueAddress = itemView.findViewById<TextView>(R.id.rv_textView_venueAddress)
        val eventDateAndTime = itemView.findViewById<TextView>(R.id.rv_textView_eventDateAndTime)
        val priceRange = itemView.findViewById<TextView>(R.id.rv_textView_priceRange)
        val seeTickets = itemView.findViewById<Button>(R.id.rv_button_seeTickets)
        var directionsButton = itemView.findViewById<Button>(R.id.rv_button_directions)
        lateinit var favoriteButton: ImageButton
        //val favoriteButton = itemView.findViewById<Button>(R.id.rv_button_favorite)


        // define a var to store url into
        var thisURL = ""
        var thisID = ""
        var thisAddress = ""

        // Primary constructor
        init {

            // if user is logged in, show and create ref to favorite button
            if (user.currentUser != null) {
                Log.d(TAG, "signed in, showing favorite button")
                favoriteButton = itemView.findViewById(R.id.rv_button_favorite)
                favoriteButton.visibility = VISIBLE

                // Implement favorite button, which appends ID of favorited event to
                // 'favorites' array on firestore
                favoriteButton.setOnClickListener() {
                    Log.d(TAG, "favorite button clicked: thisID: $thisID")

                    val db = FirebaseFirestore.getInstance()

                    var favorites: Any?

                    db.collection("users").document(user.currentUser!!.uid)
                        .get()
                        .addOnSuccessListener { documents ->

                            // get favorites
                            favorites = documents.get("favorites")
                            Log.d(TAG, "document.get(\"favorites\") = $favorites")

                            // if list DNE, make a new one
                            // otherwise, search entire list, if duplicate, remove entry, if none, add entry
                            if (favorites != null) {
                                var favList: MutableList<String>
                                favList = favorites as MutableList<String>
                                Log.d(TAG, "favList = ${favList}")
                                // now search entire list and handle adding/removing fav
                                Log.d(TAG, "looping thought favList...")
                                var duplicate = false


                                val iterator = favList.iterator()
                                while (iterator.hasNext()) {
                                    val element = iterator.next()
                                    if (element == thisID) {
                                        Log.d(
                                            TAG,
                                            "Duplicate found, removing ${favList.iterator()}"
                                        )
                                        iterator.remove()
                                        duplicate = true
                                        Toast.makeText(
                                            itemView.context,
                                            "Successfully removed from favorites",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                }

                                if (!duplicate) {
                                    Log.d(TAG, "no duplicates found, appending $thisID to favList")
                                    favList.add(thisID)
                                    Log.d(TAG, "favList = $favList")
                                    Toast.makeText(
                                        itemView.context,
                                        "Successfully added to favorites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                // Upload new favList to remote db
                                val userDocument =
                                    db.collection("users").document(user.currentUser!!.uid)

                                userDocument.update("favorites", favList)
                                    .addOnSuccessListener {
                                        // List successfully uploaded
                                        Log.d(
                                            TAG,
                                            "Successfully uploaded ${favList} to favorites key in db"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle the error
                                        Log.d(
                                            TAG,
                                            "Failed to upload ${favList} to favorites key in db"
                                        )
                                    }
                            } else {
                                // create new list on db and populate with thisID
                                Log.d(
                                    TAG,
                                    "favList = null, creating new list and adding ID into it"
                                )
                                val newList = mutableListOf<String>()
                                newList.add(thisID)

                                val userDocument =
                                    db.collection("users").document(user.currentUser!!.uid)

                                userDocument.update("favorites", newList)
                                    .addOnSuccessListener {
                                        // List successfully uploaded
                                        Log.d(
                                            TAG,
                                            "Successfully uploaded ${newList} to favorites key in db"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle the error
                                        Log.d(
                                            TAG,
                                            "Failed to upload ${newList} to favorites key in db"
                                        )
                                    }

                            }
                        }
                        .addOnFailureListener() {
                            Log.d(TAG, "${db.collection("favorites").get()} has failed")
                        }
                }
            }

            // implement directions functionality
            directionsButton.setOnClickListener() {
                Log.d(TAG, "directionsButton clicked! thisAddress = $thisAddress")

                val gmmIntentUri =
                    Uri.parse("geo:0,0?q=$thisAddress")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                itemView.context.startActivity(mapIntent)
            }

            // initially set prices ranges to be invisible
            priceRange.isVisible = false
            seeTickets.setOnClickListener() {
                Log.d(
                    TAG, "seeTickets() has been clicked | " +
                            "adapterPosition: $adapterPosition | " +
                            "thisURL: $thisURL"
                )

                // Open web browser implicit intent
                val browserIntent = Intent(Intent.ACTION_VIEW)
                //browserIntent.data = Uri.parse(thisURL)
                browserIntent.data = thisURL.toUri()
                itemView.context.startActivity(browserIntent)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create new views
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.search_row_item, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        val numEvents = events.size
        Log.d(TAG, "recyclerView count is: $numEvents")
        return numEvents
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Create references to data returned from API
        val currentEvent = events[position]
        val eventName = currentEvent.name
        val images = currentEvent.images
        val url = currentEvent.url
        val id = currentEvent.id
        val dates = currentEvent.dates
        val priceRanges = currentEvent.priceRanges
        val venue = currentEvent._embedded.venues[0]

        // Set entry items appropriately
        // EVENT NAME
        holder.eventName.text = eventName
        // EVENT VENUE AND CITY
        val venueNameAndCity = "${venue.name}, ${venue.city.name}"
        holder.venueNameAndCity.text = venueNameAndCity
        // EVENT ADDRESS
        var address = "${venue.address.line1}, ${venue.city.name}"
        if (venue.state.stateCode != null) {
            address += ", ${venue.state.stateCode}"
        }
        holder.venueAddress.text = address
        // DATE AND TIME
        var ref = dates.start
        var rawDate = ref.localDate
        var dateAndTime = "Date: ${convertDate(rawDate)}"
        if (ref.dateTBD) {
            dateAndTime = "Date: TBD"
        } else if (ref.noSpecificTime) {
            dateAndTime += " @ no particular time"
        } else if (ref.timeTBD) {
            dateAndTime += " @ Time TBD"
        } else {
            val militaryTime = ref.localTime
            dateAndTime += " @ ${convertMilitaryTimeToRegular(militaryTime)}"
        }
        holder.eventDateAndTime.text = dateAndTime
        // TICKET RANGE - show first hit for priceRanges
        if (priceRanges != null) {
            holder.priceRange.isVisible = true
            holder.priceRange.text = "$${priceRanges[0].min} - $${priceRanges[0].max} "
        }
        // EVENT IMAGE
        val highestQualityImage = images.maxByOrNull {
            it.width * it.height
        } // Load URL using glide
        val context = holder.itemView.context
        if (highestQualityImage != null) {
            val link = highestQualityImage.url
            Log.d(TAG, " --------- Glide ---------\n.load($link)\n.into(${holder.eventImage})")
            Glide.with(context)
                .load(link)
                .into(holder.eventImage)
        } else {
            Log.d(TAG, "highestQualityImage = null")
        }
        // BUTTON FUNCTIONALITY
        holder.thisURL = url
        holder.thisID = id
        holder.thisAddress = address

    }


    // helper function to convert military time into standard time
    fun convertMilitaryTimeToRegular(militaryTime: String): String {
        val (hours, minutes) = militaryTime.split(":").map { it.toInt() }

        val period = if (hours < 12 || hours == 24) "AM" else "PM"
        var regularHours = if (hours == 0 || hours == 24) 12 else if (hours > 12) hours - 12 else hours

        return String.format("%d:%02d %s", regularHours, minutes, period)
    }

    // helper function to convert date
    fun convertDate(date: String): String {
        var splitDate = date.split("-")
        val newDate = "${splitDate[1]}/${splitDate[2]}/${splitDate[0]}"
        return newDate
    }
}