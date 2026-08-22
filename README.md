<div align="center">

# ⚡ KernelEX
---
**以下软件介绍和网站都是AI写的，所以内容可能会与我的开发初衷不同，你就把这个软件当成一个终端就行了（（（**
---
**下一代 Android 高性能 ROOT 任务调度与执行引擎**

[![Release](https://img.shields.io/github/v/release/KernelExtend/KernelEX?style=flat-square&color=00e5ff&label=Release)](https://github.com/KernelExtend/KernelEX/releases)
[![Telegram](https://img.shields.io/badge/Telegram-Channel-229ED9.svg?style=flat-square&logo=telegram&logoColor=white)](https://t.me/KernelEX)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20%28API%2026%2B%29-3DDC84.svg?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![ROOT](https://img.shields.io/badge/ROOT-Magisk%20%7C%20KernelSU%20%7C%20APatch-orange.svg?style=flat-square)](https://github.com/topjohnwu/Magisk)
[![Signing](https://img.shields.io/badge/Signature-V2%20%2B%20V3%20Scheme-brightgreen.svg?style=flat-square)](https://github.com/KernelExtend/KernelEX/releases)
[![Website](https://img.shields.io/badge/Website-kernelextend.github.io-purple.svg?style=flat-square)](https://kernelextend.github.io/)

[官方网站 (Docs & Downloads)](https://kernelextend.github.io/) • [Telegram 官方频道](https://t.me/KernelEX) • [GitHub 仓库](https://github.com/KernelExtend/KernelEX) • [下载最新版 (Releases)](https://github.com/KernelExtend/KernelEX/releases/tag/v1.0.2)

</div>

---

> 📢 **欢迎加入我们的官方 Telegram 频道**：[**https://t.me/KernelEX**](https://t.me/KernelEX)，第一时间获取最新发布动态、更新日志与极客讨论！

---

## 📖 项目简介

**KernelEX** 是一款专为 Android ROOT 极客与底层开发者量身打造的高级执行与任务调度工具。

通过自主研发的 **HyperCore 流式执行引擎**，KernelEX 可以在 ROOT 环境下直接调度运行 `.sh` Shell 脚本与 `.so` / ELF 二进制原生程序，无需繁琐的命令行配置，并提供 16ms 帧同步无阻流式输出、ANSI 真彩色渲染与 MIUIX 沉浸式质感交互体验。

---

## ✨ 核心特性

### ⚡ 1. HyperCore 高性能流式引擎
- **16ms 帧同步流式分发**：在高频海量日志输出场景下，通过并发微批次队列聚合调度，确保 UI 渲染维持在 60 FPS 丝滑不卡顿。
- **环形滑窗智能内存截断**：内存平稳恒定在 $O(1)$，单行二分查断保证长任务无损展示，杜绝内存溢出（OOM）。
- **进程生命周期流控**：毫秒级捕获子进程原生 PID，支持标准输入输出交互与精确的进程销毁。

### 🧬 2. ELF & 脚本双模原生执行
- **全格式支持**：原生适配 Linux Shell 脚本（`.sh`）与 ELF 二进制可执行文件（`.so`）。
- **环境自动装载**：执行时自动注入标准 Linux 环境变量（`/system/bin`、`/sbin`、`PATH`、`TERM`、`LANG`）并赋权 `777`。
- **精确信号控制**：支持实时发送 `Ctrl+C` (`SIGINT`) 信号与 `kill -9` 优雅结束进程。

### 🎨 3. MIUIX 沉浸式质感设计
- **现代化设计语言**：深度整合 Kotlin Multiplatform 与 MIUIX 设计组件规范，视觉圆润通透。
- **ANSI 16 色与转义字符渲染**：有限状态机高效解析终端 ANSI 颜色与控制码。
- **个性化调色盘**：内置 16 款精选终端配色预设，配备直观的全色相与明暗微调滚轮。
- **主题模式自适应**：支持开启、关闭或跟随系统深色模式，提供悬浮/固定底栏个性化切换。

### 🗂️ 4. ROOT 全盘文件管理器
- **直通核心工作区**：快捷直达 `/data/adb/KernelEX` 默认工作区与全盘任意路径。
- **独立文件夹存储隔离**：支持一键为脚本创建独立时间戳专属目录，避免同名冲突。
- **安全防穿透加固**：严格校验路径分隔符与 Shell 引号转义，拦截路径穿越与命令注入风险。

---

## 🏗️ 架构与执行流程

```mermaid
graph LR
    A[选择执行文件 .sh / .so] --> B[ROOT 提权与环境装载]
    B --> C[赋权 777 & 注入 PATH/TERM]
    C --> D[启动 su 子进程 & 捕获 PID]
    D --> E[HyperCore 16ms 帧同步队列]
    E --> F[ANSI 状态机高亮解析]
    F --> G[MIUIX 终端实时真彩呈现]
```

---

## 🚀 快速上手

### 1. 运行环境要求
* **操作系统**：Android 8.0 (API 26) 及以上（最高支持至 Android 15+）
* **设备架构**：`arm64-v8a`、`armeabi-v7a`、`x86_64`
* **ROOT 方案**：已安装并授权 **Magisk**、**KernelSU** 或 **APatch**

### 2. 存放与执行脚本
1. 打开 KernelEX，在首页直接输入脚本路径，或点击 **“从文件管理器选择”** 选取执行文件；
2. 也可将文件直接保存在 `/data/adb/KernelEX/` 目录下；
3. 点击 **“立即执行”**，应用将自动跳转到终端并展示实时执行流水线。

---

## 📥 软件下载

所有发布版本均进行 **APK Signature Scheme v2 + v3** 架构签名。

| 发行版本 | 文件名 | 适用场景 | GitHub 下载链接 |
| :--- | :--- | :--- | :--- |
| 🚀 **Release 正式版** | `KernelEX-v1.0.2-release.apk` | 生产日常使用（推荐） | [点击下载 Release 版](https://github.com/KernelExtend/KernelEX/releases/download/v1.0.2/KernelEX-v1.0.2-release.apk) |
| 🛠️ **Debug 调试版** | `KernelEX-v1.0.2-debug.apk` | 开发者排查与模块调试 | [点击下载 Debug 版](https://github.com/KernelExtend/KernelEX/releases/download/v1.0.2/KernelEX-v1.0.2-debug.apk) |

---

## 🛠️ 源码编译指南

本项目使用 Gradle 与 Kotlin 2.x 进行构建：

```bash
# 1. 克隆代码仓库
git clone https://github.com/KernelExtend/KernelEX.git
cd KernelEX

# 2. 编译 Debug 调试包
./gradlew :app:assembleDebug

# 3. 编译 Release 正式包
./gradlew :app:assembleRelease
```

编译生成的 APK 位于：`app/build/outputs/apk/{release|debug}/`

---

## 🛡️ 安全审计与加固说明

KernelEX 经过严格的安全审计，具备以下安全机制：
1. **防路径穿越 (Path Traversal Protection)**：重命名与文件操作严格过滤 `../`、`\`、`\0` 等恶意跨目录符号；
2. **Shell 转义加固 (Command Injection Prevention)**：所有路径与进程参数统一采用标准单引号安全转义；
3. **资源流自动回收 (Resource Leak Prevention)**：所有底层 Process 流采用 Kotlin `use { }` 自动安全回收；
4. **ROOT 检测超时熔断**：防范因授权管理器挂起导致的协程死锁；
5. **本地备份防护**：已禁用 `allowBackup`，杜绝通过 ADB 提取敏感数据。

---

## 💬 社区与支持

* **Telegram 官方频道**：[https://t.me/KernelEX](https://t.me/KernelEX)
* **GitHub 组织**：[https://github.com/KernelExtend](https://github.com/KernelExtend)
* **官方网站**：[https://kernelextend.github.io/](https://kernelextend.github.io/)

---

## 📄 开源许可证

本项目基于 **Apache License 2.0** 许可证开源。请在遵守当地法律法规的前提下使用，开发者不对使用本工具造成的任何设备损坏或数据丢失承担责任。

---

<div align="center">
  <sub>Made with ❤️ by KernelExtend Team · Powered by <a href="https://kernelextend.github.io/">GitHub Pages</a></sub>
</div>
