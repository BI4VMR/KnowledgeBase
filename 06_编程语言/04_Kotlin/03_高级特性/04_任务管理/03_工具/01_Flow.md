# 简介
Flow 是基于 Kotlin 协程的异步数据流，能够连续地发送多个数据值，接收者可以监听这些值并作出响应。 Flow 支持在数据传递链路中配置变换、去重、截流等功能，是一种灵活易用的响应式编程工具。

`Flow<T>` 接口拥有多种具体实现，总体上可分为“冷流”与“热流”两种类型，二者的行为差异很大，我们需要根据应用场景选择合适的实现。

冷流包含开发者预先定义的一系列逻辑语句，当接收者开始从冷流监听数据时，这些逻辑语句就会被执行并发送数据。

冷流类似于包含回调接口的方法，适用于封装需要异步多次返回结果的任务，例如：文件下载、数据库查询等。

热流不包含逻辑语句，而是从外部接收数据更新请求，并将数据变化事件分发给所有监听者。

热流类似于广播电台，适用于事件通告与状态共享，例如：消息推送、状态管理等。


# Flow
`SafeFlow<T>` 是 Kotlin 中最常用的冷流实现，我们可以通过 `flow()` 方法创建 SafeFlow 实例。

🔴 示例一： Flow 的基本应用。

在本示例中，我们定义并使用 Flow ，模拟文件下载过程。

第一步，定义 Flow 实例。

`TestBase.kt` :

```kotlin
// 定义Flow
val flow: Flow<Int> = flow {
    println("Download start. Thread Name:[${Thread.currentThread().name}]")

    // 模拟下载进度从 0 至 100
    (0..100).forEach { progress ->
        // 模拟下载耗时
        delay(10L)

        // 发送当前进度给接收者
        emit(progress)
    }

    println("Download end. Thread Name:[${Thread.currentThread().name}]")
}
```

`flow(block: suspend FlowCollector<T>.() -> Unit)` 方法用于创建 SafeFlow 实例，唯一参数 `block` 即 Flow 的逻辑代码。

我们使用循环函数模拟下载过程，每当进度改变时，调用 `emit()` 方法发送当前进度给接收者。 `emit()` 方法的参数类型与 Flow 的泛型参数一致，我们使用 0 到 100 的整数表示进度，因此 `flow` 实例的类型为 `Flow<Int>` 。

第二步，执行下载过程。

我们创建两个接收协程，并在控制台上输出它们的工作线程名称，观察 Flow 的行为。

`TestBase.kt` :

```kotlin
val scope = CoroutineScope(Dispatchers.IO)

// 接收者 A
val jobA = scope.launch {
    flow.collect {
        println("Progress change. Value:[$it] Thread Name:[${Thread.currentThread().name}]")
    }
}

// 接收者 B
val jobB = scope.launch {
    flow.collect {
        println("Progress change. Value:[$it] Thread Name:[${Thread.currentThread().name}]")
    }
}
```

Flow 实例的 `collect(collector: FlowCollector<T>)` 方法用于监听 Flow 产生的数据，这是一个挂起函数，监听者需要在协程环境中调用它；该方法的唯一参数 `collector` 用于指明每次收到数据时的处理逻辑，默认参数 `it` 表示当前数据值。 Flow 的逻辑代码中每调用一次 `emit()` 方法， FlowCollector 接口就会被回调一次，二者是一一对应的。

此时运行示例程序，并查看控制台输出信息：

```text
Download start. Thread Name:[DefaultDispatcher-worker-1]
Download start. Thread Name:[DefaultDispatcher-worker-3]
Progress change. Value:[0] Thread Name:[DefaultDispatcher-worker-3]
Progress change. Value:[0] Thread Name:[DefaultDispatcher-worker-1]
Progress change. Value:[1] Thread Name:[DefaultDispatcher-worker-1]
Progress change. Value:[1] Thread Name:[DefaultDispatcher-worker-3]

# 此处已省略部分输出内容...

Progress change. Value:[100] Thread Name:[DefaultDispatcher-worker-1]
Download end. Thread Name:[DefaultDispatcher-worker-1]
Progress change. Value:[100] Thread Name:[DefaultDispatcher-worker-2]
Download end. Thread Name:[DefaultDispatcher-worker-2]
```

