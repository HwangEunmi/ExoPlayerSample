package com.sample.playersample.module

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoListener
import java.lang.ref.WeakReference

object PlayerHelper {

    val TAG = "PlayerHelper"

    var mVideoInfoListener: IVideoInfoListener? = null

    /**
     * 영상의 현재 시간을 구하는 Handler
     */
    class TimeHandler : Handler {
        private val mRef: WeakReference<CustomVideoPlayer>

        constructor(context: CustomVideoPlayer) {
            this.mRef = WeakReference<CustomVideoPlayer>(context)
        }

        companion object {
            const val TYPE_VIDEO_TIME = 0
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val context: CustomVideoPlayer? = mRef.get()
            context?.let {
                when (msg.what) {
                    TYPE_VIDEO_TIME -> {
                        it.pvView?.player?.currentPosition?.let { ct ->
                            getCurrentTime(ct)
                        }
                        sendEmptyMessageDelayed(TYPE_VIDEO_TIME, 1000)
                    }
                    else -> return
                }
            }
        }

        private fun getCurrentTime(duration: Long) {
            val totalSeconds = duration / 1000
            val second = totalSeconds % 60
            val minute = (totalSeconds / 60) % 60
            val hour = totalSeconds / 3600
            mVideoInfoListener?.let { it.onVideoTime(hour, minute, second) }
        }
    }


    val onPlayerInfoListener = object : Player.EventListener {
        override fun onPlayerStateChanged(isStateRunning: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(isStateRunning, playbackState)
            mVideoInfoListener?.let {
                it.onVideoStateChange(playbackState, isStateRunning)
            }
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            super.onPlayerError(error)
            Log.d(TAG, "onPlayerError: " + error?.message)
        }
    }

    val onVideoSizeListener = object : VideoListener {
        override fun onVideoSizeChanged(
            width: Int,
            height: Int,
            unappliedRotationDegrees: Int,
            pixelWidthHeightRatio: Float
        ) {
            super.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
            mVideoInfoListener?.let { it.onVideoSize(width, height, pixelWidthHeightRatio) }
        }
    }

    /**
     * 비디오 관련 정보 Listener 등록하기
     */
    fun setIVideoInfoListener(listener: IVideoInfoListener) {
        mVideoInfoListener = listener
    }


    /**
     * 비디오 Action Listener
     *
     * @see initPlayer : 플레이어를 초기화한다.
     * @see registerPlayer : 플레이어를 등록한다.
     * @see resumeVideo : 영상을 재생시킨다.
     * @see pauseVideo : 영상을 일시정지시킨다.
     * @see releaseVideo : 영상을 종료한다.
     * @see loadUrl : URL을 통해 영상을 불러온다.
     * @see isStateRunning : 현재 영상의 재생상태를 리턴한다.
     * @param loadError : 영상을 로드하는 중에 에러발생을 알린다.
     */
    interface IVideoActionListener {
        fun initPlayer(context: Context)
        fun registerPlayer()
        fun resumeVideo()
        fun pauseVideo()
        fun releaseVideo()
        fun loadUrl(url: String)
        fun isStateRunning(): Boolean
        fun loadError(context: Context)
    }

    /**
     * 비디오 관련 정보 Listener
     *
     * @see onVideoStateChange : 영상 재생상태를 리턴한다.
     * @see onVideoTime : 영상 재생시간을 리턴한다.
     * @see onVideoSize : 영상 사이즈를 리턴한다.
     */
    abstract class IVideoInfoListener {
        open fun onVideoStateChange(playState: Int, isRunning: Boolean) {}
        open fun onVideoTime(hour: Long, minute: Long, second: Long) {}
        open fun onVideoSize(width: Int, height: Int, pixelWidthHeightRatio: Float) {}
    }

}