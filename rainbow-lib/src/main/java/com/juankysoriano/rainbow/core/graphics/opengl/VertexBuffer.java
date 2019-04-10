/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-16 The Processing Foundation
  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package com.juankysoriano.rainbow.core.graphics.opengl;

// TODO: need to combine with PGraphicsOpenGL.VertexAttribute
public class VertexBuffer {
  static protected final int INIT_VERTEX_BUFFER_SIZE  = 256;
  static protected final int INIT_INDEX_BUFFER_SIZE   = 512;

  public int glId;
  int target;
  int elementSize;
  int ncoords;
  boolean index;

  protected RainbowGL rainbowGL;                // The interface between Processing and OpenGL.
  protected int context;            // The context that created this texture.
  private RainbowGraphicsOpenGL.GLResourceVertexBuffer glres;

  VertexBuffer(RainbowGraphicsOpenGL graphics, int target, int ncoords, int esize) {
    this(graphics, target, ncoords, esize, false);
  }

  VertexBuffer(RainbowGraphicsOpenGL graphics, int target, int ncoords, int esize, boolean index) {
    rainbowGL = graphics.rainbowGl;
    context = rainbowGL.createEmptyContext();

    this.target = target;
    this.ncoords = ncoords;
    this.elementSize = esize;
    this.index = index;
    create();
    init();
  }

  protected void create() {
    context = rainbowGL.getCurrentContext();
    glres = new RainbowGraphicsOpenGL.GLResourceVertexBuffer(this);
  }

  protected void init() {
    int size = index ? ncoords * INIT_INDEX_BUFFER_SIZE * elementSize :
                       ncoords * INIT_VERTEX_BUFFER_SIZE * elementSize;
    rainbowGL.bindBuffer(target, glId);
    rainbowGL.bufferData(target, size, null, RainbowGL.STATIC_DRAW);
  }

  protected void dispose() {
    if (glres != null) {
      glres.dispose();
      glId = 0;
      glres = null;
    }
  }

  protected boolean contextIsOutdated() {
    boolean outdated = !rainbowGL.contextIsCurrent(context);
    if (outdated) {
      dispose();
    }
    return outdated;
  }

}
