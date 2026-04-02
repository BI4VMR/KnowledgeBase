# 简介
控件是组成用户界面的基本元素，我们日常所使用的软件界面由各种控件组成，例如：按钮、文本框、图片展示框等。Android中的控件都继承自 `android.view.View` 类，View是对所有UI组件的抽象，它描述了UI组件的公共属性，例如：宽度与高度；它还拥有设置事件监听器的相关方法，例如：“点击事件”、“长按事件”等。

View是单体控件，包括按钮、文本框等，其内部无法容纳子控件；ViewGroup继承自View，它们能够拥有多个子控件并管理排列方式，包括布局管理器和列表展示类控件。ViewGroup支持嵌套使用，我们可以根据功能编写多个布局并将它们组合起来，实现灵活的界面设计。


# 布局文件
## 简介
布局文件用于描述一组控件的层级关系与初始状态，基于XML语法，应当统一放置在 `<模块根目录>/src/main/res/layout` 目录中。当程序运行时，系统将根据布局文件中的标签创建对应的View实例，并组合为树状结构，最终呈现出用户界面。

下文代码块展示了一个布局文件示例：

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
        android:layout_marginStart="50dp"
        android:src="@drawable/ic_funny_256"
        custom:layout_constraintStart_toStartOf="parent"
        custom:layout_constraintTop_toBottomOf="@id/text" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

该布局在程序运行时的效果如下文图片所示：

<!-- TODO -->

## 基本属性
TextView是文本框控件，ImageView是图片展示框控件，它们的代码实现在 `android` 包中，因此标签 `<TextView>` 和 `<ImageView>` 省略了包名，如果我们所使用的控件不在 `android` 包中，则需要申明完整的路径，例如： `<androidx.constraintlayout.widget.ConstraintLayout>` 。

每个控件都必须声明 `android:layout_width` 和 `android:layout_height` 属性，分别控制它的宽度与高度。宽高属性的有效值如下文内容所示：

🔷 `match_parent(-1)`

控件的宽高参考父元素。

假如父布局宽度为"100dp"，则本控件的宽度也是"100dp"。对于最外层的根布局，若我们将其宽高设为"match_parent"，将以窗口的宽高为基准。

🔷 `wrap_content(-2)`

控件的宽高参考其内容。

对于文本类控件，宽高取决于文本字数、字号等；对于ViewGroup类控件，宽高取决于子元素与排列规则。

🔷 `<固定值>`

控件的宽高取值为尺寸数值时，将不再受其父子元素的影响，常用的计量单位为"dp"。

`android:id` 属性能够在当前XML文件中唯一标识某个控件， `@+id/<控件ID>` 表示注册新的ID，系统会在R文件中生成此ID对应的记录，便于其它组件引用。我们可以在代码中通过 `findViewById(R.id.tvInfo)` 方法获取示例代码中的TextView对象，并对其进行进一步操作。

`android:text` 是TextView的专有属性，用于设置此文本框的初始文本内容； `android:src` 则是ImageView的专有属性，表示此图片展示框默认加载 `@drawable/ic_funny_256` 图片。

`tools` 开头的属性是便于开发者调试的工具，编译后的程序并不会携带这些属性。例如 `tools:text` 指示文本框在Android Studio的布局预览工具中显示内容，编译后的程序并不会携带这些属性。`tools:ignore` 指示Android Studio忽略某些语法警告，也不影响程序的行为。

除了宽高属性，其它属性通常不是必选的，控件将采用默认值，我们可以根据实际需要进行配置。


## 命名空间

xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"

自定义NS

xmlns:<自定义名称>="http://schemas.android.com/apk/res-auto"

实际上编译器只识别 `xmlns` 后面的数值，所以该名称可以随意定义，同一个URI也可以定义多个不同的NS


部分属性在 `android` 和 `app` 命名空间中都存在，通常Android SDK中的控件识别 `android` 命名空间的属性，而通过外部依赖引入的组件（例如：AndroidX、Material等）识别 `app` 命名空间的属性。我们应当注意区分它们，避免混用，有些Android SDK中的控件会忽略 `app` 命名空间中的属性。


## 渲染过程
LayoutInflater用于解析布局文件并生成View实例。


```java
// 通过Context获取LayoutInflater
LayoutInflater layoutInflater1 = context.getSystemService(LayoutInflater.class);

// 通过Context获取LayoutInflater（简化方法）
LayoutInflater layoutInflater2 = LayoutInflater.from(context);

// 通过Activity获取LayoutInflater
LayoutInflater layoutInflater3 = activity.getLayoutInflater();
```

方法2本质上也是通过方法1实现的，方法3适合在Activity内部使用。

获取LayoutInflater实例之后我们可以调用 `inflate` 方法解析布局文件

View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot)

第一参数 `resource` 即布局文件的ID，第二参数 `root` 表示父容器，第三参数 `attachToRoot` 用于控制是否立即附加到父容器。返回值即生成的View

后两个参数的情况如下文内容。

root不为null，attachToRoot为true

解析为View对象后自动添加到parent中，不能再手动调用root.addView方法，否则会出现IllegalStateException异常


