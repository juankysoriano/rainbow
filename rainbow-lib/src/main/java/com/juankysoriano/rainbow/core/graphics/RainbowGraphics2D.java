/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 Part of the Processing project - http://processing.org

 Copyright (c) 2005-10 Ben Fry and Casey Reas

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
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.juankysoriano.rainbow.core.drawing.Modes;
import com.juankysoriano.rainbow.core.drawing.RainbowTextureView;
import com.juankysoriano.rainbow.core.matrix.RMatrix;
import com.juankysoriano.rainbow.core.matrix.RMatrix2D;
import com.juankysoriano.rainbow.utils.RainbowMath;

import static com.juankysoriano.rainbow.core.drawing.Modes.Image.ALPHA;
import static com.juankysoriano.rainbow.core.drawing.Modes.Shape.*;

/**
 * Subclass for PGraphics that implements the graphics API using the Android 2D
 * graphics model. Similar tradeoffs to ANDROID2D mode with the original
 * (desktop) version of Processing.
 */
public class RainbowGraphics2D extends RainbowGraphics {

    /**
     * The temporary path object that does most of the drawing work. If there
     * are any points in the path (meaning that moveto has been called), then
     * vertexCount will be 1 (or more). In the POLYGON case, vertexCount is only
     * set to 1 after the first point is drawn (to indicate a moveto) and not
     * incremented after, since the variable isn't used for POLYGON paths.
     */
    private final Path path;
    private final float[] transform;
    /**
     * Temporary rectangle object.
     */
    private final RectF rect;
    private boolean breakShape;
    private float[] screenPoint;
    /**
     * coordinates for internal curve calculation
     */
    private float[] curveCoordX;
    private float[] curveCoordY;
    private float[] curveDrawX;
    private float[] curveDrawY;
    private Rect imageImplSrcRect;
    private RectF imageImplDstRect;
    private Paint tintPaint;
    private Paint strokePaint;
    private Paint fillPaint;

    private Bitmap bitmap;

    private Canvas canvas;

    public RainbowGraphics2D() {
        transform = new float[9];
        path = new Path();
        rect = new RectF();
        initPaints();
    }

    /**
     * Called in response to a resize event, handles setting the new width and
     * height internally, as well as re-allocating the pixel buffer for the new
     * size.
     * <p/>
     * Note that this will nuke any cameraMode() settings.
     */
    @Override
    public void setSize(int iwidth, int iheight) { // ignore
        width = iwidth;
        height = iheight;

        allocate();
        reapplySettings();
    }

    @Override
    protected void allocate() {
        initBitmaps();
        initPaints();
    }

    private void initBitmaps() {
        setBitmap(Bitmap.createBitmap(parent.getWidth(), parent.getHeight(), Config.ARGB_4444));

        if (primarySurface) {
            paintParentBackground();
            canvas = new Canvas(bitmap);
        } else {
            canvas = new Canvas(super.getBitmap());
        }
    }

    private void paintParentBackground() {
        Drawable parentBackground = parent.getDrawingView().getBackground();
        if (parentBackground != null) {
            parentBackground.setBounds(0, 0, width, height);
            parentBackground.draw(canvas);
        }
    }

    @Override
    public Bitmap getBitmap() {
        return primarySurface ? bitmap : super.getBitmap();
    }

    @Override
    public void setBitmap(Bitmap bitmap) {
        if (primarySurface && !hasBitmap()) {
            this.bitmap = bitmap;

        } else {
            super.setBitmap(bitmap);
        }
    }

