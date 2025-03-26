# 简介
<!-- TODO

Mockito是一个针对Java语言的模拟工具，即使有KT扩展，对于Kotlin的语法支持仍然有限，因此更建议使用MockK。

-->

# 基本应用

## 依赖隔离
下文示例展示了MockK的基本使用方法：




🔴 示例一：【标题】。

在本示例中，【此处填写示例背景】。




"DBHelper.kt":

```kotlin
class DBHelper {
    // 查询用户信息；返回用户信息Map，键为ID，值为名称。
    fun queryUsers(): Map<Long, String> = mapOf(1L to "张三", 2L to "李四")
}
```


"UserManager.kt":

```kotlin
class UserManager {

    // 依赖项：数据库
    private val mDBHelper: DBHelper = DBHelper()

    // 获取所有用户名称
    fun getUserNames(): List<String> {
        return mDBHelper.queryUsers()
            .values
            .toList()
    }
}
```



"UserManagerTest.kt":

```kotlin
@Test
fun testGetUserNames() {
    // 模拟数据
    val mockDatas: Map<Long, String> = mapOf(1L to "来宾账户", 2L to "用户A", 3L to "用户B")

    // 创建DBHelper的Mock对象
    val mockDBHelper: DBHelper = mockk(relaxed = true)
    // 定义行为：如果 `queryUsers()` 方法被调用，则返回模拟数据。
    every { mockDBHelper.queryUsers() } returns mockDatas

    // 构造待测类的对象，并注入Mock对象作为依赖。
    val manager = UserManager()
    mockDBHelper.injectMock(manager, "mDBHelper")

    // 调用待测方法
    val users = manager.getUserNames()

    // 查看返回的内容
    users.forEachIndexed { index, s ->
        println("Index:[$index] Name:[$s]")
    }
    // 验证Mock对象的方法是否被调用
    verify { mockDBHelper.queryUsers() }
    // 验证待测方法的返回值是否与预期一致
    Assert.assertTrue(mockDatas.values.containsAll(users))
}
```



"MockUtils.kt":

```kotlin
/**
 * 将Mock对象注入到目标对象中（非安全）。
 *
 * @param[target] 目标对象。
 * @param[fieldName] 目标属性名称。
 */
inline fun <reified T : Any> T.injectMockUnsafe(target: Any, fieldName: String) {
    target.javaClass.getDeclaredField(fieldName)
        .apply {
            isAccessible = true
            set(target, this@injectMockUnsafe)
        }
}
```



此时运行示例程序，并查看控制台输出信息：

```text
Index:[0] Name:[来宾账户]
Index:[1] Name:[用户A]
Index:[2] Name:[用户B]
```


根据上述输出内容可知：



## 宽松模式

1. 核心作用

在 MockK 中，relaxed = true 表示创建一个宽松的 Mock 对象：

    未明确 Stub 的方法不会抛出异常，而是返回默认值（如 null、0、false 或空集合）。

    适用于不需要测试所有交互的场景，避免为大量无关方法手动定义行为。

kotlin
复制

val relaxedMock = mockk<SomeClass>(relaxed = true)
relaxedMock.unstubbedMethod()  // 返回 null 或默认值，而非抛出异常

2. 默认返回值规则

    基本类型：0、false

    引用类型：null

    集合类型：空集合（如 emptyList()）

    Unit 函数：直接通过，不抛异常



