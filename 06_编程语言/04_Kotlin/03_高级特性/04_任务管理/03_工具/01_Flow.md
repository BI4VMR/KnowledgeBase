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

当观察者注册监听后，测试线程所发出的消息，都能被监听协程接收到，二者是对应的。

## 类型
SharedFlow 是只读的、不可变的，未提供数据更新接口，只提供了注册数据变化回调的接口。前文示例中的 MutableSharedFlow 扩展自 SharedFlow 接口，提供了 `emit()` 等数据更新接口，我们可以调用这些接口更新 Flow 中存储的数据值。

在实际应用中，我们习惯声明私有可变 Flow 变量，仅在类的内部使用，同时声明一个公开不可变 Flow 变量，将其指向私有变量，供外部组件注册监听。

```kotlin
// 私有的可变 Flow ，通常使用带有下划线前缀的变量名。
private val _messageFlow: MutableSharedFlow<String> = MutableSharedFlow()

// 公开的不可变 Flow ，只允许外部组件注册监听。
val messageFlow: SharedFlow<String> = _messageFlow
```

外部组件若要更新 Flow 中存储的值，必须调用类的业务方法，这种方式便于配合日志输出监控所有更新记录，避免外部组件随意更新 Flow 导致逻辑错误。

## 缓冲与重放
当我们创建 SharedFlow 实例时，可以通过以下参数配置重放消息数量、缓冲区容量和溢出策略。

- `replay` : 重放消息数量，默认为 `0` 。当 SharedFlow 收到新的数据时，会将指定数量的数据放入缓冲区；此后若有新的观察者注册回调， SharedFlow 将会立刻分发这些历史数据给新观察者。
- `extraBufferCapacity` : 额外缓冲容量，默认为 `0` 。当观察者仍在处理前一条数据时，若有新的数据提交请求，新数据将会进入该缓冲区。该缓冲区中的数据不会重放给新注册的其他观察者。
- `onBufferOverflow` : 缓冲区溢出策略。 `replay` 和 `extraBufferCapacity` 数值之和即总缓冲区容量，当缓冲区已满时，若有新的数据提交请求，则挂起请求者协程或丢弃一些数据。

每当外部组件提交新的数据时， SharedFlow 将依次执行所有观察者的数据监听回调方法，各个观察者处理当前数据变化事件的耗时可能不同，直到所有观察者的回调方法被执行完毕后，当前数据被标记为处理完毕，此时 SharedFlow 才能分发新的数据。

若外部组件提交新的数据时，前一条数据仍有观察者正在处理，则优先将新数据放入缓冲区；若当前缓冲区已满，又有新的数据到达，则实施 `onBufferOverflow` 指定的策略：

- `BufferOverflow.SUSPEND` : 这是默认的行为，将挂起数据提交者的协程，直到前一条数据被处理完毕再恢复运行。
- `BufferOverflow.DROP_OLDEST` : 删除最早进入缓冲区的值，剩余数据依次前移一位，并将新值追加到缓冲区末尾。
- `BufferOverflow.DROP_LATEST` : 丢弃当前提交的值，不改变缓冲区内容。

外部组件有两种方式向 SharedFlow 提交数据的方式：

- `suspend emit()` : 协程方式，当策略为 `BufferOverflow.SUSPEND` 时可能被挂起。
- `tryEmit(): Boolean` : 同步方式，不会阻塞调用者线程。如果缓冲区未满，数据将进入缓冲区并返回 `true` ；如果缓冲区已满，数据将被丢弃并返回 `false` 。当 SharedFlow 为 `SUSPEND` 模式且缓冲区大小为 0 时，该方法总是返回 `false` 。

🟠 示例三： SharedFlow 的缓存行为。

在本示例中，我们测试 SharedFlow 的缓存策略，了解它们的行为差异。

第一步，测试重放行为。

`SharedFlow.kt` :

