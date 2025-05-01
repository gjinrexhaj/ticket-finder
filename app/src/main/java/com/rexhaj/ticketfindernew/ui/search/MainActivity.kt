package com.rexhaj.ticketfindernew.ui.search
//
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Spinner
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//// Specify tag for logcat
//private const val TAG = "MainActivity"
//
//// Declare references to UI components
//private lateinit var recyclerView: RecyclerView
//private var spinnerSelectionIndex = 0
//private var editTextContents = ""
//
//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        Log.d(TAG, "onCreate($savedInstanceState)")
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        // Create reference to recyclerView
//        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//
//        // Create JSpinner entries dynamically
//        val eventList = listOf("Choose an event category",
//            "Music",
//            "Sports",
//            "Theater",
//            "Family",
//            "Arts & Theater",
//            "Concerts",
//            "Comedy",
//            "Dance")
//        val eventAdapter = ArrayAdapter<String>(
//            this, android.R.layout.simple_spinner_dropdown_item, eventList)
//        val eventSpinner = findViewById<Spinner>(R.id.spinner_eventType)
//        eventSpinner.adapter = eventAdapter
//        eventSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                Log.d(TAG, "onItemSelected($p0, $p1, $p2, $p3)")
//                // update spinnerSelectionIndex
//                spinnerSelectionIndex = p2
//            }
//
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//                Log.d(TAG, "onNothingSelected($p0)")
//            }
//        }
//
//        // Create reference to editText
//        val editText = findViewById<EditText>(R.id.editText_city)
//
//        // Attach onclick listener to search button
//        findViewById<Button>(R.id.button_search).setOnClickListener() {
//            var categoryError = false
//            var cityError = false
//            var errorMessage = "Make sure you have..."
//
//            // update editTextContents
//            editTextContents = editText.text.toString()
//
//            Log.d(
//                TAG, "--- SEARCH BUTTON CLICKED ---\n" +
//                    "spinnerSelectionIndex == $spinnerSelectionIndex\n" +
//                    "editTextContents == $editTextContents")
//
//            // check for malformation, if malformed, then show an error dialog
//            // and terminate function
//            if (spinnerSelectionIndex == 0) {
//                Log.w(TAG, "event category not specified!")
//                categoryError = true
//                errorMessage += "\n- Specified an event category"
//            }
//
//            if (editTextContents == "") {
//                Log.w(TAG, "city not specified!")
//                cityError = true
//                errorMessage += "\n- Specified a city"
//            }
//
//            if (categoryError || cityError) {
//                val builder = AlertDialog.Builder(this)
//                builder.setTitle("Error: Malformed Parameter")
//                builder.setMessage(errorMessage)
//
//                val dialog = builder.create()
//                dialog.show()
//
//                return@setOnClickListener
//            }
//
//            // If passes all checks, call search()
//            search(editTextContents, eventList[spinnerSelectionIndex])
//        }
//    }
//
//
//    private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
//    private fun search(city: String, category: String) {
//        Log.d(TAG, "search() has been called | Creating retrofit instance with base url: $BASE_URL")
//
//        // Create retrofit instance with eventService
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL) // Set the base URL for the REST API
//            .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter factory for JSON serialization/deserialization
//            .build() // Build the Retrofit instance
//        val eventAPI = retrofit.create(EventService::class.java)
//
//        // Make an API call - use enqueue to asynchronously call the api
//        // BuildConfig.apiKeySafe is built at runtime using local.properties
//        // local.properties isn't included in version control, so this serves as
//        // a somewhat safe way of storing the API key
//        // reference used: https://www.youtube.com/watch?v=bY0yvrAtbKM
//
//        val sort = "date,asc"
//
//        Log.d(TAG, "Making API call with queries | city: $city, category: $category")
//        eventAPI.getEventInfo(city, category, sort, BuildConfig.apiKeySafe
//        ).enqueue(object : Callback<EventData>{
//            override fun onResponse(p0: Call<EventData>, p1: Response<EventData>) {
//                Log.d(TAG, "onResponse")
//
//                val fullBody = p1.body()
//                if (fullBody == null) {
//                    Log.w(TAG, "Valid response was not received")
//                    return
//                }
//
//                val events = fullBody?._embedded?.events
//
//                if (events != null) {
//                    Log.d(TAG, "Calling showResults()")
//                    findViewById<TextView>(R.id.textView_noResultsFound).text = ""
//                    showResults(events)
//
//                } else {
//                    Log.d(TAG, "events appears to be null")
//                    findViewById<TextView>(R.id.textView_noResultsFound).text = "No results were found for $category in $city"
//                    // reset contents of recyclerView
//                    recyclerView.adapter = null
//                }
//            }
//
//            override fun onFailure(p0: Call<EventData>, p1: Throwable) {
//                Log.d(TAG, "onFailure: $p0, $p1")
//            }
//        })
//
//    }
//
//    private fun showResults(events: List<Event>) {
//        // Populate recyclerView adapter
//        recyclerView.adapter = RecyclerAdapter(events)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//    }
//}