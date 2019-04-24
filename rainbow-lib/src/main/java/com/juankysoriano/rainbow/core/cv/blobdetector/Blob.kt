package com.juankysoriano.rainbow.core.cv.blobdetector

import com.juankysoriano.rainbow.core.math.RVector

data class Blob(
        private var xMin: Float = Float.MAX_VALUE,
        private var xMax: Float = Float.MIN_VALUE,
        private var yMin: Float = Float.MAX_VALUE,
        private var yMax: Float = Float.MIN_VALUE,
        private val edgeVertexes: MutableList<EdgeVertex> = ArrayList()
) {

    val edgeCount: Int
        get() = edgeVertexes.size

    val area: Float
        get() {
            val w = xMax - xMin
            val h = yMax - yMin
            return w * h
        }

    val center: RVector
        get() {
            val x = (xMax + xMin) / 2
            val y = (yMax + yMin) / 2
            return RVector(x, y)
        }

    val width: Float
        get() = xMax - xMin

    val height: Float
        get() = yMax - yMin

    fun getEdgeVertex(edgeIndex: Int): EdgeVertex = edgeVertexes[edgeIndex % edgeVertexes.size]

    fun addEdgeVertex(edgeVertex: EdgeVertex) {
        edgeVertexes.add(edgeVertex)
        xMin = Math.min(edgeVertex.x, xMin)
        xMax = Math.max(edgeVertex.x, xMax)
        yMin = Math.min(edgeVertex.y, yMin)
        yMax = Math.max(edgeVertex.y, yMax)
    }
}
