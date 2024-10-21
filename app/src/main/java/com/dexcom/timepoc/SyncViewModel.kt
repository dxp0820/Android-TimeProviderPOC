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
import java.text.SimpleDateFormat
import java.util.Date

class SyncViewModel(private val application: Application) : ViewModel() {

    private val androidClock = AndroidClockFactory.createDeviceClock()
    private val _ntpTimeState = mutableStateOf("")
    val ntpTimeState: State<String> = _ntpTimeState

    private val _timeState = mutableStateOf("")
    val timeState: State<String> = _timeState

    private val _syncResult = mutableStateOf(false)
    val syncResult: State<Boolean> = _syncResult

    private val appTimer: AppTimer

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
        appTimer.startTimer(
            Long.MAX_VALUE,
            1000,
            ::updateUi
        )
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