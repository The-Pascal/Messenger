package com.example.messenger.show_images

import android.app.AlertDialog
import android.app.Dialog
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.messenger.R
import com.example.messenger.chatLog.ChatLogActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.show_image_dialog.view.*

class show_images_dialog : DialogFragment() {

    /*var chatMessage: ImageView ? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(this.activity!!)
        val inflater = activity!!.layoutInflater

        val dialogView = inflater.inflate(R.layout.show_image_dialog,null)

        var show_image = dialogView.findViewById<ImageView>(R.id.imageView_show_image_on_fullscreen)

        show_image.setImageResource(chatMessage.id)

        builder.setView(dialogView).setMessage("Image")


        return builder.create()

    }
    fun sendImageSelected(chatMessage1: ImageView){

        chatMessage= chatMessage1


    }*/
}