package edu.uw.ischool.avajjh.spacemaniacs

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class EventsResult: AppCompatActivity() {
    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var adapter: Adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_layout_2)

        recyclerViewEvent = findViewById(R.id.recyclerViewEvent)


        val eventIndexMap = (application as RepositoryApplication).repository.getEvents().withIndex().associate {
                "${it.value.name} - ${formatDate(it.value.date)}" to it.index
            }

//        Log.i("EventList", eventList.toString())
        adapter = Adapter(this, eventIndexMap.keys.toList()) { clickedItem ->
            Log.i("index", eventIndexMap.get(clickedItem).toString())
            val intent = Intent(this, EventDetail::class.java).apply {
                putExtra("index", eventIndexMap.get(clickedItem))
            }
            startActivity(intent)
        }

        val gridLayoutManager: GridLayoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)

        recyclerViewEvent.layoutManager = gridLayoutManager
        recyclerViewEvent.adapter = adapter
    }

   //formats the date to be more readable
    fun formatDate(str: String): String {

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
        outputFormat.timeZone = TimeZone.getDefault()

        val date: Date = inputFormat.parse(str)
        return outputFormat.format(date)
    }


}