package edu.uw.ischool.avajjh.spacemaniacs

import android.app.Application
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface FetchWriteCallback {
    fun onUpdateCompleted()
}
class RepositoryApplication : Application() {
    lateinit var repository: DataRepository

    override fun onCreate() {
        super.onCreate()
        repository = DataRepository()
        Log.i("RepositoryApplication", "Created!")
    }

    //Fetches from the API using fetchParams and writes to fileName in the external directory
    fun fetchWrite(callback: FetchWriteCallback, fetchParams: String, fileName: String) {
        val mainActivity = this
        val executor: Executor = Executors.newSingleThreadExecutor()
        Log.i("Download", "Before executor is running")

        executor.execute {
            val endpoint = "https://lldev.thespacedevs.com/2.2.0/" + fetchParams
            Log.i("Download", endpoint)
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            Log.i("Download", "inside executor")
            Log.i("Download", "Downloading and writing JSON")

            Log.i("Download", connection.responseCode.toString())
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.getInputStream()
                val reader = InputStreamReader(inputStream)
                val folder = getExternalFilesDir("SpaceManiacs")
                val file = File(folder, fileName)
                val outputStream = FileOutputStream(file)
                outputStream.write(reader.readText().toByteArray())
                reader.close()
                outputStream.close()
            }
            callback.onUpdateCompleted()
        }
    }

    //updates the data stored in the repository, reading from fileName in the external directory
    fun update(fileName: String) {
        val dir = getExternalFilesDir("SpaceManiacs").toString() + "/" + fileName + ".json"
        Log.i("FileReader", "attempting to read")
        try {
            val reader = FileReader(dir)
            val responseObject = JSONObject(reader.readText())
            reader.close()
            val responseArray = responseObject.getJSONArray("results")
            Log.i("length", responseArray.length().toString())
            if (fileName == "events") {
                val resultArray: Array<Event> = parseEvents(responseArray)
                repository.updateEvents(resultArray)
            } else if (fileName == "astronaut") {
                val resultArray: Array<Astronaut> = parseAstronauts(responseArray)
                repository.updateAstronauts(resultArray)
            } else if (fileName == "launches") {
                val resultArray: Array<Launch> = parseLaunches(responseArray)
                repository.updateLaunches(resultArray)
            } else {
                Log.e("update", "Invalid Filename")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Helper function that parses the events.json string and returns an Array of Event
    fun parseEvents(responseArray: org.json.JSONArray): Array<Event> {
        Log.i("parseEvents", "parsing")
        var resultArray: Array<Event> = Array(responseArray.length()) {
            val eventsObject = responseArray.getJSONObject(it)
            val typeObject = eventsObject.getJSONObject("type")
            val name = eventsObject.getString("name")
            val description = eventsObject.getString("description")
            val location = eventsObject.getString("location")
            val featureImage =eventsObject.getString("feature_image")
            val date = eventsObject.getString("date")
            val type = typeObject.getString("name")
            Event(name, description, location, featureImage, date, type)
        }
        return resultArray
    }
    fun parseAstronauts(responseArray: org.json.JSONArray): Array<Astronaut> {
        Log.i("parseAstronauts", "parsing")
        var resultArray: Array<Astronaut> = Array(responseArray.length()) {
            val astronautsObject = responseArray.getJSONObject(it)
            val name = astronautsObject.getString("name")
            val age = astronautsObject.getString("age")
            val nationality = astronautsObject.getString("nationality")
            val bio = astronautsObject.getString("bio")
            val profileImage = astronautsObject.getString("profile_image")
            val flightCount = astronautsObject.getString("flights_count")
            Astronaut(name, age, nationality, bio, profileImage, flightCount)
        }
        return resultArray
    }

    fun parseLaunches(responseArray: org.json.JSONArray) : Array<Launch> {
        Log.i("parsingLaunches", "parsing")
        var resultArray: Array<Launch> = Array(responseArray.length()) {
            val launchObjects = responseArray.getJSONObject(it)
            val missionObject = launchObjects.getJSONObject("mission")
            val name = launchObjects.getString("name")
            val windowStart = launchObjects.getString("window_start")
            val windowEnd = launchObjects.getString("window_end")
            val description = missionObject.getString("description")
            val image = launchObjects.getString("image")
            Launch(name, windowStart, windowEnd, description, image)
        }
        return resultArray
    }
}