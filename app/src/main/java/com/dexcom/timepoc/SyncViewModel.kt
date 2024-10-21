package com.dexcom.timepoc

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lyft.kronos.AndroidClockFactory
import com.lyft.kronos.KronosClock
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dexcom.timepoc.timer.AppTimer
import com.dexcom.timepoc.timer.Timer
import com.lyft.kronos.SyncListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncViewModel(private val application: Application) : ViewModel() {

    val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    private val androidClock = AndroidClockFactory.createDeviceClock()
    private val _ntpTimeState = mutableStateOf("")
    val ntpTimeState: State<String> = _ntpTimeState

    private val _timeState = mutableStateOf("")
    val timeState: State<String> = _timeState

    private var timeGovDate: Date? = null
    private val _timeGovState = mutableStateOf<String>("")
    val timeGovState: State<String> = _timeGovState

    private val _timeGovSyncResult = mutableStateOf(false)
    val timeGovSyncResult = _timeGovSyncResult

    private val _syncResult = mutableStateOf(false)
    val syncResult: State<Boolean> = _syncResult

    private val appTimer: AppTimer

    private val client = OkHttpClient()

    private val syncListener = object : SyncListener {
        override fun onError(host: String, throwable: Throwable) {
            Log.e(TAG, "host:$host ${throwable.message}", throwable)
            _syncResult.value = false
        }

        override fun onStartSync(host: String) {
            Log.d(TAG, "onStartSync $host")
        }

        override fun onSuccess(ticksDelta: Long, responseTimeMs: Long) {
            Log.d(TAG, "onSuccess($ticksDelta, $responseTimeMs)")
            _syncResult.value = true
        }
    }
    private val kronosClock: KronosClock = AndroidClockFactory.createKronosClock(
        context = application,
        syncListener = syncListener,
    )

    // Define ViewModel factory in a companion object
    companion object {
        const val TAG = "SyncViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = (this[APPLICATION_KEY] as TimePocApplication)
                SyncViewModel(
                    application = app
                )
            }
        }
    }

    init {
        kronosClock.syncInBackground()
        appTimer = Timer()
        syncTimeGov()
        appTimer.startTimer(
            Long.MAX_VALUE,
            1000,
            ::updateUi
        )
    }

    fun syncTimeGov() {
        timeGovRequest()
    }

    private fun timeGovRequest() = viewModelScope.launch(Dispatchers.Default) {
        try {
            val request = Request.Builder()
                .url("https://time.gov")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                withContext(Dispatchers.Main) {
                    _timeGovSyncResult.value = true
                }
                val dateHeader = response.header("Date")
                if (dateHeader != null) {
                    val date = sdf.parse(dateHeader)
                    timeGovDate = date
                }

                Log.d(TAG, "Date: $dateHeader")
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            _timeGovSyncResult.value = false
        }
    }

    private fun updateUi() {
        _timeState.value =
            SimpleDateFormat.getTimeInstance().format(Date(androidClock.getCurrentTimeMs()))
        val ntpTimeMs = kronosClock.getCurrentNtpTimeMs()
        if (ntpTimeMs != null) {
            _ntpTimeState.value = SimpleDateFormat.getTimeInstance().format(Date(ntpTimeMs))
        } else {
            _ntpTimeState.value = "null"
        }
        timeGovDate?.let { date ->
            timeGovDate = Date(date.time + 1000)
            _timeGovState.value = sdf.format(date)
        } ?: kotlin.run { _timeGovState.value = "No data" }

    }

    fun sync() {
        _syncResult.value = false
        viewModelScope.launch {
            val job = viewModelScope.async(Dispatchers.Default) {
                kronosClock.sync()
            }
            _syncResult.value = job.await()
        }
    }
}