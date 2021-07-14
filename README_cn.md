[English](./README.md) | 简体中文

# Tuya Android Device NVR+Gateway SDK
涂鸦安卓设备端NVR+网关SDK demo

## 介绍

Tuya安卓设备端NVR+网关SDK是一套融合了网关、日志系统以及ota升级等功能的开发套件，开发者可以基于该sdk实现入网、固件和apk升级等操作。提供标准安卓gradle依赖以及对应的说明文档，覆盖4.4及以上的安卓系统。


## 如何使用

## 集成SDK
1. 配置 build.gradle 文件 app 的 build.gradle 文件dependencies 里添加依赖库。

    implementation 'com.tuya.smart:tuyasmart-nvr_sdk:1.0.0'

2. 根目录下 build.gradle 文件添加源:

    maven { url "https://maven-other.tuya.com/repository/maven-releases/" }


## 网关控制

**网关控制实现了参数配置、回调注册、网关启动、入网、DP点下发/上报等功能。**

### SDK启动

#### 获取网关sdk实例

```java
tuyaIoTManager = TuyaIoTManager.getInstance();
```

#### 设置区域

```java
tuyaIoTManager.setRegion(Common.DeviceRegion.REGION_CN);
```

#### 注册回调函数

**按需实例化并注册回调函数到SDK中：**

```java
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
```

#### 网关参数配置

**根据实际情况填充配置参数。**

```java
    TuyaIoTManager.Config config = new TuyaIoTManager.Config();
    config.mUuid = ""; // User TODO
    config.mAuthKey = ""; // User TODO
    config.mProductKey = "";  // User TODO
    ...
```

#### 启动网关

**注意：必须等待网关启动成功后，即onStartSuccess回调后，才能进行后续操作。**

```java
    tuyaIoTManager.IotGatewayStart(this, config);
```

#### 获取短链生成二维码

**需在网络畅通情况下，否则调用此接口可能会卡住**

```java
    String short_url = tuyaIoTManager.getQrCode(null);
```

#### 配置需要接入NVR的4路IPC子设备参数

**根据实际情况，配置主子码流，音频相关的参数**

```java
    IParamConfigManager configManager = TuyaIoTManager.getInstance().getParamConfigManager();
    configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
    configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
    configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 30);
    configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
    configManager.setInt(Common.SubDeviceList.IPC_SUB_DEVICE1, Common.DeviceChannelIndex.E_CHANNEL_V_MAIN, Common.ParamKey.KEY_VIDEO_BIT_RATE, 1024000);
    ...
```

#### 设置对讲回调到SDK

**按需设置，如果没有对讲需求可以不设置**

```java
    IMediaTransManager transManager = TuyaIoTManager.getInstance().getMediaTransManager();  
    transManager.addAudioTalkCallback(new AudioTalkCallback() {
    ...
    }
```

#### 设置回放回调到SDK

**按需设置，如果没有回放需求可以不设置**

```java
    IMediaTransManager transManager = TuyaIoTManager.getInstance().getMediaTransManager();  
    transManager.setP2PEventCallback(new IP2PEventCallback() {
    ...
    }
```

#### 启动媒体传输服务

```java
    transManager.startMultiMediaTrans(5);
```

#### 启用媒体心跳服务

**注意：此心跳专用于4路nvr的ipc子设备**

```java
    tuyaIoTManager.enableSubDeviceHeartbeat(60);
```


## 配置说明

