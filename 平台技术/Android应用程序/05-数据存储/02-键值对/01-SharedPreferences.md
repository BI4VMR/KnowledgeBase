# 简介
SharedPreferences是Android SDK提供的轻量级键值对存储工具，可以持久化存储一些结构简单的配置信息，例如：用户名、用户身份标识、是否记住密码等，此工具支持存储基本类型、String和Set&lt;String&gt;类型的数据。

SharedPreferences的数据实际上存储在XML文件中，路径位于"/data/data/&lt;应用程序包名&gt;/shared_prefs"。当应用程序启动时，系统会读取XML文件并将数据加载至内存中，因此不能使用SharedPreferences存储大量的数据，否则会导致程序卡顿。

SharedPreferences具有以下特点，我们需要在合适的场合使用它。

🔷 效率较低

即使我们只修改文件中的一个键值对，系统也要把完整的内容向外存储器写入一遍，因此不适合存取较大规模的数据。

🔷 进程不安全

SharedPreferences没有跨进程的锁，因此程序拥有多个进程时应谨慎使用，以防数据出现错误。

🔷 不支持加密存储

SharedPreferences没有加密机制，设备Root后可以被随意读取，所以不要用于存储敏感信息。

# 存入数据
若要使用SharedPreferences，首先需要获取SharedPreferences实例，系统提供了以下几种方式供开发者使用：

🔷 Context类的 `getSharedPreferences()` 方法

需要传入XML文件名称和存取模式参数。文件名参数不需要包含后缀".xml"，若指定的文件不存在，系统将自动创建空文件；存取模式必须为"Context.MODE_PRIVATE"，其它值均已被废弃。

🔷 Activity类的 `getPreferences()` 方法

将自动以当前Activity类名为文件名，只需要传入存取模式参数即可。

🔷 PreferenceManager类的 `getDefaultSharedPreferences()` 方法

以"preferences"为文件名，目前已被废弃，不建议再使用。

写入数据前，需要先获取Editor实例，然后通过"put"系列方法，存入键名和对应类型的值，最后使用 `apply()` 或 `commit()` 方法提交，持久化存储数据。

```java
// 获取SharedPreferences实例
SharedPreferences sp = getSharedPreferences("test", Context.MODE_PRIVATE);
// 获取Editor实例
SharedPreferences.Editor editor = sp.edit();
// 存入数据
editor.putInt("DATA_INT", 100);
editor.putBoolean("DATA_BOOL", true);
editor.putString("DATA_STRING", "SP Test.");
// 提交数据
editor.apply();
```

`apply()` 方法先修改内存中的值，然后通过异步方式写入数据至磁盘； `commit()` 函数则会使用当前线程立刻写入数据至磁盘，因此通常不在主线程调用它，防止引起阻塞。 `commit()` 方法有返回值，表示数据是否成功持久化，该方法适用于数据一致性要求高的场合。

成功提交数据后，我们可以进入应用数据目录，找到"test.xml"并查看其中的内容。

```xml
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <boolean name="DATA_BOOL" value="true" />
    <int name="DATA_INT" value="100" />
    <string name="DATA_STRING">SP Test.</string>
</map>
```

# 读取数据
SharedPreferences实例的"get"系列方法可以读取数据，需要传入键名和默认值两个参数，系统根据键名查找键值对，并返回其数值；若系统未找到键名对应的数据，就会返回传入的默认值。

```java
// 获取SharedPreferences实例
SharedPreferences sp = getSharedPreferences("test", Context.MODE_PRIVATE);
// 根据键名取出数值
int i = sp.getInt("DATA_INT", 100);
boolean b = sp.getBoolean("DATA_BOOL", false);
String s = sp.getString("DATA_STRING", "Not Found.");
```
