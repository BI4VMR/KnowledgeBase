# 简介
ComponentName是Android系统提供的一个用于标识应用组件的类，它由包名（Package Name）和类名（Class Name）两部分组成，能够在系统范围内唯一地定位一个Activity、Service、BroadcastReceiver或ContentProvider。

在使用显式Intent启动组件时，我们需要明确指定目标组件；在通过PackageManager查询或操作特定组件时，也需要用ComponentName来标识目标。ComponentName正是承担这一职责的数据载体。

ComponentName实现了Parcelable接口，因此可以通过Intent或Bundle在组件之间传递。

# 构造方法
ComponentName提供了以下几种构造方法：

🔷 `ComponentName(String pkg, String cls)`

通过包名字符串和类名字符串创建实例。

- 参数 `pkg` ：目标组件所在应用的包名，例如 `"net.bi4vmr.study"` 。
- 参数 `cls` ：目标组件的完整类名，例如 `"net.bi4vmr.study.MainActivity"` 。

🔷 `ComponentName(Context pkg, String cls)`

通过Context对象和类名字符串创建实例，包名会自动从Context中获取。

- 参数 `pkg` ：Context对象，用于获取当前应用的包名。
- 参数 `cls` ：目标组件的完整类名。

🔷 `ComponentName(Context pkg, Class<?> cls)`

通过Context对象和Class对象创建实例，包名与类名均自动获取。

- 参数 `pkg` ：Context对象，用于获取当前应用的包名。
- 参数 `cls` ：目标组件的Class对象，例如 `MainActivity.class` 。

# 常用方法
🔷 `String getPackageName()`

获取包名。

🔷 `String getClassName()`

获取完整类名，例如 `"net.bi4vmr.study.MainActivity"` 。

🔷 `String getShortClassName()`

获取简短类名。若类名以包名开头，则返回省略包名前缀的形式，例如 `".MainActivity"` ；否则返回完整类名。

🔷 `String flattenToString()`

将ComponentName序列化为字符串，格式为 `"<包名>/<完整类名>"` ，例如 `"net.bi4vmr.study/net.bi4vmr.study.MainActivity"` 。常用于日志输出或持久化存储。

🔷 `String flattenToShortString()`

将ComponentName序列化为短格式字符串，格式为 `"<包名>/<简短类名>"` ，例如 `"net.bi4vmr.study/.MainActivity"` 。

�� `static ComponentName unflattenFromString(String str)`

将 `flattenToString()` 或 `flattenToShortString()` 生成的字符串反序列化为ComponentName对象。若字符串格式不合法，则返回 `null` 。

🔷 `static ComponentName createRelative(String pkg, String cls)`

通过包名和相对类名创建实例。若 `cls` 以 `"."` 开头，则自动拼接包名生成完整类名；否则与 `ComponentName(String, String)` 行为相同。此方法在API 23（Android 6.0）中引入。

# 基本应用
## 构造显式Intent
显式Intent需要明确指定目标组件，ComponentName是最直接的指定方式，在跨应用启动组件时尤为常用。

🔴 示例一：使用ComponentName构造显式Intent。

在本示例中，我们从当前应用启动另一个应用（包名为 `net.bi4vmr.study.demo` ）的 `MainActivity` 。

"TestUIBase.java":

```java
// 使用包名字符串和完整类名字符串构建ComponentName
ComponentName componentName = new ComponentName(
    "net.bi4vmr.study.demo",
    "net.bi4vmr.study.demo.MainActivity"
);

Intent intent = new Intent();
intent.setComponent(componentName);
startActivity(intent);
```

上述内容也可以使用Kotlin语言编写：

"TestUIBase.kt":

```kotlin
// 使用包名字符串和完整类名字符串构建ComponentName
val componentName = ComponentName(
    "net.bi4vmr.study.demo",
    "net.bi4vmr.study.demo.MainActivity"
)

val intent = Intent()
intent.component = componentName
startActivity(intent)
```

当目标组件位于当前应用内部时，可以直接传入Context和Class对象，代码更加简洁：

"TestUIBase.java":

```java
ComponentName componentName = new ComponentName(this, DetailActivity.class);

Intent intent = new Intent();
intent.setComponent(componentName);
startActivity(intent);
```

"TestUIBase.kt":

```kotlin
val componentName = ComponentName(this, DetailActivity::class.java)

val intent = Intent()
intent.component = componentName
startActivity(intent)
```

> 🚩 提示
>
> 对于应用内部跳转，也可以直接调用 `intent.setClass(context, cls)` 方法，其内部同样是通过ComponentName实现的。