### 使用者需实例化此配置类：Config，说明如下：
```java
    /**
     * 初始化 SDK 的基本属性
     */
    public final static class Config {
        /**
         * 存储路径是系统存储 SDK 数据库等文件的路径，所指定的路径必须有可读写权限，缺省是当前路径
         */
        public String mStoragePath;

        /**
         * 镜像存储路径
         */
        public String mImgPath;

        /**
         * 网关与 Zigbee 模组的串口通讯所使用的串口设备，如 /dev/ttyS1
         */
        public String mTtyDevice;

        /**
         * 网关与 Zigbee 模组的串口通讯所使用的串口波特率，仅支持 115200 和 57600
         * 波特率为 115200 需要硬件流控
         * 波特率为 57600 不需要硬件流控
         */
        public int mTtyBaudrate;

        /**
         * 广播 UDP 报文的接口，用于有线网关局域网发现
         */
        public String mEthIfname;

        /**
         * wifi网卡名称
         */
        public String mWifiIfname;

        /**
         * AP 配网模式，指定网关 AP 的 SSID
         */
        public String mSsid;

        /**
         * AP 配网模式，指定网关 AP 的 Password
         */
        public String mPassword;

        /**
         * 网关应用的版本号，用于固件升级，必须是“x.x.x”的格式
         */
        public String mVer;

        /**
         * 设置 log 打印等级，开发阶段可开启 debug 模式
         */
        public int mLogLevel;

        /**
         * 设备 UUID
         */
        public String mUuid;

        /**
         * 设备 Authkey
         */
        public String mAuthKey;

        /**
         * 设备 PID
         */
        public String mProductKey;

        /**
         * sub-device type
         */
        public int mTp;

        /**
         * sub-device detail type
         */
        public int mUddd;

        /**
         * sub-device detail type
         */
        public int mUddd2;

        /**
         * sub-device product key
         */
        public String mSubDevPID;

        /**
         * sub-device version
         */
        public String mSubDevVer;
    }
```


## 函数说明

### 回调接口

#### 网关基础回调接口：GwInfraCallbacks
```java

public interface GwInfraCallbacks {

    /* ty_op_mode_t */
    int TY_OP_MODE_ADD_START    = 0;
    int TY_OP_MODE_ADD_STOP     = 1;
    int TY_OP_MODE_AP           = 2;
    int TY_OP_MODE_EZ           = 3;

    // ty_zigbee_status_t
    int TY_ZIGBEE_STATUS_POWERUP  = 0;
    int TY_ZIGBEE_STATUS_PAIRING  = 1;
    int TY_ZIGBEE_STATUS_NORMAL   = 2;

    // ty_gw_status_t
    int TY_GW_STATUS_UNREGISTERED = 0;
    int TY_GW_STATUS_REGISTERED   = 1;

    /**
     * 涂鸦应用升级的回调函数，第三方系统开发者在此回调函数中实现升级的功能。
     *
     * @param imgFile 网关固件文件所在的路径
     * @return 成功，返回0；失败，非0
     */
    int onGwUpgrade(String imgFile);

    /**
     * 重启涂鸦应用的回调函数。
     */
    void onGwReboot();

    /**
     * 重置网关的回调函数，第三方系统开发者在此回调函数中实现实现清空网关数据的功能。
     */
    void onGwReset();

    /**
     * 从涂鸦运营平台拉取设备日志的回调函数。
     *
     * @param pathLen 返回的path路径最大长度不能超过pathLen
     * @return 成功，返回路径
     */
    String onGwFetchLocalLog(int pathLen);

    /**
     * LED控制
     */
    void onGwLedControl(int iTime);

    /**
     * obj dp
     */
     int onGwObjDp(RecvObjDP dp);

    /**
     * raw dp
     */
     int onGwRawDp(RecvRawDP dp);

    /**
     * 网关成功绑定或解绑涂鸦云的通知回调函数，第三方系统开发者可以在此回调函数中根据网关成功绑定或解绑处理其特定业务。
     *
     * @param devId device id
     * @param status 网关激活状态。
     * 0：未绑定；
     * 1：已绑定。
     * @return 成功，返回0；失败，非0
     */
    int onGwActiveStatusChanged(String devId, int status);

    /**
     * 网关上下线状态变化的回调函数，第三方系统开发者可以在此回调函数中根据网关上线或下线处理其特定业务。
     *
     * @param devId device id
     * @param status 网关在线状态。
     * 0：离线；
     * 1：在线。
     * @return 成功，返回0；失败，非0
     */
    int onGwOnlineStatusChanged(String devId, int status);

    /**
     * 网关 start 成功
     */
    void onStartSuccess();

    /**
     * 网关 start 失败
     *
     * @param err 失败信息。START_ERROR_XXX
     */
    void onStartFailure(int err);
}
```

