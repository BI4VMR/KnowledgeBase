# 简介
Timer是JDK内置的定时任务管理工具，可以在特定时刻单次执行任务，或以固定间隔循环执行任务。

Timer的任务使用TimerTask表示，
每个Timer对象将会创建一个工作线程，用于执行TimerTask任务。Timer的任务队列可以被添加多个计划任务，但TimerTask对象不可复用，如果我们将同一个TimerTask对象重复添加到Timer队列中，则会出现异常： `IllegalStateException(Task already scheduled or cancelled)` 。

# 基本应用
Timer的任务使用TimerTask承载，该类是Runnable接口的实现类，我们首先需要创建自定义类继承自TimerTask类，并实现 `run()` 方法。

"TestTimer.java":

```java
TimerTask timerTask = new TimerTask() {
    @Override
    public void run() {
        System.out.println("Task Executed. Time:[" + getTime() + "]");
    }
};
```

此处我们在任务中简单的输出任务执行的时刻信息。

接着我们创建Timer实例，并通过下文内容所示的系列方法设置任务。

🔷 `void schedule(TimerTask task, long delay)`

以当前时刻为参考点，在 `delay` 毫秒之后执行任务 `task` 。

🔷 `void schedule(TimerTask task, long delay, long period)`

以当前时刻为参考点，在 `delay` 毫秒之后执行任务 `task` ，且每隔 `period` 毫秒重复执行一次。

🔷 `void schedule(TimerTask task, Date time)`

在 `time` 时刻执行任务 `task` 。

🔷 `void schedule(TimerTask task, Date firstTime, long period)`

在 `firstTime` 时刻执行任务 `task` ，且每隔 `period` 毫秒重复执行一次。

🔷 `void scheduleAtFixedRate(TimerTask task, long delay, long period)`

以当前时刻为参考点，在 `delay` 毫秒之后执行任务 `task` ，且每隔 `period` 毫秒重复执行一次。

🔷 `void scheduleAtFixedRate(TimerTask task, Date firstTime, long period)`

在 `firstTime` 时刻执行任务 `task` ，且每隔 `period` 毫秒重复执行一次。

<br />

上述方法被调用后任务即刻开始计时，下文代码块给出了一些示例。

"TestTimer.java":

```java
// 创建Timer实例
Timer timer = new Timer();

// 以当前时刻为参考点，在1000毫秒之后执行任务TimerTask。
timer.schedule(timerTask, 1000L);

// 以当前时刻为参考点，在1秒之后执行任务TimerTask，且每隔1秒重复执行一次。
timer.schedule(timerTask, 1000L, 1000L);

// 在”当前时刻+3秒“执行任务TimerTask。
Date date = new Date();
date.setTime(System.currentTimeMillis() + 3000L);
timer.schedule(timerTask, date);

// 在”当前时刻+3秒“执行任务TimerTask，且每隔1秒重复执行一次。
Date date = new Date();
date.setTime(System.currentTimeMillis() + 3000L);
timer.schedule(timerTask, date, 1000L);

// 以当前时刻为参考点，在1秒之后执行任务TimerTask，且每隔1秒重复执行一次。
timer.scheduleAtFixedRate(timerTask, 1000L, 1000L);

// 在”当前时刻+3秒“执行任务TimerTask，在1秒之后执行任务TimerTask，且每隔1秒重复执行一次。
Date date = new Date();
date.setTime(System.currentTimeMillis() + 3000L);
timer.scheduleAtFixedRate(timerTask, date, 1000L);
```

# 终止任务
如果我们希望停止Timer队列中的任务，可以调用Timer实例的 `cancel()` 方法，它可以使未进入运行状态的任务即刻取消，但已经开始运行的任务仍将继续执行。

每个Timer实例被调用 `cancel()` 方法后就无法接受新的任务了，如果我们试图提交新的任务，则会出现异常：IllegalStateException(Timer already cancelled)。