## 查询组件信息
通过PackageManager，我们可以使用ComponentName查询指定组件的详细信息，例如标签、图标、是否被禁用等。

🔴 示例二：使用ComponentName查询Activity的标签信息。

"TestUIBase.java":

```java
ComponentName componentName = new ComponentName(this, DetailActivity.class);
PackageManager pm = getPackageManager();

try {
    ActivityInfo info = pm.getActivityInfo(componentName, 0);
    String label = info.loadLabel(pm).toString();
    Log.i(TAG, "Activity标签: " + label);
} catch (PackageManager.NameNotFoundException e) {
    Log.w(TAG, "未找到目标组件: " + e.getMessage());
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIBase.kt":

```kotlin
val componentName = ComponentName(this, DetailActivity::class.java)
val pm = packageManager

try {
    val info = pm.getActivityInfo(componentName, 0)
    val label = info.loadLabel(pm).toString()
    Log.i(TAG, "Activity标签: $label")
} catch (e: PackageManager.NameNotFoundException) {
    Log.w(TAG, "未找到目标组件: ${e.message}")
}
```

## 启用与禁用组件
通过PackageManager，我们可以动态地启用或禁用应用内的某个组件，使其对外可见或不可见。

🔴 示例三：动态禁用和启用一个Activity。

"TestUIBase.java":

```java
ComponentName componentName = new ComponentName(this, DetailActivity.class);
PackageManager pm = getPackageManager();

// 禁用组件（对外不可见，隐式Intent无法匹配到该组件）
pm.setComponentEnabledSetting(
    componentName,
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
    PackageManager.DONT_KILL_APP
);

// 启用组件
pm.setComponentEnabledSetting(
    componentName,
    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
    PackageManager.DONT_KILL_APP
);

// 恢复为Manifest中声明的默认状态
pm.setComponentEnabledSetting(
    componentName,
    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
    PackageManager.DONT_KILL_APP
);
```

上述内容也可以使用Kotlin语言编写：

"TestUIBase.kt":

```kotlin
val componentName = ComponentName(this, DetailActivity::class.java)
val pm = packageManager

// 禁用组件
pm.setComponentEnabledSetting(
    componentName,
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
    PackageManager.DONT_KILL_APP
)

// 启用组件
pm.setComponentEnabledSetting(
    componentName,
    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
    PackageManager.DONT_KILL_APP
)

// 恢复为Manifest中声明的默认状态
pm.setComponentEnabledSetting(
    componentName,
    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
    PackageManager.DONT_KILL_APP
)
```

`setComponentEnabledSetting()` 方法的第三个参数为标志位，取 `PackageManager.DONT_KILL_APP` 时表示更改后不强制杀死应用进程。

## 序列化与反序列化
ComponentName提供了 `flattenToString()` 和 `unflattenFromString()` 方法，方便将组件标识持久化到文件或SharedPreferences中，再在需要时还原。

🔴 示例四：将ComponentName序列化后存储，再反序列化还原。

"TestUIBase.java":

```java
ComponentName componentName = new ComponentName(this, DetailActivity.class);

// 序列化为字符串，可存入SharedPreferences或数据库
String flatStr = componentName.flattenToString();
Log.i(TAG, "序列化结果: " + flatStr);
// 输出示例：net.bi4vmr.study/net.bi4vmr.study.DetailActivity

// 从字符串还原ComponentName
ComponentName restored = ComponentName.unflattenFromString(flatStr);
if (restored != null) {
    Log.i(TAG, "包名: " + restored.getPackageName());
    Log.i(TAG, "类名: " + restored.getClassName());
    Log.i(TAG, "简短类名: " + restored.getShortClassName());
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIBase.kt":

```kotlin
val componentName = ComponentName(this, DetailActivity::class.java)

// 序列化为字符串
val flatStr = componentName.flattenToString()
Log.i(TAG, "序列化结果: $flatStr")
// 输出示例：net.bi4vmr.study/net.bi4vmr.study.DetailActivity

// 从字符串还原ComponentName
val restored = ComponentName.unflattenFromString(flatStr)
restored?.let {
    Log.i(TAG, "包名: ${it.packageName}")
    Log.i(TAG, "类名: ${it.className}")
    Log.i(TAG, "简短类名: ${it.shortClassName}")
}
```

运行上述代码，控制台将输出以下信息：

```text
TestUIBase: 序列化结果: net.bi4vmr.study/net.bi4vmr.study.DetailActivity
TestUIBase: 包名: net.bi4vmr.study
TestUIBase: 类名: net.bi4vmr.study.DetailActivity
TestUIBase: 简短类名: .DetailActivity
```
