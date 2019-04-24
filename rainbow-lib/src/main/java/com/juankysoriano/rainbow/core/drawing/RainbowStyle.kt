/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2008-10 Ben Fry and Casey Reas

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

package com.juankysoriano.rainbow.core.drawing

data class RainbowStyle(
        var imageDrawMode: Modes.Draw? = null,
        var rectMode: Modes.Draw? = null,
        var ellipseMode: Modes.Draw? = null,
        var shapeMode: Modes.Shape? = null,
        var colorMode: Modes.Image? = null,
        var colorModeX: Float = 0f,
        var colorModeY: Float = 0f,
        var colorModeZ: Float = 0f,
        var colorModeA: Float = 0f,
        var tint: Boolean = false,
        var tintColor: Int = 0,
        var fill: Boolean = false,
        var fillColor: Int = 0,
        var stroke: Boolean = false,
        var strokeColor: Int = 0,
        var strokeWeight: Float = 0f,
        var strokeCap: Modes.Stroke.Cap? = null,
        var strokeJoin: Modes.Stroke.Join? = null
) {

    // TODO these fellas are inconsistent, and may need to go elsewhere
    var ambientR: Float = 0f
    var ambientG: Float = 0f
    var ambientB: Float = 0f
    var specularR: Float = 0f
    var specularG: Float = 0f
    var specularB: Float = 0f
    var emissiveR: Float = 0f
    var emissiveG: Float = 0f
    var emissiveB: Float = 0f
    var shininess: Float = 0f

}
