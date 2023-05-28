package com.example.letmeknow.ui.Home

//import com.example.letmeknow.HomePagePollCardDataModel --
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letmeknow.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class HomeFragment : Fragment() {
    val arrPolls: ArrayList<HomePagePollCardDataModel> = ArrayList<HomePagePollCardDataModel>()


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
            val timer = singleUser["timer"].toString()
            val thatTime = singleUser["currentTime"] as String

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
                val TimeLeft = "Time Left: ${numberOfDays}d${numberOfHours}h${numberOfMinutes}m"
                arrPolls.add(HomePagePollCardDataModel(Title, "Author: $Author", TimeLeft))
            }

        }
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

    private fun timeCalculatorInMinutes(timer: String): Int {
        val dayInt = "${timer[0]}${timer[1]}".toInt()
        val hoursInt = "${timer[3]}${timer[4]}".toInt()
        val minutesInt = "${timer[6]}${timer[7]}".toInt()
        val totalTimerInMinutes = dayInt*1440 + hoursInt*60 + minutesInt
        return totalTimerInMinutes
    }



    var isOnpage = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isOnpage = true
    }

    override fun onDetach() {
        super.onDetach()
        isOnpage = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isOnpage = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isOnpage = false
    }

    override fun onPause() {
        super.onPause()
        isOnpage = false
    }

    override fun onStop() {
        super.onStop()
        isOnpage = false
    }

    override fun onResume() {
        super.onResume()
        isOnpage = true
    }

    override fun onStart() {
        super.onStart()
        isOnpage = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isOnpage = true
        arrPolls.clear()
//        Toast.makeText(context, "sjdfklsjfdsklfslkdfj", Toast.LENGTH_SHORT).show()
        val rootNode = FirebaseDatabase.getInstance("https://let-me-know-cb14b-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val pollNode = rootNode.getReference().child("polls")
//        var loopind = 1
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        pollNode.addValueEventListener(
            object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    arrPolls.clear()
                    //Get map of users in datasnapshot
                    if (isOnpage) {
                        val valu = dataSnapshot.value
                        collectData(valu as Map<String, Any>)
//                        loopind++
                    }
                    val recyclerView: RecyclerView = view.findViewById(R.id.homePageRecyclerView)
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

                        val recyclerAdapter= HomePageRecyclerAdapter(contex, arrPolls)
                        recyclerView.adapter = recyclerAdapter
                        recyclerAdapter.setOnItemClickListener(object: HomePageRecyclerAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
//                                Toast.makeText(context, "You clicked on $position!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, AnsweringPolls::class.java)
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