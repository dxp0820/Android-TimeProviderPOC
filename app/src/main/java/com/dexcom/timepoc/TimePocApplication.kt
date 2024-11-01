package com.dexcom.timepoc

import android.app.Application

class TimePocApplication : Application() {

    override fun onCreate() {
        super.onCreate()
//        registerReceiver(BootBroadcastReceiver(), IntentFilter(Intent.ACTION_LOCKED_BOOT_COMPLETED))
    }
}