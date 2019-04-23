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

package com.juankysoriano.rainbow.core.matrix

/**
 * 3x2 affine matrix implementation.
 */
class RMatrix2D : RMatrix {

    var m00: Float = 0.toFloat()
    var m01: Float = 0.toFloat()
    var m02: Float = 0.toFloat()
    var m10: Float = 0.toFloat()
    var m11: Float = 0.toFloat()
    var m12: Float = 0.toFloat()

    //////////////////////////////////////////////////////////////

    // TODO these need to be added as regular API, but the naming and
    // implementation needs to be improved first. (e.g. actually keeping track
    // of whether the matrix is in fact identity internally.)

    private val isIdentity: Boolean
        get() = m00 == 1f && m01 == 0f && m02 == 0f &&
                m10 == 0f && m11 == 1f && m12 == 0f

    // TODO make this more efficient, or move into PMatrix2D
    private val isWarped: Boolean
        get() = m00 != 1f || m01 != 0f ||
                m10 != 0f || m11 != 1f

    constructor() {
        reset()
    }

    constructor(m00: Float, m01: Float, m02: Float,
                m10: Float, m11: Float, m12: Float) {
        set(m00, m01, m02, m10, m11, m12)
    }

    constructor(RMatrix: RMatrix) {
        set(RMatrix)
    }

    override fun reset() {
        set(1f, 0f, 0f,
                0f, 1f, 0f)
    }

    /**
     * Returns a copy of this PMatrix.
     */
    override fun get(): RMatrix2D {
        val outgoing = RMatrix2D()
        outgoing.set(this)
        return outgoing
    }

    /**
     * Copies the matrix contents into a 6 entry float array.
     * If target is null (or not the correct size), a new array will be created.
     */
    override fun toFloatArray(): FloatArray {
        val target = FloatArray(6)
        target[0] = m00
        target[1] = m01
        target[2] = m02

        target[3] = m10
        target[4] = m11
        target[5] = m12

        return target
    }

    override fun set(src: RMatrix) {
        if (src is RMatrix2D) {
            set(src.m00, src.m01, src.m02,
                    src.m10, src.m11, src.m12)
        } else {
            throw IllegalArgumentException("PMatrix2D.set() only accepts PMatrix2D objects.")
        }
    }

    override fun set(source: FloatArray) {
        m00 = source[0]
        m01 = source[1]
        m02 = source[2]

        m10 = source[3]
        m11 = source[4]
        m12 = source[5]
    }

    override fun set(m00: Float, m01: Float, m02: Float,
                     m10: Float, m11: Float, m12: Float) {
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
    }

    override fun set(m00: Float, m01: Float, m02: Float, m03: Float,
                     m10: Float, m11: Float, m12: Float, m13: Float,
                     m20: Float, m21: Float, m22: Float, m23: Float,
                     m30: Float, m31: Float, m32: Float, m33: Float) {

    }

    override fun translate(tx: Float, ty: Float) {
        m02 += tx * m00 + ty * m01
        m12 += tx * m10 + ty * m11
    }

    override fun translate(x: Float, y: Float, z: Float) {
        throw IllegalArgumentException("Cannot use translate(x, y, z) on a PMatrix2D.")
    }

    // Implementation roughly based on AffineTransform.
    override fun rotate(angle: Float) {
        val sin = sin(angle)
        val cos = cos(angle)

        var temp1 = m00
        var temp2 = m01
        m00 = cos * temp1 + sin * temp2
        m01 = -sin * temp1 + cos * temp2
        temp1 = m10
        temp2 = m11
        m10 = cos * temp1 + sin * temp2
        m11 = -sin * temp1 + cos * temp2
    }

    override fun rotateX(angle: Float) {
        throw IllegalArgumentException("Cannot use rotateX() on a PMatrix2D.")
    }

    override fun rotateY(angle: Float) {
        throw IllegalArgumentException("Cannot use rotateY() on a PMatrix2D.")
    }

    override fun rotateZ(angle: Float) {
        rotate(angle)
    }

    override fun rotate(angle: Float, v0: Float, v1: Float, v2: Float) {
        throw IllegalArgumentException("Cannot use this version of rotate() on a PMatrix2D.")
    }

    override fun scale(s: Float) {
        scale(s, s)
    }

    override fun scale(sx: Float, sy: Float) {
        m00 *= sx
        m01 *= sy
        m10 *= sx
        m11 *= sy
    }

    override fun scale(x: Float, y: Float, z: Float) {
        throw IllegalArgumentException("Cannot use this version of scale() on a PMatrix2D.")
    }

    override fun shearX(angle: Float) {
        apply(1f, 0f, 1f, tan(angle), 0f, 0f)
    }

    override fun shearY(angle: Float) {
        apply(1f, 0f, 1f, 0f, tan(angle), 0f)
    }

    override fun apply(source: RMatrix) {
        if (source is RMatrix2D) {
            apply(source)
        } else if (source is RMatrix3D) {
            apply(source)
        }
    }

    override fun apply(source: RMatrix2D) {
        apply(source.m00, source.m01, source.m02,
                source.m10, source.m11, source.m12)
    }

