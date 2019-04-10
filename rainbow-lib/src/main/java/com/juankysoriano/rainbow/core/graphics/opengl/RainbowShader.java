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

import com.juankysoriano.rainbow.core.graphics.RainbowGraphics;
import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.juankysoriano.rainbow.core.matrix.RMatrix2D;
import com.juankysoriano.rainbow.core.matrix.RMatrix3D;
import com.juankysoriano.rainbow.core.matrix.RVector;
import com.juankysoriano.rainbow.utils.RainbowMath;

import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

/**
 * This class encapsulates a GLSL shader program, including a vertex
 * and a fragment shader. Based on the GLSLShader class from GLGraphics, which
 * in turn was originally based in the code by JohnG:
 * http://processing.org/discourse/beta/num_1159494801.html
 *
 * @webref rendering:shaders
 */
public class RainbowShader {
    static protected final int POINT = 0;
    static protected final int LINE = 1;
    static protected final int POLY = 2;
    static protected final int COLOR = 3;
    static protected final int LIGHT = 4;
    static protected final int TEXTURE = 5;
    static protected final int TEXLIGHT = 6;

    static protected String pointShaderAttrRegexp =
            "attribute *vec2 *offset";
    static protected String pointShaderInRegexp =
            "in *vec2 *offset;";
    static protected String lineShaderAttrRegexp =
            "attribute *vec4 *direction";
    static protected String lineShaderInRegexp =
            "in *vec4 *direction";
    static protected String pointShaderDefRegexp =
            "#define *PROCESSING_POINT_SHADER";
    static protected String lineShaderDefRegexp =
            "#define *PROCESSING_LINE_SHADER";
    static protected String colorShaderDefRegexp =
            "#define *PROCESSING_COLOR_SHADER";
    static protected String lightShaderDefRegexp =
            "#define *PROCESSING_LIGHT_SHADER";
    static protected String texShaderDefRegexp =
            "#define *PROCESSING_TEXTURE_SHADER";
    static protected String texlightShaderDefRegexp =
            "#define *PROCESSING_TEXLIGHT_SHADER";
    static protected String polyShaderDefRegexp =
            "#define *PROCESSING_POLYGON_SHADER";
    static protected String triShaderAttrRegexp =
            "#define *PROCESSING_TRIANGLES_SHADER";
    static protected String quadShaderAttrRegexp =
            "#define *PROCESSING_QUADS_SHADER";

    protected RainbowGraphics graphics;
    // The main renderer associated to the graphics RainbowMath.
    //protected RainbowGraphicsOpenGL pgMain;
    // We need a reference to the renderer since a shader might
    // be called by different renderers within a single application
    // (the one corresponding to the main surface, or other offscreen
    // renderers).
    protected RainbowGraphicsOpenGL primaryPG;
    protected RainbowGraphicsOpenGL currentPG;
    protected RainbowGL rainbowGL;
    protected int context;      // The context that created this shader.

    // The shader type: POINT, LINE, POLY, etc.
    protected int type;

    public int glProgram;
    public int glVertex;
    public int glFragment;
    private RainbowGraphicsOpenGL.GLResourceShader glres;

    protected URL vertexURL;
    protected URL fragmentURL;

    protected String vertexFilename;
    protected String fragmentFilename;

    protected String[] vertexShaderSource;
    protected String[] fragmentShaderSource;

    protected boolean bound;

    protected HashMap<Integer, UniformValue> uniformValues = null;

    protected HashMap<Integer, Texture> textures;
    protected HashMap<Integer, Integer> texUnits;

    // Direct buffers to pass shader data to GL
    protected IntBuffer intBuffer;
    protected FloatBuffer floatBuffer;

    protected boolean loadedAttributes = false;
    protected boolean loadedUniforms = false;

    // Uniforms common to all shader types
    protected int transformMatLoc;
    protected int modelviewMatLoc;
    protected int projectionMatLoc;
    protected int ppixelsLoc;
    protected int ppixelsUnit;
    protected int viewportLoc;
    protected int resolutionLoc;

    // Uniforms only for lines and points
    protected int perspectiveLoc;
    protected int scaleLoc;

    // Lighting uniforms
    protected int lightCountLoc;
    protected int lightPositionLoc;
    protected int lightNormalLoc;
    protected int lightAmbientLoc;
    protected int lightDiffuseLoc;
    protected int lightSpecularLoc;
    protected int lightFalloffLoc;
    protected int lightSpotLoc;

    // Texturing uniforms
    protected Texture texture;
    protected int texUnit;
    protected int textureLoc;
    protected int texMatrixLoc;
    protected int texOffsetLoc;
    protected float[] tcmat;

    // Vertex attributes
    protected int vertexLoc;
    protected int colorLoc;
    protected int normalLoc;
    protected int texCoordLoc;
    protected int normalMatLoc;
    protected int directionLoc;
    protected int offsetLoc;
    protected int ambientLoc;
    protected int specularLoc;
    protected int emissiveLoc;
    protected int shininessLoc;

    public RainbowShader() {
        graphics = null;
        rainbowGL = null;
        context = -1;

        this.vertexURL = null;
        this.fragmentURL = null;
        this.vertexFilename = null;
        this.fragmentFilename = null;

        glProgram = 0;
        glVertex = 0;
        glFragment = 0;

        intBuffer = RainbowGL.allocateIntBuffer(1);
        floatBuffer = RainbowGL.allocateFloatBuffer(1);

        bound = false;

        type = -1;
    }

    public RainbowShader(RainbowGraphics graphics) {
        this();
        this.graphics = graphics;
        primaryPG = (RainbowGraphicsOpenGL) graphics;
        rainbowGL = primaryPG.rainbowGl;
        context = rainbowGL.createEmptyContext();
    }

