package com.digitelektro.bpm;

import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity   {
    private boolean recording = true;
    int SampleRate = 8000;
    int BufferSize = AudioRecord.getMinBufferSize(SampleRate, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
    byte max = 0;
    AudioRecord recorder;
    BPM bpm;
    private Thread recordingThread = null;

    private TextView bpmtext;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bpmtext = (TextView) findViewById(R.id.textBPM);
        resetButton = (Button) findViewById(R.id.btnReset);
        resetButton.setOnTouchListener(ResetButtonListener);
        bpm = new BPM(SampleRate);
        bpm.setBPMListener(bpmListener);
        startrec();
    }

    public OnTouchListener ResetButtonListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_UP:
                        bpm.ResetBPM();
                        bpmtext.setText("0 BPM");
                    return true;
            }
            return false;
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }



    public void startrec()
    {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, BufferSize);
        recorder.startRecording();
        recordingThread = new Thread(new Runnable() {
            public void run() {
                AudioRecordThread();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void AudioRecordThread()
    {
        byte [] data = new byte[BufferSize];
        int Readed;
        while(recording)
        {
            Readed = recorder.read(data, 0, BufferSize);
            for(int i = 0; i < Readed; i=i+2)
            {
                //byte [] bytes = {data[BufferSize-1-i], data[BufferSize-1-i-1]};
                byte [] bytes = {data[i+1], data[i]};
                bpm.AddSample(GetFloatFromByte(bytes));
            }
        }
        recorder.stop();
        recorder.release();

    }

    private float GetFloatFromByte (byte [] bytes)	//Convert Byte to float
    {
        short int16 = (short)(((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF));
        float data = int16 / 65536.0f;
        return data;
    }


    public BPM.BPMListener bpmListener = new BPM.BPMListener() {
        public void onBPM(float BPM)
        {
            final String mytext = String.format( "%.2f BPM", BPM );
            //Run on main thread
            runOnUiThread(new Runnable() {
                public void run() {
                    bpmtext.setText(String.valueOf(mytext));    //Valueof, get variable from another thread
                }
            });
        }
    };
}