root不为null，attachToRoot为false

解析为View对象后暂不添加到parent，后续我们可以按需通过root.addView方法添加。

rootview可以为空，此时布局根容器的layout_width和height属性将会失效。


两个参数的inflate也是调用三个参数的inflate方法，当root为空时不会attach，非空时自动attach。
public View inflate(@LayoutRes int resource, @Nullable ViewGroup root) {
        return inflate(resource, root, root != null);
    }

在Activity中， `setContentView(int resId)` 方法的内部逻辑就是通过Activity获取LayoutInflater实例，然后以DecorView为 `root` 调用 `inflate()` 方法，因此我们仅需传入布局文件ID内容就会自动显示，并且根容器的属性生效。


# 常用方法
## 构建
有些View需要跟随用户操作或特定条件出现，此时我们无法在布局文件中预先写入，就需要通过代码创建并动态添加到容器中。


## 操作控件
布局确定了初始状态，我们也可以根据需要在代码中动态改变属性。

## 坐标系统
在android中，坐标系从屏幕左上角为原点，向右侧延伸X轴数值增大，向底部延伸Y轴数值增大

<div align="center">

![坐标系](./Assets_概述/坐标系统_坐标系.jpg)

</div>


view.getbottom
view.getx gety
view.gettransx
view.getlocationonscreen
view.getlocationinwindow

## 背景与前景



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


# 更新界面
GUI由大量控件组成，控件之间存在复杂的依赖关系，如果多个线程都能修改界面，需要引入极其复杂的同步机制，并且仍无法避免死锁、状态不一致等问题，因此绝大多数GUI系统都采用了单线程模型，通过单个线程控制界面更新，响应与分发用户的触摸与按键事件，其他线程不能直接操作控件更新方法，需要将修改提交到UI线程的队列中，等待UI线程依次处理。

在Android系统中，
应用程序的主线程通常就是UI线程，主线程的MessageQueue用于存放待处理的指令，主线程空闲时不断地从队列中检索待处理的指令，然后依次进行处理，确保指令的有序性。

子线程不能直接更新UI

每当我们发起UI更新请求时，ViewRootImpl的 `checkThread()` 方法会检查创建View的线程与请求更新的线程是否一致，如果两者不一致，将会抛出以下异常：

```text
05:52:14.065 25317 25370 E AndroidRuntime: Process: net.bi4vmr.study.system.concurrent.handler, PID: 25317
05:52:14.065 25317 25370 E AndroidRuntime: android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
05:52:14.065 25317 25370 E AndroidRuntime:        at android.view.ViewRootImpl.checkThread(ViewRootImpl.java:9526)
05:52:14.065 25317 25370 E AndroidRuntime:        at android.view.ViewRootImpl.invalidateChildInParent(ViewRootImpl.java:1931)
05:52:14.065 25317 25370 E AndroidRuntime:        at android.view.ViewGroup.invalidateChild(ViewGroup.java:6147)
05:52:14.065 25317 25370 E AndroidRuntime:        at android.view.View.invalidateInternal(View.java:18857)
05:52:14.065 25317 25370 E AndroidRuntime:        at android.view.View.invalidate(View.java:18817)
05:52:14.065 25317 25370 E AndroidRuntime:        at android.view.View.invalidate(View.java:18799)
05:52:14.065 25317 25370 E AndroidRuntime:        at net.bi4vmr.study.base.TestUIBase$onCreate$1.invoke(TestUIBaseKT.kt:62)
```

并将结果通过Handler从子线程发送至主线程，才能成功地更新UI并避免前文的异常。

主线程不能执行耗时操作

对于耗时较长的任务（例如：下载图片、统计数据等），我们应当开启子线程进行处理，并将结果通过Handler从子线程发送至主线程，才能成功地更新UI并避免前文的异常。

Handler的具体用法详见相关章节：系统提供了一些快捷方法，它们都是对Handler的封装。


当我们需要更新UI时，除了创建绑定主线程的Handler对象并提交回调方法之外，还可以使用以下快捷方法：

- Activity的 `runOnUiThread(Runnable action)` 方法。
- View的 `post(Runnable action)` 方法。
- View的 `post(Runnable action, long delayMillis)` 方法。

上述方法均为SDK对UI线程Handler的封装，第一参数 `action` 的 `run()` 方法即需要在主线程执行的更新操作；其中View的两个方法能够确保更新操作在布局测量、绘制完成之后再被执行，因此我们能够在此处获取到View的宽高等属性。

post

修改宽高或从子线程切换到主线程更新UI

通过 View.post() 添加的任务，是在 View 绘制流程的开始阶段，将所有任务重新发送到消息队列的尾部，此时相关任务的执行已经在 View 绘制任务之后，即 View 绘制流程已经结束，此时便可以正确获取到 View 的宽高了

post也是handler，但其会等到绘制后再执行
经过前面的分析我们已经知道 AttachInfo 的赋值操作是在 View 绘制任务的开始阶段，而它的调用者是 ActivityThread 的 handleResumeActivity 方法，即 Activity 生命周期 onResume 方法之后。





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