    /**
     * Creates a shader program using the specified vertex and fragment
     * shaders.
     *
     * @param graphics     the graphics program
     * @param vertFilename name of the vertex shader
     * @param fragFilename name of the fragment shader
     */
    public RainbowShader(RainbowGraphics graphics, String vertFilename, String fragFilename) {
        this.graphics = graphics;
        primaryPG = (RainbowGraphicsOpenGL) graphics;
        rainbowGL = primaryPG.rainbowGl;

        this.vertexURL = null;
        this.fragmentURL = null;
        this.vertexFilename = vertFilename;
        this.fragmentFilename = fragFilename;
        fragmentShaderSource = rainbowGL.loadFragmentShader(fragFilename);
        vertexShaderSource = rainbowGL.loadVertexShader(vertFilename);

        glProgram = 0;
        glVertex = 0;
        glFragment = 0;

        intBuffer = RainbowGL.allocateIntBuffer(1);
        floatBuffer = RainbowGL.allocateFloatBuffer(1);

        int vertType = getShaderType(vertexShaderSource, -1);
        int fragType = getShaderType(fragmentShaderSource, -1);
        if (vertType == -1 && fragType == -1) {
            type = RainbowShader.POLY;
        } else if (vertType == -1) {
            type = fragType;
        } else if (fragType == -1) {
            type = vertType;
        } else if (fragType == vertType) {
            type = vertType;
        } else {
            RainbowGraphics.showWarning(RainbowGraphicsOpenGL.INCONSISTENT_SHADER_TYPES);
        }
    }

    /**
     * @param vertURL network location of the vertex shader
     * @param fragURL network location of the fragment shader
     */
    public RainbowShader(RainbowGraphics graphics, URL vertURL, URL fragURL) {
        this.graphics = graphics;
        primaryPG = (RainbowGraphicsOpenGL) graphics;
        rainbowGL = primaryPG.rainbowGl;

        this.vertexURL = vertURL;
        this.fragmentURL = fragURL;
        this.vertexFilename = null;
        this.fragmentFilename = null;
        fragmentShaderSource = rainbowGL.loadFragmentShader(fragURL);
        vertexShaderSource = rainbowGL.loadVertexShader(vertURL);

        glProgram = 0;
        glVertex = 0;
        glFragment = 0;

        intBuffer = RainbowGL.allocateIntBuffer(1);
        floatBuffer = RainbowGL.allocateFloatBuffer(1);

        int vertType = getShaderType(vertexShaderSource, -1);
        int fragType = getShaderType(fragmentShaderSource, -1);
        if (vertType == -1 && fragType == -1) {
            type = RainbowShader.POLY;
        } else if (vertType == -1) {
            type = fragType;
        } else if (fragType == -1) {
            type = vertType;
        } else if (fragType == vertType) {
            type = vertType;
        } else {
            RainbowGraphics.showWarning(RainbowGraphicsOpenGL.INCONSISTENT_SHADER_TYPES);
        }
    }

    public RainbowShader(RainbowGraphics graphics, String[] vertSource, String[] fragSource) {
        this.graphics = graphics;
        primaryPG = (RainbowGraphicsOpenGL) graphics;
        rainbowGL = primaryPG.rainbowGl;

        this.vertexURL = null;
        this.fragmentURL = null;
        this.vertexFilename = null;
        this.fragmentFilename = null;
        vertexShaderSource = vertSource;
        fragmentShaderSource = fragSource;

        glProgram = 0;
        glVertex = 0;
        glFragment = 0;

        intBuffer = RainbowGL.allocateIntBuffer(1);
        floatBuffer = RainbowGL.allocateFloatBuffer(1);

        int vertType = getShaderType(vertexShaderSource, -1);
        int fragType = getShaderType(fragmentShaderSource, -1);
        if (vertType == -1 && fragType == -1) {
            type = RainbowShader.POLY;
        } else if (vertType == -1) {
            type = fragType;
        } else if (fragType == -1) {
            type = vertType;
        } else if (fragType == vertType) {
            type = vertType;
        } else {
            RainbowGraphics.showWarning(RainbowGraphicsOpenGL.INCONSISTENT_SHADER_TYPES);
        }
    }

    public void setVertexShader(String vertFilename) {
        this.vertexFilename = vertFilename;
        vertexShaderSource = rainbowGL.loadVertexShader(vertFilename);
    }

    public void setVertexShader(URL vertURL) {
        this.vertexURL = vertURL;
        vertexShaderSource = rainbowGL.loadVertexShader(vertURL);
    }

    public void setVertexShader(String[] vertSource) {
        vertexShaderSource = vertSource;
    }

    public void setFragmentShader(String fragFilename) {
        this.fragmentFilename = fragFilename;
        fragmentShaderSource = rainbowGL.loadFragmentShader(fragFilename);
    }

    public void setFragmentShader(URL fragURL) {
        this.fragmentURL = fragURL;
        fragmentShaderSource = rainbowGL.loadFragmentShader(fragURL);
    }

    public void setFragmentShader(String[] fragSource) {
        fragmentShaderSource = fragSource;
    }

    /**
     * Initializes (if needed) and binds the shader program.
     */
    public void bind() {
        init();
        if (!bound) {
            rainbowGL.useProgram(glProgram);
            bound = true;
            consumeUniforms();
            bindTextures();
        }

        if (hasType()) {
            bindTyped();
        }
    }

    /**
     * Unbinds the shader program.
     */
    public void unbind() {
        if (hasType()) {
            unbindTyped();
        }

        if (bound) {
            unbindTextures();
            rainbowGL.useProgram(0);
            bound = false;
        }
    }

    /**
     * Returns true if the shader is bound, false otherwise.
     */
    public boolean bound() {
        return bound;
    }

    /**
     * @param name the name of the uniform variable to modify
     * @param x    first component of the variable to modify
     * @webref rendering:shaders
     * @brief Sets a variable within the shader
     */
    public void set(String name, int x) {
        setUniformImpl(name, UniformValue.INT1, new int[]{x});
    }

    /**
     * @param y second component of the variable to modify. The variable has to be declared with an array/vector type in the shader (i.e.: int[2], vec2)
     */
    public void set(String name, int x, int y) {
        setUniformImpl(name, UniformValue.INT2, new int[]{x, y});
    }

    /**
     * @param z third component of the variable to modify. The variable has to be declared with an array/vector type in the shader (i.e.: int[3], vec3)
     */
    public void set(String name, int x, int y, int z) {
        setUniformImpl(name, UniformValue.INT3, new int[]{x, y, z});
    }

