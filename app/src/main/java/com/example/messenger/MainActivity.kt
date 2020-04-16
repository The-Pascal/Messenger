package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val registerbtn = findViewById<Button>(R.id.register_button_register)

        registerbtn.setOnClickListener{
            val email = email_editText_register.text.toString()
            val password = password_editText_register.text.toString()
        }


    }
}
