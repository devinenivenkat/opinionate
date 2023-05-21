package com.example.letmeknow.ui.CreateNewPoll

//TODO: Work on the question title clash

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.size
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.letmeknow.R
import com.example.letmeknow.UserHome
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

// TODO: Limit number of words in question.
class CreateNewPollFragment : Fragment() {

    private val userInput = ArrayList<CreateNewPollUserInput>()
    private val options = ArrayList<CreateNewPollOptions>()
    private var listindex = 0
    private var selectedButton = 1
    private var optionIndex = 0
    private lateinit var imageCont: ImageView

    private fun thisfun(item: Uri?){
        if (item != null) {
            userInput.get(selectedButton).viewUri = item
        }
        val uri: Uri = userInput.get(selectedButton).viewUri
        imageCont.setImageURI(uri)
        Log.d("TAG", "\n\n\n\n\n\n\n\n\n\n\n\n\nthisfun: \n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nIOJIOIJ")
        var loopindex = 0
        while(loopindex < userInput.size){
            val i = userInput.get(loopindex)
            Log.d("TAG", "uri:${i.viewUri} data:${i.viewData} index:${i.index} type:${i.viewType}")
            loopindex++
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_createnewpoll, container, false)
        val addOption: Button = view.findViewById(R.id.AddOption)
        val addImageButton: Button = view.findViewById(R.id.addImageButton)
        val optionContainer: LinearLayout = view.findViewById(R.id.OptionContainer)
        val descriptionContainer: LinearLayout = view.findViewById(R.id.DescriptionContainer)
        val addDescriptionButton: Button = view.findViewById(R.id.addDescriptionButton)
        val questionTitle: EditText = view.findViewById(R.id.QuestionTitle)
        val questionDescription1: EditText = view.findViewById(R.id.questionDescription1)     //
        val option1: EditText = view.findViewById(R.id.option1)
        val option2: EditText = view.findViewById(R.id.option2)
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        val newPollSubmit: Button = view.findViewById(R.id.newPollSubmit)
        val timeEditText: EditText = view.findViewById(R.id.timer)
        val getImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            thisfun(it)
        }

