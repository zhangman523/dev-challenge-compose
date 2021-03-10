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
package com.example.androiddevchallenge.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Created by admin on 2021/3/7 21:30.
 * Email: zhangman523@126.com
 */
@Composable
fun AnimatedCircle(modifier: Modifier, startDegree: Float, endDegree: Float) {

    val currentState = remember {
        MutableTransitionState(AnimatedCircleProgress.START)
            .apply { targetState = AnimatedCircleProgress.END }
    }
    val stroke = with(LocalDensity.current) { Stroke(5.dp.toPx()) }
    val strokeBg = with(LocalDensity.current) { Stroke(6.dp.toPx()) }

    val transition = updateTransition(currentState)
    val angleOffset by transition.animateFloat(
        transitionSpec = {
            tween(
                delayMillis = 0,
                durationMillis = 900,
                easing = LinearEasing
            )
        }
    ) { progress ->
        if (progress == AnimatedCircleProgress.START) {
            startDegree
        } else {
            endDegree
        }
    }

    Canvas(
        modifier = modifier,
        onDraw = {

            val innerRadius = (size.minDimension - stroke.width) / 2
            val halfSize = size / 2.0f
            val topLeft = Offset(
                halfSize.width - innerRadius,
                halfSize.height - innerRadius
            )
            val size = Size(innerRadius * 2, innerRadius * 2)

            val startAngle = -90f
            drawArc(
                color = Color(0x70ffffff),
                startAngle = startAngle,
                sweepAngle = angleOffset,
                useCenter = false,
                topLeft = topLeft,
                style = stroke,
                size = size
            )
            drawArc(
                color = Color(0x25ffffff),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                style = strokeBg,
                size = size
            )
        }
    )
}

private enum class AnimatedCircleProgress { START, END }

@Preview
@Composable
fun AnimatedCirclePreview() {
    AnimatedCircle(
        Modifier
            .height(300.dp)
            .fillMaxWidth(),
        360f, 0f
    )
}
