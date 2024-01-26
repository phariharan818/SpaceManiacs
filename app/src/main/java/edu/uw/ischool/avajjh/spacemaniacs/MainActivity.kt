package edu.uw.ischool.avajjh.spacemaniacs

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    lateinit var cardViewLaunches: CardView
    lateinit var cardViewAstronauts: CardView
    lateinit var cardViewEvents: CardView
    lateinit var cardViewFavorites: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardViewLaunches = findViewById(R.id.cardViewLaunches)
        cardViewAstronauts = findViewById(R.id.cardViewAstronauts)
        cardViewEvents = findViewById(R.id.cardViewEvents)
        cardViewFavorites = findViewById(R.id.cardViewFavorites)

        cardViewLaunches.setOnClickListener {
            val intent = Intent(this, LaunchesPage::class.java)
            startActivity(intent)
            Log.i("Main", "clicked launches")
        }

        cardViewAstronauts.setOnClickListener {
            val intent = Intent(this, RandomAstronautGenerator::class.java)
            startActivity(intent)
            Log.i("Main", "clicked astronauts")
        }

        cardViewEvents.setOnClickListener {
            val intent = Intent(this, EventsLandingPage::class.java)
            startActivity(intent)
            Log.i("Main", "clicked events")
        }

        cardViewFavorites.setOnClickListener {
            val intent = Intent(this, Favorites::class.java)
            startActivity(intent)
            Log.i("Main", "clicked favorites")
        }

    }
}