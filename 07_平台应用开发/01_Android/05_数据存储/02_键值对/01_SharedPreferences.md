# 简介
SharedPreferences是Android SDK提供的轻量级键值对存储工具，可以持久化存储一些结构简单的配置信息，例如：用户名、用户身份标识、是否记住密码等。

SharedPreferences支持存储基本类型、String类型和 `Set<String>` 类型的数据。对于JSON等支持文本与对象互相转换的格式，我们可以以String类型进行存取。

SharedPreferences的数据实际上存储在XML文件中，路径为 `/data/data/<应用程序包名>/shared_prefs` 。当应用程序启动时，系统会读取XML文件并将数据加载至内存中，因此我们不能使用该工具存储大量的数据，否则会导致程序运行缓慢。

SharedPreferences具有以下缺点：

🔷 效率较低

即使我们只修改配置文件中的一个键值对，系统也要把完整的内容向存储器中写入一遍，因此不适合需要频繁修改的场合。

🔷 不支持跨进程

SharedPreferences没有跨进程的锁，因此多进程环境下无法使用。

🔷 不支持加密存储

SharedPreferences没有加密机制，设备Root后可以被随意读取，所以我们不应该用它存储敏感信息。

<br />

因此我们需要在合适的场合使用它，不可滥用。

本章示例工程详见以下链接：

[🔗 示例工程：SharedPreferences](https://github.com/BI4VMR/Study-Android/tree/master/M05_Storage/C02_KV/S01_SharedPreferences)

# 基本应用
## 获取SharedPreferences实例
若要使用SharedPreferences读写数据，我们首先需要获取SharedPreferences实例，系统提供了以下几种方式供开发者选择：

- Context类的 `getSharedPreferences(String name, int mode)` 方法：第一参数 `name` 表示数据文件名称，不需要包含后缀".xml"。第二参数 `mode` 只能填写为 `Context.MODE_PRIVATE` 。
- Activity类的 `getPreferences(int mode)` 方法：以当前Activity类名作为数据文件的名称。

## 存入数据
在写入数据之前，我们需要通过SharedPreferences实例的 `edit()` 方法获取Editor实例，然后调用它的"put"系列方法，向XML文件存入键名和对应类型的值。

"TestUIBase.java":

```java
// 获取SharedPreferences实例
SharedPreferences sp = getSharedPreferences("kvdata", Context.MODE_PRIVATE);
// 获取Editor实例
SharedPreferences.Editor editor = sp.edit();
// 存入数据
editor.putInt("Data_Int", 100);
editor.putBoolean("Data_Boolean", true);
editor.putString("Data_String", "我能够吞下玻璃而不伤身。");
// 提交数据
editor.apply();
```

上述内容也可以使用Kotlin语言书写：

"TestUIBaseKT.kt":

```kotlin
// 获取SharedPreferences实例
val sp: SharedPreferences = getSharedPreferences("kvdata", MODE_PRIVATE)
// 获取Editor实例
val editor: SharedPreferences.Editor = sp.edit()
// 存入数据
editor.putInt("Data_Int", 100)
editor.putBoolean("Data_Boolean", true)
editor.putString("Data_String", "我能够吞下玻璃而不伤身。")
// 提交变更
editor.apply()
```

Editor的"put"系列方法形式是相似的，第一个参数为数据项的键名，若不存在该项系统会自动创建；若已存在该项，则覆盖旧的值。第二个参数为数据项的值。

Editor的 `apply()` 和 `commit()` 方法都能实现数据持久化，`apply()` 方法先修改内存中的值，然后通过异步方式写入数据至磁盘； `commit()` 方法则会使用当前线程立刻写入数据至磁盘，因此我们通常不在主线程调用它，防止引起界面卡顿。 `commit()` 方法有返回值，表示数据是否成功写入，该方法适用于数据一致性要求高的场合。

此时运行示例程序，并进入应用程序的数据目录，查看 `kvdata.xml` 文件的内容。

"kvdata.xml":

```xml
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <boolean name="DATA_BOOL" value="true" />
    <int name="DATA_INT" value="100" />
    <string name="DATA_STRING">SP Test.</string>
</map>
```

根据上述文件内容可知：

示例代码中保存的三条数据，已经成功被写入文件，完成了持久化操作。

# 读取数据
SharedPreferences实例的"get"系列方法可以读取数据，我们需要依次传入键名和默认值两个参数，系统将根据键名查找数据项，若该项存在，则返回对应的值；若该项不存在，则返回默认值。

"TestUIBase.java":

```java
// 获取SharedPreferences实例
SharedPreferences sp = getSharedPreferences("kvdata", Context.MODE_PRIVATE);
// 读取数值
int i = sp.getInt("Data_Int", 0);
boolean b = sp.getBoolean("Data_Boolean", false);
String s = sp.getString("Data_String", "Empty.");
```

上述内容也可以使用Kotlin语言书写：

"TestUIBaseKT.kt":

```kotlin
// 获取SharedPreferences实例
val sp: SharedPreferences = getSharedPreferences("kvdata", MODE_PRIVATE)
// 读取数值
val i: Int = sp.getInt("Data_Int", 0)
val b: Boolean = sp.getBoolean("Data_Boolean", false)
val s: String? = sp.getString("Data_String", "Empty.")
```


# API列表


🔶 Context类的 `getSharedPreferences(String name, int mode)`

功能简述：

获取SharedPreferences实例。

参数列表：

- `name` : 数据文件名称，不需要包含后缀".xml"。若目标文件不存在，首次访问时系统将自动创建一个空文件。
- `mode` : 存取模式，值为"Context.MODE_"系列常量，目前只能使用 `Context.MODE_PRIVATE` ，其它值已被废弃。

返回值：

SharedPreferences实例。

🔶 Activity类的 `getPreferences(int mode)` 方法

功能简述：

获取SharedPreferences实例，以当前Activity类名作为数据文件名称。

参数列表：

- `mode` : 存取模式。

返回值：

SharedPreferences实例。

🔶 PreferenceManager类的 `SharedPreferences getDefaultSharedPreferences(Context context)` 方法

功能简述：

获取SharedPreferences实例，以"preferences"作为数据文件名称。

该方法目前已被废弃，不建议使用。

参数列表：

- `context` : 上下文。

返回值：

SharedPreferences实例。

<br />
