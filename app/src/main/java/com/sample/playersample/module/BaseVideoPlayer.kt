package com.sample.playersample.module

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

open class BaseVideoPlayer(context: Context, var pvView: PlayerView?) : PlayerHelper.IVideoActionListener {

    // 기본 플레이어
    private var mExoPlayer: SimpleExoPlayer? = null

    private var mExtractorFactory: ExtractorMediaSource.Factory? = null

    init {
        initPlayer(context)
    }

    /**
     * 플레이어 초기화하기
     */
    override fun initPlayer(context: Context) {
        if (mExoPlayer != null) return
        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(2000, 5000, 1500, 2000)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .createDefaultLoadControl()

        val trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory())
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(
            context.applicationContext,
            trackSelector,
            loadControl
        )
        mExtractorFactory = ExtractorMediaSource.Factory(
            DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.applicationInfo.packageName)
            )
        )
    }

    /**
     * 플레이어 등록하기
     */
    override fun registerPlayer() {
        pvView?.player = mExoPlayer
    }

    /**
     * 플레이어 재생하기
     */
    override fun resumeVideo() {
        if (isStateRunning()) return

        pvView?.let {
            if (!it.player.playWhenReady) {
                it.player.playWhenReady = true
            }
        }
    }

    /**
     * 플레이어 일시정지하기
     */
    override fun pauseVideo() {
        if (!isStateRunning()) return

        pvView?.let {
            if (it.player.playWhenReady) {
                it.player.playWhenReady = false
            }
        }
    }

    /**
     * 플레이어 해제하기
     */
    override fun releaseVideo() {
        mExoPlayer?.release()
        mExoPlayer = null
    }

    /**
     * 플레이어의 재생상태 리턴하기
     */
    override fun isStateRunning(): Boolean {
        return pvView?.let { it.player.playWhenReady } ?: true
    }

    /**
     * 영상 로드하는 중에 발생한 에러 알리기
     */
    override fun loadError(context: Context) {}

    /**
     * 해당 URL의 영상 재생하기
     */
    override fun loadUrl(url: String) {
        mExtractorFactory?.let {
            val mediaSource = it.createMediaSource(Uri.parse(url))
            mExoPlayer?.prepare(mediaSource)
        }
    }

}