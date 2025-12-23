# 简介
Mockito是针对Java语言设计的Mock工具，即使它提供了Kotlin扩展，其对Kotlin的支持仍然有限，例如：无法模拟Kotlin中的Object、对函数式编程和协程支持欠佳等。

MockK是专为Kotlin语言（JVM平台）所设计的Mock工具，它的用法与Mockito类似，并且支持Kotlin的专有特性，其官方网站为： [🔗 MockK](https://mockk.io/) 。

本章的示例工程详见以下链接：

- [🔗 示例工程：MockK](https://github.com/BI4VMR/Study-Kotlin/tree/master/M04_Utils/C01_Test/S03_MockK)


# 基本应用
## 隔离依赖
Mock工具的基本用途是模拟被测组件所依赖的外部接口，提供假的数据与行为，使我们专注于被测组件本身的业务逻辑，不必关心外围环境的变化。

🔴 示例一：模拟待测组件的依赖项。

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

此处以用户管理功能为例，我们编写UserManager类提供用户信息查询接口，其依赖于数据库工具类DBHelper提供的接口。

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

我们的目标是测试UserManager功能是否正常，并不关心DBHelper连接何种数据库、用户信息表具有何种结构，因此我们需要创建DBHelper类的Mock对象，给UserManager提供模拟的用户信息。

"UserManagerTest.kt":

```kotlin
@Test
fun test_GetUserNames() {
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

接着，我们创建待测接口所在类UserManager的实例，并通过反射将Mock对象注入到UserManager中，扩展方法 `injectMock()` 的具体实现详见本章示例工程。

此时运行示例程序，并查看控制台输出信息：

```text
Index:[0] Name:[来宾账户]
Index:[1] Name:[用户A]
Index:[2] Name:[用户B]
```

根据上述输出内容可知：

UserManager调用DBHelper中的接口后，输出内容确实为测试代码提供的模拟数据，并非DBHelper的内置数据，这表明Mock操作成功，测试代码已经隔离了UserManager的外部依赖DBHelper。

## 宽松模式
若待测对象调用了Mock对象中的某个方法，但我们没有预先为此Mock方法定义行为，MockK不知道应当执行何种动作，就会抛出异常提醒开发者。

Mock对象中的部分方法执行与否对测试逻辑没有负面影响，例如：日志记录，我们可以使用 `every { <日志记录方法> } just runs` 语句定义它们的行为：“什么都不做”，避免运行时出现异常；有时此类方法数量较多，手动定义会产生大量重复代码，更好的方案是使用宽松模式创建Mock对象。

宽松模式的Mock对象会为每个方法添加默认行为：“什么都不做”，避免运行时异常；对于有返回值的方法，返回值如下文列表所示：

- 数值型：返回 `0` 。
- 布尔型：返回 `false` 。
- 可空引用类型：返回 `null` 。
- 非空引用类型：返回该类型的宽松模式Mock对象。
- 集合类型：返回内容为空的集合。

下文示例展示了宽松模式的具体用法。

🟠 示例二：使用宽松模式创建Mock对象。

在本示例中，我们使用宽松模式创建Mock对象，并调用未手动定义行为的Mock方法。

第一步，我们对前文“示例一”的业务代码进行修改。

我们在DBHelper类中新增日志记录方法 `saveLog()` ，然后在UserManager中调用该方法。

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

此时运行UserManagerTest中的测试用例，控制台将会输出以下错误信息：

```text
io.mockk.MockKException: no answer found for DBHelper(#1).saveLog(GetUserNames) among the configured answers: (DBHelper(#1).queryUsers())
```

这是因为我们只定义了DBHelper中 `queryUsers()` 方法的行为，没有定义 `saveLog()` 方法的行为。

第二步，我们使用宽松模式生成DBHelper的Mock对象。

由于 `saveLog()` 方法的行为对测试结果没有任何影响，我们可以使用宽松模式创建DBHelper的Mock对象，为该方法定义“什么都不做”的默认行为。

"UserManagerTest.kt":

```kotlin
@Test
fun test_GetUserNames2() {
    val mockDatas: Map<Long, String> = mapOf(1L to "来宾账户", 2L to "用户A", 3L to "用户B")

    // 创建DBHelper的Mock对象（使用 `relaxed = true` 为没有明确定义行为的类添加默认行为）
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

此时运行新的测试用例 `test_GetUserNames2` ，不会再出现 `no answer found for DBHelper(#1).saveLog(GetUserNames)` 错误，说明该方法已被宽松模式配置的默认行为。

---

有时宽松模式的默认返回值会误导我们编写不恰当的测试代码，为了解决此类问题， `mockk()` 方法还提供了一个 `relaxUnitFun` 参数，该模式只为Mock对象的无返回值方法定义默认行为，而有返回值方法则保持“若未定义行为则抛出异常”。我们可以根据实际情况选择 `relaxed` 或 `relaxUnitFun` 两种模式。

## 常用注解
在前文示例中，我们使用 `mockk()` 方法创建了一些局部Mock对象，它们仅能在单一测试用例内部被访问；有时我们希望在JUnit的 `setUp()` 方法中统一配置Mock对象及行为，并在多个测试用例中共享Mock对象，此时可以使用MockK提供的注解。

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
fun setUp() {
    // 若要使用MockK注解，需要在执行其他操作前先初始化。
    MockKAnnotations.init(this)
}

@Test
fun test_GetUserNames(){
    // 在此处使用Mock对象...
    every { mockDBHelper1.queryUsers() } returns mapOf(1L to "来宾账户", 2L to "用户A")
}
```

`@MockK` 注解表示被修饰的全局变量是Mock对象，程序运行时MockK会根据变量类型自动创建对象并完成赋值。该注解拥有两个属性，其中 `relaxed` 表示是否启用宽松模式； `relaxUnitFun` 表示是否启用仅针对无返回值方法的宽松模式。`@RelaxedMockK` 注解等同于 `@MockK(relaxed = true)` 。

MockK的注解默认不会生效，为了使注解生效并创建Mock对象，我们需要在每个测试用例执行前调用 `MockKAnnotations.init(this)` 方法。


# 定义行为
## 基本应用
为了观察被测实例在不同环境中的行为是否符合预期，我们可以通过 `every {}` 语句定义其依赖的Mock对象被访问时的动作。

在前文示例中，我们已经初步应用了指定方法返回值的 `returns <返回值>` 语句，下文列表展示了 `every {}` 语句后可填写的所有后继语句：

- `just runs` : 该方法被调用时，“什么都不做”，仅适用于无返回值的方法。
- `returns <返回值>` : 该方法被调用时，始终返回固定的值。
- `returnsMany List<返回值>` : 该方法被多次调用时，依次返回列表中的元素。
- `answers {<语句块>}` : 该方法被调用时，执行语句块。若该方法有返回值，则将语句块的最后一行内容作为返回值。
- `throws <异常对象>` : 该方法被调用时，抛出指定的异常。
- `throwsMany List<异常对象>` : 该方法被多次调用时，依次抛出列表中的异常。

下文示例展示了行为定义语句的具体用法。

🟢 示例四：模拟固定返回值。

在本示例中，我们为Mock对象定义行为，每当指定方法被调用时，返回测试用例中指定的值。

"DefineBehaviorTest.kt":

```kotlin
// 创建Mock对象
val mockFile = mockk<File>()
// 定义行为：当 `mockFile` 的 `getCanonicalPath()` 方法被访问时，返回 `/data/file1` 。
every { mockFile.canonicalPath } returns "/data/file1"

// 调用Mock对象的 `getCanonicalPath()` 方法并输出结果
println("File Path:[${mockFile.canonicalPath}]")
// 再次调用Mock对象的 `getCanonicalPath()` 方法并输出结果
println("File Path:[${mockFile.canonicalPath}]\n")


// 修改行为：当 `mockFile` 的 `getCanonicalPath()` 方法被访问时，返回 `/data/file2` 。
every { mockFile.canonicalPath } returns "/data/file2"

// 调用Mock对象的 `getCanonicalPath()` 方法并输出结果
println("File Path:[${mockFile.canonicalPath}]")
```

此时运行示例程序，并查看控制台输出信息：

```text
File Path:[/data/file1]
File Path:[/data/file1]

File Path:[/data/file2]
```

根据上述输出内容可知：

当我们定义Mock对象的 `getCanonicalPath()` 方法返回 `/data/file1` 后，调用者始终能够接收到该字符串；随后我们重新定义该方法的返回值为 `/data/file2` ，调用者则会接收到新字符串。

我们可以在前一个条件的测试完成之后，利用现有环境重新定义Mock对象的行为，继续测试后一个条件，以免为每个条件都创建单独的测试方法，产生过多的冗余代码。

---

🔵 示例五：模拟序列返回值。

在本示例中，我们为Mock对象定义行为，每当指定方法被调用时，依次返回不同的值。

"DefineBehaviorTest.kt":

```kotlin
// 创建Mock对象
val mockFile = mockk<File>()
// 设置每次调用时返回的值序列
val mockResult = listOf(100L, 200L, 1024L)
// 定义行为：当 `mockFile` 的 `length()` 方法被访问时，依次返回 `mockResult` 列表中的值。
every { mockFile.length() } returnsMany mockResult

// 多次访问Mock对象的属性并输出结果
for (i in 1..5) {
    println("第 $i 次调用： Length:[${mockFile.length()}]")
}
```

此时运行示例程序，并查看控制台输出信息：

```text
第 1 次调用： Length:[100]
第 2 次调用： Length:[200]
第 3 次调用： Length:[1024]
第 4 次调用： Length:[1024]
第 5 次调用： Length:[1024]
```

根据上述输出内容可知：

第1至3次调用的返回值与 `mockResult` 列表中的元素一一对应，而超过列表长度的调用则会返回列表中的最后一个元素。

---

🟣 示例六：自定义行为。

在本示例中，我们为Mock对象定义行为，每当指定方法被调用时，输出控制台消息。

"DefineBehaviorTest.kt":

```kotlin
// 创建Mock对象
val mockFile = mockk<File>()
// 定义行为：当 `mockFile` 的 `getCanonicalPath()` 方法被访问时，返回 `/data/file1` 。
every { mockFile.canonicalPath } answers {
    // 输出消息
    println("$mockFile `canonicalPath()` was called.")

    // 此时 `answers {}` 块的最后一行将作为返回值
    "/data/file1"
}

// 调用Mock对象的 `getCanonicalPath()` 方法并输出结果
println("File Path:[${mockFile.canonicalPath}]")
```

此时运行示例程序，并查看控制台输出信息：

```text
File(#2) `canonicalPath()` was called.
File Path:[/data/file1]
```

根据上述输出内容可知：

当Mock对象的 `getCanonicalPath()` 方法被调用时，`answers {}` 块中的语句被执行了，首先输出控制台消息，然后将表达式最后一行的内容作为返回值传递给调用者。

---

🟤 示例七：模拟异常。

在本示例中，我们为Mock对象定义行为，每当指定方法被调用时，抛出测试用例指定的异常。

"DefineBehaviorTest.kt":

```kotlin
@Test(expected = IOException::class)
fun test_define_exception() {
    // 创建Mock对象
    val mockFile = mockk<File>()
    // 定义行为：当 `mockFile` 的 `getCanonicalPath()` 方法被访问时，抛出异常。
    every { mockFile.canonicalPath } throws IOException("This is a mock exception!")

    // 调用Mock对象的 `getCanonicalPath()` 方法并输出结果
    println("File Path:[${mockFile.canonicalPath}]")
}
```

当Mock对象的 `getCanonicalPath()` 方法被调用时，其会抛出IOException异常，此处我们通过JUnit捕获了异常，因此测试用例不会执行失败。

## 参数匹配器
有参方法的行为与调用者传入的参数密切相关，因此我们可以使用参数匹配器定义Mock对象的行为，以便模拟较为复杂的环境。

下文列表展示了常用的参数匹配器：

- `any()` : 匹配任意参数值。
- `isNull()` : 匹配传入参数为空值的调用。
- `<参数值>` : 精确匹配调用者传入参数与指定参数值一致的调用。
- `eq(<参数值>)` : 精确匹配调用者传入参数与指定参数值一致的调用。
- `refEq(<参数值>)` : 精确匹配调用者传入实例与指定实例内存地址相同的调用。
- `neq(<参数值>)` : 匹配所有传入参数与指定参数值不一致的调用。
- `nrefEq(<参数值>)` : 匹配所有传入实例与指定实例内存地址不同的调用。
- `match {<语句块>}` : 自定义匹配规则。

下文示例展示了参数匹配器的具体用法。

🔴 示例八：参数匹配器。

在本示例中，我们使用参数匹配器定义Mock方法接收到不同参数时的行为。

第一步，我们定义DBHelper类，并声明一些方法以便进行Mock测试。

"DefineBehaviorTest.kt":

```kotlin
class DBHelper {

    // 根据用户ID查询姓名
    fun queryUserName(id: Int): String = "Real Name"

    // 根据身份证号查询姓名
    fun queryUserName(cardID: String): String = "Real Name"

    // 查询所有年龄和性别符合要求的用户ID
    fun queryUserNames(age: Int, male: Boolean): List<String> = listOf("Real Name")
}
```

第二步，我们使用参数匹配器定义 `queryUserName(id: Int)` 方法在不同参数下的行为。

"DefineBehaviorTest.kt":

```kotlin
val mockDBHelper = mockk<DBHelper>()
// 定义不同条件下 `queryUserName()` 方法的返回值
every { mockDBHelper.queryUserName(any<Int>()) } returns "MockUser"
every { mockDBHelper.queryUserName(1) } returns "Alice"
every { mockDBHelper.queryUserName(2) } returns "Bob"

// 查看返回值
println("QueryUserName of ID=1:[${mockDBHelper.queryUserName(1)}]")
println("QueryUserName of ID=2:[${mockDBHelper.queryUserName(2)}]")
println("QueryUserName of ID=3:[${mockDBHelper.queryUserName(3)}]")
```

此时运行示例程序，并查看控制台输出信息：

```text
QueryUserName of ID=1:[Alice]
QueryUserName of ID=2:[Bob]
QueryUserName of ID=3:[MockUser]
```

根据上述输出内容可知：

我们可以为Mock对象的同一方法注册多个行为定义语句，只要匹配器不冲突即可共存。

当Mock方法被调用时，较晚设置的条件最先被匹配，因此在定义行为阶段，我们应当先设置匹配范围广泛的行为，再设置匹配范围精确的行为。

---

在DBHelper类中存在 `queryUserName(id: Int)` 方法和 `queryUserName(cardID: String)` 方法，若我们定义方法的行为时直接在 `every {}` 中填入 `queryUserName(any())` ，就会产生歧义，此时MockK无法确定我们想要定义哪个方法的行为。

`any()` 匹配器的完整形式为： `any<T>(classifier: KClass<T>)` ，我们可以通过泛型 `<T>` 或 `classifier` 参数指明匹配器的参数类型，解决重载方法的匹配问题。

🟠 示例九：匹配重载方法。

在本示例中，我们使用参数匹配器定义不同重载方法的行为。

"DefineBehaviorTest.kt":

```kotlin
// 匹配参数为Int类型的方法
every { mockDBHelper.queryUserName(any<Int>()) } returns "MockUserA"
// 匹配参数为Int类型的方法（等价写法）
every { mockDBHelper.queryUserName(any(Int::class)) } returns "MockUserA"

// 匹配参数为String类型的方法
every { mockDBHelper.queryUserName(any<String>()) } returns "MockUserB"
// 匹配参数为String类型的方法（等价写法）
every { mockDBHelper.queryUserName(any(String::class)) } returns "MockUserB"
```

---

`<参数值>` 与匹配器 `eq(<参数值>)` 都表示精确匹配参数值，但它们的适用场景有一些区别，我们通过下文示例进行说明。

🟡 示例十：具体值与 `eq()` 匹配器的区别。

在本示例中，我们演示参数具体值与 `eq()` 匹配器的区别。

"DefineBehaviorTest.kt":

```kotlin
val mockDBHelper = mockk<DBHelper>()

// 错误用法：字面量与匹配器混用
every { mockDBHelper.queryUserNames(20, any()) } returns listOf()

// 正确用法：全部使用匹配器
every { mockDBHelper.queryUserNames(eq(20), any()) } returns listOf()
// 正确用法：全部使用字面量
every { mockDBHelper.queryUserNames(20, false) } returns listOf()
```

在同一条行为定义语句中，要么全部使用具体值，要么全部使用匹配器。我们不应将二者混用，虽然有时混用不会导致错误，但这属于未定义行为，应当尽量避免。


# 验证行为
对于有返回值的方法，我们可以通过测试框架提供的断言比较结果是否与预期相符，对于无返回值的方法，我们就需要通过Mock框架进行验证。Mock对象会记录所有方法是否被调用过、被调用时的参数等信息。


verify 是用来检查方法是否触发，当然它也很强大，它有许多参数可选，来看看这些参数：


- `verifyBlock` : 待验证的语句，是最后一个lambda参数可以写在括号外，与 `every {}` 语句类似，可以使用参数匹配器，并且一个块可以填写多个语句。
- `atLeast` : 语句块中方法最少执行次数，默认1次。
- `atMost` : 语句块中方法最多执行次数，默认为Int类型最大值。
- `exactly` : 精确次数，默认值 `-1` 表示不启用该功能，任意正整数将会覆盖`atLeast`和`atMost`参数的行为，例如设为1表示该方法必须被调用1次，未调用和调用次数大于1都会导致验证失败。
- `inverse` : 反转条件，验证方法是否不满足预设的条件。
- `timeout` : 语句块内容执行时间，如果超过该事件，则测试会失败
- `ordering` : 如果 `verifyBlock` 表达式存在多个方法，是否要求它们按照编写顺序被调用。



verify不会重置调用记录，因此如果需要复用mock对象，次数需要加1，或者重置mock对象





默认值 `Ordering.UNORDERED` 表示不限制，且任意方法被调用认为通过， `ALL` 不限制顺序，但Mock实际被调用的方法必须与verify匹配，缺少或多都会报错。 `ORDERED` 方法必须按编写顺序被调用，但不要求所有方法均被调用。 `SEQUENCE` 所有方法必须按顺序被调用。


该属性有一些对应的快捷方式

`verifyOrder {}` : 等同于 `verify (ordering = Ordering.ORDERED) {}` 。
`verifyAll {}` : 等同于 `verify (ordering = Ordering.ALL) {}` 。
`verifySequence {}` : 等同于 `verify (ordering = Ordering.SEQUENCE) {}` 。



# Kotlin相关
MockK提供了针对Object、JVM静态方法等元素的Mock工具，以便我们在Kotlin环境中便捷地编写测试代码。

🟢 示例X：模拟Object中的普通方法。

在本示例中，我们模拟Object中的非静态方法。

第一步，我们编写业务代码。

"UtilsObject.kt":

```kotlin
object UtilsObject {
    fun getCurrentTime(): Long = System.currentTimeMillis()
}
```

第二步，我们编写测试代码。

"UtilsObjectTest.kt":

```kotlin
// 为UtilsObject中的普通方法启用Mock
mockkObject(UtilsObject)
// 定义行为
every { UtilsObject.getCurrentTime() } returns 1234567890L

// 调用Mock方法
println("UtilsObject#getCurrentTime:[${Utils.getCurrentTime()}]")

// 撤销指定Object的Mock设置（可选）
unmockkObject(UtilsObject)
// 撤销所有Object、Static、构造方法的Mock设置（可选）
unmockkAll()
```

我们首先调用 `mockkObject(vararg objects: Any)` 方法并传入目标Object，为它们启用Mock功能，随后即可通过 `every {}` 语句定义方法的行为。

当Object的Mock行为使用完毕后，我们可以调用 `unmockkObject(vararg objects: Any)` 方法撤销目标Object的行为定义。如果我们为多个Object配置了Mock行为，也可以直接调用 `unmockkAll()` 方法进行撤销，无需指定目标。

`unmockkAll()` 方法对Object、静态方法、构造方法等全局Mock配置均有效，因此我们通常会在JUnit的 `tearDown()` 方法中统一调用它，防止当前测试用例设置的Mock行为干扰后续其他测试用例。

此时运行示例程序，并查看控制台输出信息：

```text
UtilsObject#getCurrentTime:[1234567890]
```

---

🔵 示例X：模拟Object中的静态方法。

在本示例中，我们模拟Object中的JVM静态方法。

第一步，我们编写业务代码。

"UtilsObject.kt":

```kotlin
object UtilsObject {
    @JvmStatic
    fun getURL(): String = "http://192.168.1.1/"
}
```

第二步，我们编写测试代码。

"UtilsObjectTest.kt":

```kotlin
// 为UtilsObject中的静态方法启用Mock
mockkStatic(UtilsObject::class)

// 定义行为
every { UtilsObject.getURL() } returns "http://test.com/"

// 调用Mock方法
println("UtilsObject#getURL:[${UtilsObject.getURL()}]")

// 撤销指定Object的Mock设置（可选）
unmockkStatic(UtilsObject::class)
```

我们首先调用 `mockkStatic(vararg classes: KClass<*>)` 方法并传入目标Object的KClass，为它们启用Mock功能，随后即可通过 `every {}` 语句定义JVM静态方法的行为。

当静态方法的Mock行为使用完毕后，我们可以调用 `unmockkStatic(vararg classes: KClass<*>)` 方法撤销目标Object的行为定义。

此时运行示例程序，并查看控制台输出信息：

```text
UtilsObject#getURL:[http://test.com/]
```

> 🚩 提示
>
> 对于使用Java语言编写的非Final静态方法，我们也可以使用上述方式进行Mock。

---

🔵 示例X：模拟伴生对象中的方法。

在本示例中，我们模拟类的伴生对象中的方法。

第一步，我们编写业务代码。

"UtilsClass.kt":

```kotlin
class UtilsClass {

    companion object {

        fun method(): String {
            return "Method in companion object."
        }

        @JvmStatic
        fun methodStatic(): String {
            return "Static method in companion object."
        }
    }
}
```

第二步，我们编写测试代码。

"UtilsClassTest.kt":

```kotlin
// 为UtilsClass伴生对象中的方法启用Mock
mockkObject(UtilsClass)

// 使用该语句也能启用伴生对象的Mock
// mockkObject(UtilsClass.Companion)

every { UtilsClass.method() } returns "Test method."
every { UtilsClass.methodStatic() } returns "Test static method."

println("UtilsClass#method:[${Utils2.method()}]")
println("UtilsClass#methodStatic:[${Utils2.methodStatic()}]")
```

对于类的伴生对象，我们可以直接调用 `mockkObject(<类名>.Companion)` 方法启用Mock；除此之外，我们也可以只传入类名，省略Companion部分，这是因为 `mockkObject()` 方法能够自动识别类的伴生对象并为其启用Mock。

由于伴生对象中 `@JvmStatic` 注解只是生成了新的静态方法，并指向Companion内原有的非静态方法，而 `mockkObject()` 已经启用了Companion的Mock功能，因此我们无需调用 `mockkStatic()` 也能为此处的JVM静态方法启用Mock。

此时运行示例程序，并查看控制台输出信息：

```text
UtilsClass#method:[Test method.]
UtilsClass#methodStatic:[Test static method.]
```


# 捕获参数

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

# 部分模拟



# 捕获Lambda


```kotlin
        // 捕获待测实例注册的广播接收器
        val receiverSlot = slot<(Intent) -> Unit>()
        every { mockContext.registerReceiver(any(), capture(receiverSlot), captureLambda()) } answers {
            // 此处将在registerReceiver被调用时立即执行lambda
            lambda<IntentFilter.() -> Unit>().invoke(IntentFilter())
        }

        // 延后回调Lambda
        receiverSlot.captured.invoke(intentLowSpeed)
```



## 私有方法
every { mockClass["privateFunName"](arg1, arg2, ...) }


# 验证私有方法

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
