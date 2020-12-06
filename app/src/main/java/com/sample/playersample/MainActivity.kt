package com.sample.playersample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.Player
import com.sample.playersample.databinding.ActivityMainBinding
import com.sample.playersample.module.CustomVideoPlayer
import com.sample.playersample.module.PlayerHelper

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mViewModel: MainViewModel

    private lateinit var mVideoPlayer: CustomVideoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        initObserve()
        initVideoView()

        mViewModel.getVideoUrl()
    }

    override fun onResume() {
        super.onResume()
        changeStateVideoPlayer(false)
    }

    override fun onPause() {
        changeStateVideoPlayer(true)
        super.onPause()
    }

    override fun onDestroy() {
        mVideoPlayer.releaseVideo()
        changeStateVideoPlayer(true)
        super.onDestroy()
    }


    private fun initObserve() {
        mViewModel.data.observe(this, Observer { data ->
            mVideoPlayer.loadUrl(data.url)
        })
    }

    private fun initVideoView() {
        mVideoPlayer = CustomVideoPlayer(this, mBinding.playerView)
        PlayerHelper.setIVideoInfoListener(object : PlayerHelper.IVideoInfoListener() {
            override fun onVideoStateChange(playState: Int, isStateRunning: Boolean) {
                super.onVideoStateChange(playState, isStateRunning)
                when (playState) {
                    // 재생 준비 완료 (즉시 재생 가능한 상태)
                    Player.STATE_READY -> {
                        changeStateVideoPlayer(false)
                    }
                    // 재생 마침 (재생이 완료된 상태)
                    Player.STATE_ENDED -> {
                        changeStateVideoPlayer(true)
                    }
                    else -> null
                }
            }

            override fun onVideoTime(hour: Long, minute: Long, second: Long) {
                super.onVideoTime(hour, minute, second)
                mBinding.tvTime.post({ mBinding.tvTime.setText("${hour}:${minute}:${second}") })
            }

            override fun onVideoSize(width: Int, height: Int, pixelWidthHeightRatio: Float) {
                super.onVideoSize(width, height, pixelWidthHeightRatio)
            }
        })

        mVideoPlayer.registerPlayer()
        mVideoPlayer.addVideoSizeListener()
    }

    /**
     * 비디오뷰 상태 변경하기
     * @param isSelected : 팝업 플레이 버튼 Select 상태
     */
    private fun changeStateVideoPlayer(isSelected: Boolean) {
        if (isSelected) {
            mVideoPlayer.pauseVideo()
        } else {
            mVideoPlayer.resumeVideo()
        }
    }

}