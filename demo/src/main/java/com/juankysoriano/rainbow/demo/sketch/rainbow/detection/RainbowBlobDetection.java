package com.juankysoriano.rainbow.demo.sketch.rainbow.detection;

import android.graphics.Bitmap;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowInputController;
import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.juankysoriano.rainbow.core.listeners.LoadPictureListener;
import com.juankysoriano.rainbow.demo.sketch.rainbow.LibraryApplication;

public class RainbowBlobDetection extends Rainbow {

    private RainbowImage rainbowImage;

    public RainbowBlobDetection(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public void onDrawingStart(RainbowInputController rainbowInputController) {
    }

    @Override
    public void onSketchSetup(RainbowDrawer rainbowDrawer) {
        rainbowImage = rainbowDrawer.loadImage(LibraryApplication.getContext(), , Bitmap.Config.ARGB_4444, new LoadPictureListener() {
            @Override
            public void onLoadSucceed(RainbowImage image) {

            }

            @Override
            public void onLoadFail() {

            }
        });
    }

    @Override
    public void onDrawingStop(RainbowInputController rainbowInputController) {
    }
}
