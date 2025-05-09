package com.rexhaj.ticketfindernew.ui.favorites

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rexhaj.ticketfindernew.BuildConfig
import com.rexhaj.ticketfindernew.EventData
import com.rexhaj.ticketfindernew.EventList
import com.rexhaj.ticketfindernew.EventService
import com.rexhaj.ticketfindernew.IDService
import com.rexhaj.ticketfindernew.databinding.FragmentFavoritesBinding
import com.rexhaj.ticketfindernew.ui.search.user
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FavoritesFragment"
private lateinit var favRecyclerView: RecyclerView
private lateinit var favoritesEventList: EventList

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val favoritesViewModel =
            ViewModelProvider(this).get(FavoritesViewModel::class.java)

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val userInstance = FirebaseAuth.getInstance().currentUser
        // Show recyclerView if logged in
        if (userInstance != null) {
            binding.textViewPleaseSignIn.visibility = INVISIBLE
            binding.recyclerViewFavorites.visibility = VISIBLE

            // create reference to recyclerView
            favRecyclerView = _binding!!.recyclerViewFavorites

            // Get favList from db
            val db = FirebaseFirestore.getInstance()
            var favorites: Any?
            var favList: MutableList<String>? = null

            db.collection("users").document(user.currentUser!!.uid)
                .get()
                .addOnSuccessListener { documents ->

                    // get favorites
                    favorites = documents.get("favorites")
                    Log.d(TAG, "document.get(\"favorites\") = $favorites")

                    favList = favorites as MutableList<String>
                    Log.d(TAG, "favList = ${favList}")

                    searchById(favList!!)
                }
        }

        // TODO: CALL favShowResults WHEN UNFAVORITE IS CLICKED TO UPDATE THE PAGE
        return root
    }


    private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
    // Search functions, replace EventService interface with new IDService interface

    @RequiresApi(Build.VERSION_CODES.R)
    private fun searchById(idList: MutableList<String>) {
        Log.d(TAG, "searchById($idList)")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL) // Set the base URL for the REST API
            .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter factory for JSON serialization/deserialization
            .build() // Build the Retrofit instance
        val idEventAPI = retrofit.create(IDService::class.java)

        Log.d(TAG, "Making API call with queries | id: $id")

        // ITERATE THROUGH ENTIRE LIST, MAKE API CALL FOR EACH AND BIND TO RECYCLERVIEW
        // Initialize favoritesEventList
        // favoritesEventList = EventData(EventList())
        favoritesEventList = EventList()

        val iterator = idList.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            makeApiCall(idEventAPI, element)
        }

        Log.d(TAG, "list popluation finished")
        //favShowResults(favoritesEventList)
    }

    private fun makeApiCall(api: IDService, element: String) {
        api.getEventInfo(element, BuildConfig.apiKeySafe
        ).enqueue(object : Callback<EventData> {
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onResponse(p0: Call<EventData>, p1: Response<EventData>) {
                Log.d(TAG, "onResponse | p1: $p1")

                val idFullBody = p1.body()

                if (idFullBody == null) {
                    Log.w(TAG, "Valid response was not received")
                    return
                }

                val event = idFullBody?._embedded?.events?.get(0)
                Log.d(TAG, "make call to show events | event: $event")
                if (event != null) {
                    favoritesEventList.events.add(event)
                } else {
                    Log.d(TAG, "EVENT NULL")
                }
                Log.d(TAG, "added event to favoritesEventList")
                Log.d(TAG, "favoritesEventList: $favoritesEventList")
                favShowResults(favoritesEventList)
            }

            override fun onFailure(p0: Call<EventData>, p1: Throwable) {
                Log.d(TAG, "onFailure: $p0, $p1")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.R)
     private fun favShowResults(events: EventList) {
        Log.d(TAG, "calling favShowResults with data: $favoritesEventList")
        // Populate recyclerView adapter
        favRecyclerView.adapter = FavoriteRecyclerAdapter(events.events)
        Log.d(TAG, "populating recyclerView with ${events.events}")
        favRecyclerView.layoutManager = LinearLayoutManager(this.context)
     }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}