#### 其他设备接入回调接口：MiscDevCallbacks
```java
public interface MiscDevCallbacks {
    /**
     * 添加设备的回调函数。
     *
     * @param permit 允许 or 禁止子设备入网
     * @param timeout 允许配网的时间，单位为秒
     * @return 成功，返回0；失败，非0
     */
    int onMiscDevAdd(int permit, int timeout);

    /**
     * 删除设备的回调函数。
     *
     * @param devId 子设备的 MAC 地址
     * @return 成功，返回0；失败，非0
     */
    int onMiscDevDel(String devId);

    /**
     * 收到obj类型dp的回调。
     *
     * @param dp 参考 RecvObjDP说明
     * @return 成功，返回0；失败，非0
     */
    int onMiscDevObjDp(RecvObjDP dp);

    /**
     * 收到raw类型dp的回调。
     *
     * @param dp 参考 RecvRawDP说明
     * @return 成功，返回0；失败，非0
     */
    int onMiscDevRawDp(RecvRawDP dp);

    /**
     * 子设备绑定涂鸦云结果通知的回调函数。
     *
     * @param devId 子设备的 MAC 地址
     * @param result 绑定结果。
     * 0：绑定成功；
     * 1：绑定失败
     * @return 成功，返回0；失败，非0
     */
    int onMiscDevBindIfm(String devId, int result);

    /**
     * 通知子设备升级的回调函数。
     *
     * @param devId 子设备的 MAC 地址
     * @param info 固件信息
     * @return 成功，返回0；失败，非0
     */
    int onMiscDevUpgrade(String devId, FwInfo info);

    /**
     * 升级文件开始下载
     */
    void onMiscDevUpgradeDownloadStart();

    /**
     * 升级文件下载进度
     */
    void onMiscDevUpgradeDownloadUpdate(int progress);

    /**
     * sdk 下载升级文件下载完成触发此接口
     */
    void onMiscDevUpgradeDownloadFinish(boolean success);

    /**
     * 重置设备的回调函数。
     *
     * @param devId 子设备的 MAC 地址
     * @return 成功，返回0；失败，非0
     */
    int onMiscDevReset(String devId);

}
```

#### zigbee相关回调接口：Z3DevCallbacks
```java
public interface Z3DevCallbacks {
        /**
         * 所有 Zigbee 子设备成功绑定或解绑涂鸦云时的通知回调。
         *
         * @param id 子设备的 MAC 地址
         * @param state 子设备激活状态。
         * 0：解绑；
         * 1：绑定。
         * @return 成功，返回0；失败，非0
         */
        int onZ3DevActiveStateChanged(String id, int state);

        /**
         * 收到obj类型dp的回调。。
         *
         * @param dp 参考 RecvObjDP说明
         * @return 成功，返回0；失败，非0
         */
        int onZ3DevObjDp(RecvObjDP dp);

        /**
         * 收到raw类型dp的回调。
         *
         * @param dp 参考 RecvRawDP说明
         * @return 成功，返回0；失败，非0
         */
        int onZ3DevRawDp(RecvRawDP dp);

        /**
         * 读取子设备属性值的回调。
         *
         * @param id 子设备的 MAC 地址
         * @param uddd uddd
         * @return 成功，返回0；失败，非0
         */
        int onZ3DevReadAttr(String id, int uddd);

        /**
         * 用户处理的 Zigbee 子设备入网的回调。
         *
         * @param desc 参考 Z3Desc说明
         * @return 成功，返回0；失败，非0
         */
        int onZ3DevJoin(Z3Desc desc); // ty_z3_desc_s *desc

        /**
         * 用户处理的 Zigbee 子设备离网的回调。
         *
         * @param id 子设备的 MAC 地址
         * @return 成功，返回0；失败，非0
         */
        int onZ3DevLeave(String id);

        /**
         * 用户处理的 Zigbee 子设备状态上报的回调。
         *
         * @param frame 参考 Z3ApsFrame说明
         * @return 成功，返回0；失败，非0
         */
        int onZ3DevZclReport(Z3ApsFrame frame);

        /**
         * 用户处理的 Zigbee 子设备状态上报的回调。
         *
         * @param frame 参考 Z3ApsFrame说明
         * @return 成功，返回0；失败，非0
         */
        int onZ3DevAllZclReport(Z3ApsFrame frame);
}
```

