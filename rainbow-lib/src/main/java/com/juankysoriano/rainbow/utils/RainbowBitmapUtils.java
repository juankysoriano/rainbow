package com.juankysoriano.rainbow.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class RainbowBitmapUtils {

    public static Bitmap getBitmap(Context context, int resId, int reqWidth, int reqHeight) {
        try {
            return Picasso.with(context).load(resId).resize(reqWidth, reqHeight).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, String path, int reqWidth, int reqHeight) {
        try {
            if (path.startsWith("http")) {
                return Picasso.with(context).load(path).resize(reqWidth, reqHeight).get();
            } else {
                return Picasso.with(context).load("file:" + path).resize(reqWidth, reqHeight).get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, File file, int reqWidth, int reqHeight) {
        try {
            return Picasso.with(context).load(file).resize(reqWidth, reqHeight).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {
        try {
            return Picasso.with(context).load(uri).resize(reqWidth, reqHeight).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, int resId, int reqWidth, int reqHeight, int mode) {
        try {
            if (mode == RainbowImage.LOAD_CENTER_CROP) {
                return Picasso.with(context).load(resId).resize(reqWidth, reqHeight).centerCrop().get();
            } else if (mode == RainbowImage.LOAD_CENTER_INSIDE) {
                return Picasso.with(context).load(resId).resize(reqWidth, reqHeight).centerInside().get();
            } else if (mode == RainbowImage.LOAD_ORIGINAL_SIZE) {
                return Picasso.with(context).load(resId).get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, String path, int reqWidth, int reqHeight, int mode) {
        try {
            if (path.startsWith("http")) {
                if (mode == RainbowImage.LOAD_CENTER_CROP) {
                    return Picasso.with(context).load(path).resize(reqWidth, reqHeight).centerCrop().get();
                } else if (mode == RainbowImage.LOAD_CENTER_INSIDE) {
                    return Picasso.with(context).load(path).resize(reqWidth, reqHeight).centerInside().get();
                } else if (mode == RainbowImage.LOAD_ORIGINAL_SIZE) {
                    return Picasso.with(context).load(path).get();
                }
            } else {
                if (mode == RainbowImage.LOAD_CENTER_CROP) {
                    return Picasso.with(context).load("file:" + path).resize(reqWidth, reqHeight).centerCrop().get();
                } else if (mode == RainbowImage.LOAD_CENTER_INSIDE) {
                    return Picasso.with(context).load("file:" + path).resize(reqWidth, reqHeight).centerInside().get();
                } else if (mode == RainbowImage.LOAD_ORIGINAL_SIZE) {
                    return Picasso.with(context).load("file:" + path).get();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, File file, int reqWidth, int reqHeight, int mode) {
        try {
            if (mode == RainbowImage.LOAD_CENTER_CROP) {
                return Picasso.with(context).load(file).resize(reqWidth, reqHeight).centerCrop().get();
            } else if (mode == RainbowImage.LOAD_CENTER_INSIDE) {
                return Picasso.with(context).load(file).resize(reqWidth, reqHeight).centerInside().get();
            } else if (mode == RainbowImage.LOAD_ORIGINAL_SIZE) {
                return Picasso.with(context).load(file).get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmap(Context context, Uri uri, int reqWidth, int reqHeight, int mode) {
        try {
            if (mode == RainbowImage.LOAD_CENTER_CROP) {
                return Picasso.with(context).load(uri).resize(reqWidth, reqHeight).centerCrop().get();
            } else if (mode == RainbowImage.LOAD_CENTER_INSIDE) {
                return Picasso.with(context).load(uri).resize(reqWidth, reqHeight).centerInside().get();
            } else if (mode == RainbowImage.LOAD_ORIGINAL_SIZE) {
                return Picasso.with(context).load(uri).get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
