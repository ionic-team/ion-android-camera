package io.ionic.libs.ioncameralib.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import io.ionic.libs.ioncameralib.R

class IONCAMRLoadingActivity : ComponentActivity() {

    companion object {
        const val DISMISS_INTENT_FILTER = "LoadingActivity_Dismiss"
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        // starting on API 34, we have to specify if the broadcast is exported or not
        // or the call to registerReceiver will result in a crash
        // we need RECEIVER_EXPORTED so that the receiver can be called from outside the Activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(dismissBroadcastReceiver, IntentFilter(DISMISS_INTENT_FILTER), Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(dismissBroadcastReceiver, IntentFilter(DISMISS_INTENT_FILTER))
        }
        enableEdgeToEdge()
    }

    private val dismissBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dismissBroadcastReceiver)
    }

}