#### 对讲相关回调接口：AudioTalkCallback
```java
public interface AudioTalkCallback {

    /**
     * 开启对讲后的数据回调接口
     * @param subDevice 子设备通道
     * @param data 对讲回调数据. 回调之后的数据格式为PCM
     * */
    void onAudioTalkData(int subDevice, byte[] data);
}
```

#### 回放相关回调接口：IP2PEventCallback
```java
public interface IP2PEventCallback {

    /**
     * p2p 事件回调
     * @param event 请求事件{@link IMediaTransManager.P2PEvent}
     * @param value 请求事件的值
     * */
    void onEvent(IMediaTransManager.P2PEvent event, Object value);

}
```


### 主调函数

#### 设备本地复位：reset()
```java
    /**
     * 本地复位【删除本地一些db文件】
     *
     * @return 成功，返回0；失败，非0
     */
    public boolean reset();
```

#### 获取ipc子设备参数配置管理器：getParamConfigManager
```java
    /**
     * 获取ipc子设备参数配置管理器
     *
     * @return 成功，返回管理器实例；失败，null
     */
    public IParamConfigManager getParamConfigManager();
```

#### 获取媒体传输管理器实例：getMediaTransManager
```java
    /**
     * 获取媒体传输管理器实例
     *
     * @return 成功，返回实例；失败，返回null
     */
    public IMediaTransManager getMediaTransManager();
```

#### 获取网关SDK管理器实例：getInstance
```java
    /**
     * 获取网关SDK管理器实例
     *
     * @return 成功，返回实例；失败，返回null
     */
    public static TuyaIoTManager getInstance();
```

#### 注册网关基础回调函数到SDK：setGwInfraCallbacks
```java
    /**
     * 注册网关基础回调函数到SDK
     *
     * @param callbacks 用户实现此接口注册进来
     * @return 无返回值
     */
    public void setGwInfraCallbacks(GwInfraCallbacks callbacks);
```

#### 注册zigbee设备相关回调函数到SDK：setZ3DevCallbacks
```java
    /**
     * 注册功能点及场景、音频等回调函数到SDK
     *
     * @param callbacks 用户实现此接口注册进来
     * @return 无返回值
     */
    public void setZ3DevCallbacks(Z3DevCallbacks callbacks);
```

#### 注册Misc设备相关回调函数到SDK：setMiscDevCallbacks
```java
    /**
     * 注册Misc设备相关回调函数到SDK
     *
     * @param callbacks 用户实现此接口注册进来
     * @return 无返回值
     */
    public void setMiscDevCallbacks(MiscDevCallbacks callbacks);
```

#### 准备工作完成后，启动网关：IotGatewayStart
```java
    /**
         * 启动设备
         *
         * @param context context of app
         * @param config config for gateway SDK
         * @return 成功，返回0；失败，非0
         */
        public void IotGatewayStart(Context context, Config config);
```

#### 使用token激活网关设备：IotGatewayActive
```java
    /**
     * 使用token激活网关设备
     *
     * @param token token of this gateway
     * @return 成功，返回0；失败，非0
     */
    public int IotGatewayActive(String token);
```

#### 本地解绑网关设备：IotGatewayUnactive
```java
    /**
     * 反激活网关设备
     *
     * @return 成功，返回0；失败，非0
     */
    public int IotGatewayUnactive();
```

#### 本地控制子设备入网的接口：IotGatewayPermitJoin
```java
    /**
     * 本地控制子设备入网的接口
     *
     * @param permit 0关闭，1开启
     * @return 成功，返回0；失败，非0
     */
    public int IotGatewayPermitJoin(boolean permit);
```

