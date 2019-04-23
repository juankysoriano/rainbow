package com.juankysoriano.rainbow.utils;

import android.graphics.Bitmap;
import android.net.Uri;

import com.juankysoriano.rainbow.core.drawing.Modes;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class RainbowBitmapUtils {

    public static Bitmap getBitmap(int resId, int reqWidth, int reqHeight, Modes.LoadMode mode) {
        try {
            if (mode == Modes.LoadMode.LOAD_CENTER_CROP) {
                return Picasso.get().load(resId).resize(reqWidth, reqHeight).centerCrop().get();
            } else if (mode == Modes.LoadMode.LOAD_CENTER_INSIDE) {
                return Picasso.get().load(resId).resize(reqWidth, reqHeight).centerInside().get();
            } else if (mode == Modes.LoadMode.LOAD_ORIGINAL_SIZE) {
                return Picasso.get().load(resId).get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(String path, int reqWidth, int reqHeight, Modes.LoadMode mode) {
        try {
            if (mode == Modes.LoadMode.LOAD_CENTER_CROP) {
                return Picasso.get().load(path).resize(reqWidth, reqHeight).centerCrop().get();
            } else if (mode == Modes.LoadMode.LOAD_CENTER_INSIDE) {
                return Picasso.get().load(path).resize(reqWidth, reqHeight).centerInside().get();
            } else if (mode == Modes.LoadMode.LOAD_ORIGINAL_SIZE) {
                return Picasso.get().load(path).get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(File file, int reqWidth, int reqHeight, Modes.LoadMode mode) {
        try {
            if (mode == Modes.LoadMode.LOAD_CENTER_CROP) {
                return Picasso.get().load(file).resize(reqWidth, reqHeight).centerCrop().get();
            } else if (mode == Modes.LoadMode.LOAD_CENTER_INSIDE) {
                return Picasso.get().load(file).resize(reqWidth, reqHeight).centerInside().get();
            } else if (mode == Modes.LoadMode.LOAD_ORIGINAL_SIZE) {
                return Picasso.get().load(file).get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(Uri uri, int reqWidth, int reqHeight, Modes.LoadMode mode) {
        try {
            if (mode == Modes.LoadMode.LOAD_CENTER_CROP) {
                return Picasso.get().load(uri).resize(reqWidth, reqHeight).centerCrop().get();
            } else if (mode == Modes.LoadMode.LOAD_CENTER_INSIDE) {
                return Picasso.get().load(uri).resize(reqWidth, reqHeight).centerInside().get();
            } else if (mode == Modes.LoadMode.LOAD_ORIGINAL_SIZE) {
                return Picasso.get().load(uri).get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
