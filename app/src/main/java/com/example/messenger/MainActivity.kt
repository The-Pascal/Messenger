package com.example.messenger

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //used to underline text
        val alreadyhaveaccount = findViewById<TextView>(R.id.already_account_register)
        alreadyhaveaccount.setPaintFlags(alreadyhaveaccount.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)


        val registerbtn = findViewById<Button>(R.id.register_button_register)



        //registration button click
        registerbtn.setOnClickListener{
            var email = email_editText_register.text.toString()
            var password = password_editText_register.text.toString()
            Log.e("register","Email: $email and password: $password")
        }

        //already have an account click listener
        already_account_register.setOnClickListener{
            val myintent = Intent(this, loginpage::class.java)
            startActivity(myintent)
        }

    }
}
