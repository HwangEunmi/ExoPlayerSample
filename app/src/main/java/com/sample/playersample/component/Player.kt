package com.sample.playersample.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.exoplayer2.SimpleExoPlayer
import com.sample.playersample.databinding.UiPlayerBinding
import com.sample.playersample.model.MovieModel
import com.sample.playersample.module.InfoListener
import com.sample.playersample.module.SectionListener
import com.sample.playersample.module.VideoPlayer
import com.sample.playersample.ui.SectionType

class Player : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var binding: UiPlayerBinding =
        UiPlayerBinding.inflate(LayoutInflater.from(context), this, false)

    var player: VideoPlayer? = null
        private set
    private var playState = true
    var movies: List<MovieModel> = mutableListOf()
        private set
    var index = -1
        private set
    private var beforePoint = 0L


    init {
        addView(binding.root)
    }

    fun setView(player: SimpleExoPlayer?) {
        binding.videoView.player = player
    }

    fun setMovies(list: List<MovieModel>) {
        this.movies = list
    }

    fun setClickListener(listener: OnClickListener) {
        binding.videoView.videoSurfaceView.setOnClickListener(listener)
    }

    fun onResume() = binding.videoView.onResume()

    fun onPause() = binding.videoView.onPause()

    fun onRemoveView() = binding.videoView.overlayFrameLayout?.removeAllViews()

    fun startPlayer(
        infoListener: InfoListener,
        sectionListener: SectionListener,
        callback: (SimpleExoPlayer?) -> Unit
    ) {
        if (player == null) {
            player = VideoPlayer()
            player?.initPlayer(context)
            player?.addListener(infoListener, sectionListener)
            player?.setPlayState(playState)
            checkBeforePoint()
            callback.invoke(player?.getPlayer())
        } else {
            callback.invoke(null)
        }
    }

    fun clearPlayer() {
        val point = player?.getPlayer()?.currentPosition ?: 0L
        beforePoint = Math.max(0, point / 1000)
        playState = player?.getPlayState() ?: false
        player?.pauseTimer()
        player?.releaseVideo()
        player = null
    }

    fun isPlaying() = player?.isPlaying() ?: false

    fun resumePlayer() = player?.resumeVideo()

    fun pausePlayer() = player?.pauseVideo()

    fun resumeTimer() {
        player?.uiType = VideoPlayer.TYPE_VIDEO
        player?.resumeTimer()
    }

    fun pauseTimer() = player?.pauseTimer()

    fun loadNext() {
        if (checkLastMovie()) return
        ++index
        player?.loadVideo(beforePoint, movies[index])
    }

    /**
     * 이전 시작지점부터 시작하기
     */
    private fun checkBeforePoint() {
        if (beforePoint != 0L) {
            player?.loadVideo(beforePoint, movies[index])
            beforePoint = 0L
        }
    }

    /**
     * 구간타입 셋팅하기
     */
    fun setSectionType(type: SectionType) {
        player?.sectionType = type
    }

    fun getSectionType() = player?.sectionType

    /**
     * 마지막 영상인지 확인하기
     */
    private fun checkLastMovie(): Boolean {
        if (index == movies.size.minus(1)) {
            player?.onComplete()
            return true
        }
        return false
    }
}