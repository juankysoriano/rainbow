package com.juankysoriano.rainbow.core.drawing

import android.graphics.Bitmap
import android.graphics.Shader
import android.net.Uri

import com.juankysoriano.rainbow.core.graphics.RainbowGraphics
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics2D
import com.juankysoriano.rainbow.core.graphics.RainbowImage
import com.juankysoriano.rainbow.core.math.RMatrix
import com.juankysoriano.rainbow.core.math.RMatrix2D
import com.juankysoriano.rainbow.core.math.RMatrix3D

import java.io.File

class RainbowDrawer {

    lateinit var graphics: RainbowGraphics
        private set

    var width: Int = 0
        get() = graphics.width
        private set

    var height: Int = 0
        get() = graphics.height
        private set

    val pixels: IntArray by lazy {
        graphics.loadPixels()
        graphics.pixels
    }

    var isDrawing: Boolean = false
        private set

    private val lineExplorer = LineExplorer()

    fun setGraphics(graphics: RainbowGraphics) {
        this.graphics = graphics
        this.width = graphics.width
        this.height = graphics.height
    }

    fun createGraphics(iwidth: Int, iheight: Int): RainbowGraphics {
        val pg = RainbowGraphics2D()
        pg.setParent(graphics.parent)
        pg.setPrimary(false)
        pg.setSize(iwidth, iheight)

        return pg
    }

    fun createImage(wide: Int, high: Int, format: Modes.Image): RainbowImage {
        val image = RainbowImage(wide, high, format)
        image.parent = graphics.parent
        return image
    }

    fun loadImage(path: String, mode: Modes.LoadMode, listener: RainbowImage.LoadPictureListener) {
        val bitmap = BitmapLoader.loadBitmap(path, width, height, mode)
        loadImage(bitmap, listener)
    }

    private fun loadImage(bitmap: Bitmap?, listener: RainbowImage.LoadPictureListener) {
        if (bitmap == null) {
            listener.onLoadFail()
        } else {
            val image = RainbowImage(bitmap)
            image.parent = graphics.parent
            listener.onLoadSucceed(image)
        }
    }

    fun loadImage(path: String, width: Int, height: Int, mode: Modes.LoadMode, listener: RainbowImage.LoadPictureListener) {
        val bitmap = BitmapLoader.loadBitmap(path, width, height, mode)
        loadImage(bitmap, listener)
    }

    fun loadImage(resID: Int, mode: Modes.LoadMode, listener: RainbowImage.LoadPictureListener) {
        val bitmap = BitmapLoader.loadBitmap(resID, width, height, mode)
        loadImage(bitmap, listener)
    }

    fun loadImage(resID: Int, width: Int, height: Int, mode: Modes.LoadMode, listener: RainbowImage.LoadPictureListener) {
        val bitmap = BitmapLoader.loadBitmap(resID, width, height, mode)
        loadImage(bitmap, listener)
    }

    fun loadImage(file: File, mode: Modes.LoadMode, listener: RainbowImage.LoadPictureListener) {
        val bitmap = BitmapLoader.loadBitmap(file, width, height, mode)
        loadImage(bitmap, listener)
    }

    fun loadImage(uri: Uri, mode: Modes.LoadMode, listener: RainbowImage.LoadPictureListener) {
        val bitmap = BitmapLoader.loadBitmap(uri, width, height, mode)
        loadImage(bitmap, listener)
    }

    fun loadImage(uri: Uri, width: Int, height: Int, mode: Modes.LoadMode, listener: RainbowImage.LoadPictureListener) {
        val bitmap = BitmapLoader.loadBitmap(uri, width, height, mode)
        loadImage(bitmap, listener)
    }

    fun color(gray: Int): Int {
        return graphics.color(gray)
    }

    fun color(fgray: Float): Int {
        return graphics.color(fgray)
    }

    fun color(gray: Int, alpha: Int): Int {
        return graphics.color(gray, alpha)
    }

    fun color(fgray: Float, falpha: Float): Int {
        return graphics.color(fgray, falpha)
    }

    fun color(x: Int, y: Int, z: Int): Int {
        return graphics.color(x, y, z)
    }

    fun color(x: Float, y: Float, z: Float): Int {
        return graphics.color(x, y, z)
    }

    fun color(x: Int, y: Int, z: Int, a: Int): Int {
        return graphics.color(x, y, z, a)
    }

    fun color(x: Float, y: Float, z: Float, a: Float): Int {
        return graphics.color(x, y, z, a)
    }

    fun updatePixels() {
        graphics.updatePixels()
    }

    fun updatePixels(x1: Int, y1: Int, x2: Int, y2: Int) {
        graphics.updatePixels(x1, y1, x2, y2)
    }

    /**
     * Prepares rainbow sketch for draw.
     */
    fun beginDraw() {
        isDrawing = true
        graphics.beginDraw()
    }

    /**
     * Ends synchronous draw. Makes drawing effective
     */
    fun endDraw() {
        graphics.endDraw()
        isDrawing = false
    }

    /**
     * Start a new shape of type POLYGON
     */
    fun beginShape() {
        graphics.beginShape()
    }

    /**
     * Start a new shape.
     *
     *
     * <B>Differences between beginShape() and line() and point() methods.</B>
     *
     *
     * beginShape() is intended to be more flexible at the expense of being a
     * little more complicated to use. it handles more complicated shapes that
     * can consist of many connected lines (so you get joins) or lines mixed
     * with curves.
     *
     *
     * The line() and point() command are for the far more common cases
     * (particularly for our audience) that simply need to draw a line or a
     * point on the screen.
     *
     *
     * From the code side of things, line() may or may not call beginShape() to
     * do the drawing. In the beta code, they do, but in the alpha code, they
     * did not. they might be implemented one way or the other depending on
     * tradeoffs of runtime efficiency vs. implementation efficiency &mdash
     * meaning the speed that things run at vs. the speed it takes me to write
     * the code and maintain it. for beta, the latter is most important so
     * that's how things are implemented.
     */
    fun beginShape(mode: Modes.Shape) {
        graphics.beginShape(mode)
    }

