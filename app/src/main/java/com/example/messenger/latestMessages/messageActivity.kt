package com.example.messenger.latestMessages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.messenger.R
import com.example.messenger.chatLog.ChatLogActivity
import com.example.messenger.registerLogin.RegistrationPage
import com.example.messenger.registerLogin.Users
import com.example.messenger.totalUsers.newmessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.newmessage.view.*

class  messageActivity : AppCompatActivity() {

    companion object{
        var currentUser: Users? = null
        var chatPartnerName : String ?= null

    }
    val CHANNEL_ID = "my_channel_01"




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        recyclerView_message_activity.adapter = adapter
        recyclerView_message_activity.addItemDecoration(DividerItemDecoration(this , DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            Log.d("newmessage","click on one of the new message ")
            val intent = Intent( this , ChatLogActivity::class.java)

            val row = item as LatestMessageRow
            intent.putExtra(newmessageActivity.USER_KEY , row.chatPartnerUser)
            startActivity(intent)


        }

        fetchCurrentUser()
        listenForNewMessages()
        //createNotificationChannel()


    }

    /*private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = "Test Notification"
            val descriptionText = "getString(R.string.channel_description)"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }*/

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

                    chatPartnerName = chatPartnerUser?.username
                    viewHolder.itemView.username_new_message.text = chatPartnerUser?.username

                    Picasso.get().load(chatPartnerUser?.imageUrl).into(viewHolder.itemView.imageView_new_message)
                }

            })

        }

    }

    val latestMessagesMap = HashMap<String , ChatLogActivity.ChatMessage>()

    private fun refreshRecyclerViewMessage(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(
                LatestMessageRow(
                    it
                )
            )
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

                notificationbuilder(chatMessage)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatLogActivity.ChatMessage::class.java)?: return

                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessage()


                adapter.add(
                    LatestMessageRow(
                        chatMessage
                    )
                )
            }

            override fun onChildRemoved(p0: DataSnapshot) {}

        })
    }
    val adapter = GroupAdapter<ViewHolder>()


    private fun notificationbuilder(chatMessage: ChatLogActivity.ChatMessage){

        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(chatPartnerName)
            .setContentText(chatMessage.text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationId = 79870

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }
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
            R.id.new_messages_menu ->{
                startActivity(Intent(this, newmessageActivity::class.java))

            }
            R.id.sign_out_menu ->{

                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegistrationPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
