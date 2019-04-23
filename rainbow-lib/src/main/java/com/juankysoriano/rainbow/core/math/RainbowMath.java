package com.juankysoriano.rainbow.core.math;

import com.juankysoriano.rainbow.core.graphics.RainbowGraphics;
import com.juankysoriano.rainbow.core.graphics.RainbowImage;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by juankysoriano on 08/04/2014.
 */
public abstract class RainbowMath {
    public static final float PI = (float) Math.PI;
    public static final float RAD_TO_DEG = 180.0f / PI;
    public static final float DEG_TO_RAD = PI / 180.0f;
    public static final float TAU = PI * 2.0f;
    public static final float TWO_PI = PI * 2.0f;
    public static final float QUARTER_PI = PI / 4.0f;
    public static final float HALF_PI = PI / 2.0f;
    public static final float THIRD_PI = PI / 3.0f;
    public static final float EPSILON = 0.0001f;
    private static final String ERROR_MIN_MAX = "Cannot use min() or max() on an empty array.";
    private static final int PERLIN_YWRAPB = 4;
    private static final int PERLIN_YWRAP = 1 << RainbowMath.PERLIN_YWRAPB;
    private static final int PERLIN_ZWRAPB = 8;
    private static final int PERLIN_ZWRAP = 1 << RainbowMath.PERLIN_ZWRAPB;
    private static final int PERLIN_SIZE = 4095;
    private static HashMap<String, Pattern> matchPatterns;
    private static Random internalRandom;
    private static int perlin_octaves = 4; // default to medium smooth
    private static float perlin_amp_falloff = 0.5f; // 50% reduction/octave
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    // [toxi 031112]
    // new vars needed due to recent change of cos table in PGraphics
    private static int perlin_TWOPI;
    private static int perlin_PI;
    private static float[] perlin_cosTable;
    private static float[] perlin;
    // ////////////////////////////////////////////////////////////
    private static Random perlinRandom;
    /**
     * Integer number formatter.
     */
    private static NumberFormat int_nf;
    private static int int_nf_digits;
    private static boolean int_nf_commas;
    private static NumberFormat float_nf;
    private static int float_nf_left, float_nf_right;
    private static boolean float_nf_commas;

    public static float abs( float n) {
        return (n < 0) ? -n : n;
    }

    public static int abs( int n) {
        return (n < 0) ? -n : n;
    }

    public static float log( float a) {
        return (float) Math.log(a);
    }

    public static float exp( float a) {
        return (float) Math.exp(a);
    }

    public static float pow( float a,  float b) {
        return (float) Math.pow(a, b);
    }

    public static int max( int a,  int b) {
        return (a > b) ? a : b;
    }

    public static float max( float a,  float b) {
        return (a > b) ? a : b;
    }

