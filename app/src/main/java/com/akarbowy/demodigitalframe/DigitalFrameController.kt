package com.akarbowy.demodigitalframe

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.VideoView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bumptech.glide.Glide
import java.util.*
import java.util.concurrent.TimeUnit

class DigitalFrameController(
    lifecycle: Lifecycle,
    private val video: VideoView,
    private val image: ImageView
) : LifecycleObserver {

    private val media = mutableListOf<MediaFile>()

    private var currentPosition = NO_POSITION

    private val timer = Timer()

    private var timerHandler: Handler = Handler(Looper.getMainLooper())

    private var task: ShowMediaTask? = null

    init {
        lifecycle.addObserver(this)

        video.setOnPreparedListener { player ->
            run {
                player.isLooping = true
                player.setVolume(0f, 0f)
            }
        }
    }

    fun setMedia(media: List<MediaFile>) {
        this.media.clear()
        this.media.addAll(media)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun play() {
        if (media.isNotEmpty()) {
            task = ShowMediaTask()
            timer.scheduleAtFixedRate(task, 0, MEDIA_ON_SCREEN_PERIOD)
        }
    }

    private inner class ShowMediaTask : TimerTask() {

        override fun run() {
            timerHandler.post {
                invalidatePosition()
                show(media[currentPosition])
            }
        }

        private fun invalidatePosition(): Int {
            if (currentPosition + 1 == media.size) {
                currentPosition = 0
            } else {
                currentPosition++
            }

            return currentPosition
        }

        private fun show(media: MediaFile) {
            if (media.type == MediaFile.Type.VIDEO) {
                image.visibility = View.GONE
                video.visibility = View.VISIBLE
                video.setVideoURI(media.contentUri)
                video.start()
            } else {
                video.visibility = View.GONE
                image.visibility = View.VISIBLE
                Glide.with(image)
                    .load(media.contentUri)
                    .centerCrop()
                    .into(image)
            }
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        task?.cancel()
        task = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stop() {
        timer.cancel()
    }

    companion object {

        private const val NO_POSITION = -1

        private val MEDIA_ON_SCREEN_PERIOD = TimeUnit.SECONDS.toMillis(6)

    }
}