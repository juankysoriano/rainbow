package com.juankysoriano.rainbow.core.touch

import android.view.MotionEvent

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer
import com.juankysoriano.rainbow.core.schedulers.RainbowScheduler
import com.juankysoriano.rainbow.core.schedulers.RainbowSchedulers

private const val DIVISIONS = 2

class RainbowInputController
internal constructor(private val scheduler: RainbowScheduler = RainbowSchedulers.single("InputController", RainbowSchedulers.Priority.NORMAL),
                     private val smoother: FingerPositionSmoother = FingerPositionSmoother(),
                     val rainbowDrawer: RainbowDrawer = RainbowDrawer()) {


    var x: Float = 0.toFloat()
        private set
    var y: Float = 0.toFloat()
        private set
    var previousX: Float = 0.toFloat()
        private set
    var previousY: Float = 0.toFloat()
        private set
    var isScreenTouched: Boolean = false
        private set
    var isFingerMoving: Boolean = false
        private set

    val verticalDirection: MovementDirection
        get() = if (smoother.y > smoother.oldY) {
            MovementDirection.DOWN
        } else {
            MovementDirection.UP
        }

    val horizontalDirection: MovementDirection
        get() = if (smoother.x > smoother.oldX) {
            MovementDirection.RIGHT
        } else {
            MovementDirection.LEFT
        }

    val smoothX: Float
        get() = smoother.x

    val smoothY: Float
        get() = smoother.y

    val previousSmoothX: Float
        get() = smoother.oldX

    val previousSmoothY: Float
        get() = smoother.oldY

    val fingerVelocity: Float
        get() = smoother.fingerVelocity

    private var rainbowInteractionListener: RainbowInteractionListener? = null

    init {
        previousY = -1f
        previousX = previousY
        y = previousX
        x = y
    }

    fun postEvent(motionEvent: MotionEvent) {
        scheduler.scheduleNow(inputEventFor(MotionEvent.obtain(motionEvent)))
    }

    private fun inputEventFor(motionEvent: MotionEvent): Runnable {
        return Runnable {
            when (motionEvent.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_DOWN -> process(motionEvent)
                MotionEvent.ACTION_MOVE -> splitIntoMultipleEvents(motionEvent)
            }//no-op
        }
    }

    private fun process(motionEvent: MotionEvent) {
        preHandleEvent(motionEvent)
        handleSketchEvent(motionEvent)
        postHandleEvent()
    }

    private fun splitIntoMultipleEvents(event: MotionEvent) {
        val px = x
        val py = y
        val diffX = event.x - px
        val diffY = event.y - py

        for (i in 1..DIVISIONS) {
            val newEventX = px + diffX * i / DIVISIONS
            val newEventY = py + diffY * i / DIVISIONS
            val subEvent = obtainEventWithNewPosition(event, newEventX, newEventY)
            process(subEvent)
        }
    }

    private fun obtainEventWithNewPosition(event: MotionEvent, newEventX: Float, newEventY: Float): MotionEvent {
        val motionEvent = MotionEvent.obtain(event)
        motionEvent.setLocation(newEventX, newEventY)
        return motionEvent
    }

    private fun preHandleEvent(motionEvent: MotionEvent) {
        x = motionEvent.x
        y = motionEvent.y
    }

    private fun handleSketchEvent(event: MotionEvent) {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isScreenTouched = true
                isFingerMoving = false
                performTouch(event, rainbowDrawer)
            }
            MotionEvent.ACTION_UP -> {
                isScreenTouched = false
                isFingerMoving = false
                performRelease(event, rainbowDrawer)
            }
            MotionEvent.ACTION_MOVE -> {
                isScreenTouched = true
                isFingerMoving = true
                performMove(event, rainbowDrawer)
            }
        }

        performMotion(event, rainbowDrawer)
    }

    private fun postHandleEvent() {
        previousX = x
        previousY = y
    }

    private fun performTouch(event: MotionEvent, rainbowDrawer: RainbowDrawer) {
        smoother.resetTo(event.x, event.y)
        rainbowInteractionListener?.onSketchTouched(event, rainbowDrawer)
    }

    private fun performRelease(event: MotionEvent, rainbowDrawer: RainbowDrawer) {
        smoother.resetTo(event.x, event.y)
        rainbowInteractionListener?.onSketchReleased(event, rainbowDrawer)
    }

    private fun performMove(event: MotionEvent, rainbowDrawer: RainbowDrawer) {
        smoother.moveTo(event.x, event.y)
        rainbowInteractionListener?.onFingerDragged(event, rainbowDrawer)
    }

    private fun performMotion(event: MotionEvent, rainbowDrawer: RainbowDrawer) {
        rainbowInteractionListener?.onMotionEvent(event, rainbowDrawer)
    }

    /**
     * Used to set a RainbowInteractionListener which will listen for different interaction events
     *
     * @param rainbowInteractionListener the listener to be set
     */
    fun attach(rainbowInteractionListener: RainbowInteractionListener) {
        this.rainbowInteractionListener = rainbowInteractionListener
    }

    /**
     * Used to remove the attached RainbowInteractionListener
     * Also stops any pending input event processing task.
     */
    fun detach() {
        this.rainbowInteractionListener = null
        this.scheduler.shutdown()
    }

    enum class MovementDirection {
        UP, DOWN, LEFT, RIGHT
    }

    interface RainbowInteractionListener {
        fun onSketchTouched(event: MotionEvent, rainbowDrawer: RainbowDrawer)

        fun onSketchReleased(event: MotionEvent, rainbowDrawer: RainbowDrawer)

        fun onFingerDragged(event: MotionEvent, rainbowDrawer: RainbowDrawer)

        fun onMotionEvent(event: MotionEvent, rainbowDrawer: RainbowDrawer)
    }
}