    public static int max( int a,  int b,  int c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    public static float max( float a,  float b,  float c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    /**
     * Find the maximum value in an array. Throws an
     * ArrayIndexOutOfBoundsException if the array is length 0.
     *
     * @param list the source array
     * @return The maximum value
     */
    public static  int max( int[] list) {
        if (list.length == 0) {
            throw new ArrayIndexOutOfBoundsException(RainbowMath.ERROR_MIN_MAX);
        }
        int max = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] > max) {
                max = list[i];
            }
        }
        return max;
    }

    /**
     * Find the maximum value in an array. Throws an
     * ArrayIndexOutOfBoundsException if the array is length 0.
     *
     * @param list the source array
     * @return The maximum value
     */
    public static  float max( float[] list) {
        if (list.length == 0) {
            throw new ArrayIndexOutOfBoundsException(RainbowMath.ERROR_MIN_MAX);
        }
        float max = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] > max) {
                max = list[i];
            }
        }
        return max;
    }

    public static  int min( int a,  int b) {
        return (a < b) ? a : b;
    }

    public static  float min( float a,  float b) {
        return (a < b) ? a : b;
    }

    public static  int min( int a,  int b,  int c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    public static  float min( float a,  float b,  float c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    /**
     * Find the minimum value in an array. Throws an
     * ArrayIndexOutOfBoundsException if the array is length 0.
     *
     * @param list the source array
     * @return The minimum value
     */
    public static  int min( int[] list) {
        if (list.length == 0) {
            throw new ArrayIndexOutOfBoundsException(RainbowMath.ERROR_MIN_MAX);
        }
        int min = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] < min) {
                min = list[i];
            }
        }
        return min;
    }

    /**
     * Find the minimum value in an array. Throws an
     * ArrayIndexOutOfBoundsException if the array is length 0.
     *
     * @param list the source array
     * @return The minimum value
     */
    public static  float min( float[] list) {
        if (list.length == 0) {
            throw new ArrayIndexOutOfBoundsException(RainbowMath.ERROR_MIN_MAX);
        }
        float min = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] < min) {
                min = list[i];
            }
        }
        return min;
    }

    public static  int constrain( int amt,  int low,  int high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    // ////////////////////////////////////////////////////////////

    public static  float constrain( float amt,  float low,  float high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    public static  float sin( float angle) {
        return (float) Math.sin(angle);
    }

    // ////////////////////////////////////////////////////////////
    // getting the time

    public static  float cos( float angle) {
        return (float) Math.cos(angle);
    }

    public static  float tan( float angle) {
        return (float) Math.tan(angle);
    }

    public static  float asin( float value) {
        return (float) Math.asin(value);
    }

    public static  float acos( float value) {
        return (float) Math.acos(value);
    }

    public static  float atan( float value) {
        return (float) Math.atan(value);
    }

    public static  float atan2( float a,  float b) {
        return (float) Math.atan2(a, b);
    }

    public static  float degrees( float radians) {
        return radians * RAD_TO_DEG;
    }

    public static  float radians( float degrees) {
        return degrees * DEG_TO_RAD;
    }

    // ////////////////////////////////////////////////////////////

    // controlling time (playing god)

    public static  int ceil( float what) {
        return (int) Math.ceil(what);
    }

    public static  int floor( float what) {
        return (int) Math.floor(what);
    }

    // ////////////////////////////////////////////////////////////

    public static  int round( float what) {
        return Math.round(what);
    }

    public static  float mag( float a,  float b) {
        return (float) Math.sqrt((a * a) + (b * b));
    }

    public static  float mag( float a,  float b,  float c) {
        return (float) Math.sqrt((a * a) + (b * b) + (c * c));
    }

    public static  float dist( float x1,  float y1,  float x2,  float y2) {
        return RainbowMath.sqrt(RainbowMath.sq(x2 - x1) + RainbowMath.sq(y2 - y1));
    }

    public static  float sqrt( float a) {
        return (float) Math.sqrt(a);
    }

    // ////////////////////////////////////////////////////////////

    public static  float sq( float a) {
        return a * a;
    }

    public static  float dist( float x1,  float y1,  float z1,  float x2,  float y2,  float z2) {
        return RainbowMath.sqrt(RainbowMath.sq(x2 - x1) + RainbowMath.sq(y2 - y1) + RainbowMath.sq(z2 - z1));
    }

    public static  float lerp( float start,  float stop,  float amt) {
        return start + ((stop - start) * amt);
    }

    /**
     * Normalize a value to exist between 0 and 1 (inclusive). Mathematically
     * the opposite of lerp(), figures out what proportion a particular value is
     * relative to start and stop coordinates.
     */
    public static  float norm( float value,  float start,  float stop) {
        return (value - start) / (stop - start);
    }

    /**
     * Convenience function to map a variable from one coordinate space to
     * another. Equivalent to unlerp() followed by lerp().
     */
    public static  float map( float value,  float istart,  float istop,  float ostart,  float ostop) {
        return ostart + ((ostop - ostart) * ((value - istart) / (istop - istart)));
    }

    // ////////////////////////////////////////////////////////////

    public static byte[] sort( byte what[]) {
        return RainbowMath.sort(what, what.length);
    }

    public static byte[] sort( byte[] what,  int count) {
         byte[] outgoing = new byte[what.length];
        System.arraycopy(what, 0, outgoing, 0, what.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    public static char[] sort( char what[]) {
        return RainbowMath.sort(what, what.length);
    }

    public static char[] sort( char[] what,  int count) {
         char[] outgoing = new char[what.length];
        System.arraycopy(what, 0, outgoing, 0, what.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    public static int[] sort( int what[]) {
        return RainbowMath.sort(what, what.length);
    }

    public static int[] sort( int[] what,  int count) {
         int[] outgoing = new int[what.length];
        System.arraycopy(what, 0, outgoing, 0, what.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    public static float[] sort( float what[]) {
        return RainbowMath.sort(what, what.length);
    }

    public static float[] sort( float[] what,  int count) {
         float[] outgoing = new float[what.length];
        System.arraycopy(what, 0, outgoing, 0, what.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    public static String[] sort( String what[]) {
        return RainbowMath.sort(what, what.length);
    }

    public static String[] sort( String[] what,  int count) {
         String[] outgoing = new String[what.length];
        System.arraycopy(what, 0, outgoing, 0, what.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    public static boolean[] expand( boolean list[]) {
        return RainbowMath.expand(list, list.length << 1);
    }

    public static boolean[] expand( boolean list[],  int newSize) {
         boolean temp[] = new boolean[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    public static byte[] expand( byte list[]) {
        return RainbowMath.expand(list, list.length << 1);
    }

    public static byte[] expand( byte list[],  int newSize) {
         byte temp[] = new byte[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    public static char[] expand( char list[]) {
        return RainbowMath.expand(list, list.length << 1);
    }

    //

	/*
     * // not very useful, because it only works for public (and protected?) //
	 * fields of a class, not local variables to methods public void
	 * printvar(String name) { try { Field field =
	 * getClass().getDeclaredField(name); println(name + " = " +
	 * field.get(this)); } catch (Exception e) { e.printStackTrace(); } }
	 */

    // ////////////////////////////////////////////////////////////

    // MATH

    // lots of convenience methods for math with floats.
    // doubles are overkill for processing applets, and casting
    // things all the time is annoying, thus the functions below.

    public static char[] expand( char list[],  int newSize) {
         char temp[] = new char[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    public static int[] expand( int list[]) {
        return RainbowMath.expand(list, list.length << 1);
    }

    public static int[] expand( int list[],  int newSize) {
         int temp[] = new int[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    public static RainbowImage[] expand( RainbowImage list[]) {
        return RainbowMath.expand(list, list.length << 1);
    }

    public static RainbowImage[] expand( RainbowImage list[],  int newSize) {
         RainbowImage temp[] = new RainbowImage[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    public static float[] expand( float list[]) {
        return RainbowMath.expand(list, list.length << 1);
    }

    public static float[] expand( float list[],  int newSize) {
         float temp[] = new float[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    public static String[] expand( String list[]) {
        return RainbowMath.expand(list, list.length << 1);
    }

    public static String[] expand( String list[],  int newSize) {
         String temp[] = new String[newSize];
        // in case the new size is smaller than list.length
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    public static Object expand( Object array) {
        return RainbowMath.expand(array, Array.getLength(array) << 1);
    }

    public static Object expand( Object list,  int newSize) {
         Class<?> type = list.getClass().getComponentType();
         Object temp = Array.newInstance(type, newSize);
        System.arraycopy(list, 0, temp, 0, Math.min(Array.getLength(list), newSize));
        return temp;
    }

    public static byte[] append(byte b[],  byte value) {
        b = RainbowMath.expand(b, b.length + 1);
        b[b.length - 1] = value;
        return b;
    }

    public static char[] append(char b[],  char value) {
        b = RainbowMath.expand(b, b.length + 1);
        b[b.length - 1] = value;
        return b;
    }

    public static int[] append(int b[],  int value) {
        b = RainbowMath.expand(b, b.length + 1);
        b[b.length - 1] = value;
        return b;
    }

    public static float[] append(float b[],  float value) {
        b = RainbowMath.expand(b, b.length + 1);
        b[b.length - 1] = value;
        return b;
    }

    public static String[] append(String b[],  String value) {
        b = RainbowMath.expand(b, b.length + 1);
        b[b.length - 1] = value;
        return b;
    }

    public static Object append(Object b,  Object value) {
         int length = Array.getLength(b);
        b = RainbowMath.expand(b, length + 1);
        Array.set(b, length, value);
        return b;
    }

    public static boolean[] shorten( boolean list[]) {
        return RainbowMath.subset(list, 0, list.length - 1);
    }

    public static boolean[] subset( boolean list[],  int start,  int count) {
         boolean output[] = new boolean[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    public static byte[] shorten( byte list[]) {
        return RainbowMath.subset(list, 0, list.length - 1);
    }

    public static byte[] subset( byte list[],  int start,  int count) {
         byte output[] = new byte[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    public static char[] shorten( char list[]) {
        return RainbowMath.subset(list, 0, list.length - 1);
    }

    public static char[] subset( char list[],  int start,  int count) {
         char output[] = new char[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    public static int[] shorten( int list[]) {
        return RainbowMath.subset(list, 0, list.length - 1);
    }

    public static int[] subset( int list[],  int start,  int count) {
         int output[] = new int[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    public static float[] shorten( float list[]) {
        return RainbowMath.subset(list, 0, list.length - 1);
    }

    public static float[] subset( float list[],  int start,  int count) {
         float output[] = new float[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    public static String[] shorten( String list[]) {
        return RainbowMath.subset(list, 0, list.length - 1);
    }

    public static String[] subset( String list[],  int start,  int count) {
         String output[] = new String[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    public static Object shorten( Object list) {
         int length = Array.getLength(list);
        return RainbowMath.subset(list, 0, length - 1);
    }

    public static Object subset( Object list,  int start,  int count) {
         Class<?> type = list.getClass().getComponentType();
         Object outgoing = Array.newInstance(type, count);
        System.arraycopy(list, start, outgoing, 0, count);
        return outgoing;
    }

    static  public boolean[] splice( boolean list[],  boolean v,  int index) {
         boolean outgoing[] = new boolean[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = v;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static  public boolean[] splice( boolean list[],  boolean v[],  int index) {
         boolean outgoing[] = new boolean[list.length + v.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(v, 0, outgoing, index, v.length);
        System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
        return outgoing;
    }

    static  public byte[] splice( byte list[],  byte v,  int index) {
         byte outgoing[] = new byte[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = v;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static  public byte[] splice( byte list[],  byte v[],  int index) {
         byte outgoing[] = new byte[list.length + v.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(v, 0, outgoing, index, v.length);
        System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
        return outgoing;
    }

    static  public char[] splice( char list[],  char v,  int index) {
         char outgoing[] = new char[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = v;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static  public char[] splice( char list[],  char v[],  int index) {
         char outgoing[] = new char[list.length + v.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(v, 0, outgoing, index, v.length);
        System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
        return outgoing;
    }

    // ////////////////////////////////////////////////////////////

    // RANDOM NUMBERS

    static  public int[] splice( int list[],  int v,  int index) {
         int outgoing[] = new int[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = v;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static  public int[] splice( int list[],  int v[],  int index) {
         int outgoing[] = new int[list.length + v.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(v, 0, outgoing, index, v.length);
        System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
        return outgoing;
    }

    static  public float[] splice( float list[],  float v,  int index) {
         float outgoing[] = new float[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = v;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static  public float[] splice( float list[],  float v[],  int index) {
         float outgoing[] = new float[list.length + v.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(v, 0, outgoing, index, v.length);
        System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
        return outgoing;
    }

    // ////////////////////////////////////////////////////////////

    // PERLIN NOISE

    // [toxi 040903]
    // octaves and amplitude amount per octave are now user controlled
    // via the noiseDetail() function.

    // [toxi 030902]
    // cleaned up code and now using bagel's cosine table to speed up

    // [toxi 030901]
    // implementation by the german demo group farbrausch
    // as used in their demo "art": http://www.farb-rausch.de/fr010src.zip

    static  public String[] splice( String list[],  String v,  int index) {
         String outgoing[] = new String[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = v;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static  public String[] splice( String list[],  String v[],  int index) {
         String outgoing[] = new String[list.length + v.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(v, 0, outgoing, index, v.length);
        System.arraycopy(list, index, outgoing, index + v.length, list.length - index);
        return outgoing;
    }

    static  public Object splice( Object list,  Object v,  int index) {
        Object[] outgoing = null;
         int length = Array.getLength(list);

        // check whether item being spliced in is an array
        if (v.getClass().getName().charAt(0) == '[') {
             int vlength = Array.getLength(v);
            outgoing = new Object[length + vlength];
            System.arraycopy(list, 0, outgoing, 0, index);
            System.arraycopy(v, 0, outgoing, index, vlength);
            System.arraycopy(list, index, outgoing, index + vlength, length - index);

        } else {
            outgoing = new Object[length + 1];
            System.arraycopy(list, 0, outgoing, 0, index);
            Array.set(outgoing, index, v);
            System.arraycopy(list, index, outgoing, index + 1, length - index);
        }
        return outgoing;
    }

    public static boolean[] subset( boolean list[],  int start) {
        return RainbowMath.subset(list, start, list.length - start);
    }

    public static byte[] subset( byte list[],  int start) {
        return RainbowMath.subset(list, start, list.length - start);
    }

    public static char[] subset( char list[],  int start) {
        return RainbowMath.subset(list, start, list.length - start);
    }

    public static int[] subset( int list[],  int start) {
        return RainbowMath.subset(list, start, list.length - start);
    }

    public static float[] subset( float list[],  int start) {
        return RainbowMath.subset(list, start, list.length - start);
    }

    public static String[] subset( String list[],  int start) {
        return RainbowMath.subset(list, start, list.length - start);
    }

    public static Object subset( Object list,  int start) {
         int length = Array.getLength(list);
        return RainbowMath.subset(list, start, length - start);
    }

    public static boolean[] concat( boolean a[],  boolean b[]) {
         boolean c[] = new boolean[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] concat( byte a[],  byte b[]) {
         byte c[] = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static char[] concat( char a[],  char b[]) {
         char c[] = new char[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static int[] concat( int a[],  int b[]) {
         int c[] = new int[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static float[] concat( float a[],  float b[]) {
         float c[] = new float[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    // [toxi 040903]
    // make perlin noise quality user controlled to allow
    // for different levels of detail. lower values will produce
    // smoother results as higher octaves are surpressed

    public static String[] concat( String a[],  String b[]) {
         String c[] = new String[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static Object concat( Object a,  Object b) {
         Class<?> type = a.getClass().getComponentType();
         int alength = Array.getLength(a);
         int blength = Array.getLength(b);
         Object outgoing = Array.newInstance(type, alength + blength);
        System.arraycopy(a, 0, outgoing, 0, alength);
        System.arraycopy(b, 0, outgoing, alength, blength);
        return outgoing;
    }

    public static boolean[] reverse( boolean list[]) {
         boolean outgoing[] = new boolean[list.length];
         int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    public static byte[] reverse( byte list[]) {
         byte outgoing[] = new byte[list.length];
         int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    public static char[] reverse( char list[]) {
         char outgoing[] = new char[list.length];
         int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    public static int[] reverse( int list[]) {
         int outgoing[] = new int[list.length];
         int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    public static float[] reverse( float list[]) {
         float outgoing[] = new float[list.length];
         int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    public static String[] reverse( String list[]) {
         String outgoing[] = new String[list.length];
         int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    public static Object reverse( Object list) {
         Class<?> type = list.getClass().getComponentType();
         int length = Array.getLength(list);
         Object outgoing = Array.newInstance(type, length);
        for (int i = 0; i < length; i++) {
            Array.set(outgoing, i, Array.get(list, (length - 1) - i));
        }
        return outgoing;
    }

    /**
     * Remove whitespace characters from the beginning and ending of a String.
     * Works like String.trim() but includes the unicode nbsp character as well.
     */
    public static String trim( String str) {
        return str.replace('\u00A0', ' ').trim();
    }

    /**
     * Trim the whitespace from a String array. This returns a new array and
     * does not affect the passed-in array.
     */
    public static String[] trim( String[] array) {
         String[] outgoing = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                outgoing[i] = array[i].replace('\u00A0', ' ').trim();
            }
        }
        return outgoing;
    }

    /**
     * Join an array of Strings together as a single String, separated by the
     * whatever's passed in for the separator.
     */
    public static String join( String str[],  char separator) {
        return RainbowMath.join(str, String.valueOf(separator));
    }

    /**
     * Join an array of Strings together as a single String, separated by the
     * whatever's passed in for the separator.
     * <p/>
     * To use this on numbers, first pass the array to nf() or nfs() to get a
     * list of String objects, then use join on that.
     * <p/>
     * <PRE>
     * e.g.String stuff[] = { &quot;apple&quot;, &quot;bear&quot;, &quot;cat&quot; };
     * String list = join(stuff, &quot;, &quot;);
     * // list is now &quot;apple, bear, cat&quot;
     * </PRE>
     */
    public static String join( String str[],  String separator) {
         StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < str.length; i++) {
            if (i != 0) {
                buffer.append(separator);
            }
            buffer.append(str[i]);
        }
        return buffer.toString();
    }

    // ////////////////////////////////////////////////////////////

    // DATA I/O

    /**
     * Splits a string into pieces, using any of the chars in the String 'delim'
     * as separator characters. For instance, in addition to white space, you
     * might want to treat commas as a separator. The delimeter characters won't
     * appear in the returned String array.
     * <p/>
     * <PRE>
     * i.e. splitTokens("a, b", " ,") -> { "a", "b" }
     * </PRE>
     * <p/>
     * To include all the whitespace possibilities, use the variable WHITESPACE,
     * found in PConstants:
     * <p/>
     * <PRE>
     * i.e. splitTokens("a   | b", WHITESPACE + "|");  ->  { "a", "b" }
     * </PRE>
     */
    public static String[] splitTokens( String what,  String delim) {
         StringTokenizer toker = new StringTokenizer(what, delim);
         String pieces[] = new String[toker.countTokens()];

        int index = 0;
        while (toker.hasMoreTokens()) {
            pieces[index++] = toker.nextToken();
        }
        return pieces;
    }

    /**
     * Split a string into pieces along a specific character. Most commonly used
     * to break up a String along a space or a tab character.
     * <p/>
     * This operates differently than the others, where the single delimeter is
     * the only breaking point, and consecutive delimeters will produce an empty
     * string (""). This way, one can split on tab characters, but maintain the
     * column alignments (of say an excel file) where there are empty columns.
     */
    public static String[] split( String what,  char delim) {
        // do this so that the exception occurs inside the user's
        // program, rather than appearing to be a bug inside split()
        if (what == null) {
            return null;
            // return split(what, String.valueOf(delim)); // huh
        }

         char chars[] = what.toCharArray();
        int splitCount = 0; // 1;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == delim) {
                splitCount++;
            }
        }
        // make sure that there is something in the input string
        // if (chars.length > 0) {
        // if the last char is a delimeter, get rid of it..
        // if (chars[chars.length-1] == delim) splitCount--;
        // on second thought, i don't agree with this, will disable
        // }
        if (splitCount == 0) {
             String splits[] = new String[1];
            splits[0] = new String(what);
            return splits;
        }
        // int pieceCount = splitCount + 1;
         String splits[] = new String[splitCount + 1];
        int splitIndex = 0;
        int startIndex = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == delim) {
                splits[splitIndex++] = new String(chars, startIndex, i - startIndex);
                startIndex = i + 1;
            }
        }
        // if (startIndex != chars.length) {
        splits[splitIndex] = new String(chars, startIndex, chars.length - startIndex);
        // }
        return splits;
    }

    /**
     * Split a String on a specific delimiter. Unlike Java's String.split()
     * method, this does not parse the delimiter as a regexp because it's more
     * confusing than necessary, and String.split() is always available for
     * those who want regexp.
     */
    public static String[] split( String what,  String delim) {
         ArrayList<String> items = new ArrayList<String>();
        int index;
        int offset = 0;
        while ((index = what.indexOf(delim, offset)) != -1) {
            items.add(what.substring(offset, index));
            offset = index + delim.length();
        }
        items.add(what.substring(offset));
         String[] outgoing = new String[items.size()];
        items.toArray(outgoing);
        return outgoing;
    }

    /**
     * Match a string with a regular expression, and returns the match as an
     * array. The first index is the matching expression, and array elements [1]
     * and higher represent each of the groups (sequences found in parens).
     * <p/>
     * This uses multiline matching (Pattern.MULTILINE) and dotall mode
     * (Pattern.DOTALL) by default, so that ^ and $ match the beginning and end
     * of any lines found in the source, and the . operator will also pick up
     * newline characters.
     */
    public static String[] match( String what,  String regexp) {
         Pattern p = RainbowMath.matchPattern(regexp);
         Matcher m = p.matcher(what);
        if (m.find()) {
             int count = m.groupCount() + 1;
             String[] groups = new String[count];
            for (int i = 0; i < count; i++) {
                groups[i] = m.group(i);
            }
            return groups;
        }
        return null;
    }

    static Pattern matchPattern( String regexp) {
        Pattern p = null;
        if (RainbowMath.matchPatterns == null) {
            RainbowMath.matchPatterns = new HashMap<String, Pattern>();
        } else {
            p = RainbowMath.matchPatterns.get(regexp);
        }
        if (p == null) {
            if (RainbowMath.matchPatterns.size() == 10) {
                // Just clear out the match patterns here if more than 10 are
                // being
                // used. It's not terribly efficient, but changes that you have
                // >10
                // different match patterns are very slim, unless you're doing
                // something really tricky (like custom match() methods), in
                // which
                // case match() won't be efficient anyway. (And you should just
                // be
                // using your own Java code.) The alternative is using a queue
                // here,
                // but that's a silly amount of work for negligible benefit.
                RainbowMath.matchPatterns.clear();
            }
            p = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL);
            RainbowMath.matchPatterns.put(regexp, p);
        }
        return p;
    }

    /**
     * Identical to match(), except that it returns an array of all matches in
     * the specified String, rather than just the first.
     */
    public static String[][] matchAll( String what,  String regexp) {
         Pattern p = RainbowMath.matchPattern(regexp);
         Matcher m = p.matcher(what);
         ArrayList<String[]> results = new ArrayList<String[]>();
         int count = m.groupCount() + 1;
        while (m.find()) {
             String[] groups = new String[count];
            for (int i = 0; i < count; i++) {
                groups[i] = m.group(i);
            }
            results.add(groups);
        }
        if (results.isEmpty()) {
            return null;
        }
         String[][] matches = new String[results.size()][count];
        for (int i = 0; i < matches.length; i++) {
            matches[i] = results.get(i);
        }
        return matches;
    }

    /**
     * <p>
     * Convert an integer to a boolean. Because of how Java handles upgrading
     * numbers, this will also cover byte and char (as they will upgrade to an
     * int without any sort of explicit cast).
     * </p>
     * <p>
     * The preprocessor will convert boolean(what) to parseBoolean(what).
     * </p>
     *
     * @return false if 0, true if any other number
     */
    static  public boolean parseBoolean( int what) {
        return (what != 0);
    }

    /**
     * Convert the string "true" or "false" to a boolean.
     *
     * @return true if 'what' is "true" or "TRUE", false otherwise
     */
    static  public boolean parseBoolean( String what) {
        return Boolean.valueOf(what);
    }

    /**
     * Convert a byte array to a boolean array. Each element will be evaluated
     * identical to the integer case, where a byte equal to zero will return
     * false, and any other value will return true.
     *
     * @return array of boolean elements
     */
    static  public boolean[] parseBoolean( byte what[]) {
         boolean outgoing[] = new boolean[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (what[i] != 0);
        }
        return outgoing;
    }

    /**
     * Convert an int array to a boolean array. An int equal to zero will return
     * false, and any other value will return true.
     *
     * @return array of boolean elements
     */
    static  public boolean[] parseBoolean( int what[]) {
         boolean outgoing[] = new boolean[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (what[i] != 0);
        }
        return outgoing;
    }

    static  public boolean[] parseBoolean( String what[]) {
         boolean outgoing[] = new boolean[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = new Boolean(what[i]).booleanValue();
        }
        return outgoing;
    }

    static  public byte parseByte( boolean what) {
        return what ? (byte) 1 : 0;
    }

    // FONT I/O

    static  public byte parseByte( char what) {
        return (byte) what;
    }

    static  public byte parseByte( int what) {
        return (byte) what;
    }

    static  public byte parseByte( float what) {
        return (byte) what;
    }

    static  public byte[] parseByte( boolean what[]) {
         byte outgoing[] = new byte[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = what[i] ? (byte) 1 : 0;
        }
        return outgoing;
    }

    static  public byte[] parseByte( char what[]) {
         byte outgoing[] = new byte[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (byte) what[i];
        }
        return outgoing;
    }

    // ////////////////////////////////////////////////////////////

    // READERS AND WRITERS

    static  public byte[] parseByte( int what[]) {
         byte outgoing[] = new byte[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (byte) what[i];
        }
        return outgoing;
    }

    static  public byte[] parseByte( float what[]) {
         byte outgoing[] = new byte[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (byte) what[i];
        }
        return outgoing;
    }

    static  public char parseChar( byte what) {
        return (char) (what & 0xff);
    }

    static  public char parseChar( int what) {
        return (char) what;
    }

    static  public char[] parseChar( byte what[]) {
         char outgoing[] = new char[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (char) (what[i] & 0xff);
        }
        return outgoing;
    }

    static  public char[] parseChar( int what[]) {
         char outgoing[] = new char[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (char) what[i];
        }
        return outgoing;
    }

    // ////////////////////////////////////////////////////////////

    // FILE INPUT

    static  public int parseInt( boolean what) {
        return what ? 1 : 0;
    }

    /**
     * Note that parseInt() will un-sign a signed byte value.
     */
    static  public int parseInt( byte what) {
        return what & 0xff;
    }

    /**
     * Note that parseInt('5') is unlike String in the sense that it won't
     * return 5, but the ascii value. This is because ((int) someChar) returns
     * the ascii value, and parseInt() is just longhand for the cast.
     */
    static  public int parseInt( char what) {
        return what;
    }

    /**
     * Same as floor(), or an (int) cast.
     */
    static  public int parseInt( float what) {
        return (int) what;
    }

    /**
     * Parse a String into an int value. Returns 0 if the value is bad.
     */
    static  public int parseInt( String what) {
        return RainbowMath.parseInt(what, 0);
    }

    /**
     * Parse a String to an int, and provide an alternate value that should be
     * used when the number is invalid.
     */
    static  public int parseInt( String what,  int otherwise) {
        try {
             int offset = what.indexOf('.');
            if (offset == -1) {
                return Integer.parseInt(what);
            } else {
                return Integer.parseInt(what.substring(0, offset));
            }
        } catch ( NumberFormatException e) {
        }
        return otherwise;
    }

    static  public int[] parseInt( boolean what[]) {
         int list[] = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            list[i] = what[i] ? 1 : 0;
        }
        return list;
    }

    static  public int[] parseInt( byte what[]) { // note this unsigns
         int list[] = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            list[i] = (what[i] & 0xff);
        }
        return list;
    }

    static  public int[] parseInt( char what[]) {
         int list[] = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            list[i] = what[i];
        }
        return list;
    }

    public static int[] parseInt( float what[]) {
         int inties[] = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            inties[i] = (int) what[i];
        }
        return inties;
    }

    // ////////////////////////////////////////////////////////////

    // FILE OUTPUT

    /**
     * Make an array of int elements from an array of String objects. If the
     * String can't be parsed as a number, it will be set to zero.
     * <p/>
     * String s[] = { "1", "300", "44" }; int numbers[] = parseInt(s);
     * <p/>
     * numbers will contain { 1, 300, 44 }
     */
    public static int[] parseInt( String what[]) {
        return RainbowMath.parseInt(what, 0);
    }

    /**
     * Make an array of int elements from an array of String objects. If the
     * String can't be parsed as a number, its entry in the array will be set to
     * the value of the "missing" parameter.
     * <p/>
     * String s[] = { "1", "300", "apple", "44" }; int numbers[] = parseInt(s,
     * 9999);
     * <p/>
     * numbers will contain { 1, 300, 9999, 44 }
     */
    public static int[] parseInt( String what[],  int missing) {
         int output[] = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            try {
                output[i] = Integer.parseInt(what[i]);
            } catch ( NumberFormatException e) {
                output[i] = missing;
            }
        }
        return output;
    }

    /**
     * Convert an int to a float value. Also handles bytes because of Java's
     * rules for upgrading values.
     */
    static  public float parseFloat( int what) { // also handles byte
        return what;
    }

    static  public float parseFloat( String what) {
        return RainbowMath.parseFloat(what, Float.NaN);
    }

    static  public float parseFloat( String what,  float otherwise) {
        try {
            return new Float(what).floatValue();
        } catch ( NumberFormatException e) {
        }

        return otherwise;
    }

    static  public float[] parseByte( byte what[]) {
         float floaties[] = new float[what.length];
        for (int i = 0; i < what.length; i++) {
            floaties[i] = what[i];
        }
        return floaties;
    }

    static  public float[] parseFloat( int what[]) {
         float floaties[] = new float[what.length];
        for (int i = 0; i < what.length; i++) {
            floaties[i] = what[i];
        }
        return floaties;
    }

    static  public float[] parseFloat( String what[]) {
        return RainbowMath.parseFloat(what, Float.NaN);
    }

    static  public float[] parseFloat( String what[],  float missing) {
         float output[] = new float[what.length];
        for (int i = 0; i < what.length; i++) {
            try {
                output[i] = new Float(what[i]).floatValue();
            } catch ( NumberFormatException e) {
                output[i] = missing;
            }
        }
        return output;
    }

    //

    static  public String str( boolean x) {
        return String.valueOf(x);
    }

    static  public String str( byte x) {
        return String.valueOf(x);
    }

    static  public String str( char x) {
        return String.valueOf(x);
    }

    // ////////////////////////////////////////////////////////////

    static  public String str( int x) {
        return String.valueOf(x);
    }

    static  public String str( float x) {
        return String.valueOf(x);
    }

    static  public String[] str( boolean x[]) {
         String s[] = new String[x.length];
        for (int i = 0; i < x.length; i++) {
            s[i] = String.valueOf(x[i]);
        }
        return s;
    }

    static  public String[] str( byte x[]) {
         String s[] = new String[x.length];
        for (int i = 0; i < x.length; i++) {
            s[i] = String.valueOf(x[i]);
        }
        return s;
    }

    static  public String[] str( char x[]) {
         String s[] = new String[x.length];
        for (int i = 0; i < x.length; i++) {
            s[i] = String.valueOf(x[i]);
        }
        return s;
    }

    static  public String[] str( int x[]) {
         String s[] = new String[x.length];
        for (int i = 0; i < x.length; i++) {
            s[i] = String.valueOf(x[i]);
        }
        return s;
    }

    static  public String[] str( float x[]) {
         String s[] = new String[x.length];
        for (int i = 0; i < x.length; i++) {
            s[i] = String.valueOf(x[i]);
        }
        return s;
    }

    // ////////////////////////////////////////////////////////////

    // URL ENCODING

    public static String[] nf( int num[],  int digits) {
         String formatted[] = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = RainbowMath.nf(num[i], digits);
        }
        return formatted;
    }

    public static String nf( int num,  int digits) {
        if ((RainbowMath.int_nf != null) && (RainbowMath.int_nf_digits == digits) && !RainbowMath.int_nf_commas) {
            return RainbowMath.int_nf.format(num);
        }

        RainbowMath.int_nf = NumberFormat.getInstance();
        RainbowMath.int_nf.setGroupingUsed(false); // no commas
        RainbowMath.int_nf_commas = false;
        RainbowMath.int_nf.setMinimumIntegerDigits(digits);
        RainbowMath.int_nf_digits = digits;
        return RainbowMath.int_nf.format(num);
    }

    // ////////////////////////////////////////////////////////////
    // SORT

    public static String[] nfc( int num[]) {
         String formatted[] = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = RainbowMath.nfc(num[i]);
        }
        return formatted;
    }

    public static String nfc( int num) {
        if ((RainbowMath.int_nf != null) && (RainbowMath.int_nf_digits == 0) && RainbowMath.int_nf_commas) {
            return RainbowMath.int_nf.format(num);
        }

        RainbowMath.int_nf = NumberFormat.getInstance();
        RainbowMath.int_nf.setGroupingUsed(true);
        RainbowMath.int_nf_commas = true;
        RainbowMath.int_nf.setMinimumIntegerDigits(0);
        RainbowMath.int_nf_digits = 0;
        return RainbowMath.int_nf.format(num);
    }

    public static String[] nfs( int num[],  int digits) {
         String formatted[] = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = RainbowMath.nfs(num[i], digits);
        }
        return formatted;
    }

    /**
     * number format signed (or space) Formats a number but leaves a blank space
     * in the front when it's positive so that it can be properly aligned with
     * numbers that have a negative sign in front of them.
     */
    public static String nfs( int num,  int digits) {
        return (num < 0) ? RainbowMath.nf(num, digits) : (' ' + RainbowMath.nf(num, digits));
    }

    public static String[] nfp( int num[],  int digits) {
         String formatted[] = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = RainbowMath.nfp(num[i], digits);
        }
        return formatted;
    }

    /**
     * number format positive (or plus) Formats a number, always placing a - or
     * + sign in the front when it's negative or positive.
     */
    public static String nfp( int num,  int digits) {
        return (num < 0) ? RainbowMath.nf(num, digits) : ('+' + RainbowMath.nf(num, digits));
    }

    public static String[] nf( float num[],  int left,  int right) {
         String formatted[] = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = RainbowMath.nf(num[i], left, right);
        }
        return formatted;
    }

    public static String nf( float num,  int left,  int right) {
        if ((RainbowMath.float_nf != null) && (RainbowMath.float_nf_left == left) && (RainbowMath.float_nf_right == right) && !RainbowMath.float_nf_commas) {
            return RainbowMath.float_nf.format(num);
        }

        RainbowMath.float_nf = NumberFormat.getInstance();
        RainbowMath.float_nf.setGroupingUsed(false);
        RainbowMath.float_nf_commas = false;

        if (left != 0) {
            RainbowMath.float_nf.setMinimumIntegerDigits(left);
        }
        if (right != 0) {
            RainbowMath.float_nf.setMinimumFractionDigits(right);
            RainbowMath.float_nf.setMaximumFractionDigits(right);
        }
        RainbowMath.float_nf_left = left;
        RainbowMath.float_nf_right = right;
        return RainbowMath.float_nf.format(num);
    }

    public static String[] nfc( float num[],  int right) {
         String formatted[] = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = RainbowMath.nfc(num[i], right);
        }
        return formatted;
    }

    public static String nfc( float num,  int right) {
        if ((RainbowMath.float_nf != null) && (RainbowMath.float_nf_left == 0) && (RainbowMath.float_nf_right == right) && RainbowMath.float_nf_commas) {
            return RainbowMath.float_nf.format(num);
        }

        RainbowMath.float_nf = NumberFormat.getInstance();
        RainbowMath.float_nf.setGroupingUsed(true);
        RainbowMath.float_nf_commas = true;

        if (right != 0) {
            RainbowMath.float_nf.setMinimumFractionDigits(right);
            RainbowMath.float_nf.setMaximumFractionDigits(right);
        }
        RainbowMath.float_nf_left = 0;
        RainbowMath.float_nf_right = right;
        return RainbowMath.float_nf.format(num);
    }

    // ////////////////////////////////////////////////////////////
    // ARRAY UTILITIES

    /**
     * Number formatter that takes into account whether the number has a sign
     * (positive, negative, etc) in front of it.
     */
    public static String[] nfs( float num[],  int left,  int right) {
         String formatted[] = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = RainbowMath.nfs(num[i], left, right);
        }
        return formatted;
    }

    public static String nfs( float num,  int left,  int right) {
        return (num < 0) ? RainbowMath.nf(num, left, right) : (' ' + RainbowMath.nf(num, left, right));
    }

    public static String[] nfp( float num[],  int left,  int right) {
         String formatted[] = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = RainbowMath.nfp(num[i], left, right);
        }
        return formatted;
    }

    //

    public static String nfp( float num,  int left,  int right) {
        return (num < 0) ? RainbowMath.nf(num, left, right) : ('+' + RainbowMath.nf(num, left, right));
    }

    /**
     * Convert a byte into a two digit hex string.
     */
    static public String hex( byte what) {
        return RainbowMath.hex(what, 2);
    }

    /**
     * Format an integer as a hex string using the specified number of digits.
     *
     * @param what   the value to format
     * @param digits the number of digits (maximum 8)
     * @return a String object with the formatted values
     */
    static public String hex( int what, int digits) {
         String stuff = Integer.toHexString(what).toUpperCase();
        if (digits > 8) {
            digits = 8;
        }

         int length = stuff.length();
        if (length > digits) {
            return stuff.substring(length - digits);

        } else if (length < digits) {
            return "00000000".substring(8 - (digits - length)) + stuff;
        }
        return stuff;
    }

    /**
     * Convert a Unicode character into a four digit hex string.
     */
    static public String hex( char what) {
        return RainbowMath.hex(what, 4);
    }

    /**
     * Convert an integer into an eight digit hex string.
     */
    static public String hex( int what) {
        return RainbowMath.hex(what, 8);
    }

    static public int unhex( String what) {
        // has to parse as a Long so that it'll work for numbers bigger than
        // 2^31
        return (int) (Long.parseLong(what, 16));
    }

    /**
     * Returns a String that contains the binary value of a byte. The returned
     * value will always have 8 digits.
     */
    static public String binary( byte what) {
        return RainbowMath.binary(what, 8);
    }

    /**
     * Returns a String that contains the binary value of an int. The digits
     * parameter determines how many digits will be used.
     */
    static public String binary( int what, int digits) {
         String stuff = Integer.toBinaryString(what);
        if (digits > 32) {
            digits = 32;
        }

         int length = stuff.length();
        if (length > digits) {
            return stuff.substring(length - digits);

        } else if (length < digits) {
             int offset = 32 - (digits - length);
            return "00000000000000000000000000000000".substring(offset) + stuff;
        }
        return stuff;
    }

    /**
     * Returns a String that contains the binary value of a char. The returned
     * value will always have 16 digits because chars are two bytes long.
     */
    static public String binary( char what) {
        return RainbowMath.binary(what, 16);
    }

    /**
     * Returns a String that contains the binary value of an int. The length
     * depends on the size of the number itself. If you want a specific number
     * of digits use binary(int what, int digits) to specify how many.
     */
    static public String binary( int what) {
        return RainbowMath.binary(what, 32);
    }

    /**
     * Unpack a binary String into an int. i.e. unbinary("00001000") would
     * return 8.
     */
    static public int unbinary( String what) {
        return Integer.parseInt(what, 2);
    }

    /**
     * Return a random number in the range [howsmall, howbig).
     * <p/>
     * The number returned will range from 'howsmall' up to (but not including
     * 'howbig'.
     * <p/>
     * If howsmall is >= howbig, howsmall will be returned, meaning that
     * random(5, 5) will return 5 (useful) and random(7, 4) will return 7 (not
     * useful.. better idea?)
     */
    public static float random( float howsmall,  float howbig) {
        if (howsmall >= howbig) {
            return howsmall;
        }
         float diff = howbig - howsmall;
        return random(diff) + howsmall;
    }

    /**
     * Return a random number in the range [0, howbig).
     * <p/>
     * The number returned will range from zero up to (but not including)
     * 'howbig'.
     */
    public static float random( float howbig) {
        // for some reason (rounding error?) Math.random() * 3
        // can sometimes return '3' (once in ~30 million tries)
        // so a check was added to avoid the inclusion of 'howbig'

        // avoid an infinite loop
        if (howbig == 0) {
            return 0;
        }

        // internal random number object
        if (internalRandom == null) {
            internalRandom = new Random();
        }

        float value = 0;
        do {
            // value = (float)Math.random() * howbig;
            value = internalRandom.nextFloat() * howbig;
        } while (value == howbig);
        return value;
    }

    /**
     * Return a random number in the range [0, howbig).
     * <p/>
     * The number returned will range from zero up to (but not including)
     * 'howbig'.
     */
    public static int random( int howbig) {
        // for some reason (rounding error?) Math.random() * 3
        // can sometimes return '3' (once in ~30 million tries)
        // so a check was added to avoid the inclusion of 'howbig'

        // avoid an infinite loop
        if (howbig == 0) {
            return 0;
        }

        // internal random number object
        if (internalRandom == null) {
            internalRandom = new Random();
        }

        return internalRandom.nextInt(howbig);
    }

    public static void randomSeed( long what) {
        // internal random number object
        if (internalRandom == null) {
            internalRandom = new Random();
        }
        internalRandom.setSeed(what);
    }

    /**
     * Computes the Perlin noise function value at point x.
     */
    public static float noise( float x) {
        // is this legit? it's a dumb way to do it (but repair it later)
        return noise(x, 0f, 0f);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

    /**
     * Computes the Perlin noise function value at x, y, z.
     */
    public static float noise(float x, float y, float z) {
        if (perlin == null) {
            if (perlinRandom == null) {
                perlinRandom = new Random();
            }
            perlin = new float[RainbowMath.PERLIN_SIZE + 1];
            for (int i = 0; i < (RainbowMath.PERLIN_SIZE + 1); i++) {
                perlin[i] = perlinRandom.nextFloat(); // (float)Math.random();
            }
            // [toxi 031112]
            // noise broke due to recent change of cos table in PGraphics
            // this will take care of it
            perlin_cosTable = RainbowGraphics.cosLUT;
            perlin_TWOPI = perlin_PI = RainbowGraphics.SINCOS_LENGTH;
            perlin_PI >>= 1;
        }

        if (x < 0) {
            x = -x;
        }
        if (y < 0) {
            y = -y;
        }
        if (z < 0) {
            z = -z;
        }

        int xi = (int) x, yi = (int) y, zi = (int) z;
        float xf = (x - xi);
        float yf = (y - yi);
        float zf = (z - zi);
        float rxf, ryf;

        float r = 0;
        float ampl = 0.5f;

        float n1, n2, n3;

        for (int i = 0; i < perlin_octaves; i++) {
            int of = xi + (yi << RainbowMath.PERLIN_YWRAPB) + (zi << RainbowMath.PERLIN_ZWRAPB);

            rxf = noise_fsc(xf);
            ryf = noise_fsc(yf);

            n1 = perlin[of & RainbowMath.PERLIN_SIZE];
            n1 += rxf * (perlin[(of + 1) & RainbowMath.PERLIN_SIZE] - n1);
            n2 = perlin[(of + RainbowMath.PERLIN_YWRAP) & RainbowMath.PERLIN_SIZE];
            n2 += rxf * (perlin[(of + RainbowMath.PERLIN_YWRAP + 1) & RainbowMath.PERLIN_SIZE] - n2);
            n1 += ryf * (n2 - n1);

            of += RainbowMath.PERLIN_ZWRAP;
            n2 = perlin[of & RainbowMath.PERLIN_SIZE];
            n2 += rxf * (perlin[(of + 1) & RainbowMath.PERLIN_SIZE] - n2);
            n3 = perlin[(of + RainbowMath.PERLIN_YWRAP) & RainbowMath.PERLIN_SIZE];
            n3 += rxf * (perlin[(of + RainbowMath.PERLIN_YWRAP + 1) & RainbowMath.PERLIN_SIZE] - n3);
            n2 += ryf * (n3 - n2);

            n1 += noise_fsc(zf) * (n2 - n1);

            r += n1 * ampl;
            ampl *= perlin_amp_falloff;
            xi <<= 1;
            xf *= 2;
            yi <<= 1;
            yf *= 2;
            zi <<= 1;
            zf *= 2;

            if (xf >= 1.0f) {
                xi++;
                xf--;
            }
            if (yf >= 1.0f) {
                yi++;
                yf--;
            }
            if (zf >= 1.0f) {
                zi++;
                zf--;
            }
        }
        return r;
    }

    // [toxi 031112]
    // now adjusts to the size of the cosLUT used via
    // the new variables, defined above
    private static float noise_fsc( float i) {
        // using bagel's cosine table instead
        return 0.5f * (1.0f - perlin_cosTable[(int) (i * perlin_PI) % perlin_TWOPI]);
    }

    /**
     * Computes the Perlin noise function value at the point x, y.
     */
    public static float noise( float x,  float y) {
        return noise(x, y, 0f);
    }

    public static void noiseSeed( long what) {
        if (perlinRandom == null) {
            perlinRandom = new Random();
        }
        perlinRandom.setSeed(what);
        // force table reset after changing the random number seed [0122]
        perlin = null;
    }

    public void noiseDetail( int lod) {
        if (lod > 0) {
            perlin_octaves = lod;
        }
    }

    // ////////////////////////////////////////////////////////////

    // INT NUMBER FORMATTING

    public void noiseDetail( int lod,  float falloff) {
        if (lod > 0) {
            perlin_octaves = lod;
        }
        if (falloff > 0) {
            perlin_amp_falloff = falloff;
        }
    }
}