#### 将Misc子设备绑定到涂鸦云：IotGatewayMiscDevBind
```java
    /**
     * 将子设备绑定到涂鸦云
     *
     * @param uddd 用户自定义，可用于区分不同类型的设备
     * @param dev_id 子设备的 MAC 地址
     * @param pid 在涂鸦 IoT 平台上创建子设备产品得到的 PID
     * @param ver 子设备的软件版本，用于固件升级
     * @return 成功，返回0；失败，非0
     */
    public int IotGatewayMiscDevBind(int uddd, String dev_id, String pid, String ver);
```

#### 将Misc子设备从涂鸦云解绑：IotGatewayMiscDevBind
```java
    /**
     * unbind misc device from tuya cloud.
     *
     * @param dev_id device unique ID.
     * @return 成功，返回0；失败，非0
     */
    public int IotGatewayMiscDevUnbind(String dev_id);
```

#### 刷新Misc子设备在涂鸦云在线状态的接口：IotGatewayMiscDevHbFresh
```java
    /**
     * 刷新子设备在涂鸦云在线状态的接口
     *
     * @param dev_id 子设备的 MAC 地址
     * @param timeout 间隔在线的超时时间，单位是秒
     * @return 成功，返回0；失败，非0
     */
    public int IotGatewayMiscDevHbFresh(String dev_id, int timeout);
```

#### 设置网关本地日志存放路径：IotGatewayLogPathSet
```java
    /**
     * 设置网关本地日志存放路径
     *
     * @param path 日志路径，如：/sdcard/tuya/iot/
     * @return 成功，返回0；失败，非0
     */
    public int IotGatewayLogPathSet(String path);
```

#### 初始化心跳管理器：IotGatewayHbInit
```java
    /**
     * 初始化心跳管理器
     *
     * @return 成功，返回0；失败，非0
     */
    public int IotGatewayHbInit();
```

#### 将 Zigbee 子设备绑定到涂鸦云：IotGateWayZ3DevBind
```java
    /**
     * 将 Zigbee 子设备绑定到涂鸦云的接口
     *
     * @param uddd 用户自定义，可用于区分不同类型的设备
     * @param dev_id 子设备的 MAC 地址
     * @param pid 在涂鸦 IoT 平台上创建子设备产品得到的 PID
     * @param ver 子设备的软件版本，用于固件升级
     * @return 成功，返回0；失败，非0
     */
    public int IotGateWayZ3DevBind(int uddd, String dev_id, String pid, String ver);
```

#### 将 Zigbee 子设备从涂鸦云解绑：IotGateWayZ3DevUnbind
```java
    /**
     * unbind z3 device from tuya cloud
     *
     * @param dev_id device unique ID.
     * @return 成功，返回0；失败，非0
     */
    public int IotGateWayZ3DevUnbind(String dev_id);
```

#### 下发ZCL数据帧给Zigbee子设备：IotGateWayZ3DevSendZclCmd
```java
    /**
     * 下发 ZCL 数据帧给 Zigbee 子设备
     *
     * @param frame 参考 Z3ApsFrame 类说明
     * @return 成功，返回0；失败，非0
     */
    public int IotGateWayZ3DevSendZclCmd(Z3ApsFrame frame);
```

#### 本地移除Zigbee子设备：IotGateWayZ3DevDel
```java
    /**
     * 本地移除 Zigbee 子设备的接口
     *
     * @param id 子设备的 MAC 地址
     * @return 成功，返回0；失败，非0
     */
    public int IotGateWayZ3DevDel(String id);
```

#### 本地升级Zigbee子设备：IotGateWayZ3DevUpgrade
```java
    /**
     * 本地升级 Zigbee 子设备的接口
     *
     * @param id 子设备的 MAC 地址
     * @return 成功，返回0；失败，非0
     */
    public int IotGateWayZ3DevUpgrade(String id, String img);
```

#### set custom device：IotGateWayZ3DevSetCustom
```java
    /**
         * set custom device
         *
         * @param file json file
         * @return 成功，返回0；失败，非0
         */
        public int IotGateWayZ3DevSetCustom(String file);
```

