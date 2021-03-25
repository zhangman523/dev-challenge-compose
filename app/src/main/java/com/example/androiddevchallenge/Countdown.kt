package com.example.androiddevchallenge

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import androidx.core.content.ContextCompat.getSystemService
import com.example.androiddevchallenge.ui.theme.bgColorCenter
import com.example.androiddevchallenge.ui.theme.bgColorEdge
import com.example.androiddevchallenge.ui.theme.darkRed
import com.example.androiddevchallenge.ui.theme.lightOrange
import com.google.android.material.animation.AnimationUtils.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin

val Offset.theta: Float get() = (atan2(y.toDouble(), x.toDouble()) * 180 / PI).toFloat()

const val TickWidth = 9f
const val Epsilon = 9f
const val RadiusA = 0.36f
const val RadiusB = 0.40f
const val RadiusC = 0.48f
const val RadiusD = 0.75f
const val RadiusE = 1.4f
const val EndRadiusFraction = 0.75f

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
  var totalSeconds by mutableStateOf(0)
  val seconds: Int get() = totalSeconds % 60
  val minutes: Int get() = floor(totalSeconds.toDouble() / 60).toInt()

  var isDragging by mutableStateOf(false)
  var endPosition by mutableStateOf<Offset?>(null)
  // var minutes by mutableStateOf(0)

  // var isCountingDown by mutableStateOf(false)

  private var job: Job? = null

  fun endDrag() {
    val current = endPosition
    if (current != null) {
      // totalSeconds = ((current.theta + 180f) / 360f * 60f).roundToInt()
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

      val nextMinutes = when {
        prevTheta > 90 && nextTheta < -90f -> minutes + 1
        prevTheta < -90f && nextTheta > 90f -> max(0, minutes - 1)
        else -> minutes
      }
      totalSeconds = floor((nextMinutes) * 60 + ((next.theta + 180f) / 360f * 60f)).toInt()
      next
    } else {
      delta
    }
    endPosition = next
  }

  val time: String
    get() {
      return buildString {
        append("$minutes".padStart(2, '0'))
        append(":")
        append("$seconds".padStart(2, '0'))
      }
    }

  fun stop() {
    job?.cancel()
    job = null
  }

  fun countDown() {
    val next = totalSeconds - 1
    totalSeconds = next
    val theta = (((next % 60) * 6 - 180) * PI / 180).toFloat()
    val radius = 100f
    endPosition = Offset(
      cos(theta) * radius,
      sin(theta) * radius
    )
  }

  fun toggle() {
    if (job == null) {
      job = scope.launch {
        while (totalSeconds > 0) {
          delay(1000)
          countDown()
        }
        endPosition = null
      }
    } else {
      stop()
    }
  }
}

@SuppressLint("RestrictedApi")
@Composable
fun TickWheel(
  modifier: Modifier,
  ticks: Int,
  startColor: Color,
  endColor: Color,
  state: TickwheelState,
  content: @Composable () -> Unit
) {
  var origin by remember { mutableStateOf(Offset.Zero) }
  val vibrator = systemService<Vibrator>()
  val secondTransition by animateFloatAsState(state.seconds.toFloat())
  val minuteTransition by animateFloatAsState(state.minutes.toFloat())

  Box(
    modifier
      .onSizeChanged { origin = it.center.toOffset() }
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
      .drawWithCache {
        val unitRadius = size.width / 2f
        val a = unitRadius * RadiusA
        val b = unitRadius * RadiusB
        val c = unitRadius * RadiusC
        val d = unitRadius * RadiusD
        val e = unitRadius * RadiusE

        val offBrush = Color.White
          .copy(alpha = 0.1f)
          .toBrush()

        val matrix = Matrix().also { it.setRotate(-182f, size.width / 2, size.height / 2) }

        val sweep = ShaderBrush(
          android.graphics
            .SweepGradient(
              size.width / 2,
              size.height / 2,
              startColor.toArgb(),
              endColor.toArgb()
            )
            .also { it.setLocalMatrix(matrix) }
        )

        onDrawBehind {
          val endAngle = state.seconds * 6 - 180
          val minutes = state.minutes
          for (i in 0 until ticks) {
            val angle = i * (360 / ticks) - 180 //180 to 180
            val theta = angle * PI.toFloat() / 180f // radians
            val on = angle < endAngle
            val up = minutes >= minuteTransition
            val t = 1 - abs(minutes - minuteTransition)

            if (up) {
              if (minutes > 1) { // only needed when
                drawTick(
                  sweep,
                  theta,
                  lerp(b, a, t),
                  lerp(c, b, t),
                  1 - t
                )
              }

              if (minutes > 0) {
                drawTick(
                  sweep,
                  theta,
                  lerp(c, b, t),
                  lerp(d, c, t),
                  1f
                )
              }
              drawTick(
                if (on) sweep else offBrush,
                theta,
                lerp(d, c, t),
                lerp(e, d, t),
                t
              )
            } else {
              if (minutes > 0) { // only needed when
                drawTick(
                  sweep,
                  theta,
                  lerp(a, b, t),
                  lerp(b, c, t),
                  t
                )
              }

              drawTick(
                if (on) sweep else offBrush,
                theta,
                lerp(b, c, t),
                lerp(c, d, t),
                1f
              )
              drawTick(
                offBrush,
                theta,
                lerp(c, d, t),
                lerp(d, e, t),
                1 - t
              )
            }
          }
        }
      },
    contentAlignment = Alignment.Center
  ) {
    content()
  }
}

fun Color.toBrush(): Brush = SolidColor(this)

fun DrawScope.drawTick(
  brush: Brush,
  theta: Float,
  startRadius: Float,
  endRadius: Float,
  alpha: Float
) {
  drawLine(
    brush,
    center + Offset(
      cos(theta) * (startRadius + Epsilon),
      sin(theta) * (startRadius + Epsilon)
    ),
    center + Offset(
      cos(theta) * (endRadius - Epsilon),
      sin(theta) * (endRadius - Epsilon)
    ),
    TickWidth,
    StrokeCap.Round,
    alpha = alpha.coerceIn(0f, 1f)
  )
}

@Composable
inline fun <reified T> systemService(): T? {
  val context = LocalContext.current
  return remember { getSystemService(context, T::class.java) }
}