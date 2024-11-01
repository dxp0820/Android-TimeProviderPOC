package com.dexcom.timepoc.models

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dexcom.timepoc.SharedPrefsHelper

private const val TAG = "BootBroadcastReceiver"
class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPrefs = context.getSharedPreferences(SharedPrefsHelper.PREFS_KEY, Context.MODE_PRIVATE)
        StringBuilder().apply {
            if (intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
                sharedPrefs.edit().putBoolean(SharedPrefsHelper.KEY_RESYNC, true).apply()
                sharedPrefs.edit().putString(SharedPrefsHelper.KEY_TIME,null).apply()
            }
            append("Action: ${intent.action}\n")
            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            toString().also { log ->
                Log.d(TAG, log)
            }
        }
    }

}