package com.dexcom.timepoc.timer


/**
 * Use this timer for tasks which require millisecond precision
 */
interface AppTimer {

    fun startTimer(
        countDownTimeMs: Long,
        countDownIntervalMs: Long,
        onTick: (() -> Unit)? = null,
        onTimerFinished: (() -> Unit)? = null,
    )

    fun cancel()
}