io.mockk.MockKException: no answer found for DBHelper(#1).saveLog(GetUserNames) among the configured answers: (DBHelper(#1).queryUsers()))

	at io.mockk.impl.stub.MockKStub.defaultAnswer(MockKStub.kt:93)
	at io.mockk.impl.stub.MockKStub.answer(MockKStub.kt:44)
	at io.mockk.impl.recording.states.AnsweringState.call(AnsweringState.kt:16)
	at io.mockk.impl.recording.CommonCallRecorder.call(CommonCallRecorder.kt:53)
	at io.mockk.impl.stub.MockKStub.handleInvocation(MockKStub.kt:271)
	at io.mockk.impl.instantiation.JvmMockFactoryHelper$mockHandler$1.invocation(JvmMockFactoryHelper.kt:24)
	at io.mockk.proxy.jvm.advice.Interceptor.call(Interceptor.kt:21)
	at net.bi4vmr.study.mockk.base.DBHelper.saveLog(DBHelper.kt:34)
	at net.bi4vmr.study.mockk.base.UserManager.getUserNames2(UserManager.kt:23)


    /**
     * 输出日志。
     *
     * @param[info] 消息内容。
     */
    fun saveLog(info: String) {
        println(info)
    }


## MockK注解
在前文示例中，我们使用 `mockk()` 方法创建了一些Mock对象；如果需要Mock的类数量较多，我们也可以使用MockK提供的注解。

🔴 示例一：使用注解创建Mock对象。

在本示例中，我们以JUnit4框架为例，使用MockK提供的注解创建Mock对象。

"AnnotationTest.kt":

```text
// 创建一个DBHelper类的Mock对象
@MockK
lateinit var mockDBHelper1: DBHelper

// 创建一个DBHelper类的Mock对象（宽松模式）
@RelaxedMockK
lateinit var mockDBHelper2: DBHelper

// 创建一个DBHelper类的Mock对象（宽松模式）
@MockK(relaxed = true)
lateinit var mockDBHelper3: DBHelper

// 创建一个DBHelper类的Mock对象（宽松模式，仅对无返回值的方法生效）。
@MockK(relaxUnitFun = true)
lateinit var mockDBHelper4: DBHelper

@Before
fun setup() {
    // 若要使用MockK注解，需要在执行其他操作前先初始化。
    MockKAnnotations.init(this)
}

@After
fun teardown() {
    // 撤销所有Mock
    unmockkAll()
}
```

`@MockK` 注解表示被修饰的全局变量是Mock对象，程序运行时MockK会根据变量类型自动创建对象并完成赋值。该注解拥有两个属性，其中 `relaxed` 表示是否启用宽松模式； `relaxUnitFun` 表示是否启用仅针对无返回值方法的宽松模式。

`@RelaxedMockK` 注解等同于 `@MockK(relaxed = true)` 。

上述注解默认没有任何效果，为了使它们生效，我们需要在测试代码执行前调用 `MockKAnnotations.init(this)` 方法；测试代码执行完毕后，我们还应该调用 `unmockkAll()` 方法撤销所有Mock行为。


# 行为验证

测试框架可以验证有直接返回值的方法，但是对于没有返回值的 void 方法应该如何测试呢？void 方法的输出结果其实是调用了另外一个方法，所以需要验证该方法是否有被调用，调用时参数是否正确。

# 参数匹配器


# 参数捕获器

前文章节中，我们通过verify方法对待测方法的参数进行验证，但仅限首次调用时，且匹配器只能匹配预设的类型，对于复杂条件无法处理，此时我们可以使用参数捕获器。

常见的场景：

参数为引用类型的回调方法： result(a: List<String>) 我们希望验证列表长度为5，匹配器无法处理这种场景。
参数为引用类型的普通方法：proc(a: SS) 该方法会将变量的值进行修改，我们希望在方法结束后校验参数值的变化。

🔴 示例一：使用参数捕获器验证回调方法。

在本示例中，我们使用参数捕获器验证回调方法的参数是否符合预期。

第一步，编写业务代码。

我们首先定义一个接口， `onResult()` 方法用于回调事件。

"FileCallback.kt":

```kotlin
interface FileCallback {
    fun onResult(result: Boolean, message: String)
}
```

接下来，我们编写业务逻辑。

"FileHelper.kt":

```kotlin
class FileHelper {

    fun saveFile(path: String, callback: FileCallback) {
        try {
            File(path).createNewFile()
            callback.onResult(true, "OK!")
        } catch (e: Exception) {
            callback.onResult(false, "${e.message}")
            System.err.println("Save file failed! Reason:[${e.message}]")
        }
    }
}
```

此处 `saveFile()` 方法的第二参数为FileCallback接口实现，文件操作结果将会通过 `onResult()` 方法反馈给调用者。

第二步，编写测试代码。

"FileHelperTest.kt":

```kotlin
@Test
fun testSaveFile() {
    val fileHelper = FileHelper()
    // 创建Callback的Mock对象
    val mockCallback: FileCallback = mockk(relaxed = true)

    // 调用待测方法，传入Callback的Mock对象
    val invalidPath = "/invalid_path.txt"
    fileHelper.saveFile(invalidPath, mockCallback)

    // 定义捕获器， `slot()` 方法的泛型即参数类型。
    val captorResult = slot<String>()

    // 验证回调方法已触发，并使用 `capture()` 方法捕获第二个参数。
    verify {
        mockCallback.onResult(any(), capture(captorResult))
    }

    // 查看捕获到的参数值
    val capturedValue: String = captorResult.captured
    println("捕获到的参数值:[$capturedValue]")
    // 进一步验证该参数值
    assertFalse(capturedValue.startsWith("OK"))
}
```




当回调方法被触发后，我们可以在 `verify()` 方法中使用 `capture()` 方法配置参数捕获器，检测


```text
捕获到的参数值:[拒绝访问。]
```




🔴 示例一：使用参数捕获器记录所有参数。

在本示例中，我们使用参数捕获器记录所有的参数值。

我们在前文“示例”的基础上进行修改，在 `onResult()` 方法上设置捕获器。

"FileHelperTest.kt":

```kotlin
@Test
fun `testSaveFile2`() {
    val capturedValues = mutableListOf<String>()

    val fileHelper = FileHelper()
    // 创建Callback的Mock对象
    val mockCallback: FileCallback = mockk()
    // Mock回调方法，使用List作为捕获接收器。
    every { mockCallback.onResult(any(), capture(capturedValues)) } returns Unit

    // 多次调用待测方法，传入Callback的Mock对象
    val invalidPath = "/invalid_path.txt"
    fileHelper.saveFile(invalidPath, mockCallback)
    fileHelper.saveFile(invalidPath, mockCallback)
    val validPath = "/tmp/valid_path.txt"
    fileHelper.saveFile(validPath, mockCallback)
    File(validPath).deleteOnExit()

    // 查看捕获到的参数值
    capturedValues.forEachIndexed { index, s ->
        println("Index:[$index] Value:[$s]")
    }
}
```

此处我们需要在Mock回调方法时设置捕获器，与先前在 `verify()` 中设置捕获器的操作不同，因为我们需要捕获所有参数，`verify()` 只能单次验证。



```text
Index:[0] Value:[拒绝访问。]
Index:[1] Value:[拒绝访问。]
Index:[2] Value:[系统找不到指定的路径。]
```