        // Special workaround for questionDescription1
        var inputToArrayForDescription1 = CreateNewPollUserInput("description", "", Uri.parse(""), listindex)
        userInput.add(inputToArrayForDescription1)
        val uru = listindex
        questionDescription1.addTextChangedListener{
            userInput.get(uru).viewData = it.toString()
            Log.d("TAG", "\n\n\n\n\n\n\n\n\n\n\n\n\nthisfun: \n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
            var loopindex = 0
            while (loopindex < userInput.size) {
                val i = userInput.get(loopindex)
                Log.d("TAG", "uri:${i.viewUri} data:${i.viewData} index:${i.index} type:${i.viewType}")
                loopindex++
            }
        }
        listindex++

        // Adding Option1 to array
        var inputToArrayForOption1 = CreateNewPollOptions("", optionIndex)
        options.add(inputToArrayForOption1)
        val indexNowForOption1 = optionIndex
        option1.addTextChangedListener{
            options[indexNowForOption1].data = it.toString()
        }
        optionIndex++

        // Adding Option2 to array
        var inputToArrayForOption2 = CreateNewPollOptions("", optionIndex)
        options.add(inputToArrayForOption2)
        val indexNowForOption2 = optionIndex
        option2.addTextChangedListener{
            options[indexNowForOption2].data = it.toString()
        }
        optionIndex++

        addOption.setOnClickListener{
            var inputToArrayForOption = CreateNewPollOptions("", optionIndex)
            options.add(inputToArrayForOption)
            val indexNowForOption = optionIndex
            val optionViewLayout: TextInputLayout = LayoutInflater.from(context).inflate(R.layout.optionedittext1layout, null) as TextInputLayout
            val optionView: TextInputEditText = LayoutInflater.from(context).inflate(R.layout.optionedittext1, null) as TextInputEditText
            optionContainer.addView(optionViewLayout)
            optionViewLayout.addView(optionView)
            optionView.addTextChangedListener{
                options[indexNowForOption].data = it.toString()
            }
            optionIndex++
        }

        addImageButton.setOnClickListener{
            var objec = CreateNewPollUserInput("image", "", Uri.parse(""), listindex)
            userInput.add(objec)
            val newImageContainer: LinearLayout =
                LayoutInflater.from(context).inflate(R.layout.simple_linear_layout, null) as LinearLayout
            var imageCont2: ImageView =
                LayoutInflater.from(context).inflate(R.layout.poll_image_container, null) as ImageView
            imageCont2.id = 4000 + listindex
            newImageContainer.addView(imageCont2)

            val newImageButton: View = LayoutInflater.from(context).inflate(R.layout.sellect_image_button, null)
            val removeImageButton: View = LayoutInflater.from(context).inflate(R.layout.removeimagebutton, null)
//            val viewImageView: View = LayoutInflater.from(context).inflate(R.layout.imageviewpollsetting1, null)
//            val newImage = viewImageView as ImageView
            val uru = listindex + 1 - 1
            newImageButton.setOnClickListener{
                selectedButton = uru
                imageCont = imageCont2
                getImage.launch("image/*")
            }
            Log.d("TAG", "onCreateView: " + userInput.toString())
            newImageContainer.addView(newImageButton)

            listindex++
            descriptionContainer.addView(newImageContainer)
            descriptionContainer.addView(removeImageButton)
            removeImageButton.setOnClickListener{
                userInput.remove(objec)
                listindex--
                descriptionContainer.removeView(imageCont2)
                descriptionContainer.removeView(newImageContainer)
                descriptionContainer.removeView(removeImageButton)
            }
        }

        addDescriptionButton.setOnClickListener{
            var objec = CreateNewPollUserInput("description", "", Uri.parse(""), listindex)
            userInput.add(objec)

            val descriptionBoxLayout: TextInputLayout = LayoutInflater.from(context).inflate(R.layout.descriptionboximageviewpollsetting1layout, null) as TextInputLayout
            val descriptionBox: TextInputEditText = LayoutInflater.from(context).inflate(R.layout.descriptionboximageviewpollsetting1, null) as TextInputEditText
            val removeDescriptionButton: View = LayoutInflater.from(context).inflate(R.layout.removedescriptionbutton, null)
            val uru = listindex
            descriptionBox.addTextChangedListener{
                userInput[uru].viewData = it.toString()
                Log.d("TAG", "\n\n\n\n\n\n\n\n\n\n\n\n\nthisfun: \n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
                var loopindex = 0
                while (loopindex < userInput.size) {
                    val i = userInput.get(loopindex)
                    Log.d("TAG", "uri:${i.viewUri} data:${i.viewData} index:${i.index} type:${i.viewType}")
                    loopindex++
                }

            }
            listindex++
            descriptionContainer.addView(descriptionBoxLayout)
            descriptionBoxLayout.addView(descriptionBox)
            descriptionContainer.addView(removeDescriptionButton)
            removeDescriptionButton.setOnClickListener{
                userInput.remove(objec)
                listindex--
                descriptionContainer.removeView(descriptionBox)
                descriptionContainer.removeView(removeDescriptionButton)
            }
        }

        newPollSubmit.setOnClickListener{
            val rootNode = FirebaseDatabase.getInstance("https://let-me-know-cb14b-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val refStorage = FirebaseStorage.getInstance("gs://let-me-know-cb14b.appspot.com")
            val questionTitleText: String = questionTitle.text.toString()
            if(checkFormat(timeEditText.text.toString())) {

                // Adding timer
                val timerStringReference = rootNode.getReference("polls/$questionTitleText/timer")
                timerStringReference.setValue(timeEditText.text.toString())

                // Adding currentTime
                val currentTimeReference = rootNode.getReference("polls/$questionTitleText/currentTime")
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val current = LocalDateTime.now().format(formatter)
                currentTimeReference.setValue(current)

                // Adding questionTitle
                val referenceQuestion = rootNode.getReference("polls/$questionTitleText/questionTitle")
                referenceQuestion.setValue("$questionTitleText")

                //Uploading userEmail
                val referenceUserEmail = rootNode.getReference("polls/$questionTitleText/userEmail")
                referenceUserEmail.setValue("$userEmail")

                // Uploading Description
                var imageCount = 0
                for(inputItem in userInput){
                    if(inputItem.viewType=="image"){
                        imageCount++
                    }
                }
                var descriptionUploadedCount = 0
                var imageUploadedCount = 0
                var userinpindex = 0
                for (inputitem in userInput) {
                    if (inputitem.viewType == "image") {
                        if(inputitem.viewUri==Uri.parse("")){
                            continue
                        }else{
                                val userinpdb =
                                rootNode.getReference("polls/$questionTitleText/userinput/description$userinpindex")
                            val linkToFile = "$questionTitleText.$userinpindex.jpg"
                            val fileStore = refStorage.getReference("images/$linkToFile")
                            fileStore.putFile(inputitem.viewUri)
                                .addOnSuccessListener(
                                    OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                                            val imageUrl = it.toString()
                                            userinpdb.child("url").setValue(imageUrl)
                                            imageUploadedCount++
                                            descriptionUploadedCount++
                                        }
                                    })

                                .addOnFailureListener(OnFailureListener { e ->
                                    print(e.message)
                                })
                            userinpdb.child("type").setValue(inputitem.viewType)
                            userinpdb.child("filename").setValue(linkToFile)
                            userinpindex++
                        }
                    } else if (inputitem.viewType == "description") {
                        if(inputitem.viewData==""){
                            continue
                        }else{
                            val userinpdb =
                                rootNode.getReference("polls/$questionTitleText/userinput/description$userinpindex")
                            userinpdb.child("type").setValue(inputitem.viewType)
                            userinpdb.child("data").setValue(inputitem.viewData).addOnSuccessListener{
                                descriptionUploadedCount++
                            }
                            userinpindex++
                        }

                    }
                }

                // Uploading options
                var optionUploadedCount = 0
                for(inputItem in options){
                    val referenceOption = rootNode.getReference("polls/$questionTitleText/options/Option${inputItem.index+1}")
                    referenceOption.setValue(inputItem.data).addOnSuccessListener{
                        optionUploadedCount++
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(context, UserHome::class.java)
                    startActivity(intent)
                }, 1500)

            }else{
                Toast.makeText(context, "Please enter the correct format", Toast.LENGTH_SHORT).show()
            }


        }


        return view
    }
    private fun checkFormat(givenString: String): Boolean {
        val format = "DDLDDLDDL"
        if (givenString.length != format.length) return false
        givenString.forEachIndexed { index, c ->
            when (format[index]) {
                'L' -> if (!c.isLetter() || (c!='d' && c!='h' && c!='m')) return false
                'D' -> if (!c.isDigit()) return false
            }
        }
        return true
    }

}