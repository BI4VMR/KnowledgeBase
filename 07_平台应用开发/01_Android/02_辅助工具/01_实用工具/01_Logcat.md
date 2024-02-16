# 简介
Logcat是系统内置的日志信息工具，可以查看系统输出的日志信息。

# 输出日志
我们可以在应用程序的Java代码中输出日志以便进行调试。使用日志工具时，需要先导入 `android.util.Log` 包，然后调用Log类中的静态方法输出日志。日志的基本格式分为两部分，第一部分为标签(Tag)，便于检索信息；第二部分为消息的内容。

```java
// 输出Verbose级别日志
Log.v(TAG, "Verbose");
// 输出Debug级别日志
Log.d(TAG, "Debug");
// 输出Info级别日志
Log.i(TAG, "Info");
// 输出Warn级别日志
Log.w(TAG, "Warn");
// 输出Error级别日志
Log.e(TAG, "Error");
```




```text
02-16 15:51:43.827 17872 17872 V TestApp-TestUIBase: Verbose Log.
02-16 15:51:43.827 17872 17872 D TestApp-TestUIBase: Debug Log.
02-16 15:51:43.827 17872 17872 I TestApp-TestUIBase: Info Log.
02-16 15:51:43.827 17872 17872 W TestApp-TestUIBase: Warn Log.
02-16 15:51:43.827 17872 17872 E TestApp-TestUIBase: Error Log.
```


# 查看日志
我们可以在Android Studio中查看虚拟机或实体机输出的日志信息：

![Android Studio工具面板](./Assets_Logcat/查看日志_AndroidStudio工具面板.jpg)

日志信息按重要程度从低到高分为Verbose、Debug、Info、Warn、Error和Assert，当我们选择某个级别时，将会筛选出本级别及以上的信息。

# 调试命令




```text
12-16 17:54:00.412  2018 19420 W InputReader: Device has associated, but no associated display id.
12-16 17:54:01.357  2018  2069 I ActivityTaskManager: START u0 {act=android.intent.action.MAIN cat=[android.intent.category.HOME] flg=0x10000100 cmp=com.android.launcher3/.Launcher (has extras)} from uid 0
12-16 17:54:00.412  2018 19420 I chatty  : uid=1000(system) Binder:2018_1D identical 8 lines
12-16 17:54:00.412  2018 19420 W InputReader: Device has associated, but no associated display id.
12-16 17:54:01.632  2018  4805 W InputReader: Device has associated, but no associated display id.
12-16 17:54:01.632  2018  4805 I chatty  : uid=1000(system) Binder:2018_F identical 8 lines
12-16 17:54:01.632  2018  4805 W InputReader: Device has associated, but no associated display id.
12-16 17:54:04.411  1752  1881 W audio_hw_generic: Not supplying enough data to HAL, expected position 50888065 , only wrote 50697378
```




```text
# 清空缓冲区中的已有内容
ADB_Shell$ logcat -c


ADB_Shell$ logcat --pid=<pid>



暂时设置所有缓冲区大小为4M，立即生效，但是ACC OFF/ON后失效
logcat -G 4M



永久设置所有缓冲区大小为8M，重启生效
setprop persist.logd.size 8M


logcat -g

```


logcat -v <format>

    1

格式	说明
brief	显示优先级/标记和过程的PID发出的消息（默认格式）
process	只显示PID
tag	只显示优先级/标记
raw	显示原始的日志消息，没有其他元数据字段
time	调用显示日期、时间、优先级/标签和过程的PID发出消息
threadtime	调用显示日期、时间、优先级、标签遗迹PID TID线程发出的消息
long	显示所有元数据字段与空白行和单独的消息







# Chatty机制
为了减轻I/O负载，从Android O开始Log的chatty机制，如果应用程序连续输出三行或以上完全相同的日志，系统并不会原样输出所有内容，而是将中间的行全部忽略，替换为一行Tag为"chatty"的日志，其中的数字表明了被折叠的行数。



```java
// 连续输出100行相同的日志
for (int i = 0; i < 100; i++) {
    Log.i(TAG, "Chatty机制测试内容。");
}
```


```text
02-16 15:50:25.999 17621 17621 I TestApp-TestUIBase: Chatty机制测试内容。
02-16 15:50:26.001 17621 17621 I chatty  : uid=10182(net.bi4vmr.study.tool.common.logcat) identical 98 lines
02-16 15:50:26.001 17621 17621 I TestApp-TestUIBase: Chatty机制测试内容。
```
