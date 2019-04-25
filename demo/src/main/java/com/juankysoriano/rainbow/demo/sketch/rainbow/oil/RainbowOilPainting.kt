package com.juankysoriano.rainbow.demo.sketch.rainbow.oil

import android.view.ViewGroup
import com.juankysoriano.rainbow.core.Rainbow
import com.juankysoriano.rainbow.core.drawing.Modes
import com.juankysoriano.rainbow.core.graphics.RainbowImage
import com.juankysoriano.rainbow.utils.RainbowMath.radians
import com.juankysoriano.rainbow.utils.RainbowMath.random


class RainbowOilPainting(viewGroup: ViewGroup) : Rainbow(viewGroup) {

    private lateinit var rainbowImage: RainbowImage

    override fun onSketchSetup() {
        with(rainbowDrawer) {
            translate(width / 2f, height / 2f)
            background(255)
            noFill()
            noSmooth()
            loadImage(com.juankysoriano.rainbow.demo.R.drawable.winnie,
                    width,
                    height,
                    Modes.LoadMode.LOAD_CENTER_CROP,
                    object : RainbowImage.LoadPictureListener {
                        override fun onLoadSucceed(image: RainbowImage) {
                            rainbowImage = image
                            rainbowImage.loadPixels()
                        }

                        override fun onLoadFail() {
                        }

                    })
        }
    }

    override fun onDrawingStep() {
        if (!::rainbowImage.isInitialized) {
            return
        }
        with(rainbowDrawer) {
            for (y in (height * 0.1f).toInt() until (height * 0.9f).toInt()) {
                for (x in (width * 0.1f).toInt() until (width * 0.9f).toInt()) {
                    if (random(100000) < 1) {
                        val pixelColor = rainbowImage.pixels[x + y * width]
                        val color = color(red(pixelColor), green(pixelColor), blue(pixelColor), 100f)
                        pushMatrix()
                        translate(x - width / 2f, y - height / 2f)
                        rotate(radians(random(-90f, 90f)))
                        when {
                            stepCount() < 20 -> paintStroke(random(150f, 250f) * 2.5f, color, 3 * random(20f, 40f).toInt())
                            stepCount() < 50 -> paintStroke(random(75f, 125f) * 2, color, 3 * random(8f, 12f).toInt())
                            stepCount() < 120 -> paintStroke(random(30f, 60f) * 1.5f, color, 3 * random(5f, 15f).toInt())
                            stepCount() < 400 -> paintStroke(random(15f, 30f), color, 3 * random(1f, 4f).toInt())
                            stepCount() < 600 -> paintStroke(random(1f, 10f), color, 3 * random(1f, 7f).toInt())
                        }

                        popMatrix()
                    }
                }
            }
        }
    }

    private fun paintStroke(strokeLength: Float, color: Int, thickness: Int) {
        val stepLength = strokeLength / 4f
        var tangent1 = 0f
        var tangent2 = 0f
        val odds = random(1f)
        if (odds < 0.7f) {
            tangent1 = random(-strokeLength, strokeLength)
            tangent2 = random(-strokeLength, strokeLength)
        }

        with(rainbowDrawer) {
            stroke(color)
            strokeWeight(thickness.toFloat())
            bezier(tangent1, -stepLength * 2, 0f, -stepLength, 0f, stepLength, tangent2, stepLength * 2)

            var z = 1
            for (i in thickness downTo 1) {
                val offset = random(-50f, 25f)
                val newColor = color(
                        red(color) + offset,
                        green(color) + offset,
                        blue(color) + offset,
                        random(100f, 255f)
                )
                stroke(newColor)
                strokeWeight(random(0f, 3f))
                bezier(tangent1, -stepLength * 2, z - thickness / 2f, -stepLength * random(0.9f, 1.1f), z - thickness / 2f, stepLength * random(0.9f, 1.1f), tangent2, stepLength * 2)
                z++
            }
        }
    }
}
