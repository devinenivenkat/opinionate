package com.example.letmeknow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomePageRecyclerAdapter(val context: Context,val arrPosts: ArrayList<HomePagePollCardDataModel>) : RecyclerView.Adapter<HomePageRecyclerAdapter.ViewHolder>() {

    private lateinit var mlistener: onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mlistener = listener
    }

    class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val QuestionTitle = itemView.findViewById<TextView>(R.id.QuestionTitle)
        val QuestionAuthor = itemView.findViewById<TextView>(R.id.QuestionAuthor)
        val TimeLeft = itemView.findViewById<TextView>(R.id.timer)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.home_page_poll_card, parent, false)
        return ViewHolder(itemView, mlistener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.QuestionTitle.text = arrPosts[position].QuestionTitle
        holder.QuestionAuthor.text = arrPosts[position].QuestionAuthor
        holder.TimeLeft.text = arrPosts[position].TimeLeft

    }

    override fun getItemCount(): Int {
        return arrPosts.size
    }

}