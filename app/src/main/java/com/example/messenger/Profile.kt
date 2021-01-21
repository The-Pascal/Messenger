package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.messenger.R.*
import com.example.messenger.latestMessages.messageActivity
import com.example.messenger.registerLogin.RegistrationPage
import com.example.messenger.registerLogin.Users
import com.example.messenger.totalUsers.UserItem
import com.example.messenger.totalUsers.newmessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_newmessage.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.newmessage.view.*
import java.text.SimpleDateFormat
import java.util.*

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_profile)

        val uid = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(Users:: class.java)
                    username_profile.text = user?.username
                    email_profile.text = user?.email
                    val profileUrl = user?.imageUrl
                Picasso.get().load(profileUrl.toString()).into(imageView6)
            }

        })

        sign_out.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("/Users/$uid/active")
                .setValue(false)
            FirebaseAuth.getInstance().signOut()


            val intent = Intent(this, RegistrationPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }


    }
}