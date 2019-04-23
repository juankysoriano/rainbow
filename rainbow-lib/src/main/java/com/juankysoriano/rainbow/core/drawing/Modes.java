package com.juankysoriano.rainbow.core.drawing;

public interface Modes {
    enum Arc {
        UNDEFINED,
        OPEN,
        CHORD,
        PIE
    }

    enum Blend {
        REPLACE,
        BLEND,
        ADD,
        SUBTRACT,
        LIGHTEST,
        DARKEST,
        DIFFERENCE,
        EXCLUSION,
        MULTIPLY,
        SCREEN,
        OVERLAY,
        HARD_LIGHT,
        SOFT_LIGHT,
        DODGE,
        BURN
    }

    enum Draw {
        /**
         * Draw mode convention to use (x, y) to (width, height)
         */
        CORNER,
        /**
         * Draw mode convention to use (x1, y1) to (x2, y2) coordinates
         */
        CORNERS,
        /**
         * Draw mode from the center, and using the radius
         */
        RADIUS,
        /**
         * Draw from the center, using second pair of values as the diameter.
         * Formerly called CENTER_DIAMETER in alpha releases.
         */
        CENTER,
        /**
         * Synonym for the CENTER constant. Draw from the center,
         * using second pair of values as the diameter.
         */
        DIAMETER
    }

    enum Filter {
        BLUR,
        GRAY,
        RGB,
        INVERT,
        OPAQUE,
        POSTERIZE,
        THRESHOLD,
        ERODE,
        DILATE
    }

    enum Image {
        RGB,  // image & color
        ARGB,  // image
        HSB,  // color
        ALPHA // image
    }

    enum Shape {
        UNDEFINED,
        OPEN,
        CLOSE,
        POINTS,   // vertices
        LINES,   // beginShape(), createShape()
        TRIANGLES,   // vertices
        TRIANGLE_STRIP,  // vertices
        TRIANGLE_FAN,  // vertices
        QUAD,  // primitive
        QUADS,  // vertices
        QUAD_STRIP,  // vertices
        POLYGON  // in the end, probably cannot
    }

    interface Stroke {
        enum Cap {
            BUTT,
            ROUND,
            SQUARE
        }
        enum Join {
            ROUND,
            MITER,
            BEVEL
        }
    }

    enum LoadMode {
        LOAD_CENTER_INSIDE,
        LOAD_CENTER_CROP,
        LOAD_ORIGINAL_SIZE
    }
}