根据上述输出内容可知：

当接收者调用 Flow 实例的 `collect()` 方法时， Flow 的逻辑代码开始执行，且执行线程与接收者线程相同。

两个接收者使用各自的工作线程独立推进 Flow 的逻辑代码，二者没有关联，数据也不会共享。


# SharedFlow
## 简介
`SharedFlow<T>` 是一种通用的热流实现，侧重于事件分发，我们可以自由配置它的缓存数量、重放消息等行为。

## 基本应用
下文示例展示了 SharedFlow 的基本使用方法。

🟠 示例二： SharedFlow 的基本应用。

在本示例中，我们定义 SharedFlow 用于通告事件消息。

第一步，定义 SharedFlow 实例。

`SharedFlow.kt` :

```kotlin
// 定义 SharedFlow ，用于通告事件消息。
val sharedFlow: MutableSharedFlow<String> = MutableSharedFlow()

// 发送一些消息
runBlocking {
    println("测试线程发送消息：正在初始化...")
    sharedFlow.emit("正在初始化...")
    println("测试线程发送消息：初始化成功！")
    sharedFlow.emit("初始化成功！")
}

// 创建协程监听 SharedFlow 中的消息
val scope = CoroutineScope(Dispatchers.IO)
scope.launch {
    // 调用 `collect()` 方法监听 SharedFlow 中的数据
    sharedFlow.collect { value ->
        println("监听协程收到消息：$value")
    }
    // 热流的 `collect()` 后不能放置任何语句！
}
```

热流被创建后永不停止，因此观察者的 `collect()` 方法不会执行完成，该语句后不可放置其他语句。

此时运行示例程序，并查看控制台输出信息：

```text
测试线程发送消息：正在初始化...
测试线程发送消息：初始化成功！
```

根据上述输出内容可知：

测试线程发出两条消息，但监听协程没有收到数据变化；这说明 SharedFlow 默认不会重放历史数据，观察者只会收到注册监听器之后的时间点产生的新消息。

第二步，在上述代码的基础上，通过测试线程继续发送消息。

```kotlin
println("测试线程发送消息：【文件一】下载完成！")
sharedFlow.emit("【文件一】下载完成！")
println("测试线程发送消息：【文件二】下载完成！")
sharedFlow.emit("【文件二】下载完成！")
```

此时运行示例程序，并查看控制台输出信息：

```text
测试线程发送消息：【文件一】下载完成！
测试线程发送消息：【文件二】下载完成！
监听协程收到消息：【文件一】下载完成！
监听协程收到消息：【文件二】下载完成！
```

根据上述输出内容可知：

在观察者注册监听后，测试线程所发出的消息，都能被监听协程接收到，二者是对应的。

<!-- TODO
## 类型
MutableSharedFlow 拥有.emit("正在初始化...")等提交新数据值的接口，可以用于更新数据，而 SharedFlow 是 不可变类型，它们没有数据更新接口，是只读的。

在实际应用中，我们习惯声明私有可变变量，在内部使用，然后声明公开的不可变变量，关联到私有变量 ，外部只能进行监听，修改必须通过业务方法，可以配合日志监控所有修改行为，避免非预期的修改导致逻辑错误。

```kotlin
// 内部可变变量，使用带有_前缀的变量名
private val _sharedFlow: MutableSharedFlow<String> = MutableSharedFlow()

// 公开不可变变量，只能监听。
val sharedFlow: SharedFlow<String> = _sharedFlow

```

## 缓存


可以配置 replay 缓存，让新的收集者可以收到最近的N个历史数据。如果 replay = 0 (默认)，则不缓存，新收集者不会收到任何历史数据。


