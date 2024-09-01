# 简介
Handler是Android中的一种消息队列机制，也是UI体系的基础工具。

与大多数操作系统相同，Android的UI体系也采用了单线程模型；各个组件将更新指令通过Handler发送至事件队列中，事件循环不断地从队列中检索待处理的更新指令，然后依次进行处理，确保有序性。
<!-- TODO

APP的主线程通常就是控制UI更新的线程，因此我们需要在主线程中进行UI更新操作；对于耗时较长的任务（例如：下载图片、统计数据等），我们

在Android中，Handler机制是用于在不同线程之间传递消息和调度任务的核心组件。它主要用于解决线程间的通信问题，特别是在Android应用程序中，它常用于实现异步任务和UI更新。

Android中的消息通信机制，用于 子线程与主线程间的通讯，实现了一种 非堵塞的消息传递机制，把子线程中的 UI更新信息 发送给主线程(UI线程)，以此完成UI更新操作。Android中主线程不能进行耗时操作，所有的耗时操作都需要在子线程中进行，耗时操作完成后如果需要更新ui，就需要把信息传给主线程更新ui。



<div align="center">

![Handler的工作原理](./Assets_Handler/简介_Handler的工作原理.jpg)

</div>



    Message：消息
    Hanlder：消息的发起者
    Looper：消息的遍历者
    MessageQueue：消息队列


Handler的主要功能包括：
消息传递：在不同线程之间传递消息。
任务调度：在指定时间执行任务。
线程同步：协调不同线程之间的操作。
Handler 的主要组成部分
Looper：每个线程都有一个Looper对象，负责管理该线程的消息队列（MessageQueue）。
MessageQueue：存放消息的队列，Looper从队列中取出消息并交给Handler处理。
Message：消息对象，包含数据和处理逻辑。
Handler：负责处理消息的对象。




这里主要就是创建了消息队列MessageQueue，并让它供Looper持有，因为一个线程最大只有一个Looper对象，所以一个线程最多也只有一个消息队列。然后再把当前线程赋值给mThread。

无消息，阻塞，有消息，工作



-->

# 基本应用
以下示例展示了Handler的基本使用方法：

🔴 示例一：使用Handler处理自定义消息。

在本示例中，我们通过Handler处理自定义消息，实现更新界面的功能。

第一步，我们在测试Activity中声明一个Handler对象 `mHandler` ，重写它的 `handleMessage()` 方法，实现自定义消息处理逻辑。

"TestUIBase.java":

```java
// 声明常量表示不同的消息
private final int MSG_TEST_01 = 0x01;
private final int MSG_TEST_02 = 0x02;

// 创建Handler对象，使用主线程的事件循环。
private final Handler mHandler = new Handler(Looper.getMainLooper()) {

    // 回调方法：当有新消息需要处理时被触发
    @Override
    public void handleMessage(@NonNull Message msg) {
        int code = msg.what;
        switch (code) {
            case MSG_TEST_01:
                Log.d(TAG, "HandleMessage 1.");
                // 在此处编写收到1号消息后需要执行的逻辑...
                tvLog.append("HandleMessage 1.");
                break;
            case MSG_TEST_02:
                Log.d(TAG, "HandleMessage 2. Arg1:[" + msg.arg1 + "] Arg2:[" + msg.arg2 + "]");
                // 在此处编写收到2号消息后需要执行的逻辑...
                tvLog.append("HandleMessage 2. Arg1:[" + msg.arg1 + "] Arg2:[" + msg.arg2 + "]");
        }
    }
};
```

在上述代码中，Handler的构造方法唯一参数为 `Looper.getMainLooper()` ，表示该Handler将关联到主线程。

`handleMessage(Message msg)` 是一个回调方法，当Looper开始处理MessageQueue首部的Message时，该方法将被触发，唯一参数 `msg` 即需要处理的Message。

Handler通过数字序号区分不同的Message，我们通常将序号定义为常量或枚举，以便发送者和Handler规范地引用，明确消息的用途。

由于此处的Handler与主线程相关联，我们可以在 `handleMessage()` 方法中进行更新界面的操作。此处的 `tvLog` 是一个TextView，Handler收到消息后会将文本更新到TextView中。

上述内容也可以使用Kotlin语言书写：

"TestUIBase.kt":