    private void initPaints() {
        fillPaint = new Paint();
        fillPaint.setStyle(Style.FILL);
        strokePaint = new Paint();
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStyle(Style.STROKE);
        tintPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    public void dispose() {
        recycle();
    }

    public void recycle() {
        super.recycle();

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public void resetMatrix() {
        canvas.setMatrix(new android.graphics.Matrix());
    }

    @Override
    public void beginDraw() {
        checkSettings();
        vertexCount = 0;
    }

    @Override
    public void endDraw() {
        if (primarySurface) {
            RainbowTextureView textureView = parent.getDrawingView();
            Canvas screen = textureView.lockCanvas();
            if (canPaint(screen)) {
                screen.drawBitmap(bitmap, 0, 0, null);
                textureView.unlockCanvasAndPost(screen);
            }
        } else {
            loadPixels();
        }
    }

    private boolean canPaint(Canvas screen) {
        return screen != null && hasBitmap();
    }

    private boolean hasBitmap() {
        return getBitmap() != null;
    }

    @Override
    public void loadPixels() {
        if ((pixels == null) || (pixels.length != width * height)) {
            pixels = new int[width * height];
        }
        getBitmap().getPixels(pixels, 0, width, 0, 0, width, height);
    }

    @Override
    public void beginShape(Modes.Shape mode) {
        shapeMode = mode;
        vertexCount = 0;
        curveVertexCount = 0;
    }

    @Override
    public void texture(RainbowImage image) {
        showMethodWarning("texture");
    }

    @Override
    public void vertex(float x, float y) {
        if (shapeMode == POLYGON) {
            vertexPolygon(x, y);
        } else {
            curveVertexCount = 0;

            if (vertexCount == vertices.length) {
                float temp[][] = new float[vertexCount << 1][VERTEX_FIELD_COUNT];
                System.arraycopy(vertices, 0, temp, 0, vertexCount);
                vertices = temp;
            }

            vertices[vertexCount][X] = x;
            vertices[vertexCount][Y] = y;
            vertexCount++;

            switch (shapeMode) {
                case POINTS:
                    break;
                case LINES:
                    vertexLines(x, y);
                    break;
                case TRIANGLES:
                    vertexTriangles(x, y);
                    break;
                case TRIANGLE_STRIP:
                    vertexTriangleStrip(x, y);
                    break;
                case TRIANGLE_FAN:
                    vertexTriangleFan(x, y);
                    break;
                case QUAD:
                case QUADS:
                    vertexQuads(x, y);
                    break;
                case QUAD_STRIP:
                    vertexQuadStrip(x, y);
                    break;
            }
        }
    }

    private void vertexQuadStrip(float x, float y) {
        if ((vertexCount >= 4) && ((vertexCount % 2) == 0)) {
            quad(
                    vertices[vertexCount - 4][X],
                    vertices[vertexCount - 4][Y],
                    vertices[vertexCount - 2][X],
                    vertices[vertexCount - 2][Y],
                    x,
                    y,
                    vertices[vertexCount - 3][X],
                    vertices[vertexCount - 3][Y]
            );
        }
    }

    private void vertexQuads(float x, float y) {
        if ((vertexCount % 4) == 0) {
            quad(
                    vertices[vertexCount - 4][X],
                    vertices[vertexCount - 4][Y],
                    vertices[vertexCount - 3][X],
                    vertices[vertexCount - 3][Y],
                    vertices[vertexCount - 2][X],
                    vertices[vertexCount - 2][Y],
                    x,
                    y
            );
            vertexCount = 0;
        }
    }

    private void vertexTriangleFan(float x, float y) {
        if (vertexCount >= 3) {
            triangle(vertices[0][X], vertices[0][Y], vertices[vertexCount - 2][X], vertices[vertexCount - 2][Y], x, y);
        }
    }

    private void vertexTriangleStrip(float x, float y) {
        if (vertexCount >= 3) {
            triangle(vertices[vertexCount - 2][X], vertices[vertexCount - 2][Y], x,
                     y,
                     vertices[vertexCount - 3][X],
                     vertices[vertexCount - 3][Y]
            );
        }
    }

    private void vertexTriangles(float x, float y) {
        if ((vertexCount % 3) == 0) {
            triangle(vertices[vertexCount - 3][X], vertices[vertexCount - 3][Y], vertices[vertexCount - 2][X], vertices[vertexCount - 2][Y], x, y);
            vertexCount = 0;
        }
    }

    private void vertexLines(float x, float y) {
        if ((vertexCount % 2) == 0) {
            line(vertices[vertexCount - 2][X], vertices[vertexCount - 2][Y], x, y);
            vertexCount = 0;
        }
    }

    private void vertexPolygon(float x, float y) {
        if (vertexCount == 0) {
            path.reset();
            path.moveTo(x, y);
            vertexCount = 1;
        } else if (breakShape) {
            path.moveTo(x, y);
            breakShape = false;
        } else {
            path.lineTo(x, y);
        }
    }

    @Override
    public void breakShape() {
        breakShape = true;
    }

    @Override
    public void endShape(Modes.Shape mode) {
        if (shapeMode == POINTS && stroke && vertexCount > 0) {
            endPointsShape();
        } else if (shapeMode == POLYGON) {
            endPolygonShape(mode);
        }
        shapeMode = UNDEFINED;
    }

    private void endPointsShape() {
        android.graphics.Matrix m = canvas.getMatrix();
        if (strokeWeight == 1 && m.isIdentity()) {
            if (screenPoint == null) {
                screenPoint = new float[2];
            }
            for (int i = 0; i < vertexCount; i++) {
                screenPoint[0] = vertices[i][X];
                screenPoint[1] = vertices[i][Y];
                m.mapPoints(screenPoint);
                set(RainbowMath.round(screenPoint[0]), RainbowMath.round(screenPoint[1]), strokeColor);
                float x = vertices[i][X];
                float y = vertices[i][Y];
                set(RainbowMath.round(screenX(x, y)), RainbowMath.round(screenY(x, y)), strokeColor);
            }
        } else {
            float sw = strokeWeight / 2;
            // temporarily use the stroke Paint as a fill
            strokePaint.setStyle(Style.FILL);
            for (int i = 0; i < vertexCount; i++) {
                float x = vertices[i][X];
                float y = vertices[i][Y];
                rect.set(x - sw, y - sw, x + sw, y + sw);
                canvas.drawOval(rect, strokePaint);
            }
            strokePaint.setStyle(Style.STROKE);
        }
    }

    private void endPolygonShape(Modes.Shape mode) {
        if (!path.isEmpty()) {
            if (mode == CLOSE) {
                path.close();
            }
            drawPath();
        }
    }

    @Override
    public void set(int x, int y, int argb) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            return;
        }
        getBitmap().setPixel(x, y, argb);
    }

