# DogeTransfer 项目设置指南

## 快速开始

### 方法 1: 使用 Android Studio（推荐）

这是最简单的方法，Android Studio 会自动处理所有依赖和配置。

1. **安装 Android Studio**
   - 下载并安装最新版本的 [Android Studio](https://developer.android.com/studio)
   - 确保安装了 Android SDK API 36

2. **打开项目**
   - 启动 Android Studio
   - 选择 "Open" 或 "Open an Existing Project"
   - 选择 DogeTransfer 项目目录
   - 等待 Gradle 同步完成（首次可能需要几分钟）

3. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击运行按钮（绿色三角形）或按 Shift+F10
   - 应用将自动安装并启动

### 方法 2: 手动配置 Gradle Wrapper

如果您想使用命令行构建，需要先配置 Gradle Wrapper：

#### 步骤 1: 下载 gradle-wrapper.jar

由于 gradle-wrapper.jar 是二进制文件，您需要：

**选项 A - 使用 Android Studio 自动生成（推荐）**
1. 在 Android Studio 中打开项目
2. Gradle 会自动下载并配置 wrapper

**选项 B - 从其他 Android 项目复制**
1. 找到任何已配置好的 Android 项目
2. 复制 `gradle/wrapper/gradle-wrapper.jar` 到本项目相同位置

**选项 C - 使用系统 Gradle 生成**
```bash
# 如果系统已安装 Gradle
gradle wrapper --gradle-version 8.13
```

#### 步骤 2: 验证配置

确保以下文件存在：
- `gradlew` (Linux/Mac)
- `gradlew.bat` (Windows)
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

#### 步骤 3: 构建项目

**Windows:**
```cmd
.\gradlew.bat assembleDebug
```

**Linux/Mac:**
```bash
chmod +x gradlew
./gradlew assembleDebug
```

生成的 APK 位于：`app/build/outputs/apk/debug/app-debug.apk`

## 环境要求

### 开发环境
- JDK 11 或更高版本
- Android SDK API 24-36
- Kotlin 2.0.21
- Gradle 8.13

### 运行环境
- Android 8.0 (API 26) 或更高版本
- 支持 WiFi Direct 的设备

## 常见问题

### Q: Gradle 同步失败
**A:** 
- 检查网络连接
- 配置 Gradle 代理（如果在国内）
- 在 `gradle.properties` 中添加：
  ```properties
  systemProp.http.proxyHost=your.proxy.host
  systemProp.http.proxyPort=port
  systemProp.https.proxyHost=your.proxy.host
  systemProp.https.proxyPort=port
  ```

### Q: 找不到 gradle-wrapper.jar
**A:** 
- 使用 Android Studio 打开项目，会自动下载
- 或从 [Gradle 官网](https://gradle.org/releases/) 下载对应版本

### Q: 构建失败
**A:**
- 清理项目：`.\gradlew.bat clean`
- 删除 `.gradle` 和 `build` 目录
- 重新同步 Gradle
- 检查 JDK 版本是否为 11+

### Q: 权限错误（Linux/Mac）
**A:**
```bash
chmod +x gradlew
```

## 依赖下载加速（可选）

如果在国内，可以使用国内镜像加速依赖下载。

### 方法 1: 修改项目的 settings.gradle.kts

在文件顶部添加：
```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}
```

### 方法 2: 使用全局配置

在用户目录下创建 `~/.gradle/init.gradle`：
```groovy
allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        google()
        mavenCentral()
    }
}
```

## 调试模式

### 启用详细日志
```bash
.\gradlew.bat assembleDebug --info
```

### 查看依赖树
```bash
.\gradlew.bat dependencies
```

### 清理构建
```bash
.\gradlew.bat clean
```

## 开发提示

1. **使用 Android Studio 的 Layout Inspector** 查看 UI 层次结构
2. **启用开发者选项** 在设备上调试
3. **使用 Logcat** 查看运行日志
4. **WiFi Direct 调试** 建议使用两台真机测试，模拟器不支持 WiFi Direct

## 联系支持

如遇到问题：
1. 查看 [README.md](README.md) 的故障排除部分
2. 检查 Android Studio 的 Build Output
3. 提交 Issue 并附上错误日志 