package com.example.messenger.chatLog

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import com.example.messenger.R
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger.latestMessages.messageActivity
import com.example.messenger.registerLogin.Users
import com.example.messenger.totalUsers.newmessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.newmessage.view.*
import kotlinx.android.synthetic.main.receive_image_from.view.*
import kotlinx.android.synthetic.main.send_image_to.view.*
import java.text.SimpleDateFormat
import java.util.*


class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerView_chat_log.adapter = adapter

        val user = intent.getParcelableExtra<Users>(newmessageActivity.USER)
        supportActionBar?.title = user.username

        profilename_chatLog.text = user.username
        if(user.active)
        {
            active_status_chatLog.text = "Active"
            val deepColor = Color.parseColor("#0C5EDA")
            active_status_chatLog.setTextColor(deepColor)
        }
        else
        {
            val sfd = SimpleDateFormat("HH:mm")
            active_status_chatLog.text = "Last seen: " + sfd.format(Date(user.timestamp))
        }
        Picasso.get().load(user.imageUrl).into(profileimage_chatLog)

        back_arrow_chatLog.setOnClickListener{
            val intent = Intent(this,messageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }


        listenForMessages()

        send_button_chat_log.setOnClickListener{
            performSendMessage()
        }

        button_to_send_image_chat_log.setOnClickListener{

          uploadImageDataTodevice()
        }

    }

    private fun uploadImageDataTodevice()
    {
        button_to_send_image_chat_log.startAnimation()
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,0)

    }

    var selectedPhotoUri : Uri?= null

    var imageUrl: String ? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode== Activity.RESULT_OK && data!= null)
        {
            selectedPhotoUri = data.data

            uploadPhotoToFirebase()

        }
    }

    private fun uploadPhotoToFirebase() {
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images-send-by-users/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.e("imageshare", "Photo uploaded successfully: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener{

                    imageUrl= it.toString()
                    Log.e("imageshare", "image downloaded url : $imageUrl")
                }
            }
        val icon : Bitmap
        val deepColor = Color.parseColor("#808000")
        icon = BitmapFactory.decodeResource(resources,R.drawable.tick_icon)
        button_to_send_image_chat_log.doneLoadingAnimation(deepColor,icon)


    }

    private fun listenForMessages(){
        val user = intent.getParcelableExtra<Users>(newmessageActivity.USER)
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)


                val uid = FirebaseAuth.getInstance().uid

                if(chatMessage != null) {
                    Log.e("chat",chatMessage.text)

                    val currentUser = messageActivity.currentUser

                    if(chatMessage.fromId == uid && user.uid == chatMessage.toId )
                    {
                        if(chatMessage.text!="" && chatMessage.imageUrl!=""){

                            adapter.add(sendImageInChatLog(chatMessage,currentUser!!))
                            adapter.add(
                                ChatToItem(
                                    chatMessage.text,
                                    currentUser!!
                                )
                            )
                        }
                        else if( chatMessage.text =="" && chatMessage.imageUrl!="") {
                            adapter.add(sendImageInChatLog(chatMessage,currentUser!!))

                        }
                        else if(chatMessage.text!="" && chatMessage.imageUrl==""){
                            adapter.add(ChatToItem(chatMessage.text, currentUser!!))
                        }
                        else{

                        }

                        recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)

                    }
                    else if(chatMessage.toId == uid && user.uid == chatMessage.fromId){


                        if(chatMessage.text!="" && chatMessage.imageUrl!=""){

                            adapter.add(receiveImageInChatLog(chatMessage , user.imageUrl))
                            adapter.add(ChatFromItem(chatMessage.text, user.imageUrl))
                        }
                        else if( chatMessage.text =="" && chatMessage.imageUrl!="") {
                            adapter.add(receiveImageInChatLog(chatMessage , user.imageUrl))

                        }
                        else if(chatMessage.text!="" && chatMessage.imageUrl==""){
                            adapter.add(ChatFromItem(chatMessage.text,user.imageUrl))
                        }
                        else{

                        }
                        recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)

                    }
                    else{
                    }

                }
            }
        })
    }

    class ChatMessage(val id : String ,val text: String, val fromId : String , val toId : String , val timestamp: Long , val imageUrl: String)
    {
        constructor(): this("","","","",-1,"")
    }

    private fun performSendMessage(){
        val text = enter_message_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<Users>(newmessageActivity.USER)
        val toId = user.uid
        if(imageUrl == null){
            imageUrl =""
        }


        if(fromId == null )   return
//        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage =
            ChatMessage(
                ref.key!!,
                text,
                fromId,
                toId,
                System.currentTimeMillis(),
                imageUrl!!
            )

        ref.setValue(chatMessage)
            .addOnSuccessListener {
                enter_message_chat_log.text.clear()
                recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)
                Log.d("imageshare","image successfully saved in database with url : $imageUrl")
            }

        toref.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/new-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestToMessageRef = FirebaseDatabase.getInstance().getReference("/new-messages/$toId/$fromId")
        latestToMessageRef.setValue(chatMessage)

        imageUrl= null
        button_to_send_image_chat_log.revertAnimation{
            button_to_send_image_chat_log.setBackgroundResource(R.drawable.rounded_button)
        }
    }

}


class ChatFromItem(val text: String, val image : String): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text
        Picasso.get().load(image).into(viewHolder.itemView.imageView_from_row)
    }
    override fun getLayout(): Int {

        return R.layout.chat_from_row

    }

}

class ChatToItem(val text:String, val user: Users): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text
        val uri = user.imageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.imageView_to_row)

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}

class sendImageInChatLog( val chatMessage: ChatLogActivity.ChatMessage ,val user:Users): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.send_image_to
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val uri= chatMessage.imageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.send_image_chatlog)
        val profileimage = user.imageUrl
        Picasso.get().load(profileimage).into(viewHolder.itemView.profileimage_send_image)

    }

}

class receiveImageInChatLog( val chatMessage: ChatLogActivity.ChatMessage , val image : String): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.receive_image_from
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val uri= chatMessage.imageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.receive_image_chatlog)
        Picasso.get().load(image).into(viewHolder.itemView.profileimage_receive_chatlog)
    }

}

