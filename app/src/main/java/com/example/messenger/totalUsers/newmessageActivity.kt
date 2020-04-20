package com.example.messenger.totalUsers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.messenger.R
import com.example.messenger.chatLog.ChatLogActivity
import com.example.messenger.registerLogin.Users
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

        supportActionBar?.title = "Select User"

        fetchusers()
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }

    private fun fetchusers(){
        val ref =FirebaseDatabase.getInstance().getReference("/Users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach{
                    Log.e("Newmessages",it.toString())
                    val user = it.getValue(Users::class.java)
                    if(user != null) {
                        adapter.add(UserItem(user))
                    }
                }
                adapter.setOnItemClickListener{item, view ->

                    val userItem = item as UserItem
                    val intent= Intent(view.context , ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()

                }
                recyclerView_new_messages.adapter = adapter
            }

        })


    }
}

class  UserItem(val user: Users) : Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.user_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_user_row.text = user.username
        Picasso.get().load(user.imageUrl).into(viewHolder.itemView.profileimage_user_row)
    }

}