package com.example.letmeknow.ui.MyPolls

//import com.example.letmeknow.HomePagePollCardDataModel --
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
import com.example.letmeknow.*
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


class MyPollsFragment : Fragment() {
    val arrPolls: ArrayList<MyPollsPagePollCardDataModel> = ArrayList()
    @RequiresApi(Build.VERSION_CODES.O)
    private fun collectData(users: Map<String, Any>) {

        //iterate through each user, ignoring their UID
        for ((_, value) in users) {

            //Get user map
            val singleUser = value as Map<*, *>
//            Toast.makeText(context, singleUser.toString(), Toast.LENGTH_SHORT).show()
            //Get phone field and append to list
            val Title: String = singleUser["questionTitle"].toString()
            val Author: String = singleUser["userEmail"].toString()
            val timer = singleUser["timer"] as String
            val thatTime = singleUser["currentTime"] as String
            val currentUser: String = FirebaseAuth.getInstance().currentUser!!.email!!
            if (Author == currentUser){
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
                    arrPolls.add(MyPollsPagePollCardDataModel(Title, "Author: $Author", "Time Left: $TimeLeft"))
                }else{
                    val TimeLeft = "Expired"
                    arrPolls.add(MyPollsPagePollCardDataModel(Title, "Author: $Author", TimeLeft))
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
        val diff: Long = date2.getTime() - date1.getTime()
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
        isOnPage = true
        arrPolls.clear()
//        Toast.makeText(context, "sjdfklsjfdsklfslkdfj", Toast.LENGTH_SHORT).show()
        val rootNode = FirebaseDatabase.getInstance("https://let-me-know-cb14b-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val pollNode = rootNode.getReference().child("polls")
//        var loopind = 1
        val view = inflater.inflate(R.layout.fragment_mypolls, container, false)
        pollNode.addValueEventListener(
            object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    arrPolls.clear()
                    //Get map of users in datasnapshot
//                    if (loopind == 1) {
                    if(isOnPage) {
                        val valu = dataSnapshot.value
                        collectData(valu as Map<String, Any>)
                    }
//                        loopind++
//                    }
                    val recyclerView: RecyclerView = view.findViewById(R.id.myPollsPageRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(context)

                    //        while (index<allPolls.size){
                    //        arrPolls.add(HomePagePollCardDataModel(allPolls.get("something ${index.toString()}"), "it's author 1"))
                    //        index++
                    //        }
                    //        allPolls.forEach{
                    //            val Title: String = it.value["title"].toString()
                    //            val Author: String = it.value["author"].toString()
                    //            arrPolls.add(HomePagePollCardDataModel(Title, Author))
                    //        }
                    val contex = context
                    if (contex != null) {

                        val recyclerAdapter= MyPollsPageRecyclerAdapter(contex, arrPolls)
                        recyclerView.adapter = recyclerAdapter
                        recyclerAdapter.setOnItemClickListener(object: HomePageRecyclerAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
//                                Toast.makeText(context, "You clicked on $position!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, DataAnalysis::class.java)
                                intent.putExtra("Title", arrPolls[position].QuestionTitle)
                                intent.putExtra("Author", arrPolls[position].QuestionAuthor)


                                startActivity(intent)
                            }

                        })
                    }


                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //handle databaseError
                }
            })
        return view
    }
}