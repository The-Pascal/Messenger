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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegistrationPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val registerbtn = findViewById<Button>(R.id.register_button_register)

        //used to underline text
        val alreadyhaveaccount = findViewById<TextView>(R.id.already_account_register)
//        alreadyhaveaccount.setPaintFlags(alreadyhaveaccount.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        //registration button click
        registerbtn.setOnClickListener{
            register()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()

        google_register.setOnClickListener{
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        //already have an account click listener
        already_account_register.setOnClickListener{
            val myintent = Intent(this, loginpage::class.java)
            startActivity(myintent)
        }

        //image selection click listener
        image_button_register.setOnClickListener{
            val intent =Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        //verify if user is logged in
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

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(this,"Sign in successful /n welcome",Toast.LENGTH_SHORT).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this,"Sign in failed!! ",Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun register(){

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
                Toast.makeText(this, "Error creating account. ${it.message}",Toast.LENGTH_SHORT).show()
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

    companion object {
        private const val TAG = "Register_Activity"
        private const val RC_SIGN_IN = 9001
    }
}

@Parcelize
class Users(val uid: String , val username: String , val imageUrl : String): Parcelable{
    constructor(): this("","","")
}
