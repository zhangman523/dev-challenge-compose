package com.example.androiddevchallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import com.example.androiddevchallenge.ui.theme.bgColorCenter
import com.example.androiddevchallenge.ui.theme.bgColorEdge
import com.example.androiddevchallenge.ui.theme.darkRed
import com.example.androiddevchallenge.ui.theme.lightOrange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

val Offset.theta: Float get() = (atan2(y.toDouble(), x.toDouble()) * 180 / PI).toFloat()

const val EndRadiusFraction = 0.75f
const val StartRadiusFraction = 0.5f
const val TickWidth = 9f

@Composable
fun Countdown() {
  val scope = rememberCoroutineScope()
  val state = remember { TickwheelState(scope) }
  Column(
    Modifier
      .fillMaxSize()
      .background(Brush.radialGradient(listOf(bgColorCenter, bgColorEdge))),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    TickWheel(
      modifier = Modifier.fillMaxWidth(),
      state = state,
      ticks = 60,
      startColor = lightOrange,
      endColor = darkRed
    ) {
      Text(
        text = state.time,
        style = TextStyle(
          color = Color.White,
          fontSize = 48.sp,
          textAlign = TextAlign.Center,
        )
      )
    }
    IconButton(onClick = { state.toggle() }) {
      Icon(Icons.Default.PlayArrow, contentDescription = "Play")
    }
  }
}

class TickwheelState(val scope: CoroutineScope) {
  var seconds by mutableStateOf(0)
  var isDragging by mutableStateOf(false)
  var endPosition by mutableStateOf<Offset?>(null)
  var isStarted by mutableStateOf(false)
  var minutes by mutableStateOf(0)

  // var isCountingDown by mutableStateOf(false)

  private var job: Job? = null

  fun endDrag() {
    val current = endPosition
    if (current != null) {
      seconds = ((current.theta + 180f) / 360f * 60f).roundToInt()
      isStarted = false
      isDragging = false
    } else {
      error("Position was null when it shouldn't hare been ")
    }
  }

  fun startDrag(startPosition: Offset) {
    endPosition = startPosition
    isDragging = true
    stop()
  }


  fun onDrag(delta: Offset) {
    val prev = endPosition
    val next = if (prev != null) {
      val prevTheta = prev.theta
      val next = prev + delta
      val nextTheta = next.theta

      when {
        prevTheta > 90 && nextTheta < -90f -> minutes++
        prevTheta < -90f && nextTheta > 90f -> minutes--
      }
      next
    } else {
      delta
    }
    seconds = ((next.theta + 180f) / 360f * 60f).roundToInt()
    endPosition = next
  }

  val time: String
    get() {
      return "${seconds}s"
    }

  fun stop() {
    job?.cancel()
    job = null
  }

  fun toggle() {
    if (job == null) {
      job = scope.launch {
        while (seconds > 0) {
          delay(1000)
          val next = seconds - 1
          seconds = next
          val theta = (((next % 60) * 6 - 180) * PI / 180).toFloat()
          val radius = 100f
          endPosition = Offset(
            cos(theta) * radius,
            sin(theta) * radius
          )
        }
        endPosition = null
      }
    } else {
      stop()
    }
  }
}

@Composable
fun TickWheel(
  modifier: Modifier,
  ticks: Int,
  startColor: Color,
  endColor: Color,
  state: TickwheelState,
  content: @Composable ColumnScope.() -> Unit
) {
  var origin by remember { mutableStateOf(Offset.Zero) }
  Box(
    modifier
      .onSizeChanged {
        origin = it.center.toOffset()
      }
      .aspectRatio(1f)
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { offset ->
            state.startDrag(offset - origin)
          },
          onDragEnd = {
            state.endDrag()
          },
          onDragCancel = {
            state.endDrag()
          },
          onDrag = { change, amount ->
            state.onDrag(amount)
            change.consumeAllChanges()
          }
        )
      }

      .drawBehind {
        val endTheta = state.endPosition?.theta ?: -180f //-180 to 180
        val startRadius = size.width / 2 * StartRadiusFraction
        val endRadius = size.width / 2 * EndRadiusFraction
        val sweep = Brush.sweepGradient(
          0f to startColor,
          1f to endColor,
          center = center,
        )
        val offBrush = SolidColor(Color.White.copy(alpha = 0.1f))
        for (i in 0 until ticks) {
          val angle = i * (360 / ticks) - 180 //180 to 180
          val theta = angle * PI.toFloat() / 180f // radians

          val startPos = Offset(
            cos(theta) * startRadius,
            sin(theta) * startRadius
          )
          val endPos = Offset(
            cos(theta) * endRadius,
            sin(theta) * endRadius
          )
          val on = angle < endTheta
          drawLine(
            brush = if (on) sweep else offBrush,
            start = center + startPos,
            end = center + endPos,
            strokeWidth = TickWidth,
            cap = StrokeCap.Round,
          )
        }

      },
    contentAlignment = Alignment.Center
  ) {
    ColumnScope.content()
  }
}