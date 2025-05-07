package com.rexhaj.ticketfindernew

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.sql.Time
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone


private const val TAG = "RecyclerAdapter"


class RecyclerAdapter(private val events: List<Event>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


    // Inner class
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Provides reference to views present in a row item

        val eventImage = itemView.findViewById<ImageView>(R.id.rv_imageView_eventImage)
        val eventName = itemView.findViewById<TextView>(R.id.rv_textView_eventName)
        val venueNameAndCity = itemView.findViewById<TextView>(R.id.rv_textView_venueNameAndCity)
        val venueAddress = itemView.findViewById<TextView>(R.id.rv_textView_venueAddress)
        val eventDateAndTime = itemView.findViewById<TextView>(R.id.rv_textView_eventDateAndTime)
        val priceRange = itemView.findViewById<TextView>(R.id.rv_textView_priceRange)
        val seeTickets = itemView.findViewById<Button>(R.id.rv_button_seeTickets)

        // define a var to store url into
        var thisURL = ""

        // Primary constructor
        init {

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
            R.layout.row_item, parent, false)
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
        // SEE TICKETS LINK BUTTON
        holder.thisURL = url
    }


}