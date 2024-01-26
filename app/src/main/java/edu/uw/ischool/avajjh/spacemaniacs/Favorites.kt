package edu.uw.ischool.avajjh.spacemaniacs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader

class Favorites : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val favorites = readFavoritesData()
        setupListView(favorites)
    }

    private fun setupListView(astronauts: List<Astronaut>) {
        val listView = findViewById<ListView>(R.id.listViewFavs)
        val adapter = ArrayAdapter(this, R.layout.list_item_astronaut, R.id.textView, astronauts.map { it.name })
        listView.adapter = adapter
    }
    private fun readFavoritesData() : List<Astronaut> {
        val path = getExternalFilesDir("SpaceManiacs").toString() + "/favorites.json"
        val file = File(path)
        if (!file.exists()) {
            return emptyList()
        }
        val fileContent = file.readText()
        val jsonArray = JSONArray(fileContent)
        val astronauts = mutableListOf<Astronaut>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val astronaut = Astronaut(
                jsonObject.getString("name"),
                jsonObject.getString("age"),
                jsonObject.getString("nationality"),
                jsonObject.getString("bio"),
                jsonObject.getString("profileImage"),
                jsonObject.getString("flightCount")
            )
            astronauts.add(astronaut)
        }
        Log.i("astro", astronauts.toString())

        return astronauts
    }

}