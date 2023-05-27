package com.example.letmeknow

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class AnsweringPolls : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answering_polls)
        val questionQuestionTitle: TextInputEditText = findViewById(R.id.QuestionTitleAnsweringPoll)
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        val descriptionContainer: LinearLayout = findViewById(R.id.DescriptionContainer)
        val optionSubmit: Button = findViewById(R.id.optionSubmit)
        val currentUser = FirebaseAuth.getInstance().currentUser?.email
        var userKaEmail = "${currentUser.toString()}"
        userKaEmail = userKaEmail.replace("@", "at")
        userKaEmail = userKaEmail.replace(".", "dot")
        val bundle: Bundle? = intent.extras
        val Title = bundle!!.getString("Title")

        questionQuestionTitle.setText(Title)
        questionQuestionTitle.isEnabled = false
        val rootNode = FirebaseDatabase.getInstance("https://let-me-know-cb14b-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val desiredQuestion = rootNode.getReference("polls/${Title!!}")

        desiredQuestion.get().addOnSuccessListener {
            if(it.exists()){
                // Add Views to DescriptionContainer
                if(it.child("userinput").exists()){
                    val userInputMap = it.child("userinput").value as Map<String, Map<String, String>>
                    val inputCount = userInputMap.count()
                    for(i in 0 until inputCount){
                        val desiredInputMap = userInputMap["description$i"]!!
                        when (desiredInputMap["type"]){
                            "description" -> {
                                val dividerImage = LayoutInflater.from(this).inflate(R.layout.dividerimage, null)
                                descriptionContainer.addView(dividerImage)
                                val descriptionBoxLayout: TextInputLayout = LayoutInflater.from(this).inflate(R.layout.descriptionboximageviewpollsetting1layout, null) as TextInputLayout
                                val descriptionBox: TextInputEditText = LayoutInflater.from(this).inflate(R.layout.descriptionboximageviewpollsetting1, null) as TextInputEditText
                                descriptionBox.setText(desiredInputMap["data"])
                                descriptionBox.isEnabled = false
                                descriptionContainer.addView(descriptionBoxLayout)
                                descriptionBoxLayout.addView(descriptionBox)
                            }
                            "image" -> {
                                val dividerImage = LayoutInflater.from(this).inflate(R.layout.dividerimage, null)
                                descriptionContainer.addView(dividerImage)
                                val imageContainer = LayoutInflater.from(this).inflate(R.layout.poll_image_container, null) as ImageView
                                val storageReference = FirebaseStorage.getInstance("gs://let-me-know-cb14b.appspot.com").reference.child("images/$Title.$i.jpg")
                                val localFile = File.createTempFile("tempfile$i", "jpg")
                                storageReference.getFile(localFile).addOnSuccessListener {
//                                    Setting image by setting imageview's Bitmap
//                                    val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
//                                    imageContainer.setImageBitmap(bitmap)

//                                    Setting image by setting imageview's Uri
                                    val localFileUri = localFile.toUri()
                                    imageContainer.setImageURI(localFileUri)
                                }.addOnFailureListener{
                                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                                }
                                descriptionContainer.addView(imageContainer)
                                localFile.deleteOnExit()
                            }
                        }

                    }
                }


                // Add Options to radioGroup
                val optionMap = it.child("options").value as Map<String, String>
                val optionCount = optionMap.count()
                for(i in 1 until (optionCount+1)){
                    val optionRadioButton: RadioButton = LayoutInflater.from(this).inflate(R.layout.radiobuttonradiogroupansweringpolls, null) as RadioButton
                    optionRadioButton.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    optionRadioButton.text = optionMap["Option$i"]
                    radioGroup.addView(optionRadioButton)
                }

                optionSubmit.setOnClickListener {
                    val optionCount = radioGroup.childCount
                    var optBoolean = false
                    for(i in 0 until optionCount){
                        val myRadio = radioGroup.getChildAt(i) as RadioButton
                        if (myRadio.isChecked){
                            val optionEntry = rootNode.getReference("polls/$Title/answers/$userKaEmail")
                            optionEntry.setValue("Option${i+1}")
                            optBoolean = true
                            break
                        }
                    }
                    if(optBoolean){
                        val intent = Intent(this, UserHome::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }
}