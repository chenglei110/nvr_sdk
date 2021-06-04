package com.tuya.smart.iotos.nvr_gw_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;

import com.tuya.smart.aiipc.base.permission.PermissionUtil;
import com.tuya.smart.iotos.nvr_gw_demo.capture.FileAudioCapture;
import com.tuya.smart.iotos.nvr_gw_demo.capture.H264FileVideoCapture;
import com.tuya.smart.iotos.nvr_sdk.AudioTalkCallback;
import com.tuya.smart.iotos.nvr_sdk.DPEvent;
import com.tuya.smart.iotos.nvr_sdk.FwInfo;
import com.tuya.smart.iotos.nvr_sdk.GwInfraCallbacks;
import com.tuya.smart.iotos.nvr_sdk.IP2PEventCallback;
import com.tuya.smart.iotos.nvr_sdk.Log;
import com.tuya.smart.iotos.nvr_sdk.MiscDevCallbacks;
import com.tuya.smart.iotos.nvr_sdk.PlaybackControl;
import com.tuya.smart.iotos.nvr_sdk.PlaybackQuery;
import com.tuya.smart.iotos.nvr_sdk.RecvObjDP;
import com.tuya.smart.iotos.nvr_sdk.RecvRawDP;
import com.tuya.smart.iotos.nvr_sdk.TuyaIoTManager;
import com.tuya.smart.iotos.nvr_sdk.Z3ApsFrame;
import com.tuya.smart.iotos.nvr_sdk.Z3Desc;
import com.tuya.smart.iotos.nvr_sdk.Z3DevCallbacks;
import com.tuya.smart.iotos.nvr_sdk.api.Common;
import com.tuya.smart.iotos.nvr_sdk.api.IMediaTransManager;
import com.tuya.smart.iotos.nvr_sdk.api.IParamConfigManager;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TuyaIoTManager tuyaIoTManager;

    FileAudioCapture fileAudioCapture;
    H264FileVideoCapture h264FileMainVideoCapture;

    boolean gwInited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.get_shorturl).setOnClickListener(this::onClick);
        findViewById(R.id.start_trans).setOnClickListener(this::onClick);
        findViewById(R.id.reset).setOnClickListener(this::onClick);
        findViewById(R.id.cloud_storage).setOnClickListener(this::onClick);

        PermissionUtil.check(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO
        }, () -> {
            initSDK();
        });
    }

    private void registerCallbacks() {

        tuyaIoTManager = TuyaIoTManager.getInstance();

        GwInfraCallbacks gwInfraCallbacks = new GwInfraCallbacks() {

            @Override
            public int onGwUpgrade(String imgFile) {
                // 升级文件下好了，在这里进行升级动作
                return 0;
            }

            @Override
            public void onGwReboot() {

            }

            @Override
            public void onGwReset() {

            }

            @Override
            public String onGwFetchLocalLog(int pathLen) {
                return null;
            }

            @Override
            public void onGwLedControl(int iTime) {

            }

            @Override
            public int onGwObjDp(RecvObjDP dp) {

                Log.d(TAG, "gw device obj dp: " + dp.toString());

                tuyaIoTManager.sendDP(dp.cid, dp.dpEvent);

                return 0;
            }

            @Override
            public int onGwRawDp(RecvRawDP dp) {

                Log.d(TAG, "gw device raw dp: " + dp.toString());

                tuyaIoTManager.sendDP(dp.cid, dp.dpid, DPEvent.Type.PROP_RAW, dp.data);

                return 0;
            }

            @Override
            public int onGwActiveStatusChanged(String devId, int status) {

                Log.d(TAG, "gw device active stat: " + status);

                return 0;
            }

            @Override
            public int onGwOnlineStatusChanged(String devId, int status) {

                Log.d(TAG, "gw device online stat: " + status);

                return 0;
            }

            @Override
            public void onStartSuccess() {
                Log.d(TAG, "start sdk ok");
                gwInited = true;
            }

            @Override
            public void onStartFailure(int err) {
                Log.d(TAG, "start sdk failed");
            }
        };

        MiscDevCallbacks miscDevCallbacks = new MiscDevCallbacks() {
            @Override
            public int onMiscDevAdd(int permit, int timeout) {
                return 0;
            }

            @Override
            public int onMiscDevDel(String devId) {
                return 0;
            }

            @Override
            public int onMiscDevObjDp(RecvObjDP dp) {
                Log.d(TAG, "misc device obj dp: " + dp.toString());

                //tuyaIoTManager.sendDP(dp.cid, dp.dpEvent[0].dpid, dp.dpEvent[0].type, dp.dpEvent[0].value);
                // 或者
                tuyaIoTManager.sendDP(dp.cid, dp.dpEvent);

                return 0;
            }

            @Override
            public int onMiscDevRawDp(RecvRawDP dp) {

                Log.d(TAG, "misc device raw dp: " + dp.toString());

                tuyaIoTManager.sendDP(dp.cid, dp.dpid, DPEvent.Type.PROP_RAW, dp.data);

                return 0;
            }

            @Override
            public int onMiscDevBindIfm(String devId, int result) {
                return 0;
            }

            @Override
            public int onMiscDevUpgrade(String devId, FwInfo info) {
                Log.d(TAG, "misc device upgrade info callback");
                return 0;
            }

            @Override
            public void onMiscDevUpgradeDownloadStart() {

            }

            @Override
            public void onMiscDevUpgradeDownloadUpdate(int progress) {

            }

            @Override
            public void onMiscDevUpgradeDownloadFinish(boolean success) {

            }

            @Override
            public int onMiscDevReset(String devId) {
                return 0;
            }
        };

        Z3DevCallbacks z3DevCallbacks = new Z3DevCallbacks() {
            @Override
            public int onZ3DevActiveStateChanged(String id, int state) {

                Log.d(TAG, "zigbee device " + id + " active state: " + state);

                return 0;
            }

            @Override
            public int onZ3DevObjDp(RecvObjDP dp) {

                Log.d(TAG, "zigbee device obj dp: " + dp.toString());

                return 0;
            }

            @Override
            public int onZ3DevRawDp(RecvRawDP dp) {

                Log.d(TAG, "zigbee device raw dp: " + dp.toString());

                return 0;
            }

            @Override
            public int onZ3DevReadAttr(String id, int uddd) {

                Log.d(TAG, "zigbee device " + id + " read attr " + uddd);

                return 0;
            }

            @Override
            public int onZ3DevJoin(Z3Desc desc) {

                Log.d(TAG, "zigbee device join: " + desc.toString());

                return 0;
            }

            @Override
            public int onZ3DevLeave(String id) {

                Log.d(TAG, "zigbee device leave: " + id);

                return 0;
            }

            @Override
            public int onZ3DevZclReport(Z3ApsFrame frame) {

                Log.d(TAG, "zigbee device zcl report: " + frame.toString());

                return 0;
            }

            @Override
            public int onZ3DevAllZclReport(Z3ApsFrame frame) {

                Log.d(TAG, "zigbee device all zcl report: " + frame.toString());

                return 0;
            }
        };

        tuyaIoTManager.setGwInfraCallbacks(gwInfraCallbacks);
        tuyaIoTManager.setMiscDevCallbacks(miscDevCallbacks);
        tuyaIoTManager.setZ3DevCallbacks(z3DevCallbacks);
    }

    private void initSDK() {

        // 1. 设置log定向目录
        Log.init(this, "/sdcard/tuya/iot", 3);

        // 2. 实例化
        tuyaIoTManager = TuyaIoTManager.getInstance();
        tuyaIoTManager.setRegion(Common.DeviceRegion.REGION_CN);

        // 3. 设置回调
        registerCallbacks();

        // 4. 构建填充配置
        TuyaIoTManager.Config config = new TuyaIoTManager.Config();

        // 5. 用户根据实际情况填写配置
        config.mUuid = ""; // user todo
        config.mAuthKey = ""; // user todo
        config.mProductKey = ""; // user todo

        // 存储
        config.mStoragePath = "/sdcard/";
        // 网络
        config.mEthIfname = "wlan0";
        config.mVer = "1.0.0";
        // zigbee串口
        config.mTtyDevice = "/dev/ttyS4";
        config.mTtyBaudrate = 115200;
        config.mLogLevel = 4;

        config.mSubDevPID = ""; // user todo
        config.mTp = Common.DeviceType.GP_DEV_OTHER;
        config.mUddd = 0x2 << 24;
        config.mUddd2 = 0x1 << 31;
        config.mSubDevVer = "1.0.0";

        // 6. 启动网关
        tuyaIoTManager.IotGatewayStart(this, config);

        Log.d(TAG, "init iot sdk done");

        // 7. 若未配网激活，扫设备二维码激活【注意：网络需畅通】
    }

    // 配置NVR的视频子设备参数
    private void LoadParamConfig() {
        IParamConfigManager configManager = TuyaIoTManager.getInstance().getParamConfigManager();
        if (configManager == null) {
            Log.d(TAG, "gateway must be inited first");
            return ;
        }

        // 设备1：
        /**
         * 主码流参数配置
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 30);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_BIT_RATE, 1024000);

        /**
         * 子码流参数配置
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 15);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_BIT_RATE, 512000);

        /**
         * 音频流参数
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_CHANNEL_NUM, 1);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_SAMPLE_RATE, 8000);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_SAMPLE_BIT, 16);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_FRAME_RATE, 25);

        // 设备2：
        /**
         * 主码流参数配置
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 30);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_BIT_RATE, 1024000);

        /**
         * 子码流参数配置
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 15);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_BIT_RATE, 512000);

        /**
         * 音频流参数
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_CHANNEL_NUM, 1);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_SAMPLE_RATE, 8000);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_SAMPLE_BIT, 16);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE2, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_FRAME_RATE, 25);

        // 设备3：
        /**
         * 主码流参数配置
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 30);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_BIT_RATE, 1024000);

        /**
         * 子码流参数配置
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 15);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_BIT_RATE, 512000);

        /**
         * 音频流参数
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_CHANNEL_NUM, 1);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_SAMPLE_RATE, 8000);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_SAMPLE_BIT, 16);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE3, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_FRAME_RATE, 25);

        // 设备4：
        /**
         * 主码流参数配置
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 30);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_BIT_RATE, 1024000);

        /**
         * 子码流参数配置
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 15);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_V_2RD, Common.ParamKey.KEY_VIDEO_BIT_RATE, 512000);

        /**
         * 音频流参数
         * */
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_CHANNEL_NUM, 1);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_SAMPLE_RATE, 8000);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_SAMPLE_BIT, 16);
        configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE4, Common.DeviceChannelIndex.E_CHANNEL_A_MAIN, Common.ParamKey.KEY_AUDIO_FRAME_RATE, 25);
    }

    // 使能媒体传输
    private void enableMediaTrans() {
        IMediaTransManager transManager = TuyaIoTManager.getInstance().getMediaTransManager();
        if (transManager == null) {
            Log.d(TAG, "gateway must be inited first");
            return;
        }

        transManager.setP2PEventCallback(new IP2PEventCallback() {
            @Override
            public void onEvent(IMediaTransManager.P2PEvent event, Object value) {
                switch (event) {

                    case TRANS_PLAYBACK_QUERY_MONTH_SIMPLIFY_GW: // 回放查询
                        PlaybackQuery query_month = (PlaybackQuery)value;

                        Log.d(TAG, "pb query_month " + query_month.toString());

                        break;
                    case TRANS_PLAYBACK_QUERY_DAY_TS_GW: // 回放查询
                        PlaybackQuery query_day = (PlaybackQuery)value;

                        Log.d(TAG, "pb query_day " + query_day.toString());

                        break;
                    case TRANS_PLAYBACK_START_TS_GW: // 开始回放
                        PlaybackControl control_start = (PlaybackControl)value;

                        Log.d(TAG, "pb control_start " + control_start.toString());

                        h264FileMainVideoCapture.playbackCtrl(control_start.channel, control_start.idx, 1);
                        fileAudioCapture.playbackCtrl(control_start.channel, control_start.idx, 1);

                        break;
                    case TRANS_PLAYBACK_STOP_GW: // 停止回放
                        PlaybackControl control_stop = (PlaybackControl)value;

                        Log.d(TAG, "pb control_stop " + control_stop.toString());

                        h264FileMainVideoCapture.playbackCtrl(control_stop.channel, control_stop.idx, 0);
                        fileAudioCapture.playbackCtrl(control_stop.channel, control_stop.idx, 0);

                        // 主动停止时：可以调用
                        transManager.playbackSendFinish(control_stop.channel, control_stop.idx);

                        break;

                }
            }
        });

        // 用于对讲测试
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_SYSTEM, 8000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                640, AudioTrack.MODE_STREAM);
        if (audioTrack != null) {
            Log.d(TAG, "audio play");
        }

        transManager.addAudioTalkCallback(new AudioTalkCallback() {
            @Override
            public void onAudioTalkData(int subDevice, byte[] data) {
                Log.d(TAG, "subDevice: " + subDevice + " audio len: " + data.length);
                audioTrack.write(data, 0, data.length);
                audioTrack.play();
            }
        });

        transManager.startMultiMediaTrans(5);

        h264FileMainVideoCapture = new H264FileVideoCapture(this, "test.h264");
        fileAudioCapture = new FileAudioCapture(this);
        h264FileMainVideoCapture.startVideoCapture(Common.DeviceChannelIndex.E_CHANNEL_V_MAIN);
        fileAudioCapture.startFileCapture();
    }

    public void onClick(View v) {
        if (tuyaIoTManager == null) {
            return;
        }

        if (gwInited == false) {
            Log.d(TAG, "gateway must be inited first");
            return;
        }

        switch (v.getId()) {
            case R.id.start_trans:
                Log.d(TAG, "start trans");

                // 加载子设备音视频参数配置到SDK
                LoadParamConfig();

                // 开始推流
                enableMediaTrans();

                // 使能心跳，超时时间这里设置的是60秒
                tuyaIoTManager.enableSubDeviceHeartbeat(60);

                break;
            case R.id.get_shorturl:
                Log.d(TAG, "get short url");
                String url = tuyaIoTManager.getQrCode(null);
                // 可将此二维码通过二维码生成器展示在界面上，测试时可参考：https://cli.im/text
                Log.d(TAG, "get short url: " + url);
                break;
            case R.id.reset:
                Log.d(TAG, "resetting...");
                tuyaIoTManager.reset();
                break;
            case R.id.cloud_storage:
                IMediaTransManager transManager = TuyaIoTManager.getInstance().getMediaTransManager();
                int ret = transManager.startCloudStorage(0);
                Log.d(TAG, "startCloudStorage ret: " + ret);
                break;
        }
    }
}