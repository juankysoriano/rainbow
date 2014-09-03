package com.juankysoriano.rainbow.core.listeners;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;

public interface LoadPictureListener {
    public void onLoadSucceed(RainbowImage image);

    public void onLoadFail();
}