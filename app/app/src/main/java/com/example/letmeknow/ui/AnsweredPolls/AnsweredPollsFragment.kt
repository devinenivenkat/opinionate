package com.example.letmeknow.ui.AnsweredPolls

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letmeknow.HomePagePollCardDataModel
import com.example.letmeknow.HomePageRecyclerAdapter
import com.example.letmeknow.R
import com.example.letmeknow.AnsweringPolls
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class AnsweredPollsFragment : Fragment() {
    val arrPolls: ArrayList<HomePagePollCardDataModel> = ArrayList<HomePagePollCardDataModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun collectData(users: Map<String, Any>) {

        //iterate through each user, ignoring their UID
        for ((_, value) in users) {
            var currentUser: String = FirebaseAuth.getInstance().currentUser!!.email!!
            currentUser = currentUser.replace("@", "at")
            currentUser = currentUser.replace(".", "dot")

            //Get user map
            val singleUser = value as Map<*, *>
            for ((key, value) in singleUser){
                val Title: String = singleUser["questionTitle"].toString()
                val Author: String = singleUser["userEmail"].toString()
                val timer = singleUser["timer"] as String
                val thatTime = singleUser["currentTime"] as String
                var bool = false
                if (key == "answers"){
                    val userAny = value
                    val userMap  = userAny as Map<*, *>
                    for((key, _) in userMap){
                        if (key.toString() == currentUser){
                            bool = true
                        }
                    }
                }
                if(bool==true){
                    // Calculating Time Difference In Minutes
                    val diffinMinutes = diffInMinutes(thatTime)

                    // Calculating Timer Time In Minutes
                    val timerInMinutes = timeCalculatorInMinutes(timer)

                    if(timerInMinutes>diffinMinutes){
                        val timeLeftInMinutes = (timerInMinutes - diffinMinutes)
                        val numberOfDays = timeLeftInMinutes/1440
                        val timeLeftInMinutesAfterDays = timeLeftInMinutes-(numberOfDays*1440)
                        val numberOfHours = (timeLeftInMinutesAfterDays)/60
                        val timeLeftInMinutesAfterHours = timeLeftInMinutesAfterDays-numberOfHours*60
                        val numberOfMinutes = timeLeftInMinutesAfterHours
                        val TimeLeft = "${numberOfDays}d${numberOfHours}h${numberOfMinutes}m"
                        arrPolls.add(HomePagePollCardDataModel(Title, "Author: $Author", "Time Left: $TimeLeft"))
                    }else{
                        val TimeLeft = "Expired"
                        arrPolls.add(HomePagePollCardDataModel(Title, "Author: $Author", TimeLeft))
                    }
                }
            }
        }
    }

    private fun timeCalculatorInMinutes(timer: String): Int {
        val dayInt = "${timer[0]}${timer[1]}".toInt()
        val hoursInt = "${timer[3]}${timer[4]}".toInt()
        val minutesInt = "${timer[6]}${timer[7]}".toInt()
        val totalTimerInMinutes = dayInt*1440 + hoursInt*60 + minutesInt
        return totalTimerInMinutes
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun diffInMinutes(thatTime: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        val date1String = thatTime
        val date1 = dateFormat.parse(date1String)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val date2String = LocalDateTime.now().format(formatter)
        val date2 = dateFormat.parse(date2String)
        val diff: Long = date2.time - date1.time
        return diff.toInt() / 60000
    }

    private var isOnPage = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isOnPage = true
    }

    override fun onDetach() {
        super.onDetach()
        isOnPage = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isOnPage = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isOnPage = false
    }

    override fun onPause() {
        super.onPause()
        isOnPage = false
    }

    override fun onStop() {
        super.onStop()
        isOnPage = false
    }

    override fun onResume() {
        super.onResume()
        isOnPage = true
    }

    override fun onStart() {
        super.onStart()
        isOnPage = true
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arrPolls.clear()
        val rootNode = FirebaseDatabase.getInstance("https://let-me-know-cb14b-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val pollNode = rootNode.reference.child("polls")
        val view = inflater.inflate(R.layout.fragment_answeredpolls, container, false)
        pollNode.addValueEventListener(
            object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    arrPolls.clear()
                    //Get map of users in dataSnapshot
                    if(isOnPage) {
                        val valu = dataSnapshot.value
                        collectData(valu as Map<String, Any>)
                    }

                    val recyclerView: RecyclerView = view.findViewById(R.id.homePageRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(context)

                    val contex = context
                    if (contex != null) {
                        val recyclerAdapter= HomePageRecyclerAdapter(contex, arrPolls)
                        recyclerView.adapter = recyclerAdapter
                        recyclerAdapter.setOnItemClickListener(object: HomePageRecyclerAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                if (arrPolls[position].TimeLeft != "Expired"){
                                    val intent = Intent(context, AnsweringPolls::class.java)
                                    intent.putExtra("Title", arrPolls[position].QuestionTitle)
                                    intent.putExtra("Author", arrPolls[position].QuestionAuthor)
                                    startActivity(intent)
                                }else{
                                    Toast.makeText(context, "Expired polls can't be re-answered!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //handle databaseError
                }
            }
        )
        return view
    }
}