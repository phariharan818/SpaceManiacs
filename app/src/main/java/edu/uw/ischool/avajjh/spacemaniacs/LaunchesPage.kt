package edu.uw.ischool.avajjh.spacemaniacs

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

class LaunchesPage : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshButton: Button
    private lateinit var launchAdapter: SimpleLaunchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launches_page)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        refreshButton = findViewById(R.id.refresh)

        launchAdapter = SimpleLaunchAdapter(mutableListOf(), recyclerView)
        recyclerView.adapter = launchAdapter


        refreshButton.setOnClickListener {
            Log.i("button", "clicked on refresh")
            val paramsString = "launch/upcoming/"
            (application as RepositoryApplication).fetchWrite(fetchCallBack, paramsString, "launches.json")
        }

    }

    val fetchCallBack = object : FetchWriteCallback {
        override fun onUpdateCompleted() {
            (application as RepositoryApplication).update("launches")
            val launchArray: Array<Launch> = (application as RepositoryApplication).repository.getLaunches()
            launchAdapter.updateData(launchArray.toList())
        }
    }


    fun removeItem(position: Int) {
        launchAdapter.removeLaunch(position)
    }
}

class SimpleLaunchAdapter(private var launchList: MutableList<Launch>, private val recyclerView: RecyclerView) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val CHANNEL_ID = "UpcomingLaunchChannel"
        private const val NOTIFICATION_ID = 1
    }

    fun removeLaunch(position: Int) {
        if (position in 0 until launchList.size) {
            (recyclerView.findViewHolderForAdapterPosition(position) as? HeaderViewHolder)?.cancelCountdown()
            launchList.removeAt(position)
            notifyItemRemoved(position)
        }
    }


    fun updateData(newLaunchList: List<Launch>) {
        val currentTimeMillis = System.currentTimeMillis()
        val filteredList = newLaunchList.filter { launch ->
            val launchTimeMillis = calculateTimeMillisFromLaunch(launch)
            launchTimeMillis > currentTimeMillis
        }

        val sortedList = filteredList.sortedBy { it.windowStart }
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            if (launchList.isEmpty()) {
                launchList.addAll(sortedList)
                notifyDataSetChanged()
            } else {
                val existingIds = launchList.map { it.name }
                val newItems = sortedList.filterNot { it.name in existingIds }
                launchList.addAll(newItems)
                notifyItemRangeInserted(itemCount - newItems.size, newItems.size)
            }

            sortedList.forEach {
                Log.d("LaunchSorting", "${it.name}: ${it.windowStart}")
            }
        }
    }

    private fun calculateTimeMillisFromLaunch(launch: Launch): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        try {
            val startDate = dateFormat.parse(launch.windowStart)

            if (startDate != null) {
                return startDate.time
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }




    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.header_layout,
                    parent,
                    false
                )
            )
        } else {
            ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_layout,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val launch = launchList[position - 1]
            holder.titleTextView.text = launch.name
            holder.descriptionTextView.text = launch.description
        } else if (holder is HeaderViewHolder && position == 0) {
            val upcomingLaunch = launchList.firstOrNull()
            upcomingLaunch?.let {
                holder.headerTitleTextView.text = it.name
                holder.headerWindowStartTextView.text = it.windowStart
                holder.bindImage(it.image)
                holder.bindCountdown(it.windowStart, it.windowEnd, holder.adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return launchList.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTitleTextView: TextView = itemView.findViewById(R.id.upcomingLaunchName)
        val headerWindowStartTextView: TextView = itemView.findViewById(R.id.upcomingLaunchCountdown)
        val upcomingImage: ImageView = itemView.findViewById(R.id.upcomingLaunchImage)
        val notifyButton: Button = itemView.findViewById(R.id.notifyButton)
        private var countDownTimer: CountDownTimer? = null
        private var initialTimeDifference: Long = 0

        init {
            notifyButton.setOnClickListener {
                notifyUser(itemView.context, initialTimeDifference)
            }
        }

        private fun notifyUser(context: Context, initialTimeDifference: Long) {
            createNotificationChannel(context)

            val timeUntilLaunch = formatMillisToTime(initialTimeDifference)

            val intent = Intent(context, LaunchesPage::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Upcoming Launch")
                .setContentText("The launch is happening at $timeUntilLaunch UTC!")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Upcoming Launch Channel"
                val descriptionText = "Channel for upcoming launch notifications"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun bindImage(url: String) {
            Picasso.get().load(url).into(upcomingImage)
        }

        fun bindCountdown(windowStart: String, windowEnd: String, adapterPosition: Int) {
            if (initialTimeDifference == 0L) {
                initialTimeDifference = calculateTimeDifference(windowStart, windowEnd)
            }

            val countdownTextView: TextView = itemView.findViewById(R.id.upcomingLaunchCountdown)
            val countdownLabel: TextView = itemView.findViewById(R.id.upcomingLaunchCountdownLabel)

            countdownLabel.text = "Countdown to Launch:"

            countDownTimer = object : CountDownTimer(initialTimeDifference, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val pstTimeZone = TimeZone.getTimeZone("America/Los_Angeles")
                    val pstCalendar = Calendar.getInstance(pstTimeZone)
                    pstCalendar.timeInMillis = System.currentTimeMillis() + millisUntilFinished

                    val currentTime = Calendar.getInstance()
                    val remainingTime = pstCalendar.timeInMillis - currentTime.timeInMillis

                    val hours = remainingTime / (1000 * 60 * 60)
                    val minutes = (remainingTime % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (remainingTime % (1000 * 60)) / 1000

                    val countdownText = String.format(
                        "%02d:%02d:%02d",
                        hours,
                        minutes,
                        seconds
                    )

                    countdownTextView.text = countdownText
                }

                override fun onFinish() {
                    Log.d("Countdown", "Countdown finished!")

                    Handler(itemView.context.mainLooper).post {
                        (itemView.context as? LaunchesPage)?.removeItem(adapterPosition)
                    }
                }
            }
            countDownTimer?.start()
        }


        fun calculateTimeDifference(windowStart: String, windowEnd: String): Long {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            try {
                val startDate = dateFormat.parse(windowStart)
                val endDate = dateFormat.parse(windowEnd)

                if (startDate != null && endDate != null) {
                    return endDate.time - startDate.time
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return 0
        }


        private fun formatMillisToTime(millis: Long): String {
            val hours = millis / (1000 * 60 * 60)
            val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
            val seconds = (millis % (1000 * 60)) / 1000
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        fun cancelCountdown() {
            countDownTimer?.cancel()
        }
    }
}