#### 获取所有子设备信息：IotGateWayDevTraversal
```java
    /**
         * 获取子设备信息：子设备遍历，通过此接口可以遍历网关下所有的子设备
         *
         * @return 成功，返回DevDescIf对象；失败或结束，返回null
         */
        public static DevDescIf IotGateWayDevTraversal();
```

#### 获取子设备信息：IotGateWayDevInfoGet
```java
    /**
         * 获取子设备信息
         *
         * @param devId if devid = sub-device_id, then return the sub-dev info
         *             if devid = NULL, then return the gateway info
         * @return 成功，返回DevDescIf对象；失败或结束，返回null
         */
        public static DevDescIf IotGateWayDevInfoGet(String devId);
```

#### 解绑设备：IotGateWayDevUnbind
```java
    /**
         * unbind device from tuya cloud.
         *
         * @param devId device unique ID
         * @return 成功，返回0；失败，非0
         */
        public int IotGateWayDevUnbind(String devId);
```

#### 设置区域：SetRegion
```java
    /**
         * SetRegion
         *
         * @param region  region on successful
         *
         * @return region
         */
        public int setRegion(int region);
```

#### 获取用于扫设备二维码进行配网激活的短链接：getQrCode
```java
    /**
         * getQrCode
         *
         * @param appid  app id
         *
         * @return QrCode or null if failed
         */
        public String getQrCode(String appid);
```

#### 获取注册状态：getRegisterStatus
```java
    /**
         * getRegisterStatus
         *
         * @return get register status of ipc
         */
        public int getRegisterStatus();
```

#### 上报dp消息：sendDP
```java
    /**
         * 上报dp消息
         *
         * @param dpId   dp id
         * @param type 类型 DPEvent.Type
         * @param val  值
         * @return
         */
        final public int sendDP(String devId, int dpId, int type, Object val);
```

#### 上报多个dp消息：sendDP
```java
    /**
         * 上报多个dp消息
         *
         * @param events 多个dp类型
         * @return
         */
        final public int sendDP(String devId, DPEvent... events);
```

#### 媒体参数管理器类设置：setInt
```java
    /**
     * 媒体参数管理器类设置，可参考demo了解
     *
     * @param device 设备通道号
     * @param index 媒体类型索引
     * @param key key
     * @param value 多个dp类型
     * @return 成功，返回0；失败，非0
     */
    void setInt(int device, int index, String key, int value);;
```

#### 媒体传输管理器类开启传输能力：startMultiMediaTrans
```java
        /**
         * 启动多媒体传输通道.这个方法必须调用，否则无法push 数据流到App端，也无法接受Mqtt消息.
         * 调用这个方法之后，可用通过{@link com.tuya.smart.aiipc.ipc_sdk.api.IMediaTransManager#pushMediaStream(int, int, byte[])} 方法push
         * 一帧数据流到该SDK
         * @param max 接收并发量
         * @return success: 0 fase: !0
         * */
        int startMultiMediaTrans(int max);
```

#### 媒体传输管理器类开始云存储功能：startCloudStorage
```java
        /**
         * 开始云存储功能. 该方法调用之后(以购买购买云存储)，设备具有云储存相关功能
         * */
        int startCloudStorage(int subDevice);
```

#### 媒体传输管理器类注册对讲回调：addAudioTalkCallback
```java
     /**
        * 注册对讲回调接口
        * 该接口将回调对讲时的音频数据
        * @param cb 回调接口
        * */
       void addAudioTalkCallback(AudioTalkCallback cb);
```

#### 媒体传输管理器类注册回放相关回调：setP2PEventCallback
```java
     /**
          * 设置DP 事件回调接口
          * 注册该回调之后device端将接受到来自App端的控制指令，并做对应的处理
          * {@link P2PEvent}
          *
          * @param cb 回调接口
        */
         void setP2PEventCallback(IP2PEventCallback cb);
```

#### 媒体传输管理器类发移动侦测信息：notifyMotionEvent
```java
    /**
         * 当移动侦测事件发生时，发送报警图片到App端
         * @param subDevice device channel
         * @param data 报警图片字节流
         * @param snapType 图片格式的类型
         * @param notifyType 事件的类型
         * */
        int notifyMotionEvent(int subDevice, byte[] data, int snapType, int notifyType);
```

