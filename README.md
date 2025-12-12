# 赛博视奸 (CyberObserver)

> **赛博朋克风格跨设备实时监控系统**

![License](https://img.shields.io/badge/license-MIT-blue.svg) ![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)

[🇨🇳 中文文档](README.md) | [🇺🇸 English](README_EN.md)

---

## 🌌 愿景 (Vision)

*"In the digital void, I see myself."*

**CyberObserver** 象征着对 **数字自我意识** 的终极追求。这是一个诞生于极客对掌控与完美的不妥协渴望的赛博监控项目。我们以极简的 Java 全栈技术为桥梁，打破物理现实与数字孪生的界限。

目标是实现 **全知感知 (Omniscient Perception)**：
- **全谱采集**：无死角聚合 PC 与手机的电量、前台应用、后台进程及高频传感器数据。
- **逆向动力学 (Human IK)**：通过传感器数据反推用户的物理体态。你睡，Avatar 即睡；你工作，它便守望。
- **现实镜像**：在 Web3D 空间中高保真还原设备空间位置与多屏协同状态。
- **上帝模式**：通过授权码实现对数字领地的绝对控制与远程执行。

我们拒绝因“个人能力限制”而妥协。每一行代码，都是向“人机合一”完美境界迈进的一步。

---

## 📖 简介 (Introduction)

**CyberObserver** 是一个“上帝视角”监控系统。它为你的工作环境创建数字孪生，将实时遥测数据渲染为赛博朋克风格的 3D 界面。

## ✨ 功能特性 (Features)

### 1. 大脑 (Server)
- **技术**: Java (Spring Boot 3) + WebSocket + Three.js
- **功能**: 中央数据枢纽与 3D 可视化展示。
- **视觉**: 低多边形赛博朋克美学，设备姿态实时镜像。

### 2. 哨兵 (PC Agent)
- **技术**: Java Native (OSHI + JNA)
- **功能**: 深度系统监控。
    - CPU/内存/电池状态。
    - 活动窗口检测。
    - 远程控制执行（计划中）。

### 3. 侦察兵 (Mobile Agent)
- **技术**: Android Native (AccessibilityService)
- **功能**: 上下文感知。
    - **姿态推演**: 基于传感器检测是否在行走、睡觉或游戏。
    - **应用使用**: 通过无障碍服务监控当前前台应用。

---

## 🛠️ 架构 (Architecture)

```mermaid
graph TD
    User["Web Dashboard (Three.js)"] <-->|WebSocket| Server["CyberServer (Spring Boot)"]
    PC["CyberPC (Java/OSHI)"] -->|WebSocket| Server
    Mobile["Android (Sensors)"] -->|WebSocket| Server
```

---

## 🚀 快速开始 (Quick Start)

### 前置条件
- JDK 17+
- Maven 3.6+
- Android Studio (用于构建移动端)

### 1. 构建项目
```bash
# 根目录
mvn clean install
```

### 2. 启动服务端
```bash
cd server
mvn spring-boot:run
# 访问仪表盘: http://localhost:8080
```

### 3. 启动 PC 客户端
```bash
# 根目录
mvn exec:java -pl pc -Dexec.mainClass="com.cyber.pc.CyberPC"
```

### 4. 构建安卓应用
- 在 **Android Studio** 中打开 `mobile` 目录。
- 构建并安装 APK 到你的设备。
- 在系统设置中手动授予 **无障碍服务权限**。

---

## ⚠️ 免责声明 (Disclaimer)

本工具仅供**个人私人设备**使用。数据采集功能（尤其是无障碍服务）具有高度侵入性。请勿在未经机主明确同意的情况下安装。

---

*Project by CyberObserver Team*
