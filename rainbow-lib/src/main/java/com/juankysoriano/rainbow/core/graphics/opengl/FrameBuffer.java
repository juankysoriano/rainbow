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

import com.juankysoriano.rainbow.utils.RainbowMath;

import java.nio.IntBuffer;

/**
 * Encapsulates a Frame Buffer Object for offscreen rendering.
 * When created with onscreen == true, it represents the normal
 * framebuffer. Needed by the stack mechanism in OPENGL2 to return
 * to onscreen rendering after a sequence of pushFramebuffer calls.
 * It transparently handles the situations when the FBO extension is
 * not available.
 * <p>
 * By Andres Colubri.
 */

public class FrameBuffer {
    protected RainbowGraphicsOpenGL graphics;
    protected RainbowGL rainbowGL;
    protected int context;   // The context that created this framebuffer.

    public int glFbo;
    public int glDepth;
    public int glStencil;
    public int glDepthStencil;
    public int glMultisample;
    public int width;
    public int height;
    private RainbowGraphicsOpenGL.GLResourceFrameBuffer glres;

    protected int depthBits;
    protected int stencilBits;
    protected boolean packedDepthStencil;

    protected boolean multisample;
    protected int nsamples;

    protected int numColorBuffers;
    protected Texture[] colorBufferTex;

    protected boolean screenFb;
    protected boolean noDepth;

    protected IntBuffer pixelBuffer;

    FrameBuffer(RainbowGraphicsOpenGL graphics) {
        this.graphics = graphics;
        rainbowGL = graphics.rainbowGl;
        context = rainbowGL.createEmptyContext();
    }

    FrameBuffer(RainbowGraphicsOpenGL graphics, int w, int h, int samples, int colorBuffers,
                int depthBits, int stencilBits, boolean packedDepthStencil,
                boolean screen) {
        this(graphics);

        glFbo = 0;
        glDepth = 0;
        glStencil = 0;
        glDepthStencil = 0;
        glMultisample = 0;

        if (screen) {
            // If this framebuffer is used to represent a on-screen buffer,
            // then it doesn't make it sense for it to have multisampling,
            // color, depth or stencil buffers.
            depthBits = stencilBits = samples = colorBuffers = 0;
        }

        width = w;
        height = h;

        if (1 < samples) {
            multisample = true;
            nsamples = samples;
        } else {
            multisample = false;
            nsamples = 1;
        }

        numColorBuffers = colorBuffers;
        colorBufferTex = new Texture[numColorBuffers];
        for (int i = 0; i < numColorBuffers; i++) {
            colorBufferTex[i] = null;
        }

        if (depthBits < 1 && stencilBits < 1) {
            this.depthBits = 0;
            this.stencilBits = 0;
            this.packedDepthStencil = false;
        } else {
            if (packedDepthStencil) {
                // When combined depth/stencil format is required, the depth and stencil
                // bits are overriden and the 24/8 combination for a 32 bits surface is
                // used.
                this.depthBits = 24;
                this.stencilBits = 8;
                this.packedDepthStencil = true;
            } else {
                this.depthBits = depthBits;
                this.stencilBits = stencilBits;
                this.packedDepthStencil = false;
            }
        }

        screenFb = screen;

        allocate();
        noDepth = false;

        pixelBuffer = null;
    }

    FrameBuffer(RainbowGraphicsOpenGL graphics, int w, int h) {
        this(graphics, w, h, 1, 1, 0, 0, false, false);
    }

    FrameBuffer(RainbowGraphicsOpenGL graphics, int w, int h, boolean screen) {
        this(graphics, w, h, 1, 1, 0, 0, false, screen);
    }

    public void clear() {
        graphics.pushFramebuffer();
        graphics.setFramebuffer(this);
        rainbowGL.clearDepth(1);
        rainbowGL.clearStencil(0);
        rainbowGL.clearColor(0, 0, 0, 0);
        rainbowGL.clear(RainbowGL.DEPTH_BUFFER_BIT |
                                RainbowGL.STENCIL_BUFFER_BIT |
                                RainbowGL.COLOR_BUFFER_BIT);
        graphics.popFramebuffer();
    }

    public void copyColor(FrameBuffer dest) {
        copy(dest, RainbowGL.COLOR_BUFFER_BIT);
    }

    public void copyDepth(FrameBuffer dest) {
        copy(dest, RainbowGL.DEPTH_BUFFER_BIT);
    }

    public void copyStencil(FrameBuffer dest) {
        copy(dest, RainbowGL.STENCIL_BUFFER_BIT);
    }

    public void copy(FrameBuffer dest, int mask) {
        rainbowGL.bindFramebufferImpl(RainbowGL.READ_FRAMEBUFFER, this.glFbo);
        rainbowGL.bindFramebufferImpl(RainbowGL.DRAW_FRAMEBUFFER, dest.glFbo);
        rainbowGL.blitFramebuffer(0, 0, this.width, this.height,
                                  0, 0, dest.width, dest.height, mask, RainbowGL.NEAREST
        );
        rainbowGL.bindFramebufferImpl(RainbowGL.READ_FRAMEBUFFER, graphics.getCurrentFB().glFbo);
        rainbowGL.bindFramebufferImpl(RainbowGL.DRAW_FRAMEBUFFER, graphics.getCurrentFB().glFbo);
    }