```kotlin
companion object {
    // 声明常量表示不同的消息
    private const val MSG_TEST_01: Int = 0x01
    private const val MSG_TEST_02: Int = 0x02
}

// 创建Handler对象，使用主线程的事件循环。
private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {

    // 回调方法：当有新消息需要处理时被触发
    override fun handleMessage(msg: Message) {
        // 根据"what"属性区分消息
        val code: Int = msg.what
        when (code) {
            0x01 -> {
                Log.d(TAG, "HandleMessage 1.")
                // 在此处编写收到1号消息后需要执行的逻辑...
                tvLog.append("HandleMessage 1.")
            }
            0x02 -> {
                Log.d(TAG, "HandleMessage 2. Arg1:[${msg.arg1}] Arg2:[${msg.arg2}]")
                // 在此处编写收到1号消息后需要执行的逻辑...
                tvLog.append("HandleMessage 2. Arg1:[${msg.arg1}] Arg2:[${msg.arg2}]")
            }
        }
    }
}
```

第二步，我们通过 `mHandler` 向MessageQueue发送一条消息1。


"TestUIBase.java":

```java
Log.i(TAG, "--- 向队列中发送消息1 ---");

mHandler.sendEmptyMessage(MSG_TEST_01);
```

Handler对象的 `sendEmptyMessage(int what)` 方法用于向MessageQueue发送内容为空的消息，唯一参数 `what` 指明了消息的数字序号，这种消息不包含任何参数，仅用于触发Handler对象的 `handleMessage()` 回调方法。

上述内容也可以使用Kotlin语言书写：

"TestUIBase.kt":

```kotlin
Log.i(TAG, "--- 向队列中发送消息1 ---")

mHandler.sendEmptyMessage(MSG_TEST_01)
```

此时运行示例程序，并查看控制台输出信息与界面外观：

```text
20:58:42.638 18979 18979 I TestUIBase: --- 向队列中发送消息1 ---
20:58:42.676 18979 18979 D TestUIBase: HandleMessage 1.
```

🟠 示例二：发送携带额外数据的消息。

在前文“示例一”中，我们创建的Handler对象实现了处理编号为2消息的逻辑，能够将消息携带的两个参数输出到控制台与界面上。

在本示例中，我们通过Handler发送编号为2的消息，并设置两个参数。

"TestUIBase.java":

```java
Log.i(TAG, "--- 向队列中发送消息2 ---")

// 从系统申请一个Message对象
Message msg = Message.obtain();
msg.what = MSG_TEST_01;
// 设置参数
msg.arg1 = 114514;
msg.arg2 = 1919810;
// 发送消息
mHandler.sendMessage(msg);
```

在上述代码中，我们使用Message的静态方法 `obtain()` 获取Message对象，然后设置其他参数并将其提交到MessageQueue中。

Message对象被使用完毕后，系统不会立刻将其回收，而是放置在缓存池中；当我们调用 `obtain()` 方法时，系统会从缓存池中取出已缓存的Message对象，将旧内容清空后返回。因此我们推荐使用 `obtain()` 方法获取Message对象，而不是手动创建Message对象。

Message对象的 `arg1` 与 `arg2` 属性是两个"int"型变量，它们可以携带一些简单的参数，以便 `handleMessage()` 回调方法处理这种Message时进行对应的操作。

上述内容也可以使用Kotlin语言书写：

"TestUIBase.kt":

```kotlin
Log.i(TAG, "--- 向队列中发送消息2 ---");

// 从系统申请一个Message对象
val msg: Message = Message.obtain()
msg.what = MSG_TEST_02
// 设置参数
msg.arg1 = 114514
msg.arg2 = 1919810
// 发送消息
mHandler.sendMessage(msg)
```

此时运行示例程序，并查看控制台输出信息与界面外观：

```text
20:55:06.048 18979 18979 I TestUIBase: --- 向队列中发送消息2 ---
20:55:06.105 18979 18979 D TestUIBase: HandleMessage 2. Arg1:[114514] Arg2:[1919810]
```

除了 `arg1` 与 `arg2` 属性之外，Message还提供了一些其他类型的容器，它们所需的资源依次递增：

- `Object obj` 属性：该属性可以携带单个Object数据；它的访问修饰符为"public"，我们可以直接读写。
- `Bundle data` 属性：该属性可以携带一组Bundle数据；我们需要使用 `setData()` 和 `getData()` 方法读写。



<!-- TODO




# 延时

Message在MessageQueue中是根据when从小到大来排队的，when是开机到现在的时间+延时时间。



