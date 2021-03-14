package com.example.androiddevchallenge

// import androidx.compose.foundation.MutatePriority
// import androidx.compose.foundation.MutatorMutex
// import androidx.compose.foundation.gestures.detectDragGestures
// import androidx.compose.foundation.gestures.drag
// import androidx.compose.foundation.gestures.forEachGesture
// import androidx.compose.foundation.gestures.horizontalDrag
// import androidx.compose.foundation.gestures.verticalDrag
// import androidx.compose.foundation.interaction.DragInteraction
// import androidx.compose.foundation.interaction.MutableInteractionSource
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.DisposableEffect
// import androidx.compose.runtime.MutableState
// import androidx.compose.runtime.State
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.rememberUpdatedState
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.composed
// import androidx.compose.ui.geometry.Offset
// import androidx.compose.ui.input.pointer.PointerInputChange
// import androidx.compose.ui.input.pointer.PointerInputScope
// import androidx.compose.ui.input.pointer.consumePositionChange
// import androidx.compose.ui.input.pointer.pointerInput
// import androidx.compose.ui.input.pointer.positionChange
// import androidx.compose.ui.input.pointer.util.VelocityTracker
// import androidx.compose.ui.platform.debugInspectorInfo
// import kotlinx.coroutines.CoroutineScope
// import kotlinx.coroutines.coroutineScope
// import kotlinx.coroutines.launch
// import kotlin.math.sign