    /**
     * @param w fourth component of the variable to modify. The variable has to be declared with an array/vector type in the shader (i.e.: int[4], vec4)
     */
    public void set(String name, int x, int y, int z, int w) {
        setUniformImpl(name, UniformValue.INT4, new int[]{x, y, z, w});
    }

    public void set(String name, float x) {
        setUniformImpl(name, UniformValue.FLOAT1, new float[]{x});
    }

    public void set(String name, float x, float y) {
        setUniformImpl(name, UniformValue.FLOAT2, new float[]{x, y});
    }

    public void set(String name, float x, float y, float z) {
        setUniformImpl(name, UniformValue.FLOAT3, new float[]{x, y, z});
    }

    public void set(String name, float x, float y, float z, float w) {
        setUniformImpl(name, UniformValue.FLOAT4, new float[]{x, y, z, w});
    }

    /**
     * @param vec modifies all the components of an array/vector uniform variable. PVector can only be used if the type of the variable is vec3.
     */
    public void set(String name, RVector vec) {
        setUniformImpl(name, UniformValue.FLOAT3,
                       new float[]{vec.x, vec.y, vec.z}
        );
    }

    public void set(String name, boolean x) {
        setUniformImpl(name, UniformValue.INT1, new int[]{(x) ? 1 : 0});
    }

    public void set(String name, boolean x, boolean y) {
        setUniformImpl(name, UniformValue.INT2,
                       new int[]{(x) ? 1 : 0, (y) ? 1 : 0}
        );
    }

    public void set(String name, boolean x, boolean y, boolean z) {
        setUniformImpl(name, UniformValue.INT3,
                       new int[]{(x) ? 1 : 0, (y) ? 1 : 0, (z) ? 1 : 0}
        );
    }

    public void set(String name, boolean x, boolean y, boolean z, boolean w) {
        setUniformImpl(name, UniformValue.INT4,
                       new int[]{(x) ? 1 : 0, (y) ? 1 : 0, (z) ? 1 : 0, (w) ? 1 : 0}
        );
    }

    public void set(String name, int[] vec) {
        set(name, vec, 1);
    }

    /**
     * @param ncoords number of coordinates per element, max 4
     */
    public void set(String name, int[] vec, int ncoords) {
        if (ncoords == 1) {
            setUniformImpl(name, UniformValue.INT1VEC, vec);
        } else if (ncoords == 2) {
            setUniformImpl(name, UniformValue.INT2VEC, vec);
        } else if (ncoords == 3) {
            setUniformImpl(name, UniformValue.INT3VEC, vec);
        } else if (ncoords == 4) {
            setUniformImpl(name, UniformValue.INT4VEC, vec);
        } else if (4 < ncoords) {
            RainbowGraphics.showWarning("Only up to 4 coordinates per element are " +
                                                "supported.");
        } else {
            RainbowGraphics.showWarning("Wrong number of coordinates: it is negative!");
        }
    }

    public void set(String name, float[] vec) {
        set(name, vec, 1);
    }

    public void set(String name, float[] vec, int ncoords) {
        if (ncoords == 1) {
            setUniformImpl(name, UniformValue.FLOAT1VEC, vec);
        } else if (ncoords == 2) {
            setUniformImpl(name, UniformValue.FLOAT2VEC, vec);
        } else if (ncoords == 3) {
            setUniformImpl(name, UniformValue.FLOAT3VEC, vec);
        } else if (ncoords == 4) {
            setUniformImpl(name, UniformValue.FLOAT4VEC, vec);
        } else if (4 < ncoords) {
            RainbowGraphics.showWarning("Only up to 4 coordinates per element are " +
                                                "supported.");
        } else {
            RainbowGraphics.showWarning("Wrong number of coordinates: it is negative!");
        }
    }

    public void set(String name, boolean[] vec) {
        set(name, vec, 1);
    }

    public void set(String name, boolean[] boolvec, int ncoords) {
        int[] vec = new int[boolvec.length];
        for (int i = 0; i < boolvec.length; i++) {
            vec[i] = (boolvec[i]) ? 1 : 0;
        }
        set(name, vec, ncoords);
    }

    /**
     * @param mat matrix of values
     */
    public void set(String name, RMatrix2D mat) {
        float[] matv = {mat.m00, mat.m01,
                mat.m10, mat.m11};
        setUniformImpl(name, UniformValue.MAT2, matv);
    }

    public void set(String name, RMatrix3D mat) {
        set(name, mat, false);
    }

    /**
     * @param use3x3 enforces the matrix is 3 x 3
     */
    public void set(String name, RMatrix3D mat, boolean use3x3) {
        if (use3x3) {
            float[] matv = {mat.m00, mat.m01, mat.m02,
                    mat.m10, mat.m11, mat.m12,
                    mat.m20, mat.m21, mat.m22};
            setUniformImpl(name, UniformValue.MAT3, matv);
        } else {
            float[] matv = {mat.m00, mat.m01, mat.m02, mat.m03,
                    mat.m10, mat.m11, mat.m12, mat.m13,
                    mat.m20, mat.m21, mat.m22, mat.m23,
                    mat.m30, mat.m31, mat.m32, mat.m33};
            setUniformImpl(name, UniformValue.MAT4, matv);
        }
    }

    /**
     * @param tex sets the sampler uniform variable to read from this image texture
     */
    public void set(String name, RainbowImage tex) {
        setUniformImpl(name, UniformValue.SAMPLER2D, tex);
    }

    /**
     * Extra initialization method that can be used by subclasses, called after
     * compiling and attaching the vertex and fragment shaders, and before
     * linking the shader program.
     */
    protected void setup() {
    }