replay：重新发射给新的订阅者的值的数量，可以将旧的数据回播给新的订阅者。不能为负数，默认为0。
extraBufferCapacity：在replay基础上的缓冲池的数量，当有剩余缓冲区空间时，调用emit发射数据不会被挂起，同样的不能为负数，默认值为0。


## 背压
onBufferOverflow：配置一个emit在缓冲区溢出时的触发操作。默认为BufferOverflow.SUSPEND，缓存溢出时挂起。另外还有BufferOverflow.DROP_OLDEST在溢出时删除缓冲区中最旧的值，将新值添加到缓冲区，不会进行挂起。BufferOverflow.DROP_LATEST在缓冲区溢出时删除当前添加到缓冲区的最新值来保持缓冲区内容不变，不会进行挂起。

tryEmit() :Boolean 同步发送数据，如果缓冲满将发送失败并返回false，因此缓冲为0时总是失败，不可用。
emit() 发送数据，挂起函数，如果缓冲区满可能阻塞当前协程（该行为可由溢出策略控制。）

-->

# StateFlow
## 简介
`StateFlow<T>` 是一种专用的热流实现，侧重于状态管理。

此处我们以常见的功能开关状态为例，比较 StateFlow 与 SharedFlow 的区别：

- 初始值：一个功能要么是开启的、要么是关闭的，必然存在状态；因此我们创建 StateFlow 时必须指定初始值，而 SharedFlow 没有初始值。
- 缓存数量：我们只需要维护最新的功能开关状态，因此 StateFlow 只缓存最后接收到的值，而 SharedFlow 默认不缓存数据。
- 粘性事件：观察者调用 `collect()` 方法监听数据变化时，将会立刻收到一次事件回调，数据值为缓存的值。这种行为使 UI 初始化时自动完成状态同步，无需开发者手动获取最新状态再更新控件。
- 忽略重复值：当外部组件向 StateFlow 投送数据时， StateFlow 会将当前持有的值与外部传入的值作比较，若二者相同则不通知观察者，仅当新旧数据发生变化时才通知观察者。
- 丢弃中间值：若外部组件短时间内投送多条数据， StateFlow 只会使用最后一个状态通知观察者。该特性在状态管理场景中无影响，因为观察者期望获取最终状态，并不关心中间值。在事件分发场景中，观察者期望能够接收所有事件，如果我们错误地将 StateFlow 用于猝发高频事件场景，可能会导致事件丢失。

下文列表展示了两种热流实现的核心行为差异：

<div align="center">

|    特性    | StateFlow | SharedFlow |
| :--------: | :-------: | :--------: |
|   初始值   | 必须指明  |   不支持   |
|  缓存数量  |   1 条    |  默认为 0  |
|  粘性事件  |   1 条    |  默认为 0  |
| 忽略重复值 | 强制开启  |  默认关闭  |
| 丢弃中间值 | 强制开启  |  默认关闭  |

</div>

## 基本应用
下文示例展示了 StateFlow 的基本使用方法。

🔴 示例三： StateFlow 的基本应用。

在本示例中，我们定义 StateFlow 用于维护某个功能的开关状态。

第一步，定义 StateFlow 实例。

`StateFlow.kt` :

```kotlin
// 定义 StateFlow ，用于管理开关状态，初始值为 `false` 。
val stateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

// 可以访问 `value` 属性获取 StateFlow 容器中当前的值。
println("当前的值：${stateFlow.value}")

// 创建协程监听 StateFlow 中的消息
val scope = CoroutineScope(Dispatchers.IO)
val listenJob = scope.launch {
    // 调用 `collect` 方法监听Flow中的数据
    stateFlow.collect { value ->
        println("监听协程收到消息：$value")
    }
}
```

我们可以通过 `MutableStateFlow(< 初始值 >)` 构造方法创建可变 StateFlow 实例，并通过 `value` 属性访问当前存储的值。

此时运行示例程序，并查看控制台输出信息：

