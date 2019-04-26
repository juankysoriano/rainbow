package com.juankysoriano.rainbow.demo.sketch.rainbow.portrait

import android.graphics.Color.*
import android.view.ViewGroup
import com.juankysoriano.rainbow.core.Rainbow
import com.juankysoriano.rainbow.core.drawing.Modes
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer
import com.juankysoriano.rainbow.core.graphics.RainbowImage
import com.juankysoriano.rainbow.core.matrix.RVector
import com.juankysoriano.rainbow.utils.RainbowMath.*
import kotlin.math.floor


private const val STEPS = 800

class RainbowMessyLinePaiting(viewGroup: ViewGroup) : Rainbow(viewGroup) {
    private lateinit var originalImage: RainbowImage
    private lateinit var thresholdImage: RainbowImage
    private lateinit var paint: Paint
    override fun onSketchSetup() {
        with(rainbowDrawer) {
            background(255)
            smooth()
            loadImage(com.juankysoriano.rainbow.demo.R.drawable.winnie,
                    width,
                    height,
                    Modes.LoadMode.LOAD_CENTER_CROP,
                    object : RainbowImage.LoadPictureListener {
                        override fun onLoadSucceed(image: RainbowImage) {
                            originalImage = image
                            originalImage.loadPixels()
                            thresholdImage = rainbowDrawer.createImage(width, height, Modes.Image.RGB)
                            thresholdImage.set(0, 0, originalImage)
                            thresholdImage.filter(Modes.Filter.THRESHOLD, 0.6f)
                            paint = Paint(rainbowDrawer, originalImage, thresholdImage)
                            paint.reset()
                        }

                        override fun onLoadFail() {
                            //no-op
                        }

                    })
        }
    }

    override fun onDrawingStep() {
        if (!::paint.isInitialized || stepCount() > width) {
            return
        }

        for (i in 0..STEPS) {
            paint.updateAndDisplay()
        }
    }

    class Paint(private val rainbowDrawer: RainbowDrawer, private val originalImage: RainbowImage, private val thresholdImage: RainbowImage) {
        var ppos = RVector()
        var pos = RVector()
        var vel = RVector()
        var force = RVector()

        var maxSpeed = 3f
        var perception = 5f
        var bound = 60
        var boundForceFactor = 0.16f
        var noiseScale = 100f
        var noiseInfluence = 1 / 20f

        var dropRate = 0.0001f
        var dropRange = 40
        var dropAlpha = 150f
        var drawAlpha = 60f
        var drawColor = rainbowDrawer.color(0f, 0f, 0f, drawAlpha)
        var count = 0
        var maxCount = 100
        var z = 0f

        fun updateAndDisplay() {
            update()
            show()
            z += 0.01f
        }

        fun update() {
            ppos = pos.copy()
            force.mult(0f)

            // Add pixels force
            val target = RVector()
            var count = 0
            for (i in -floor(perception / 2f).toInt()..(perception / 2f).toInt()) {
                for (j in -floor(perception / 2f).toInt()..(perception / 2f).toInt()) {
                    if (i == 0 && j == 0) {
                        continue
                    }
                    val x = floor(pos.x + i)
                    val y = floor(pos.y + j)
                    if (x <= rainbowDrawer.width - 1 && x >= 0 && y < rainbowDrawer.height - 1 && y >= 0) {
                        val c = get(x.toInt(), y.toInt())
                        var b = rainbowDrawer.brightness(c)
                        b = 1 - b / 100f
                        val p = RVector(i.toFloat(), j.toFloat())
                        p.normalize()
                        val pCopy = p.copy()
                        pCopy.mult(b)
                        pCopy.div(p.mag())
                        target.add(pCopy)
                        count++
                    }
                }
            }
            if (count != 0) {
                target.div(count.toFloat())
                force.add(target)
            }

            var n = noise(pos.x / noiseScale, pos.y / noiseScale, z)
            n = map(n, 0f, 1f, 0f, 5f * TWO_PI)
            val p = RVector(cos(n), sin(n))
            if (force.mag() < 0.01f) {
                p.mult(noiseInfluence * 5)
            } else {
                p.mult(noiseInfluence)
            }
            force.add(p)

            // Add bound force
            val boundForce = RVector()
            if (pos.x < bound) {
                boundForce.x = (bound - pos.x) / bound
            }
            if (pos.x > rainbowDrawer.width - bound) {
                boundForce.x = (pos.x - rainbowDrawer.width) / bound
            }
            if (pos.y < bound) {
                boundForce.y = (bound - pos.y) / bound
            }
            if (pos.y > rainbowDrawer.height - bound) {
                boundForce.y = (pos.y - rainbowDrawer.height) / bound
            }
            boundForce.mult(boundForceFactor)
            force.add(boundForce)


            vel.add(force)
            vel.mult(0.9999f)
            if (vel.mag() > maxSpeed) {
                vel.mult(maxSpeed / vel.mag())
            }

            pos.add(vel)
            if (pos.x > rainbowDrawer.width || pos.x < 0 || pos.y > rainbowDrawer.height || pos.y < 0) {
                this.reset()
            }


        }