```kotlin
// 定义 SharedFlow ，用于通告事件消息。
val sharedFlow: MutableSharedFlow<Int> = MutableSharedFlow(
    replay = 1,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.SUSPEND
)


// 发送一些消息
runBlocking {
    println("测试线程发送消息： [1]")
    sharedFlow.emit(1)
    println("测试线程发送消息： [2]")
    sharedFlow.emit(2)
}


// 创建协程监听 SharedFlow 中的消息
val scope = CoroutineScope(Dispatchers.IO)
val listenJob = scope.launch {
    println("监听协程注册回调")
    sharedFlow.collect { value ->
        println("监听协程收到消息： [$value]")
        delay(1000.milliseconds)
        println("监听协程处理消息： [$value] 完毕！")
    }
}
```

此时运行示例程序，并查看控制台输出信息：

```text
测试线程发送消息： [1]
测试线程发送消息： [2]
监听协程注册回调
监听协程收到消息： [2]
```

根据上述输出内容可知：

该 Flow 被配置为重放一个消息、缓冲一个消息，因此观察者注册监听时，先前被提交的 2 号消息立刻被重放了。

第二步，测试 `SUSPEND` 行为。

在上述代码的基础上，通过测试线程继续提交一些消息。

`SharedFlow.kt` :

```kotlin
sharedFlow.emit(3)
println("测试线程发送消息： [3] 完毕，当前时间：[${getTime()}]")
sharedFlow.emit(4)
println("测试线程发送消息： [4] 完毕，当前时间：[${getTime()}]")
sharedFlow.emit(5)
println("测试线程发送消息： [5] 完毕，当前时间：[${getTime()}]")
```

此时运行示例程序，并查看控制台输出信息：

```text
监听协程收到消息： [2]
测试线程发送消息： [3] 完毕，当前时间：[11:37:07.124]
测试线程发送消息： [4] 完毕，当前时间：[11:37:07.128]
监听协程处理消息： [2] 完毕！
监听协程收到消息： [3]
测试线程发送消息： [5] 完毕，当前时间：[11:37:07.865]
监听协程处理消息： [3] 完毕！
监听协程收到消息： [4]
监听协程处理消息： [4] 完毕！
监听协程收到消息： [5]
监听协程处理消息： [5] 完毕！
```

根据上述输出内容可知：

当测试线程提交 3 与 4 号消息时， 2 号消息仍在被观察者处理，它们将会进入缓冲区， 3 号在 `extraBufferCapacity` 中， 4 号在 `replay` 中。

当测试线程提交 5 号消息时，缓冲区已满，此时策略为 `SUSPEND` ，因此测试协程被挂起；我们在观察者回调内挂起 1 秒模拟处理耗时，因此约 1 秒后 2 号消息处理结束， 3 号消息被分发且缓冲区空出，此时测试协程解除挂起并继续提交 5 号消息。

第三步，测试 `DROP_OLDEST` 行为。

我们将 SharedFlow 的缓冲溢出策略改为 `DROP_OLDEST` ，其他代码保持不变，再次运行示例程序，并查看控制台输出信息：

```text
测试线程发送消息： [1]
测试线程发送消息： [2]
监听协程注册回调
监听协程收到消息： [2]
测试线程发送消息： [3] 完毕，当前时间：[13:47:46.497]
测试线程发送消息： [4] 完毕，当前时间：[13:47:46.501]
测试线程发送消息： [5] 完毕，当前时间：[13:47:46.501]
监听协程处理消息： [2] 完毕！
监听协程收到消息： [4]
监听协程处理消息： [4] 完毕！
监听协程收到消息： [5]
监听协程处理消息： [5] 完毕！
```

根据上述输出内容可知：

3 、 4 、 5 号消息连续地被提交，测试协程没有被挂起。

当 5 号消息被提交时，观察者仍在处理 2 号消息，且缓冲区已满，此时最旧的 3 号消息被丢弃， 5 号消息加入队列尾部。观察者处理完 2 号消息后，只收到了 4 和 5 号消息，符合预期。

第四步，测试 `DROP_LATEST` 行为。

我们将 SharedFlow 的缓冲溢出策略改为 `DROP_LATEST` ，其他代码保持不变，再次运行示例程序，并查看控制台输出信息：

```text
测试线程发送消息： [1]
测试线程发送消息： [2]
监听协程注册回调
监听协程收到消息： [2]
测试线程发送消息： [3] 完毕，当前时间：[13:53:52.358]
测试线程发送消息： [4] 完毕，当前时间：[13:53:52.365]
测试线程发送消息： [5] 完毕，当前时间：[13:53:52.366]
监听协程处理消息： [2] 完毕！
监听协程收到消息： [3]
监听协程处理消息： [3] 完毕！
监听协程收到消息： [4]
监听协程处理消息： [4] 完毕！
```

