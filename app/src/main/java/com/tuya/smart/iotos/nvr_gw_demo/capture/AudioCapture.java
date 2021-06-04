package com.tuya.smart.iotos.nvr_gw_demo.capture;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.tuya.smart.iotos.nvr_sdk.TuyaIoTManager;
import com.tuya.smart.iotos.nvr_sdk.api.Common;
import com.tuya.smart.iotos.nvr_sdk.api.IMediaTransManager;

public class AudioCapture {

    private AudioRecord audioRecord;
    //只是推送数据流
    private boolean isAudioPush = false;

    private int pcmBufferSize;
    private byte[] pcmBuffer;

    IMediaTransManager transManager;

    public AudioCapture() {

        float captureInterval = 1.0f / 25;
        pcmBufferSize = (int )(8000 * 16 * 1 * captureInterval) / 8;
        //必须要有对应的权限 Manifest.permission.RECORD_AUDIO
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, pcmBufferSize);

        pcmBuffer = new byte[pcmBufferSize];
        transManager = TuyaIoTManager.getInstance().getMediaTransManager();
    }

    public void startCapture() {
        if (audioRecord == null) {
            return;
        }
        if (!isAudioPush) {
            isAudioPush = true;
        }
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
            audioRecord.startRecording();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isAudioPush && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                        int len = audioRecord.read(pcmBuffer, 0, pcmBuffer.length);
                        if (len > 0) {
                            //trans audio stream
                            transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, 0, pcmBuffer);
                        }
                    }
                }
            }).start();
        }
    }

    public void stopCapture() {
        audioRecord.stop();
        isAudioPush = false;
    }

}
