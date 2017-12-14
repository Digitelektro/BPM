package com.digitelektro.bpm;

import java.util.ArrayList;

/**
 * Created by digitelektro on 2015.07.25..
 */
public class BPM {

    int minBPM = 100;
    int maxBPM = 150;
    private int WindowStart;
    private int WindowEnd;
    private int DecimateBy;
    private int SampleRate;
    private int DecimateSampleRate = 8000;
    private float DecimatedSampleRate;
    private float [] acorr;
    private float[] SamplesBuffer;
    private int SampleCounter = 0;
    private int DecimateCounter = 1;

    private BiquadFilter DecimateFilter;
    private BiquadFilter CorellationFilter;

    //Event interface
    public interface BPMListener
    {
        void onBPM(float BPM);
    }

    ArrayList<BPMListener> BPMlisteners = new ArrayList<BPMListener> ();
    public void setBPMListener (BPMListener listener)
    {
        // Store the listener object
        this.BPMlisteners.add(listener);
    }


    public BPM(int SampleRate)
    {
        this.SampleRate = SampleRate;
        DecimateBy = SampleRate / DecimateSampleRate;
        DecimatedSampleRate = SampleRate / DecimateBy;
        WindowEnd = (int)(60.0f / (float)minBPM * (DecimatedSampleRate));
        WindowStart = (int)(60.0f / (float)maxBPM * (DecimatedSampleRate));
        acorr = new float[WindowEnd];
        SamplesBuffer = new float[WindowEnd * 3];
        DecimateFilter = BiquadFilter.SetLowPassFilter(SampleRate, DecimatedSampleRate / 2, 1);
        CorellationFilter = BiquadFilter.SetLowPassFilter(DecimatedSampleRate, 15, 1);
    }


    public void AddSample(float Sample)
    {
        Sample = DecimateFilter.Transform(Sample);
        if (DecimateCounter == DecimateBy)
        {
            DecimateCounter = 0;
            SamplesBuffer[SampleCounter] = Sample;
            SampleCounter++;
        }
        DecimateCounter++;
        if(SampleCounter == SamplesBuffer.length)
        {
            SampleCounter = 0;
            AutoCorellation(SamplesBuffer);
        }
    }

    public void RemoveBias()
    {
        float minval = 1e12f; // arbitrary large number
        for (int i = WindowStart; i < WindowEnd; i++)
        {
            if (acorr[i] < minval)
                minval = acorr[i];
        }
        for (int i = WindowStart; i < WindowEnd; i++)
        {
            acorr[i] -= minval;
        }
    }

    private void AutoCorellation(float[] Buffer)
    {
        float sum = 0;
        for(int i = 0; i < Buffer.length; i++)
        {
            Buffer[i] = Math.abs(Buffer[i]);
        }

        for (int offs = WindowStart; offs < WindowEnd; offs++)
        {
            sum = 0;
            for (int i = 0; i < Buffer.length - WindowEnd; i++)
            {
                sum += Buffer[i] * Buffer[i + offs]; 
            }
            acorr[offs] += sum;

        }
        int pos = 0;
        float max = 0;

        RemoveBias();

        for (int i = WindowStart; i < WindowEnd; i++)
        {
            if (acorr[i] > max)
            {
                max = acorr[i];
                pos = i;
            }
        }
        float bpm = DecimatedSampleRate / (float)pos * 60.0f;
        for (BPMListener listener : BPMlisteners)
        {
            listener.onBPM(bpm);
        }
    }

    public void ResetBPM()
    {
        for (int i = 0; i < acorr.length; i++)
        {
            acorr[i] = 0;
        }
    }

}
