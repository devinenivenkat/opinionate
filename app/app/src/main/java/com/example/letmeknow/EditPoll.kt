package com.example.letmeknow

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.letmeknow.ui.CreateNewPoll.CreateNewPollOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EditPoll : AppCompatActivity() {

    // Defining the arrayLists
    private val descriptionArray = ArrayList<EditPollUserInput>()
    private var descriptionArrayCount = 0
    private val optionArray = ArrayList<CreateNewPollOptions>()
    private var optionArrayCount = 0
    private var imageIndex = 0
    private lateinit var loaderImage: ImageView
    private val urlArray = ArrayList<String>()

    // Defining imageSetterAndArrayAppender Function
    private fun imageSetterAndArrayAppender(myImageUri: Uri?){
        if(myImageUri != null){
            descriptionArray[imageIndex].newviewUri = myImageUri
//                    loaderImage.setImageURI(myImageUri)
            loaderImage.setImageURI(myImageUri)
        }
    }

    // Creating checkFormat Function
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_poll)

        // Declaring registerForActivityResult
        val getImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            imageSetterAndArrayAppender(it)
        }

        // Hooking the activity elements
        val descriptionContainer: LinearLayout = findViewById(R.id.descriptionContainer)
        val questionTitle: TextInputEditText = findViewById(R.id.questionTitle)
        val addImageButton: Button = findViewById(R.id.addImageButton)
        val addDescriptionButton: Button = findViewById(R.id.addDescriptionButton)
        val optionContainer: LinearLayout = findViewById(R.id.optionContainer)
        val addOptionButton: Button = findViewById(R.id.addOptionButton)
        val timer: TextInputEditText = findViewById(R.id.timer)
        val submitButton: Button = findViewById(R.id.submitButton)

        // Bundle extraction
        val bundle: Bundle? = intent.extras
        val title = bundle!!.getString("Title")!!

        // Database and Storage references
        val databaseNode = FirebaseDatabase.getInstance("https://let-me-know-cb14b-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val storageNode = FirebaseStorage.getInstance("gs://let-me-know-cb14b.appspot.com")
        val desiredQuestionInDatabase = databaseNode.getReference("polls/$title")

        desiredQuestionInDatabase.get().addOnSuccessListener {
            if(it.exists()) {
                // Adding to questionTitle
                questionTitle.setText(title)
                questionTitle.isEnabled = false

                // Adding to descriptionContainer
                if(it.child("userinput").exists()){
                    // Collecting data from node snapshot and converting to Map
                    val userInputMap = it.child("userinput").value as Map<String, Map<String, String>>
                    val inputCount = userInputMap.count()

                    for(i in 0 until inputCount){
                        val desiredInputMap = userInputMap["description$i"]!!
                        when(desiredInputMap["type"]){
                            "description" -> {
                                val dividerImage = LayoutInflater.from(this).inflate(R.layout.dividerimage, null)
                                val descriptionBoxLayout: TextInputLayout = LayoutInflater.from(this).inflate(R.layout.descriptionboximageviewpollsetting1layout, null) as TextInputLayout
                                val descriptionBox: TextInputEditText = LayoutInflater.from(this).inflate(R.layout.descriptionboximageviewpollsetting1, null) as TextInputEditText
                                val removeDescriptionButton: View = LayoutInflater.from(this).inflate(R.layout.removedescriptionbutton, null)

                                descriptionBox.setText(desiredInputMap["data"], TextView.BufferType.EDITABLE)
                                descriptionContainer.addView(dividerImage)
                                descriptionContainer.addView(descriptionBoxLayout)
                                descriptionBoxLayout.addView(descriptionBox)
                                descriptionContainer.addView(removeDescriptionButton)

                                val entryToDescriptionArray = EditPollUserInput("description", desiredInputMap["data"]!!, "", Uri.parse(""), descriptionArrayCount)
                                descriptionArray.add(entryToDescriptionArray)
                                val descriptionArrayCountNow = descriptionArrayCount
                                descriptionArrayCount++

                                descriptionBox.addTextChangedListener{myDescriptionBox ->
                                    descriptionArray[descriptionArrayCountNow].viewData = myDescriptionBox.toString()
                                }

                                removeDescriptionButton.setOnClickListener {
                                    descriptionArray.remove(entryToDescriptionArray)
                                    descriptionArrayCount--
                                    descriptionContainer.removeView(descriptionBoxLayout)
                                    descriptionContainer.removeView(removeDescriptionButton)
                                }
                            }
                            "image" -> {
                                val dividerImage = LayoutInflater.from(this).inflate(R.layout.dividerimage, null)
                                val imageContainer = LayoutInflater.from(this).inflate(R.layout.poll_image_container, null) as ImageView
                                val newImageButton: View = LayoutInflater.from(this).inflate(R.layout.sellect_image_button, null)
                                val removeImageButton: View = LayoutInflater.from(this).inflate(R.layout.removeimagebutton, null)
                                val imageUrlString = desiredInputMap["url"]

                                if(!desiredInputMap.containsKey("url")){
                                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                                    Toast.makeText(this, "Please re-upload the image", Toast.LENGTH_SHORT).show()
                                    descriptionContainer.addView(dividerImage)
                                    descriptionContainer.addView(imageContainer)
                                    descriptionContainer.addView(newImageButton)
                                    descriptionContainer.addView(removeImageButton)

                                    var entryToDescriptionArray = EditPollUserInput("image", "", "", Uri.parse(""),descriptionArrayCount)
                                    descriptionArray.add(entryToDescriptionArray)
                                    val descriptionArrayCountNow = descriptionArrayCount
                                    descriptionArrayCount++

                                    newImageButton.setOnClickListener {
                                        imageIndex = descriptionArrayCountNow
                                        loaderImage = imageContainer
                                        getImage.launch("image/*")
                                    }

                                    removeImageButton.setOnClickListener{
                                        descriptionArray.remove(entryToDescriptionArray)
                                        descriptionArrayCount--
                                        descriptionContainer.removeView(imageContainer)
                                        descriptionContainer.removeView(newImageButton)
                                        descriptionContainer.removeView(removeImageButton)
                                    }
                                }else{
                                    Glide.with(this).load(imageUrlString).into(imageContainer)
                                    descriptionContainer.addView(dividerImage)
                                    descriptionContainer.addView(imageContainer)
                                    descriptionContainer.addView(newImageButton)
                                    descriptionContainer.addView(removeImageButton)

                                    var entryToDescriptionArray = EditPollUserInput("image", "", imageUrlString!!, Uri.parse(""),descriptionArrayCount)
                                    urlArray.add(imageUrlString)
                                    descriptionArray.add(entryToDescriptionArray)
                                    val descriptionArrayCountNow = descriptionArrayCount
                                    descriptionArrayCount++

                                    newImageButton.setOnClickListener {
                                        imageIndex = descriptionArrayCountNow
                                        loaderImage = imageContainer
                                        getImage.launch("image/*")
                                    }

                                    removeImageButton.setOnClickListener{
                                        descriptionArray.remove(entryToDescriptionArray)
                                        descriptionArrayCount--
                                        descriptionContainer.removeView(imageContainer)
                                        descriptionContainer.removeView(newImageButton)
                                        descriptionContainer.removeView(removeImageButton)
                                    }
                                }


                            }
                        }
                    }
                }

                // Adding to optionContainer
                val optionMap = it.child("options").value as Map<String, String>
                for(i in 0 until optionMap.count()){
                    val dividerImage = LayoutInflater.from(this).inflate(R.layout.dividerimage, null)
                    val optionBoxLayout = LayoutInflater.from(this).inflate(R.layout.descriptionboximageviewpollsetting1layout, null) as TextInputLayout
                    val optionBox = LayoutInflater.from(this).inflate(R.layout.textviewanalysisoptions, null) as TextInputEditText
                    val removeOptionButton = LayoutInflater.from(this).inflate(R.layout.removeoptionedittext, null) as Button

                    optionBox.setText(optionMap["Option${i+1}"], TextView.BufferType.EDITABLE)
                    optionContainer.addView(dividerImage)
                    optionContainer.addView(optionBoxLayout)
                    optionBoxLayout.addView(optionBox)
                    if(i>1){optionContainer.addView(removeOptionButton)}

                    var inputToArrayForOption = CreateNewPollOptions(optionMap["Option${i+1}"]!!, optionArrayCount)
                    optionArray.add(inputToArrayForOption)
                    val optionArrayCountNow = optionArrayCount
                    optionArrayCount++

                    optionBox.addTextChangedListener {myOptionBox ->
                        optionArray[optionArrayCountNow].data = myOptionBox.toString()
                    }

                    removeOptionButton.setOnClickListener {
                        optionArray.remove(inputToArrayForOption)
                        optionArrayCount--
                        optionContainer.removeView(optionBoxLayout)
                        optionContainer.removeView(removeOptionButton)
                    }
                }

                // Adding to timer
                val timerString = it.child("timer").value as String
                timer.setText(timerString, TextView.BufferType.EDITABLE)

                // addImageButton Listener
                addImageButton.setOnClickListener {
                    val dividerImage = LayoutInflater.from(this).inflate(R.layout.dividerimage, null)
                    val imageContainer = LayoutInflater.from(this).inflate(R.layout.poll_image_container, null) as ImageView
                    val newImageButton: View = LayoutInflater.from(this).inflate(R.layout.sellect_image_button, null)
                    val removeImageButton: View = LayoutInflater.from(this).inflate(R.layout.removeimagebutton, null)

                    descriptionContainer.addView(dividerImage)
                    descriptionContainer.addView(imageContainer)
                    descriptionContainer.addView(newImageButton)
                    descriptionContainer.addView(removeImageButton)

                    var entryToDescriptionArray = EditPollUserInput("image", "", "", Uri.parse(""),descriptionArrayCount)
                    descriptionArray.add(entryToDescriptionArray)
                    val descriptionArrayCountNow = descriptionArrayCount
                    descriptionArrayCount++

                    newImageButton.setOnClickListener{
                        imageIndex = descriptionArrayCountNow
                        loaderImage = imageContainer
                        getImage.launch("image/*")
                    }

                    removeImageButton.setOnClickListener{
                        descriptionArray.remove(entryToDescriptionArray)
                        descriptionArrayCount--
                        descriptionContainer.removeView(imageContainer)
                        descriptionContainer.removeView(newImageButton)
                        descriptionContainer.removeView(removeImageButton)
                    }
                }

                // addDescriptionButton
                addDescriptionButton.setOnClickListener{
                    val dividerImage = LayoutInflater.from(this).inflate(R.layout.dividerimage, null)
                    val descriptionBoxLayout: TextInputLayout = LayoutInflater.from(this).inflate(R.layout.descriptionboximageviewpollsetting1layout, null) as TextInputLayout
                    val descriptionBox: TextInputEditText = LayoutInflater.from(this).inflate(R.layout.descriptionboximageviewpollsetting1, null) as TextInputEditText
                    val removeDescriptionButton: View = LayoutInflater.from(this).inflate(R.layout.removedescriptionbutton, null)

                    descriptionContainer.addView(dividerImage)
                    descriptionContainer.addView(descriptionBoxLayout)
                    descriptionBoxLayout.addView(descriptionBox)
                    descriptionContainer.addView(removeDescriptionButton)

                    var entryToDescriptionArray = EditPollUserInput("description", "", "", Uri.parse(""), descriptionArrayCount)
                    descriptionArray.add(entryToDescriptionArray)
                    val descriptionArrayCountNow = descriptionArrayCount
                    descriptionArrayCount++

                    descriptionBox.addTextChangedListener{myDescriptionBox ->
                        descriptionArray[descriptionArrayCountNow].viewData = myDescriptionBox.toString()
                    }

                    removeDescriptionButton.setOnClickListener {
                        descriptionArray.remove(entryToDescriptionArray)
                        descriptionArrayCount--
                        descriptionContainer.removeView(descriptionBoxLayout)
                        descriptionContainer.removeView(removeDescriptionButton)
                    }
                }

                // addOptionButton Listener
                addOptionButton.setOnClickListener{
                    val dividerImage = LayoutInflater.from(this).inflate(R.layout.dividerimage, null)
                    val optionBoxLayout = LayoutInflater.from(this).inflate(R.layout.descriptionboximageviewpollsetting1layout, null) as TextInputLayout
                    val optionBox = LayoutInflater.from(this).inflate(R.layout.textviewanalysisoptions, null) as TextInputEditText
                    val removeOptionButton = LayoutInflater.from(this).inflate(R.layout.removeoptionedittext, null) as Button

                    optionContainer.addView(dividerImage)
                    optionContainer.addView(optionBoxLayout)
                    optionBoxLayout.addView(optionBox)
                    optionContainer.addView(removeOptionButton)

                    var inputToArrayForOption = CreateNewPollOptions("", optionArrayCount)
                    optionArray.add(inputToArrayForOption)
                    val optionArrayCountNow = optionArrayCount
                    optionArrayCount++

                    optionBox.addTextChangedListener {myOptionBox ->
                        optionArray[optionArrayCountNow].data = myOptionBox.toString()
                    }

                    removeOptionButton.setOnClickListener {
                        optionArray.remove(inputToArrayForOption)
                        optionArrayCount--
                        optionContainer.removeView(optionBoxLayout)
                        optionContainer.removeView(removeOptionButton)
                    }
                }

                // submitButton Listener
                submitButton.setOnClickListener{
                    // Checking necessary conditions

                        // Checking timer format
                    var checkFormatBool = checkFormat(timer.text.toString())
                    if(!checkFormatBool){
                        Toast.makeText(this, "Check the Deadline Time format!", Toast.LENGTH_SHORT).show()
                    }

                        // Checking if at least two options are filled
                    var filledOptionsCount = 0
                    for(option in optionArray){
                        if(option.data != ""){
                            filledOptionsCount++
                        }
                    }
                    val filledOptionCountBool: Boolean = if(filledOptionsCount<2){
                        Toast.makeText(this, "You must fill at least two options!", Toast.LENGTH_SHORT).show()
                        false
                    }else{
                        true
                    }

                    if(checkFormatBool && filledOptionCountBool) {
                        // Cleaning the desiredQuestionInDatabase
                        desiredQuestionInDatabase.child("userinput").removeValue()
                        desiredQuestionInDatabase.child("options").removeValue()

                        // Cleaning the images from storage
                        for (input in descriptionArray) {
                            if (input.viewType == "image") {
                                if (input.newviewUri == Uri.parse("") && input.viewUrl != "") {
                                    urlArray.remove(input.viewUrl)
                                }
                            }
                        }
                        for (imageUrl in urlArray) {
                            val photoRef = storageNode.getReferenceFromUrl(imageUrl)
                            photoRef.delete()
                        }

                        // Adding to currentTime
                        val currentTimeReference =
                            databaseNode.getReference("polls/$title/currentTime")
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        val current = LocalDateTime.now().format(formatter)
                        currentTimeReference.setValue(current)

                        // Adding to timer
                        val timerStringReference = databaseNode.getReference("polls/$title/timer")
                        timerStringReference.setValue(timer.text.toString())

                        // Adding to options
                        for (option in optionArray) {
                            if (option.data == "") {
                                continue
                            } else {
                                val optionReference = databaseNode.getReference(
                                    "polls/$title/options/Option${
                                        optionArray.indexOf(option) + 1
                                    }"
                                )
                                optionReference.setValue(option.data)
                            }
                        }

                        // Adding to userinput
                        for (input in descriptionArray) {
                            if (input.viewType == "description") {
                                if (input.viewData == "") {
                                    continue
                                } else {
                                    val descriptionReference = databaseNode.getReference(
                                        "polls/$title/userinput/description${
                                            descriptionArray.indexOf(input)
                                        }"
                                    )
                                    descriptionReference.child("data").setValue(input.viewData)
                                    descriptionReference.child("type").setValue("description")
                                }
                            } else if (input.viewType == "image") {
                                if (input.newviewUri == Uri.parse("") && input.viewUrl == "") {
                                    continue
                                } else {
                                    val descriptionReference = databaseNode.getReference(
                                        "polls/$title/userinput/description${
                                            descriptionArray.indexOf(input)
                                        }"
                                    )
                                    val linkToFile = "$title.${descriptionArray.indexOf(input)}.jpg"
                                    val fileStorage = storageNode.getReference("images/$linkToFile")
                                    if (input.newviewUri != Uri.parse("")) {
                                        fileStorage.putFile(input.newviewUri)
                                            .addOnSuccessListener { taskSnapshot ->
                                                taskSnapshot.storage.downloadUrl.addOnSuccessListener { myUrl ->
                                                    val imageUrl = myUrl.toString()
                                                    descriptionReference.child("url")
                                                        .setValue(imageUrl)
                                                    descriptionReference.child("type")
                                                        .setValue("image")
                                                    descriptionReference.child("filename")
                                                        .setValue(linkToFile)
                                                }
                                            }.addOnFailureListener {
                                                Toast.makeText(
                                                    this,
                                                    "Couldn't upload the image",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    } else if (input.newviewUri == Uri.parse("") && input.viewUrl != "") {
                                        val linkToFile = "$title.${descriptionArray.indexOf(input)}"
                                        val fileInStorage =
                                            storageNode.getReferenceFromUrl(input.viewUrl)
                                        val localFile = File.createTempFile(linkToFile, "jpg")
                                        fileInStorage.getFile(localFile).addOnSuccessListener {
                                            val localFileUri = localFile.toUri()
                                            fileInStorage.delete().addOnSuccessListener {
                                                val descriptionReference =
                                                    databaseNode.getReference(
                                                        "polls/$title/userinput/description${
                                                            descriptionArray.indexOf(input)
                                                        }"
                                                    )
                                                val fileStorage =
                                                    storageNode.getReference("images/$linkToFile.jpg")
                                                fileStorage.putFile(localFileUri)
                                                    .addOnSuccessListener { taskSnapshot ->
                                                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { myUrl ->
                                                            val imageUrl = myUrl.toString()
                                                            descriptionReference.child("url")
                                                                .setValue(imageUrl)
                                                            descriptionReference.child("type")
                                                                .setValue("image")
                                                            descriptionReference.child("filename")
                                                                .setValue(linkToFile)
                                                        }
                                                    }.addOnFailureListener {
                                                        Toast.makeText(
                                                            this,
                                                            "Couldn't upload the image",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                            localFile.deleteOnExit()
                                        }
                                    }
                                }
                            }
                        }
                        Toast.makeText(this, "Poll Edited!", Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Couldn't find the question in database!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

}