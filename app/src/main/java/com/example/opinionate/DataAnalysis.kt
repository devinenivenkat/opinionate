package com.example.letmeknow

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class DataAnalysis : AppCompatActivity() {

    // Random Color Generator
    fun colorPicker(colorIntList: ArrayList<Int>): Int{
        val random = Random()
        val color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        return if(color in colorIntList){
            colorPicker(colorIntList)
        }else{
            color
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_analysis)

        // Declaring necessary variables
        val bundle: Bundle? = intent.extras
        val title = bundle!!.getString("Title")
        val rootNode = FirebaseDatabase.getInstance("https://let-me-know-cb14b-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val questionTitle: TextView = findViewById(R.id.QuestionTitleAnalysis)
        val desiredQuestion = rootNode.getReference("polls/${title!!}")
        val optionContainer: LinearLayout = findViewById(R.id.OptionContainer)
        val barChart: BarChart = findViewById(R.id.barChart)

        desiredQuestion.addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    optionContainer.removeAllViews()

                    questionTitle.text = title

                    val valu = snapshot.value as Map<String, Any>
                    var answersMap = emptyMap<String, String>()
                    if(valu.containsKey("answers")){
                        answersMap = valu["answers"] as Map<String, String>
                    }

                    val optionMap = valu["options"] as Map<String, String>
                    val optionCount = optionMap.count()
                    for(i in 1 until (optionCount+1)){
                        val optionTextViewLayout: TextInputLayout = LayoutInflater.from(this@DataAnalysis).inflate(R.layout.textviewanalysisoptionslayout, null) as TextInputLayout
                        val optionTextView: TextInputEditText = LayoutInflater.from(this@DataAnalysis).inflate(R.layout.textviewanalysisoptions, null) as TextInputEditText
                        optionTextView.setText(optionMap["Option$i"])
                        optionTextViewLayout.hint = "Option$i"
                        optionContainer.addView(optionTextViewLayout)
                        optionTextViewLayout.addView(optionTextView)
                    }

                    // Color Set for Bars
                    val colorIntList = ArrayList<Int>()
                    for(i in 0 until optionCount){
                        val newColorInt = colorPicker(colorIntList)
                        colorIntList.add(newColorInt)
                    }

                    // Adding Bar Chart
                        // Adding x-axis values
                    val xValues = ArrayList<String>()
                    for(i in 1 until (optionCount+1)){
                        xValues.add("Option$i")
                    }
                        // Adding y-axis values
                    val barEntries = ArrayList<BarEntry>()
                    for(i in 0 until optionCount){
                        val yValue: Float = countExtractor(answersMap,"Option${i+1}")
                        barEntries.add(BarEntry(yValue, i))
                    }
                    val barDataSet = BarDataSet(barEntries, "Options")

                        // Enter x-axis and y-axis values to barChart
                    val data = BarData(xValues, barDataSet)
                    barChart.data = data

                    // Styling the barChart
                    barChart.setBackgroundColor(ContextCompat.getColor(this@DataAnalysis, R.color.white))
                    barChart.setNoDataText("No answers so far!")
                    barChart.animateXY(3000, 3000)
                    barDataSet.colors = colorIntList
                    barChart.axisLeft.textColor = ContextCompat.getColor(applicationContext, R.color.black)
                    barChart.axisLeft.textSize = 10f
                    barChart.xAxis.textColor = ContextCompat.getColor(applicationContext, R.color.black)
                    barChart.axisRight.isEnabled = false
                    barChart.setTouchEnabled(false)
                    barChart.setDrawValueAboveBar(false)
                    barChart.xAxis.textSize = 10f
                    barChart.xAxis.apply {
                        setDrawGridLines(false)
                        setDrawGridLines(false)
                        position = XAxis.XAxisPosition.BOTTOM
                    }
                    barChart.legend.isEnabled = false
                    barChart.setDescription(null)

                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle Error
                }
            }
        )
    }

    private fun countExtractor(map: Map<String, String>, option: String): Float {
        var yValue = 0
        for((_, value) in map.entries){
            if (value == option){
                yValue++
            }
        }
        return yValue.toFloat()
    }

}