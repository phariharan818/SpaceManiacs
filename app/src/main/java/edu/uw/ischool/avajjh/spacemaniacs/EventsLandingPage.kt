package edu.uw.ischool.avajjh.spacemaniacs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast


class EventsLandingPage : AppCompatActivity() {
    lateinit var dropDownSpaceAgency: Spinner
    lateinit var dropDownYear: Spinner
    lateinit var dropDownEventType: Spinner
    lateinit var buttonSearch: Button
    val agencyMap: MutableMap<String, String> = mutableMapOf<String, String>()
    val eventMap: MutableMap<String, String> = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.events_layout_1)
        fillAgencyMap()
        fillEventMap()
        linkUIElements()
        fillAgencyDropdown()
        fillYearDropdown()
        fillEventDropdown()

        //generates results page
        buttonSearch.setOnClickListener {
            Log.i("button", "clicked on search")
            var paramsString = "event/?" + createParamsString()
            Log.i("params", paramsString)
            (application as RepositoryApplication).fetchWrite(fetchCallBack, paramsString, "events.json")
        }
    }

    //excutes the following functions after the fetch write function completes
    val fetchCallBack = object:FetchWriteCallback {
        override fun onUpdateCompleted() {
            (application as RepositoryApplication).update("events")
            val eventArray: Array<Event> = (application as RepositoryApplication).repository.getEvents()
            if (eventArray.isEmpty()) {
                runOnUiThread() {
                    Toast.makeText(
                        this@EventsLandingPage,
                        "Filter returned no results",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val intent = Intent(this@EventsLandingPage, EventsResult::class.java)
                startActivity(intent)
            }
        }
    }

    //Maps out space agencies to the corresponding agency id used for the fetch request
    fun fillAgencyMap() {
        agencyMap.put("SpaceX", "121")
        agencyMap.put("European Space Agency", "27")
        agencyMap.put("NASA", "44")
        agencyMap.put("Canadian Space Agency", "16")
        agencyMap.put("Japan Aerospace Exploration Agency", "37")
        agencyMap.put("ROSCOSMOS", "63")
    }

    //Maps out event types to the corresponding event type id used for the fetch request
    fun fillEventMap() {
        eventMap.put("Spacecraft Undocking", "8")
        eventMap.put("Docking", "2")
        eventMap.put("Press Event", "20")
        eventMap.put("Spacecraft Capture", "29")
        eventMap.put("Spacecraft Berthing", "4")
        eventMap.put("Sounding Rocket Launch", "31")
    }

    //Links UI elements to vars
    fun linkUIElements() {
        dropDownSpaceAgency = findViewById(R.id.spinnerSpaceAgency)
        dropDownYear = findViewById(R.id.spinnerYear)
        dropDownEventType = findViewById(R.id.spinnerEventType)
        buttonSearch = findViewById(R.id.buttonSearch)
    }

    //Fills the agency dropdown using the agency map
    fun fillAgencyDropdown() {
        val agencyKeys = listOf("") + agencyMap.keys.toList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, agencyKeys)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropDownSpaceAgency.adapter = adapter
    }

    //Fills the years dropdown using years 1980 to 2023
    fun fillYearDropdown() {
        val years = listOf("") + (1980..2023).map { it.toString() }

        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropDownYear.adapter = yearAdapter
    }

    //Fills the events dropdown using the events map
    fun fillEventDropdown() {
        val eventKeys = listOf("") + eventMap.keys.toList()

        val eventAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, eventKeys)
        eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropDownEventType.adapter = eventAdapter
    }

    //Constructs a param string for fetching using the data from the drop downs
    fun createParamsString(): String {
        val selectedSpaceAgency = dropDownSpaceAgency.selectedItem.toString()
        val selectedYear = dropDownYear.selectedItem.toString()
        val selectedEventType = dropDownEventType.selectedItem.toString()

        // Constructing params excluding blank values
        val paramsList = mutableListOf<String>()

        if (selectedSpaceAgency.isNotBlank()) {
            paramsList.add("agency__ids=${agencyMap.get(selectedSpaceAgency)}")
        }

        if (selectedEventType.isNotBlank()) {
            paramsList.add("type=${eventMap.get(selectedEventType)}")
        }

        if (selectedYear.isNotBlank()) {
            paramsList.add("year=$selectedYear")
        }
        Log.i("params", paramsList.joinToString("&"))
        // Constructing the final params string
        return paramsList.joinToString("&")
    }
}