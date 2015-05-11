package com.juankysoriano.rainbow.core.cv.blobdetector;

//==================================================
//class EdgeDetection
//==================================================
public class EdgeDetection {

    // Isovalue
    // ------------------
    protected float isovalue;

    // Grid
    // ------------------
    protected int resx, resy;
    protected float stepx, stepy;
    protected float[] gridValue;
    protected int nbGridValue;

    // EdgeVertex
    // ------------------
    protected EdgeVertex[] edgeVrt;
    protected int nbEdgeVrt;

    // Lines
    // what we pass to the renderer
    // ------------------
    protected int nbLineToDraw;
    // public byte colorFlag;
    public int imgWidth, imgHeight;
    public int[] pixels;

    public float m_coeff = 3.0f * 255.0f;

    // --------------------------------------------
    // Constructor
    // --------------------------------------------
    public EdgeDetection(int imgWidth, int imgHeight) {
        init(imgWidth, imgHeight);
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
    }

    // --------------------------------------------
    // setThreshold()
    // --------------------------------------------
    public void setThreshold(float value) {
        if (value < 0.0f) {
            value = 0.0f;
        }
        if (value > 1.0f) {
            value = 1.0f;
        }
        setIsovalue(value * m_coeff);
    }

    // --------------------------------------------
    // setImage()
    // --------------------------------------------
    public void setImage(int[] pixels) {
        if (pixels != this.pixels) {
            this.pixels = pixels;
        }
    }

    // --------------------------------------------
    // computeIsovalue()
    // --------------------------------------------
    public void computeIsovalue() {
        int pixel, r, g, b;
        int x, y;
        int offset;

        for (y = 0; y < imgHeight; y++)
            for (x = 0; x < imgWidth; x++) {
                offset = x + imgWidth * y;

                // Add R,G,B
                pixel = pixels[offset];
                r = (pixel & 0x00FF0000) >> 16;
                g = (pixel & 0x0000FF00) >> 8;
                b = (pixel & 0x000000FF);

                gridValue[offset] = (float) (r + g + b);
            }
    }

    // --------------------------------------------
    // getSquareIndex()
    // --------------------------------------------
    protected int getSquareIndex(int x, int y) {
        int squareIndex = 0;
        int offY = resx * y;
        int nextOffY = resx * (y + 1);

        if (gridValue[x + offY] > isovalue) {
            squareIndex |= 1;
        }
        if (gridValue[x + 1 + offY] > isovalue) {
            squareIndex |= 2;
        }
        if (gridValue[x + 1 + nextOffY] > isovalue) {
            squareIndex |= 4;
        }
        if (gridValue[x + nextOffY] > isovalue) {
            squareIndex |= 8;
        }
        return squareIndex;
    }

    // --------------------------------------------
    // getEdgeVertex()
    // --------------------------------------------
    public EdgeVertex getEdgeVertex(int index) {
        return edgeVrt[index];
    }

    // init(int, int)
    // ------------------
    public void init(int resx, int resy) {
        this.resx = resx;
        this.resy = resy;

        this.stepx = 1.0f / ((float) (resx - 1));
        this.stepy = 1.0f / ((float) (resy - 1));

        // Allocate gridValue
        nbGridValue = resx * resy;
        gridValue = new float[nbGridValue];

        // Allocate EdgeVertices
        edgeVrt = new EdgeVertex[2 * nbGridValue];
        nbEdgeVrt = 2 * nbGridValue;

        // Allocate Lines
        nbLineToDraw = 0;

        // Precompute some values
        int n = 0;
        for (int x = 0; x < resx; x++) {
            for (int y = 0; y < resy; y++) {
                int index = 2 * n;
                edgeVrt[index] = new EdgeVertex(x * stepx, y * stepy);
                edgeVrt[index + 1] = new EdgeVertex(x * stepx, y * stepy);
                n++;
            }
        }

    }

    // setIsoValue(float)
    // ------------------
    public void setIsovalue(float iso) {
        this.isovalue = iso;
    }

}
