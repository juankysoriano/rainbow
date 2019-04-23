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

package com.juankysoriano.rainbow.core.drawing;

public class RainbowStyle {
    public Modes.Draw imageDrawMode;
    public Modes.Draw rectMode;
    public Modes.Draw ellipseMode;
    public Modes.Shape shapeMode;

    public Modes.Image colorMode;
    public float colorModeX;
    public float colorModeY;
    public float colorModeZ;
    public float colorModeA;

    public boolean tint;
    public int tintColor;
    public boolean fill;
    public int fillColor;
    public boolean stroke;
    public int strokeColor;
    public float strokeWeight;
    public Modes.Stroke.Cap strokeCap;
    public Modes.Stroke.Join strokeJoin;

    // TODO these fellas are inconsistent, and may need to go elsewhere
    public float ambientR, ambientG, ambientB;
    public float specularR, specularG, specularB;
    public float emissiveR, emissiveG, emissiveB;
    public float shininess;

}