    @Override
    public float screenX(float x, float y) {
        if (screenPoint == null) {
            screenPoint = new float[2];
        }
        screenPoint[0] = x;
        screenPoint[1] = y;
        canvas.getMatrix().mapPoints(screenPoint);
        return screenPoint[0];
    }

    @Override
    public float screenY(float x, float y) {
        if (screenPoint == null) {
            screenPoint = new float[2];
        }
        screenPoint[0] = x;
        screenPoint[1] = y;
        canvas.getMatrix().mapPoints(screenPoint);
        return screenPoint[1];
    }

    private void drawPath() {
        if (fill) {
            canvas.drawPath(path, fillPaint);
        }
        if (stroke) {
            canvas.drawPath(path, strokePaint);
        }
    }

    @Override
    public void bezierVertex(float x1, float y1, float x2, float y2, float x3, float y3) {
        bezierVertexCheck();
        path.cubicTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    public void bezierVertex(float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        showDepthWarningXYZ("bezierVertex");
    }

    @Override
    public void quadraticVertex(float ctrlX, float ctrlY, float endX, float endY) {
        bezierVertexCheck();
        path.quadTo(ctrlX, ctrlY, endX, endY);
    }

    @Override
    public void quadraticVertex(float x2, float y2, float z2, float x4, float y4, float z4) {
        showDepthWarningXYZ("quadVertex");
    }

    @Override
    protected void curveVertexCheck() {
        super.curveVertexCheck();

        if (curveCoordX == null) {
            curveCoordX = new float[4];
            curveCoordY = new float[4];
            curveDrawX = new float[4];
            curveDrawY = new float[4];
        }
    }

    @Override
    protected void curveVertexSegment(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        curveCoordX[0] = x1;
        curveCoordY[0] = y1;

        curveCoordX[1] = x2;
        curveCoordY[1] = y2;

        curveCoordX[2] = x3;
        curveCoordY[2] = y3;

        curveCoordX[3] = x4;
        curveCoordY[3] = y4;

        curveToBezierMatrix.mult(curveCoordX, curveDrawX);
        curveToBezierMatrix.mult(curveCoordY, curveDrawY);

        if (vertexCount == 0) {
            path.moveTo(curveDrawX[0], curveDrawY[0]);
            vertexCount = 1;
        }

        path.cubicTo(curveDrawX[1], curveDrawY[1], curveDrawX[2], curveDrawY[2], curveDrawX[3], curveDrawY[3]);
    }

    @Override
    public void curveVertex(float x, float y, float z) {
        showDepthWarningXYZ("curveVertex");
    }

    @Override
    public void point(float... vertex) {
        if (stroke) {
            canvas.drawPoints(vertex, strokePaint);
        }
    }

    @Override
    public void point(float x, float y) {
        if (stroke) {
            canvas.drawPoint(x, y, strokePaint);
        }
    }

    @Override
    public void line(float... vertex) {
        if (stroke) {
            canvas.drawLines(vertex, strokePaint);
        }
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        if (stroke) {
            canvas.drawLine(x1, y1, x2, y2, strokePaint);
        }
    }

    @Override
    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        path.reset();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close();
        drawPath();
    }

