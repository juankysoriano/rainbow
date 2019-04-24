package com.juankysoriano.rainbow.demo

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.juankysoriano.rainbow.core.Rainbow
import com.juankysoriano.rainbow.demo.sketch.rainbow.forces.RainbowParticleSystem

class SketchActivity : Activity() {

    private lateinit var sketch: Rainbow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sketch)
        sketch = RainbowParticleSystem(findViewById<View>(R.id.rainbow_layout) as ViewGroup)
    }

    public override fun onPause() {
        sketch.pause()
        super.onPause()
    }

    public override fun onStart() {
        super.onStart()
        sketch.start()
    }

    public override fun onResume() {
        super.onResume()
        sketch.resume()
    }

    public override fun onStop() {
        sketch.stop()
        super.onStop()
    }

    public override fun onDestroy() {
        sketch.destroy()
        super.onDestroy()
    }
}
