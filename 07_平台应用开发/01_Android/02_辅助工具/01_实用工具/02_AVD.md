# 简介
AVD(Android Virtual Device)是Android SDK提供的Android模拟器，基于QEMU实现，能够模拟不同屏幕尺寸、分辨率、系统版本、硬件架构的Android设备，以便开发者对应用程序进行调试。

# 虚拟网络
默认情况下，AVD的虚拟网络工作在NAT模式，虚拟机可以访问互联网与主机网络。

主机在虚拟网络中的IP是 `10.0.2.2` 和 `10.0.2.3` ，如果主机在8080端口上开放了HTTP服务，虚拟机便可以通过 `http://10.0.2.2:8080` 访问该服务。

# 查看错误日志
有时AVD会启动失败并显示弹窗： `The emulator process for AVD <虚拟机名称> has terminated.` ，这是由于系统环境或配置出现问题导致的，但此处没有更多详细信息供我们排查具体原因。

我们可以在Android Studio的用户数据目录中找到日志文件，并查找与AVD有关的错误信息：

- Windows平台 : `%USERPROFILE%\AppData\Local\Google\<Android Studio版本号>\log\`
- Linux平台 : `~/.cache/Google/<Android Studio版本号>/log/`

下文代码块展示了一个AVD启动失败的错误信息示例：

```text
2024-07-10 09:49:27,745 [ 976238] INFO - Emulator: Pixel - Android emulator version 34.2.15.0 (build_id 11906825) (CL:N/A)
2024-07-10 09:49:27,745 [ 976238] INFO - Emulator: Pixel - Found systemPath /home/bi4vmr/Software/Android-SDK/system-images/android-33/default/x86_64/
2024-07-10 09:49:27,751 [ 976244] INFO - Emulator: Pixel - /home/bi4vmr/Software/Android-SDK/emulator/qemu/linux-x86_64/qemu-system-x86_64: /lib/x86_64-linux-gnu/libpthread.so.0: version GLIBC_2.30' not found (required by /home/bi4vmr/Software/Android-SDK/emulator/lib64/qt/lib/libQt6WebEngineCoreAndroidEmu.so.6)
```

根据上述输出内容可知：

当前系统的"libc"库版本低于AVD所要求的版本，因此虚拟机无法启动成功，此时需要升级系统或将AVD降级至旧版本。

# 安装指定版本

以下页面记录了AVD的历史版本，可以选择一个进行下载，并解压到

https://developer.android.com/studio/emulator_archive

上述页面只有Windows版本，如果是Linux系统，可以先查看目标版本号，然后将版本号替换至以下链接进行下载：

https://dl.google.com/android/repository/emulator-linux_x64-<目标版本号>.zip

将原本SDK中的AVD改名，解压下载的文件至Android-SDK/emulator/，然后将原本SDK中的package.xml文件复制到下载的目录中，打开该文件将版本号修改为与当前版本一致：

```xml
<ns2:repository ...>
    <localPackage ...>
        <revision>
            <major>33</major>
            <minor>1</minor>
            <micro>24</micro>
        </revision>
    </localPackage>
</ns2:repository>
```
