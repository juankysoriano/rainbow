package com.juankysoriano.rainbow.core.cv.blobdetector;

//==================================================
//class EdgeDetection
//==================================================
public class EdgeDetection extends Metaballs2D {
    public final static byte C_R = 0x01;
    public final static byte C_G = 0x02;
    public final static byte C_B = 0x04;
    // public final static byte C_ALL = C_R|C_G|C_B;

    // public byte colorFlag;
    public int imgWidth, imgHeight;
    public int[] pixels;
    public boolean posDiscrimination;

    public float m_coeff = 3.0f * 255.0f;

    // --------------------------------------------
    // Constructor
    // --------------------------------------------
    public EdgeDetection(int imgWidth, int imgHeight) {
        super.init(imgWidth, imgHeight);
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        posDiscrimination = false;
    }

    // --------------------------------------------
    // setPosDiscrimination()
    // --------------------------------------------
    public void setPosDiscrimination(boolean is) {
        posDiscrimination = is;
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
    // computeEdges()
    // --------------------------------------------
    public void computeEdges(int[] pixels) {
        setImage(pixels);
        computeMesh();
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

        if (posDiscrimination) {
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
        } else {
            if (gridValue[x + offY] < isovalue) {
                squareIndex |= 1;
            }
            if (gridValue[x + 1 + offY] < isovalue) {
                squareIndex |= 2;
            }
            if (gridValue[x + 1 + nextOffY] < isovalue) {
                squareIndex |= 4;
            }
            if (gridValue[x + nextOffY] < isovalue) {
                squareIndex |= 8;
            }
        }
        return squareIndex;
    }

    // --------------------------------------------
    // getEdgeVertex()
    // --------------------------------------------
    public EdgeVertex getEdgeVertex(int index) {
        return edgeVrt[index];
    }
};
