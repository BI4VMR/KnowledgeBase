# 简介
我们日常使用的软件界面由各类控件组成，例如按钮、文本框、列表等。Android中的控件都继承自 `android.view.View` 类，View是对所有UI组件的抽象，它描述了UI组件的公共属性，例如宽度与高度；它还拥有设置事件监听的相关方法，例如“点击事件”、“长按事件”等。

View是单个控件，其内部无法容纳子控件，ViewGroup继承自View，它能够拥有多个子控件并管理排列方式，典型应用是布局管理器和列表类控件。ViewGroup可以嵌套使用，这意味着我们可以在布局管理器中添加列表类控件或另一个布局管理器，灵活地进行界面设计。

静态的XML布局文件描述了界面在初始状态下拥有的控件与排列方式，当XML文件渲染时，控件标签将被系统解析并生成相应的Java或Kotlin实例；除了静态声明控件，我们也可以在逻辑代码中动态创建控件对象并添加至ViewGroup。

控件在XML布局文件中的表示方式如下文代码片段所示：

"testui_base.xml":

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:ignore="HardcodedText">

    <TextView
        android:id="@+id/text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="我能够吞下玻璃而不伤身。"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        tools:textColor="#F0F" />

    <ImageView
        android:id="@+id/image"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:importantForAccessibility="no"
        android:src="@drawable/ic_funny_256"
        custom:layout_constraintStart_toStartOf="parent"
        custom:layout_constraintTop_toBottomOf="@id/text" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

这是一个文本框控件，其代码实现位于"android"包中，因此标签 `<TextView>` 省略了包名，如果我们所使用的控件不在"android"包中，则需要申明完整的路径，例如： `<androidx.recyclerview.widget.RecyclerView>` 。

在布局文件中，每个控件都必须声明 `android:layout_width` 和 `android:layout_height` 属性，分别控制它的宽度与高度。宽高属性的有效值如下文内容所示：

🔷 `match_parent`

控件的宽高参考父元素。

假如父布局宽度为"100dp"，则本控件的宽度也是"100dp"。对于最外层的根布局，若我们将其宽高设为"match_parent"，将以窗口的宽高为基准。

🔷 `wrap_content`

控件的宽高参考其内容。

对于文本类控件，宽高取决于文本字数、字号等；对于ViewGroup类控件，宽高取决于子元素与排列规则。

🔷 `<固定值>`

控件的宽高取值为尺寸数值时，将不再受其父子元素的影响，常用的计量单位为"dp"。

`android:id` 属性能够在当前XML文件中唯一标识某个控件， `@+id/<控件ID>` 表示注册新的ID，系统会在R文件中生成此ID对应的记录，便于其它组件引用。我们可以在逻辑代码中通过 `findViewById(R.id.tvInfo)` 方法获取示例代码中的TextView对象，并对其进行进一步操作。

`android:text` 属性是TextView控件所拥有的方法，表示设置此文本框的初始内容是“初始化”。

`tools:text` 属性是便于开发者调试的工具，此类属性仅在Android Studio的布局预览工具中显示，编译后的程序并不会携带这些属性。

除了宽高属性，其它属性通常不是必选的，它们将采用默认值，我们可以根据实际需要进行配置。



xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"

自定义NS

xmlns:<自定义名称>="http://schemas.android.com/apk/res-auto"

实际上编译器只识别 `xmlns` 后面的数值，所以该名称可以随意定义，同一个URI也可以定义多个不同的NS


部分属性在app和android NS都存在，例如app:tint和android:tint，我们应该注意区分它们，SDK中的组件会识别android属性，而第三方库(androidx materier等)会识别app。
如果我们将appns的属性配置给andorid包中的组件，可能不会生效。

# 事件监听器
## 简介
控件的监听器用于接收控件产生的各类事件，开发者可以通过注册监听器实现事件的处理逻辑，例如界面上有一个“新建文件”按钮，我们为其设置点击事件监听器，并在回调方法中预先配置相关逻辑；当用户点击按钮时，监听器的回调方法将会触发，执行新建文件的相关操作。

