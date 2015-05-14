package com.juankysoriano.rainbow.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.demo.sketch.rainbow.LibraryApplication;
import com.juankysoriano.rainbow.demo.sketch.rainbow.detection.RainbowBlobDetection;

public class SketchActivity extends Activity {

    private RainbowBlobDetection sketch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LibraryApplication.setContext(getApplicationContext());
        setContentView(R.layout.sketch);
        sketch = new RainbowBlobDetection(getSketchView());
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
        super.onDestroy();
    }
}
