package com.digitelektro.bpm;

/**
 * Created by digitelektro on 2015.07.25..
 */
public class BiquadFilter {

    // coefficients
    private double a0;
    private double a1;
    private double a2;
    private double a3;
    private double a4;

    // state
    private float x1;
    private float x2;
    private float y1;
    private float y2;


    public BiquadFilter(double aa0, double aa1, double aa2, double b0, double b1, double b2)
    {
        a0 = b0/aa0;
        a1 = b1/aa0;
        a2 = b2/aa0;
        a3 = aa1/aa0;
        a4 = aa2/aa0;

        // zero initial samples
        x1 = x2 = 0;
        y1 = y2 = 0;
    }

    private void SetCoefficients(double aa0, double aa1, double aa2, double b0, double b1, double b2)
    {
        // precompute the coefficients
        a0 = b0/aa0;
        a1 = b1/aa0;
        a2 = b2/aa0;
        a3 = aa1/aa0;
        a4 = aa2/aa0;
    }

    public static BiquadFilter SetLowPassFilter(float sampleRate, float cutoffFrequency, float q)
    {
        // H(s) = 1 / (s^2 + s/Q + 1)
        double w0 = 2 * Math.PI * cutoffFrequency / sampleRate;
        double cosw0 = Math.cos(w0);
        double alpha = Math.sin(w0) / (2 * q);

        double b0 = (1 - cosw0) / 2;
        double b1 = 1 - cosw0;
        double b2 = (1 - cosw0) / 2;
        double aa0 = 1 + alpha;
        double aa1 = -2 * cosw0;
        double aa2 = 1 - alpha;
        return new BiquadFilter(aa0,aa1,aa2,b0,b1,b2);
    }

    public float Transform(float inSample)
    {
        // compute result
        double result = a0 * inSample + a1 * x1 + a2 * x2 - a3 * y1 - a4 * y2;

        // shift x1 to x2, sample to x1
        x2 = x1;
        x1 = inSample;

        // shift y1 to y2, result to y1
        y2 = y1;
        y1 = (float)result;

        return y1;
    }
}
