package com.sample.playersample.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.Player
import com.sample.playersample.databinding.ActivityMainBinding
import com.sample.playersample.module.InfoListener
import com.sample.playersample.module.SectionListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setListener()
        initObserve()

        mViewModel.getVideoUrl()
    }

    override fun onStart() {
        super.onStart()
        binding.apply {
            videoView.startPlayer(infoListener, sectionListener) { player ->
                if (player != null) binding.videoView.setView(player)
            }
            videoView.onResume()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.apply {
            videoView.onPause()
            videoView.clearPlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoView.onRemoveView()
    }

    private fun setListener() {
        binding.videoView.setClickListener {
            if (binding.videoView.isPlaying()) {
                binding.videoView.pausePlayer()
            } else {
                binding.videoView.resumePlayer()
            }
        }
    }

    private fun initObserve() {
        mViewModel.data.observe(this, { movie ->
            binding.videoView.setMovies(movie)
            binding.videoView.loadNext()
        })
    }

    private val infoListener = object : InfoListener() {
        override fun onVideoStateChange(isRunning: Boolean, playState: Int) {
            super.onVideoStateChange(isRunning, playState)
            when (playState) {
                Player.STATE_READY -> {
                    binding.videoView.resumeTimer()
                }
                Player.STATE_ENDED -> {
                    binding.videoView.pauseTimer()
                    binding.videoView.loadNext()
                }
            }
        }

        override fun onVideoTime(hour: Long, minute: Long, second: Long) {
            super.onVideoTime(hour, minute, second)
            if (!binding.videoView.isPlaying()) return
            binding.tvTime.text = "${"%02d".format(minute)}:${"%02d".format(second)}"
        }

        override fun onVideoFrameTime(presentationTime: Long) {
            super.onVideoFrameTime(presentationTime)
        }
    }

    private val sectionListener = object : SectionListener {
        override fun onVideoSection(type: SectionType) {
            when (type) {
                // 영상 초기화
                SectionType.INIT -> {
                    if (binding.videoView.getSectionType() == SectionType.INIT) return
                    Log.d("debug", "INIT")
                }
                // 영상 시작
                SectionType.START -> {
                    if (binding.videoView.getSectionType() == SectionType.START) return
                    Log.d("debug", "START")
                }
                // 영상 종료
                SectionType.END -> {
                    if (binding.videoView.getSectionType() == SectionType.END) return
                    Log.d("debug", "END")
                }
            }
            binding.videoView.setSectionType(type)
        }
    }
}