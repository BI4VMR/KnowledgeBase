# 概述
Bundle是Android系统组件之间实现数据传递的工具，其内部实现是一个 `Map<String, Object>` 结构，可以以键值对的形式传递一组数据。

Bundle支持所有Java基本数据类型、String、Parcelable对象、Serializable对象，以及它们的数组与列表。

# 基本应用
Bundle中的每条数据使用字符串Key进行唯一标识，其提供了以下方法进行键值对操作：

- "put"系列方法用于存入数据项，例如 `putString(String key, String value)`。
- "get"系列方法用于读取数据项，例如 `getString(String key)` ，此类方法在Key不存在时将会返回空值，因此还有对应的重载方法用于设置默认值，例如 `getString(String key, String default)`， 可以根据需要选择。
- `remove(String key)` 方法用于移除指定的数据项。
- `clear()` 方法用于清除所有数据项。

我们在启动Activity时，经常利用Intent传递一些信息，以便目标Activity进行初始化操作，此时就可以利用Bundle封装数据。

此处我们将从DemoBaseUI启动DstActivity，并传递书籍信息。

DemoBaseUI.java:

```java
// 新建Bundle对象，并存入一些数据。
Bundle bundle = new Bundle();
bundle.putString("KEY_ID", "0001");
bundle.putFloat("KEY_PRICE", 39.9F);
bundle.putBoolean("KEY_SOLDOUT", false);

Intent intent = new Intent();
intent.setClass(this, DstActivity.class);
// 将数据存入Intent，然后启动目标Activity。
intent.putExtras(bundle);
startActivity(intent);
```

Intent内部拥有一个Bundle对象，我们使用Intent的 `putExtras(Bundle bundle)` 方法可以将参数Bundle的所有数据保存在Intent中，该方法将会复制每条属性至内部Bundle，因此即使多次调用也不会覆盖已有数据。

在DstActivity的 `onCreate()` 方法中，我们可以获取启动它的Intent，并从中读取各项数据。

DstActivity.java:

```java
// 从Intent中取出Bundle对象
Bundle bundle = getIntent().getExtras();
if (bundle != null) {
    // 使用Key取出对应的值
    String id = bundle.getString("KEY_ID");
    String name = bundle.getString("KEY_NAME");
    float price = bundle.getFloat("KEY_PRICE", -1F);
    boolean isSoldout = bundle.getBoolean("KEY_SOLDOUT", true);

    String bookInfo = "ID:" + id + "\n名称:" + name + "\n价格:" + price + "\n售空:" + isSoldout;
    Log.d("myapp", bookInfo);
}
```

运行程序并启动DstActivity后，我们可以看到日志输出以下内容：

```text
2023-03-08 17:06:14.315 5894-5894/? D/myapp: ID:0001
    名称:书籍
    价格:39.9
    售空:false
```

由于Intent内部封装了Bundle，所以存入数据时我们也可以直接调用Intent的 `putStringExtra()` 等系列方法，而不必先封装Bundle再调用 `putExtras()` 方法。同理，接收端也可以直接调用Intent的 `getStringExtra()` 取出数据，这种方式的缺点是不支持配置默认值，应当根据实际需要选用。