    public void bind() {
        rainbowGL.bindFramebufferImpl(RainbowGL.FRAMEBUFFER, glFbo);
    }

    public void disableDepthTest() {
        noDepth = true;
    }

    public void finish() {
        if (noDepth) {
            // No need to clear depth buffer because depth testing was disabled.
            rainbowGL.disable(RainbowGL.DEPTH_TEST);
        }
    }

    public void readPixels() {
        if (pixelBuffer == null) {
            createPixelBuffer();
        }
        pixelBuffer.rewind();
        rainbowGL.readPixels(0, 0, width, height, RainbowGL.RGBA, RainbowGL.UNSIGNED_BYTE,
                             pixelBuffer
        );
    }

    public void getPixels(int[] pixels) {
        if (pixelBuffer != null) {
            pixelBuffer.get(pixels, 0, pixels.length);
            pixelBuffer.rewind();
        }
    }

    public IntBuffer getPixelBuffer() {
        return pixelBuffer;
    }

    public boolean hasDepthBuffer() {
        return 0 < depthBits;
    }

    public boolean hasStencilBuffer() {
        return 0 < stencilBits;
    }

    public void setFBO(int id) {
        if (screenFb) {
            glFbo = id;
        }
    }

    ///////////////////////////////////////////////////////////

    // Color buffer setters.

    public void setColorBuffer(Texture tex) {
        setColorBuffers(new Texture[]{tex}, 1);
    }

    public void setColorBuffers(Texture[] textures) {
        setColorBuffers(textures, textures.length);
    }

    public void setColorBuffers(Texture[] textures, int n) {
        if (screenFb) {
            return;
        }

        if (numColorBuffers != RainbowMath.min(n, textures.length)) {
            throw new RuntimeException("Wrong number of textures to set the color " +
                                               "buffers.");
        }

        for (int i = 0; i < numColorBuffers; i++) {
            colorBufferTex[i] = textures[i];
        }

        graphics.pushFramebuffer();
        graphics.setFramebuffer(this);

        // Making sure nothing is attached.
        for (int i = 0; i < numColorBuffers; i++) {
            rainbowGL.framebufferTexture2D(RainbowGL.FRAMEBUFFER, RainbowGL.COLOR_ATTACHMENT0 + i,
                                           RainbowGL.TEXTURE_2D, 0, 0
            );
        }

        for (int i = 0; i < numColorBuffers; i++) {
            rainbowGL.framebufferTexture2D(RainbowGL.FRAMEBUFFER, RainbowGL.COLOR_ATTACHMENT0 + i,
                                           colorBufferTex[i].glTarget,
                                           colorBufferTex[i].glName, 0
            );
        }

        rainbowGL.validateFramebuffer();

        graphics.popFramebuffer();
    }

    public void swapColorBuffers() {
        for (int i = 0; i < numColorBuffers - 1; i++) {
            int i1 = (i + 1);
            Texture tmp = colorBufferTex[i];
            colorBufferTex[i] = colorBufferTex[i1];
            colorBufferTex[i1] = tmp;
        }

        graphics.pushFramebuffer();
        graphics.setFramebuffer(this);
        for (int i = 0; i < numColorBuffers; i++) {
            rainbowGL.framebufferTexture2D(RainbowGL.FRAMEBUFFER, RainbowGL.COLOR_ATTACHMENT0 + i,
                                           colorBufferTex[i].glTarget,
                                           colorBufferTex[i].glName, 0
            );
        }
        rainbowGL.validateFramebuffer();

        graphics.popFramebuffer();
    }

    public int getDefaultReadBuffer() {
        if (screenFb) {
            return rainbowGL.getDefaultReadBuffer();
        } else {
            return RainbowGL.COLOR_ATTACHMENT0;
        }
    }

    public int getDefaultDrawBuffer() {
        if (screenFb) {
            return rainbowGL.getDefaultDrawBuffer();
        } else {
            return RainbowGL.COLOR_ATTACHMENT0;
        }
    }

    ///////////////////////////////////////////////////////////

    // Allocate/release framebuffer.

    protected void allocate() {
        dispose(); // Just in the case this object is being re-allocated.

        context = rainbowGL.getCurrentContext();
        glres = new RainbowGraphicsOpenGL.GLResourceFrameBuffer(this); // create the FBO resources...

        if (screenFb) {
            glFbo = 0;
        } else {
            if (multisample) {
                initColorBufferMultisample();
            }

            if (packedDepthStencil) {
                initPackedDepthStencilBuffer();
            } else {
                if (0 < depthBits) {
                    initDepthBuffer();
                }
                if (0 < stencilBits) {
                    initStencilBuffer();
                }
            }
        }
    }

    protected void dispose() {
        if (screenFb) {
            return;
        }
        if (glres != null) {
            glres.dispose();
            glFbo = 0;
            glDepth = 0;
            glStencil = 0;
            glMultisample = 0;
            glDepthStencil = 0;
            glres = null;
        }
    }

