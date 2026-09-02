# 简介
Flow 是基于 Kotlin 协程的异步数据流接口，可以连续、异步地发射多个值，接收者可以观察每个值的变化并作出响应。在处理链路中flow可配置变换、去重、等，是一种灵活易用的响应式编程工具

Flow 有多个具体实现，总体上可分为“冷流”与“热流”两种类型，二者的行为具有很大差异，我们应当根据场景选择合适的实现。


冷流是一系列开发者预先定义的逻辑组合，无状态、被动的，当监听者开始接收时才会开始执行逻辑代码，且多个监听者使用各自的线程执行逻辑，能够收到逻辑代码发送的所有数据。
类似于包含回调接口参数的函数，当调用者执行函数时才会活跃，且返回数据给调用者。

冷流适用于封装需要多次返回结果的异步任务，例如：文件下载、数据库查询等任务。


热流自身不负责业务逻辑，由外部控制投送数据，监听者只会从监听时刻开始接收数据，默认不会返回监听前已经发出的数据，且多个监听者监听到的数据是同步的。

热流适用于事件通告与状态共享，例如：消息推送、UI状态管理等。


被动 | 主动
互不干扰 | 共享数据
MP3 | 广播电台



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

我们使用循环模拟下载过程，每当下载进度改变时，调用 `emit()` 方法发送当前进度给接收者。 `emit()` 方法的参数类型与 Flow 的泛型参数一致，我们使用 0 到 100 的整数表示进度，因此 `flow` 实例的类型为 `Flow<Int>` 。

第二步，执行下载过程。

我们创建两个接收协程，并在控制台输出它们的工作线程名称，观察 Flow 的行为。

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

Flow 实例的 `collect(collector: FlowCollector<T>)` 方法用于监听 Flow 产生的数据，这是一个挂起函数，监听者需要在协程作用域中调用它；该方法的唯一参数 `collector` 用于指明收到数据时的处理逻辑，默认参数 `it` 表示当前接收到的数据值。 Flow 逻辑代码中每调用一次 `emit()` 方法， FlowCollector 接口就会被调用一次，二者是一一对应的。

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

两个接收者使用各自的工作线程独立推进 Flow 逻辑代码，二者没有关联，不会相互等待。




# SharedFlow

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

SharedFlow侧重在事件，当某个事件触发，发送到队列之中，按照挂起或者非挂起、缓存策略等将事件发送到接受方，在具体使用时，SharedFlow更适合通知ui界面的一些事件，比如toast等，也适合作为viewModel和repository之间的桥梁用作数据的传输。


# StateFlow
`StateFlow<T>` 侧重于维护“状态”，

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



## 数据操作
StateFlow 具有去重的特性，因此使用时需要注意，防止出现使用不当导致未按预期发出更新。

equals 判等决定是否更新
声明式基于不可变对象感知变化，不能使用可变对象，不能原地修改变量

理想情况
student(id name score)
val a = student
state = student

state.value = student.copy(score = 100)


student是data class，我们更新状态时调用copy方法创建新对象并修改name属性，此时对象变更，状态能够感知变化并重组。


错误用法

student(id name var score)
val a = student
state = student


a.score = 100
state.value = student

修改后将对象赋值给state，此时界面不会重组，因为初始化时状态指向变量，执行 a a.score = 100时，该对象属性被修改，state也被修改，再执行
state.value = student语句实际上并没有任何作用，因此不会更新。

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
