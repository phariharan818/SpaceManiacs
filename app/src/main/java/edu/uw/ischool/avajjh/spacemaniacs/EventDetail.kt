package edu.uw.ischool.avajjh.spacemaniacs

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class EventDetail: AppCompatActivity() {
    lateinit var imageViewEvent: ImageView
    lateinit var textViewName: TextView
    lateinit var textViewDetails: TextView
    lateinit var textViewDate: TextView
    lateinit var textViewDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_detail)

        linkUI()
        val eventData: Event = (application as RepositoryApplication).repository.getEvents()[intent.getIntExtra("index", 0)]
        Picasso.get()
            .load(eventData.featureImage)
            .into(imageViewEvent)

        textViewName.setText(eventData.name)
        textViewDetails.setText("${eventData.type} @ ${eventData.location}")
        textViewDate.setText(formatDate(eventData.date))
        textViewDescription.setText(eventData.description)

    }

    fun linkUI() {
        imageViewEvent = findViewById(R.id.imageViewEvent)
        textViewName = findViewById(R.id.textViewEventName)
        textViewDetails = findViewById(R.id.textViewDetails)
        textViewDate = findViewById(R.id.textViewDate)
        textViewDescription = findViewById(R.id.textViewDescription)
    }

    fun formatDate(str: String): String {

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
        outputFormat.timeZone = TimeZone.getDefault()

        val date: Date = inputFormat.parse(str)
        return outputFormat.format(date)
    }
}