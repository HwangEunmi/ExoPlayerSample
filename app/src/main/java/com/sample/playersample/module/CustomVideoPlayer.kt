package com.sample.playersample.module

import android.content.Context
import com.google.android.exoplayer2.ui.PlayerView

class CustomVideoPlayer(context: Context, pvView: PlayerView?) : BaseVideoPlayer(context, pvView) {

    // 영상 재생시간 Handler
    private var mTimeHandler: PlayerHelper.TimeHandler

    init {
        this.mTimeHandler = PlayerHelper.TimeHandler(this)
    }

    override fun registerPlayer() {
        super.registerPlayer()
        pvView?.player?.addListener(PlayerHelper.onPlayerInfoListener)
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
        pvView?.let {
            it.player.removeListener(PlayerHelper.onPlayerInfoListener)
            it.player.videoComponent?.removeVideoListener(PlayerHelper.onVideoSizeListener)
        }
    }

    /**
     * 영상의 현재 재생위치 리턴하기
     */
    fun getCurrentPosition(): Long = pvView?.player?.currentPosition ?: 0

    /**
     * 영상 사이즈 리턴 Listener 등록하기
     */
    fun addVideoSizeListener() {
        pvView?.let { it.player?.videoComponent?.addVideoListener(PlayerHelper.onVideoSizeListener) }
    }

}