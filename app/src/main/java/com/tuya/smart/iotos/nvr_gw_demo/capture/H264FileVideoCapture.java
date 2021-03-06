package com.tuya.smart.iotos.nvr_gw_demo.capture;

import android.content.Context;
import android.util.Log;

import com.tuya.smart.iotos.nvr_sdk.MediaFrame;
import com.tuya.smart.iotos.nvr_sdk.TuyaIoTManager;
import com.tuya.smart.iotos.nvr_sdk.api.Common;
import com.tuya.smart.iotos.nvr_sdk.api.IMediaTransManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class H264FileVideoCapture {

    enum FrameType {
        IDR,
        P,
        PPS,
        SPS
    }

    class Frame {
        FrameType type;
        byte[] data;
    }

    private final static int VIDEO_BUF_SIZE = 1024 * 10;

    private byte[] SPS;

    private byte[] PPS;

    private File videoFile;

    private String videoPath;

    private InputStream videoFis;

    private BufferedInputStream bufferedInputStream;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    IMediaTransManager transManager;

    Context context;

    public H264FileVideoCapture(Context context,String file) {

        this.context = context;
        transManager = TuyaIoTManager.getInstance().getMediaTransManager();

//        videoBuffer = new byte[VIDEO_BUF_SIZE];

//        videoFile = new File(videoPath);
//        infoFile = new File(videoInfoPath);

        try {
            videoFis = context.getAssets().open(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getData(byte[] buffer) {
        try {
            int len = bufferedInputStream.read(buffer, 0, 1024);
            if (len == 0) {
                bufferedInputStream.reset();
                len = bufferedInputStream.read(buffer, 0, 1024);
            }
            return len;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void dealType(int type, Frame frame) {
        if ((type & 31) == 5) {
            frame.type = FrameType.IDR;
        } else if ((type & 31) == 7) {
            frame.type = FrameType.SPS;
            //SPS
        } else if ((type & 31) == 8) {
            //PPS
            frame.type = FrameType.PPS;
        } else if ((type & 31) == 1) {
            frame.type = FrameType.P;
        }
    }

    private static int isDiv(byte[] data, int start, Frame frame) {
        int type;
        int ret;
        int i = 0;
        for (; i + start + 4 < data.length; i++) {
            int idx = start + i;
            if (data[idx] == 0 && data[idx + 1] == 0 && data[idx + 2] == 1) {
                if (i == 0){
                    i+=3;
                    type = data[idx + 3];
                    dealType(type, frame);
                    continue;
                }
                return idx;
            }
            if (data[idx] == 0 && data[idx + 1] == 0 && data[idx + 2] == 0 && data[idx + 3] == 1) {
                if (i == 0){
                    i+=4;
                    type = data[idx + 4];
                    dealType(type, frame);
                    continue;
                }
                return idx;
            }
        }
        ret = i - data.length;


        return ret;
    }


    private Frame takeData() throws IOException {
        Frame frame = new Frame();
        int pos = 0;
        byte[] buffer = new byte[VIDEO_BUF_SIZE];
        while (true) {
            int len = bufferedInputStream.read(buffer, 0, VIDEO_BUF_SIZE);
            if (len <= 0) {
                videoFis.reset();
                len = bufferedInputStream.read(buffer, 0, VIDEO_BUF_SIZE);
            }
            byteArrayOutputStream.write(buffer, 0, len);
            byte[] data = byteArrayOutputStream.toByteArray();
            if ((pos = isDiv(data, pos, frame)) > 0) {
                frame.data = new byte[pos];
                System.arraycopy(data, 0, frame.data, 0, pos);
                byteArrayOutputStream.reset();
                byteArrayOutputStream.write(data, pos, data.length - pos);
                break;
            } else {
                pos += data.length;
            }
        }
        return frame;
    }

    private Frame takeFrame() {
        try {
            while (true) {
                Frame frame = takeData();
                if (frame != null) {
                    if (frame.type == FrameType.SPS) {
                        SPS = frame.data;
                        continue;
                    } else if (frame.type == FrameType.PPS) {
                        PPS = frame.data;
                        continue;
                    } else {
                        if (frame.type == FrameType.IDR) {
                            if (SPS != null && PPS != null) {
                                byte[] data = new byte[frame.data.length + SPS.length + PPS.length];
                                System.arraycopy(SPS, 0, data, 0, SPS.length);
                                System.arraycopy(PPS, 0, data, SPS.length, PPS.length);
                                System.arraycopy(frame.data, 0, data, SPS.length + PPS.length, frame.data.length);
                                frame.data = data;
                            }
                        }
                    }
                }

                if (frame == null)
                    Log.d("FileTest", "frame null");
                else if (frame.data == null)
                    Log.d("FileTest", "frame data null");
                else
                    Log.d("FileTest", "frame data len " + frame.data.length);

                return frame;
            }

        } catch (IOException e1) {
//            e1.printStackTrace();
        }
        return null;
    }

    public void startVideoCapture(int streamType) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                bufferedInputStream = new BufferedInputStream(videoFis, 5 * 1024 * 1024);

                int frameRate = 30;
                int sleepTick = 1000 / frameRate;
                Frame frame = null;

                while (true) {
                    frame = takeFrame();
                    //Log.d("FileTest", "frame type " + frame.type + " frame len: " + frame.data.length);
                    if (frame == null || frame.type == null) {
                        Log.d("FileTest", "frame error");
                        continue;
                    }
                    if (frame.type == FrameType.IDR) {
                        // ?????????
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE1, streamType, Common.NAL_TYPE.NAL_TYPE_IDR, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE2, streamType, Common.NAL_TYPE.NAL_TYPE_IDR, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE3, streamType, Common.NAL_TYPE.NAL_TYPE_IDR, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE4, streamType, Common.NAL_TYPE.NAL_TYPE_IDR, frame.data);
                        // ?????????
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.NAL_TYPE.NAL_TYPE_IDR, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.NAL_TYPE.NAL_TYPE_IDR, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.NAL_TYPE.NAL_TYPE_IDR, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.NAL_TYPE.NAL_TYPE_IDR, frame.data);

                        // ????????????
                        if (enable == 1) {
                            MediaFrame mediaFrame = new MediaFrame(MediaFrame.E_VIDEO_I_FRAME, 0, 0, frame.data);
                            transManager.playbackSendFrame(chn, client, mediaFrame);
//                            transManager.playbackSendFrame(0, 1, mediaFrame);
//                            transManager.playbackSendFrame(0, 2, mediaFrame);
//                            transManager.playbackSendFrame(0, 3, mediaFrame);
                        }

                    } else {
                        // ?????????
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE1, streamType, Common.NAL_TYPE.NAL_TYPE_PB, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE2, streamType, Common.NAL_TYPE.NAL_TYPE_PB, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE3, streamType, Common.NAL_TYPE.NAL_TYPE_PB, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE4, streamType, Common.NAL_TYPE.NAL_TYPE_PB, frame.data);

                        // ?????????
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.NAL_TYPE.NAL_TYPE_PB, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.NAL_TYPE.NAL_TYPE_PB, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.NAL_TYPE.NAL_TYPE_PB, frame.data);
                        transManager.pushMediaStream(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.NAL_TYPE.NAL_TYPE_PB, frame.data);

                        // ????????????
                        if (enable == 1) {
                            MediaFrame mediaFrame = new MediaFrame(MediaFrame.E_VIDEO_PB_FRAME, 0, 0, frame.data);
                            transManager.playbackSendFrame(chn, client, mediaFrame);
//                            transManager.playbackSendFrame(0, 1, mediaFrame);
//                            transManager.playbackSendFrame(0, 2, mediaFrame);
//                            transManager.playbackSendFrame(0, 3, mediaFrame);
                        }
                    }

                    try {
                        Thread.sleep(sleepTick);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        Log.d("FileTest", "startVideoCapture: ");
    }

    public void stopFileCapture() {

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
