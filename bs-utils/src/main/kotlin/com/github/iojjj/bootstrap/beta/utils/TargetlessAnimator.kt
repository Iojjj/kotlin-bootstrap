package com.github.iojjj.bootstrap.beta.utils

import android.animation.Animator
import android.animation.TimeInterpolator
import android.os.Build
import android.support.annotation.RequiresApi
import java.util.*

class TargetlessAnimator(private val delegate: Animator) : Animator() {

    override fun isRunning(): Boolean = delegate.isRunning

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun addPauseListener(listener: AnimatorPauseListener?) = delegate.addPauseListener(listener)

    override fun setTarget(target: Any?) {

    }

    override fun getDuration(): Long = delegate.duration

    override fun getStartDelay(): Long = delegate.startDelay

    override fun addListener(listener: AnimatorListener?) = delegate.addListener(listener)

    override fun getListeners(): ArrayList<AnimatorListener> = delegate.listeners

    override fun setStartDelay(startDelay: Long) {
        delegate.startDelay = startDelay
    }

    override fun cancel() = delegate.cancel()

    override fun removeListener(listener: AnimatorListener?) = delegate.removeListener(listener)

    override fun start() = delegate.start()

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun getInterpolator(): TimeInterpolator = delegate.interpolator

    override fun clone(): Animator = delegate.clone()

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun resume() = delegate.resume()

    override fun setInterpolator(value: TimeInterpolator?) {
        delegate.interpolator = value
    }

    override fun isStarted(): Boolean = delegate.isStarted

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun removePauseListener(listener: AnimatorPauseListener?) = delegate.removePauseListener(listener)

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun isPaused(): Boolean = delegate.isPaused

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun pause() = delegate.pause()

    override fun end() = delegate.end()

    override fun setDuration(duration: Long): Animator = delegate.setDuration(duration)

    override fun setupStartValues() = delegate.setupStartValues()

    override fun removeAllListeners() = delegate.removeAllListeners()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getTotalDuration(): Long = delegate.totalDuration

    override fun setupEndValues() = delegate.setupEndValues()
}