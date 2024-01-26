package edu.uw.ischool.avajjh.spacemaniacs


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class Adapter(ctx: Context, questions: List<String>, private val clickListener: (String) -> Unit) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    private lateinit var questions: List<String>
    private lateinit var inflator: LayoutInflater


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewEvent)

        init {
            itemView.setOnClickListener {
                clickListener(questions[adapterPosition])
            }
        }
    }

    init {
        this.questions = questions;
        this.inflator = LayoutInflater.from(ctx)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View = inflator.inflate(R.layout.events_grid_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = questions[position]
    }
}