    protected void draw(int idxId, int count, int offset) {
        rainbowGL.bindBuffer(RainbowGL.ELEMENT_ARRAY_BUFFER, idxId);
        rainbowGL.drawElements(RainbowGL.TRIANGLES, count, RainbowGL.INDEX_TYPE,
                               offset * RainbowGL.SIZEOF_INDEX
        );
        rainbowGL.bindBuffer(RainbowGL.ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Returns the ID location of the attribute parameter given its name.
     *
     * @param name String
     * @return int
     */
    protected int getAttributeLoc(String name) {
        init();
        return rainbowGL.getAttribLocation(glProgram, name);
    }

    /**
     * Returns the ID location of the uniform parameter given its name.
     *
     * @param name String
     * @return int
     */
    protected int getUniformLoc(String name) {
        init();
        return rainbowGL.getUniformLocation(glProgram, name);
    }

    protected void setAttributeVBO(int loc, int vboId, int size, int type,
                                   boolean normalized, int stride, int offset) {
        if (-1 < loc) {
            rainbowGL.bindBuffer(RainbowGL.ARRAY_BUFFER, vboId);
            rainbowGL.vertexAttribPointer(loc, size, type, normalized, stride, offset);
        }
    }

    protected void setUniformValue(int loc, int x) {
        if (-1 < loc) {
            rainbowGL.uniform1i(loc, x);
        }
    }

    protected void setUniformValue(int loc, int x, int y) {
        if (-1 < loc) {
            rainbowGL.uniform2i(loc, x, y);
        }
    }

    protected void setUniformValue(int loc, int x, int y, int z) {
        if (-1 < loc) {
            rainbowGL.uniform3i(loc, x, y, z);
        }
    }

    protected void setUniformValue(int loc, int x, int y, int z, int w) {
        if (-1 < loc) {
            rainbowGL.uniform4i(loc, x, y, z, w);
        }
    }

    protected void setUniformValue(int loc, float x) {
        if (-1 < loc) {
            rainbowGL.uniform1f(loc, x);
        }
    }

    protected void setUniformValue(int loc, float x, float y) {
        if (-1 < loc) {
            rainbowGL.uniform2f(loc, x, y);
        }
    }

    protected void setUniformValue(int loc, float x, float y, float z) {
        if (-1 < loc) {
            rainbowGL.uniform3f(loc, x, y, z);
        }
    }

    protected void setUniformValue(int loc, float x, float y, float z, float w) {
        if (-1 < loc) {
            rainbowGL.uniform4f(loc, x, y, z, w);
        }
    }

    protected void setUniformVector(int loc, int[] vec, int ncoords,
                                    int length) {
        if (-1 < loc) {
            updateIntBuffer(vec);
            if (ncoords == 1) {
                rainbowGL.uniform1iv(loc, length, intBuffer);
            } else if (ncoords == 2) {
                rainbowGL.uniform2iv(loc, length, intBuffer);
            } else if (ncoords == 3) {
                rainbowGL.uniform3iv(loc, length, intBuffer);
            } else if (ncoords == 4) {
                rainbowGL.uniform3iv(loc, length, intBuffer);
            }
        }
    }

    protected void setUniformVector(int loc, float[] vec, int ncoords,
                                    int length) {
        if (-1 < loc) {
            updateFloatBuffer(vec);
            if (ncoords == 1) {
                rainbowGL.uniform1fv(loc, length, floatBuffer);
            } else if (ncoords == 2) {
                rainbowGL.uniform2fv(loc, length, floatBuffer);
            } else if (ncoords == 3) {
                rainbowGL.uniform3fv(loc, length, floatBuffer);
            } else if (ncoords == 4) {
                rainbowGL.uniform4fv(loc, length, floatBuffer);
            }
        }
    }

    protected void setUniformMatrix(int loc, float[] mat) {
        if (-1 < loc) {
            updateFloatBuffer(mat);
            if (mat.length == 4) {
                rainbowGL.uniformMatrix2fv(loc, 1, false, floatBuffer);
            } else if (mat.length == 9) {
                rainbowGL.uniformMatrix3fv(loc, 1, false, floatBuffer);
            } else if (mat.length == 16) {
                rainbowGL.uniformMatrix4fv(loc, 1, false, floatBuffer);
            }
        }
    }

    protected void setUniformTex(int loc, Texture tex) {
        if (texUnits != null) {
            Integer unit = texUnits.get(loc);
            if (unit != null) {
                rainbowGL.activeTexture(RainbowGL.TEXTURE0 + unit);
                tex.bind();
            } else {
                throw new RuntimeException("Cannot find unit for texture " + tex);
            }
        }
    }

    protected void setUniformImpl(String name, int type, Object value) {
        int loc = getUniformLoc(name);
        if (-1 < loc) {
            if (uniformValues == null) {
                uniformValues = new HashMap<Integer, UniformValue>();
            }
            uniformValues.put(loc, new UniformValue(type, value));
        } else {
            RainbowGraphics.showWarning("The shader doesn't have a uniform called \"" +
                                                name + "\" OR the uniform was removed during " +
                                                "compilation because it was unused.");
        }
    }

    protected void consumeUniforms() {
        if (uniformValues != null && 0 < uniformValues.size()) {
            int unit = 0;
            for (Integer loc : uniformValues.keySet()) {
                UniformValue val = uniformValues.get(loc);
                if (val.type == UniformValue.INT1) {
                    int[] v = ((int[]) val.value);
                    rainbowGL.uniform1i(loc, v[0]);
                } else if (val.type == UniformValue.INT2) {
                    int[] v = ((int[]) val.value);
                    rainbowGL.uniform2i(loc, v[0], v[1]);
                } else if (val.type == UniformValue.INT3) {
                    int[] v = ((int[]) val.value);
                    rainbowGL.uniform3i(loc, v[0], v[1], v[2]);
                } else if (val.type == UniformValue.INT4) {
                    int[] v = ((int[]) val.value);
                    rainbowGL.uniform4i(loc, v[0], v[1], v[2], v[3]);
                } else if (val.type == UniformValue.FLOAT1) {
                    float[] v = ((float[]) val.value);
                    rainbowGL.uniform1f(loc, v[0]);
                } else if (val.type == UniformValue.FLOAT2) {
                    float[] v = ((float[]) val.value);
                    rainbowGL.uniform2f(loc, v[0], v[1]);
                } else if (val.type == UniformValue.FLOAT3) {
                    float[] v = ((float[]) val.value);
                    rainbowGL.uniform3f(loc, v[0], v[1], v[2]);
                } else if (val.type == UniformValue.FLOAT4) {
                    float[] v = ((float[]) val.value);
                    rainbowGL.uniform4f(loc, v[0], v[1], v[2], v[3]);
                } else if (val.type == UniformValue.INT1VEC) {
                    int[] v = ((int[]) val.value);
                    updateIntBuffer(v);
                    rainbowGL.uniform1iv(loc, v.length, intBuffer);
                } else if (val.type == UniformValue.INT2VEC) {
                    int[] v = ((int[]) val.value);
                    updateIntBuffer(v);
                    rainbowGL.uniform2iv(loc, v.length / 2, intBuffer);
                } else if (val.type == UniformValue.INT3VEC) {
                    int[] v = ((int[]) val.value);
                    updateIntBuffer(v);
                    rainbowGL.uniform3iv(loc, v.length / 3, intBuffer);
                } else if (val.type == UniformValue.INT4VEC) {
                    int[] v = ((int[]) val.value);
                    updateIntBuffer(v);
                    rainbowGL.uniform4iv(loc, v.length / 4, intBuffer);
                } else if (val.type == UniformValue.FLOAT1VEC) {
                    float[] v = ((float[]) val.value);
                    updateFloatBuffer(v);
                    rainbowGL.uniform1fv(loc, v.length, floatBuffer);
                } else if (val.type == UniformValue.FLOAT2VEC) {
                    float[] v = ((float[]) val.value);
                    updateFloatBuffer(v);
                    rainbowGL.uniform2fv(loc, v.length / 2, floatBuffer);
                } else if (val.type == UniformValue.FLOAT3VEC) {
                    float[] v = ((float[]) val.value);
                    updateFloatBuffer(v);
                    rainbowGL.uniform3fv(loc, v.length / 3, floatBuffer);
                } else if (val.type == UniformValue.FLOAT4VEC) {
                    float[] v = ((float[]) val.value);
                    updateFloatBuffer(v);
                    rainbowGL.uniform4fv(loc, v.length / 4, floatBuffer);
                } else if (val.type == UniformValue.MAT2) {
                    float[] v = ((float[]) val.value);
                    updateFloatBuffer(v);
                    rainbowGL.uniformMatrix2fv(loc, 1, false, floatBuffer);
                } else if (val.type == UniformValue.MAT3) {
                    float[] v = ((float[]) val.value);
                    updateFloatBuffer(v);
                    rainbowGL.uniformMatrix3fv(loc, 1, false, floatBuffer);
                } else if (val.type == UniformValue.MAT4) {
                    float[] v = ((float[]) val.value);
                    updateFloatBuffer(v);
                    rainbowGL.uniformMatrix4fv(loc, 1, false, floatBuffer);
                } else if (val.type == UniformValue.SAMPLER2D) {
                    RainbowImage img = (RainbowImage) val.value;
                    Texture tex = currentPG.getTexture(img);

                    if (textures == null) {
                        textures = new HashMap<Integer, Texture>();
                    }
                    textures.put(loc, tex);

                    if (texUnits == null) {
                        texUnits = new HashMap<Integer, Integer>();
                    }
                    if (texUnits.containsKey(loc)) {
                        unit = texUnits.get(loc);
                        rainbowGL.uniform1i(loc, unit);
                    } else {
                        texUnits.put(loc, unit);
                        rainbowGL.uniform1i(loc, unit);
                    }
                    unit++;
                }
            }
            uniformValues.clear();
        }
    }

    protected void updateIntBuffer(int[] vec) {
        intBuffer = RainbowGL.updateIntBuffer(intBuffer, vec, false);
    }

    protected void updateFloatBuffer(float[] vec) {
        floatBuffer = RainbowGL.updateFloatBuffer(floatBuffer, vec, false);
    }

    protected void bindTextures() {
        if (textures != null && texUnits != null) {
            for (int loc : textures.keySet()) {
                Texture tex = textures.get(loc);
                Integer unit = texUnits.get(loc);
                if (unit != null) {
                    rainbowGL.activeTexture(RainbowGL.TEXTURE0 + unit);
                    tex.bind();
                } else {
                    throw new RuntimeException("Cannot find unit for texture " + tex);
                }
            }
        }
    }

    protected void unbindTextures() {
        if (textures != null && texUnits != null) {
            for (int loc : textures.keySet()) {
                Texture tex = textures.get(loc);
                Integer unit = texUnits.get(loc);
                if (unit != null) {
                    rainbowGL.activeTexture(RainbowGL.TEXTURE0 + unit);
                    tex.unbind();
                } else {
                    throw new RuntimeException("Cannot find unit for texture " + tex);
                }
            }
            rainbowGL.activeTexture(RainbowGL.TEXTURE0);
        }
    }

    public void init() {
        if (glProgram == 0 || contextIsOutdated()) {
            create();
            if (compile()) {
                rainbowGL.attachShader(glProgram, glVertex);
                rainbowGL.attachShader(glProgram, glFragment);

                setup();

                rainbowGL.linkProgram(glProgram);

                validate();
            }
        }
    }

    protected void create() {
        context = rainbowGL.getCurrentContext();
        glres = new RainbowGraphicsOpenGL.GLResourceShader(this);
    }

    protected boolean compile() {
        boolean vertRes = true;
        if (hasVertexShader()) {
            vertRes = compileVertexShader();
        } else {
            RainbowGraphics.showException("Doesn't have a vertex shader");
        }

        boolean fragRes = true;
        if (hasFragmentShader()) {
            fragRes = compileFragmentShader();
        } else {
            RainbowGraphics.showException("Doesn't have a fragment shader");
        }

        return vertRes && fragRes;
    }

    protected void validate() {
        rainbowGL.getProgramiv(glProgram, RainbowGL.LINK_STATUS, intBuffer);
        boolean linked = intBuffer.get(0) == 0 ? false : true;
        if (!linked) {
            RainbowGraphics.showException("Cannot link shader program:\n" +
                                                  rainbowGL.getProgramInfoLog(glProgram));
        }

        rainbowGL.validateProgram(glProgram);
        rainbowGL.getProgramiv(glProgram, RainbowGL.VALIDATE_STATUS, intBuffer);
        boolean validated = intBuffer.get(0) == 0 ? false : true;
        if (!validated) {
            RainbowGraphics.showException("Cannot validate shader program:\n" +
                                                  rainbowGL.getProgramInfoLog(glProgram));
        }
    }

    protected boolean contextIsOutdated() {
        boolean outdated = !rainbowGL.contextIsCurrent(context);
        if (outdated) {
            dispose();
        }
        return outdated;
    }

    protected boolean hasVertexShader() {
        return vertexShaderSource != null && 0 < vertexShaderSource.length;
    }

    protected boolean hasFragmentShader() {
        return fragmentShaderSource != null && 0 < fragmentShaderSource.length;
    }

    protected boolean compileVertexShader() {
        rainbowGL.shaderSource(glVertex, RainbowMath.join(vertexShaderSource, "\n"));
        rainbowGL.compileShader(glVertex);

        rainbowGL.getShaderiv(glVertex, RainbowGL.COMPILE_STATUS, intBuffer);
        boolean compiled = intBuffer.get(0) == 0 ? false : true;
        if (!compiled) {
            RainbowGraphics.showException("Cannot compile vertex shader:\n" +
                                                  rainbowGL.getShaderInfoLog(glVertex));
            return false;
        } else {
            return true;
        }
    }

    protected boolean compileFragmentShader() {
        rainbowGL.shaderSource(glFragment, RainbowMath.join(fragmentShaderSource, "\n"));
        rainbowGL.compileShader(glFragment);

        rainbowGL.getShaderiv(glFragment, RainbowGL.COMPILE_STATUS, intBuffer);
        boolean compiled = intBuffer.get(0) == 0 ? false : true;
        if (!compiled) {
            RainbowGraphics.showException("Cannot compile fragment shader:\n" +
                                                  rainbowGL.getShaderInfoLog(glFragment));
            return false;
        } else {
            return true;
        }
    }

    protected void dispose() {
        if (glres != null) {
            glres.dispose();
            glVertex = 0;
            glFragment = 0;
            glProgram = 0;
            glres = null;
        }
    }

    static protected int getShaderType(String[] source, int defaultType) {
        for (int i = 0; i < source.length; i++) {
            String line = source[i].trim();

            if (RainbowMath.match(line, colorShaderDefRegexp) != null) {
                return RainbowShader.COLOR;
            } else if (RainbowMath.match(line, lightShaderDefRegexp) != null) {
                return RainbowShader.LIGHT;
            } else if (RainbowMath.match(line, texShaderDefRegexp) != null) {
                return RainbowShader.TEXTURE;
            } else if (RainbowMath.match(line, texlightShaderDefRegexp) != null) {
                return RainbowShader.TEXLIGHT;
            } else if (RainbowMath.match(line, polyShaderDefRegexp) != null) {
                return RainbowShader.POLY;
            } else if (RainbowMath.match(line, triShaderAttrRegexp) != null) {
                return RainbowShader.POLY;
            } else if (RainbowMath.match(line, quadShaderAttrRegexp) != null) {
                return RainbowShader.POLY;
            } else if (RainbowMath.match(line, pointShaderDefRegexp) != null) {
                return RainbowShader.POINT;
            } else if (RainbowMath.match(line, lineShaderDefRegexp) != null) {
                return RainbowShader.LINE;
            } else if (RainbowMath.match(line, pointShaderAttrRegexp) != null) {
                return RainbowShader.POINT;
            } else if (RainbowMath.match(line, pointShaderInRegexp) != null) {
                return RainbowShader.POINT;
            } else if (RainbowMath.match(line, lineShaderAttrRegexp) != null) {
                return RainbowShader.LINE;
            } else if (RainbowMath.match(line, lineShaderInRegexp) != null) {
                return RainbowShader.LINE;
            }
        }
        return defaultType;
    }

    // ***************************************************************************
    //
    // Processing specific

    protected int getType() {
        return type;
    }

    protected void setType(int type) {
        this.type = type;
    }

    protected boolean hasType() {
        return POINT <= type && type <= TEXLIGHT;
    }

    protected boolean isPointShader() {
        return type == POINT;
    }

    protected boolean isLineShader() {
        return type == LINE;
    }

    protected boolean isPolyShader() {
        return POLY <= type && type <= TEXLIGHT;
    }

    protected boolean checkPolyType(int type) {
        if (getType() == RainbowShader.POLY) {
            return true;
        }

        if (getType() != type) {
            if (type == TEXLIGHT) {
                RainbowGraphics.showWarning(RainbowGraphicsOpenGL.NO_TEXLIGHT_SHADER_ERROR);
            } else if (type == LIGHT) {
                RainbowGraphics.showWarning(RainbowGraphicsOpenGL.NO_LIGHT_SHADER_ERROR);
            } else if (type == TEXTURE) {
                RainbowGraphics.showWarning(RainbowGraphicsOpenGL.NO_TEXTURE_SHADER_ERROR);
            } else if (type == COLOR) {
                RainbowGraphics.showWarning(RainbowGraphicsOpenGL.NO_COLOR_SHADER_ERROR);
            }
            return false;
        }

        return true;
    }

    protected int getLastTexUnit() {
        return texUnits == null ? -1 : texUnits.size() - 1;
    }

    protected void setRenderer(RainbowGraphicsOpenGL pg) {
        this.currentPG = pg;
    }

    protected void loadAttributes() {
        if (loadedAttributes) {
            return;
        }

        vertexLoc = getAttributeLoc("vertex");
        if (vertexLoc == -1) {
            vertexLoc = getAttributeLoc("position");
        }

        colorLoc = getAttributeLoc("color");
        texCoordLoc = getAttributeLoc("texCoord");
        normalLoc = getAttributeLoc("normal");

        ambientLoc = getAttributeLoc("ambient");
        specularLoc = getAttributeLoc("specular");
        emissiveLoc = getAttributeLoc("emissive");
        shininessLoc = getAttributeLoc("shininess");

        directionLoc = getAttributeLoc("direction");

        offsetLoc = getAttributeLoc("offset");

        directionLoc = getAttributeLoc("direction");
        offsetLoc = getAttributeLoc("offset");

        loadedAttributes = true;
    }

    protected void loadUniforms() {
        if (loadedUniforms) {
            return;
        }
        transformMatLoc = getUniformLoc("transform");
        if (transformMatLoc == -1) {
            transformMatLoc = getUniformLoc("transformMatrix");
        }

        modelviewMatLoc = getUniformLoc("modelview");
        if (modelviewMatLoc == -1) {
            modelviewMatLoc = getUniformLoc("modelviewMatrix");
        }

        projectionMatLoc = getUniformLoc("projection");
        if (projectionMatLoc == -1) {
            projectionMatLoc = getUniformLoc("projectionMatrix");
        }

        viewportLoc = getUniformLoc("viewport");
        resolutionLoc = getUniformLoc("resolution");
        ppixelsLoc = getUniformLoc("ppixels");

        normalMatLoc = getUniformLoc("normalMatrix");

        lightCountLoc = getUniformLoc("lightCount");
        lightPositionLoc = getUniformLoc("lightPosition");
        lightNormalLoc = getUniformLoc("lightNormal");
        lightAmbientLoc = getUniformLoc("lightAmbient");
        lightDiffuseLoc = getUniformLoc("lightDiffuse");
        lightSpecularLoc = getUniformLoc("lightSpecular");
        lightFalloffLoc = getUniformLoc("lightFalloff");
        lightSpotLoc = getUniformLoc("lightSpot");

        textureLoc = getUniformLoc("texture");
        if (textureLoc == -1) {
            textureLoc = getUniformLoc("texMap");
        }

        texMatrixLoc = getUniformLoc("texMatrix");
        texOffsetLoc = getUniformLoc("texOffset");

        perspectiveLoc = getUniformLoc("perspective");
        scaleLoc = getUniformLoc("scale");
        loadedUniforms = true;
    }

    protected void setCommonUniforms() {
        if (-1 < transformMatLoc) {
            currentPG.updateGLProjmodelview();
            setUniformMatrix(transformMatLoc, currentPG.glProjmodelview);
        }

        if (-1 < modelviewMatLoc) {
            currentPG.updateGLModelview();
            setUniformMatrix(modelviewMatLoc, currentPG.glModelview);
        }

        if (-1 < projectionMatLoc) {
            currentPG.updateGLProjection();
            setUniformMatrix(projectionMatLoc, currentPG.glProjection);
        }

        if (-1 < viewportLoc) {
            float x = currentPG.viewport.get(0);
            float y = currentPG.viewport.get(1);
            float w = currentPG.viewport.get(2);
            float h = currentPG.viewport.get(3);
            setUniformValue(viewportLoc, x, y, w, h);
        }

        if (-1 < resolutionLoc) {
            float w = currentPG.viewport.get(2);
            float h = currentPG.viewport.get(3);
            setUniformValue(resolutionLoc, w, h);
        }

        if (-1 < ppixelsLoc) {
            ppixelsUnit = getLastTexUnit() + 1;
            setUniformValue(ppixelsLoc, ppixelsUnit);
            rainbowGL.activeTexture(RainbowGL.TEXTURE0 + ppixelsUnit);
            currentPG.bindFrontTexture();
        } else {
            ppixelsUnit = -1;
        }
    }

    protected void bindTyped() {
        if (currentPG == null) {
            setRenderer(primaryPG.getCurrentPG());
            loadAttributes();
            loadUniforms();
        }
        setCommonUniforms();

        if (-1 < vertexLoc) {
            rainbowGL.enableVertexAttribArray(vertexLoc);
        }
        if (-1 < colorLoc) {
            rainbowGL.enableVertexAttribArray(colorLoc);
        }
        if (-1 < texCoordLoc) {
            rainbowGL.enableVertexAttribArray(texCoordLoc);
        }
        if (-1 < normalLoc) {
            rainbowGL.enableVertexAttribArray(normalLoc);
        }

        if (-1 < normalMatLoc) {
            currentPG.updateGLNormal();
            setUniformMatrix(normalMatLoc, currentPG.glNormal);
        }

        if (-1 < ambientLoc) {
            rainbowGL.enableVertexAttribArray(ambientLoc);
        }
        if (-1 < specularLoc) {
            rainbowGL.enableVertexAttribArray(specularLoc);
        }
        if (-1 < emissiveLoc) {
            rainbowGL.enableVertexAttribArray(emissiveLoc);
        }
        if (-1 < shininessLoc) {
            rainbowGL.enableVertexAttribArray(shininessLoc);
        }

        int count = currentPG.lightCount;
        setUniformValue(lightCountLoc, count);
        if (0 < count) {
            setUniformVector(lightPositionLoc, currentPG.lightPosition, 4, count);
            setUniformVector(lightNormalLoc, currentPG.lightNormal, 3, count);
            setUniformVector(lightAmbientLoc, currentPG.lightAmbient, 3, count);
            setUniformVector(lightDiffuseLoc, currentPG.lightDiffuse, 3, count);
            setUniformVector(lightSpecularLoc, currentPG.lightSpecular, 3, count);
            setUniformVector(lightFalloffLoc, currentPG.lightFalloffCoefficients,
                             3, count
            );
            setUniformVector(lightSpotLoc, currentPG.lightSpotParameters, 2, count);
        }

        if (-1 < directionLoc) {
            rainbowGL.enableVertexAttribArray(directionLoc);
        }

        if (-1 < offsetLoc) {
            rainbowGL.enableVertexAttribArray(offsetLoc);
        }

        if (-1 < perspectiveLoc) {
            setUniformValue(perspectiveLoc, 0);
        }

        if (-1 < scaleLoc) {
            float f = RainbowGL.STROKE_DISPLACEMENT;
            if (currentPG.orthoProjection()) {
                setUniformValue(scaleLoc, 1, 1, f);
            } else {
                setUniformValue(scaleLoc, f, f, f);
            }
        }
    }

    protected void unbindTyped() {
        if (-1 < offsetLoc) {
            rainbowGL.disableVertexAttribArray(offsetLoc);
        }

        if (-1 < directionLoc) {
            rainbowGL.disableVertexAttribArray(directionLoc);
        }

        if (-1 < textureLoc && texture != null) {
            rainbowGL.activeTexture(RainbowGL.TEXTURE0 + texUnit);
            texture.unbind();
            rainbowGL.activeTexture(RainbowGL.TEXTURE0);
            texture = null;
        }

        if (-1 < ambientLoc) {
            rainbowGL.disableVertexAttribArray(ambientLoc);
        }
        if (-1 < specularLoc) {
            rainbowGL.disableVertexAttribArray(specularLoc);
        }
        if (-1 < emissiveLoc) {
            rainbowGL.disableVertexAttribArray(emissiveLoc);
        }
        if (-1 < shininessLoc) {
            rainbowGL.disableVertexAttribArray(shininessLoc);
        }

        if (-1 < vertexLoc) {
            rainbowGL.disableVertexAttribArray(vertexLoc);
        }
        if (-1 < colorLoc) {
            rainbowGL.disableVertexAttribArray(colorLoc);
        }
        if (-1 < texCoordLoc) {
            rainbowGL.disableVertexAttribArray(texCoordLoc);
        }
        if (-1 < normalLoc) {
            rainbowGL.disableVertexAttribArray(normalLoc);
        }

        if (-1 < ppixelsLoc) {
            rainbowGL.enableFBOLayer();
            rainbowGL.activeTexture(RainbowGL.TEXTURE0 + ppixelsUnit);
            currentPG.unbindFrontTexture();
            rainbowGL.activeTexture(RainbowGL.TEXTURE0);
        }

        rainbowGL.bindBuffer(RainbowGL.ARRAY_BUFFER, 0);
    }

    protected void setTexture(Texture tex) {
        texture = tex;

        float scaleu = 1;
        float scalev = 1;
        float dispu = 0;
        float dispv = 0;

        if (tex != null) {
            if (tex.invertedX()) {
                scaleu = -1;
                dispu = 1;
            }

            if (tex.invertedY()) {
                scalev = -1;
                dispv = 1;
            }

            scaleu *= tex.maxTexcoordU();
            dispu *= tex.maxTexcoordU();
            scalev *= tex.maxTexcoordV();
            dispv *= tex.maxTexcoordV();

            setUniformValue(texOffsetLoc, 1.0f / tex.width, 1.0f / tex.height);

            if (-1 < textureLoc) {
                texUnit = -1 < ppixelsUnit ? ppixelsUnit + 1 : getLastTexUnit() + 1;
                setUniformValue(textureLoc, texUnit);
                rainbowGL.activeTexture(RainbowGL.TEXTURE0 + texUnit);
                tex.bind();
            }
        }

        if (-1 < texMatrixLoc) {
            if (tcmat == null) {
                tcmat = new float[16];
            }
            tcmat[0] = scaleu;
            tcmat[4] = 0;
            tcmat[8] = 0;
            tcmat[12] = dispu;
            tcmat[1] = 0;
            tcmat[5] = scalev;
            tcmat[9] = 0;
            tcmat[13] = dispv;
            tcmat[2] = 0;
            tcmat[6] = 0;
            tcmat[10] = 0;
            tcmat[14] = 0;
            tcmat[3] = 0;
            tcmat[7] = 0;
            tcmat[11] = 0;
            tcmat[15] = 0;
            setUniformMatrix(texMatrixLoc, tcmat);
        }
    }

    protected boolean supportsTexturing() {
        return -1 < textureLoc;
    }

    protected boolean supportLighting() {
        return -1 < lightCountLoc || -1 < lightPositionLoc || -1 < lightNormalLoc;
    }

    protected boolean accessTexCoords() {
        return -1 < texCoordLoc;
    }

    protected boolean accessNormals() {
        return -1 < normalLoc;
    }

    protected boolean accessLightAttribs() {
        return -1 < ambientLoc || -1 < specularLoc || -1 < emissiveLoc ||
                -1 < shininessLoc;
    }

    protected void setVertexAttribute(int vboId, int size, int type,
                                      int stride, int offset) {
        setAttributeVBO(vertexLoc, vboId, size, type, false, stride, offset);
    }

    protected void setColorAttribute(int vboId, int size, int type,
                                     int stride, int offset) {
        setAttributeVBO(colorLoc, vboId, size, type, true, stride, offset);
    }

    protected void setNormalAttribute(int vboId, int size, int type,
                                      int stride, int offset) {
        setAttributeVBO(normalLoc, vboId, size, type, false, stride, offset);
    }

    protected void setTexcoordAttribute(int vboId, int size, int type,
                                        int stride, int offset) {
        setAttributeVBO(texCoordLoc, vboId, size, type, false, stride, offset);
    }

    protected void setAmbientAttribute(int vboId, int size, int type,
                                       int stride, int offset) {
        setAttributeVBO(ambientLoc, vboId, size, type, true, stride, offset);
    }

    protected void setSpecularAttribute(int vboId, int size, int type,
                                        int stride, int offset) {
        setAttributeVBO(specularLoc, vboId, size, type, true, stride, offset);
    }

    protected void setEmissiveAttribute(int vboId, int size, int type,
                                        int stride, int offset) {
        setAttributeVBO(emissiveLoc, vboId, size, type, true, stride, offset);
    }

    protected void setShininessAttribute(int vboId, int size, int type,
                                         int stride, int offset) {
        setAttributeVBO(shininessLoc, vboId, size, type, false, stride, offset);
    }

    protected void setLineAttribute(int vboId, int size, int type,
                                    int stride, int offset) {
        setAttributeVBO(directionLoc, vboId, size, type, false, stride, offset);
    }

    protected void setPointAttribute(int vboId, int size, int type,
                                     int stride, int offset) {
        setAttributeVBO(offsetLoc, vboId, size, type, false, stride, offset);
    }

    // ***************************************************************************
    //
    // Class to store a user-specified value for a uniform parameter
    // in the shader
    protected static class UniformValue {
        static final int INT1 = 0;
        static final int INT2 = 1;
        static final int INT3 = 2;
        static final int INT4 = 3;
        static final int FLOAT1 = 4;
        static final int FLOAT2 = 5;
        static final int FLOAT3 = 6;
        static final int FLOAT4 = 7;
        static final int INT1VEC = 8;
        static final int INT2VEC = 9;
        static final int INT3VEC = 10;
        static final int INT4VEC = 11;
        static final int FLOAT1VEC = 12;
        static final int FLOAT2VEC = 13;
        static final int FLOAT3VEC = 14;
        static final int FLOAT4VEC = 15;
        static final int MAT2 = 16;
        static final int MAT3 = 17;
        static final int MAT4 = 18;
        static final int SAMPLER2D = 19;

        int type;
        Object value;

        UniformValue(int type, Object value) {
            this.type = type;
            this.value = value;
        }
    }
}