        fun reset() {
            count = 0
            var hasFound = false
            while (!hasFound) {
                pos.x = random(rainbowDrawer.width).toFloat()
                pos.y = random(rainbowDrawer.height).toFloat()
                val c = get(floor(pos.x).toInt(), floor(pos.y).toInt())
                val b = rainbowDrawer.brightness(c)
                if (b < 70)
                    hasFound = true
            }
            val index = floor(pos.x).toInt() + rainbowDrawer.width * floor(pos.y).toInt()
            drawColor = originalImage.pixels[index]
            drawColor = rainbowDrawer.color(rainbowDrawer.brightness(drawColor), drawAlpha)
            ppos = pos.copy()
            vel.mult(0f)
        }

        fun show() {
            count++
            if (count > maxCount) {
                this.reset()
            }
            rainbowDrawer.stroke(drawColor)
            val brightness = rainbowDrawer.brightness(drawColor)
            var drawWeight = 0.5f
            if (brightness < 35) {
                drawWeight = 1.5f
            }
            rainbowDrawer.strokeWeight(drawWeight)
            if (force.mag() > 0.1f && random(1f) < dropRate) {
                drawColor = rainbowDrawer.color(brightness, dropAlpha)
                rainbowDrawer.stroke(drawColor)
                val boldWeight = 0f + random(3f, 12f)
                rainbowDrawer.strokeWeight(boldWeight)
                drawColor = rainbowDrawer.color(brightness, drawAlpha)
            }
            rainbowDrawer.line(ppos.x, ppos.y, pos.x, pos.y)

            this.fadeLineFromImg(ppos.x, ppos.y, pos.x, pos.y)
        }

        private fun fadeLineFromImg(x1: Float, y1: Float, x2: Float, y2: Float) {
            val xOffset = floor(abs(x1 - x2))
            val yOffset = floor(abs(y1 - y2))
            val step = if (xOffset < yOffset) {
                yOffset
            } else {
                xOffset
            }
            for (i in 0..step.toInt()) {
                val x = floor(x1 + (x2 - x1) * i / step)
                val y = floor(y1 + (y2 - y1) * i / step)
                var originColor = get(x.toInt(), y.toInt())

                val r = min(255, red(originColor) + 50)
                val g = min(255, green(originColor) + 50)
                val b = min(255, blue(originColor) + 50)

                originColor = rainbowDrawer.color(r, g, b, rainbowDrawer.brightness(originColor).toInt())

                set(x.toInt(), y.toInt(), originColor)
            }
        }

        private fun get(x: Int, y: Int): Int {
            val index = (y * rainbowDrawer.width + x)
            return thresholdImage.pixels[index]
        }

        private fun set(x: Int, y: Int, color: Int) {
            val index = (y * rainbowDrawer.width + x)
            thresholdImage.pixels[index] = color
        }

        private fun RVector.copy(): RVector {
            return RVector(x, y)
        }


    }
}