#### 媒体传输管理器类回放帧传输：playbackSendFrame
```java
     /**
          * 回放帧传输
          *
          * @param chn 通道号
          * @param client client号
          * @param frame 媒体帧
        */
         int playbackSendFrame(int chn, int client, MediaFrame frame);
```

#### 媒体传输管理器类回放完成：playbackSendFinish
```java
     /**
          * 回放完成
          *
          * @param chn 通道号
          * @param client client号
        */
         int playbackSendFinish(int chn, int client);
```


## 辅助类说明

### DPEvent

```java
   /**
    * DP点类
    */
    public class DPEvent {

        public class Type {
            //Boolean
            public static final int PROP_BOOL = 0;
            //Integer
            public static final int PROP_VALUE = 1;
            //String
            public static final int PROP_STR = 2;
            //Integer
            public static final int PROP_ENUM = 3;
            //Integer
            public static final int PROP_BITMAP = 4;
            //RAW
            public static final int PROP_RAW = 5;
        }

        public int dpid;            // dp id
        public short type;          // dp type
        public Object value;        // dp value
        /**
        * 发生的时间戳(单位秒)
        */
        public int timestamp;  // dp happen time. if 0, mean now
        ...
    }
```

### Z3ApsFrame

```java
    /**
    * 涂鸦封装的 Zigbee ZCL 数据帧。
    */
    public class Z3ApsFrame {
        /**
        * 子设备的 MAC 地址
        */
        public String mId;
        /**
        * 子设备的短地址
        */
        public int mNodeId;
        /**
        * Zigbee Profile ID
        */
        public int mProfileId;
        /**
        * Zigbee Cluster ID
        */
        public int mClusterId;
        /**
        * 源 endpoint
        */
        public int mSrcEndpoint;
        /**
        * 目的 endpoint
        */
        public int mDstEndpoint;
        /**
        * 组 ID，组播才需要
        */
        public int mGroupId;
        /**
        * ZCL 命令类型。
        * 1 表示 global；
        * 2 表示特定 cluster
        */
        public int mCmdType;
        /**
        * ZCL Command ID
        */
        public int mCmdId;
        /**
        * 传输类型。
        * 0 表示单播；
        * 1 表示组播；
        * 2 表示广播
        */
        public int mFrameType;
        /**
        * 禁止响应。
        * 1 表示禁止响应；
        * 0 表示使能响应
        */
        public int mDisableAck;
        /**
        * ZCL payload 的长度
        */
        public int mMsgLength;
        /**
        * ZCL payload
        */
        public byte[] mMessage;
        ...
    }
```

### Z3Desc

```java
public class Z3Desc {
    public final static int MAX_EP_NUM = 10;
    public final static int MAX_CLUSTER_NUM = 10;
    /**
     * 子设备的 MAC 地址
     */
    public String mId;
    /**
     * Zigbee Profile ID
     */
    public int mProfileId[];
    public int mDeviceId[];
    public int mClusterId[][];
    public int mEndpoint[];
    public int mEpNum;
    public int mUcNum;
    /**
     * 子设备的短地址
     */
    public int mNodeId;
    /**
     * 厂商名称
     */
    public String mManuName;
    /**
     * 设备型号
     */
    public String mModelId;
    /**
     * 重新入网标记
     */
    public int mRejoinFlag;
    public int mPowerSource;
    public int mVersion;
    ....
}
```

### DevDescIf

