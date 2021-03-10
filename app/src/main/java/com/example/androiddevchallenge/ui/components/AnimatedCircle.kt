package com.example.androiddevchallenge.ui.components

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
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



  Canvas(modifier = modifier, onDraw = {

    val innerRadius = (size.minDimension - stroke.width) / 2
    val halfSize = size / 2.0f
    val topLeft = Offset(
      halfSize.width - innerRadius,
      halfSize.height - innerRadius
    )
    val size = Size(innerRadius * 2, innerRadius * 2)

    var startAngle = -90f
    val sweep = angleOffset
    drawArc(
      color =Color(0x70ffffff),
      startAngle = startAngle,
      sweepAngle = sweep,
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
  })
}

private enum class AnimatedCircleProgress { START, END }

@Preview
@Composable
fun AnimatedCirclePreview() {
  AnimatedCircle(
    Modifier
      .height(300.dp)
      .fillMaxWidth(), 360f, 0f
  )
}