/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 Part of the Processing project - http://processing.org

 Copyright (c) 2004-10 Ben Fry and Casey Reas
 Copyright (c) 2001-04 Massachusetts Institute of Technology

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License version 2.1 as published by the Free Software Foundation.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General
 Public License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA
 */

package com.juankysoriano.rainbow.core.graphics;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.Modes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.juankysoriano.rainbow.core.drawing.Modes.Blend.REPLACE;
import static com.juankysoriano.rainbow.core.drawing.Modes.Filter.BLUR;
import static com.juankysoriano.rainbow.core.drawing.Modes.Filter.THRESHOLD;
import static com.juankysoriano.rainbow.core.drawing.Modes.Image.*;

/**
 * Storage class for pixel data. This is the base class for most image and pixel
 * information, such as PGraphics and the video library classes.
 * <p/>
 * Code for copying, resizing, scaling, and blending contributed by <A
 * HREF="http://www.toxi.co.uk">toxi</A>.
 * <p/>
 */
public class RainbowImage implements Cloneable {

    public static final int ALPHA_MASK = 0xff000000;
    public static final int RED_MASK = 0x00ff0000;
    public static final int GREEN_MASK = 0x0000ff00;
    public static final int BLUE_MASK = 0x000000ff;

    private static final int PRECISIONB = 15;
    private static final int PRECISIONF = 1 << PRECISIONB;
    private static final int PREC_MAXVAL = PRECISIONF - 1;
    private static final int PREC_ALPHA_SHIFT = 24 - PRECISIONB;
    private static final int PREC_RED_SHIFT = 16 - PRECISIONB;
    /**
     * Format for this image, one of RGB, ARGB or ALPHA. note that RGB images
     * still require 0xff in the high byte because of how they'll be manipulated
     * by other functions
     */
    public Modes.Image format;
    public int[] pixels;
    public int width, height;
    /**
     * Path to parent object that will be used with save(). This prevents users
     * from needing savePath() to use PImage.save().
     */
    public Rainbow parent;
    /**
     * Loaded pixels flag
     */
    public boolean loaded = false;
    /**
     * modified portion of the image
     */
    protected boolean modified;
    /**
     * Use ImageIO functions from Java 1.4 and later to handle image save.
     * Various formats are supported, typically jpeg, png, bmp, and wbmp. To get
     * a list of the supported formats for writing, use: <BR>
     * <TT>println(javax.imageio.ImageIO.getReaderFormatNames())</TT>
     */

    private Bitmap bitmap;
    private int fracU, ifU, fracV, ifV, u1, u2, v1, v2, sX, sY, iw, iw1, ih1;
    private int ul, ll, ur, lr, cUL, cLL, cUR, cLR;
    private int srcXOffset, srcYOffset;
    private int r, g, b, a;
    private int[] srcBuffer;
    private int blurRadius;
    private int blurKernelSize;
    private int[] blurKernel;
    private int[][] blurMult;

    /**
     * Create an empty image object, set its format to RGB. The pixel array is
     * not allocated.
     */
    public RainbowImage() {
        format = RGB;
    }

    /**
     * Create a new RGB (alpha ignored) image of a specific size. All pixels are
     * set to zero, meaning black, but since the alpha is zero, it will be
     * transparent.
     */
    public RainbowImage(int width, int height) {
        init(width, height, RGB);
    }

    /**
     * Function to be used by subclasses of PImage to init later than at the
     * constructor, or re-init later when things changes. Used by Capture and
     * Movie classes (and perhaps others), because the width/height will not be
     * known when super() is called. (Leave this public so that other libraries
     * can do the same.)
     */
    public void init(int width, int height, Modes.Image format) { // ignore
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        this.format = format;
    }

    // ////////////////////////////////////////////////////////////

    public RainbowImage(int width, int height, Modes.Image format) {
        init(width, height, format);
    }

    /**
     * Construct a new PImage from an Android normalBitmap. The pixels[] array is not
     * initialized, nor is data copied to it, until loadPixels() is called.
     */
    public RainbowImage(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.pixels = null;
        this.format = bitmap.hasAlpha() ? ARGB : RGB;
    }

    public RainbowImage(RainbowImage rainbowImage) {
        Bitmap bitmap = Bitmap.createBitmap(rainbowImage.getBitmap(), 0, 0, rainbowImage.width, rainbowImage.height);
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.pixels = null;
        this.format = bitmap.hasAlpha() ? ARGB : RGB;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        if (this.bitmap == null || !this.bitmap.equals(bitmap)) {
            this.bitmap = bitmap;
        } else {
            this.bitmap.recycle();
            this.bitmap = bitmap;
        }
    }

    /**
     * Blend two colors based on a particular mode.
     * <UL>
     * <LI>REPLACE - destination colour equals colour of source pixel: C = A.
     * Sometimes called "Normal" or "Copy" in other software.
     * <p/>
     * <LI>BLEND - linear interpolation of colours: <TT>C = A*factor + B</TT>
     * <p/>
     * <LI>ADD - additive blending with white clip:
     * <TT>C = min(A*factor + B, 255)</TT>. Clipped to 0..255, Photoshop calls
     * this "Linear Burn", and Director calls it "Add Pin".
     * <p/>
     * <LI>SUBTRACT - substractive blend with black clip:
     * <TT>C = max(B - A*factor, 0)</TT>. Clipped to 0..255, Photoshop calls
     * this "Linear Dodge", and Director calls it "Subtract Pin".
     * <p/>
     * <LI>DARKEST - only the darkest colour succeeds:
     * <TT>C = min(A*factor, B)</TT>. Illustrator calls this "Darken".
     * <p/>
     * <LI>LIGHTEST - only the lightest colour succeeds:
     * <TT>C = max(A*factor, B)</TT>. Illustrator calls this "Lighten".
     * <p/>
     * <LI>DIFFERENCE - subtract colors from underlying image.
     * <p/>
     * <LI>EXCLUSION - similar to DIFFERENCE, but less extreme.
     * <p/>
     * <LI>MULTIPLY - Multiply the colors, result will always be darker.
     * <p/>
     * <LI>SCREEN - Opposite multiply, uses inverse values of the colors.
     * <p/>
     * <LI>OVERLAY - A mix of MULTIPLY and SCREEN. Multiplies dark values, and
     * screens light values.
     * <p/>
     * <LI>HARD_LIGHT - SCREEN when greater than 50% gray, MULTIPLY when lower.
     * <p/>
     * <LI>SOFT_LIGHT - Mix of DARKEST and LIGHTEST. Works like OVERLAY, but not
     * as harsh.
     * <p/>
     * <LI>DODGE - Lightens light tones and increases contrast, ignores darks.
     * Called "Color Dodge" in Illustrator and Photoshop.
     * <p/>
     * <LI>BURN - Darker areas are applied, increasing contrast, ignores lights.
     * Called "Color Burn" in Illustrator and Photoshop.
     * </UL>
     * <p/>
     * A useful reference for blending modes and their algorithms can be found
     * in the <A HREF="http://www.w3.org/TR/SVG12/rendering.html">SVG</A>
     * specification.
     * </P>
     * <p/>
     * It is important to note that Processing uses "fast" code, not necessarily
     * "correct" code. No biggie, most software does. A nitpicker can find
     * numerous "off by 1 division" problems in the blend code where
     * <TT>&gt;&gt;8</TT> or <TT>&gt;&gt;7</TT> is used when strictly speaking
     * <TT>/255.0</T> or <TT>/127.0</TT> should have been used.
     * </P>
     * <p/>
     * For instance, exclusion (not intended for real-time use) reads
     * <TT>r1 + r2 - ((2 * r1 * r2) / 255)</TT> because <TT>255 == 1.0</TT> not
     * <TT>256 == 1.0</TT>. In other words, <TT>(255*255)>>8</TT> is not the
     * same as <TT>(255*255)/255</TT>. But for real-time use the shifts are
     * preferrable, and the difference is insignificant for applications built
     * with Processing.
     * </P>
     */
    public static int blendColor(int c1, int c2, Modes.Blend mode) { // ignore
        switch (mode) {
            case REPLACE:
                return c2;
            case BLEND:
                return blend_blend(c1, c2);

            case ADD:
                return blend_add_pin(c1, c2);
            case SUBTRACT:
                return blend_sub_pin(c1, c2);

            case LIGHTEST:
                return blend_lightest(c1, c2);
            case DARKEST:
                return blend_darkest(c1, c2);

            case DIFFERENCE:
                return blend_difference(c1, c2);
            case EXCLUSION:
                return blend_exclusion(c1, c2);

            case MULTIPLY:
                return blend_multiply(c1, c2);
            case SCREEN:
                return blend_screen(c1, c2);

            case HARD_LIGHT:
                return blend_hard_light(c1, c2);
            case SOFT_LIGHT:
                return blend_soft_light(c1, c2);
            case OVERLAY:
                return blend_overlay(c1, c2);

            case DODGE:
                return blend_dodge(c1, c2);
            case BURN:
                return blend_burn(c1, c2);
        }
        return 0;
    }

