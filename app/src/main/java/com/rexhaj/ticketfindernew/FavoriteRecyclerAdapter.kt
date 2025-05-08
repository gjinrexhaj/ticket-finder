package com.rexhaj.ticketfindernew

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
/*

private const val TAG = "RecyclerAdapter"

val userInstance = FirebaseAuth.getInstance()

class FavoriteRecyclerAdapter (private val events: List<Event>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


    // Inner class
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Provides reference to views present in a row item

        // TODO: IMPLEMENT FAV-RECYCL-ADAPTER
        val eventName = itemView.findViewById<TextView>(R.id.rv_fav_eventName)
        val venueNameAndCity = itemView.findViewById<TextView>(R.id.rv_fav_venueNameAndCity)
        val venueAddress = itemView.findViewById<TextView>(R.id.rv_fav_venueAddress)
        val eventDateAndTime = itemView.findViewById<TextView>(R.id.rv_fav_eventDateAndTime)
        val priceRange = itemView.findViewById<TextView>(R.id.rv_fav_priceRange)
        val seeTickets = itemView.findViewById<Button>(R.id.rv_fav_seeTickets)
        var directionsButton = itemView.findViewById<Button>(R.id.rv_fav_directions)
        var unfavoriteButton = itemView.findViewById<ImageView>(R.id.rv_fav_buttonUnfavorite)


        // define a var to store url into
        var thisURL = ""
        var thisID = ""
        var thisAddress = ""

        // Primary constructor
        init {

            // if user is logged in, show and create ref to favorite button
            if (userInstance.currentUser != null) {
                unfavoriteButton.setOnClickListener() {
                    Log.d(TAG, "favorite button clicked: thisID: $thisID")
                    // TODO: REMOVE CORRESPONDING EVENT ID FOR THIS EVENT
                    val db = FirebaseFirestore.getInstance()

                    var favorites: Any?

                    // get db info
                    db.collection("users").document(userInstance.currentUser!!.uid)
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
                                        Log.d(TAG, "key found, removing ${favList.iterator()}")
                                        iterator.remove()
                                        Toast.makeText(itemView.context, "Successfully removed from favorites", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                // Upload new favList to remote db
                                val userDocument = db.collection("users").document(userInstance.currentUser!!.uid)

                                userDocument.update("favorites", favList)
                                    .addOnSuccessListener {
                                        // List successfully uploaded
                                        Log.d(TAG, "Successfully uploaded ${favList} to favorites key in db")
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle the error
                                        Log.d(TAG, "Failed to upload ${favList} to favorites key in db")
                                    }
                            } else {
                                // create new list on db and populate with thisID
                                Log.d(TAG, "favList = null, creating new list and adding ID into it")
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
            R.layout.favorite_row_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        // Create references to data returned from API
        val currentEvent = events[position]
        val eventName = currentEvent.name
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
        // TODO: represent time in 12hr format as opposed to military time
        var ref = dates.start
        var dateAndTime = "Date: ${ref.localDate}"
        if (ref.dateTBD) {
            dateAndTime = "Date: TBD"
        } else if (ref.noSpecificTime) {
            dateAndTime += " @ no particular time"
        } else if (ref.timeTBD) {
            dateAndTime += " @ Time TBD"
        } else {
            dateAndTime += " @ ${ref.localTime}"
        }
        holder.eventDateAndTime.text = dateAndTime
        // TICKET RANGE - show first hit for priceRanges
        if (priceRanges != null) {
            holder.priceRange.isVisible = true
            holder.priceRange.text = "$${priceRanges[0].min}"
            holder.priceRange.text = "$${priceRanges[0].max}"
        }
        // BUTTON FUNCTIONALITY
        holder.thisURL = url
        holder.thisID = id
        holder.thisAddress = address
    }

    override fun getItemCount(): Int {
        val numEvents = events.size
        Log.d(TAG, "recyclerView count is: $numEvents")
        return numEvents
    }


}


 */