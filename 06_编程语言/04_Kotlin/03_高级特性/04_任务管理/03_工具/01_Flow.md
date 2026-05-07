# 简介
数据流分为“冷流”和“热流”，它们的行为具有很大的差异，我们应当根据场景选择合适的实现。

冷流是一系列开发者预先定义的逻辑组合，当监听者开始接收时才会开始执行逻辑代码，且多个监听者使用各自的线程执行逻辑，外部无法干涉。

冷流适合封装需要多次返回结果的异步任务，例如网络下载、数据库查询等任务。


热流侧重于状态、事件的传递与共享，自身不负责业务逻辑，由外部控制投送数据，监听者只会从监听时刻开始接收数据。

热流适用于状态共享与发布、例如全局状态管理、事件通知、UI更新等。




# 基本应用
## Flow
flow是冷流的实现


🔴 示例一：Flow的基本应用。

在本示例中，我们定义一个Flow实例，模拟下载文件的过程，然后接收下载进度并显示到控制台上。

"TestBase.kt":

```kotlin
// 定义Flow
val flow: Flow<Int> = flow {
    println("Download start. Thread Name:[${Thread.currentThread().name}]")

    // 模拟下载进度从0至100
    (0..100).forEach { progress ->
        // 模拟下载耗时
        delay(10L)

        // 发送当前进度给接收者
        emit(progress)
    }

    println("Download end. Thread Name:[${Thread.currentThread().name}]")
}


// 开启协程接收Flow中的数据
val scope = CoroutineScope(Dispatchers.IO)
val job1 = scope.launch {
    flow.collect {
        println("Download progress change. Value:[$it] Thread Name:[${Thread.currentThread().name}]")
    }
}

val job2 = scope.launch {
    flow.collect {
        println("Download progress change. Value:[$it] Thread Name:[${Thread.currentThread().name}]")
    }
}
```


`flow(block: suspend FlowCollector<T>.() -> Unit)` 函数用于创建Flow实例，唯一参数 `block` 即该流的逻辑代码，此处我们在循环中模拟下载进度，每次进度改变时，调用 `emit()` 方法发送数据给接收者。

`collect(collector: FlowCollector<T>)` 方法用于监听Flow产生的数据，这是一个挂起函数，接收者需要创建协程调用，唯一参数 `collector` 用于指定每次接收数据时的处理逻辑，将在Flow逻辑代码的`emit()` 方法被调用时触发，默认参数 `it` 表示数据值。


```text
Download start. Thread Name:[DefaultDispatcher-worker-1]
Download start. Thread Name:[DefaultDispatcher-worker-3]
Download progress change. Value:[0] Thread Name:[DefaultDispatcher-worker-3]
Download progress change. Value:[0] Thread Name:[DefaultDispatcher-worker-1]
Download progress change. Value:[1] Thread Name:[DefaultDispatcher-worker-1]
Download progress change. Value:[1] Thread Name:[DefaultDispatcher-worker-3]


Download progress change. Value:[100] Thread Name:[DefaultDispatcher-worker-1]
Download end. Thread Name:[DefaultDispatcher-worker-1]
Download progress change. Value:[100] Thread Name:[DefaultDispatcher-worker-2]
Download end. Thread Name:[DefaultDispatcher-worker-2]
```

我们创建了两个接收者，每个接收者使用自己的工作线程执行Flow定义时的逻辑，每次emit()回调collect()设定的逻辑，相互之间没有关联。



## SharedFlow

SharedFlow<T>

一个更通用的、可高度配置的热流。
特点：

可以配置 replay 缓存，让新的收集者可以收到最近的N个历史数据。如果 replay = 0 (默认)，则不缓存，新收集者不会收到任何历史数据。
可以配置缓存策略、订阅策略等。


场景：非常适合用于广播一次性的事件（如显示 Toast、导航事件），此时通常配置 replay = 0。


sharedflow 不支持初始值， 默认状态下不会丢弃。

replay：重新发射给新的订阅者的值的数量，可以将旧的数据回播给新的订阅者。不能为负数，默认为0。
extraBufferCapacity：在replay基础上的缓冲池的数量，当有剩余缓冲区空间时，调用emit发射数据不会被挂起，同样的不能为负数，默认值为0。
onBufferOverflow：配置一个emit在缓冲区溢出时的触发操作。默认为BufferOverflow.SUSPEND，缓存溢出时挂起。另外还有BufferOverflow.DROP_OLDEST在溢出时删除缓冲区中最旧的值，将新值添加到缓冲区，不会进行挂起。BufferOverflow.DROP_LATEST在缓冲区溢出时删除当前添加到缓冲区的最新值来保持缓冲区内容不变，不会进行挂起。



tryEmit() :Boolean 同步发送数据，如果缓冲满将发送失败并返回false，因此缓冲为0时总是失败，不可用。
emit() 发送数据，挂起函数，如果缓冲区满可能阻塞当前协程（该行为可由溢出策略控制。）




## StateFlow
StateFlow<T>

一个专门设计用来持有状态的热流。
特点：

永远有值：创建时必须提供一个初始值。
粘性：新的收集者会立即收到最新的（当前）状态值。
值去重：如果连续设置相同的值，只会发射一次。

可能会丢数据


场景：非常适合用于 Android ViewModel 中管理 UI 状态。




val stateFlow = MutableStateFlow<Int>(value = -1)
每次更新数据都会和旧数据做一次比较，只有不同时候才会更新数值。
stateflow，可以设置初始值，保留最新的一个状态，因此会丢失中间的数据。

StateFlow重点在状态，ui永远有状态，所以StateFlow必须有初始值，同时对ui而言，过期的状态毫无意义，所以stateFLow永远更新最新的数据（和liveData相似），所以必须有粘滞度=1的粘滞事件，让ui状态保持到最新。另外在一个时间内发送多个事件，不会管中间事件有没有消费完成都会执行最新的一条.(中间值会丢失)
SharedFlow侧重在事件，当某个事件触发，发送到队列之中，按照挂起或者非挂起、缓存策略等将事件发送到接受方，在具体使用时，SharedFlow更适合通知ui界面的一些事件，比如toast等，也适合作为viewModel和repository之间的桥梁用作数据的传输。


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
