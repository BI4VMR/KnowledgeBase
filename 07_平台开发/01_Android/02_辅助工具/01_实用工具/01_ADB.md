# 简介
ADB的全称为Android Debug Bridge，是Android设备的调试工具，主要包括 `adb` 和 `recovery` 两个命令，我们可以通过它们查看日志、向设备中传输文件、刷写系统镜像等。

ADB是一个CS架构的软件，以PC设备作为服务端、Android设备作为客户端，我们可以通过USB线缆或TCP/IP网络使用ADB。

ADB包含以下三个部分：

🔷 ADB Server

ADB Server运行在PC设备上，默认使用TCP 5037端口。系统启动后，当我们首次执行 `adb` 命令时，服务端进程将会自动启动。

ADB Server用于连接ADB Client和Android设备，当它收到ADB Client发送的指令后，会将指令转发至对应的Android设备。

🔷 ADB Client

ADB Client与ADB Shell一一对应，每个Shell即为一个客户端进程，它们负责将我们键入的命令发送到服务端，并回显服务端的输出信息。

🔷 ADBD

"adbd"是运行在Android设备上的进程，用于连接ADB Server并执行其发出的指令。

<br />

本章的示例工程详见以下链接：

- [🔗 示例工程：ADB](https://github.com/BI4VMR/Study-Android/tree/master/M02_Tool/C01_Common/S01_ADB)


# 安装与配置
ADB包含在Android SDK中，如果我们已经在PC上安装了Android SDK，可以在 `<Android SDK安装目录>/platform-tools/` 路径下找到ADB的可执行文件。为了方便地在命令行中全局调用ADB命令，我们可以将该目录添加到操作系统的 `PATH` 环境变量中。

我们可以在终端中执行 `adb version` 命令，如果能够输出版本信息，说明ADB已经可用了：

```text
[bi4vmr@Fedora ~]$ adb version
Android Debug Bridge version 1.0.41
Version 34.0.5-10900879
```

如果我们并不需要完整的Android SDK，则可以在 [🔗 ADB - 官方下载页面](https://developer.android.google.cn/tools/releases/platform-tools#downloads) 网页中下载独立的ADB软件包。


# 基本应用
## 通过USB连接设备
我们首先需要进入Android设备的 `系统设置 -> 关于手机` 页面，连续点击5次“版本号”，启用隐藏菜单“开发者选项”；然后进入 `系统设置 -> 系统 -> 开发者选项` 页面，启用“USB调试”选项；并通过USB线缆将Android设备与PC相连。

接下来，我们在PC终端上输入 `adb device` 命令启动ADB服务器，此时Android设备就会弹出ADB授权窗口，当我们点击“允许”按钮后，设备即可连接到ADB服务器。

> 🚩 提示
>
> 不同设备的“开发者选项”启用方式、菜单位置可能与原生Android有区别，我们可以通过查阅设备制造商提供的开发文档、使用搜索引擎等方式了解详情。

## 通过TCP/IP连接设备
通过USB线缆调试有以下缺点：

- 对于手机、平板电脑等便携设备，通过USB方式调试时无法随意移动。
- 部分设备的USB端口分为ADB与OTG模式，在ADB模式下无法识别U盘等设备，此时我们无法调试相关功能。

为了解决上述问题，我们可以使用Android设备的无线网卡与PC互联。

我们首先需要确保PC和Android设备之间IP可达，然后使用USB线缆连接PC完成ADB授权，并在PC终端输入 `adb tcpip <端口>` 命令设置ADB的TCP/IP端口：

```text
# 开启TCP/IP支持，并设置端口为"5555"。
[bi4vmr@Fedora ~]$ adb tcpip 5555
```

接着执行 `adb connect <设备IP>:<端口>` 命令，使PC与Android设备通过TCP/IP网络互联。

```text
# 连接设备
[bi4vmr@Fedora ~]$ adb connect 172.18.7.1
connected to 172.18.7.1:5555
```

当终端输出内容为“连接成功”后，我们就可以拔出USB线缆并使用无线网络调试了。

## 服务端进程管理
ADB Server进程通常是系统自动管理的，无需人工干预；当我们首次执行ADB命令时，ADB Server将会自动启动。

假如我们遇到ADB Server功能异常的情况，可以尝试通过以下命令重启ADB Server进程。

```text
# 强制停止已经开启的ADB Server进程
[bi4vmr@Fedora ~]$ adb kill-server

# 启动新的ADB Server进程
[bi4vmr@Fedora ~]$ adb start-server
* daemon not running; starting now at tcp:5037
* daemon started successfully
```

如果ADB Server的默认端口被其他服务占用导致启动失败，我们可以添加 `-p <端口号>` 选项指定一个其他端口。

```text
# 启动ADB Server，指定端口为"32768"。
[bi4vmr@Fedora ~]$ adb -p 32768 start-server
```

## 查看设备列表
我们可以通过 `adb devices` 命令查看已连接的设备及其状态。

```text
[bi4vmr@Fedora ~]$ adb devices
List of devices attached
892QAEVCM9NEA   device
emulator-5554   device
172.18.7.1:5555 device
```

根据上述输出内容可知：

第一列是设备的标识符，通过USB连接时标识符为设备序列号；通过TCP/IP连接时标识符为 `<IP地址>:<端口号>` ；AVD模拟器的标识符为 `emulator-<模拟器序号>` 。

第二列是设备的状态，显示 `device` 时为就绪状态，可以执行其他命令；显示 `offline` 时表示设备ADB服务异常，我们可以尝试在设备上重新开启ADB、重启设备等操作进行修复；显示 `recovery` 时表示设备正处于Recovery模式。

## 选择设备
当有多个设备连接至PC时，直接输入ADB命令将会出现错误："more than one device and emulator"，这是因为ADB不知道应当将命令发送给哪个设备。

我们可以在ADB命令的第一个选项位置添加 `-s <设备标识符>` 选项，指定当前操作的设备。

```text
# 进入序列号为"892QAEVCM9NEA"的设备Shell
[bi4vmr@Fedora ~]$ adb -s 892QAEVCM9NEA shell
```

> 🚩 提示
>
> 命令语句中的 `-s <设备标识符>` 选项必须紧接着 `adb` 命令书写，如果书写在其他位置会出现错误。

## ADB Shell
Android设备的Shell与PC的Shell是类似的，可以通过命令查看信息或操纵设备。我们在ADB Shell中只能访问Android设备的文件，如果需要访问PC中的文件（例如：传输文件、安装APK等），可以在PC的Shell中通过 `adb` 命令进行操作。

我们可以在PC的终端中执行 `adb shell` 命令进入Android设备的Shell，此时命令提示符将会发生改变，接下来即可进行交互式操作；当我们想要返回PC的终端时，需要按下 `Ctrl+D` 组合键或执行 `exit` 命令。

```text
# 进入Android设备的Shell
[bi4vmr@Fedora ~]$ adb -s shell
ADB $

# 按下"Ctrl+D"组合键退出ADB Shell
ADB $ ^D
[bi4vmr@Fedora ~]$
```

有时我们希望使用ADB Shell执行命令语句，但仍然保持终端环境为PC的Shell，此时可以使用 `adb shell "<命令语句...>"` 命令。这是ADB Shell的非交互式用法，我们可以在CMD或Bash脚本中使用。

```text
# 在Android设备的Shell中执行"free -h"，但不进入Shell。
[bi4vmr@Fedora ~]$ adb shell "free -h"
                total        used        free      shared     buffers
Mem:             5.5G        5.1G        363M        2.6M        125M
-/+ buffers/cache:           5.0G        487M
Swap:            2.0G        847M        1.1G
[bi4vmr@Fedora ~]$
```

根据上述输出内容可知：

PC的终端显示了Android设备 `free -h` 的执行结果，但并未进入ADB Shell，命令提示符仍然为PC终端的状态。

## 文件传输
### 向Android端传输文件
我们可以使用 `adb push <PC端文件或目录路径> <Android端目标路径>` 命令将PC中的文件传输至Android设备。

```text
# 将PC当前目录中的"test.txt"文件复制到Android设备中
[bi4vmr@Fedora ~]$ adb push test.txt "/storage/emulated/0/"
test.txt: 1 file pushed, 0 skipped. 0.3 MB/s (96 bytes in 0.000s)
```

如果文件传输失败，通常是因为ADB Shell的默认用户"shell"对目标路径无写入权限。对于拥有Root权限的设备，我们可以先执行 `adb root; adb remount;` 命令提升权限后再尝试传输。

PC端的文件路径中允许使用通配符，此时可以仅传输符合规则的部分文件。

```text
# 将PC当前目录中所有以".apk"结尾的文件复制到Android设备中
[bi4vmr@Fedora ~]$ adb push *.apk "/storage/emulated/0/"
demo01.apk: 1 file pushed, 0 skipped.
demo02.apk: 1 file pushed, 0 skipped.
2 files pushed, 0 skipped.
```

当我们使用通配符时，PC端的路径不能被双引号包围，这意味着通配符无法与带有空格的路径并存。

### 向PC端传输文件
我们可以使用 `adb pull <Android端目标路径> <PC端文件或目录路径>` 命令将Android设备中的文件传输至PC。

```text
# 将Android设备中的"test.txt"文件复制到PC的当前目录
[bi4vmr@Fedora ~]$ adb pull "/storage/emulated/0/test.txt"
test.txt: 1 file pushed, 0 skipped.

# 将Android设备中的"test.txt"文件复制到PC的指定目录
[bi4vmr@Fedora ~]$ adb pull "/storage/emulated/0/test.txt" "Downloads/"
test.txt: 1 file pushed, 0 skipped.
```

`adb pull` 命令可以传输单个文件或具有层级的目录，但无法使用通配符。

## 截屏与录屏
### 截屏
我们可以使用ADB Shell中的 `screencap` 命令截取设备屏幕内容并保存至文件。

```text
# 截取屏幕内容，并保存至设备SD卡根目录。
[bi4vmr@Fedora ~]$ adb shell "screencap /storage/emulated/0/screenshot.png"

# 将图片复制到PC
[bi4vmr@Fedora ~]$ adb pull "/storage/emulated/0/screenshot.png"
```

`screencap` 命令的常用选项详见下文内容：

🔷 `-p`

指定目标文件格式为PNG。

该命令只能生成PNG文件，当文件路径带有".png"后缀时可以省略该选项；当未指明后缀时必须添加该选项。

🔷 `-d <显示器ID>`

该命令默认截取首个显示器的图像，如果我们希望截取其他显示器的图像，则需要添加该选项。

### 录屏
我们可以使用ADB Shell中的 `screenrecord` 命令录制设备屏幕内容为视频文件。

```text
# 开启屏幕录制，并保存至设备SD卡根目录。
[bi4vmr@Fedora ~]$ adb shell "screenrecord /storage/emulated/0/screenrecord.mp4"

# 将录像复制到PC
[bi4vmr@Fedora ~]$ adb pull "/storage/emulated/0/screenrecord.mp4"
```

`screenrecord` 命令的常用选项见下文：

🔶 `--size <宽度（像素）>x<高度（像素）>`

指定分辨率。

🔶 `--bit-rate <比特率>`

指定比特率。

默认的比特率为4Mbps，我们自行设置比特率时，可以添加"K"或"M"后缀。

🔶 `--time-limit <时间限制（秒）>`

指定最大录制时长。

系统支持的最大时长为180秒。

## 软件包管理
### 安装
我们可以使用 `adb install <APK文件路径>` 命令安装存储在PC上的软件包。

`adb install` 命令的常用选项见下文：

🔷 `-r`

覆盖安装。

🔷 `-d`

降级安装。

🔷 `-t`

允许安装测试APK。

🔷 `-g`

为应用程序自动授予所有运行时权限。

### 卸载
我们可以使用 `adb uninstall <应用包名>` 命令卸载指定的软件包。

如果我们不希望卸载应用时删除数据文件，可以使用 `-k` 选项。


# 在APP中使用ADB Shell
我们可以在应用程序中执行ADB Shell命令，以便查询系统信息或修改配置项。

🔴 示例一：在应用程序中执行ADB命令。

在本示例中，我们通过Java提供的接口在应用程序中执行ADB命令。

在Android环境中执行命令的方法与Java环境是类似的，我们需要通过 `Runtime.getRuntime().exec(String cmd)` 执行命令。

"TestUIBase.java":

```java
// 命令语句
final String cmd = "free -h";

try {
    // 执行命令
    Process process = Runtime.getRuntime().exec(cmd);
    // 阻塞当前线程等待命令执行完毕
    int resultCode = process.waitFor();
    Log.i(TAG, "'free -h'命令的执行结果:[" + resultCode + "]");
} catch (Exception e) {
    e.printStackTrace();
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIBaseKT.kt":

```kotlin
// 命令语句
val cmd = "free -h"

runCatching {
    // 执行命令
    val process: Process = Runtime.getRuntime().exec(cmd)
    // 阻塞当前线程等待命令执行完毕
    val resultCode: Int = process.waitFor()
    Log.i(TAG, "'free -h'命令的执行结果:[$resultCode]")
} catch (e: Exception) {
    e.printStackTrace()
}
```

命令的执行结果是一个数字，值为"0"时表示执行成功；值不为"0"时表示执行失败，各个错误码的含义由命令所属软件定义。如果我们需要获取命令的执行结果，可以使用Process实例的 `waitFor()` 方法，它会阻塞当前作用域并在命令执行结束时返回状态码。

命令的输出信息通过InputStream反馈给当前应用程序，Process实例的 `getInputStream()` 方法用于获取标准信息输出内容； `getErrorStream()` 方法则用于获取标准错误输出内容，我们可以根据需要选择。

"TestUIBase.java":

```java
if (resultCode == 0) {
    /* 命令执行成功 */
    InputStream isStdOut = process.getInputStream();
    String text = IOUtil.readFile(isStdOut);
    Log.i(TAG, "标准信息输出：\n" + text);
} else {
    /* 命令执行失败 */
    InputStream isStdErr = process.getErrorStream();
    String text = IOUtil.readFile(isStdErr);
    Log.i(TAG, "标准错误输出：\n" + text);
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIBaseKT.kt":

```kotlin
if (resultCode == 0) {
    /* 命令执行成功 */
    val isStdOut: InputStream = process.inputStream
    val text: String = IOUtil.readFile(isStdOut)
    Log.i(TAG, "标准信息输出：\n$text")
} else {
    /* 命令执行失败 */
    val isStdErr: InputStream = process.errorStream
    val text: String = IOUtil.readFile(isStdErr)
    Log.i(TAG, "标准错误输出：\n$text")
}
```

此时运行示例程序，并查看控制台输出信息：

```text
18:41:53.648 21865 21865 I TestUIBase: 'free -h'命令的执行结果:[0]
18:41:53.651 21865 21865 I TestUIBase: 标准信息输出：
18:41:53.652 21865 21865 I TestUIBase:          total        used        free      shared     buffers
Mem:             5.5G        5.0G        502M        1.4M         80M
-/+ buffers/cache:           4.9G        581M
Swap:            2.0G        0.9G        1.0G
```


# 疑难解答
## 索引

<div align="center">

|       序号        |                      摘要                       |
| :---------------: | :---------------------------------------------: |
| [案例一](#案例一) | 在Linux环境中，PC无法识别通过USB连接的ADB设备。 |

</div>

## 案例一
### 问题描述
Android设备已经开启了ADB，当我们将其使用USB方式连接至Linux PC时，PC无法识别该设备。

### 问题分析
部分版本的Linux系统没有内置Android设备对应的USB配置，因此无法识别设备。

### 解决方案
以下项目包含众多Android设备厂商对应的USB识别码：

[🔗 GitHub - 51-android - snowdream](https://github.com/snowdream/51-android)

我们可以访问该项目的网页，并下载 `51-android` 配置文件，然后将其放置到PC的 `/etc/udev/rules.d/` 目录中，打开终端执行 `systemctl restart udevd` 命令加载配置文件。
