package com.sample.playersample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sample.playersample.model.MainModel

class MainViewModel : ViewModel() {

    private val _data = MutableLiveData<MainModel>()
    val data: LiveData<MainModel>
        get() = _data


    fun getVideoUrl() {
        // Rest API 호출후 ...
        val url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        _data.value = MainModel(url)
    }

}