监听器通常是一个接口，我们需要在接口的实现类中书写逻辑代码，并通过控件的"setXXXListener"系列方法将监听器实例传递给控件。

## 点击监听器
点击监听器是所有控件通用的监听器，当用户使用手指触摸控件并抬起手指时触发。

"TestUIBase.java":

```java
// 获取按钮"btnTest"的实例
Button btnTest = findViewById(R.id.btnTest);
// 实现点击监听器并传递给"btnTest"
btnTest.setOnClickListener(new View.OnClickListener() {

    @Override
    public void onClick(View v) {
        Log.i(TAG, "按钮Test被点击了！");
    }
});
```

上述内容也可以使用Kotlin语言编写：

"TestUIBase.kt":

```kotlin
val btnTest: Button = findViewById(R.id.btnTest)
btnTest.setOnClickListener {
    Log.i(TAG, "按钮Test被点击了！")
}
```

我们首先通过 `findViewById()` 方法获取了按钮 `btnTest` 实例，然后通过它的 `setOnClickListener()` 方法设置了点击事件监听器，其参数即监听器实例；我们在回调方法 `onClick()` 中实现了自己的逻辑。

我们将上述代码放置在界面的初始化回调方法 `onCreate()` 中，当界面加载后，点击监听器即被设置；后续用户每点击一次按钮，此处的 `onClick()` 回调方法将被触发一次，控制台中也将显示相应的日志内容。


# 疑难解答
## 索引

<div align="center">

|       序号        |                    摘要                     |
| :---------------: | :-----------------------------------------: |
| [案例一](#案例一) | 调用 `setBackgroundResource()` 方法无效果。 |

</div>

## 案例一
### 问题描述
当我们调用View的 `setBackgroundResource()` 方法更新背景资源时，实际无任何效果。

### 问题分析
在本案例中，我们切换主题后调用View的 `setBackgroundResource()` 方法，希望控件重新加载资源，由于AOSP原生主题机制通过主题属性切换资源，属性本身的ID是不变的，这导致 `setBackgroundResource()` 方法调用在第一个 `if` 语句就被Return了，后续逻辑均不会执行。

"frameworks/base/core/java/android/view/View.java":

```java
public void setBackgroundResource(@DrawableRes int resid) {
    if (resid != 0 && resid == mBackgroundResource) {
        return;
    }

    // 此处已省略部分代码...
}
```

### 解决方案
`setBackgroundResource()` 是SDK的内置方法，相关逻辑无法修改。

我们可以先根据资源ID获取当前环境下的最新Drawable对象，再调用View的 `setBackground()` 方法更新UI。




<!-- TODO
## 实用技巧

# 防止快速点击

    private fun View.clickAntiJitter(interval: Long = 500L, action: (view: View) -> Unit) {
        setOnClickListener {
            val currentTS = SystemClock.uptimeMillis()
            if (currentTS - lastClickTS < interval) {
                Log.w(TAG, "Click too quickly, ignored!")
                return@setOnClickListener
            }
            lastClickTS = currentTS
            action(it)
        }
    }


# 连续点击触发


```kotlin
// 连续点击计数器，数组的大小即为需要设置的连点次数。
private var clickTSRecords: LongArray = LongArray(10)


// 调试后门：5秒内连续点击10次用户名弹出云端环境选择窗口。
tvNickName.setOnClickListener {
    // 所有现有数据左移一位，舍弃最旧的一位数据。
    System.arraycopy(clickTSRecords, 1, clickTSRecords, 0, clickTSRecords.size - 1)
    // 将当前点击时间记录到数组末尾
    clickTSRecords[clickTSRecords.size - 1] = SystemClock.uptimeMillis()
    // 当前时间与最早一次的点击时间比较，如果差值小于5秒，则触发连点事件。
    if (SystemClock.uptimeMillis() - clickTSRecords[0] <= 5000L) {
        "Repeat click 10 times in 5 seconds, open environment switch dialog.".logi(TAG)
        showEnvironmentDialog()
        // 事件已经触发，重置记录器。
        Arrays.fill(clickTSRecords, 0L)
    }
}
```


-->