```text
当前的值：false
监听协程收到消息：false
```

根据上述输出内容可知：

StateFlow 具有粘性事件，观察者调用 `collect()` 方法注册状态监听后，回调方法将会立刻被触发一次，参数即 StateFlow 当前存储的值。

第二步，在上述代码的基础上，通过测试线程连续投送多条不同的数据，观察 StateFlow 的行为。

`StateFlow.kt` :

```kotlin
// 连续变化测试
println("测试线程发送状态：`true`")
stateFlow.value = true
println("测试线程发送状态：`false`")
stateFlow.value = false
println("测试线程发送状态：`true`")
stateFlow.value = true
```

此时运行示例程序，并查看控制台输出信息：

```text
测试线程发送状态：`true`
测试线程发送状态：`false`
测试线程发送状态：`true`
监听协程收到消息：true
```

根据上述输出内容可知：

StateFlow 在外部组件连续投送数据时，可能会丢弃中间值，仅回调最终状态给观察者。

<!-- TODO
## 数据更新
由于 StateFlow 具有去重的特性，我们在使用过程中需要注意，防止出现使用不当导致未按预期发出更新。

equals 判等决定是否更新
声明式基于不可变对象感知变化，不能使用可变对象，不能原地修改变量

理想情况
student(id name score)
val a = student
state = student

state.value = student.copy(score = 100)


student是data class，我们更新状态时调用copy方法创建新对象并修改name属性，此时对象变更，状态能够感知变化并重组。


错误用法

修改现有对象的属性

由于 StateFlow 容器指向的对象与 `initData` 指向的对象相同，修改语句 `` 执行时， StateFlow 中的内容也被同步改变，因此赋值语句执行时，StateFlow 发现当前存储的值与外部提交的值相等，不会通知观察者。




新代码都应当使用data class与不可变属性构建对象，并通过copy新建对象并赋值属性的方式修改，对于已有的使用了var class的系统，应当手动实现新建对象并copy属性的操作，或将其封装为dataclass，不能直接赋值。



# 操作符

流程操作符

onStart：在上游流启动之前被调用。
onEach：在上游流的每个值被下游发出之前调用。
onCompletion：在流程完成或取消后调用，并将取消异常或失败作为操作的原因参数传递。

需要注意的是，onStart在SharedFlow(热数据流)一起使用时，并不能保证发生在onStart操作内部或立即发生在onStart操作之后的上游流排放将被收集。这个问题我们在后面文章的热数据流时讲解。


转换操作符

transform 通用转换，修改原本的行为


map {} 可以将数据转换为另一个对象

mapNotNull {} 同map，忽略null值

fliter{} 返回true的元素继续传递给下游，返回false的元素被丢弃

filterNot/filterIsInstance/filterNotNull

zip: a.zip(b) 组合a与b之后发送给下游。

take(N) 只取0至N个结果，忽略后续结果。

takeWhile：首个false返回时，丢弃该结果与后续结果。

drop(N) 丢弃0至N个结果，将后续结果转发。


末端操作符

collect，开启收集，每次收到上游发送的结果后执行一次函数体。

toList操作符，开启收集，并返回列表。


# 异常处理

catch { cause ->
// 捕获先前操作出现的异常
cause.print
// 或发送新的值
emit(11)
}

catch操作符只对其出现位置之前流程的异常生效，出现位置之后的异常无法被处理。 


出现在末端操作符内的异常只能自行添加try catch或使用协程环境的CoroutineExceptionHandler处理。


# 线程切换

flowOn(调度器)

只影响没有被设置上下文的部分，例如

a{}.flowOn(调度器A).b{}.flowOn(调度器B)

此时b会在调度器B中执行，a仍继续使用调度器A，因为执行到B语句时，a已经被设置了调度器，不受影响，b未被设置，将被该语句影响。


# 转换


asflow

将range/list等转为flow。

flowof(1,3).collect



stateIn

shareIn

-->
