package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.matrix.RVector;

import java.util.ArrayList;
import java.util.List;

public class Blob {
    private float xMin, xMax, yMin, yMax;
    private List<EdgeVertex> edgeVertexes;

    public Blob() {
        edgeVertexes = new ArrayList<>();
        xMin = Integer.MAX_VALUE;
        xMax = Integer.MIN_VALUE;
        yMin = Integer.MAX_VALUE;
        yMax = Integer.MIN_VALUE;
    }

    public EdgeVertex getEdgeVertex(int edgeIndex) {
        return edgeVertexes.get(edgeIndex % edgeVertexes.size());
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
        float x = (xMax + xMin) / 2;
        float y = (yMax + yMin) / 2;
        return new RVector(x, y);
    }

    public float getWidth() {
        return xMax - xMin;
    }

    public float getHeight() {
        return yMax - yMin;
    }
}
