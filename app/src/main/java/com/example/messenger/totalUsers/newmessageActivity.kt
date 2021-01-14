package com.example.messenger.totalUsers

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger.R
import com.example.messenger.chatLog.ChatLogActivity
import com.example.messenger.latestMessages.messageActivity
import com.example.messenger.registerLogin.RegistrationPage
import com.example.messenger.registerLogin.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_newmessage.*
import kotlinx.android.synthetic.main.user_row.view.*


class newmessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newmessage)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar2)

        toolbar.title = "Select User"

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)

        val intent = Intent(applicationContext, RegistrationPage::class.java)

        toolbar.setNavigationOnClickListener {
            startActivity(Intent(intent))
            finish()
        }


        fetchusers()
    }

    private fun fetchusers(){
        val ref =FirebaseDatabase.getInstance().getReference("/Users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val adapter = GroupAdapter<ViewHolder>()
                recyclerView_new_messages.adapter = adapter
                p0.children.forEach{
                    Log.e(USER,it.toString())
                    val user = it.getValue(Users::class.java)
                    val uid = FirebaseAuth.getInstance().uid
                    if(user != null && user.uid != uid) {
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener{item, view ->

                    val userItem = item as UserItem
                    val intent= Intent(view.context , ChatLogActivity::class.java)
                    intent.putExtra(USER, userItem.user)
                    startActivity(intent)
                    finish()
                }

            }

        })


    }

    companion object{
        const val USER = "USER_ITEM"
    }
}

class  UserItem(val user: Users) : Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.user_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_user_row.text = user.username
        if(user.active) {
            val deepColor = Color.parseColor("#2ECC71")
            viewHolder.itemView.active_status_user_row.text = "Active"
            viewHolder.itemView.active_status_user_row.setTextColor(deepColor)
        }
        else {
            val deepColor = Color.parseColor("#27E1EF")
            viewHolder.itemView.active_status_user_row.text = "Inactive"
            viewHolder.itemView.active_status_user_row.setTextColor(deepColor)
        }
        Picasso.get().load(user.imageUrl).into(viewHolder.itemView.profileimage_user_row)
    }

}

