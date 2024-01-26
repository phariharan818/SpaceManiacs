package edu.uw.ischool.avajjh.spacemaniacs

import android.app.IntentService
import android.content.Intent
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class FetchWrite(private val callback: FetchWriteCallback) : IntentService("FetchWrite") {

    override fun onHandleIntent(intent: Intent?) {
        val mainActivity = this
        val executor: Executor = Executors.newSingleThreadExecutor()
        Log.i("Download", "Before executor is running")

        executor.execute {
            val endpoint = "https://lldev.thespacedevs.com/2.2.0/" + intent?.getStringExtra("params")
            Log.i("Download", "$endpoint")
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            Log.i("Download", "inside executor")
            Log.i("Download", connection.responseCode.toString())
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.getInputStream()
                val reader = InputStreamReader(inputStream)
                val fileName = intent?.getStringExtra("fileName") + ".json"

                Log.i("Download", "Downloading and writing JSON")
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
}