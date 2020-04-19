package com.example.messenger

import android.content.ClipData
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import com.example.messenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.Group
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.user_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() ,
Animation.AnimationListener{

    val adapter = GroupAdapter<ViewHolder>()


    private lateinit var animFadeIn: Animation
    private lateinit var animFadeOut: Animation
    private lateinit var animFadeInOut: Animation

    private lateinit var animZoomIn: Animation
    private lateinit var animZoomOut: Animation

    private lateinit var animLeftRight: Animation
    private lateinit var animRightLeft: Animation
    private lateinit var animTopBottom: Animation

    private lateinit var animBounce: Animation
    private lateinit var animFlash: Animation

    private lateinit var animRotateLeft: Animation
    private lateinit var animRotateRight: Animation


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

        loadanimationTransition()

      // setupDummyData()
    }

    private fun loadanimationTransition(){
        animLeftRight = AnimationUtils.loadAnimation(
            this, R.anim.left_right)
        animRightLeft = AnimationUtils.loadAnimation(
            this, R.anim.right_left)
        animFadeIn = AnimationUtils.loadAnimation(
            this, R.anim.fade_in)
        //animFadeIn.setAnimationListener(this)
        animFadeOut = AnimationUtils.loadAnimation(
            this, R.anim.fade_out)
        animFadeInOut = AnimationUtils.loadAnimation(
            this, R.anim.fade_in_out)

        animZoomIn = AnimationUtils.loadAnimation(
            this, R.anim.zoom_in)
        animZoomOut = AnimationUtils.loadAnimation(
            this, R.anim.zoom_out)

        animLeftRight = AnimationUtils.loadAnimation(
            this, R.anim.left_right)
        animRightLeft = AnimationUtils.loadAnimation(
            this, R.anim.right_left)
        animTopBottom = AnimationUtils.loadAnimation(
            this, R.anim.top_bot)

        animBounce = AnimationUtils.loadAnimation(
            this, R.anim.bounce)
        animFlash = AnimationUtils.loadAnimation(
            this, R.anim.flash)

        animRotateLeft = AnimationUtils.loadAnimation(
            this, R.anim.rotate_left)
        animRotateRight = AnimationUtils.loadAnimation(
            this, R.anim.rotate_right)

    }


    private fun listenForMessages(){

        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)


                val user = intent.getParcelableExtra<Users>(newmessageActivity.USER_KEY)
                val uid = FirebaseAuth.getInstance().uid
                val databaseref = FirebaseDatabase.getInstance().getReference("/Users")
                val imageUrl :String ?= null



                if(chatMessage != null) {
                    Log.e("chat",chatMessage.text)

                    if(chatMessage.fromId == uid && user.uid == chatMessage.toId)
                    {
                        val currentUser = messageActivity.currentUser
                        adapter.add(ChatToItem(chatMessage.text , currentUser!!))
                        recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)

                    }
                    else if(chatMessage.toId == uid && user.uid == chatMessage.fromId){

                        adapter.add(ChatFromItem(chatMessage.text ,user.imageUrl))
                        recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)
                    }
                    else{
                    }

                }

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }
    class ChatMessage(val id : String ,val text: String, val fromId : String , val toId : String , val timestamp: Long)
    {
        constructor(): this("","","","",-1)
    }

    private fun performSendMessage(){
        val text = enter_message_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<Users>(newmessageActivity.USER_KEY)
        val toId = user.uid

        if(fromId == null )   return

        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()

        val chatMessage = ChatMessage(ref.key!!,text , fromId, toId, System.currentTimeMillis() /1000)
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                enter_message_chat_log.text.clear()
                recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)
                Log.d("chatmessage","this is message ")
            }

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/new-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestToMessageRef = FirebaseDatabase.getInstance().getReference("/new-messages/$toId/$fromId")
        latestToMessageRef.setValue(chatMessage)



    }

    override fun onAnimationRepeat(animation: Animation?) {}

    override fun onAnimationEnd(animation: Animation?) {}

    override fun onAnimationStart(animation: Animation?) {}

}


private fun Animation.setAnimationListener(childEventListener: ChildEventListener) {

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

class ChatToItem(val text:String, val user: Users): Item<ViewHolder>(), Animation.AnimationListener {

    private lateinit var animLeftRight: Animation
    private lateinit var animRightLeft: Animation



    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text
        val uri = user.imageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.imageView_to_row)

    }

    override fun getLayout(): Int {

        return R.layout.chat_to_row


    }

    override fun onAnimationRepeat(animation: Animation?) {

    }

    override fun onAnimationEnd(animation: Animation?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAnimationStart(animation: Animation?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