    private static int blend_blend(int a, int b) {
        int f = (b & ALPHA_MASK) >>> 24;

        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | mix(a & RED_MASK, b & RED_MASK, f) & RED_MASK | mix(a & GREEN_MASK, b & GREEN_MASK, f) & GREEN_MASK | mix(
                a & BLUE_MASK,
                b & BLUE_MASK,
                f
        ));
    }

    /**
     * additive blend with clipping
     */
    private static int blend_add_pin(int a, int b) {
        int f = (b & ALPHA_MASK) >>> 24;

        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | low(((a & RED_MASK) + ((b & RED_MASK) >> 8) * f), RED_MASK) & RED_MASK
                | low(((a & GREEN_MASK) + ((b & GREEN_MASK) >> 8) * f), GREEN_MASK) & GREEN_MASK | low((a & BLUE_MASK) + (((b & BLUE_MASK) * f) >> 8), BLUE_MASK));
    }

    /**
     * subtractive blend with clipping
     */
    private static int blend_sub_pin(int a, int b) {
        int f = (b & ALPHA_MASK) >>> 24;

        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | high(((a & RED_MASK) - ((b & RED_MASK) >> 8) * f), GREEN_MASK) & RED_MASK
                | high(((a & GREEN_MASK) - ((b & GREEN_MASK) >> 8) * f), BLUE_MASK) & GREEN_MASK | high((a & BLUE_MASK) - (((b & BLUE_MASK) * f) >> 8), 0));
    }

    /**
     * only returns the blended lightest colour
     */
    private static int blend_lightest(int a, int b) {
        int f = (b & ALPHA_MASK) >>> 24;

        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | high(a & RED_MASK, ((b & RED_MASK) >> 8) * f) & RED_MASK | high(a & GREEN_MASK, ((b & GREEN_MASK) >> 8) * f) & GREEN_MASK | high(a
                                                                                                                                                                                                          & BLUE_MASK, ((b & BLUE_MASK) * f) >> 8));
    }

    /**
     * only returns the blended darkest colour
     */
    private static int blend_darkest(int a, int b) {
        int f = (b & ALPHA_MASK) >>> 24;

        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | mix(a & RED_MASK, low(a & RED_MASK, ((b & RED_MASK) >> 8) * f), f) & RED_MASK
                | mix(a & GREEN_MASK, low(a & GREEN_MASK, ((b & GREEN_MASK) >> 8) * f), f) & GREEN_MASK | mix(a & BLUE_MASK, low(a & BLUE_MASK, ((b & BLUE_MASK) * f) >> 8), f));
    }

    /**
     * returns the absolute value of the difference of the input colors C = |A -
     * B|
     */
    private static int blend_difference(int a, int b) {
        // setup (this portion will always be the same)
        int f = (b & ALPHA_MASK) >>> 24;
        int ar = (a & RED_MASK) >> 16;
        int ag = (a & GREEN_MASK) >> 8;
        int ab = (a & BLUE_MASK);
        int br = (b & RED_MASK) >> 16;
        int bg = (b & GREEN_MASK) >> 8;
        int bb = (b & BLUE_MASK);
        // formula:
        int cr = (ar > br) ? (ar - br) : (br - ar);
        int cg = (ag > bg) ? (ag - bg) : (bg - ag);
        int cb = (ab > bb) ? (ab - bb) : (bb - ab);
        // alpha blend (this portion will always be the same)
        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | (peg(ar + (((cr - ar) * f) >> 8)) << 16) | (peg(ag + (((cg - ag) * f) >> 8)) << 8) | (peg(ab + (((cb - ab) * f) >> 8))));
    }

    /**
     * Cousin of difference, algorithm used here is based on a Lingo version
     * found here: http://www.mediamacros.com/item/item-1006687616/ (Not yet
     * verified to be correct).
     */
    private static int blend_exclusion(int a, int b) {
        // setup (this portion will always be the same)
        int f = (b & ALPHA_MASK) >>> 24;
        int ar = (a & RED_MASK) >> 16;
        int ag = (a & GREEN_MASK) >> 8;
        int ab = (a & BLUE_MASK);
        int br = (b & RED_MASK) >> 16;
        int bg = (b & GREEN_MASK) >> 8;
        int bb = (b & BLUE_MASK);
        // formula:
        int cr = ar + br - ((ar * br) >> 7);
        int cg = ag + bg - ((ag * bg) >> 7);
        int cb = ab + bb - ((ab * bb) >> 7);
        // alpha blend (this portion will always be the same)
        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | (peg(ar + (((cr - ar) * f) >> 8)) << 16) | (peg(ag + (((cg - ag) * f) >> 8)) << 8) | (peg(ab + (((cb - ab) * f) >> 8))));
    }

    /**
     * returns the product of the input colors C = A * B
     */
    private static int blend_multiply(int a, int b) {
        // setup (this portion will always be the same)
        int f = (b & ALPHA_MASK) >>> 24;
        int ar = (a & RED_MASK) >> 16;
        int ag = (a & GREEN_MASK) >> 8;
        int ab = (a & BLUE_MASK);
        int br = (b & RED_MASK) >> 16;
        int bg = (b & GREEN_MASK) >> 8;
        int bb = (b & BLUE_MASK);
        // formula:
        int cr = (ar * br) >> 8;
        int cg = (ag * bg) >> 8;
        int cb = (ab * bb) >> 8;
        // alpha blend (this portion will always be the same)
        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | (peg(ar + (((cr - ar) * f) >> 8)) << 16) | (peg(ag + (((cg - ag) * f) >> 8)) << 8) | (peg(ab + (((cb - ab) * f) >> 8))));
    }

    // ////////////////////////////////////////////////////////////

    // COPYING IMAGE DATA

    /**
     * returns the inverse of the product of the inverses of the input colors
     * (the inverse of multiply). C = 1 - (1-A) * (1-B)
     */
    private static int blend_screen(int a, int b) {
        // setup (this portion will always be the same)
        int f = (b & ALPHA_MASK) >>> 24;
        int ar = (a & RED_MASK) >> 16;
        int ag = (a & GREEN_MASK) >> 8;
        int ab = (a & BLUE_MASK);
        int br = (b & RED_MASK) >> 16;
        int bg = (b & GREEN_MASK) >> 8;
        int bb = (b & BLUE_MASK);
        // formula:
        int cr = 255 - (((255 - ar) * (255 - br)) >> 8);
        int cg = 255 - (((255 - ag) * (255 - bg)) >> 8);
        int cb = 255 - (((255 - ab) * (255 - bb)) >> 8);
        // alpha blend (this portion will always be the same)
        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | (peg(ar + (((cr - ar) * f) >> 8)) << 16) | (peg(ag + (((cg - ag) * f) >> 8)) << 8) | (peg(ab + (((cb - ab) * f) >> 8))));
    }

    /**
     * returns either multiply or screen for darker or lighter values of B (the
     * inverse of overlay) C = B < 0.5 : 2 * A * B B >=0.5 : 1 - (2 * (255-A) *
     * (255-B))
     */
    private static int blend_hard_light(int a, int b) {
        // setup (this portion will always be the same)
        int f = (b & ALPHA_MASK) >>> 24;
        int ar = (a & RED_MASK) >> 16;
        int ag = (a & GREEN_MASK) >> 8;
        int ab = (a & BLUE_MASK);
        int br = (b & RED_MASK) >> 16;
        int bg = (b & GREEN_MASK) >> 8;
        int bb = (b & BLUE_MASK);
        // formula:
        int cr = (br < 128) ? ((ar * br) >> 7) : (255 - (((255 - ar) * (255 - br)) >> 7));
        int cg = (bg < 128) ? ((ag * bg) >> 7) : (255 - (((255 - ag) * (255 - bg)) >> 7));
        int cb = (bb < 128) ? ((ab * bb) >> 7) : (255 - (((255 - ab) * (255 - bb)) >> 7));
        // alpha blend (this portion will always be the same)
        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | (peg(ar + (((cr - ar) * f) >> 8)) << 16) | (peg(ag + (((cg - ag) * f) >> 8)) << 8) | (peg(ab + (((cb - ab) * f) >> 8))));
    }

    // ////////////////////////////////////////////////////////////

    // MARKING IMAGE AS LOADED / FOR USE IN RENDERERS

    /**
     * returns the inverse multiply plus screen, which simplifies to C = 2AB +
     * A^2 - 2A^2B
     */
    private static int blend_soft_light(int a, int b) {
        // setup (this portion will always be the same)
        int f = (b & ALPHA_MASK) >>> 24;
        int ar = (a & RED_MASK) >> 16;
        int ag = (a & GREEN_MASK) >> 8;
        int ab = (a & BLUE_MASK);
        int br = (b & RED_MASK) >> 16;
        int bg = (b & GREEN_MASK) >> 8;
        int bb = (b & BLUE_MASK);
        // formula:
        int cr = ((ar * br) >> 7) + ((ar * ar) >> 8) - ((ar * ar * br) >> 15);
        int cg = ((ag * bg) >> 7) + ((ag * ag) >> 8) - ((ag * ag * bg) >> 15);
        int cb = ((ab * bb) >> 7) + ((ab * ab) >> 8) - ((ab * ab * bb) >> 15);
        // alpha blend (this portion will always be the same)
        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | (peg(ar + (((cr - ar) * f) >> 8)) << 16) | (peg(ag + (((cg - ag) * f) >> 8)) << 8) | (peg(ab + (((cb - ab) * f) >> 8))));
    }

    /**
     * returns either multiply or screen for darker or lighter values of A (the
     * inverse of hard light) C = A < 0.5 : 2 * A * B A >=0.5 : 1 - (2 * (255-A)
     * * (255-B))
     */
    private static int blend_overlay(int a, int b) {
        // setup (this portion will always be the same)
        int f = (b & ALPHA_MASK) >>> 24;
        int ar = (a & RED_MASK) >> 16;
        int ag = (a & GREEN_MASK) >> 8;
        int ab = (a & BLUE_MASK);
        int br = (b & RED_MASK) >> 16;
        int bg = (b & GREEN_MASK) >> 8;
        int bb = (b & BLUE_MASK);
        // formula:
        int cr = (ar < 128) ? ((ar * br) >> 7) : (255 - (((255 - ar) * (255 - br)) >> 7));
        int cg = (ag < 128) ? ((ag * bg) >> 7) : (255 - (((255 - ag) * (255 - bg)) >> 7));
        int cb = (ab < 128) ? ((ab * bb) >> 7) : (255 - (((255 - ab) * (255 - bb)) >> 7));
        // alpha blend (this portion will always be the same)
        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | (peg(ar + (((cr - ar) * f) >> 8)) << 16) | (peg(ag + (((cg - ag) * f) >> 8)) << 8) | (peg(ab + (((cb - ab) * f) >> 8))));
    }

    /**
     * Returns the first (underlay) color divided by the inverse of the second
     * (overlay) color. C = A / (255-B)
     */
    private static int blend_dodge(int a, int b) {
        // setup (this portion will always be the same)
        int f = (b & ALPHA_MASK) >>> 24;
        int ar = (a & RED_MASK) >> 16;
        int ag = (a & GREEN_MASK) >> 8;
        int ab = (a & BLUE_MASK);
        int br = (b & RED_MASK) >> 16;
        int bg = (b & GREEN_MASK) >> 8;
        int bb = (b & BLUE_MASK);
        // formula:
        int cr = (br == 255) ? 255 : peg((ar << 8) / (255 - br)); // division
        // requires
        // pre-peg()-ing
        int cg = (bg == 255) ? 255 : peg((ag << 8) / (255 - bg)); // "
        int cb = (bb == 255) ? 255 : peg((ab << 8) / (255 - bb)); // "
        // alpha blend (this portion will always be the same)
        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | (peg(ar + (((cr - ar) * f) >> 8)) << 16) | (peg(ag + (((cg - ag) * f) >> 8)) << 8) | (peg(ab + (((cb - ab) * f) >> 8))));
    }

    // ////////////////////////////////////////////////////////////

    // GET/SET PIXELS

    /**
     * returns the inverse of the inverse of the first (underlay) color divided
     * by the second (overlay) color. C = 255 - (255-A) / B
     */
    private static int blend_burn(int a, int b) {
        // setup (this portion will always be the same)
        int f = (b & ALPHA_MASK) >>> 24;
        int ar = (a & RED_MASK) >> 16;
        int ag = (a & GREEN_MASK) >> 8;
        int ab = (a & BLUE_MASK);
        int br = (b & RED_MASK) >> 16;
        int bg = (b & GREEN_MASK) >> 8;
        int bb = (b & BLUE_MASK);
        // formula:
        int cr = (br == 0) ? 0 : 255 - peg(((255 - ar) << 8) / br); // division
        // requires
        // pre-peg()-ing
        int cg = (bg == 0) ? 0 : 255 - peg(((255 - ag) << 8) / bg); // "
        int cb = (bb == 0) ? 0 : 255 - peg(((255 - ab) << 8) / bb); // "
        // alpha blend (this portion will always be the same)
        return (low(((a & ALPHA_MASK) >>> 24) + f, 0xff) << 24 | (peg(ar + (((cr - ar) * f) >> 8)) << 16) | (peg(ag + (((cg - ag) * f) >> 8)) << 8) | (peg(ab + (((cb - ab) * f) >> 8))));
    }

    private static int low(int a, int b) {
        return (a < b) ? a : b;
    }

    private static int mix(int a, int b, int f) {
        return a + (((b - a) * f) >> 8);
    }

    /**
     * Grab a subsection of a PImage, and copy it into a fresh PImage. As of
     * release 0149, no longer honors imageMode() for the coordinates.
     */

    private static int high(int a, int b) {
        return (a > b) ? a : b;
    }

    private static int peg(int n) {
        return (n < 0) ? 0 : ((n > 255) ? 255 : n);
    }

    public void recycle() {
        if (this.bitmap != null) {
            this.bitmap.recycle();
            this.pixels = null;
            this.bitmap = null;
        }
    }

    /**
     * Returns the native Bitmap object for this PImage.
     */
    public Object getNative() {
        return bitmap;
    }

    /**
     * Mark the pixels in this region as needing an update.
     * <p/>
     * This is not currently used by any of the renderers, however the api is
     * structured this way in the hope of being able to use this to speed things
     * up in the future.
     */
    public void updatePixels(int x, int y, int w, int h) { // ignore
        setModified();
    }

    public void setModified() { // ignore
        modified = true;
        if (loaded) {
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        }
    }

    /**
     * Duplicate an image, returns new PImage object. The pixels[] array for the
     * new object will be unique and recopied from the source image. This is
     * implemented as an override of Object.clone(). We recommend using get()
     * instead, because it prevents you from needing to catch the
     * CloneNotSupportedException, and from doing a cast from the result.
     */
    @Override
    public Object clone() throws CloneNotSupportedException { // ignore
        return get();
    }

    /**
     * Returns a copy of this PImage. Equivalent to get(0, 0, width, height).
     */
    public RainbowImage get() {
        return get(0, 0, width, height);
    }

    /**
     * @param w width of pixel rectangle to get
     * @param h height of pixel rectangle to get
     */
    public RainbowImage get(int x, int y, int w, int h) {
        int targetX = 0;
        int targetY = 0;
        int targetWidth = w;
        int targetHeight = h;
        boolean cropped = false;

        if (x < 0) {
            w += x; // x is negative, removes the left edge from the width
            targetX = -x;
            cropped = true;
            x = 0;
        }
        if (y < 0) {
            h += y; // y is negative, clip the number of rows
            targetY = -y;
            cropped = true;
            y = 0;
        }

        if (x + w > width) {
            w = width - x;
            cropped = true;
        }
        if (y + h > height) {
            h = height - y;
            cropped = true;
        }

        if (w < 0) {
            w = 0;
        }
        if (h < 0) {
            h = 0;
        }

        Modes.Image targetFormat = format;
        if (cropped && format == RGB) {
            targetFormat = ARGB;
        }

        RainbowImage target = new RainbowImage(targetWidth, targetHeight, targetFormat);
        target.parent = parent; // parent may be null so can't use createImage()
        if (w > 0 && h > 0) {
            getImpl(x, y, w, h, target, targetX, targetY);
        }
        return target;
    }

    /**
     * Internal function to actually handle getting a block of pixels that has
     * already been properly cropped to a valid region. That is, x/y/w/h are
     * guaranteed to be inside the image space, so the implementation can use
     * the fastest possible pixel copying method.
     */
    protected void getImpl(int sourceX, int sourceY, int sourceWidth, int sourceHeight, RainbowImage target, int targetX, int targetY) {
        if (pixels == null) {
            bitmap.getPixels(target.pixels, targetY * target.width + targetX, target.width, sourceX, sourceY, sourceWidth, sourceHeight);
        } else {
            int sourceIndex = sourceY * width + sourceX;
            int targetIndex = targetY * target.width + targetX;
            for (int row = 0; row < sourceHeight; row++) {
                System.arraycopy(pixels, sourceIndex, target.pixels, targetIndex, sourceWidth);
                sourceIndex += width;
                targetIndex += target.width;
            }
        }
    }

    /**
     * Resize this image to a new width and height. Use 0 for wide or high to
     * make that dimension scale proportionally.
     */
    public RainbowImage resize(int w, int h) { // ignore
        if (w <= 0 && h <= 0) {
            throw new IllegalArgumentException("width or height must be > 0 for resize");
        }

        if (w == 0) { // Use height to determine relative size
            float diff = (float) h / (float) height;
            w = (int) (width * diff);
        } else if (h == 0) { // Use the width to determine relative size
            float diff = (float) w / (float) width;
            h = (int) (height * diff);
        }
        if (this.bitmap == null) {
            this.bitmap = Bitmap.createBitmap(pixels, w, h, Config.ARGB_4444);
        } else {
            Bitmap newBitmap = Bitmap.createScaledBitmap(this.bitmap, w, h, true);
            this.bitmap.recycle();
            this.bitmap = newBitmap;
        }
        this.width = w;
        this.height = h;

        loadPixels();
        updatePixels();
        return this;
    }

    /**
     * Call this when finished messing with the pixels[] array.
     * <p/>
     * Mark all pixels as needing update.
     */
    public void updatePixels() { // ignore
        setModified();
    }

    public boolean isLoaded() { // ignore
        return loaded;
    }

    /**
     * Returns an ARGB "color" type (a packed 32 bit int with the color. If the
     * coordinate is outside the image, zero is returned (black, but completely
     * transparent).
     * <p/>
     * If the image is in RGB format (i.e. on a PVideo object), the value will
     * get its high bits set, just to avoid cases where they haven't been set
     * already.
     * <p/>
     * If the image is in ALPHA format, this returns a white with its alpha
     * value set.
     * <p/>
     * This function is included primarily for beginners. It is quite slow
     * because it has to check to see if the x, y that was provided is inside
     * the bounds, and then has to check to see what image type it is. If you
     * want things to be more efficient, access the pixels[] array directly.
     */
    public int get(int x, int y) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            return 0;
        }

        if (pixels == null) {
            return bitmap.getPixel(x, y);

        } else {
            switch (format) {
                case RGB:
                    return pixels[y * width + x] | 0xff000000;

                case ARGB:
                    return pixels[y * width + x];

                case ALPHA:
                    return (pixels[y * width + x] << 24) | 0xffffff;
            }
        }
        return 0;
    }

    /**
     * Set a single pixel to the specified color.
     */
    public void set(int x, int y, int c) {
        if (pixels == null) {
            bitmap.setPixel(x, y, c);

        } else {
            if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
                return;
            }
            pixels[y * width + x] = c;
            setModified();
        }
    }

    /**
     * Efficient method of drawing an image's pixels directly to this surface.
     * No variations are employed, meaning that any scale, tint, or imageMode
     * settings will be ignored.
     */
    public void set(int x, int y, RainbowImage img) {
        if (img.format == ALPHA) {
            throw new IllegalArgumentException("set() not available for ALPHA images");
        }

        int sx = 0;
        int sy = 0;
        int sw = img.width;
        int sh = img.height;

        if (x < 0) { // off left edge
            sx -= x;
            sw += x;
            x = 0;
        }
        if (y < 0) { // off top edge
            sy -= y;
            sh += y;
            y = 0;
        }
        if (x + sw > width) { // off right edge
            sw = width - x;
        }
        if (y + sh > height) { // off bottom edge
            sh = height - y;
        }

        if ((sw <= 0) || (sh <= 0)) {
            return;
        }

        setImpl(img, sx, sy, sw, sh, x, y);
    }

    /**
     * Internal function to actually handle setting a block of pixels that has
     * already been properly cropped from the image to a valid region.
     */
    protected void setImpl(RainbowImage sourceImage, int sourceX, int sourceY, int sourceWidth, int sourceHeight, int targetX, int targetY) {
        if (sourceImage.pixels == null) {
            sourceImage.loadPixels();
        }

        if (pixels == null) {
            if (!bitmap.isMutable()) {
                if (bitmap != null) {
                    bitmap = bitmap.copy(Config.ARGB_4444, true);
                } else {
                    Bitmap copy = bitmap.copy(Config.ARGB_4444, true);
                    bitmap.recycle();
                    bitmap = copy;
                }
            }

            int offset = sourceY * sourceImage.width + sourceX;
            bitmap.setPixels(sourceImage.pixels, offset, sourceImage.width, targetX, targetY, sourceWidth, sourceHeight);

        } else {
            int srcOffset = sourceY * sourceImage.width + sourceX;
            int dstOffset = targetY * width + targetX;
            for (int y = sourceY; y < sourceY + sourceHeight; y++) {
                System.arraycopy(sourceImage.pixels, srcOffset, pixels, dstOffset, sourceWidth);
                srcOffset += sourceImage.width;
                dstOffset += width;
            }
            setModified();
        }
    }

    /**
     * Call this when you want to mess with the pixels[] array.
     * <p/>
     * For subclasses where the pixels[] buffer isn't set by default, this
     * should copy all data into the pixels[] array
     */
    public void loadPixels() { // ignore
        if (pixels == null || pixels.length != width * height) {
            pixels = new int[width * height];
        }
        if (bitmap != null && !modified) {
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        }
        setLoaded();
    }

    public void setLoaded() { // ignore
        loaded = true;
    }

    /**
     * Set alpha channel for an image using another image as the source.
     */
    public void mask(RainbowImage alpha) {
        if (alpha.pixels == null) {
            alpha.loadPixels();
            mask(alpha.pixels);
            alpha.pixels = null;
        } else {
            mask(alpha.pixels);
        }
    }

    /**
     * Set alpha channel for an image. Black colors in the source image will
     * make the destination image completely transparent, and white will make
     * things fully opaque. Gray values will be in-between steps.
     * <p/>
     * Strictly speaking the "blue" value from the source image is used as the
     * alpha color. For a fully grayscale image, this is correct, but for a
     * color image it's not 100% accurate. For a more accurate conversion, first
     * use filter(GRAY) which will make the image into a "correct" grayscake by
     * performing a proper luminance-based conversion.
     */
    public void mask(int alpha[]) {
        loadPixels();
        if (alpha.length != pixels.length) {
            throw new RuntimeException("The PImage used with mask() must be " + "the same size as the applet.");
        }
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = ((alpha[i] & 0xff) << 24) | (pixels[i] & 0xffffff);
        }
        format = ARGB;
        updatePixels();
    }

    /**
     * Method to apply a variety of basic filters to this image.
     * <p/>
     * <UL>
     * <LI>filter(BLUR) provides a basic blur.
     * <LI>filter(GRAY) converts the image to grayscale based on luminance.
     * <LI>filter(INVERT) will invert the color components in the image.
     * <LI>filter(OPAQUE) set all the high bits in the image to opaque
     * <LI>filter(THRESHOLD) converts the image to black and white.
     * <LI>filter(DILATE) grow white/light areas
     * <LI>filter(ERODE) shrink white/light areas
     * </UL>
     * Luminance conversion code contributed by <A
     * HREF="http://www.toxi.co.uk">toxi</A>
     * <p/>
     * Gaussian blur code contributed by <A
     * HREF="http://incubator.quasimondo.com">Mario Klingemann</A>
     */
    public void filter(Modes.Filter mode) {
        loadPixels();

        switch (mode) {
            case BLUR:
                filter(BLUR, 1);
                break;

            case GRAY:
                if (format == ALPHA) {
                    for (int i = 0; i < pixels.length; i++) {
                        int col = 255 - pixels[i];
                        pixels[i] = 0xff000000 | (col << 16) | (col << 8) | col;
                    }
                    format = RGB;

                } else {
                    for (int i = 0; i < pixels.length; i++) {
                        int col = pixels[i];
                        int lum = (77 * (col >> 16 & 0xff) + 151 * (col >> 8 & 0xff) + 28 * (col & 0xff)) >> 8;
                        pixels[i] = (col & ALPHA_MASK) | lum << 16 | lum << 8 | lum;
                    }
                }
                break;

            case INVERT:
                for (int i = 0; i < pixels.length; i++) {
                    pixels[i] ^= 0xffffff;
                }
                break;

            case POSTERIZE:
                throw new RuntimeException("Use filter(POSTERIZE, int levels) " + "instead of filter(POSTERIZE)");

            case RGB:
                for (int i = 0; i < pixels.length; i++) {
                    pixels[i] |= 0xff000000;
                }
                format = RGB;
                break;

            case THRESHOLD:
                filter(THRESHOLD, 0.5f);
                break;

            case ERODE:
                dilate(true);
                break;

            case DILATE:
                dilate(false);
                break;
        }
        updatePixels();
    }

    /**
     * Method to apply a variety of basic filters to this image. These filters
     * all take a parameter.
     * <p/>
     * <UL>
     * <LI>filter(BLUR, int radius) performs a gaussian blur of the specified
     * radius.
     * <LI>filter(POSTERIZE, int levels) will posterize the image to between 2
     * and 255 levels.
     * <LI>filter(THRESHOLD, float center) allows you to set the center point
     * for the threshold. It takes a value from 0 to 1.0.
     * </UL>
     * Gaussian blur code contributed by <A
     * HREF="http://incubator.quasimondo.com">Mario Klingemann</A> and later
     * updated by toxi for better speed.
     */
    public void filter(Modes.Filter mode, float param) {
        loadPixels();

        switch (mode) {
            case BLUR:
                if (format == ALPHA) {
                    blurAlpha(param);
                } else if (format == ARGB) {
                    blurARGB(param);
                } else {
                    blurRGB(param);
                }
                break;

            case GRAY:
                throw new RuntimeException("Use filter(GRAY) instead of " + "filter(GRAY, param)");

            case INVERT:
                throw new RuntimeException("Use filter(INVERT) instead of " + "filter(INVERT, param)");

            case OPAQUE:
                throw new RuntimeException("Use filter(OPAQUE) instead of " + "filter(OPAQUE, param)");

            case POSTERIZE:
                int levels = (int) param;
                if ((levels < 2) || (levels > 255)) {
                    throw new RuntimeException("Levels must be between 2 and 255 for " + "filter(POSTERIZE, levels)");
                }
                int levels1 = levels - 1;
                for (int i = 0; i < pixels.length; i++) {
                    int rlevel = (pixels[i] >> 16) & 0xff;
                    int glevel = (pixels[i] >> 8) & 0xff;
                    int blevel = pixels[i] & 0xff;
                    rlevel = (((rlevel * levels) >> 8) * 255) / levels1;
                    glevel = (((glevel * levels) >> 8) * 255) / levels1;
                    blevel = (((blevel * levels) >> 8) * 255) / levels1;
                    pixels[i] = ((0xff000000 & pixels[i]) | (rlevel << 16) | (glevel << 8) | blevel);
                }
                break;

            case THRESHOLD: // greater than or equal to the threshold
                int thresh = (int) (param * 255);
                for (int i = 0; i < pixels.length; i++) {
                    int max = Math.max((pixels[i] & RED_MASK) >> 16, Math.max((pixels[i] & GREEN_MASK) >> 8, (pixels[i] & BLUE_MASK)));
                    pixels[i] = (pixels[i] & ALPHA_MASK) | ((max < thresh) ? 0x000000 : 0xffffff);
                }
                break;

            case ERODE:
                throw new RuntimeException("Use filter(ERODE) instead of " + "filter(ERODE, param)");
            case DILATE:
                throw new RuntimeException("Use filter(DILATE) instead of " + "filter(DILATE, param)");
        }
        updatePixels(); // mark as modified
    }

    /**
     * Optimized code for building the blur kernel. further optimized blur code
     * (approx. 15% for radius=20) bigger speed gains for larger radii (~30%)
     * added support for various image types (ALPHA, RGB, ARGB) [toxi 050728]
     */
    protected void buildBlurKernel(float r) {
        int radius = (int) (r * 3.5f);
        radius = (radius < 1) ? 1 : ((radius < 248) ? radius : 248);
        if (blurRadius != radius) {
            blurRadius = radius;
            blurKernelSize = 1 + blurRadius << 1;
            blurKernel = new int[blurKernelSize];
            blurMult = new int[blurKernelSize][256];

            int bk, bki;
            int[] bm, bmi;

            for (int i = 1, radiusi = radius - 1; i < radius; i++) {
                blurKernel[radius + i] = blurKernel[radiusi] = bki = radiusi * radiusi;
                bm = blurMult[radius + i];
                bmi = blurMult[radiusi--];
                for (int j = 0; j < 256; j++) {
                    bm[j] = bmi[j] = bki * j;
                }
            }
            bk = blurKernel[radius] = radius * radius;
            bm = blurMult[radius];
            for (int j = 0; j < 256; j++) {
                bm[j] = bk * j;
            }
        }
    }

    protected void blurAlpha(float r) {
        int sum, cb;
        int read, ri, ym, ymi, bk0;
        int b2[] = new int[pixels.length];
        int yi = 0;

        buildBlurKernel(r);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cb = sum = 0;
                read = x - blurRadius;
                if (read < 0) {
                    bk0 = -read;
                    read = 0;
                } else {
                    if (read >= width) {
                        break;
                    }
                    bk0 = 0;
                }
                for (int i = bk0; i < blurKernelSize; i++) {
                    if (read >= width) {
                        break;
                    }
                    int c = pixels[read + yi];
                    int[] bm = blurMult[i];
                    cb += bm[c & BLUE_MASK];
                    sum += blurKernel[i];
                    read++;
                }
                ri = yi + x;
                b2[ri] = cb / sum;
            }
            yi += width;
        }

        yi = 0;
        ym = -blurRadius;
        ymi = ym * width;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cb = sum = 0;
                if (ym < 0) {
                    bk0 = ri = -ym;
                    read = x;
                } else {
                    if (ym >= height) {
                        break;
                    }
                    bk0 = 0;
                    ri = ym;
                    read = x + ymi;
                }
                for (int i = bk0; i < blurKernelSize; i++) {
                    if (ri >= height) {
                        break;
                    }
                    int[] bm = blurMult[i];
                    cb += bm[b2[read]];
                    sum += blurKernel[i];
                    ri++;
                    read += width;
                }
                pixels[x + yi] = (cb / sum);
            }
            yi += width;
            ymi += width;
            ym++;
        }
    }

    protected void blurRGB(float r) {
        int sum, cr, cg, cb;
        int read, ri, ym, ymi, bk0;
        int r2[] = new int[pixels.length];
        int g2[] = new int[pixels.length];
        int b2[] = new int[pixels.length];
        int yi = 0;

        buildBlurKernel(r);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cb = cg = cr = sum = 0;
                read = x - blurRadius;
                if (read < 0) {
                    bk0 = -read;
                    read = 0;
                } else {
                    if (read >= width) {
                        break;
                    }
                    bk0 = 0;
                }
                for (int i = bk0; i < blurKernelSize; i++) {
                    if (read >= width) {
                        break;
                    }
                    int c = pixels[read + yi];
                    int[] bm = blurMult[i];
                    cr += bm[(c & RED_MASK) >> 16];
                    cg += bm[(c & GREEN_MASK) >> 8];
                    cb += bm[c & BLUE_MASK];
                    sum += blurKernel[i];
                    read++;
                }
                ri = yi + x;
                r2[ri] = cr / sum;
                g2[ri] = cg / sum;
                b2[ri] = cb / sum;
            }
            yi += width;
        }

        yi = 0;
        ym = -blurRadius;
        ymi = ym * width;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cb = cg = cr = sum = 0;
                if (ym < 0) {
                    bk0 = ri = -ym;
                    read = x;
                } else {
                    if (ym >= height) {
                        break;
                    }
                    bk0 = 0;
                    ri = ym;
                    read = x + ymi;
                }
                for (int i = bk0; i < blurKernelSize; i++) {
                    if (ri >= height) {
                        break;
                    }
                    int[] bm = blurMult[i];
                    cr += bm[r2[read]];
                    cg += bm[g2[read]];
                    cb += bm[b2[read]];
                    sum += blurKernel[i];
                    ri++;
                    read += width;
                }
                pixels[x + yi] = 0xff000000 | (cr / sum) << 16 | (cg / sum) << 8 | (cb / sum);
            }
            yi += width;
            ymi += width;
            ym++;
        }
    }

    protected void blurARGB(float r) {
        int sum, cr, cg, cb, ca;
        int read, ri, ym, ymi, bk0;
        int wh = pixels.length;
        int r2[] = new int[wh];
        int g2[] = new int[wh];
        int b2[] = new int[wh];
        int a2[] = new int[wh];
        int yi = 0;

        buildBlurKernel(r);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cb = cg = cr = ca = sum = 0;
                read = x - blurRadius;
                if (read < 0) {
                    bk0 = -read;
                    read = 0;
                } else {
                    if (read >= width) {
                        break;
                    }
                    bk0 = 0;
                }
                for (int i = bk0; i < blurKernelSize; i++) {
                    if (read >= width) {
                        break;
                    }
                    int c = pixels[read + yi];
                    int[] bm = blurMult[i];
                    ca += bm[(c & ALPHA_MASK) >>> 24];
                    cr += bm[(c & RED_MASK) >> 16];
                    cg += bm[(c & GREEN_MASK) >> 8];
                    cb += bm[c & BLUE_MASK];
                    sum += blurKernel[i];
                    read++;
                }
                ri = yi + x;
                a2[ri] = ca / sum;
                r2[ri] = cr / sum;
                g2[ri] = cg / sum;
                b2[ri] = cb / sum;
            }
            yi += width;
        }

        yi = 0;
        ym = -blurRadius;
        ymi = ym * width;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cb = cg = cr = ca = sum = 0;
                if (ym < 0) {
                    bk0 = ri = -ym;
                    read = x;
                } else {
                    if (ym >= height) {
                        break;
                    }
                    bk0 = 0;
                    ri = ym;
                    read = x + ymi;
                }
                for (int i = bk0; i < blurKernelSize; i++) {
                    if (ri >= height) {
                        break;
                    }
                    int[] bm = blurMult[i];
                    ca += bm[a2[read]];
                    cr += bm[r2[read]];
                    cg += bm[g2[read]];
                    cb += bm[b2[read]];
                    sum += blurKernel[i];
                    ri++;
                    read += width;
                }
                pixels[x + yi] = (ca / sum) << 24 | (cr / sum) << 16 | (cg / sum) << 8 | (cb / sum);
            }
            yi += width;
            ymi += width;
            ym++;
        }
    }

    /**
     * Generic dilate/erode filter using luminance values as decision factor.
     * [toxi 050728]
     */
    protected void dilate(boolean isInverted) {
        int currIdx = 0;
        int maxIdx = pixels.length;
        int[] out = new int[maxIdx];

        if (!isInverted) {
            // erosion (grow light areas)
            while (currIdx < maxIdx) {
                int currRowIdx = currIdx;
                int maxRowIdx = currIdx + width;
                while (currIdx < maxRowIdx) {
                    int colOrig, colOut;
                    colOrig = colOut = pixels[currIdx];
                    int idxLeft = currIdx - 1;
                    int idxRight = currIdx + 1;
                    int idxUp = currIdx - width;
                    int idxDown = currIdx + width;
                    if (idxLeft < currRowIdx) {
                        idxLeft = currIdx;
                    }
                    if (idxRight >= maxRowIdx) {
                        idxRight = currIdx;
                    }
                    if (idxUp < 0) {
                        idxUp = 0;
                    }
                    if (idxDown >= maxIdx) {
                        idxDown = currIdx;
                    }

                    int colUp = pixels[idxUp];
                    int colLeft = pixels[idxLeft];
                    int colDown = pixels[idxDown];
                    int colRight = pixels[idxRight];

                    // compute luminance
                    int currLum = 77 * (colOrig >> 16 & 0xff) + 151 * (colOrig >> 8 & 0xff) + 28 * (colOrig & 0xff);
                    int lumLeft = 77 * (colLeft >> 16 & 0xff) + 151 * (colLeft >> 8 & 0xff) + 28 * (colLeft & 0xff);
                    int lumRight = 77 * (colRight >> 16 & 0xff) + 151 * (colRight >> 8 & 0xff) + 28 * (colRight & 0xff);
                    int lumUp = 77 * (colUp >> 16 & 0xff) + 151 * (colUp >> 8 & 0xff) + 28 * (colUp & 0xff);
                    int lumDown = 77 * (colDown >> 16 & 0xff) + 151 * (colDown >> 8 & 0xff) + 28 * (colDown & 0xff);

                    if (lumLeft > currLum) {
                        colOut = colLeft;
                        currLum = lumLeft;
                    }
                    if (lumRight > currLum) {
                        colOut = colRight;
                        currLum = lumRight;
                    }
                    if (lumUp > currLum) {
                        colOut = colUp;
                        currLum = lumUp;
                    }
                    if (lumDown > currLum) {
                        colOut = colDown;
                    }
                    out[currIdx++] = colOut;
                }
            }
        } else {
            // dilate (grow dark areas)
            while (currIdx < maxIdx) {
                int currRowIdx = currIdx;
                int maxRowIdx = currIdx + width;
                while (currIdx < maxRowIdx) {
                    int colOrig, colOut;
                    colOrig = colOut = pixels[currIdx];
                    int idxLeft = currIdx - 1;
                    int idxRight = currIdx + 1;
                    int idxUp = currIdx - width;
                    int idxDown = currIdx + width;
                    if (idxLeft < currRowIdx) {
                        idxLeft = currIdx;
                    }
                    if (idxRight >= maxRowIdx) {
                        idxRight = currIdx;
                    }
                    if (idxUp < 0) {
                        idxUp = 0;
                    }
                    if (idxDown >= maxIdx) {
                        idxDown = currIdx;
                    }

                    int colUp = pixels[idxUp];
                    int colLeft = pixels[idxLeft];
                    int colDown = pixels[idxDown];
                    int colRight = pixels[idxRight];

                    // compute luminance
                    int currLum = 77 * (colOrig >> 16 & 0xff) + 151 * (colOrig >> 8 & 0xff) + 28 * (colOrig & 0xff);
                    int lumLeft = 77 * (colLeft >> 16 & 0xff) + 151 * (colLeft >> 8 & 0xff) + 28 * (colLeft & 0xff);
                    int lumRight = 77 * (colRight >> 16 & 0xff) + 151 * (colRight >> 8 & 0xff) + 28 * (colRight & 0xff);
                    int lumUp = 77 * (colUp >> 16 & 0xff) + 151 * (colUp >> 8 & 0xff) + 28 * (colUp & 0xff);
                    int lumDown = 77 * (colDown >> 16 & 0xff) + 151 * (colDown >> 8 & 0xff) + 28 * (colDown & 0xff);

                    if (lumLeft < currLum) {
                        colOut = colLeft;
                        currLum = lumLeft;
                    }
                    if (lumRight < currLum) {
                        colOut = colRight;
                        currLum = lumRight;
                    }
                    if (lumUp < currLum) {
                        colOut = colUp;
                        currLum = lumUp;
                    }
                    if (lumDown < currLum) {
                        colOut = colDown;
                        currLum = lumDown;
                    }
                    out[currIdx++] = colOut;
                }
            }
        }
        System.arraycopy(out, 0, pixels, 0, maxIdx);
    }

    /**
     * Copy things from one area of this image to another area in the same
     * image.
     */
    public void copy(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        blend(this, sx, sy, sw, sh, dx, dy, dw, dh, REPLACE);
    }

    /**
     * Copies area of one image into another PImage object.
     *
     * @see RainbowImage#blendColor(int, int, Modes.Blend)
     */
    public void blend(RainbowImage src, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, Modes.Blend mode) {
        int sx2 = sx + sw;
        int sy2 = sy + sh;
        int dx2 = dx + dw;
        int dy2 = dy + dh;

        loadPixels();
        if (src == this) {
            if (intersect(sx, sy, sx2, sy2, dx, dy, dx2, dy2)) {
                blit_resize(get(sx, sy, sx2 - sx, sy2 - sy), 0, 0, sx2 - sx - 1, sy2 - sy - 1, pixels, width, height, dx, dy, dx2, dy2, mode);
            } else {
                blit_resize(src, sx, sy, sx2, sy2, pixels, width, height, dx, dy, dx2, dy2, mode);
            }
        } else {
            src.loadPixels();
            blit_resize(src, sx, sy, sx2, sy2, pixels, width, height, dx, dy, dx2, dy2, mode);
        }
        updatePixels();
    }

    /**
     * Check to see if two rectangles intersect one another
     */
    private boolean intersect(int sx1, int sy1, int sx2, int sy2, int dx1, int dy1, int dx2, int dy2) {
        int sw = sx2 - sx1 + 1;
        int sh = sy2 - sy1 + 1;
        int dw = dx2 - dx1 + 1;
        int dh = dy2 - dy1 + 1;

        if (dx1 < sx1) {
            dw += dx1 - sx1;
            if (dw > sw) {
                dw = sw;
            }
        } else {
            int w = sw + sx1 - dx1;
            if (dw > w) {
                dw = w;
            }
        }
        if (dy1 < sy1) {
            dh += dy1 - sy1;
            if (dh > sh) {
                dh = sh;
            }
        } else {
            int h = sh + sy1 - dy1;
            if (dh > h) {
                dh = h;
            }
        }
        return !(dw <= 0 || dh <= 0);
    }

    /**
     * Internal blitter/resizer/copier from toxi. Uses bilinear filtering if
     * smooth() has been enabled 'mode' determines the blending mode used in the
     * process.
     */
    private void blit_resize(RainbowImage img, int srcX1, int srcY1, int srcX2, int srcY2, int[] destPixels, int screenW, int screenH, int destX1, int destY1, int destX2, int destY2, Modes.Blend mode) {
        if (srcX1 < 0) {
            srcX1 = 0;
        }
        if (srcY1 < 0) {
            srcY1 = 0;
        }
        if (srcX2 > img.width) {
            srcX2 = img.width;
        }
        if (srcY2 > img.height) {
            srcY2 = img.height;
        }

        int srcW = srcX2 - srcX1;
        int srcH = srcY2 - srcY1;
        int destW = destX2 - destX1;
        int destH = destY2 - destY1;

        if (destW <= 0 || destH <= 0 || srcW <= 0 || srcH <= 0 || destX1 >= screenW || destY1 >= screenH || srcX1 >= img.width || srcY1 >= img.height) {
            return;
        }

        int dx = (int) (srcW / (float) destW * PRECISIONF);
        int dy = (int) (srcH / (float) destH * PRECISIONF);

        srcXOffset = destX1 < 0 ? -destX1 * dx : srcX1 * PRECISIONF;
        srcYOffset = destY1 < 0 ? -destY1 * dy : srcY1 * PRECISIONF;

        if (destX1 < 0) {
            destW += destX1;
            destX1 = 0;
        }
        if (destY1 < 0) {
            destH += destY1;
            destY1 = 0;
        }

        destW = low(destW, screenW - destX1);
        destH = low(destH, screenH - destY1);

        int destOffset = destY1 * screenW + destX1;
        srcBuffer = img.pixels;

        // use bilinear filtering
        iw = img.width;
        iw1 = img.width - 1;
        ih1 = img.height - 1;

        switch (mode) {

            case BLEND:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        // davbol - renamed old blend_multiply to
                        // blend_blend
                        destPixels[destOffset + x] = blend_blend(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case ADD:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_add_pin(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case SUBTRACT:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_sub_pin(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case LIGHTEST:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_lightest(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case DARKEST:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_darkest(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case REPLACE:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = filter_bilinear();
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case DIFFERENCE:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_difference(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case EXCLUSION:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_exclusion(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case MULTIPLY:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_multiply(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case SCREEN:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_screen(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case OVERLAY:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_overlay(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case HARD_LIGHT:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_hard_light(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case SOFT_LIGHT:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_soft_light(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            // davbol - proposed 2007-01-09
            case DODGE:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_dodge(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

            case BURN:
                for (int y = 0; y < destH; y++) {
                    filter_new_scanline();
                    for (int x = 0; x < destW; x++) {
                        destPixels[destOffset + x] = blend_burn(destPixels[destOffset + x], filter_bilinear());
                        sX += dx;
                    }
                    destOffset += screenW;
                    srcYOffset += dy;
                }
                break;

        }

    }

    private void filter_new_scanline() {
        sX = srcXOffset;
        fracV = srcYOffset & PREC_MAXVAL;
        ifV = PREC_MAXVAL - fracV;
        v1 = (srcYOffset >> PRECISIONB) * iw;
        v2 = low((srcYOffset >> PRECISIONB) + 1, ih1) * iw;
    }

    private int filter_bilinear() {
        fracU = sX & PREC_MAXVAL;
        ifU = PREC_MAXVAL - fracU;
        ul = (ifU * ifV) >> PRECISIONB;
        ll = (ifU * fracV) >> PRECISIONB;
        ur = (fracU * ifV) >> PRECISIONB;
        lr = (fracU * fracV) >> PRECISIONB;
        u1 = (sX >> PRECISIONB);
        u2 = low(u1 + 1, iw1);

        // get color values of the 4 neighbouring texels
        cUL = srcBuffer[v1 + u1];
        cUR = srcBuffer[v1 + u2];
        cLL = srcBuffer[v2 + u1];
        cLR = srcBuffer[v2 + u2];

        r = ((ul * ((cUL & RED_MASK) >> 16) + ll * ((cLL & RED_MASK) >> 16) + ur * ((cUR & RED_MASK) >> 16) + lr * ((cLR & RED_MASK) >> 16)) << PREC_RED_SHIFT) & RED_MASK;

        g = ((ul * (cUL & GREEN_MASK) + ll * (cLL & GREEN_MASK) + ur * (cUR & GREEN_MASK) + lr * (cLR & GREEN_MASK)) >>> PRECISIONB) & GREEN_MASK;

        b = (ul * (cUL & BLUE_MASK) + ll * (cLL & BLUE_MASK) + ur * (cUR & BLUE_MASK) + lr * (cLR & BLUE_MASK)) >>> PRECISIONB;

        a = ((ul * ((cUL & ALPHA_MASK) >>> 24) + ll * ((cLL & ALPHA_MASK) >>> 24) + ur * ((cUR & ALPHA_MASK) >>> 24) + lr * ((cLR & ALPHA_MASK) >>> 24)) << PREC_ALPHA_SHIFT) & ALPHA_MASK;

        return a | r | g | b;
    }

    /**
     * Copies area of one image into another PImage object.
     */
    public void copy(RainbowImage src, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        blend(src, sx, sy, sw, sh, dx, dy, dw, dh, REPLACE);
    }

    /**
     * Blends one area of this image to another area.
     *
     * @see RainbowImage#blendColor(int, int, Modes.Blend)
     */
    public void blend(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, Modes.Blend mode) {
        blend(this, sx, sy, sw, sh, dx, dy, dw, dh, mode);
    }

    /**
     * Save this image to disk.
     * <p/>
     * As of revision 0100, this function requires an absolute path, in order to
     * avoid confusion. To save inside the sketch folder, use the function
     * savePath() from Imagine, or use saveFrame() instead. As of revision 0116,
     * savePath() is not needed if this object has been created (as recommended)
     * via createImage() or createGraphics() or one of its neighbors.
     * <p/>
     * As of revision 0115, when using Java 1.4 and later, you can write to
     * several formats besides tga and tiff. If Java 1.4 is installed and the
     * extension used is supported (usually png, jpg, jpeg, bmp, and tiff), then
     * those methods will be used to write the image. To get a list of the
     * supported formats for writing, use: <BR>
     * <TT>println(javax.imageio.ImageIO.getReaderFormatNames())</TT>
     * <p/>
     * To use the original built-in image writers, use .tga or .tif as the
     * extension, or don't include an extension. When no extension is used, the
     * extension .tif will be added to the file name.
     * <p/>
     * The ImageIO API claims to support wbmp files, however they probably
     * require a black and white image. Basic testing produced a zero-length
     * file with no error.
     */
    public boolean save(String path) { // ignore
        boolean success = false;

        loadPixels();

        try {
            String mPath = path;

            File imageFile = new File(mPath);

            boolean created = imageFile.createNewFile();

            if (!created) {
                return false;
            }

            OutputStream output = new FileOutputStream(imageFile);

            String lower = path.toLowerCase();
            String extension = lower.substring(lower.lastIndexOf('.') + 1);
            if (extension.equals("jpg") || extension.equals("jpeg")) {
                Bitmap outgoing = Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
                success = outgoing.compress(CompressFormat.JPEG, 100, output);

            } else if (extension.equals("png")) {
                Bitmap outgoing = Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
                success = outgoing.compress(CompressFormat.PNG, 100, output);

            }
            output.flush();
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!success) {
            System.err.println("Could not write the image to " + path);
        }
        return success;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public interface LoadPictureListener {
        void onLoadSucceed(RainbowImage image);

        void onLoadFail();
    }
}
