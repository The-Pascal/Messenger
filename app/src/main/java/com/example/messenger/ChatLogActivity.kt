package com.example.messenger

import android.content.ClipData
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import com.example.messenger.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)



        val user = intent.getParcelableExtra<Users>(newmessageActivity.USER_KEY)
        supportActionBar?.title = user.username

        send_button_chat_log.setOnClickListener{
            performSendMessage()
        }

       setupDummyData()
    }

    class ChatMessage(val id : String , val fromId : String ,)

    private fun performSendMessage(){
        val text = enter_message_chat_log.text.toString()

        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()

        val chatMessage = ChatMessage(text)
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("chatmessage","this is message ")
            }

    }

    private fun setupDummyData(){
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(ChatFromItem("this is new msg "))
        adapter.add(ChatToItem("tis is another mesaage "))
        adapter.add(ChatFromItem("what about now "))
        adapter.add(ChatToItem("cool"))

        recyclerView_chat_log.adapter = adapter
    }
}

class ChatFromItem(val text: String): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text
    }
    override fun getLayout(): Int {

        return R.layout.chat_from_row

    }

}

class ChatToItem(val text:String): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text
    }
    override fun getLayout(): Int {

        return R.layout.chat_to_row

    }

}

