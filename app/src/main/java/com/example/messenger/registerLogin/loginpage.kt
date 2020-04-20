package com.example.messenger.registerLogin

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.messenger.R
import com.example.messenger.latestMessages.messageActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_loginpage.*

class loginpage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginpage)

        //used to underline text
        val backToRegistration = findViewById<TextView>(R.id.back_to_registration_login)
        backToRegistration.setPaintFlags(backToRegistration.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        login()

        backToRegistration.setOnClickListener{
            val intent = Intent(this , RegistrationPage::class.java)
            startActivity(intent)
        }
    }

    fun login(){
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
}
