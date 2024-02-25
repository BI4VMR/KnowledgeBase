# 简介
Logcat是系统内置的日志信息工具，它提供了日志输出、实时查看、持久化记录等功能。

# 查看日志
## ADB Shell
### 基本应用
我们可以在ADB Shell中执行 `logcat` 命令实时查看日志信息。

```text
ADB$ logcat
12-16 17:54:00.412  2018 19420 W InputReader: Device has associated, but no associated display id.
12-16 17:54:01.357  2018  2069 I ActivityTaskManager: START u0 {act=android.intent.action.MAIN cat=[android.intent.category.HOME] flg=0x10000100 cmp=com.android.launcher3/.Launcher (has extras)} from uid 0
12-16 17:54:00.412  2018 19420 W InputReader: Device has associated, but no associated display id.
12-16 17:54:04.411  1752  1881 W audio_hw_generic: Not supplying enough data to HAL, expected position 50888065 , only wrote 50697378
```

上述内容为Logcat默认的输出格式，每列内容的含义分别为：日期、时间、进程ID、线程ID、日志级别、日志Tag、日志内容。

我们可以通过 `logcat -v <格式名称>` 命令更改日志输出格式，每种格式名称及其内容详见下文表格：

<div align="center">

|   格式名称   |                      内容                       |
| :----------: | :---------------------------------------------: |
|   `brief`    |                    默认格式                     |
|    `time`    |     日期、时间、日志级别、日志Tag、进程ID。     |
| `threadtime` | 日期、时间、进程ID、线程ID、日志级别、日志Tag。 |
|    `tag`     |               日志级别、日志Tag。               |
|  `process`   |               日志级别、进程ID。                |
|    `long`    | 默认格式，但时间等信息与日志内容分为两行显示。  |
|    `raw`     |          只包含日志内容，无其他信息。           |

</div>

<!-- TODO
### 筛选关键日志
ADB_Shell$ logcat --pid=<pid>
-->

### 日志缓冲区
Logcat会在内存中暂存部分历史日志，有时系统日志输出频率过快，我们便无法筛选出有效信息，此时需要调整缓冲区大小，使内存中暂存更多的日志信息。

```text
# 暂时设置缓冲区大小为8M
# 仅当前启动生效，系统重启后将还原为修改前的值。
ADB$ logcat -G 8M

# 永久设置缓冲区大小为8M（需要Root权限）
# 永久生效，但需要重启系统才会加载新配置，我们可以配合"logcat -G"命令避免重启。
ADB$ setprop persist.logd.size 8M

# 查询配置文件中的缓冲区大小
ADB$ getprop persist.logd.size 8M
```

对于无法取得Root权限的设备，我们可以更改“开发者选项”页面中的 `日志记录器缓冲区大小` 配置项实现配置持久化。

当我们需要实时筛选日志以便确认问题时，可能会被日志缓冲区中的历史内容所干扰，此时我们可以使用 `logcat -c` 命令清除缓冲区再筛选新的日志。

```text
# 级联命令：清空缓冲区再实时筛选日志。
ADB$ logcat -c; logcat | grep -iE "<关键字>"
```

## Android Studio
若设备可以通过USB或IP网络连接到电脑，我们可以通过Android Studio的Logcat面板查看日志信息。

![Android Studio工具面板](./Assets_Logcat/查看日志_AndroidStudio工具面板.jpg)

日志信息按重要程度从低到高分为Verbose、Debug、Info、Warn、Error和Assert，当我们选择某个级别时，将会筛选出本级别及更高级别的信息。

## 日志文件
大部分系统会将一段时间内的日志写入到文件中，以便开发者查看，通常日志目录的名称为 `/data/log/` ，该路径在不同的系统中可能并不相同。

除了Logcat日志记录外，以下目录还包括一些额外的日志信息，我们可以根据需要查看。

- `/data/system/dropbox/` : 应用程序崩溃日志。
- `/data/anr/` : 应用程序ANR日志。
- `/data/tombstones/` : 应用程序Native库异常日志。

# 输出日志
我们可以在应用程序中输出日志到Logcat以便调试。

在使用日志工具前，我们需要先导入 `android.util.Log` 包，然后调用Log类中的一系列静态方法输出日志。这些方法均具有两个参数，第一参数为日志Tag，便于我们检索信息，通常为组件名称；第二参数为消息内容字符串。

"TestUIBase.java":

```java
// 输出Verbose级别日志
Log.v(TAG, "Verbose Log.");
// 输出Debug级别日志
Log.d(TAG, "Debug Log.");
// 输出Info级别日志
Log.i(TAG, "Info Log.");
// 输出Warn级别日志
Log.w(TAG, "Warn Log.");
// 输出Error级别日志
Log.e(TAG, "Error Log.");
```

此时运行示例程序，并查看控制台输出信息：

```text
15:51:43.827 17872 17872 V TestApp-TestUIBase: Verbose Log.
15:51:43.827 17872 17872 D TestApp-TestUIBase: Debug Log.
15:51:43.827 17872 17872 I TestApp-TestUIBase: Info Log.
15:51:43.827 17872 17872 W TestApp-TestUIBase: Warn Log.
15:51:43.827 17872 17872 E TestApp-TestUIBase: Error Log.
```

# Java日志兼容性
如果我们需要编写不包含Android组件依赖的纯Java组件，就无法使用Logcat工具输出日志了，此时只能使用Java的控制台输出指令。

Java中的标准信息输出语句 `System.out.print()` 在Logcat中Tag为"System.out"，而错误信息输出语句 `System.err.print()` 在Logcat中Tag为"System.err"，我们可以通过以下ADB命令筛选这两种日志内容：

```text
ADB$ logcat -c; logcat | grep -iE "System.out|System.err"
22:59:33.325 19296 19296 I System.out: 标准信息输出测试。
22:59:33.325 19296 19296 W System.err: 错误信息输出测试。
```

# Chatty机制
为了减轻I/O负载，从Android O开始Logcat新增了Chatty机制。

如果应用程序短时间内连续输出3行或更多完全相同的日志，系统并不会原样输出所有内容，而是将中间的行全部折叠，替换为一行Tag为"chatty"的内容，其中的数字表明了被折叠的行数。

我们可以连续输出100行相同的日志来测试Chatty机制。

"TestUIBase.java":

```java
// 连续输出100行相同的日志
for (int i = 0; i < 100; i++) {
    Log.i(TAG, "Chatty机制测试内容。");
}
```

此时运行示例程序，并查看控制台输出信息：

```text
15:50:25.999 17621 17621 I TestApp-TestUIBase: Chatty机制测试内容。
15:50:26.001 17621 17621 I chatty  : uid=10182(net.bi4vmr.study.tool.common.logcat) identical 98 lines
15:50:26.001 17621 17621 I TestApp-TestUIBase: Chatty机制测试内容。
```

<!-- TODO
# 处理超长的行
message的内存分配大概是4k左右

实测为4047个字符，中文1350个字符
-->