# 取消

其实就是在Activity的onDestroy方法中调用mHandler.removeCallbacksAndMessages(null)，这样就移除了MessageQueue中target为该mHandler的Message，因为MessageQueue没有引用该Handler发送的Message了，所以当Activity退出时，Message、Handler、Activity都是可回收的了，这样就能解决内存泄漏的问题了。

WeakReference<OuterClass>


# 在子线程中更新UI


    Handler的sendMessage方式
    Handler的post方式
    Activity的runOnUiThread方法
    View的post方式

onCreate方法中，子线程可能可以更新UI，因为子线程不能更新UI的检测是在ViewRootImpl的checkThread完成的，而onCreate方法中，ViewRootImpl还没有创建，所以不会去检测。



Android系统中创建子线程的基本方式与Java一致，我们通常创建Thread类的匿名内部类，在"run()"方法内填写业务操作，再调用"start()"方法启动任务。

Android应用程序的主线程是UI线程，负责第一时间对用户交互进行响应。多个线程同时更新UI可能会造成死锁，因此Android系统不允许非主线程更新UI，需要借助Android提供的消息机制。

```java
// 文本框
TextView textView=findViewById(R.id.tv_text);
// 按钮
Button bt=findViewById(R.id.bt_chtext);
bt.setOnClickListener(v -> {
    // 开启新线程，隐藏文本框。
    new Thread(new Runnable() {
        @Override
        public void run() {
            textView.setVisibility(View.INVISIBLE);
        }
    }).start();
});
```

上述代码中，用户点击按钮后将会开启子线程，并在其中更新UI，运行此代码后程序将会退出，查看Logcat可以发现：

```text
2022-03-21 14:00:26.074 8061-8088/net.bi4vmr.study.service_base E/AndroidRuntime: FATAL EXCEPTION: Thread-2
    Process: net.bi4vmr.study.service_base, PID: 8061
    android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
        at android.view.ViewRootImpl.checkThread(ViewRootImpl.java:9316)
        at android.view.ViewRootImpl.requestLayout(ViewRootImpl.java:1772)
        at android.view.View.requestLayout(View.java:25697)
[省略部分内容...]
```



# 非主线程的Handler

原因也说得很清楚了，说的是我们不能在一个没有调用Looper.prepare()的线程去创建Handler对象。那为什么主线程我们不需要去手动调用Looper.prepare()就可以直接使用Handler呢？原来是启动App时，系统帮我们创建好了，App的入口，是ActivityThread.main方法，代码如下：
   public static void main(String[] args) {
       // 不相干代码
       ......
       // 1.调用Looper.prepareMainLooper，其实也就是调用的Looper.loop，初始化Looper、MessageQueue等
       Looper.prepareMainLooper();
       // 2.创建ActivityThread的同时，初始化了成员变量Handler mH
       ActivityThread thread = new ActivityThread();
       thread.attach(false);
       // 
       if (sMainThreadHandler == null) {
           // 把创建的Handler mH赋值给sMainThreadHandler
           sMainThreadHandler = thread.getHandler();
       }

       if (false) {
           Looper.myLooper().setMessageLogging(new
                   LogPrinter(Log.DEBUG, "ActivityThread"));
       }
       // 3.调用Looper.loop()方法，开启死循环，从MessageQueue中不断取出Message来处理
       Looper.loop();

       throw new RuntimeException("Main thread loop unexpectedly exited");
   }

看样子，创建Handler还是需要调用Looper.prepare的，我们平常在主线程不需要手动调用，是因为系统在启动App时，就帮我们调用了。并且还需要调用Looper.loop方法，这个方法后面我们会讲到。所以使用Handler通信之前需要有以下三步：

    调用Looper.prepare()
    创建Handler对象
    调用Looper.loop()

那按照这个套路，我们完善下之前的代码，其实就是在子线程中创建Handler之前调用Looper.prepare()，之后调用Looper.loop()方法，如下：
// 创建一个子线程，并在子线程中创建一个Handler，且重写handleMessage
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                subHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        // 处理消息
                        switch (msg.what) {
                            case MSG_MAIN_TO_SUB:
                                Log.e(TAG, "接收到消息： " +  Thread.currentThread().getName() + ","+ msg.obj);
                                break;
                            default:
                                break;
                        }
                    }
                };
                Looper.loop();
            }
        }).start();

这时候再点击按钮，在主线程向子线程发送消息，log如下图。







-->