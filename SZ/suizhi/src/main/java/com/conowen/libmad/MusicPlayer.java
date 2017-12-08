package com.conowen.libmad;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MusicPlayer {
    private static final String TAG = MusicPlayer.class.getSimpleName();

    private Thread mThread;
    private AudioTrack mAudioTrack;
    private NativeMP3Decoder MP3Decoder;

    private short[] audioBuffer;
    private int samplerate;
    private int mAudioMinBufSize;
    private int ret;

    private int duration = -1;
    private int length = -1;

    private boolean mThreadFlag;
    private Lock myLock;

    //private String mKey;

    private MusicPlayer() {
    }

    public MusicPlayer(String filepath) {

        this.init(filepath);
    }

    public MusicPlayer(String filepath, String key) {

        //this.mKey = key;

        this.init(filepath, key);
    }

//    public void setPath(String filepath) {
//
//        if (mAudioTrack == null) {
//            this.init(filepath, mKey);
//        }
//    }

    private void init(String filePath) {

        MP3Decoder = new NativeMP3Decoder();

        ret = MP3Decoder.initAudioPlayer(filePath, 0);

        if (ret == -1) {
            Log.i(TAG, "Couldn't open file '" + filePath + "'");
        } else {
            init();
        }
    }

    public void init(String filePath, String key) {

        myLock = new ReentrantLock();

        MP3Decoder = new NativeMP3Decoder();

        ret = MP3Decoder.initAudioPlayerKey(filePath, 0, key);

        if (ret == -1) {
            Log.i(TAG, "Couldn't open file '" + filePath + "'");
        } else {
            init();
        }
    }

    private void init() {
        mThreadFlag = true;

        initAudioPlayer();

        audioBuffer = new short[1024 * 1024];

        mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (mThreadFlag) {

                    if (myLock != null)
                        myLock.tryLock();

                    if (mAudioTrack != null
                            && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                            && MP3Decoder != null) {
                        // ****从libmad处获取data******/
                        MP3Decoder.getAudioBuf(audioBuffer, mAudioMinBufSize);

                        //往track中写数据
                        if (mAudioTrack == null)
                            initAudioTrack();
                        mAudioTrack.write(audioBuffer, 0, mAudioMinBufSize);

                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (myLock != null)
                        myLock.unlock();
                }
            }
        });
    }

    private void initAudioPlayer() {
        //目前只支持PCM8和PCM16精度的音频
        samplerate = MP3Decoder.getAudioSamplerate();
        samplerate = samplerate / 2;
        //DRMLog.e("采样率samplerate= " + samplerate);
        // 声音文件一秒钟buffer的大小
        mAudioMinBufSize = AudioTrack.getMinBufferSize(
                samplerate,     //每秒samplerate个点
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT); //一个采样点16比特-2个字节
        //DRMLog.e("缓冲bufferSize= " + mAudioMinBufSize);

        initAudioTrack();
    }

    private void initAudioTrack() {
        // STREAM_ALARM：警告声
        // STREAM_MUSIC：音乐声，例如music等
        // STREAM_RING：铃声
        // STREAM_SYSTEM：系统声音
        // STREAM_VOICE_CALL：电话声音
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC, // 指定在流的类型
                samplerate,// 设置音频数据的采样率
                AudioFormat.CHANNEL_OUT_STEREO, //双声道立体声
                AudioFormat.ENCODING_PCM_16BIT, // 设置音频数据块是8位还是16位
                mAudioMinBufSize,
                AudioTrack.MODE_STREAM);// 设置模式类型，在这里设置为流类型

        // AudioTrack中有MODE_STATIC和MODE_STREAM两种分类:
        // 1.STREAM方式表示由用户通过write方式把数据一次一次得写到audiotrack中。
        // 这种方式的缺点就是JAVA层和Native层不断地交换数据，效率损失较大。
        // 2.STATIC方式表示是一开始创建的时候，就把音频数据放到一个固定的buffer，然后直接传给audiotrack，
        // 后续就不用一次次得write了。AudioTrack会自己播放这个buffer中的数据。
        // 这种方法对于铃声等体积较小的文件比较合适。
    }

    /**
     * 播放
     */
    public void play() {
        if (mAudioTrack == null) return;
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {

            mThreadFlag = true;// 音频线程开始

            mAudioTrack.play();

            mThread.start();

        } else if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {

            mThreadFlag = true;// 音频线程开始

            mAudioTrack.play();
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mAudioTrack != null) {
            mAudioTrack.pause();
        }
    }

    /**
     * 停止
     */
    public void stop() {
        if (mAudioTrack != null && (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                || mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED)) {

            mAudioTrack.stop();

            this.reset();
        }
    }

    /**
     * 指定播放的位置（拖拽）
     *
     * @param position
     */
    public void seekTo(int position) {

        Log.d(TAG, "seekTo： " + position);

        if (MP3Decoder != null && duration != -1 && length != -1) {
            long l = length;
            long d = duration;
            long p = position;
            int pos = (int) (p * l / d);
            MP3Decoder.setPosition(pos);
        } else {
            long l = getLength();
            long d = getDuration();
            long p = position;
            int pos = (int) (p * l / d);
            MP3Decoder.setPosition(pos);
        }
    }

    /**
     * 是否正在播放(true,false)
     *
     * @return
     */
    public boolean isPlaying() {
        return mAudioTrack != null && (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING);
    }

    /**
     * 重置播放（点击播放下一首时，需要把之前的的音乐重置一下类似清空。）
     */
    public void reset() {
        mThreadFlag = false;// 音频线程暂停
        length = -1; // add 20170419
        duration = -1;

        // 关闭并释放资源,update20170622,原第233行
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }

        if (MP3Decoder != null) {
            MP3Decoder.closeAduioFile();
            MP3Decoder = null;
        }
    }

    /**
     * 总进度时间
     *
     * @return
     */
    public int getDuration() {
        if (MP3Decoder != null && duration == -1) {
            duration = MP3Decoder.getDuration();
        }
        return duration;
    }

    /**
     * 文件大小
     *
     * @return
     */
    public int getLength() {
        if (MP3Decoder != null && length == -1) {
            length = MP3Decoder.getLength();
        }
        //DRMLog.e("music-length: "+length);
        return length;
    }

    /**
     * 当前播放的时间
     *
     * @return
     */
    public int getCurrentPosition() {
        int position = 0;
        if (MP3Decoder != null) {
            long l = this.getLength();
            long d = this.getDuration();
            long p = MP3Decoder.getPosition();
            //DRMLog.e("music-getPosition: "+p);
            position = (int) (p * d / l);
        }
        return position;
    }

    /**
     * 释放资源（点击停止之后释放资源）
     */
    public void release() {

        mThreadFlag = false;
        duration = -1;
        length = -1;

        if (myLock != null)
            myLock.lock();

        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }

        if (MP3Decoder != null) {
            MP3Decoder.closeAduioFile();
            MP3Decoder = null;
        }
        if (myLock != null)
            myLock.unlock();
    }

}
