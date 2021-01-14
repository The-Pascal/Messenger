package com.example.messenger.latestMessages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.messenger.R
import com.example.messenger.chatLog.ChatLogActivity
import com.example.messenger.registerLogin.RegistrationPage
import com.example.messenger.registerLogin.Users
import com.example.messenger.totalUsers.newmessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.newmessage.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class  messageActivity : AppCompatActivity() {

    //onCreate start
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        setSupportActionBar(toolbar)

        recyclerView_message_activity.adapter = adapter
        recyclerView_message_activity.apply { DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)}

        adapter.setOnItemClickListener { item, view ->
            Log.d("newmessage","click on one of the new message ")
            val intent = Intent( this , ChatLogActivity::class.java)

            val row = item as LatestMessageRow
            intent.putExtra(newmessageActivity.USER , row.chatPartnerUser)
            startActivity(intent)
        }

        fetchCurrentUser()
        listenForNewMessages()
    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(Users::class.java)
                Log.d("LatestMessages","Current User ${currentUser?.imageUrl}")
            }

        })
    }

    val latestMessagesMap = HashMap<String , ChatLogActivity.ChatMessage>()

    private fun refreshRecyclerViewMessage(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForNewMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/new-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val chatMessage = p0.getValue(ChatLogActivity.ChatMessage::class.java)?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessage()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLogActivity.ChatMessage::class.java)?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessage()
            }


        })
    }

    val adapter = GroupAdapter<ViewHolder>()

    class LatestMessageRow(val chatMessage: ChatLogActivity.ChatMessage) : Item<ViewHolder>(){

        var chatPartnerUser: Users?= null

        override fun getLayout(): Int {
            return R.layout.newmessage

        }

        override fun bind(viewHolder: ViewHolder, position: Int) {

            viewHolder.itemView.message_new_message.text = chatMessage.text

            val chatPartnerId: String
            if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            }
            else{
                chatPartnerId = chatMessage.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("/Users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser = p0.getValue(Users:: class.java)

                    val chatPartnerActive = chatPartnerUser?.active
                    chatPartnerName = chatPartnerUser?.username
                    viewHolder.itemView.username_new_message.text = chatPartnerUser?.username
                    val sfd = SimpleDateFormat("HH:mm")
                    viewHolder.itemView.time_textview.text = sfd.format(Date(chatMessage.timestamp))
                    Picasso.get().load(chatPartnerUser?.imageUrl).into(viewHolder.itemView.imageView_new_message)

                    if(chatPartnerActive==false)
                    {
                        viewHolder.itemView.active_button_user_row.visibility = View.INVISIBLE
                    }
                }

            })

        }

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.new_messages_menu ->{
                startActivity(Intent(this, newmessageActivity::class.java))
            }
            R.id.sign_out_menu ->{

                val uid = FirebaseAuth.getInstance().uid
                FirebaseDatabase.getInstance().getReference("/Users/$uid/active")
                    .setValue(false)

                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegistrationPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
        }
        return super.onOptionsItemSelected(item)
    }


    companion object{
        var currentUser: Users? = null
        var chatPartnerName : String ?= null
        val CHANNEL_ID = "my_channel_01"
    }

}