    /**
     * Sets whether the upcoming vertex is part of an edge. Equivalent to
     * glEdgeFlag(), for people familiar with OpenGL.
     */
    fun edge(edge: Boolean) {
        graphics.edge(edge)
    }

    /**
     * Sets the current normal vector. Only applies with 3D rendering and inside
     * a beginShape/endShape block.
     *
     *
     * This is for drawing three dimensional shapes and surfaces, allowing you
     * to specify a vector perpendicular to the surface of the shape, which
     * determines how lighting affects it.
     *
     *
     * For people familiar with OpenGL, this function is basically identical to
     * glNormal3f().
     */
    fun normal(nx: Float, ny: Float, nz: Float) {
        graphics.normal(nx, ny, nz)
    }

    /**
     * Set texture mode to either to use coordinates based on the IMAGE (more
     * intuitive for new users) or NORMALIZED (better for advanced chaps)
     */
    fun textureMode(mode: Int) {
        graphics.textureMode(mode)
    }

    fun textureWrap(wrap: Int) {
        graphics.textureWrap(wrap)
    }

    /**
     * Set texture image for current shape. Needs to be called between @see
     * beginShape and @see endShape
     *
     * @param image reference to a PImage object
     */
    fun texture(image: RainbowImage) {
        graphics.texture(image)
    }

    /**
     * Removes texture image for current shape. Needs to be called between @see
     * beginShape and @see endShape
     */
    fun noTexture() {
        graphics.noTexture()
    }

    fun vertex(x: Float, y: Float) {
        graphics.vertex(x, y)
    }

    fun vertex(x: Float, y: Float, z: Float) {
        graphics.vertex(x, y, z)
    }

    /**
     * Used by renderer subclasses or PShape to efficiently pass in already
     * formatted vertex information.
     *
     * @param v vertex parameters, as a float array of length
     * VERTEX_FIELD_COUNT
     */
    fun vertex(v: FloatArray) {
        graphics.vertex(v)
    }

    fun vertex(x: Float, y: Float, u: Float, v: Float) {
        graphics.vertex(x, y, u, v)
    }

    fun vertex(x: Float, y: Float, z: Float, u: Float, v: Float) {
        graphics.vertex(x, y, z, u, v)
    }

    /**
     * This feature is in testing, do not use or rely upon its implementation
     */
    fun breakShape() {
        graphics.breakShape()
    }

    fun beginContour() {
        graphics.beginContour()
    }

    fun endContour() {
        graphics.endContour()
    }

    fun endShape() {
        graphics.endShape()
    }

    fun endShape(mode: Modes.Shape) {
        graphics.endShape(mode)
    }

    fun clip(a: Float, b: Float, c: Float, d: Float) {
        graphics.clip(a, b, c, d)
    }

    fun noClip() {
        graphics.noClip()
    }

    fun blendMode(mode: Int) {
        graphics.blendMode(mode)
    }

    fun bezierVertex(x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) {
        graphics.bezierVertex(x2, y2, x3, y3, x4, y4)
    }

    fun bezierVertex(x2: Float, y2: Float, z2: Float, x3: Float, y3: Float, z3: Float, x4: Float, y4: Float, z4: Float) {
        graphics.bezierVertex(x2, y2, z2, x3, y3, z3, x4, y4, z4)
    }

    fun quadraticVertex(cx: Float, cy: Float, x3: Float, y3: Float) {
        graphics.quadraticVertex(cx, cy, x3, y3)
    }

    fun quadraticVertex(cx: Float, cy: Float, cz: Float, x3: Float, y3: Float, z3: Float) {
        graphics.quadraticVertex(cx, cy, cz, x3, y3, z3)
    }

    fun curveVertex(x: Float, y: Float) {
        graphics.curveVertex(x, y)
    }

    fun curveVertex(x: Float, y: Float, z: Float) {
        graphics.curveVertex(x, y, z)
    }

    fun point(x: Float, y: Float) {
        graphics.point(x, y)
    }

    fun point(vararg points: Float) {
        graphics.point(*points)
    }

    fun point(x: Float, y: Float, z: Float) {
        graphics.point(x, y, z)
    }

    fun line(vararg vertex: Float) {
        graphics.line(*vertex)
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
        graphics.line(x1, y1, x2, y2)
    }

    /**
     * Explores a imaginary line in order to seek for line points. When a line point is found a listener is notified.
     *
     * @param x1       start x
     * @param y1       start y
     * @param x2       end x
     * @param y2       end y
     * @param listener PointDetectedListener which will apply a operation over the identified point
     */
    fun exploreLine(x1: Float,
                    y1: Float,
                    x2: Float,
                    y2: Float,
                    precision: Precision,
                    listener: PointDetectedListener) {
        lineExplorer.exploreLine(x1, y1, x2, y2, precision, listener)
    }

