package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.matrix.RVector;

//==================================================
//class Blob
//==================================================
public class Blob {
    protected int id;
    protected float x, y; // position of its center
    protected float w, h; // width & height
    protected int[] line;
    protected float xMin, xMax, yMin, yMax;
    protected int nbLine;

    private final BlobDetection parent;

    public Blob(BlobDetection parent, int maxLinesPerBlob) {
        this.parent = parent;
        line = new int[maxLinesPerBlob]; // stack of index
        nbLine = 0;
    }

    public EdgeVertex getEdgeVertexA(int iEdge) {
        if (iEdge * 2 < parent.nbLineToDraw * 2) {
            return parent.getEdgeVertex(line[iEdge * 2]);
        } else {
            return null;
        }
    }

    public EdgeVertex getEdgeVertexB(int iEdge) {
        if ((iEdge * 2 + 1) < parent.nbLineToDraw * 2) {
            return parent.getEdgeVertex(line[iEdge * 2 + 1]);
        } else {
            return null;
        }
    }

    public int getEdgeNb() {
        return nbLine;
    }

    public void update() {
        w = (xMax - xMin);
        h = (yMax - yMin);
        x = 0.5f * (xMax + xMin);
        y = 0.5f * (yMax + yMin);
        nbLine /= 2;
    }

    public float getArea() {
        return w * h;
    }

    public RVector getCenter() {
        return new RVector(x, y);
    }
}