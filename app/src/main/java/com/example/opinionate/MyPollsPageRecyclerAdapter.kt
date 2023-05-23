package com.example.letmeknow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class MyPollsPageRecyclerAdapter(val context: Context, val arrPolls: ArrayList<MyPollsPagePollCardDataModel>): RecyclerView.Adapter<MyPollsPageRecyclerAdapter.ViewHolder>(){
    private lateinit var mlistener: HomePageRecyclerAdapter.onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: HomePageRecyclerAdapter.onItemClickListener){
        mlistener = listener
    }

    class ViewHolder(itemView: View, listener: HomePageRecyclerAdapter.onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val QuestionTitle = itemView.findViewById<TextView>(R.id.QuestionTitle)
        val QuestionAuthor = itemView.findViewById<TextView>(R.id.QuestionAuthor)
        val TimeLeft = itemView.findViewById<TextView>(R.id.timer)
        val RemoveButton = itemView.findViewById<Button>(R.id.removeButton)
        val EditButton = itemView.findViewById<Button>(R.id.editButton)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.my_polls_page_poll_card, parent, false)
        return ViewHolder(itemView, mlistener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.QuestionTitle.text = arrPolls[position].QuestionTitle
        holder.QuestionAuthor.text = arrPolls[position].QuestionAuthor
        holder.TimeLeft.text = arrPolls[position].TimeLeft
        holder.RemoveButton.setOnClickListener {
            val rootNode = FirebaseDatabase.getInstance("https://let-me-know-cb14b-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val desiredQuestion = rootNode.getReference("polls/${holder.QuestionTitle.text}")
            desiredQuestion.get().addOnSuccessListener{
                if(it.exists()){
                    desiredQuestion.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener{
                            Toast.makeText(context, "Failure to remove", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener{
                Toast.makeText(context, "Couldn't locate the poll in the database", Toast.LENGTH_SHORT).show()
            }
        }
        holder.EditButton.setOnClickListener {
            Toast.makeText(context, "Still working on this feature...", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "If the app crashes on clicking submit", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "Kindly delete the poll", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, EditPoll::class.java)
            intent.putExtra("Title", arrPolls[position].QuestionTitle)
            startActivity(context, intent, Bundle())
        }
    }

    override fun getItemCount(): Int {
        return arrPolls.size
    }
}