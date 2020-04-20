package com.example.messenger.registerLogin

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.messenger.R
import com.example.messenger.latestMessages.messageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegistrationPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //used to underline text
        val alreadyhaveaccount = findViewById<TextView>(R.id.already_account_register)
        alreadyhaveaccount.setPaintFlags(alreadyhaveaccount.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        val registerbtn = findViewById<Button>(R.id.register_button_register)

        //registration button click
        registerbtn.setOnClickListener{

            register()
        }

        //already have an account click listener
        already_account_register.setOnClickListener{
            val myintent = Intent(this, loginpage::class.java)
            startActivity(myintent)
        }

        image_button_register.setOnClickListener{
            val intent =Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)

        }

        verifyUserIsLoggedIn()

    }

    var selectedPhotoUri : Uri?= null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode== Activity.RESULT_OK && data!= null)
        {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            imageview_register.setImageBitmap(bitmap)
            image_button_register.alpha = 0f
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //image_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    fun register(){

        val email = email_editText_register.text.toString()
        val password = password_editText_register.text.toString()
        Log.e("register","Email: $email and password: $password")

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this , "Enter all fields first ",Toast.LENGTH_SHORT).show()
            return
        }

        var auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(!it.isSuccessful){
                    return@addOnCompleteListener
                }
                else{
                    Toast.makeText(this,"Account successfully created with uid : ${it.result?.user?.uid} ",Toast.LENGTH_SHORT).show()
                    uploadPhotoToFirebase()
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Error creating account",Toast.LENGTH_SHORT).show()
            }

    }

    private fun uploadPhotoToFirebase() {
            if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.e("registerActivity", "Photo uploaded successfully: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener{
                    Log.e("registerActivity", "image downloaded url : $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
            }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl : String){
        val uid = FirebaseAuth.getInstance().uid?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
        val users = Users(
            uid,
            username_editText_register.text.toString(),
            profileImageUrl
        )
        ref.setValue(users)
            .addOnSuccessListener {
                Log.e("registerActivity","User is saved with $uid and $profileImageUrl")

                val intent = Intent(this, messageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }

    private fun verifyUserIsLoggedIn(){

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is signed in
            val intent = Intent(this, messageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }


    }
}

@Parcelize
class Users(val uid: String , val username: String , val imageUrl : String): Parcelable{
    constructor(): this("","","")
}
