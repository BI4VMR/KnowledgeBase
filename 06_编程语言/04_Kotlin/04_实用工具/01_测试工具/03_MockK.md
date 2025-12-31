# 简介
Mockito是一个针对Java语言的Mock工具，即使提供了Kotlin扩展，它对Kotlin的功能支持仍然有限，例如：无法模拟Kotlin的Object、函数式编程支持不佳等。

MockK是专门为Kotlin语言设计的Mock工具，使用方法与Mockito类似，并且支持Kotlin的专有特性，它的官方网站为： [🔗 MockK](https://mockk.io/) 。

本章的示例工程详见以下链接：

- [🔗 示例工程：MockK](https://github.com/BI4VMR/Study-Kotlin/tree/master/M04_Utils/C01_Test/S03_MockK)


# 基本应用
## 依赖隔离
Mock工具的基本用途是模拟待测接口的周边接口，提供假数据与行为，使我们专注于待测接口本身，不必关心外围环境的变化。

🔴 示例一：模拟待测接口的依赖项。

在本示例中，我们创建Mock对象，并将它们注入到待测对象中，实现依赖隔离。

第一步，我们在构建系统中声明对于MockK组件的依赖。

如果构建系统为Maven，我们可以使用下文代码块中的语句声明依赖：

"pom.xml":

```xml
<dependency>
    <groupId>io.mockk</groupId>
    <artifactId>mockk</artifactId>
    <version>1.13.17</version>
    <scope>test</scope>
</dependency>
```

如果构建系统为Gradle，我们可以使用下文代码块中的语句声明依赖：

"build.gradle":

```groovy
dependencies {
    testImplementation 'io.mockk:mockk:1.13.17'
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
dependencies {
    testImplementation("io.mockk:mockk:1.13.17")
}
```

第二步，我们编写业务代码。

此处以用户管理功能为例，我们编写一个UserManager类提供用户信息查询接口，其依赖于数据库工具类DBHelper提供的接口。

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

第三步，我们编写测试代码。

我们的目标是测试UserManager中的接口是否正常，并不关心DBHelper连接何种数据库、用户信息表具有何种结构，因此我们需要创建DBHelper类的Mock对象，给UserManager提供预设的用户信息。

"UserManagerTest.kt":

```kotlin
@Test
fun testGetUserNames() {
    // 模拟数据
    val mockDatas: Map<Long, String> = mapOf(1L to "来宾账户", 2L to "用户A", 3L to "用户B")

    // 创建DBHelper的Mock对象
    val mockDBHelper: DBHelper = mockk()
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

我们首先通过 `mockk<T>()` 方法创建Mock对象，并通过 `every { mockDBHelper.queryUsers() } returns mockDatas` 语句定义Mock对象的行为：如果 `mockDBHelper` 对象的 `queryUsers()` 方法被调用，则返回 `mockDatas` 给调用者。

接着，我们创建了待测接口所在类UserManager的对象，并通过反射将Mock对象注入到UserManager中，扩展方法 `injectMock()` 的具体实现详见本章示例工程。

此时运行示例程序，并查看控制台输出信息：

```text
Index:[0] Name:[来宾账户]
Index:[1] Name:[用户A]
Index:[2] Name:[用户B]
```

根据上述输出内容可知：

UserManager调用DBHelper中的接口后，输出内容确实为测试代码提供的模拟数据，并非DBHelper的内置数据，说明Mock操作成功。

## 宽松模式
若待测方法调用了Mock对象中的某个方法，且我们没有预先为此Mock方法定义行为，MockK不知道应当执行何种动作，就会抛出异常提醒开发者。

Mock对象中的部分方法执行与否对测试逻辑没有负面影响，例如：日志记录，我们可以使用 `every { <日志记录方法> } just runs` 语句定义它们的行为：“什么都不做”，避免运行时出现异常；但有时这种方法数量较多，手动定义会产生大量重复代码，更好的方案是使用宽松模式创建Mock对象。

宽松模式的Mock对象会为每个方法添加默认行为：“什么都不做”，对于有返回值的方法，返回值内容如下文列表所示：

- 数值型：返回 `0` 。
- 布尔型：返回 `false` 。
- 引用类型：返回 `null` 。
- 集合类型：返回空集合。

🟠 示例二：使用宽松模式创建Mock对象。

在本示例中，我们使用宽松模式创建Mock对象，并调用未手动定义行为的Mock方法。

第一步，我们对前文“示例一”的业务代码进行修改。

我们在DBHelper类中新增一个日志记录方法 `saveLog()` ，然后在UserManager中调用该方法。

"UserManager.kt":

```kotlin
// 获取所有用户名称（新增了日志记录的步骤）
fun getUserNames2(): List<String> {
    // 新增日志记录操作
    mDBHelper.saveLog("GetUserNames")
    return mDBHelper.queryUsers()
        .values
        .toList()
}
```

此时运行UserManagerTest中的测试方法，控制台就会输出以下错误信息：

```text
io.mockk.MockKException: no answer found for DBHelper(#1).saveLog(GetUserNames) among the configured answers: (DBHelper(#1).queryUsers())
```

这是因为我们只定义了DBHelper中 `queryUsers()` 方法的行为，没有定义 `saveLog()` 方法的行为。

第二步，我们使用宽松模式生成DBHelper的Mock对象。

由于 `saveLog()` 方法对测试逻辑没有任何影响，我们可以用宽松模式创建DBHelper的Mock对象，为其定义“什么都不做”的默认行为。

"UserManagerTest.kt":

```kotlin
@Test
fun testGetUserNames2() {
    val mockDatas: Map<Long, String> = mapOf(1L to "来宾账户", 2L to "用户A", 3L to "用户B")

    // 创建DBHelper的Mock对象（使用relaxed = true为没有明确定义行为的类添加默认行为）
    val mockDBHelper: DBHelper = mockk(relaxed = true)
    // 定义行为：如果 `queryUsers()` 方法被调用，则返回模拟数据。
    every { mockDBHelper.queryUsers() } returns mockDatas

    // 构造待测类的对象，并注入Mock对象作为依赖。
    val manager = UserManager()
    mockDBHelper.injectMock(manager, "mDBHelper")

    // 调用待测方法
    val users = manager.getUserNames2()
}
```

`mockk()` 方法的 `relaxed` 参数用于控制是否需要启用宽松模式，默认为禁用，我们将其设为 `true` 即可为所有方法定义默认行为。

有时宽松模式的默认返回值会误导我们编写错误的代码，为了解决此类问题， `mockk()` 方法提供了一个 `relaxUnitFun` 参数，这种模式只为Mock对象的无返回值方法定义默认行为，而有返回值方法则保持“若未定义行为则抛出异常”。我们可以根据实际情况选择 `relaxed` 或 `relaxUnitFun` 模式。

## 常用注解
在前文示例中，我们使用 `mockk()` 方法创建了一些Mock对象；如果需要Mock的对象数量较多，我们也可以使用MockK提供的注解。

🟡 示例三：使用注解创建Mock对象。

在本示例中，我们以JUnit 4平台为例，使用MockK提供的注解创建Mock对象。

"AnnotationTest.kt":

```kotlin
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

@Test
fun testGetUserNames(){
    // 在此处使用Mock对象
}
```

`@MockK` 注解表示被修饰的全局变量是Mock对象，程序运行时MockK会根据变量类型自动创建对象并完成赋值。该注解拥有两个属性，其中 `relaxed` 表示是否启用宽松模式； `relaxUnitFun` 表示是否启用仅针对无返回值方法的宽松模式。

`@RelaxedMockK` 注解等同于 `@MockK(relaxed = true)` 。

上述注解默认没有任何效果，为了使它们生效，我们需要在测试代码执行前调用 `MockKAnnotations.init(this)` 方法；测试代码执行完毕后，我们还应该调用 `unmockkAll()` 方法撤销所有Mock行为。

## Kotlin相关
Mockk



🟢 示例四：Mock Object中的方法。

在本示例中，我们使用MockK模拟Object中的方法。

第一步，我们编写业务代码。

"Utils.kt":

```kotlin
object Utils {
    fun getCurrentTime(): Long = System.currentTimeMillis()
}
```

第二步，我们编写测试代码。

"UtilsTest.kt":

```kotlin
// Mock Utils中的普通方法
mockkObject(Utils)
// 定义行为
every { Utils.getCurrentTime() } returns 1234567890L
// 调用Mock方法
println("Utils#getCurrentTime:[${Utils.getCurrentTime()}]")

// 撤销Mock（可选）
unmockkObject(Utils)
```

我们可以使用 `mockkObject(objects: Any)` 方法启用Object的Mock，然后通过 `every {}` 语句定义Mock对象的行为。

如果我们不再需要针对某个Object的Mock，可以调用 `unmockkObject(objects: Any)` 方法撤销Mock行为。




🔵 示例五：Mock Object中的静态方法。

在本示例中，我们使用MockK模拟Object中的 `@JvmStatic` 方法。

第一步，我们编写业务代码。

"Utils.kt":

```kotlin
object Utils {
    @JvmStatic
    fun getURL(): String {
        return "http://192.168.1.1/"
    }
}
```

第二步，我们编写测试代码。

"UtilsTest.kt":

```kotlin
// Mock Utils中的静态方法
mockkStatic(Utils::class)
// 定义行为
every { Utils.getURL() } returns "http://example.com/"
// 调用Mock方法
println("Utils#getURL:[${Utils.getURL()}]")
```




<!-- TODO
# 行为定义

every{...} 语句 没有什么好解释的，它就是 Mockito 中的when，用来监听指定的代码语句，并做出接下来的动作，例如：

    return value 返回某个值
    just Runs 继续执行（仅用于 Unit 方法）
    answer {} 执行某个语句块

因为Kotlin中有 协程 这个特性（本质上是线程），所以单元测试在执行时可能会遇到执行协程中代码的问题，这个时候如果需要监听，则需要使用 coEvery{ ... }
当然了除了 coEvery{...} ， 还有 coVerify{...}、 coRun、 coAssert 、 coAnswer、coInvoke 等用于协程中的方法，后面就不再赘述了。



# 行为验证

测试框架可以验证有直接返回值的方法，但是对于没有返回值的 void 方法应该如何测试呢？void 方法的输出结果其实是调用了另外一个方法，所以需要验证该方法是否有被调用，调用时参数是否正确。


verify 是用来检查方法是否触发，当然它也很强大，它有许多参数可选，来看看这些参数：

fun verify(
    ordering: Ordering = Ordering.UNORDERED,
    inverse: Boolean = false,
    atLeast: Int = 1,
    atMost: Int = Int.MAX_VALUE,
    exactly: Int = -1,
    timeout: Long = 0,
    verifyBlock: MockKVerificationScope.() -> Unit
){}

他们作用如下：

    ordering： 表示verify{ .. } 中的内容（下面简称语句块）是按照顺序执行。 默认是无序的
    inverse：如果为true，表示语句块中的内容不发生（即方法不执行）
    atLeast：语句块中方法最少执行次数
    atMost：语句块中方法最多执行次数
    exactly：语句块中的方法具体执行次数
    timeout：语句块内容执行时间，如果超过该事件，则测试会失败
    verifyBlock： Lambda表达式，语句块本身

除了这些，还有别的 verify 语句，方便你使用：

    verifySequence{...}：验证代码按顺序执行，而且要每一行的代码都要在语句块中指定出来。
    verifyAll{...}：验证代码全部都执行，没有顺序的规定
    verifyOrder{...}：验证代码按顺序执行


verify不会重置调用记录，因此如果需要复用mock对象，次数需要加1，或者重置mock对象


# 参数匹配器

-->


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



<!-- TODO

如果你要验证、执行 object类里面的私有方法，你需要在mock的时候指定一个值 recordPrivateCalls， 它默认是false：

mockkObject(ObjectClass, recordPrivateCalls = true)


enum 类也是一样的mock方式




验证mock对象私有方法

验证是放在 verify{...} 中的，也是通过反射的方式来验证:

verify{ mockClass["privateFunName"](arg1, arg2, ...) }

主要分成三个部分：

    mock类
    中括号，里面填入双引号+私有方法名
    小括号，里面填入传参，可以使用 allAny<T>()、mockk() … 或你想要的传入的实参




    偏函数模拟

 

every { 
    mockObject.someMethod(any()) 
} answers { 
    originalCall(it.invocation.args.first()) 
}

备注：对于某些方法调用，我们并不想完全使用模拟的值，而是想使用特定的函数调用过程，那么可以使用originalCall来实现对实际函数的调用。



    构造函数

 

mockkConstructor(MyClass::class)
every { 
    anyConstructed<MyClass>().someMethod() 
} returns "Mocked Result"// 执行测试代码
unmockkConstructor(MyClass::class)

备注：使用mockkConstructor方法mock构造函数，并通过anyConstructed进行类的构造，最后通过 unmockkConstructor取消构造函数的mock。

    Lambada表达式

 

val lambdaMock: () -> Unit = mockk()
every { 
    lambdaMock.invoke() 
} just Runs


模拟私有函数

在罕见情况下，可能需要模拟私有函数。这个过程较为复杂，因为不能直接调用此类函数。

    val mock = spyk(ExampleClass(), recordPrivateCalls = true)
    every { mock["sum"](any<Int>(), 5) } returns 25

或使用:

every { mock invoke "openDoor" withArguments listOf("left", "rear") } returns "OK"

模拟属性

通常可以像模拟 get/set 函数或字段访问一样模拟属性。对于更多场景，可以使用其他方法。

    every { mock getProperty "speed" } returns 33
    every { mock setProperty "acceleration" value less(5) } just Runs
    verify { mock getProperty "speed" }
    verify { mock setProperty "acceleration" value less(5) }


部分模拟：

在Java环境中，Mock工具（如Mockito）的SPY模式主要用于部分模拟（Partial Mocking）的场景。SPY模式允许你在保留对象原有行为的同时，模拟其中的部分方法。这在需要测试复杂对象的部分功能时非常有用。
使用场景
测试部分功能：当你只想测试对象的某些方法，而不希望影响其他方法的真实行为时。
依赖复杂逻辑的对象：当对象的某些方法依赖复杂的逻辑或外部资源（如数据库、网络）时，可以通过SPY模式模拟这些方法。
验证方法调用：当你需要验证某些方法是否被调用，以及调用的参数是否正确时。


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class Calculator {
    public int add(int a, int b) {
        return a + b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }
}

public class SpyExampleTest {

    @Test
    public void testSpy() {
        // 创建一个真实对象
        Calculator realCalculator = new Calculator();

        // 创建一个SPY对象
        Calculator spyCalculator = Mockito.spy(realCalculator);

        // 模拟add方法的行为
        doReturn(10).when(spyCalculator).add(2, 3);

        // 调用add方法，返回模拟值
        System.out.println(spyCalculator.add(2, 3)); // 输出：10

        // 调用multiply方法，返回真实值
        System.out.println(spyCalculator.multiply(2, 3)); // 输出：6

        // 验证add方法是否被调用
        verify(spyCalculator).add(2, 3);
    }
}


-->
