package com.juankysoriano.rainbow.core.drawing

interface Modes {
    enum class Arc {
        UNDEFINED,
        OPEN,
        CHORD,
        PIE
    }

    enum class Blend {
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

    enum class Draw {
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
         * Synonym for the CENTER constant. Draw from the center,
         * using second pair of values as the diameter.
         */
        DIAMETER
    }

    enum class Filter {
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

    enum class Image {
        RGB, // image & color
        ARGB, // image
        HSB, // color
        ALPHA // image
    }

    enum class Shape {
        UNDEFINED,
        OPEN,
        CLOSE,
        POINTS, // vertices
        LINES, // beginShape(), createShape()
        TRIANGLES, // vertices
        TRIANGLE_STRIP, // vertices
        TRIANGLE_FAN, // vertices
        QUAD, // primitive
        QUADS, // vertices
        QUAD_STRIP, // vertices
        POLYGON  // in the end, probably cannot
    }

    interface Stroke {
        enum class Cap {
            BUTT,
            ROUND,
            SQUARE
        }

        enum class Join {
            ROUND,
            MITER,
            BEVEL
        }
    }

    enum class LoadMode {
        LOAD_CENTER_INSIDE,
        LOAD_CENTER_CROP,
        LOAD_ORIGINAL_SIZE
    }
}