// /**
//  * State of [draggable]. Allows for a granular control of how deltas are consumed by the user as
//  * well as to write custom drag methods using [drag] suspend function.
//  */
// interface DraggableState {
//   /**
//    * Call this function to take control of drag logic.
//    *
//    * All actions that change the logical drag position must be performed within a [drag]
//    * block (even if they don't call any other methods on this object) in order to guarantee
//    * that mutual exclusion is enforced.
//    *
//    * If [drag] is called from elsewhere with the [dragPriority] higher or equal to ongoing
//    * drag, ongoing drag will be canceled.
//    *
//    * @param dragPriority of the drag operation
//    * @param block to perform drag in
//    */
//   suspend fun drag(
//     dragPriority: MutatePriority = MutatePriority.Default,
//     block: suspend DragScope.() -> Unit
//   )
//
//   /**
//    * Dispatch drag delta in pixels avoiding all drag related priority mechanisms.
//    *
//    * **NOTE:** unlike [drag], dispatching any delta with this method will bypass scrolling of
//    * any priority. This method will also ignore `reverseDirection` and other parameters set in
//    * [draggable].
//    *
//    * This method is used internally for low level operations, allowing implementers of
//    * [DraggableState] influence the consumption as suits them, e.g introduce nested scrolling.
//    * Manually dispatching delta via this method will likely result in a bad user experience,
//    * you must prefer [drag] method over this one.
//    *
//    * @param delta amount of scroll dispatched in the nested drag process
//    */
//   fun dispatchRawDelta(delta: Offset)
// }
//
// /**
//  * Scope used for suspending drag blocks
//  */
// interface DragScope {
//   /**
//    * Attempts to drag by [pixels] px.
//    */
//   fun dragBy(offset: Offset)
// }
//
// /**
//  * Default implementation of [DraggableState] interface that allows to pass a simple action that
//  * will be invoked when the drag occurs.
//  *
//  * This is the simplest way to set up a [draggable] modifier. When constructing this
//  * [DraggableState], you must provide a [onDelta] lambda, which will be invoked whenever
//  * drag happens (by gesture input or a custom [DraggableState.drag] call) with the delta in
//  * pixels.
//  *
//  * If you are creating [DraggableState] in composition, consider using [rememberDraggableState].
//  *
//  * @param onDelta callback invoked when drag occurs. The callback receives the delta in pixels.
//  */
// fun DraggableState(onDelta: (Offset) -> Unit): DraggableState =
//   DefaultDraggableState(onDelta)
//
// /**
//  * Create and remember default implementation of [DraggableState] interface that allows to pass a
//  * simple action that will be invoked when the drag occurs.
//  *
//  * This is the simplest way to set up a [draggable] modifier. When constructing this
//  * [DraggableState], you must provide a [onDelta] lambda, which will be invoked whenever
//  * drag happens (by gesture input or a custom [DraggableState.drag] call) with the delta in
//  * pixels.
//  *
//  * @param onDelta callback invoked when drag occurs. The callback receives the delta in pixels.
//  */
// @Composable
// fun rememberDraggableState(onDelta: (Offset) -> Unit): DraggableState {
//   val onDeltaState = rememberUpdatedState(onDelta)
//   return remember { DraggableState { onDeltaState.value.invoke(it) } }
// }
//
// /**
//  * Configure touch dragging for the UI element in a single [Orientation]. The drag distance
//  * reported to [DraggableState], allowing users to react on the drag delta and update their state.
//  *
//  * The common usecase for this component is when you need to be able to drag something
//  * inside the component on the screen and represent this state via one float value
//  *
//  * If you need to control the whole dragging flow, consider using [pointerInput] instead with the
//  * helper functions like [detectDragGestures].
//  *
//  * If you are implementing scroll/fling behavior, consider using [scrollable].
//  *
//  * @sample androidx.compose.foundation.samples.DraggableSample
//  *
//  * @param state [DraggableState] state of the draggable. Defines how drag events will be
//  * interpreted by the user land logic.
//  * @param orientation orientation of the drag
//  * @param enabled whether or not drag is enabled
//  * @param interactionSource [MutableInteractionSource] that will be used to emit
//  * [DragInteraction.Start] when this draggable is being dragged.
//  * @param startDragImmediately when set to true, draggable will start dragging immediately and
//  * prevent other gesture detectors from reacting to "down" events (in order to block composed
//  * press-based gestures).  This is intended to allow end users to "catch" an animating widget by
//  * pressing on it. It's useful to set it when value you're dragging is settling / animating.
//  * @param onDragStarted callback that will be invoked when drag is about to start at the starting
//  * position, allowing user to suspend and perform preparation for drag, if desired. This suspend
//  * function is invoked with the draggable scope, allowing for async processing, if desired
//  * @param onDragStopped callback that will be invoked when drag is finished, allowing the
//  * user to react on velocity and process it. This suspend function is invoked with the draggable
//  * scope, allowing for async processing, if desired
//  * @param reverseDirection reverse the direction of the scroll, so top to bottom scroll will
//  * behave like bottom to top and left to right will behave like right to left.
//  */
// fun Modifier.xyDraggable(
//   state: DraggableState,
//   interactionSource: MutableInteractionSource? = null,
//   onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit = {},
//   onDragStopped: suspend CoroutineScope.(velocity: Float) -> Unit = {},
// ): Modifier = composed(
//   inspectorInfo = debugInspectorInfo {
//     name = "draggable"
//     properties["interactionSource"] = interactionSource
//     properties["onDragStarted"] = onDragStarted
//     properties["onDragStopped"] = onDragStopped
//     properties["state"] = state
//   }
// ) {
//   val draggedInteraction = remember { mutableStateOf<DragInteraction.Start?>(null) }
//   DisposableEffect(interactionSource) {
//     onDispose {
//       draggedInteraction.value?.let { interaction ->
//         interactionSource?.tryEmit(DragInteraction.Cancel(interaction))
//         draggedInteraction.value = null
//       }
//     }
//   }
//   val interactionSourceState = rememberUpdatedState(interactionSource)
//   val onDragStartedState = rememberUpdatedState(onDragStarted)
//   val updatedDraggableState = rememberUpdatedState(state)
//   val onDragStoppedState = rememberUpdatedState(onDragStopped)
//   val dragBlock: suspend PointerInputScope.() -> Unit = remember {
//     {
//       dragForEachGesture(
//         interactionSource = interactionSourceState,
//         dragStartInteraction = draggedInteraction,
//         onDragStarted = onDragStartedState,
//         onDragStopped = onDragStoppedState,
//         dragState = updatedDraggableState
//       )
//     }
//   }
//   Modifier.pointerInput(Unit, dragBlock)
// }
//
// private suspend fun PointerInputScope.dragForEachGesture(
//   interactionSource: State<MutableInteractionSource?>,
//   dragStartInteraction: MutableState<DragInteraction.Start?>,
//   startDragImmediately: State<Boolean>,
//   onDragStarted: State<suspend CoroutineScope.(startedPosition: Offset) -> Unit>,
//   onDragStopped: State<suspend CoroutineScope.(velocity: Float) -> Unit>,
//   dragState: State<DraggableState>
// ) {
//   coroutineScope {
//     forEachGesture {
//
//       suspend fun DragScope.performDrag(
//         initialDelta: Offset,
//         dragStart: PointerInputChange,
//         velocityTracker: VelocityTracker
//       ): Boolean {
//         return awaitPointerEventScope {
//           dragBy(initialDelta)
//           velocityTracker.addPosition(dragStart.uptimeMillis, dragStart.position)
//           val dragTick = { event: PointerInputChange ->
//             velocityTracker.addPosition(event.uptimeMillis, event.position)
//             val delta =
//               event.positionChange()
//             event.consumePositionChange()
//             dragBy(delta)
//           }
//           verticalDrag(dragStart.id, dragTick)
//         }
//       }
//
//       var initialDelta = 0f
//       val drag = awaitPointerEventScope {
//         val down = awaitFirstDown(requireUnconsumed = false)
//         // since we start immediately we don't wait for slop and set initial delta to 0
//         initialDelta = 0f
//         down
//       }
//       var isDragSuccessful = false
//       val velocityTracker = VelocityTracker()
//       var enabledWhenInteractionAdded = false
//       try {
//         // remember enabled state when we add interaction to remove later if needed
//         enabledWhenInteractionAdded = enabled.value
//         val overSlopOffset =
//           if (isVertical()) Offset(0f, initialDelta)
//           else Offset(initialDelta, 0f)
//         val adjustedStart = drag.position -
//             overSlopOffset * sign(drag.position.run { if (isVertical()) y else x })
//         if (enabledWhenInteractionAdded) {
//           onDragStarted.value.invoke(this@coroutineScope, adjustedStart)
//           launch {
//             dragStartInteraction.value?.let { oldInteraction ->
//               interactionSource.value?.emit(
//                 DragInteraction.Cancel(oldInteraction)
//               )
//             }
//             val interaction = DragInteraction.Start()
//             interactionSource.value?.emit(interaction)
//             dragStartInteraction.value = interaction
//           }
//         }
//         dragState.value.drag(dragPriority = MutatePriority.UserInput) {
//           isDragSuccessful = performDrag(initialDelta, drag, velocityTracker)
//         }
//       } catch (cancellation: CancellationException) {
//         isDragSuccessful = false
//       } finally {
//         if (enabledWhenInteractionAdded) {
//           launch {
//             dragStartInteraction.value?.let { interaction ->
//               interactionSource.value?.emit(
//                 DragInteraction.Stop(interaction)
//               )
//               dragStartInteraction.value = null
//             }
//           }
//         }
//         val velocity =
//           if (isDragSuccessful) {
//             velocityTracker.calculateVelocity().run { if (isVertical()) y else x }
//           } else {
//             0f
//           }
//         onDragStopped.value.invoke(
//           this@coroutineScope,
//           if (reverseDirection.value) velocity * -1 else velocity
//         )
//       }
//     }
//   }
// }
//
// private class DefaultDraggableState(val onDelta: (Offset) -> Unit) : DraggableState {
//
//   private val dragScope: DragScope = object : DragScope {
//     override fun dragBy(pixels: Float): Unit = onDelta(pixels)
//   }
//
//   private val scrollMutex = MutatorMutex()
//
//   override suspend fun drag(
//     dragPriority: MutatePriority,
//     block: suspend DragScope.() -> Unit
//   ): Unit = coroutineScope {
//     scrollMutex.mutateWith(dragScope, dragPriority, block)
//   }
//
//   override fun dispatchRawDelta(delta: Float) {
//     return onDelta(delta)
//   }
// }