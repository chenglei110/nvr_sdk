package com.tuya.smart.iotos.nvr_gw_demo.capture;

import android.content.Context;

import com.tuya.smart.iotos.nvr_sdk.MediaFrame;
import com.tuya.smart.iotos.nvr_sdk.TuyaIoTManager;
import com.tuya.smart.iotos.nvr_sdk.api.Common;
import com.tuya.smart.iotos.nvr_sdk.api.IMediaTransManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileAudioCapture {

    private final static int AUDIO_FRAME_SIZE = 640;
    private final static int AUDIO_FPS = 25;


    private InputStream fis;

    private byte[] pcmBuffer;

    IMediaTransManager transManager;

    Context context;

    public FileAudioCapture(Context context) {

        this.context = context;
        try {
            fis = context.getAssets().open("rawfiles/jupiter_8k_16bit_mono.raw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pcmBuffer = new byte[AUDIO_FRAME_SIZE];

        transManager = TuyaIoTManager.getInstance().getMediaTransManager();
    }

    public void startFileCapture() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        int size = fis.read(pcmBuffer);
                        if (size < AUDIO_FRAME_SIZE) {
                            fis.reset();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {

                    }
                    //Log.d("audio", "len: " + pcmBuffer.length);
                    transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, 0, pcmBuffer);
                    transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, 0, pcmBuffer);
                    transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, 0, pcmBuffer);
                    transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, 0, pcmBuffer);

                    // 测试回放
                    if (enable == 1) {
                        MediaFrame mediaFrame = new MediaFrame(MediaFrame.E_AUDIO_FRAME, 0, 0, pcmBuffer);
                        transManager.playbackSendFrame(chn, client, mediaFrame);
                    }

                    int frameRate = AUDIO_FPS;
                    int sleepTick = 1000 / frameRate;

                    try {
                        Thread.sleep(sleepTick);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stopFileCapture() {
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private volatile int enable = 0;
    private volatile int chn = 0;
    private volatile int client = 0;

    public int playbackCtrl(int chn, int client, int enable) {

        this.enable = enable;
        this.chn = chn;
        this.client = client;

        return 0;
    }

}
