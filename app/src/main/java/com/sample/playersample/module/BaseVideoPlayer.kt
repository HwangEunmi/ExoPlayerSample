package com.sample.playersample.module

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import java.util.*

open class BaseVideoPlayer : ControlListener {

    private var exoPlayer: SimpleExoPlayer? = null

    override fun initPlayer(context: Context) {
        if (exoPlayer != null) return

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30000,
                30000,
                4000,
                4000
            )
            .setTargetBufferBytes(-1)
            .createDefaultLoadControl()

        val trackSelector = DefaultTrackSelector()
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(
            context.applicationContext,
            DefaultRenderersFactory(context),
            trackSelector,
            loadControl
        )
    }

    override fun resumeVideo() {
        if (getPlayState()) return
        exoPlayer?.let {
            if (!it.playWhenReady)
                it.playWhenReady = true
        }
    }

    override fun pauseVideo() {
        if (!getPlayState()) return
        exoPlayer?.let {
            if (it.playWhenReady)
                it.playWhenReady = false
        }
    }

    override fun releaseVideo() {
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun load(millis: Long, url: String) {
        exoPlayer?.seekTo(millis * 1000)
        val mediaSource = buildMediaSource(Uri.parse(url))
        exoPlayer?.prepare(mediaSource, false, true)
    }

    override fun seekTo(millis: Long) {
        exoPlayer?.seekTo(millis * 1000)
    }

    override fun setPlayState(isRunning: Boolean) {
        exoPlayer?.playWhenReady = isRunning
    }

    override fun getPlayState(): Boolean {
        return exoPlayer?.playWhenReady ?: false
    }

    override fun getPlayer() = exoPlayer


    /**
     * 타입별 따른 영상 MediaSource 리턴하기
     */
    private fun buildMediaSource(uri: Uri): MediaSource {
        val userAgent = "KakaoVXPlayer"
        val extension = getDeterminedExtension(uri.lastPathSegment)
        return when (extension) {
            ExtensionType.HLS ->
                HlsMediaSource.Factory(
                    DefaultHttpDataSourceFactory(
                        userAgent
                    )
                ).createMediaSource(uri)
            ExtensionType.MPD -> {
                val dashChunkSourceFactory = DefaultDashChunkSource.Factory(
                    DefaultHttpDataSourceFactory(
                        "ua",
                        DefaultBandwidthMeter()
                    )
                )
                val manifestDataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
                DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory)
                    .createMediaSource(uri)
            }
            ExtensionType.PROGRESS -> ExtractorMediaSource.Factory(
                DefaultHttpDataSourceFactory(userAgent)
            ).createMediaSource(uri)
            ExtensionType.UNDEFIEND -> HlsMediaSource.Factory(
                DefaultHttpDataSourceFactory(userAgent)
            ).createMediaSource(uri)
        }
    }

    private fun getDeterminedExtension(extension: String? = null): ExtensionType {
        extension ?: return ExtensionType.UNDEFIEND
        val ext = extension.toLowerCase(Locale.getDefault())
        return if (ext.contains("m3u8")) ExtensionType.HLS
        else if (ext.contains("mp3") || extension.contains("mp4")) ExtensionType.PROGRESS
        else if (ext.contains("mpd") || ext.contains("mp")) ExtensionType.MPD
        else ExtensionType.UNDEFIEND
    }

}

enum class ExtensionType(val value: String) {
    HLS(MimeTypes.APPLICATION_M3U8),
    MPD(MimeTypes.APPLICATION_MPD),
    PROGRESS(MimeTypes.VIDEO_MP4),
    UNDEFIEND(MimeTypes.APPLICATION_M3U8)
}

/**
 * 비디오 Control Listener
 *
 * @see initPlayer : 플레이어를 초기화한다.
 * @see resumeVideo : 영상을 재생시킨다.
 * @see pauseVideo : 영상을 일시정지시킨다.
 * @see releaseVideo : 영상을 종료한다.
 * @see load : 영상을 시작지점부터 재생한다.
 * @see setPlayState : 재생상태를 셋팅한다.
 * @see getPlayState : 재생상태를 리턴한다.
 * @see seekTo : 해당 지점으로 이동한다.
 * @see getPlayer : 플레이어를 리턴한다.
 */
interface ControlListener {
    fun initPlayer(context: Context)
    fun resumeVideo()
    fun pauseVideo()
    fun releaseVideo()
    fun load(millis: Long, url: String)
    fun seekTo(millis: Long)
    fun setPlayState(isRunning: Boolean)
    fun getPlayState(): Boolean
    fun getPlayer(): Player?
}