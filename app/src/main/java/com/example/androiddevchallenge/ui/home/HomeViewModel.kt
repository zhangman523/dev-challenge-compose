/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge.ui.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
/**
 * Created by admin on 2021/3/7 19:51.
 * Email: zhangman523@126.com
 */
class HomeViewModel : ViewModel(), Runnable {

    val _timeStr = MutableLiveData<String>()
    val timeStr: LiveData<String> = _timeStr

    val _isPlay = MutableLiveData<Boolean>()
    val isPlay: LiveData<Boolean> = _isPlay

    val _Proportions = MutableLiveData<Float>()
    val proportions: LiveData<Float> = _Proportions

    val _isEnd = MutableLiveData(true)
    val isEnd: LiveData<Boolean> = _isEnd

    val mToastMessage = MutableLiveData<String>()

    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var mSecond: Int = 0
    private var mTotalSecond: Int = 0

    private fun changePlayStatus(status: Boolean) {
        _isPlay.value = status
    }

    /**
     * @param second
     */
    fun start(second: Int) {
        if (_isPlay.value != null && _isPlay.value!!) {
            return
        }
        changePlayStatus(true)
        _isEnd.value = false
        mTotalSecond = second
        mSecond = second
        _timeStr.value = formatTime(mSecond)
        mHandler.postDelayed(this, 1000)
    }

    fun resumeOrPause() {
        if (_isPlay.value != null && _isPlay.value!!) {
            changePlayStatus(false)
            mHandler.removeCallbacks(this)
        } else {
            if (mSecond <= 0) {
                return
            }
            changePlayStatus(true)
            mHandler.postDelayed(this, 1000)
        }
    }

    fun stop() {
        changePlayStatus(false)
        _isEnd.value = true
        mTotalSecond = 0
        mSecond = 0
        _timeStr.value = formatTime(mSecond)
        _Proportions.value = 360f
        mHandler.removeCallbacks(this)
        mToastMessage.value = "Time is up!"
    }

    override fun run() {
        mSecond--
        if (mSecond < 0) {
            changePlayStatus(false)
            _isEnd.value = true
            mToastMessage.value = "Time is up!"
            _Proportions.value = 360f
            mHandler.removeCallbacks(this)
        } else {
            _timeStr.value = formatTime(mSecond)
            _Proportions.value = 360f - (360f / mTotalSecond * (mTotalSecond - mSecond))
            Log.d("timer count", mSecond.toString() + " proportions:" + _Proportions.value)

            mHandler.postDelayed(this, 1000)
        }
    }

    private fun formatTime(second: Int): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.US)
        format.timeZone = TimeZone.getTimeZone("GMT+00:00") // fix 8 h
        return format.format(Date(second * 1000L))
    }
}
