package com.juankysoriano.rainbow.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.demo.sketch.rainbow.RainbowSketch;

public class SketchActivity extends Activity {

    private RainbowSketch sketch;
    private ViewGroup sketchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sketch);
        sketchView = getSketchView();
        sketch = new RainbowSketch(sketchView);
    }

    private ViewGroup getSketchView() {
        return (ViewGroup) findViewById(R.id.rainbow_layout);
    }

    @Override
    public void onPause() {
        sketch.pause();
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        sketch.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        sketch.resume();
    }

    @Override
    public void onStop() {
        sketch.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        sketch.destroy();
        sketch = null;
        super.onDestroy();
    }
}