TimerTask也拥有 `cancel()` 方法，它只会将当前实例标记为取消状态，但不影响Timer队列中的其他任务。

# 两种循环任务的区别
`​schedule()` 方法与 `scheduleAtFixedRate()` 方法都能定义周期任务，但它们的行为是不同的。

首先 `​schedule()` 方法会忽略已经错过的任务； `scheduleAtFixedRate()` 方法则会“补齐”已经错过的任务。

我们定义一个间隔为1秒循环任务，在任务中打印执行时刻，并将任务开始时刻设置为3秒前。

"TestTimer.java":

```java
// 获取当前时间
Date date = new Date();
System.out.println("Reference Time: " + date);

// 阻塞当前线程3秒，不处理任何任务。
try {
    Thread.sleep(3000L);
} catch (InterruptedException e) {
    e.printStackTrace();
}

// 创建Timer并开启任务，参考时间点设置为3秒前。
Timer timer = new Timer();
timer.schedule(timerTask, date, 1000L);
```

此时运行示例程序，并查看控制台输出信息：

```text
Reference Time: Fri Apr 12 00:12:48 CST 2024
Time: 00:12:51.821756300
Time: 00:12:52.824136400
Time: 00:12:53.826305200
```

根据上述输出内容可知：

通过 `schedule()` 方法开启的任务，将会忽略已经错过的任务，只以当前时刻为基准，执行新的任务。

接着我们仍然沿用前文的代码逻辑，并将 `schedule()` 方法替换为 `scheduleAtFixedRate()` 方法。

此时运行示例程序，并查看控制台输出信息：

```text
Reference Time: Fri Apr 12 00:13:16 CST 2024
Time: 00:13:19.133863300
Time: 00:13:19.133863300
Time: 00:13:19.133863300
Time: 00:13:20.098573800
Time: 00:13:21.100862700
Time: 00:13:22.103028900
```

根据上述输出内容可知：

通过 `scheduleAtFixedRate()` 方法开启的任务，将会“补齐”已经错过的任务，例如此处任务的参考时间为3秒前，任务间隔为1秒，因此任务连续执行了3次。

其次，当Timer中有多个任务时， `​schedule()` 方法会被任务阻塞，如果任务执行时长大于预设间隔，则后续任务将被延后； `scheduleAtFixedRate()` 方法不会被任务阻塞，后续任务将会按照原定间隔开启。

上述规律仅适用于多个不同的任务，如果Timer中仅有一个任务且执行时间大于预设间隔，使用上述两个方法的效果相同，后续任务都会被延后执行。

# 疑难解答
## 索引

<div align="center">

|       序号        |                        摘要                        |
| :---------------: | :------------------------------------------------: |
| [案例一](#案例一) | 周期任务未按照预定间隔运行，而是以很高的频率运行。 |
| [案例二](#案例二) |   所有TimerTask均执行完毕后，进程仍然没有退出。    |

</div>

## 案例一
### 问题描述
周期任务并未按照计划的间隔运行，而是以很高的频率连续运行。

### 问题分析
通过 `scheduleAtFixedRate()` 方法开启的任务，如果错过参考时刻，将会“补齐”它们，详见相关章节： [🧭 两种循环任务的区别](#两种循环任务的区别) 。

由于Timer的参考时间为系统时钟，因此当系统时间发生跳变时，就会出现这种现象。

### 解决方案
使用 `schedule()` 方法开启周期任务；或将Timer替换为Executor等参考时间为相对时间的工具。

## 案例二
### 问题描述
所有TimerTask均执行完毕后，进程仍然没有退出。

### 问题分析
Timer的工作线程会持续运行，当所有的TimerTask均执行完毕后，仍会运行一段时间，因此导致进程长时间未退出。

### 解决方案
当任务运行完毕时，我们可以主动调用一次JVM的垃圾回收方法，使Timer对象与工作线程被回收。

```java
public class MyTask extends TimerTask {

    @Override
    public void run() {
        // 主动调用JVM资源回收方法
        System.gc();
    }
}
```
