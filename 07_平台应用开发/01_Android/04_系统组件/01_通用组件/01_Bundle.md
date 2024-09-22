# 简介
Bundle是Android系统组件之间传递数据时的封装工具，其内部实现是一个 `Map<String, Object>` 结构，能够以键值对的形式传递一组数据。

Bundle支持所有Java基本数据类型、String、Parcelable对象、Serializable对象，以及它们的数组与列表。

# 基本应用
Bundle中的每条数据被字符串形式的Key唯一标识，Bundle对象提供了以下方法进行键值对操作：

- "put"系列方法用于存入数据项，例如 `putString(String key, String value)` 。
- "get"系列方法用于读取数据项，例如 `getString(String key)` ，此类方法在Key不存在时将会返回空值，因此还有对应的重载方法用于设置默认值，例如 `getString(String key, String default)` ，我们可以根据需要选择。
- `remove(String key)` 方法用于移除指定的数据项。
- `clear()` 方法用于清除所有数据项。

我们在启动Activity时，经常利用Intent传递一些初始化信息，以便目标Activity确定需要加载的内容，此时就可以利用Bundle封装数据。

🔴 示例一：Bundle的基本应用。

在本示例中，我们从TestUIBase启动BookInfoActivity，并通过Bundle传递书籍信息。

第一步，我们在TestUIBase中组装数据，并通过Intent对象的 `putExtras(Bundle bundle)` 方法传递Bundle对象。

"TestUIBase.java":

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

上述内容也可以使用Kotlin语言书写：

"TestUIBase.kt":

```kotlin
// 新建Bundle对象，并存入一些数据。
val bundle = Bundle()
bundle.putString("KEY_ID", "0001")
bundle.putString("KEY_NAME", "书籍")
bundle.putFloat("KEY_PRICE", 39.9F)
bundle.putBoolean("KEY_SOLDOUT", false)

val intent = Intent()
intent.setClass(this, BookInfoActivity::class.java)
// 将数据存入Intent，然后启动目标Activity。
intent.putExtras(bundle)
startActivity(intent)
```

Intent对象内部拥有一个Bundle对象，Intent的 `putExtras(Bundle bundle)` 方法会将参数 `bundle` 中的所有数据复制到Intent内部。

第二步，我们在BookInfoActivity的 `onCreate()` 方法中，获取启动它的Intent对象，并从中读取各项数据。

"BookInfoActivity.java":

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
    Log.i(TAG, bookInfo);
}
```

上述内容也可以使用Kotlin语言书写：

"BookInfoActivity.kt":

```kotlin
// 从Intent中取出Bundle对象
val bundle: Bundle? = intent.extras
bundle?.let {
    // 使用Key取出对应的值
    val id: String? = it.getString("KEY_ID")
    val name: String? = it.getString("KEY_NAME")
    val price: Float = bundle.getFloat("KEY_PRICE", -1F)
    val isSoldout: Boolean = bundle.getBoolean("KEY_SOLDOUT", true)

    val bookInfo = "ID:$id\n名称:$name\n价格:$price\n售空:$isSoldout"
    Log.d(TAG, bookInfo)
    binding.tvInfo.text = bookInfo
}
```

此时运行示例程序，并查看控制台输出信息与界面外观：

```text
17:06:14.315  6795  6867 D BookInfoActivity: ID:0001
    名称:书籍
    价格:39.9
    售空:false
```

由于Intent内部封装了Bundle，所以存入数据时我们也可以直接调用Intent的 `putStringExtra()` 等系列方法，而不必先封装Bundle再调用 `putExtras()` 方法。同理，接收端也可以直接调用Intent的 `getStringExtra()` 取出数据，这种方式的缺点是不支持配置默认值，我们应当根据实际需要选用。