```java
    public class DevDescIf {
        public String mId;                      // Device ID
        public String mSwVer;                   // version
        public String mSchemaId;                // Schema object model ID
        public String mProductKey;              // Product ID(PID) You can choose between PID and Firmware Key
        public String mFirmwareKey;             // Firmware Key
        public boolean mIsOem;                  // Is oem?　1:firmware key  0:product key
        public String mSigmeshDevKey;           // Sigmesh Device key (Temporarily Unsupported)
        public String mSigmeshMac;              // Sigmesh Device mac　(Temporarily Unsupported)
        public int mUddd;                       // User detial type define
        public int mUddd2;                      // The maximum value is 1, reserved for user-owned child devices
        public int mTp;                         // Device Type
        public boolean mBind;                   // True:Is binding　/ False:unbounded
        public boolean mSync;                   // True:Has synchronized /　False:Not synchronized
        public boolean mSigmeshSync;            // Sigmesh synchroniz (Temporarily Unsupported)
        public GwAttachAttr[] mAttr;            // Attribute array
        public boolean mResetFlag;              // Reset flag
        public ChCode mChDminfo;                // Temporary ignorability
    }
```

### 对讲回调：TalkCallback
```java
public interface TalkCallback {
     /**
          * 回放帧传输
          *
          * @param subDevice 通道号
          * @param talkData 语音数据 PCM格式 8000sample 16bit mono
          * @return 无
        */
    void onTalkData(int subDevice, byte[] talkData);
}
```

### Raw功能点表示：RecvRawDP
```java
public class RecvRawDP {

    // DP_TRANS_TYPE_T
    public static final byte DP_CMD_LAN  =    0;       // cmd from LAN
    public static final byte DP_CMD_MQ    =   1 ;      // cmd from MQTT
    public static final byte DP_CMD_TIMER =   2;       // cmd from Local Timer
    public static final byte DP_CMD_SCENE_LINKAGE = 3;  // cmd from scene linkage
    public static final byte DP_CMD_RELIABLE_TRANSFER = 4; // cmd from reliable transfer
    public static final byte DP_CMD_BT   =    5;      // cmd from bt
    public static final byte DP_CMD_SCENE_LINKAGE_LAN = 6;  // cmd from lan scene linkage

    public int cmd_tp;
    public int dtt_tp;
    public String cid;
    public int dpid;
    public String mb_id;
    public byte []data;
...
}

```

### obj功能点表示：RecvObjDP
```java
public class RecvObjDP {

    // DP_TRANS_TYPE_T
    public static final byte DP_CMD_LAN  =    0;       // cmd from LAN
    public static final byte DP_CMD_MQ    =   1 ;      // cmd from MQTT
    public static final byte DP_CMD_TIMER =   2;       // cmd from Local Timer
    public static final byte DP_CMD_SCENE_LINKAGE = 3;  // cmd from scene linkage
    public static final byte DP_CMD_RELIABLE_TRANSFER = 4; // cmd from reliable transfer
    public static final byte DP_CMD_BT   =    5;      // cmd from bt
    public static final byte DP_CMD_SCENE_LINKAGE_LAN = 6;  // cmd from lan scene linkage

    public int cmd_tp;
    public int dtt_tp;
    public String cid;
    public String mb_id;
    public DPEvent []dpEvent;
...
}

### 回放控制及查询：PlaybackControl、PlaybackAlarmFragment、PlaybackQuery
```java
public class PlaybackControl {
    public int channel;
    public int idx;
    public String subdid;

    public long start_timestamp;
    public long end_timestamp;
    public long playTime;
...
}

public class PlaybackAlarmFragment {

    public int type;
    public long start_timestamp;
    public long end_timestamp;

    public PlaybackAlarmFragment(int type, long start_timestamp, long end_timestamp) {
        this.type = type;
        this.start_timestamp = start_timestamp;
        this.end_timestamp = end_timestamp;
    }
...
}

public class PlaybackQuery {
    public int channel;
    public String subdid;
    public int year;
    public int month;
    public int day;
    public PlaybackAlarmFragment[] fragment; // for DAY_TS
...
}
```

### 固件信息：FwInfo
```java
public class FwInfo {

    public int tp;
    public String swVer;
    public int fileSize;
    public String fwUrl;
    public String fwHmac;
...
}
```

## 如何获得技术支持
You can get support from Tuya with the following methods:

Tuya Smart Help Center: https://support.tuya.com/en/help  
Technical Support Council: https://iot.tuya.com/council/   

## 使用的开源License
This Tuya Android Device SDK Sample is licensed under the MIT License.