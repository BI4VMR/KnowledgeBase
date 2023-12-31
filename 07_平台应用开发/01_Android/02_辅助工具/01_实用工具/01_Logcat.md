# 简介
Logcat是系统内置的日志信息工具，可以查看系统输出的日志信息。

# 输出日志
我们可以在应用程序的Java代码中输出日志以便进行调试。使用日志工具时，需要先导入 `android.util.Log` 包，然后调用Log类中的静态方法输出日志。日志的基本格式分为两部分，第一部分为标签(Tag)，便于检索信息；第二部分为消息的内容。

```java
// 输出Verbose级别日志
Log.v("Test", "Verbose");
//输出Debug级别日志
Log.d("Test", "Debug");
//输出Info级别日志
Log.i("Test", "Info");
//输出Warn级别日志
Log.w("Test", "Warn");
//输出Error级别日志
Log.e("Test", "Error");
```

# 查看日志
我们可以在Android Studio中查看虚拟机或实体机输出的日志信息：

![Android Studio工具面板](./Assets_Logcat/查看日志_AndroidStudio工具面板.jpg)

日志信息按重要程度从低到高分为Verbose、Debug、Info、Warn、Error和Assert，当我们选择某个级别时，将会筛选出本级别及以上的信息。

<!-- TODO
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
是因为相邻的几行打印内容完全相同，从Android O开始Log的chatty机制，会把中间的重复内容不再打印。而是打印类似如上的 ”identical 391 lines“ ，告知有多少行的日志是一样的，这不是错误，只是减少了重复打印的次数。


```java
        for (int i = 0; i < 100; i++) {
            Log.i("TestApp", "日志输出示例。");
        }
```


```text
12-16 19:00:48.117 18300 18300 I TestApp : 日志输出示例。
12-16 19:00:48.118 18300 18300 I chatty  : uid=10147(net.bi4vmr.study.sys.contentprovider.server) identical 98 lines
12-16 19:00:48.118 18300 18300 I TestApp : 日志输出示例。
```


 -->
 