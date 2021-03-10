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
package com.example.androiddevchallenge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
import com.example.androiddevchallenge.ui.home.HomeViewModel
import com.example.androiddevchallenge.ui.home.selectTimeScreen
import com.example.androiddevchallenge.ui.home.timeCountDownScreen
import com.example.androiddevchallenge.ui.theme.MyTheme

class MainActivity : AppCompatActivity() {

  val homeViewModel by viewModels<HomeViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MyTheme {
        MyApp(homeViewModel)
      }
    }
    homeViewModel.mToastMessage.observe(this, Observer { message ->
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    })
  }
}

@Composable
fun MyApp(viewModel: HomeViewModel) {
  val timeState: State<String> = viewModel.timeStr.observeAsState("")

  val isPlayState: State<Boolean> = viewModel.isPlay.observeAsState(false)

  val proportions: State<Float> = viewModel.proportions.observeAsState(360f)
  val isEndState: State<Boolean> = viewModel.isEnd.observeAsState(true)
  Scaffold(
    topBar = {
      val title = "Countdown timer"
      TopAppBar(
        title = { Text(text = title) },
      )
    },
    content = { innerPadding ->
      val modifier = Modifier.padding(innerPadding)
      Surface(color = MaterialTheme.colors.background, modifier = modifier) {
        if (isEndState.value) {
          selectTimeScreen(startClick = { seconds ->
            if (seconds == 0) {
              viewModel.mToastMessage.value = "please select times!"
            } else {
              viewModel.start(seconds)
            }
          })
        } else {
          timeCountDownScreen(
            time = timeState.value,
            isPlay = isPlayState.value,
            360f,
            proportions.value,
            startOrPauseClick = {
              viewModel.resumeOrPause()
            },
            stopClick = {
              viewModel.stop()
            }
          )
        }
      }
    }
  )

}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
  MyTheme {
    MyApp(viewModel = HomeViewModel())
  }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
  MyTheme(darkTheme = true) {
    MyApp(viewModel = HomeViewModel())
  }
}
