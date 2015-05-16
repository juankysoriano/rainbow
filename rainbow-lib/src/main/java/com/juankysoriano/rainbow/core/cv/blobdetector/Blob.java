package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.matrix.RVector;

import java.util.ArrayList;
import java.util.List;

public class Blob {
    private float x, y;
    private float xMin, xMax, yMin, yMax;
    private List<EdgeVertex> edgeVertexes;

    public Blob(float x, float y) {
        this.x = x;
        this.y = y;
        edgeVertexes = new ArrayList<>();
        xMin = Integer.MAX_VALUE;
        xMax = Integer.MIN_VALUE;
        yMin = Integer.MAX_VALUE;
        yMax = Integer.MIN_VALUE;
    }

    public EdgeVertex getEdgeVertexA(int lineIndex) {
        return edgeVertexes.get(lineIndex * 2);
    }

    public EdgeVertex getEdgeVertexB(int lineIndex) {
        return edgeVertexes.get(lineIndex * 2 + 1);
    }

    public int getLineCount() {
        return edgeVertexes.size() / 2;
    }

    public int getEdgeCount() {
        return edgeVertexes.size();
    }

    public void addEdgeVertex(EdgeVertex edgeVertex) {
        edgeVertexes.add(edgeVertex);
        xMin = Math.min(edgeVertex.x, xMin);
        xMax = Math.max(edgeVertex.x, xMax);
        yMin = Math.min(edgeVertex.y, yMin);
        yMax = Math.max(edgeVertex.y, yMax);
    }

    public float getArea() {
        float w = (xMax - xMin);
        float h = (yMax - yMin);
        return w * h;
    }

    public RVector getCenter() {
        x = (xMax + xMin)/2;
        y = (yMax + yMin)/2;
        return new RVector(x, y);
    }

    public float getWidth() {
        return xMax - xMin;
    }

    public float getHeight() {
        return yMax - yMin;
    }
}
