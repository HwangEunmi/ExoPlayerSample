package com.sample.playersample.module

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.video.VideoFrameMetadataListener
import com.google.android.exoplayer2.video.VideoListener
import com.sample.playersample.model.MovieModel
import com.sample.playersample.ui.SectionType
import java.lang.ref.WeakReference

class VideoPlayer : BaseVideoPlayer() {
    private var handler: UiHandler

    private var infoListener: InfoListener? = null
    private var sectionListener: SectionListener? = null

    var uiType = TYPE_VIDEO
    var sectionType: SectionType = SectionType.END
    private var movie: MovieModel? = null

    init {
        this.handler = UiHandler(this)
    }

    companion object {
        const val TYPE_VIDEO = 0
    }

    /**
     * 플레이어 리스너 추가하기
     */
    fun addListener(listener: InfoListener, sectionListener: SectionListener) {
        this.infoListener = listener
        this.sectionListener = sectionListener
        getPlayer()?.addListener(onInfoListener)
        getPlayer()?.videoComponent?.addVideoListener(onVideoSizeListener)
        getPlayer()?.videoComponent?.setVideoFrameMetadataListener(onFrameMetaDataListener)
    }

    fun isPlaying() = getPlayer()?.playWhenReady

    override fun resumeVideo() {
        super.resumeVideo()
        this.uiType = TYPE_VIDEO
        resumeTimer()
    }

    override fun pauseVideo() {
        super.pauseVideo()
        pauseTimer()
    }

    fun resumeTimer() {
        if (!handler.hasMessages(uiType)) {
            val msg = handler.obtainMessage()
            msg.obj = command
            handler.sendMessage(msg)
        }
    }

    fun pauseTimer() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun releaseVideo() {
        getPlayer()?.removeListener(onInfoListener)
        getPlayer()?.videoComponent?.removeVideoListener(onVideoSizeListener)
        getPlayer()?.videoComponent?.clearVideoFrameMetadataListener(onFrameMetaDataListener)
        this.infoListener = null
        this.sectionListener = null
        this.movie = null
        super.releaseVideo()
    }

    /**
     * 영상 불러오기
     */
    fun loadVideo(
        millis: Long,
        movie: MovieModel,
    ) {
        this.movie = movie
        load(millis, movie.url)
    }

    /**
     * 영상 재생이 완료되었을때 호출한다.
     */
    fun onComplete() {
        sectionListener?.onVideoSection(SectionType.END)
    }


    var flagRunning = false
    private val onInfoListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            when (playbackState) {
                Player.STATE_BUFFERING -> flagRunning = false
                Player.STATE_READY -> {
                    if (flagRunning) return
                    flagRunning = true
                    infoListener?.onVideoStateChange(playWhenReady, Player.STATE_READY)
                }
                Player.STATE_ENDED -> {
                    if (!flagRunning) return
                    flagRunning = false

                    sectionType = SectionType.INIT
                    infoListener?.onVideoStateChange(playWhenReady, Player.STATE_ENDED)
                }
            }
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            super.onTimelineChanged(timeline, manifest, reason)
            if (reason == 1) {
                infoListener?.onVideoEnd()
            }
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            super.onPlayerError(error)
            Log.d("debug", "onPlayerError: " + error?.message)
        }
    }

    private val onVideoSizeListener = object : VideoListener {
        override fun onVideoSizeChanged(
            width: Int,
            height: Int,
            unappliedRotationDegrees: Int,
            pixelWidthHeightRatio: Float
        ) {
            super.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
            val width = (width * pixelWidthHeightRatio).toInt()
            val height = (height * pixelWidthHeightRatio).toInt()
            infoListener?.onVideoSize(width, height)
        }
    }

    private val onFrameMetaDataListener =
        VideoFrameMetadataListener { time, _, _ ->
            infoListener?.onVideoFrameTime(
                time
            )
        }

    private val command = object : UiHandler.Command {
        override fun execute() {
            if (movie == null) return
            when (uiType) {
                TYPE_VIDEO -> {
                    getCurrentTime(getPlayer()?.currentPosition ?: -1L)

                    val pos = getPlayer()?.currentPosition ?: -1L
                    when (Math.round(pos?.div(1000.0))) {
                        0L -> {
                            sectionListener?.onVideoSection(
                                SectionType.START,
                            )
                        }
                    }

                    handler.send()
                }
            }
        }
    }

    private fun getCurrentTime(duration: Long) {
        val totalSeconds = duration / 1000
        val second = totalSeconds % 60
        val minute = (totalSeconds / 60) % 60
        val hour = totalSeconds / 3600
        infoListener?.let { it.onVideoTime(hour, minute, second) }
    }


    class UiHandler(context: VideoPlayer) : Handler(Looper.getMainLooper()) {
        private val ref: WeakReference<VideoPlayer> = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.obj is Command) {
                (msg.obj as Command).execute()
            }
        }

        fun send() {
            val context: VideoPlayer? = ref.get()
            context?.let {
                val msg = obtainMessage()
                msg.obj = it.command
                sendMessageDelayed(msg, 1000)
            }
        }

        interface Command {
            fun execute()
        }
    }
}

/**
 * 비디오 Section Listener
 *
 * @see type : 구간별 타입
 */
interface SectionListener {
    fun onVideoSection(type: SectionType)
}

/**
 * 비디오 Info Listener
 *
 * @see onVideoStateChange : 영상 재생상태를 리턴한다.
 * @see onVideoTime : 영상 재생시간을 리턴한다.
 * @see onVideoSize : 영상 사이즈를 리턴한다.
 * @see onVideoFrameTime : 실시간 영상 시간을 리턴한다.
 * @see onVideoEnd : 영상이 종료될 때 호출한다.
 */
abstract class InfoListener {
    open fun onVideoStateChange(isRunning: Boolean, playState: Int) {}
    open fun onVideoTime(hour: Long, minute: Long, second: Long) {}
    open fun onVideoSize(width: Int, height: Int) {}
    open fun onVideoFrameTime(presentationTime: Long) {}
    open fun onVideoEnd() {}
}