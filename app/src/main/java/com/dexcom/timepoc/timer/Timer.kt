package com.dexcom.timepoc.timer

import android.os.CountDownTimer


class Timer() : AppTimer {

    private var countDownTimer: CountDownTimer? = null

    constructor(timer: CountDownTimer) : this() {
        countDownTimer = timer
    }

    override fun startTimer(
        countDownTimeMs: Long,
        countDownIntervalMs: Long,
        onTick: (() -> Unit)?,
        onTimerFinished: (() -> Unit)?,
    ) {
        initTimer(countDownTimeMs, countDownIntervalMs, onTick, onTimerFinished)
        countDownTimer?.start()
    }

    override fun cancel() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun initTimer(
        countDownTime: Long,
        countDownInterval: Long,
        onTick: (() -> Unit)?,
        onTimerFinished: (() -> Unit)?,
    ) {
        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(countDownTime, countDownInterval) {

                override fun onTick(millisUntilFinished: Long) {
                    onTick?.invoke()
                }

                override fun onFinish() {
                    onTimerFinished?.invoke()
                }
            }

        }
    }

}