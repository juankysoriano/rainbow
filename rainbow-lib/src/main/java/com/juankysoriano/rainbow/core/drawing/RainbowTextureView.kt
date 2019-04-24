package com.juankysoriano.rainbow.core.drawing

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.TextureView
import android.view.ViewGroup

import com.juankysoriano.rainbow.core.Rainbow

@SuppressLint("ViewConstructor")
class RainbowTextureView(parent: ViewGroup, private val rainbow: Rainbow) : TextureView(parent.context) {

    init {
        parent.addView(this, 0, MATCH_PARENT_PARAMS)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        rainbow.rainbowInputController.postEvent(event)
        parent.requestDisallowInterceptTouchEvent(true)
        return true
    }

    companion object {
        private const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
        private val MATCH_PARENT_PARAMS = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
}
