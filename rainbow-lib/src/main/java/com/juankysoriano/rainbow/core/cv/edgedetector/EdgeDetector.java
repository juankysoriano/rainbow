package com.juankysoriano.rainbow.core.cv.edgedetector;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.juankysoriano.rainbow.core.matrix.RVector;
import com.juankysoriano.rainbow.utils.RainbowMath;

import java.util.ArrayList;
import java.util.List;

/*
 An algorithm that uses a custom implementation of a Sobel/Scharr operator to get 
 the significant points of a picture.
 */

public class EdgeDetector {
    public static int SOBEL = 0, SCHARR = 1; // indexes of the kernels in the
    private int[][][] OPERATOR = new int[][][]{{{2, 2, 0}, {2, 0, -2}, {0, -2, -2}}, // Sobel
            // kernel
            {{6, 10, 0}, {10, 0, -10}, {0, -10, -6}}}; // Scharr kernel
    // previous array
    private int op = SCHARR;
    private int treshold = 350;
    private int step = 3;

    public EdgeDetector() {
    }

    public void setOperator(int operator) {
        op = operator;
    }

    public void changeTreshold(int threshold) {
        this.treshold = RainbowMath.constrain(threshold, 50, 2000);
    }

    public void changeStep(int step) {
        this.step = RainbowMath.constrain(step, 2, 40);
    }

    // This method add significant points of the given picture to a given list
    public List<RVector> extractPoints(RainbowImage img) {
        List<RVector> vertices = new ArrayList<>();
        int col = 0, colSum = 0, W = img.width - 1, H = img.height - 1;

        // For any pixel in the image excepting borders
        for (int Y = 1; Y < H; Y += step) {
            for (int X = 1; X < W; X += step, colSum = 0) {
                // Convolute surrounding pixels with desired operator
                for (int y = -1; y <= 1; y++) {
                    for (int x = -1; x <= 1; x++, col = img.get((X + x), (Y + y))) {
                        colSum += OPERATOR[op][x + 1][y + 1] * ((col >> 16 & 0xFF) + (col >> 8 & 0xFF) + (col & 0xFF));
                    }
                }
                // And if the resulting sum is over the treshold add pixel
                // position to the list
                if (RainbowMath.abs(colSum) > treshold) {
                    vertices.add(new RVector(X, Y));
                }
            }
        }
        return vertices;
    }
}