    protected boolean contextIsOutdated() {
        if (screenFb) {
            return false;
        }

        boolean outdated = !rainbowGL.contextIsCurrent(context);
        if (outdated) {
            dispose();
            for (int i = 0; i < numColorBuffers; i++) {
                colorBufferTex[i] = null;
            }
        }
        return outdated;
    }

    protected void initColorBufferMultisample() {
        if (screenFb) {
            return;
        }

        graphics.pushFramebuffer();
        graphics.setFramebuffer(this);

        rainbowGL.bindRenderbuffer(RainbowGL.RENDERBUFFER, glMultisample);
        rainbowGL.renderbufferStorageMultisample(RainbowGL.RENDERBUFFER, nsamples,
                                                 RainbowGL.RGBA8, width, height
        );
        rainbowGL.framebufferRenderbuffer(RainbowGL.FRAMEBUFFER, RainbowGL.COLOR_ATTACHMENT0,
                                          RainbowGL.RENDERBUFFER, glMultisample
        );

        graphics.popFramebuffer();
    }

    protected void initPackedDepthStencilBuffer() {
        if (screenFb) {
            return;
        }

        if (width == 0 || height == 0) {
            throw new RuntimeException("PFramebuffer: size undefined.");
        }

        graphics.pushFramebuffer();
        graphics.setFramebuffer(this);

        rainbowGL.bindRenderbuffer(RainbowGL.RENDERBUFFER, glDepthStencil);

        if (multisample) {
            rainbowGL.renderbufferStorageMultisample(RainbowGL.RENDERBUFFER, nsamples,
                                                     RainbowGL.DEPTH24_STENCIL8, width, height
            );
        } else {
            rainbowGL.renderbufferStorage(RainbowGL.RENDERBUFFER, RainbowGL.DEPTH24_STENCIL8,
                                          width, height
            );
        }

        rainbowGL.framebufferRenderbuffer(RainbowGL.FRAMEBUFFER, RainbowGL.DEPTH_ATTACHMENT,
                                          RainbowGL.RENDERBUFFER, glDepthStencil
        );
        rainbowGL.framebufferRenderbuffer(RainbowGL.FRAMEBUFFER, RainbowGL.STENCIL_ATTACHMENT,
                                          RainbowGL.RENDERBUFFER, glDepthStencil
        );

        graphics.popFramebuffer();
    }

    protected void initDepthBuffer() {
        if (screenFb) {
            return;
        }

        if (width == 0 || height == 0) {
            throw new RuntimeException("PFramebuffer: size undefined.");
        }

        graphics.pushFramebuffer();
        graphics.setFramebuffer(this);

        rainbowGL.bindRenderbuffer(RainbowGL.RENDERBUFFER, glDepth);

        int glConst = RainbowGL.DEPTH_COMPONENT16;
        if (depthBits == 16) {
            glConst = RainbowGL.DEPTH_COMPONENT16;
        } else if (depthBits == 24) {
            glConst = RainbowGL.DEPTH_COMPONENT24;
        } else if (depthBits == 32) {
            glConst = RainbowGL.DEPTH_COMPONENT32;
        }

        if (multisample) {
            rainbowGL.renderbufferStorageMultisample(RainbowGL.RENDERBUFFER, nsamples, glConst,
                                                     width, height
            );
        } else {
            rainbowGL.renderbufferStorage(RainbowGL.RENDERBUFFER, glConst, width, height);
        }

        rainbowGL.framebufferRenderbuffer(RainbowGL.FRAMEBUFFER, RainbowGL.DEPTH_ATTACHMENT,
                                          RainbowGL.RENDERBUFFER, glDepth
        );

        graphics.popFramebuffer();
    }

    protected void initStencilBuffer() {
        if (screenFb) {
            return;
        }

        if (width == 0 || height == 0) {
            throw new RuntimeException("PFramebuffer: size undefined.");
        }

        graphics.pushFramebuffer();
        graphics.setFramebuffer(this);

        rainbowGL.bindRenderbuffer(RainbowGL.RENDERBUFFER, glStencil);

        int glConst = RainbowGL.STENCIL_INDEX1;
        if (stencilBits == 1) {
            glConst = RainbowGL.STENCIL_INDEX1;
        } else if (stencilBits == 4) {
            glConst = RainbowGL.STENCIL_INDEX4;
        } else if (stencilBits == 8) {
            glConst = RainbowGL.STENCIL_INDEX8;
        }
        if (multisample) {
            rainbowGL.renderbufferStorageMultisample(RainbowGL.RENDERBUFFER, nsamples, glConst,
                                                     width, height
            );
        } else {
            rainbowGL.renderbufferStorage(RainbowGL.RENDERBUFFER, glConst, width, height);
        }

        rainbowGL.framebufferRenderbuffer(RainbowGL.FRAMEBUFFER, RainbowGL.STENCIL_ATTACHMENT,
                                          RainbowGL.RENDERBUFFER, glStencil
        );

        graphics.popFramebuffer();
    }

    protected void createPixelBuffer() {
        pixelBuffer = IntBuffer.allocate(width * height);
        pixelBuffer.rewind();
    }
}
