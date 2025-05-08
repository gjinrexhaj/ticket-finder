package com.rexhaj.ticketfindernew.ui.search

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rexhaj.ticketfindernew.BuildConfig
import com.rexhaj.ticketfindernew.Event
import com.rexhaj.ticketfindernew.EventData
import com.rexhaj.ticketfindernew.RecyclerAdapter
import com.rexhaj.ticketfindernew.databinding.FragmentSearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private lateinit var recyclerView: RecyclerView
private const val TAG = "SearchFragment"
private var spinnerSelectionIndex = 0
private var editTextContents = ""
private var BundleRecyclerViewState: Bundle? = null


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textSearch
//        searchViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        // TO ACCESS STUFF, DO _binding.

        // Create reference to recyclerView
        recyclerView = _binding!!.recyclerView

        // Create JSpinner entries dynamically
        val eventList = listOf("Choose an event category",
            "Music",
            "Sports",
            "Theater",
            "Family",
            "Arts & Theater",
            "Concerts",
            "Comedy",
            "Dance")
        val eventAdapter = ArrayAdapter<String>(
            requireContext().applicationContext, android.R.layout.simple_spinner_dropdown_item, eventList)
        val eventSpinner = _binding!!.spinnerEventType
        eventSpinner.adapter = eventAdapter
        eventSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.d(TAG, "onItemSelected($p0, $p1, $p2, $p3)")
                // update spinnerSelectionIndex
                spinnerSelectionIndex = p2
                Log.d(TAG, "spinnerSelectionIndex = $spinnerSelectionIndex")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.d(TAG, "onNothingSelected($p0)")
                Log.d(TAG, "spinnerSelectionIndex = $spinnerSelectionIndex")
            }
        }

        // Create reference to editText
        val editText = _binding!!.editTextCity

        // Attach onclick listener to search button
        _binding!!.buttonSearch.setOnClickListener() {
            var categoryError = false
            var cityError = false
            var errorMessage = "Make sure you have..."

            // update editTextContents
            editTextContents = editText.text.toString()

            Log.d(
                TAG, "--- SEARCH BUTTON CLICKED ---\n" +
                    "spinnerSelectionIndex == $spinnerSelectionIndex\n" +
                    "editTextContents == $editTextContents")

            // check for malformation, if malformed, then show an error dialog
            // and terminate function
            if (spinnerSelectionIndex == 0) {
                Log.w(TAG, "event category not specified!")
                categoryError = true
                errorMessage += "\n- Specified an event category"
            }

            if (editTextContents == "") {
                Log.w(TAG, "city not specified!")
                cityError = true
                errorMessage += "\n- Specified a city"
            }

            if (categoryError || cityError) {
                val builder = AlertDialog.Builder(this.context)
                builder.setTitle("Error: Malformed Parameter")
                builder.setMessage(errorMessage)

                val dialog = builder.create()
                dialog.show()

                return@setOnClickListener
            }

            // If passes all checks, call search()
            search(editTextContents, eventList[spinnerSelectionIndex])
        }



        return root
    }

    // Search function
    private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
    private fun search(city: String, category: String) {
        Log.d(TAG, "search() has been called | Creating retrofit instance with base url: $BASE_URL")

        // Create retrofit instance with eventService
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL) // Set the base URL for the REST API
            .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter factory for JSON serialization/deserialization
            .build() // Build the Retrofit instance
        val eventAPI = retrofit.create(com.rexhaj.ticketfindernew.EventService::class.java)

        // Make an API call - use enqueue to asynchronously call the api
        // BuildConfig.apiKeySafe is built at runtime using local.properties
        // local.properties isn't included in version control, so this serves as
        // a somewhat safe way of storing the API key
        // reference used: https://www.youtube.com/watch?v=bY0yvrAtbKM

        val sort = "date,asc"

        Log.d(TAG, "Making API call with queries | city: $city, category: $category")
        eventAPI.getEventInfo(city, category, sort, BuildConfig.apiKeySafe
        ).enqueue(object : Callback<EventData>{
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onResponse(p0: Call<EventData>, p1: Response<EventData>) {
                Log.d(TAG, "onResponse")

                val fullBody = p1.body()
                if (fullBody == null) {
                    Log.w(TAG, "Valid response was not received")
                    return
                }

                val events = fullBody?._embedded?.events

                if (events != null) {
                    Log.d(TAG, "Calling showResults()")
                    _binding!!.textViewNoResultsFound.text = ""
                    showResults(events)

                } else {
                    Log.d(TAG, "events appears to be null")
                    _binding!!.textViewNoResultsFound.text = "No results were found for $category in $city"
                    // reset contents of recyclerView
                    recyclerView.adapter = null
                }
            }

            override fun onFailure(p0: Call<EventData>, p1: Throwable) {
                Log.d(TAG, "onFailure: $p0, $p1")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showResults(events: List<Event>) {
        // Populate recyclerView adapter
        recyclerView.adapter = RecyclerAdapter(events)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}