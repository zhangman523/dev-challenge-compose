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

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Pause
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material.icons.sharp.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.ui.components.AnimatedCircle
import com.example.androiddevchallenge.ui.theme.MyTheme
import com.example.androiddevchallenge.ui.theme.primaryColor
import com.example.androiddevchallenge.ui.theme.white
import com.example.androiddevchallenge.util.Pager
import com.example.androiddevchallenge.util.PagerState

/**
 * Created by admin on 2021/3/6 21:34.
 * Email: zhangman523@126.com
 */
@Composable
fun TimeCountDownScreen(
    time: String,
    isPlay: Boolean,
    startDegree: Float,
    endDegree: Float,
    startOrPauseClick: () -> Unit,
    stopClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(15.dp)
            .background(color = primaryColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.padding(16.dp)) {
            AnimatedCircle(
                Modifier
                    .height(300.dp)
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                startDegree, endDegree
            )
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(
                    time,
                    color = white,
                    style = MaterialTheme.typography.h2,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconToggleButton(
                checked = isPlay,
                onCheckedChange = { startOrPauseClick() },
            ) {
                Icon(
                    imageVector = if (isPlay) Icons.Sharp.Pause else Icons.Sharp.PlayArrow,
                    contentDescription = null,
                    tint = white,
                )
            }

            IconButton(onClick = stopClick) {
                Icon(
                    imageVector = Icons.Sharp.Stop,
                    contentDescription = null,
                    tint = white,
                )
            }
        }
    }
}

@Composable
fun SelectTimeScreen(startClick: (Int) -> Unit) {
    val hoursData = mutableListOf<Int>()
    val minutesAndSecondsData = mutableListOf<Int>()
    for (i in 0..23) {
        hoursData.add(i)
    }
    for (i in 0..59) {
        minutesAndSecondsData.add(i)
    }

    var hours = 0
    var minutes = 0
    var seconds = 0
    Column(
        modifier = Modifier
            .padding(15.dp)
            .background(color = primaryColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .height(300.dp)
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageComposable(
                    hoursData,
                    select = { current ->
                        hours = hoursData[current]
                    }
                )
                Text("h", color = white, style = MaterialTheme.typography.body1)
                PageComposable(
                    minutesAndSecondsData,
                    select = { current ->
                        minutes = minutesAndSecondsData[current]
                    }
                )
                Text("m", color = white, style = MaterialTheme.typography.body1)
                PageComposable(
                    minutesAndSecondsData,
                    select = { current ->
                        seconds = minutesAndSecondsData[current]
                    }
                )
                Text("s", color = white, style = MaterialTheme.typography.body1)
            }
        }
        IconButton(
            onClick = {
                val secondSum = hours * 60 * 60 + minutes * 60 + seconds
                startClick(secondSum)
            }
        ) {
            Icon(
                imageVector = Icons.Sharp.PlayArrow,
                contentDescription = null,
                tint = white,
            )
        }
    }
}

@Composable
fun PageComposable(data: MutableList<Int>, select: (Int) -> Unit) {
    val pagerState = remember { PagerState() }
    Log.d("CurrentOffset", pagerState.toString())
    pagerState.maxPage = data.size - 1
    Pager(
        state = pagerState,
        modifier = Modifier
            .padding(start = 10.dp, top = 16.dp, end = 10.dp)
            .width(50.dp)
            .height(200.dp),
        pageSelect = select
    ) {
        val color: Long
        if (currentPage == page) {
            color = 0xffffffff
        } else {
            color = 0xff333333
        }
        Text(
            text = data[page].toString(),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.body1,
            color = Color(color),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(device = Devices.PIXEL_4_XL)
@Composable
fun HomeScreenPreview() {
    MyTheme(darkTheme = false) {
//    HomeScreen("00:12:44", true, 360f, 0f, { }, { })
    }
}