根据上述输出内容可知：

3 、 4 、 5 号消息连续地被提交，测试协程没有被挂起。

当 5 号消息被提交时，观察者仍在处理 2 号消息，且缓冲区已满，此时最新的 5 号消息被丢弃。观察者处理完 2 号消息后，只收到了 3 和 4 号消息，符合预期。


# StateFlow
## 简介
`StateFlow<T>` 是一种专用的热流实现，侧重于状态管理。

此处我们以常见的功能开关状态为例，比较 StateFlow 与 SharedFlow 的区别：

- 初始值：一个功能要么是开启的、要么是关闭的，必然存在状态；因此我们创建 StateFlow 时必须指定初始值，而 SharedFlow 没有初始值。
- 缓冲数量：我们只需要维护最新的状态，因此 StateFlow 只保留最后接收到的值，而 SharedFlow 默认不缓冲数据。
- 粘性事件：观察者调用 `collect()` 方法监听数据变化时，将会立刻收到一次事件回调，数据值为缓冲区中的值。这种行为使 UI 初始化时自动完成状态同步，开发者无需手动获取最新状态再更新控件。
- 忽略重复值：当外部组件向 StateFlow 提交数据时， StateFlow 会将当前持有的值与外部传入的值作比较，若二者相同则不通知观察者，仅当新旧数据发生变化时才通知观察者。
- 丢弃中间值：若外部组件短时间内提交多条数据， StateFlow 只会使用最后一个状态通知观察者。该特性在状态管理场景中无影响，因为观察者期望获取最终状态，并不关心中间值。在事件分发场景中，观察者期望能够接收每条事件，如果我们错误地将 StateFlow 用于猝发高频事件场景，可能导致部分事件丢失。

下文列表展示了两种热流实现的核心行为差异：

<div align="center">

|    特性    | StateFlow | SharedFlow |
| :--------: | :-------: | :--------: |
|   初始值   | 必须指明  |   不支持   |
|  缓冲数量  |   1 条    |  默认为 0  |
|  粘性事件  |   1 条    | 默认不重放 |
| 忽略重复值 | 强制开启  |  默认关闭  |
| 丢弃中间值 | 强制开启  |  默认关闭  |

</div>

## 基本应用
下文示例展示了 StateFlow 的基本使用方法。

🔴 示例四： StateFlow 的基本应用。

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

## 数据更新
由于 StateFlow 具有去重的特性，在使用过程中，我们需要注意数据更新方法，避免不当操作造成未按预期触发更新。

当我们提交数据时，StateFlow 将使用 `equals()` 方法比较内部存储的值和新提交的值是否相同，仅当二者不等时，才会更新数据并通知观察者。

基于上述规则，我们不能读取 StateFlow 中存储的值并修改其属性，必须创建新对象填入需要更新的属性值，然后从旧对象复制无需更新的属性值。这种方式要求我们不能以对象本身作为实体的唯一标识，而是需要以 ID 等字段作为唯一标识。

🔴 示例五：数据更新。

在本示例中，我们了解 StateFlow 的常见错误用法，并将其改正。

第一步，准备数据实体类。

`StateFlow.kt` :

```kotlin
data class Student(
    val id: String = "",
    var name: String = "",
    var age: Int = 0
)
```

第二步，修改原值的属性并提交更新。

`StateFlow.kt` :

```kotlin
val initData = Student("1", "张三", 20)
// 定义 StateFlow ，初始值为 `initData` 。
val stateFlow: MutableStateFlow<Student> = MutableStateFlow(initData)


// 开启协程接收Flow中的数据
val scope = CoroutineScope(Dispatchers.IO)
val listenJob = scope.launch {
    stateFlow.collect { value ->
        println("监听协程收到消息：$value")
    }
}


// 直接修改 Flow 容器中对象的属性
initData.age = 21

// 读取当前 Flow 容器中的对象
println("Flow 当前存储的状态：${stateFlow.value}")

// 使用原对象更新 Flow
println("测试线程更新状态（原对象）：$initData")
stateFlow.value = initData
```