    fun triangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        graphics.triangle(x1, y1, x2, y2, x3, y3)
    }

    fun quad(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) {
        graphics.quad(x1, y1, x2, y2, x3, y3, x4, y4)
    }

    fun rectMode(mode: Modes.Draw) {
        graphics.rectMode(mode)
    }

    fun rect(a: Float, b: Float, c: Float, d: Float) {
        graphics.rect(a, b, c, d)
    }

    fun rect(a: Float, b: Float, c: Float, d: Float, r: Float) {
        graphics.rect(a, b, c, d, r)
    }

    fun rect(a: Float, b: Float, c: Float, d: Float, tl: Float, tr: Float, br: Float, bl: Float) {
        graphics.rect(a, b, c, d, tl, tr, br, bl)
    }

    fun ellipseMode(mode: Modes.Draw) {
        graphics.ellipseMode(mode)
    }

    fun ellipse(a: Float, b: Float, c: Float, d: Float) {
        graphics.ellipse(a, b, c, d)
    }

    /**
     * Identical parameters and placement to ellipse, but draws only an arc of
     * that ellipse.
     *
     *
     * start and stop are always radians because angleMode() was goofy.
     * ellipseMode() sets the placement.
     *
     *
     * also tries to be smart about start < stop.
     */
    fun arc(a: Float, b: Float, c: Float, d: Float, start: Float, stop: Float) {
        graphics.arc(a, b, c, d, start, stop)
    }

    fun arc(a: Float, b: Float, c: Float, d: Float, start: Float, stop: Float, mode: Modes.Arc) {
        graphics.arc(a, b, c, d, start, stop, mode)
    }

    fun box(size: Float) {
        graphics.box(size)
    }

    fun box(w: Float, h: Float, d: Float) {
        graphics.box(w, h, d)
    }

    fun sphereDetail(res: Int) {
        graphics.sphereDetail(res)
    }

    /**
     * Set the detail level for approximating a sphere. The ures and vres params
     * control the horizontal and vertical resolution.
     *
     *
     * Code for sphereDetail() submitted by toxi [031031]. Code for enhanced u/v
     * version from davbol [080801].
     */
    fun sphereDetail(ures: Int, vres: Int) {
        graphics.sphereDetail(ures, vres)
    }

    /**
     * Draw a sphere with radius r centered at coordinate 0, 0, 0.
     *
     *
     * Implementation notes:
     *
     *
     * cache all the points of the sphere in a static array top and bottom are
     * just a bunch of triangles that land in the center point
     *
     *
     * sphere is a series of concentric circles who radii vary along the shape,
     * based on, er.. cos or something
     *
     *
     * <PRE>
     * [toxi 031031] new sphere code. removed all multiplies with
     * radius, as scale() will take care of that anyway
     *
     *
     * [toxi 031223] updated sphere code (removed modulos)
     * and introduced sphereAt(x,y,z,r)
     * to avoid additional translate()'s on the user/sketch side
     *
     *
     * [davbol 080801] now using separate sphereDetailU/V
    </PRE> *
     */
    fun sphere(r: Float) {
        graphics.sphere(r)
    }

    /**
     * Evalutes quadratic bezier at point t for points a, b, c, d. t varies
     * between 0 and 1, and a and d are the on curve points, b and c are the
     * control points. this can be done once with the x coordinates and a second
     * time with the y coordinates to get the location of a bezier curve at t.
     *
     *
     * For instance, to convert the following example:
     *
     *
     * <PRE>
     * stroke(255, 102, 0);
     * line(85, 20, 10, 10);
     * line(90, 90, 15, 80);
     * stroke(0, 0, 0);
     * bezier(85, 20, 10, 10, 90, 90, 15, 80);
     *
     *
     * // draw it in gray, using 10 steps instead of the default 20
     * // this is a slower way to do it, but useful if you need
     * // to do things with the coordinates at each step
     * stroke(128);
     * beginShape(LINE_STRIP);
     * for (int i = 0; i &lt;= 10; i++) {
     * float t = i / 10.0f;
     * float x = bezierPoint(85, 10, 90, 15, t);
     * float y = bezierPoint(20, 10, 90, 80, t);
     * vertex(x, y);
     * }
     * endShape();
    </PRE> *
     */
    fun bezierPoint(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
        return graphics.bezierPoint(a, b, c, d, t)
    }

    /**
     * Provide the tangent at the given point on the bezier curve. Fix from
     * davbol for 0136.
     */
    fun bezierTangent(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
        return graphics.bezierTangent(a, b, c, d, t)
    }

    fun bezierDetail(detail: Int) {
        graphics.bezierDetail(detail)
    }

    /**
     * Draw a cubic bezier curve. The first and last points are the on-curve
     * points. The middle two are the 'control' points, or 'handles' in an
     * application like Illustrator.
     *
     *
     * Identical to typing:
     *
     *
     * <PRE>
     * beginShape();
     * vertex(x1, y1);
     * bezierVertex(x2, y2, x3, y3, x4, y4);
     * endShape();
    </PRE> *
     *
     *
     * In Postscript-speak, this would be:
     *
     *
     * <PRE>
     * moveto(x1, y1);
     * curveto(x2, y2, x3, y3, x4, y4);
    </PRE> *
     *
     *
     * If you were to try and continue that curve like so:
     *
     *
     * <PRE>
     * curveto(x5, y5, x6, y6, x7, y7);
    </PRE> *
     *
     *
     * This would be done in processing by adding these statements:
     *
     *
     * <PRE>
     * bezierVertex(x5, y5, x6, y6, x7, y7)
    </PRE> *
     *
     *
     * To draw a quadratic (instead of cubic) curve, use the control point twice
     * by doubling it:
     *
     *
     * <PRE>
     * bezier(x1, y1, cx, cy, cx, cy, x2, y2);
    </PRE> *
     */
    fun bezier(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) {
        graphics.bezier(x1, y1, x2, y2, x3, y3, x4, y4)
    }

    fun bezier(x1: Float,
               y1: Float,
               z1: Float,
               x2: Float,
               y2: Float,
               z2: Float,
               x3: Float,
               y3: Float,
               z3: Float,
               x4: Float,
               y4: Float,
               z4: Float) {
        graphics.bezier(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4)
    }

    /**
     * Get a location along a catmull-rom curve segment.
     *
     * @param t Value between zero and one for how far along the segment
     */
    fun curvePoint(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
        return graphics.curvePoint(a, b, c, d, t)
    }

    /**
     * Calculate the tangent at a t value (0..1) on a Catmull-Rom curve. Code
     * thanks to Dave Bollinger (Bug #715)
     */
    fun curveTangent(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
        return graphics.curveTangent(a, b, c, d, t)
    }

    fun curveDetail(detail: Int) {
        graphics.curveDetail(detail)
    }

    fun curveTightness(tightness: Float) {
        graphics.curveTightness(tightness)
    }

    /**
     * Draws a segment of Catmull-Rom curve.
     *
     *
     * As of 0070, this function no longer doubles the first and last points.
     * The curves are a bit more boring, but it's more mathematically correct,
     * and properly mirrored in curvePoint().
     *
     *
     * Identical to typing out:
     *
     *
     * <PRE>
     * beginShape();
     * curveVertex(x1, y1);
     * curveVertex(x2, y2);
     * curveVertex(x3, y3);
     * curveVertex(x4, y4);
     * endShape();
    </PRE> *
     */
    fun curve(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) {
        graphics.curve(x1, y1, x2, y2, x3, y3, x4, y4)
    }

    fun curve(x1: Float,
              y1: Float,
              z1: Float,
              x2: Float,
              y2: Float,
              z2: Float,
              x3: Float,
              y3: Float,
              z3: Float,
              x4: Float,
              y4: Float,
              z4: Float) {
        graphics.curve(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4)
    }

    /**
     * If true in PImage, use bilinear interpolation for copy() operations. When
     * inherited by PGraphics, also controls shapes.
     */
    fun smooth() {
        graphics.smooth()
    }

    fun smooth(level: Int) {
        graphics.smooth(level)
    }

    /**
     * Disable smoothing. See smooth().
     */
    fun noSmooth() {
        graphics.noSmooth()
    }

    /**
     * The mode can only be set to CORNERS, CORNER, and CENTER.
     *
     *
     * Support for CENTER was added in release 0146.
     */
    fun imageMode(mode: Modes.Draw) {
        graphics.imageMode(mode)
    }

    fun image(image: RainbowImage, x: Float, y: Float) {
        graphics.image(image, x, y)
    }

    /**
     * Draw an image(), also specifying u/v coordinates. In this method, the u,
     * v coordinates are always based on image space location, regardless of the
     * current textureMode().
     */
    fun image(image: RainbowImage, a: Float, b: Float, c: Float, d: Float, u1: Int, v1: Int, u2: Int, v2: Int) {
        graphics.image(image, a, b, c, d, u1, v1, u2, v2)
    }

    /**
     * Push a copy of the current transformation matrix onto the stack.
     */
    fun pushMatrix() {
        graphics.pushMatrix()
    }

    /**
     * Replace the current transformation matrix with the top of the stack.
     */
    fun popMatrix() {
        graphics.popMatrix()
    }

    /**
     * Translate in X and Y.
     */
    fun translate(tx: Float, ty: Float) {
        graphics.translate(tx, ty)
    }

    /**
     * Translate in X, Y, and Z.
     */
    fun translate(tx: Float, ty: Float, tz: Float) {
        graphics.translate(tx, ty, tz)
    }

    /**
     * Two dimensional rotation.
     *
     *
     * Same as rotateZ (this is identical to a 3D rotation along the z-axis) but
     * included for clarity. It'd be weird for people drawing 2D graphics to be
     * using rotateZ. And they might kick our a-- for the confusion.
     *
     *
     * <A HREF="http://www.xkcd.com/c184.html">Additional background</A>.
     */
    fun rotate(angle: Float) {
        graphics.rotate(angle)
    }

    /**
     * Rotate around the X axis.
     */
    fun rotateX(angle: Float) {
        graphics.rotateX(angle)
    }

    /**
     * Rotate around the Y axis.
     */
    fun rotateY(angle: Float) {
        graphics.rotateY(angle)
    }

    /**
     * Rotate around the Z axis.
     *
     *
     * The functions rotate() and rotateZ() are identical, it's just that it
     * make sense to have rotate() and then rotateX() and rotateY() when using
     * 3D; nor does it make sense to use a function called rotateZ() if you're
     * only doing things in 2D. so we just decided to have them both be the
     * same.
     */
    fun rotateZ(angle: Float) {
        graphics.rotateZ(angle)
    }

    /**
     * Rotate about a vector in space. Same as the glRotatef() function.
     */
    fun rotate(angle: Float, vx: Float, vy: Float, vz: Float) {
        graphics.rotate(angle, vx, vy, vz)
    }

    /**
     * Scale in all dimensions.
     */
    fun scale(s: Float) {
        graphics.scale(s)
    }

    /**
     * Scale in X and Y. Equivalent to scale(sx, sy, 1).
     *
     *
     * Not recommended for use in 3D, because the z-dimension is just scaled by
     * 1, since there's no way to know what else to scale it by.
     */
    fun scale(sx: Float, sy: Float) {
        graphics.scale(sx, sy)
    }

    /**
     * Scale in X, Y, and Z.
     */
    fun scale(x: Float, y: Float, z: Float) {
        graphics.scale(x, y, z)
    }

    /**
     * Shear along X axis
     */
    fun shearX(angle: Float) {
        graphics.shearX(angle)
    }

    /**
     * Skew along Y axis
     */
    fun shearY(angle: Float) {
        graphics.shearY(angle)
    }

    /**
     * Set the current transformation matrix to identity.
     */
    fun resetMatrix() {
        graphics.resetMatrix()
    }

    fun applyMatrix(source: RMatrix) {
        graphics.applyMatrix(source)
    }

    fun applyMatrix(source: RMatrix2D) {
        graphics.applyMatrix(source)
    }

    /**
     * Apply a 3x2 affine transformation matrix.
     */
    fun applyMatrix(n00: Float, n01: Float, n02: Float, n10: Float, n11: Float, n12: Float) {
        graphics.applyMatrix(n00, n01, n02, n10, n11, n12)
    }

    fun applyMatrix(source: RMatrix3D) {
        graphics.applyMatrix(source)
    }

    /**
     * Apply a 4x4 transformation matrix.
     */
    fun applyMatrix(n00: Float,
                    n01: Float,
                    n02: Float,
                    n03: Float,
                    n10: Float,
                    n11: Float,
                    n12: Float,
                    n13: Float,
                    n20: Float,
                    n21: Float,
                    n22: Float,
                    n23: Float,
                    n30: Float,
                    n31: Float,
                    n32: Float,
                    n33: Float) {
        graphics.applyMatrix(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33)
    }

    /**
     * Set the current transformation to the contents of the specified source.
     */
    fun setMatrix(source: RMatrix3D) {
        graphics.setMatrix(source)
    }

    /**
     * Set the current transformation matrix to the contents of another.
     */
    fun setMatrix(source: RMatrix) {
        graphics.matrix = source
    }

    /**
     * Set the current transformation to the contents of the specified source.
     */
    fun setMatrix(source: RMatrix2D) {
        graphics.setMatrix(source)
    }

    /**
     * Copy the current transformation matrix into the specified target. Pass in
     * null to create a new matrix.
     */
    fun getMatrix(target: RMatrix2D): RMatrix2D? {
        return graphics.getMatrix(target)
    }

    /**
     * Copy the current transformation matrix into the specified target. Pass in
     * null to create a new matrix.
     */
    fun getMatrix(target: RMatrix3D): RMatrix3D? {
        return graphics.getMatrix(target)
    }

    /**
     * Print the current model (or "transformation") matrix.
     */
    fun printMatrix() {
        graphics.printMatrix()
    }

    fun beginCamera() {
        graphics.beginCamera()
    }

    fun endCamera() {
        graphics.endCamera()
    }

    fun camera() {
        graphics.camera()
    }

    fun camera(eyeX: Float, eyeY: Float, eyeZ: Float, centerX: Float, centerY: Float, centerZ: Float, upX: Float, upY: Float, upZ: Float) {
        graphics.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
    }

    fun printCamera() {
        graphics.printCamera()
    }

    fun ortho() {
        graphics.ortho()
    }

    fun ortho(left: Float, right: Float, bottom: Float, top: Float) {
        graphics.ortho(left, right, bottom, top)
    }

    fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        graphics.ortho(left, right, bottom, top, near, far)
    }

    fun perspective() {
        graphics.perspective()
    }

    fun perspective(fovy: Float, aspect: Float, zNear: Float, zFar: Float) {
        graphics.perspective(fovy, aspect, zNear, zFar)
    }

    fun frustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        graphics.frustum(left, right, bottom, top, near, far)
    }

    fun printProjection() {
        graphics.printProjection()
    }

    /**
     * Given an x and y coordinate, returns the x position of where that point
     * would be placed on screen, once affected by translate(), scale(), or any
     * other transformations.
     */
    fun screenX(x: Float, y: Float): Float {
        return graphics.screenX(x, y)
    }

    /**
     * Given an x and y coordinate, returns the y position of where that point
     * would be placed on screen, once affected by translate(), scale(), or any
     * other transformations.
     */
    fun screenY(x: Float, y: Float): Float {
        return graphics.screenY(x, y)
    }

    /**
     * Maps a three dimensional point to its placement on-screen.
     *
     *
     * Given an (x, y, z) coordinate, returns the x position of where that point
     * would be placed on screen, once affected by translate(), scale(), or any
     * other transformations.
     */
    fun screenX(x: Float, y: Float, z: Float): Float {
        return graphics.screenX(x, y, z)
    }

    /**
     * Maps a three dimensional point to its placement on-screen.
     *
     *
     * Given an (x, y, z) coordinate, returns the y position of where that point
     * would be placed on screen, once affected by translate(), scale(), or any
     * other transformations.
     */
    fun screenY(x: Float, y: Float, z: Float): Float {
        return graphics.screenY(x, y, z)
    }

    /**
     * Maps a three dimensional point to its placement on-screen.
     *
     *
     * Given an (x, y, z) coordinate, returns its z value. This value can be
     * used to determine if an (x, y, z) coordinate is in front or in back of
     * another (x, y, z) coordinate. The units are based on how the zbuffer is
     * set up, and don't relate to anything "real". They're only useful for in
     * comparison to another value obtained from screenZ(), or directly out of
     * the zbuffer[].
     */
    fun screenZ(x: Float, y: Float, z: Float): Float {
        return graphics.screenZ(x, y, z)
    }

    /**
     * Returns the model space x value for an x, y, z coordinate.
     *
     *
     * This will give you a coordinate after it has been transformed by
     * translate(), rotate(), and camera(), but not yet transformed by the
     * projection matrix. For instance, his can be useful for figuring out how
     * points in 3D space relate to the edge coordinates of a shape.
     */
    fun modelX(x: Float, y: Float, z: Float): Float {
        return graphics.modelX(x, y, z)
    }

    /**
     * Returns the model space y value for an x, y, z coordinate.
     */
    fun modelY(x: Float, y: Float, z: Float): Float {
        return graphics.modelY(x, y, z)
    }

    /**
     * Returns the model space z value for an x, y, z coordinate.
     */
    fun modelZ(x: Float, y: Float, z: Float): Float {
        return graphics.modelZ(x, y, z)
    }

    fun pushStyle() {
        graphics.pushStyle()
    }

    fun popStyle() {
        graphics.popStyle()
    }

    fun style(s: RainbowStyle) {
        graphics.style(s)
    }

    fun strokeWeight(weight: Float) {
        graphics.strokeWeight(weight)
    }

    fun strokeJoin(mode: Modes.Stroke.Join) {
        graphics.strokeJoin(mode)
    }

    fun strokeCap(mode: Modes.Stroke.Cap) {
        graphics.strokeCap(mode)
    }

    fun noStroke() {
        graphics.noStroke()
    }

    fun fillShader(shader: Shader) {
        if (graphics is RainbowGraphics2D) {
            (graphics as RainbowGraphics2D).fillPaint.shader = shader
        }
    }

    fun strokeShader(shader: Shader) {
        if (graphics is RainbowGraphics2D) {
            (graphics as RainbowGraphics2D).strokePaint.shader = shader
        }
    }

    /**
     * Set the tint to either a grayscale or ARGB value. See notes attached to
     * the fill() function.
     */
    fun stroke(rgb: Int) {
        graphics.stroke(rgb)
    }

    fun stroke(rgb: Int, alpha: Float) {
        graphics.stroke(rgb, alpha)
    }

    fun stroke(gray: Float) {
        graphics.stroke(gray)
    }

    fun stroke(gray: Float, alpha: Float) {
        graphics.stroke(gray, alpha)
    }

    fun stroke(x: Float, y: Float, z: Float) {
        graphics.stroke(x, y, z)
    }

    fun stroke(x: Float, y: Float, z: Float, a: Float) {
        graphics.stroke(x, y, z, a)
    }

    fun noTint() {
        graphics.noTint()
    }

    /**
     * Set the tint to either a grayscale or ARGB value.
     */
    fun tint(rgb: Int) {
        graphics.tint(rgb)
    }

    fun tint(rgb: Int, alpha: Float) {
        graphics.tint(rgb, alpha)
    }

    fun tint(gray: Float) {
        graphics.tint(gray)
    }

    fun tint(gray: Float, alpha: Float) {
        graphics.tint(gray, alpha)
    }

    fun tint(x: Float, y: Float, z: Float) {
        graphics.tint(x, y, z)
    }

    fun tint(x: Float, y: Float, z: Float, a: Float) {
        graphics.tint(x, y, z, a)
    }

    fun noFill() {
        graphics.noFill()
    }

    /**
     * Set the fill to either a grayscale value or an ARGB int.
     */
    fun fill(rgb: Int) {
        graphics.fill(rgb)
    }

    fun fill(rgb: Int, alpha: Float) {
        graphics.fill(rgb, alpha)
    }

    fun fill(gray: Float) {
        graphics.fill(gray)
    }

    fun fill(gray: Float, alpha: Float) {
        graphics.fill(gray, alpha)
    }

    fun fill(x: Float, y: Float, z: Float) {
        graphics.fill(x, y, z)
    }

    fun fill(x: Float, y: Float, z: Float, a: Float) {
        graphics.fill(x, y, z, a)
    }

    fun ambient(rgb: Int) {
        graphics.ambient(rgb)
    }

    fun ambient(gray: Float) {
        graphics.ambient(gray)
    }

    fun ambient(x: Float, y: Float, z: Float) {
        graphics.ambient(x, y, z)
    }

    fun specular(rgb: Int) {
        graphics.specular(rgb)
    }

    fun specular(gray: Float) {
        graphics.specular(gray)
    }

    fun specular(x: Float, y: Float, z: Float) {
        graphics.specular(x, y, z)
    }

    fun shininess(shine: Float) {
        graphics.shininess(shine)
    }

    fun emissive(rgb: Int) {
        graphics.emissive(rgb)
    }

    fun emissive(gray: Float) {
        graphics.emissive(gray)
    }

    fun emissive(x: Float, y: Float, z: Float) {
        graphics.emissive(x, y, z)
    }

    fun lights() {
        graphics.lights()
    }

    fun noLights() {
        graphics.noLights()
    }

    fun ambientLight(red: Float, green: Float, blue: Float) {
        graphics.ambientLight(red, green, blue)
    }

    fun ambientLight(red: Float, green: Float, blue: Float, x: Float, y: Float, z: Float) {
        graphics.ambientLight(red, green, blue, x, y, z)
    }

    fun directionalLight(red: Float, green: Float, blue: Float, nx: Float, ny: Float, nz: Float) {
        graphics.directionalLight(red, green, blue, nx, ny, nz)
    }

    fun pointLight(red: Float, green: Float, blue: Float, x: Float, y: Float, z: Float) {
        graphics.pointLight(red, green, blue, x, y, z)
    }

    fun spotLight(red: Float,
                  green: Float,
                  blue: Float,
                  x: Float,
                  y: Float,
                  z: Float,
                  nx: Float,
                  ny: Float,
                  nz: Float,
                  angle: Float,
                  concentration: Float) {
        graphics.spotLight(red, green, blue, x, y, z, nx, ny, nz, angle, concentration)
    }

    fun lightFalloff(constant: Float, linear: Float, quadratic: Float) {
        graphics.lightFalloff(constant, linear, quadratic)
    }

    fun lightSpecular(x: Float, y: Float, z: Float) {
        graphics.lightSpecular(x, y, z)
    }

    /**
     * Set the background to a gray or ARGB color.
     *
     *
     * For the main drawing surface, the alpha value will be ignored. However,
     * alpha can be used on PGraphics objects from createGraphics(). This is the
     * only way to set all the pixels partially transparent, for instance.
     *
     *
     * Note that background() should be called before any transformations occur,
     * because some implementations may require the current transformation
     * matrix to be identity before drawing.
     */
    fun background(rgb: Int) {
        graphics.background(rgb)
    }

    /**
     * See notes about alpha in background(x, y, z, a).
     */
    fun background(rgb: Int, alpha: Float) {
        graphics.background(rgb, alpha)
    }

    /**
     * Set the background to a grayscale value, based on the current colorMode.
     */
    fun background(gray: Float) {
        graphics.background(gray)
    }

    /**
     * See notes about alpha in background(x, y, z, a).
     */
    fun background(gray: Float, alpha: Float) {
        graphics.background(gray, alpha)
    }

    /**
     * Set the background to an r, g, b or h, s, b value, based on the current
     * colorMode.
     */
    fun background(x: Float, y: Float, z: Float) {
        graphics.background(x, y, z)
    }

    /**
     * Clear the background with a color that includes an alpha value. This can
     * only be used with objects created by createGraphics(), because the main
     * drawing surface cannot be set transparent.
     *
     *
     * It might be tempting to use this function to partially clear the screen
     * on each frame, however that's not how this function works. When calling
     * background(), the pixels will be replaced with pixels that have that
     * level of transparency. To do a semi-transparent overlay, use fill() with
     * alpha and draw a rectangle.
     */
    fun background(x: Float, y: Float, z: Float, a: Float) {
        graphics.background(x, y, z, a)
    }

    fun clear() {
        graphics.clear()
    }

    /**
     * Takes an RGB or ARGB image and sets it as the background. The width and
     * height of the image must be the same size as the sketch. Use
     * image.resize(width, height) to make short work of such a task.
     *
     *
     * Note that even if the image is set as RGB, the high 8 bits of each pixel
     * should be set opaque (0xFF000000), because the image data will be copied
     * directly to the screen, and non-opaque background images may have strange
     * behavior. Using image.filter(OPAQUE) will handle this easily.
     *
     *
     * When using 3D, this will also clear the zbuffer (if it exists).
     */
    fun background(image: RainbowImage) {
        image(image, 0f, 0f, width.toFloat(), height.toFloat())
    }

    fun image(image: RainbowImage, x: Float, y: Float, c: Float, d: Float) {
        graphics.image(image, x, y, c, d)
    }

    fun colorMode(mode: Modes.Image) {
        graphics.colorMode(mode)
    }

    fun colorMode(mode: Modes.Image, max: Float) {
        graphics.colorMode(mode, max)
    }

    /**
     * Set the colorMode and the maximum values for (r, g, b) or (h, s, b).
     *
     *
     * Note that this doesn't set the maximum for the alpha value, which might
     * be confusing if for instance you switched to
     *
     *
     * <PRE>
     * colorMode(HSB, 360, 100, 100);
    </PRE> *
     *
     *
     * because the alpha values were still between 0 and 255.
     */
    fun colorMode(mode: Modes.Image, maxX: Float, maxY: Float, maxZ: Float) {
        graphics.colorMode(mode, maxX, maxY, maxZ)
    }

    fun colorMode(mode: Modes.Image, maxX: Float, maxY: Float, maxZ: Float, maxA: Float) {
        graphics.colorMode(mode, maxX, maxY, maxZ, maxA)
    }

    fun alpha(what: Int): Float {
        return graphics.alpha(what)
    }

    fun red(what: Int): Float {
        return graphics.red(what)
    }

    fun green(what: Int): Float {
        return graphics.green(what)
    }

    fun blue(what: Int): Float {
        return graphics.blue(what)
    }

    fun hue(what: Int): Float {
        return graphics.hue(what)
    }

    fun saturation(what: Int): Float {
        return graphics.saturation(what)
    }

    fun brightness(what: Int): Float {
        return graphics.brightness(what)
    }

    /**
     * Grab a subsection of a PImage, and copy it into a fresh PImage. As of
     * release 0149, no longer honors imageMode() for the coordinates.
     */

    /**
     * Interpolate between two colors, using the current color mode.
     */
    fun lerpColor(c1: Int, c2: Int, amt: Float): Int {
        return graphics.lerpColor(c1, c2, amt)
    }

    /**
     * Return true if this renderer should be drawn to the screen. Defaults to
     * returning true, since nearly all renderers are on-screen beasts. But can
     * be overridden for subclasses like PDF so that a window doesn't open up. <br></br>
     * <br></br>
     * A better name? showFrame, displayable, isVisible, visible, shouldDisplay,
     * what to call this?
     */
    fun displayable(): Boolean {
        return graphics.displayable()
    }

    /**
     * Returns an ARGB "color" type (a packed 32 bit int with the color. If the
     * coordinate is outside the image, zero is returned (black, but completely
     * transparent).
     *
     *
     * If the image is in RGB format (i.e. on a PVideo object), the value will
     * get its high bits set, just to avoid cases where they haven't been set
     * already.
     *
     *
     * If the image is in ALPHA format, this returns a white with its alpha
     * value set.
     *
     *
     * This function is included primarily for beginners. It is quite slow
     * because it has to check to see if the x, y that was provided is inside
     * the bounds, and then has to check to see what image type it is. If you
     * want things to be more efficient, access the pixels[] array directly.
     */
    operator fun get(x: Int, y: Int): Int {
        return graphics.get(x, y)
    }

    /**
     * @param w width of pixel rectangle to get
     * @param h height of pixel rectangle to get
     */
    operator fun get(x: Int, y: Int, w: Int, h: Int): RainbowImage {
        return graphics.get(x, y, w, h)
    }

    /**
     * Returns a copy of this PImage. Equivalent to get(0, 0, width, height).
     */
    fun get(): RainbowImage {
        return graphics.get()
    }

    /**
     * Set a single pixel to the specified color.
     */
    operator fun set(x: Int, y: Int, c: Int) {
        graphics.set(x, y, c)
    }

    /**
     * Efficient method of drawing an image's pixels directly to this surface.
     * No variations are employed, meaning that any scale, tint, or imageMode
     * settings will be ignored.
     */
    operator fun set(x: Int, y: Int, img: RainbowImage) {
        graphics.set(x, y, img)
    }

    /**
     * Set alpha channel for an image. Black colors in the source image will
     * make the destination image completely transparent, and white will make
     * things fully opaque. Gray values will be in-between steps.
     *
     *
     * Strictly speaking the "blue" value from the source image is used as the
     * alpha color. For a fully grayscale image, this is correct, but for a
     * color image it's not 100% accurate. For a more accurate conversion, first
     * use filter(GRAY) which will make the image into a "correct" grayscake by
     * performing a proper luminance-based conversion.
     */
    fun mask(alpha: IntArray) {
        graphics.mask(alpha)
    }

    /**
     * Set alpha channel for an image using another image as the source.
     */
    fun mask(alpha: RainbowImage) {
        graphics.mask(alpha)
    }

    /**
     * Method to apply a variety of basic filters to this image.
     *
     *
     * <UL>
     * <LI>filter(BLUR) provides a basic blur.
    </LI> * <LI>filter(GRAY) converts the image to grayscale based on luminance.
    </LI> * <LI>filter(INVERT) will invert the color components in the image.
    </LI> * <LI>filter(OPAQUE) set all the high bits in the image to opaque
    </LI> * <LI>filter(THRESHOLD) converts the image to black and white.
    </LI> * <LI>filter(DILATE) grow white/light areas
    </LI> * <LI>filter(ERODE) shrink white/light areas
    </LI></UL> *
     * Luminance conversion code contributed by <A HREF="http://www.toxi.co.uk">toxi</A>
     *
     *
     * Gaussian blur code contributed by <A HREF="http://incubator.quasimondo.com">Mario Klingemann</A>
     */
    fun filter(mode: Modes.Filter) {
        graphics.filter(mode)
    }

    /**
     * Method to apply a variety of basic filters to this image. These filters
     * all take a parameter.
     *
     *
     * <UL>
     * <LI>filter(BLUR, int radius) performs a gaussian blur of the specified
     * radius.
    </LI> * <LI>filter(POSTERIZE, int levels) will posterize the image to between 2
     * and 255 levels.
    </LI> * <LI>filter(THRESHOLD, float center) allows you to set the center point
     * for the threshold. It takes a value from 0 to 1.0.
    </LI></UL> *
     * Gaussian blur code contributed by <A HREF="http://incubator.quasimondo.com">Mario Klingemann</A> and later
     * updated by toxi for better speed.
     */
    fun filter(mode: Modes.Filter, param: Float) {
        graphics.filter(mode, param)
    }

    /**
     * Copy things from one area of this image to another area in the same
     * image.
     */
    fun copy(sx: Int, sy: Int, sw: Int, sh: Int, dx: Int, dy: Int, dw: Int, dh: Int) {
        graphics.copy(sx, sy, sw, sh, dx, dy, dw, dh)
    }

    /**
     * Copies area of one image into another PImage object.
     */
    fun copy(src: RainbowImage, sx: Int, sy: Int, sw: Int, sh: Int, dx: Int, dy: Int, dw: Int, dh: Int) {
        graphics.copy(src, sx, sy, sw, sh, dx, dy, dw, dh)
    }

    /**
     * Blends one area of this image to another area.
     *
     * @see com.juankysoriano.rainbow.core.graphics.RainbowImage.blendColor
     */
    fun blend(sx: Int, sy: Int, sw: Int, sh: Int, dx: Int, dy: Int, dw: Int, dh: Int, mode: Modes.Blend) {
        graphics.blend(sx, sy, sw, sh, dx, dy, dw, dh, mode)
    }

    /**
     * Copies area of one image into another PImage object.
     *
     * @see com.juankysoriano.rainbow.core.graphics.RainbowImage.blendColor
     */
    fun blend(src: RainbowImage, sx: Int, sy: Int, sw: Int, sh: Int, dx: Int, dy: Int, dw: Int, dh: Int, mode: Modes.Blend) {
        graphics.blend(src, sx, sy, sw, sh, dx, dy, dw, dh, mode)
    }

    fun invalidate() {
        beginDraw()
        endDraw()
    }

    interface PointDetectedListener {
        fun onPointDetected(px: Float, py: Float, x: Float, y: Float)
    }

    enum class Precision private constructor(val value: Int) {
        VERY_HIGH(1),
        HIGH(4),
        NORMAL(16),
        LOW(32)
    }

    companion object {

        /**
         * Interpolate between two colors. Like lerp(), but for the individual color
         * components of a color supplied as an int value.
         */
        fun lerpColor(c1: Int, c2: Int, amt: Float, mode: Modes.Image): Int {
            return RainbowGraphics.lerpColor(c1, c2, amt, mode)
        }

        /**
         * Returns color resulting of blending two input colors given the blend mode.
         *
         * @param c1
         * @param c2
         * @param mode
         * @return
         */
        fun blendColor(c1: Int, c2: Int, mode: Modes.Blend): Int {
            return RainbowImage.blendColor(c1, c2, mode)
        }
    }
}
