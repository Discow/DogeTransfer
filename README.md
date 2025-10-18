# DogeTransfer - WiFi Direct 文件传输应用

## 项目简介

DogeTransfer 是一个基于 WiFi Direct 技术的 Android 文件传输应用。支持两台 Android 设备之间直接传输文件，无需热点，无需在同一个 WiFi 网络下。

## 功能特性

- ✅ **自动设备发现**：自动扫描并发现附近的 WiFi Direct 设备
- ✅ **点对点连接**：直接连接两台设备，无需路由器或热点
- ✅ **文件传输**：支持任意类型文件的发送和接收
- ✅ **实时进度**：显示文件传输的实时进度
- ✅ **现代化 UI**：Material Design 3 风格界面
- ✅ **Android 8.0+ 支持**：兼容 Android 8.0 及以上版本

## 技术特点

### WiFi Direct
- 使用 Android WiFi P2P API
- 自动处理设备发现和连接
- 支持群组所有者协商

### 文件传输
- Socket 通信实现文件传输
- 8KB 缓冲区优化传输速度
- 支持大文件传输
- 传输进度实时显示

### 权限管理
- 自动适配 Android 不同版本的权限要求
- Android 13+ 使用 NEARBY_WIFI_DEVICES 权限
- Android 12 及以下使用位置权限

## 系统要求

- Android 8.0 (API 26) 或更高版本
- 支持 WiFi Direct 的设备
- 必需权限：
  - WiFi 状态和控制权限
  - 位置权限（Android 12 及以下）
  - 附近 WiFi 设备权限（Android 13+）
  - 存储/媒体访问权限

## 构建项目

### 使用 Android Studio（推荐）

1. 打开 Android Studio
2. 选择 "Open an Existing Project"
3. 选择本项目目录
4. 等待 Gradle 同步完成
5. 点击 "Run" 或使用快捷键 Shift+F10

### 使用命令行

如果 gradle wrapper 未配置，请先在 Android Studio 中打开项目一次，或手动下载 gradle-wrapper.jar：

```bash
# Windows
.\gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

## 使用说明

### 连接设备

1. **启动应用**：在两台设备上同时打开 DogeTransfer
2. **授予权限**：首次使用时授予所有必需权限
3. **搜索设备**：点击"搜索设备"按钮开始扫描
4. **连接设备**：在设备列表中点击要连接的设备
5. **确认连接**：双方确认后建立连接

### 发送文件

1. **连接设备**：确保已成功连接到另一台设备
2. **选择文件**：点击"选择文件"按钮选择要发送的文件
3. **发送文件**：点击"发送文件"按钮开始传输
4. **查看进度**：在界面上查看传输进度

### 接收文件

- 文件自动接收到：`/Android/data/com.example.dogetransfer/files/DogeTransfer/`
- 接收的文件会显示在"接收的文件"列表中
- 点击文件可查看详细信息

## 项目结构

```
app/src/main/java/com/example/dogetransfer/
├── MainActivity.kt                 # 主活动
├── model/
│   ├── DeviceInfo.kt              # 设备信息数据类
│   ├── FileInfo.kt                # 文件信息数据类
│   └── TransferProgress.kt        # 传输进度数据类（在 FileInfo.kt 中）
├── wifi/
│   ├── WiFiDirectManager.kt       # WiFi Direct 管理器
│   ├── WiFiDirectListener.kt      # WiFi Direct 事件监听器
│   └── WiFiDirectBroadcastReceiver.kt  # 广播接收器
├── transfer/
│   └── FileTransferService.kt     # 文件传输服务
└── adapter/
    ├── DeviceAdapter.kt           # 设备列表适配器
    └── FileAdapter.kt             # 文件列表适配器
```

## 依赖库

- AndroidX Core KTX
- Material Components
- ConstraintLayout
- Lifecycle (ViewModel & Runtime)
- Kotlin Coroutines
- RecyclerView
- CardView

## 注意事项

1. **WiFi Direct 限制**：
   - 某些设备可能不支持 WiFi Direct
   - 连接距离通常在 50-100 米范围内
   - 同一时间只能连接一个设备

2. **权限要求**：
   - 必须授予所有权限才能正常使用
   - Android 6.0+ 需要运行时权限授予
   - 位置权限用于 WiFi 扫描（系统要求）

3. **文件存储**：
   - 接收的文件存储在应用私有目录
   - 卸载应用会删除接收的文件
   - 可以使用文件管理器访问

## 故障排除

### 无法发现设备
- 确保两台设备都已启用 WiFi
- 检查是否授予了所有必需权限
- 尝试重启 WiFi 或设备
- 确保设备支持 WiFi Direct

### 连接失败
- 检查设备是否已连接其他 WiFi Direct 设备
- 尝试先断开现有连接
- 重新搜索并连接
- 检查设备间距离

### 文件传输失败
- 确保连接稳定
- 检查存储空间是否充足
- 对于大文件，保持设备屏幕常亮
- 避免传输过程中切换应用

## 开发计划

- [ ] 支持多文件批量传输
- [ ] 添加传输历史记录
- [ ] 支持文件夹传输
- [ ] 添加传输速度显示
- [ ] 支持传输暂停和恢复
- [ ] 添加二维码连接功能

## 开源协议

本项目采用 Apache License 2.0 开源协议。

## 联系方式

如有问题或建议，请提交 Issue 或 Pull Request。 