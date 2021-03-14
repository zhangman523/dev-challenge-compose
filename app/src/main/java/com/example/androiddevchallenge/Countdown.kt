package com.example.androiddevchallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.toOffset
import kotlin.math.PI
import kotlin.math.atan2

val Offset.theta: Float get() = (atan2(y.toDouble(), x.toDouble()) * 180 / PI).toFloat()

@Composable
fun Countdown() {
  var origin by remember { mutableStateOf(Offset.Zero) }
  var position by remember { mutableStateOf<Offset?>(null) }
  Column(verticalArrangement = Arrangement.Center) {
    Text(
      """
        origin: $origin
        position: $position
        theta: ${position?.theta}
      """.trimIndent()
    )
    Box(
      Modifier
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { offset ->
              position = offset - origin

            },
            onDragEnd = {
              position = null
            },
            onDragCancel = {
              position = null
            },
            onDrag = { change, amount ->
              position = position?.let { it + amount } ?: amount
              change.consumeAllChanges()
            }
          )
        }
        .onSizeChanged {
          origin = it.center.toOffset()
        }
        .fillMaxWidth()
        .background(color = Blue, shape = CircleShape)
        .aspectRatio(1f)
        .drawBehind {
          origin = center
          drawCircle(Color.White, center = center, radius = 20f)
        }
    ) {

    }
  }
}
