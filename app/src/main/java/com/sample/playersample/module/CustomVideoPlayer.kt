package com.sample.playersample.module

import android.content.Context
import com.google.android.exoplayer2.ui.PlayerView

class CustomVideoPlayer : BaseVideoPlayer {

    // 영상 재생시간 Handler
    private var mTimeHandler: PlayerHelper.TimeHandler


    constructor(context: Context, videoView: PlayerView) : super(context, videoView) {
        this.mTimeHandler = PlayerHelper.TimeHandler(this)
    }

    override fun registerPlayer() {
        super.registerPlayer()
        mPvView?.player?.addListener(PlayerHelper.onPlayerInfoListener)
    }

    override fun resumeVideo() {
        super.resumeVideo()
        if (!mTimeHandler.hasMessages(PlayerHelper.TimeHandler.TYPE_VIDEO_TIME)) {
            mTimeHandler.sendEmptyMessage(PlayerHelper.TimeHandler.TYPE_VIDEO_TIME)
        }
    }

    override fun pauseVideo() {
        super.pauseVideo()
        mTimeHandler.removeCallbacksAndMessages(null)
    }

    override fun releaseVideo() {
        super.releaseVideo()
        mPvView?.let {
            it.player.removeListener(PlayerHelper.onPlayerInfoListener)
            it.player.videoComponent?.removeVideoListener(PlayerHelper.onVideoSizeListener)
        }
    }

    /**
     * 영상의 현재 재생위치 리턴하기
     */
    fun getCurrentPosition(): Long = mPvView?.player?.currentPosition ?: 0

    /**
     * 영상 사이즈 리턴 Listener 등록하기
     */
    fun addVideoSizeListener() {
        mPvView?.let { it.player?.videoComponent?.addVideoListener(PlayerHelper.onVideoSizeListener) }
    }

    /**
     * 비디오 관련 정보 Listener 등록하기
     */
    fun setIVideoInfoListener(listener: PlayerHelper.IVideoInfoListener) {
        PlayerHelper.mVideoInfoListener = listener
    }

}