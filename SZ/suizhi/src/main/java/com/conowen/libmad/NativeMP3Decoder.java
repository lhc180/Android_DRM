package com.conowen.libmad;

public class NativeMP3Decoder {

    static {
        System.loadLibrary("mad");
    }

    public NativeMP3Decoder() {

    }

    public native int initAudioPlayer(String file, int startAddr);

    public native int initAudioPlayerKey(String file, int startAddr, String key);

    public native int getAudioBuf(short[] audioBuffer, int numSamples);

    public native void closeAduioFile();

    public native int getAudioSamplerate();

    public native void setPosition(int position);

    public native int getLength();

    public native int getPosition();

    public native int getDuration();

}