    @Override
    public void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        path.reset();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.lineTo(x4, y4);
        path.close();
        drawPath();
    }

    @Override
    protected void rectImpl(float x1, float y1, float x2, float y2) {
        if (fill) {
            canvas.drawRect(x1, y1, x2, y2, fillPaint);
        }
        if (stroke) {
            canvas.drawRect(x1, y1, x2, y2, strokePaint);
        }
    }

    @Override
    protected void arcImpl(float x, float y, float w, float h, float start, float stop, Modes.Arc mode) {

        if (stop - start >= RainbowMath.TWO_PI) {
            ellipseImpl(x, y, w, h);

        } else {

            start = start * RainbowMath.RAD_TO_DEG;
            stop = stop * RainbowMath.RAD_TO_DEG;

            while (start < 0) {
                start += 360;
                stop += 360;
            }
            if (start > stop) {
                float temp = start;
                start = stop;
                stop = temp;
            }

            float sweep = stop - start;
            rect.set(x, y, x + w, y + h);

            if (mode == Modes.Arc.UNDEFINED) {
                if (fill) {
                    canvas.drawArc(rect, start, sweep, true, fillPaint);
                }
                if (stroke) {
                    canvas.drawArc(rect, start, sweep, false, strokePaint);
                }
            } else if (mode == Modes.Arc.OPEN) {
                if (fill) {
                    showMissingWarning("arc");
                }
                if (stroke) {
                    canvas.drawArc(rect, start, sweep, false, strokePaint);
                }
            } else if (mode == Modes.Arc.CHORD) {
                showMissingWarning("arc");

            } else if (mode == Modes.Arc.PIE) {
                if (fill) {
                    canvas.drawArc(rect, start, sweep, true, fillPaint);
                }
                if (stroke) {
                    canvas.drawArc(rect, start, sweep, true, strokePaint);
                }

            }
        }
    }

    @Override
    protected void ellipseImpl(float x, float y, float w, float h) {
        rect.set(x, y, x + w, y + h);
        if (fill) {
            canvas.drawOval(rect, fillPaint);
        }
        if (stroke) {
            canvas.drawOval(rect, strokePaint);
        }
    }

    /**
     * Ignored (not needed)
     */
    @Override
    public void bezierDetail(int detail) {
    }

    /**
     * Ignored (not needed)
     */
    @Override
    public void curveDetail(int detail) {
    }

    @Override
    public void smooth() {
        smooth = true;
        strokePaint.setAntiAlias(true);
        fillPaint.setAntiAlias(true);
    }

    @Override
    public void noSmooth() {
        smooth = false;
        strokePaint.setAntiAlias(false);
        fillPaint.setAntiAlias(false);
    }

    /**
     * Handle renderer-specific image drawing.
     */
    @Override
    protected void imageImpl(RainbowImage src, float x1, float y1, float x2, float y2, int u1, int v1, int u2, int v2) {

        if (src.getBitmap() == null && src.format == ALPHA) {
            // create an alpha normalBitmap for this feller
            src.setBitmap(Bitmap.createBitmap(src.width, src.height, Config.ARGB_4444));
            int[] px = new int[src.pixels.length];
            for (int i = 0; i < px.length; i++) {
                px[i] = src.pixels[i] << 24 | 0xFFFFFF;
            }
            src.getBitmap().setPixels(px, 0, src.width, 0, 0, src.width, src.height);
            src.modified = false;
        }

        if (src.getBitmap() == null || src.width != src.getBitmap().getWidth() || src.height != src.getBitmap().getHeight()) {
            src.setBitmap(Bitmap.createBitmap(src.width, src.height, Config.ARGB_4444));
            src.modified = true;
        }
        if (src.modified) {
            if (!src.getBitmap().isMutable()) {
                src.setBitmap(Bitmap.createBitmap(src.width, src.height, Config.ARGB_4444));
            }
            src.getBitmap().setPixels(src.pixels, 0, src.width, 0, 0, src.width, src.height);
            src.modified = false;
        }

        if (imageImplSrcRect == null) {
            imageImplSrcRect = new Rect(u1, v1, u2, v2);
            imageImplDstRect = new RectF(x1, y1, x2, y2);
        } else {
            imageImplSrcRect.set(u1, v1, u2, v2);
            imageImplDstRect.set(x1, y1, x2, y2);
        }

        canvas.drawBitmap(src.getBitmap(), imageImplSrcRect, imageImplDstRect, tint ? tintPaint : null);
    }

    @Override
    public void pushMatrix() {
        canvas.save();
    }

    @Override
    public void popMatrix() {
        canvas.restore();
    }

    @Override
    public void translate(float tx, float ty) {
        canvas.translate(tx, ty);
    }

    @Override
    public void rotate(float angle) {
        canvas.rotate(angle * RainbowMath.RAD_TO_DEG);
    }

    @Override
    public void scale(float s) {
        canvas.scale(s, s);
    }

    @Override
    public void scale(float sx, float sy) {
        canvas.scale(sx, sy);
    }

    @Override
    public void shearX(float angle) {
        canvas.skew((float) Math.tan(angle), 0);
    }

    @Override
    public void shearY(float angle) {
        canvas.skew(0, (float) Math.tan(angle));
    }

    @Override
    public void applyMatrix(float n00, float n01, float n02, float n10, float n11, float n12) {
        android.graphics.Matrix m = new android.graphics.Matrix();
        m.setValues(new float[]{n00, n01, n02, n10, n11, n12, 0, 0, 1});
        canvas.concat(m);
    }

    @Override
    public RMatrix getMatrix() {
        return getMatrix((RMatrix2D) null);
    }

    @Override
    public RMatrix2D getMatrix(RMatrix2D target) {
        if (target == null) {
            target = new RMatrix2D();
        }

        android.graphics.Matrix m = new android.graphics.Matrix();
        canvas.getMatrix(m);
        m.getValues(transform);
        target.set(transform[0], transform[1], transform[2], transform[3], transform[4], transform[5]);
        return target;
    }

    @Override
    public void setMatrix(RMatrix2D source) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.setValues(new float[]{source.m00, source.m01, source.m02, source.m10, source.m11, source.m12, 0, 0, 1});
        canvas.setMatrix(matrix);
    }

    @Override
    public void strokeCap(Modes.Stroke.Cap mode) {
        super.strokeCap(mode);

        if (strokeCap == Modes.Stroke.Cap.ROUND) {
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
        } else if (strokeCap == Modes.Stroke.Cap.SQUARE) {
            strokePaint.setStrokeCap(Paint.Cap.SQUARE);
        } else if (strokeCap == Modes.Stroke.Cap.BUTT) {
            strokePaint.setStrokeCap(Paint.Cap.BUTT);
        }
    }

    @Override
    public void strokeJoin(Modes.Stroke.Join mode) {
        super.strokeJoin(mode);

        if (strokeJoin == Modes.Stroke.Join.MITER) {
            strokePaint.setStrokeJoin(Paint.Join.MITER);
        } else if (strokeJoin == Modes.Stroke.Join.ROUND) {
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
        } else if (strokeJoin == Modes.Stroke.Join.BEVEL) {
            strokePaint.setStrokeJoin(Paint.Join.BEVEL);
        }
    }

    @Override
    public void strokeWeight(float weight) {
        super.strokeWeight(weight);
        strokePaint.setStrokeWidth(weight);
    }

    @Override
    protected void strokeFromCalc() {
        super.strokeFromCalc();
        strokePaint.setColor(strokeColor);
        strokePaint.setShader(null);
    }

    @Override
    protected void tintFromCalc() {
        super.tintFromCalc();
        tintPaint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void fillFromCalc() {
        super.fillFromCalc();
        fillPaint.setColor(fillColor);
        fillPaint.setShader(null);
    }

    @Override
    public void backgroundImpl() {
        canvas.drawColor(backgroundColor);
    }

    /**
     * Update the pixels[] buffer to the PGraphics image.
     * <p/>
     * Unlike in PImage, where updatePixels() only requests that the update
     * happens, in PGraphicsJava2D, this will happen immediately.
     */
    @Override
    public void updatePixels(int x, int y, int c, int d) {
        if ((x != 0) || (y != 0) || (c != width) || (d != height)) {
            // Show a warning message, but continue anyway.
            showVariationWarning("updatePixels(x, y, w, h)");
        }
        updatePixels();
    }

    /**
     * Update the pixels[] buffer to the PGraphics image.
     * <p/>
     * Unlike in PImage, where updatePixels() only requests that the update
     * happens, in PGraphicsJava2D, this will happen immediately.
     */
    @Override
    public void updatePixels() {
        getBitmap().setPixels(pixels, 0, width, 0, 0, width, height);
    }

    @Override
    public RainbowGraphics2D resize(int wide, int high) {
        showMethodWarning("resize");
        return this;
    }

    @Override
    public int get(int x, int y) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            return 0;
        }
        return getBitmap().getPixel(x, y);
    }

    @Override
    public RainbowImage get() {
        return get(0, 0, width, height);
    }

    @Override
    public void set(int x, int y, RainbowImage src) {
        if (src.format == ALPHA) {
            throw new RuntimeException("set() not available for ALPHA images");
        }

        if (src.getBitmap() == null) {
            canvas.drawBitmap(src.pixels, 0, src.width, x, y, src.width, src.height, false, null);
        } else {
            if (src.width != src.getBitmap().getWidth() || src.height != src.getBitmap().getHeight()) {
                src.setBitmap(Bitmap.createBitmap(src.width, src.height, Config.ARGB_4444));
                src.modified = true;
            }
            if (src.modified) {
                if (!src.getBitmap().isMutable()) {
                    src.setBitmap(Bitmap.createBitmap(src.width, src.height, Config.ARGB_4444));
                }
                src.getBitmap().setPixels(src.pixels, 0, src.width, 0, 0, src.width, src.height);
                src.modified = false;
            }
            canvas.save();
            canvas.setMatrix(null); // set to identity
            canvas.drawBitmap(src.getBitmap(), x, y, null);
            canvas.restore();
        }
    }

    @Override
    public void copy(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        rect.set(sx, sy, sx + sw, sy + sh);
        Rect src = new Rect(dx, dy, dx + dw, dy + dh);
        canvas.drawBitmap(getBitmap(), src, rect, null);
    }

    public Paint getFillPaint() {
        return fillPaint;
    }

    public Paint getStrokePaint() {
        return strokePaint;
    }
}
