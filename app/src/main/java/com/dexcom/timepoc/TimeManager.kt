package com.dexcom.timepoc

import android.util.Log
import com.dexcom.timepoc.SyncViewModel.Companion.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeManager {
    private val client = OkHttpClient()
    private val sdfPattern = "EEE, dd MMM yyyy HH:mm:ss z"
    fun gmtTime(utcString : String) : Date? {
        val dateFormat = SimpleDateFormat(sdfPattern, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.parse(utcString)
    }

    fun gmtTime(date : Date) : String {
        val dateFormat = SimpleDateFormat(sdfPattern, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(date)
    }

    fun localTime(date : Date) : String {
        val dateFormat = SimpleDateFormat(sdfPattern, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(date)
    }

    fun timeGovDateHeader() : String? {
        try {
            val request = Request.Builder()
                .url("https://time.gov")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val dateHeader = response.header("Date")
                Log.d(TAG, "Date: $dateHeader")
                return dateHeader
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return null
    }

}