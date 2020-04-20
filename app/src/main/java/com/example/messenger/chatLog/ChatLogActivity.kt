package com.example.messenger.chatLog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import com.example.messenger.R
import com.example.messenger.latestMessages.messageActivity
import com.example.messenger.totalUsers.newmessageActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import com.example.messenger.registerLogin.Users
import com.example.messenger.show_images.show_images_dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.send_image_in_chat_log.view.*
import kotlinx.android.synthetic.main.show_image_dialog.*
import java.util.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerView_chat_log.adapter = adapter

        val user = intent.getParcelableExtra<Users>(newmessageActivity.USER_KEY)
        supportActionBar?.title = user.username

       listenForMessages()

        send_button_chat_log.setOnClickListener{
            performSendMessage()
        }

        button_to_send_image_chat_log.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)

        }

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

    }

    private fun listenForMessages(){

        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)


                   /* adapter.setOnItemClickListener { item, view ->

                        val dialog = show_images_dialog()
                        val imageForDialog = findViewById<ImageView>(R.id.imageView_show_image_on_fullscreen)
                        dialog.sendImageSelected(imageForDialog)
                        dialog.show(supportFragmentManager, "123")

                    }*/


                val user = intent.getParcelableExtra<Users>(newmessageActivity.USER_KEY)
                val uid = FirebaseAuth.getInstance().uid

                if(chatMessage != null) {
                    Log.e("chat",chatMessage.text)

                    if(chatMessage.fromId == uid && user.uid == chatMessage.toId)
                    {
                        val currentUser =
                            messageActivity.currentUser
                        adapter.add(
                            ChatToItem(
                                chatMessage.text,
                                currentUser!!
                            )
                        )
                        recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)
                        if(chatMessage.imageUrl!="") {
                            adapter.add(loadImagesInChatLog(chatMessage))
                        }

                    }
                    else if(chatMessage.toId == uid && user.uid == chatMessage.fromId){

                        adapter.add(
                            ChatFromItem(
                                chatMessage.text,
                                user.imageUrl
                            )
                        )
                        recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)
                        if(chatMessage.imageUrl!="") {
                            adapter.add(loadImagesInChatLog(chatMessage))
                        }

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
        val user = intent.getParcelableExtra<Users>(newmessageActivity.USER_KEY)
        val toId = user.uid
        if(imageUrl == null){
            imageUrl =""
        }


        if(fromId == null )   return
        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()


        val chatMessage =
            ChatMessage(
                ref.key!!,
                text,
                fromId,
                toId,
                System.currentTimeMillis() / 1000,
                imageUrl!!
            )

        ref.setValue(chatMessage)
            .addOnSuccessListener {
                enter_message_chat_log.text.clear()
                recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)
                Log.d("imageshare","image successfully saved in database with url : $imageUrl")
            }

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/new-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestToMessageRef = FirebaseDatabase.getInstance().getReference("/new-messages/$toId/$fromId")
        latestToMessageRef.setValue(chatMessage)
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

class loadImagesInChatLog( val chatMessage: ChatLogActivity.ChatMessage): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.send_image_in_chat_log
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val uri= chatMessage.imageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.imageView_chat_log)


    }

}

