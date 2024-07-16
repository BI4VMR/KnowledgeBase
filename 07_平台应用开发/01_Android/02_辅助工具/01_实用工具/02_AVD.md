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

# 安装指定版本的AVD
当系统环境等因素有限制时，我们只能运行特定版本的AVD，但SDK管理器中默认只显示最新版本的AVD，无法直接降级至早期版本，此时就需要手动进行降级。

安装指定版本AVD的操作步骤如下文内容所示：

第一步，访问 [🧭 AVD历史版本存档](https://developer.android.com/studio/emulator_archive) 网页，选择一个旧版AVD并下载压缩包，例如 `33.1.24` 。

第二步，进入Android SDK目录，将当前已经存在的AVD目录修改为其他名称，例如将 `<SDK根目录>/emulator/` 改为 `<SDK根目录>/emulator_bak/` 。最好不要删除当前版本的AVD文件，它们在后续步骤中还有作用。

第三步，将压缩包中的 `emulator` 目录解压缩到SDK目录中。

第四步，进入“第二步”中已被重命名的原始AVD目录，将 `package.xml` 文件复制到“第三步”解压缩后的 `emulator` 目录中，然后修改该文件，将其中的版本号改为与当前AVD版本号一致：

"package.xml":

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

此时目标版本的AVD就安装完成了，我们可以尝试创建并启动虚拟机。

> 🚩 提示
>
> 上文提到的“AVD历史版本存档”网页中，只存在Windows平台的AVD程序包；若我们正在使用的是Linux平台，可以先访问网页查看目标AVD版本的Windows程序包下载地址，再将版本号部分拼接至SDK下载地址，下载对应的Linux程序包：
>
> 若我们希望安装AVD 33.1.24，存档网页显示的Windows程序包下载地址为：
>
> `https://redirector.gvt1.com/edgedl/android/repository/emulator-windows_x64-11237101.zip`
>
> 此处的"11237101"即为版本号，我们将其填入以下URL即可下载对应的Linux程序包：
>
> `https://dl.google.com/android/repository/emulator-linux_x64-11237101.zip`
