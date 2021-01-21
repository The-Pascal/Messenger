package com.example.messenger.registerLogin

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_loginpage.*


class loginpage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginpage)

        //used to underline text
        val backToRegistration = findViewById<TextView>(R.id.back_to_registration_login)

        //Login function
        login()

        //forgot password
        forgot_password_login.setOnClickListener{
            val intent = Intent(this,ResetPassword::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //Back to registration text
        backToRegistration.setOnClickListener{
            val intent = Intent(this , RegistrationPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun login(){

        google_login.setOnClickListener {

            login_button_login.startAnimation()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            auth = FirebaseAuth.getInstance()

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, loginpage.RC_SIGN_IN)
        }


        login_button_login.setOnClickListener{

            login_button_login.startAnimation()

            val email = email_editText_login.text.toString()
            val password = password_editText_login.text.toString()

            if(email.isEmpty() || password.isEmpty()){
                login_button_login.revertAnimation()
                Toast.makeText(this, "Enter all Credentials",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    if(it.isSuccessful) {
                        val deepColor = Color.parseColor("#27E1EF")
                        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.blue_tick)
                        login_button_login.doneLoadingAnimation(deepColor, largeIcon)
                        val uid = FirebaseAuth.getInstance().uid
                        FirebaseDatabase.getInstance().getReference("/Users/$uid/active")
                            .setValue(true)
                        val handler = Handler()
                        handler.postDelayed({
                                Toast.makeText(this, "Successfully Logged in", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, messageActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                        },1000)
                    }
                    else
                    {
                        login_button_login.revertAnimation()
                        Toast.makeText(this, "Wrong credentials. Try again!!",Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                }
                .addOnFailureListener {
                    login_button_login.revertAnimation()
                    Toast.makeText(this , "Error Logging in !!",Toast.LENGTH_SHORT).show()
                }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == loginpage.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(loginpage.TAG, "firebaseAuthWithGoogle:" + account.id)

                Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()

                val deepColor = Color.parseColor("#27E1EF")
                val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.blue_tick)
                login_button_login.doneLoadingAnimation(deepColor, largeIcon)
                val uid = FirebaseAuth.getInstance().uid
                FirebaseDatabase.getInstance().getReference("/Users/$uid/active")
                    .setValue(true)
                val handler = Handler()
                handler.postDelayed({
                    Toast.makeText(this, "Successfully Logged in", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, messageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                },1000)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(loginpage.TAG, "Google sign in failed", e)
                Toast.makeText(this, "Failed to Sign in !!", Toast.LENGTH_SHORT).show()
                login_button_login.revertAnimation()
            }
        }

    }

    companion object {
        private const val TAG = "Login_Activity"
        private const val RC_SIGN_IN = 9001
    }

}
