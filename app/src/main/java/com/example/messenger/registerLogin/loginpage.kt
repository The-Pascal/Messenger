package com.example.messenger.registerLogin

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger.R
import com.example.messenger.latestMessages.messageActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_loginpage.*
import kotlinx.android.synthetic.main.activity_main.*

class loginpage : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginpage)

        //used to underline text
        val backToRegistration = findViewById<TextView>(R.id.back_to_registration_login)

        //Login function
        login()

        //Back to registration text
        backToRegistration.setOnClickListener{
            val intent = Intent(this , RegistrationPage::class.java)
            startActivity(intent)
        }
    }

    private fun login(){

//        google_login.setOnClickListener {
//
//            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build()
//
//            googleSignInClient = GoogleSignIn.getClient(this, gso)
//
//            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
//        }


        login_button_login.setOnClickListener{
            val email = email_editText_login.text.toString()
            val password = password_editText_login.text.toString()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Enter all Credentials",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    if(it.isSuccessful) {
                        Toast.makeText(this, "Successfully Logged in", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this , messageActivity::class.java))
                    }
                    else
                    {
                        Toast.makeText(this, "Email is badly formatted",Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this , "Error Logging in !!",Toast.LENGTH_SHORT).show()
                }

        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RegistrationPage.RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                val account = task.getResult(ApiException::class.java)!!
//                Log.d(RegistrationPage.TAG, "firebaseAuthWithGoogle:" + account.id)
////                firebaseAuthWithGoogle(account.idToken!!)
//            } catch (e: ApiException) {
//                // Google Sign In failed, update UI appropriately
//                Log.w(RegistrationPage.TAG, "Google sign in failed", e)
//                // ...
//            }
//        }
//
//    }
}
