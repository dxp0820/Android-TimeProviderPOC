package com.dexcom.timepoc

import android.content.Context
import android.os.SystemClock
import com.dexcom.timepoc.models.TimePoc
import com.google.gson.Gson

class SharedPrefsHelper(private val context: Context) {

    companion object {
        const val KEY_TIME = "key_time"
        const val KEY_RESYNC = "resync"
        const val PREFS_KEY = "preferences_key"
    }

    private var systemReboot = false

    private val sharedPrefs =
        context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    val gson = Gson()

    init {
        systemReboot = sharedPrefs.getBoolean(KEY_RESYNC, false)
    }

    fun saveUtcTimestamp(timestampMs: Long) {
        val timePoc = TimePoc(
            utcTimeStamp = timestampMs,
            elapsedReadTime = SystemClock.elapsedRealtime(),
        )
        sharedPrefs.edit().putBoolean(KEY_RESYNC,false).apply()
        val jsonStr = gson.toJson(timePoc)
        sharedPrefs.edit().putString(KEY_TIME, jsonStr).apply()
    }

    fun getTimePoc(): TimePoc? {
        val currentTimePoc = sharedPrefs.getString(KEY_TIME, null)?.let {
            gson.fromJson(it, TimePoc::class.java)
        }
        return currentTimePoc
    }

    fun needsResync() : Boolean {
        return sharedPrefs.getBoolean(KEY_RESYNC,true)
    }

}