    override fun apply(source: RMatrix3D) {
        throw IllegalArgumentException("Cannot use apply(PMatrix3D) on a PMatrix2D.")
    }

    override fun apply(n00: Float, n01: Float, n02: Float,
                       n10: Float, n11: Float, n12: Float) {
        var t0 = m00
        var t1 = m01
        m00 = n00 * t0 + n10 * t1
        m01 = n01 * t0 + n11 * t1
        m02 += n02 * t0 + n12 * t1

        t0 = m10
        t1 = m11
        m10 = n00 * t0 + n10 * t1
        m11 = n01 * t0 + n11 * t1
        m12 += n02 * t0 + n12 * t1
    }

    override fun apply(n00: Float, n01: Float, n02: Float, n03: Float,
                       n10: Float, n11: Float, n12: Float, n13: Float,
                       n20: Float, n21: Float, n22: Float, n23: Float,
                       n30: Float, n31: Float, n32: Float, n33: Float) {
        throw IllegalArgumentException("Cannot use this version of apply() on a PMatrix2D.")
    }

    /**
     * Apply another matrix to the left of this one.
     */
    override fun preApply(left: RMatrix2D) {
        preApply(left.m00, left.m01, left.m02,
                left.m10, left.m11, left.m12)
    }

    override fun preApply(left: RMatrix3D) {
        throw IllegalArgumentException("Cannot use preApply(PMatrix3D) on a PMatrix2D.")
    }

    override fun preApply(n00: Float, n01: Float, n02: Float,
                          n10: Float, n11: Float, n12: Float) {
        var n02 = n02
        var n12 = n12
        var t0 = m02
        var t1 = m12
        n02 += t0 * n00 + t1 * n01
        n12 += t0 * n10 + t1 * n11

        m02 = n02
        m12 = n12

        t0 = m00
        t1 = m10
        m00 = t0 * n00 + t1 * n01
        m10 = t0 * n10 + t1 * n11

        t0 = m01
        t1 = m11
        m01 = t0 * n00 + t1 * n01
        m11 = t0 * n10 + t1 * n11
    }

    override fun preApply(n00: Float, n01: Float, n02: Float, n03: Float,
                          n10: Float, n11: Float, n12: Float, n13: Float,
                          n20: Float, n21: Float, n22: Float, n23: Float,
                          n30: Float, n31: Float, n32: Float, n33: Float) {
        throw IllegalArgumentException("Cannot use this version of preApply() on a PMatrix2D.")
    }

    //////////////////////////////////////////////////////////////

    /**
     * Multiply the x and y coordinates of a PVector against this matrix.
     */
    override fun mult(source: RVector, target: RVector): RVector {
        var target = target
        target.x = m00 * source.x + m01 * source.y + m02
        target.y = m10 * source.x + m11 * source.y + m12
        return target
    }

    /**
     * Multiply a two element vector against this matrix.
     * If out is null or not length four, a new float array will be returned.
     * The values for vec and out can be the same (though that's less efficient).
     */
    override fun mult(source: FloatArray, target: FloatArray): FloatArray {
        var out = target
        if (out.size != 2) {
            out = FloatArray(2)
        }

        if (source.contentEquals(out)) {
            val tx = m00 * source[0] + m01 * source[1] + m02
            val ty = m10 * source[0] + m11 * source[1] + m12

            out[0] = tx
            out[1] = ty

        } else {
            out[0] = m00 * source[0] + m01 * source[1] + m02
            out[1] = m10 * source[0] + m11 * source[1] + m12
        }

        return out
    }

    fun multX(x: Float, y: Float): Float {
        return m00 * x + m01 * y + m02
    }

    fun multY(x: Float, y: Float): Float {
        return m10 * x + m11 * y + m12
    }

    /**
     * Transpose this matrix.
     */
    override fun transpose() {}

    /**
     * Invert this matrix. Implementation stolen from OpenJDK.
     *
     * @return true if successful
     */
    override fun invert(): Boolean {
        val determinant = determinant()
        if (Math.abs(determinant) <= java.lang.Float.MIN_VALUE) {
            return false
        }

        val t00 = m00
        val t01 = m01
        val t02 = m02
        val t10 = m10
        val t11 = m11
        val t12 = m12

        m00 = t11 / determinant
        m10 = -t10 / determinant
        m01 = -t01 / determinant
        m11 = t00 / determinant
        m02 = (t01 * t12 - t11 * t02) / determinant
        m12 = (t10 * t02 - t00 * t12) / determinant

        return true
    }

    /**
     * @return the determinant of the matrix
     */
    override fun determinant(): Float {
        return m00 * m11 - m01 * m10
    }

    //////////////////////////////////////////////////////////////

    private fun max(a: Float, b: Float): Float {
        return if (a > b) a else b
    }

    private fun abs(a: Float): Float {
        return if (a < 0) -a else a
    }

    private fun sin(angle: Float): Float {
        return Math.sin(angle.toDouble()).toFloat()
    }

    private fun cos(angle: Float): Float {
        return Math.cos(angle.toDouble()).toFloat()
    }

    private fun tan(angle: Float): Float {
        return Math.tan(angle.toDouble()).toFloat()
    }
}
