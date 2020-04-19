package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.activity_newmessage.*
import kotlinx.android.synthetic.main.newmessage.view.*

class  messageActivity : AppCompatActivity() {

    companion object{
        var currentUser: Users? = null

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        recyclerView_message_activity.adapter = adapter

        fetchCurrentUser()
        listenForNewMessages()

    }

    class LatestMessageRow(val chatMessage: ChatLogActivity.ChatMessage) : Item<ViewHolder>(){
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
                    val user = p0.getValue(Users:: class.java)
                    viewHolder.itemView.username_new_message.text = user?.username


                }

            })

        }

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

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLogActivity.ChatMessage::class.java)?: return

                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessage()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLogActivity.ChatMessage::class.java)?: return

                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessage()


                adapter.add(LatestMessageRow(chatMessage))
            }

            override fun onChildRemoved(p0: DataSnapshot) {}

        })
    }
    val adapter = GroupAdapter<ViewHolder>()


    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser =p0.getValue(Users::class.java)
                Log.d("LatestMessages","Current User ${currentUser?.imageUrl}")
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.new_messages_menu->{
                startActivity(Intent(this, newmessageActivity::class.java))

            }
            R.id.sign_out_menu->{

                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegistrationPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu , menu)
        return super.onCreateOptionsMenu(menu)
    }
}
