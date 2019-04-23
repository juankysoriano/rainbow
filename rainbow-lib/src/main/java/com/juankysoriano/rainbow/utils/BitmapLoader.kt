package com.juankysoriano.rainbow.utils

import android.graphics.Bitmap
import android.net.Uri
import com.juankysoriano.rainbow.core.drawing.Modes
import com.squareup.picasso.Picasso
import java.io.File

object BitmapLoader {

    fun loadBitmap(resId: Int, reqWidth: Int, reqHeight: Int, mode: Modes.LoadMode): Bitmap {
        return when (mode) {
            Modes.LoadMode.LOAD_CENTER_CROP -> Picasso.get().load(resId).resize(reqWidth, reqHeight).centerCrop().get()
            Modes.LoadMode.LOAD_CENTER_INSIDE -> Picasso.get().load(resId).resize(reqWidth, reqHeight).centerInside().get()
            Modes.LoadMode.LOAD_ORIGINAL_SIZE -> Picasso.get().load(resId).get()
        }
    }

    fun loadBitmap(path: String, reqWidth: Int, reqHeight: Int, mode: Modes.LoadMode): Bitmap {
        return when (mode) {
            Modes.LoadMode.LOAD_CENTER_CROP -> Picasso.get().load(path).resize(reqWidth, reqHeight).centerCrop().get()
            Modes.LoadMode.LOAD_CENTER_INSIDE -> Picasso.get().load(path).resize(reqWidth, reqHeight).centerInside().get()
            Modes.LoadMode.LOAD_ORIGINAL_SIZE -> Picasso.get().load(path).get()
        }
    }

    fun loadBitmap(file: File, reqWidth: Int, reqHeight: Int, mode: Modes.LoadMode): Bitmap {
        return when (mode) {
            Modes.LoadMode.LOAD_CENTER_CROP -> Picasso.get().load(file).resize(reqWidth, reqHeight).centerCrop().get()
            Modes.LoadMode.LOAD_CENTER_INSIDE -> Picasso.get().load(file).resize(reqWidth, reqHeight).centerInside().get()
            Modes.LoadMode.LOAD_ORIGINAL_SIZE -> Picasso.get().load(file).get()
        }
    }

    fun loadBitmap(uri: Uri, reqWidth: Int, reqHeight: Int, mode: Modes.LoadMode): Bitmap {
        return when (mode) {
            Modes.LoadMode.LOAD_CENTER_CROP -> Picasso.get().load(uri).resize(reqWidth, reqHeight).centerCrop().get()
            Modes.LoadMode.LOAD_CENTER_INSIDE -> Picasso.get().load(uri).resize(reqWidth, reqHeight).centerInside().get()
            Modes.LoadMode.LOAD_ORIGINAL_SIZE -> Picasso.get().load(uri).get()
        }
    }

}
