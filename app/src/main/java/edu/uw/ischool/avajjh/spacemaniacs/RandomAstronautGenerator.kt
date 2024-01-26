package edu.uw.ischool.avajjh.spacemaniacs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class RandomAstronautGenerator : AppCompatActivity() {
    lateinit var randomAstronaut : Astronaut
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_astronaut_generator)
        val randomizeButton = findViewById<Button>(R.id.randomizeButton)
        val favoritesButton = findViewById<Button>(R.id.favoritesButton)
        favoritesButton.visibility = View.GONE

        favoritesButton.setOnClickListener() {
            Log.i("arrayAstronauts", randomAstronaut.name)
            addAstronautToFavorites(randomAstronaut)
        }

        randomizeButton.setOnClickListener() {
            Log.i("button", "clicked on randomize")
            (application as RepositoryApplication).fetchWrite(fetchCallBack, "astronaut/", "astronaut.json")
            (application as RepositoryApplication).update("astronaut")
        }
    }

    private fun addAstronautToFavorites(randomAstronaut: Astronaut) {
        val path = getExternalFilesDir("SpaceManiacs").toString() + "/" + "favorites.json"
        val file = File(path)
        val favoritesArray: JSONArray

        if (!file.exists()) {
            // create a new file
            file.createNewFile()
            favoritesArray = JSONArray()
        } else {
            val fileContent = file.readText()
            favoritesArray = JSONArray(fileContent)
        }
        // convert Astronaut object to JSONObject and add it to the array
        val astronautJson = JSONObject().apply {
            put("name", randomAstronaut.name)
            put("age", randomAstronaut.age)
            put("nationality", randomAstronaut.nationality)
            put("bio", randomAstronaut.bio)
            put("profileImage", randomAstronaut.profileImage)
            put("flightCount", randomAstronaut.flightCount)
        }
        favoritesArray.put(astronautJson)
        Log.i("arrayAstronauts", favoritesArray.toString())

        // Write updated array back to file
        file.writeText(favoritesArray.toString())

    }

    //excutes the following functions after the fetch write function completes
    val fetchCallBack = object:FetchWriteCallback {
        override fun onUpdateCompleted() {
            Log.i("insideCallback", "hi")
            (application as RepositoryApplication).update("astronaut")
            val astronautArray : Array<Astronaut> = (application as RepositoryApplication).repository.getAstronauts()
            Log.i("Data", astronautArray[0].name)

            runOnUiThread() {
                val favoritesButton = findViewById<Button>(R.id.favoritesButton)
                favoritesButton.visibility = View.VISIBLE
            val astronautName = findViewById<TextView>(R.id.astronautName)
            val astronautAge = findViewById<TextView>(R.id.astronautAge)
            val astronautNationality = findViewById<TextView>(R.id.astronautNationality)
            val astronautFlight = findViewById<TextView>(R.id.astronautFlightCount)
            val astronautBio = findViewById<TextView>(R.id.astronautBio)
            val astronautImage = findViewById<ImageView>(R.id.astronautImage)
            val astronautArrayLength = astronautArray.size - 1 // should be 10?
            randomAstronaut = astronautArray[(0..astronautArrayLength).random()]

            val astronautImageURL = randomAstronaut.profileImage
            astronautName.text = "Name: ${randomAstronaut.name}"
            astronautAge.text = "Age:  ${randomAstronaut.age}"
            astronautNationality.text = "Nationality: ${randomAstronaut.nationality}"
            astronautFlight.text = "Flight Count: ${randomAstronaut.flightCount}"
            astronautBio.text = "Bio: ${randomAstronaut.bio}"
            Picasso.get()
                .load(astronautImageURL)
                .into(astronautImage)

            }

        }
    }



}