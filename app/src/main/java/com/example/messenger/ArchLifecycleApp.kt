package com.example.messenger

import android.app.Application
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.messenger.latestMessages.messageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ArchLifecycleApp : Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            FirebaseDatabase.getInstance().getReference("/Users/$uid/active")
                .setValue(false)
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            FirebaseDatabase.getInstance().getReference("/Users/$uid/active")
                .setValue(true)
        }


    }

}