此时运行示例程序，并查看控制台输出信息：

```text
监听协程收到消息：Student(id=1, name=张三, age=20)
Flow 当前存储的状态：Student(id=1, name=张三, age=21)
测试线程更新状态（原对象）：Student(id=1, name=张三, age=21)
```

根据上述输出内容可知：

StateFlow 中存储的值与 `initData` 变量所指向的对象相同，当 `initData.age = 21` 语句执行后，对象属性已被改变，此时再执行 `stateFlow.value = initData` 更新数据是无效的，二者 `equals()` 的结果为 `true` 。

第三步，在上述代码的基础上，创建新的对象并提交更新。

`StateFlow.kt` :

```kotlin
// 创建新对象，指明需要更新的属性，并复制其他属性。
val newData = initData.copy(age = 22)

// 使用新对象更新 Flow
println("测试线程更新状态（新对象）：$newData")
stateFlow.value = newData
```

此时运行示例程序，并查看控制台输出信息：

```text
测试线程更新状态（新对象）：Student(id=1, name=张三, age=22)
监听协程收到消息：Student(id=1, name=张三, age=22)
```

根据上述输出内容可知：

我们使用 Data Class 的 `copy()` 方法创建新对象并复制所有属性，然后将 `age` 属性改写为 22 ，可以正确触发更新，此处 `newData` 与 StateFlow 中存储的值是不同的对象。

---

如果 中存储的是列表，我们需要使用 创建副本并修改需要更新的元素。

如果只是增加/删除/重排序
只需要生成新列表，未改变的对象无需复制，直接引用即可。

🔴 示例六：列表更新。

在本示例中，我们了解 StateFlow 内容为列表时的更新方式。

第一步，修改原列表并提交更新。

`StateFlow.kt` :

```kotlin
val initList: MutableList<Student> = mutableListOf(
    Student("1", "张三", 20),
    Student("2", "李四", 21)
)
val stateFlow: MutableStateFlow<List<Student>> = MutableStateFlow(initList)


// 开启协程接收Flow中的数据
val scope = CoroutineScope(Dispatchers.IO)
val listenJob = scope.launch {
    stateFlow.collect { list ->
        println("----- 监听协程收到消息，列表长度：${list.size} -----")
        list.forEach { student -> println("$student") }
        println("----- 监听协程收到消息，完毕。 -----")
    }
}

// 直接修改 Flow 容器中的列表项
initList[1].name = "李田所"
initList[1].age = 24

// 使用原列表更新 Flow
println("测试线程更新状态（原列表）：$initList")
stateFlow.value = initList
```

此时运行示例程序，并查看控制台输出信息：

```text
----- 监听协程收到消息，列表长度：2 -----
Student(id=1, name=张三, age=20)
Student(id=2, name=李四, age=21)
----- 监听协程收到消息，完毕。 -----
测试线程更新状态（原列表）：[Student(id=1, name=张三, age=20), Student(id=2, name=李田所, age=24)]
```

根据上述输出内容可知：

如果我们直接修改 StateFlow 中存储的列表内容，也无法触发更新，原理与 StateFlow 数据类型为对象时相同。

第二步，在上述代码的基础上，创建新的列表并提交更新。

`StateFlow.kt` :

```kotlin
// 创建新列表
val newList = initList.toMutableList()
// 创建新对象，替换列表中的旧数据。
val newData = newList[1].copy(age = 25)
newList[1] = newData

// 使用新对象更新 Flow
println("测试线程更新状态（新列表）：$newList")
stateFlow.value = newList
```

对于属性未改变的对象，可以直接复用原列表中的引用；对于属性已改变的对象，需要采用新建对象的方式。这种操作符合列表等值判断的规则，因此可以更新成功。

此时运行示例程序，并查看控制台输出信息：

```text
测试线程更新状态（新列表）：[Student(id=1, name=张三, age=20), Student(id=2, name=李田所, age=25)]
----- 监听协程收到消息，列表长度：2 -----
Student(id=1, name=张三, age=20)
Student(id=2, name=李田所, age=25)
----- 监听协程收到消息，完毕。 -----
```


# 操作符
<!-- TODO
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
