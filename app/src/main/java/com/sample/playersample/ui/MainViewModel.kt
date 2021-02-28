package com.sample.playersample.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sample.playersample.model.MovieModel

class MainViewModel : ViewModel() {

    private val _data = MutableLiveData<List<MovieModel>>()
    val data: LiveData<List<MovieModel>>
        get() = _data


    fun getVideoUrl() {
        // Rest API 호출후 ...
        val url =
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        val list = mutableListOf<MovieModel>()
        for (index in 1..3) {
            list.add(MovieModel(url))
        }
        _data.value = list
    }
}

enum class SectionType {
    INIT,
    START,
    END
}