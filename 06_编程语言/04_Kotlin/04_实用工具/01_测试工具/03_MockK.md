# 简介
Mockito是针对Java语言设计的Mock工具，即使它提供了Kotlin扩展，其对Kotlin的支持仍然有限，例如：无法模拟Kotlin中的Object、对函数式编程和协程支持欠佳等。

MockK是专为JVM平台Kotlin语言所设计的Mock工具，它的用法与Mockito类似，并且支持Kotlin的专有特性，其官方网站为： [🔗 MockK](https://mockk.io/) 。

本章的示例工程详见以下链接：

- [🔗 示例工程：MockK](https://github.com/BI4VMR/Study-Kotlin/tree/master/M04_Utils/C01_Test/S03_MockK)


# 基本应用
## 隔离依赖
Mock工具的基本用途是模拟被测组件所依赖的外部接口，提供测试数据与行为，使我们专注于被测组件本身的业务逻辑，不必关心外围环境的变化。

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

UserManager调用DBHelper中的接口后，输出内容确实为测试代码提供的模拟数据，并非DBHelper的内置数据，表明Mock操作成功，测试代码已经隔离了UserManager的外部依赖DBHelper。

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

- `<参数值>` : 精确匹配调用者传入参数与指定参数值一致的调用。
- `eq(<参数值>)` : 精确匹配调用者传入参数与指定参数值一致的调用。
- `any()` : 匹配任意参数值。
- `isNull(<是否取反>)` : 匹配传入参数为空值的调用。默认不取反，如果取反则表示匹配“传入参数非空值”的调用。
- `refEq(<参数值>)` : 精确匹配调用者传入实例与指定实例内存地址相同的调用。
- `neq(<参数值>)` : 匹配所有传入参数与指定参数值不一致的调用。
- `nrefEq(<参数值>)` : 匹配所有传入实例与指定实例内存地址不同的调用。
- `more(<参考值>, <是否包含边界值>)` : 匹配传入参数大于参考值的调用（基于Comparable接口）。第二参数用于指明是否包括参数与参考值相等的情况，默认为 `false` 。
- `less(<参考值>, <是否包含边界值>)` : 匹配传入参数小于参考值的调用（基于Comparable接口）。
- `range(<起始值>, <终止值>, <是否包含起始值>, <是否包含终止值>)` : 匹配传入参数在指定范围内的调用。默认包含两侧的边界值。
- `ofType(<KClass>)` : 匹配传入参数类型与指定类型一致的调用。
- `and(<匹配器1>, <匹配器2>)` : 匹配符合两条规则的调用。用于组合其他匹配器，例如： `and(isNull(true), ofType(String::class))` 表示匹配“传入参数非空且为字符串”的调用。
- `or(<匹配器1>, <匹配器2>)` : 匹配符合任意规则的调用。
- `not(<匹配器>)` : 匹配不符合该规则的调用。
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

🟡 示例十：具体值与 `eq()` 匹配器。

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

## 偏函数模拟
默认情况下，Mock对象的所有方法实现均为模拟行为，要么执行我们通过 `every {}` 语句块定义的行为，要么返回宽松模式的默认值。有时我们希望执行原始方法的逻辑，或者获取原始方法的返回值并进行处理，这种需求被称为“偏函数模拟”。

在 `answers {}` 语句块中，我们可以调用 `callOriginal()` 方法，此方法将会调用 `every {}` 语句中的原始方法；若原始方法具有返回值，也会通过此方法返回，以便我们实现偏函数模拟。

🟢 示例十一：偏函数模拟。

在本示例中，我们调用Mock对象的原始方法，对返回值进行处理后再传递给调用者。

"DefineBehaviorTest.kt":

```kotlin
val mockDBHelper = mockk<DBHelper>()
every { mockDBHelper.queryUserNames(any(), any()) } answers {
    // 调用原始方法
    val rawList = callOriginal()
    println("真实调用的返回值：$rawList")

    // 追加一些模拟数据再作为新的返回值
    ArrayList(rawList + "MockUser1" + "MockUser2")
}

// 调用Mock对象的 `queryUserNames()` 方法并输出结果
val result = mockDBHelper.queryUserNames(22, false)
println("Mock方法的返回值：$result")
```

此时运行示例程序，并查看控制台输出信息：

```text
真实调用的返回值：[Real Name]
Mock方法的返回值：[Real Name, MockUser1, MockUser2]
```


# 验证行为
## 基本应用
对于被测实例的有返回值方法，我们可以通过断言比较返回值与预期值是否相符，从而验证方法行为是否符合预期；对于无返回值方法，我们则需要通过Mock框架进行验证。

无返回值方法通常会调用依赖组件的方法以实现特定功能，在单元测试中，我们将Mock对象作为依赖组件，而Mock对象能够记录其方法是否被调用、调用参数以及调用次数等信息，因此我们可以通过检查Mock对象信息，间接验证被测实例的行为是否符合预期。

MockK提供的行为验证方法是 `verify()` ，它的参数及含义如下文列表所示：

- `atLeast = <次数>` : 指定目标方法被调用的最少次数，默认为一次。
- `atMost = <次数>` : 指定目标方法被调用的最多次数，默认为Int的最大值。
- `exactly = <次数>` : 指定目标方法被调用的次数，默认为 `-1` ，表示不启用该功能。如果该值为任意正整数，表示启用该功能，将会覆盖 `atLeast` 和 `atMost` 参数配置，例如：数值 `1` 表示目标方法必须被调用一次，未被调用或被调用次数大于 `1` 都会导致验证失败。
- `inverse` : 反转其他条件，将不满足其他条件的场景视为验证成功。例如： `verify(inverse = true) {}` 表示“目标方法未被调用至少一次”，即目标方法从未被调用。
- `timeout = <毫秒>` : 指定目标方法最大允许执行的时长。
- `ordering` : 如果 `verifyBlock` 表达式具有多个方法，是否要求它们按照编写顺序被调用。
- `verifyBlock` : 待验证的方法调用，与 `every {}` 语句类似，此处可以使用参数匹配器，并且可以包含多个语句。

下文示例展示了 `verify()` 方法的具体用法。

🔵 示例十二：基本的行为验证。

在本示例中，我们使用 `verify {}` 方法验证被测接口是否正确地调用了依赖组件。

第一步，我们编写业务代码。

LogManager利用Logger实现日志输出功能，其中 `printInfo()` 方法能够将一组消息分行输出。

"LogManager.kt":

```kotlin
class LogManager {

    // 外部依赖
    var logger: Logger = Logger.getAnonymousLogger()

    // 业务方法：将一组消息分行输出
    fun printInfo(messages: List<String>) {
        messages.forEach { message ->
            logger.log(Level.INFO, message)
        }
    }
}
```

第二步，我们编写测试代码。

"LogManagerTest.kt":

```kotlin
// 创建Logger的Mock对象
val mockLogger: Logger = mockk(relaxed = true)

// 创建待测类的实例并注入Mock对象
val manager = LogManager()
manager.logger = mockLogger
// 执行业务方法
val text: List<String> = listOf("LineA", "LineB")
manager.printInfo(text)

// 验证行为：Logger的记录方法应当被调用2次
verify(exactly = 2) {
    mockLogger.log(eq(Level.INFO), any<String>())
}
```

此处我们将长度为2的List传递给 `printInfo()` 方法，预期输出两行日志，因此Logger的 `log()` 方法应当被调用两次， `verify()` 方法的参数为 `exactly = 2` 。

随后，我们在同一个测试用例中再次调用 `printInfo()` 方法，传入一个长度为3的List。

"LogManagerTest.kt":

```kotlin
// 再次执行业务方法
val text2: List<String> = listOf("Line1", "Line2", "Line3")
manager.printInfo(text2)

// 验证行为：Logger的记录方法应当被调用2次 + 3次
verify(exactly = 2 + text2.size) {
    mockLogger.log(eq(Level.INFO), any<String>())
}
```

此时预期的调用次数应当为5，即首次打印的2次及第二次打印的3次之和；这是因为我们使用了同一个Mock对象， `verify()` 方法不会重置调用记录，其中的调用次数会不断累加。

## 验证调用顺序
`verify()` 方法的 `ordering` 参数用于验证多个方法被调用的顺序，可选的值如下文列表所示：

- `UNORDERED` : 默认值，不限制调用顺序，只要 `verify()` 语句指明的方法均被调用即验证通过。
- `ALL` : 不限制调用顺序，但要求Mock对象的调用记录与 `verify()` 语句匹配，若调用记录中存在 `verify()` 语句未指明的方法，则视为验证失败。例如： `verify()` 语句要求调用“方法A和方法B”，而调用记录为“方法A、方法C、方法B”，此时将会验证失败，因为调用记录中包含了 `verify()` 语句未指明的“方法C”。
- `ORDERED` : 限制调用顺序，但不验证调用记录中是否包含 `verify()` 语句未指明的方法。
- `SEQUENCE` : 限制调用顺序，且要求Mock对象的调用记录与 `verify()` 语句匹配。

该属性有一些对应的快捷方法可供选用：

- `verifyOrder {}` : 等同于 `verify (ordering = Ordering.ORDERED) {}` 。
- `verifyAll {}` : 等同于 `verify (ordering = Ordering.ALL) {}` 。
- `verifySequence {}` : 等同于 `verify (ordering = Ordering.SEQUENCE) {}` 。

下文示例以 `verifySequence {}` 方法为例，展示验证调用顺序的具体方法。

🟣 示例十三：验证一组方法的调用顺序。

在本示例中，我们使用 `verifySequence {}` 方法验证被测接口是否正确地调用了依赖组件。

第一步，我们编写业务代码。

"LogManager.kt":

```kotlin
class LogManager {

    // 业务方法：导出日志
    fun saveLog(callback: StateCallback) {
        // 通知外部监听者操作开始
        callback.onStart()

        // 模拟耗时操作
        Thread.sleep(200L)

        // 通知外部监听者操作结束
        callback.onEnd(200L)
    }

    // 事件监听器
    interface StateCallback {

        // 操作开始
        fun onStart()

        // 操作完成：通知消耗时长
        fun onEnd(time: Long)
    }
}
```

第二步，我们编写测试代码。

"LogManagerTest.kt":

```kotlin
// 创建监听器的Mock对象
val mockListener: LogManager.StateCallback = mockk(relaxed = true)

// 执行业务方法并传入监听器
LogManager().saveLog(mockListener)

// 验证行为：日志导出后，监听器中的起始和结束方法都应当被调用，且符合先开始后结束的顺序。
verifySequence {
    mockListener.onStart()
    mockListener.onEnd(any())
}
```


# Kotlin相关
## 单例与静态方法
MockK提供了针对Object、JVM静态方法等元素的Mock工具，以便我们在Kotlin环境中便捷地编写测试代码。

🟤 示例十四：模拟Object中的普通方法。

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

🔴 示例十五：模拟Object中的静态方法。

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

🟠 示例十六：模拟伴生对象中的方法。

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

## 构造方法
在前文“示例一”中，UserManager的依赖组件DBHelper是一个私有变量，测试代码无法正常注入Mock对象，因此我们通过反射将Mock对象传入到变量中。编写反射代码较为繁琐，MockK能够拦截构造方法并返回Mock对象，简化此类场景的注入过程。

🟡 示例十七：模拟构造方法。

在本示例中，我们模拟类的构造方法。

第一步，我们编写业务代码。

订单类Order的构造方法需要接收一个字符串，表示商品信息，并且提供了两个方法输出商品信息。

"Order.kt":

```kotlin
class Order(
    private val goods: String
) {

    fun showInfo1(): String = goods

    fun showInfo2(): String = goods
}
```

第二步，我们编写测试代码。

"ConstructorTest.kt":

```kotlin
// 为Order类的构造方法启用Mock
mockkConstructor(Order::class)

// 定义行为：当Order类的任意构造方法被调用时，返回Mock对象并指定 `showInfo1()` 方法的行为。
every { anyConstructed<Order>().showInfo1() } returns "[Mocked Order Info]"

// 新建Order对象并检查方法的行为
val order = Order("Apple")
println("已Mock的 `showInfo1()` 方法：${order.showInfo1()}")
println("未Mock的 `showInfo2()` 方法：${order.showInfo2()}")

// 撤销指定类的构造方法Mock设置（可选）
unmockkConstructor(Order::class)
```

我们首先调用 `mockkConstructor(vararg classes: KClass<*>)` 方法并传入目标类的KClass，为它们的构造方法启用Mock功能，随后即可通过 `every {}` 语句定义方法的行为。

`anyConstructed<Order>().showInfo1()` 表示当Order类的任意构造方法被调用时，返回Mock对象，且该对象的 `showInfo1()` 方法具有此处 `every {}` 语句所定义的行为。

当构造方法的Mock行为使用完毕后，我们可以调用 `unmockkConstructor(vararg classes: KClass<*>)` 方法撤销行为定义。

此时运行示例程序，并查看控制台输出信息：

```text
已Mock的 `showInfo1()` 方法：[Mocked Order Info]
未Mock的 `showInfo2()` 方法：[Apple]
```

根据上述输出内容可知：

在Mock构造方法的同时我们也要配置实例方法的行为，若调用者访问已定义行为的实例方法，Mock对象将执行 `every {}` 语句中指定的逻辑；若调用者访问未定义行为的实例方法，Mock对象将执行真实的逻辑。

---

如果我们希望针对不同的构造参数设置差异化的Mock行为，可以使用参数匹配器。

🟢 示例十八：模拟特定条件的构造方法。

在本示例中，我们模拟输入参数与预定规则相符的构造方法。

"ConstructorTest.kt":

```kotlin
// 为Order类的构造方法启用Mock
mockkConstructor(Order::class)

// 定义行为：仅当构造Order对象的参数为"Apple"时，返回Mock对象并指定 `showInfo1()` 方法的行为。
every {
    constructedWith<Order>(EqMatcher("Apple")).showInfo1()
} returns "[Mocked Order Info]"

// 新建Order对象并检查方法的行为
println("使用Apple构造的实例：${Order("Apple").showInfo1()}")
println("使用Banana构造的实例：${Order("Banana").showInfo1()}")
```

`constructedWith()` 方法可以配置参数匹配器，但此处不支持 `any()` 、 `eq()` 等简化形式，我们需要创建匹配器实例，例如此处的 `EqMatcher("Apple")` 表示匹配参数值等于 `Apple` 的调用。

此时运行示例程序，并查看控制台输出信息：

```text
使用Apple构造的实例：[Mocked Order Info]
使用Banana构造的实例：[Banana]
```

## 属性
MockK允许我们对Kotlin的属性进行模拟与验证，下文示例展示了相关方法。

🔵 示例十九：模拟与验证属性的Get调用。

在本示例中，我们模拟与验证Kotlin属性的Get调用。

第一步，编写业务代码。

"Properties.kt":

```kotlin
class Properties {
    var name: String = "Real User"
}
```

第二步，编写测试代码。

"PropertiesTest.kt":

```kotlin
val mock = mockk<Properties>()

// 定义行为：如果调用者访问 `name` 属性，则返回 "Mock User"。
every { mock.name } returns "Mock User"
// 对于读取属性的调用，也可以使用 `getProperty()` 进行定义，与前一行等价。
every { mock getProperty ("name") } returns "Mock User"

println("访问 `name` 属性：${mock.name}")

// 验证属性是否已被访问
verify { mock.name }
// 对于读取属性的调用，也可以使用 `getProperty()` 进行验证，与前一行等价。
verify { mock getProperty ("name") }
```

在 `every {}` 与 `verify {}` 语句块中，我们可以通过 `getProperty(<属性名称>)` 指明模拟或验证属性的Get方法。为了简化代码，我们也可以直接使用 `<Mock对象>.<属性>` 语句，它与 `getProperty(<属性名称>)` 是等价的。

---

🟣 示例二十：模拟与验证属性的Set调用。

在本示例中，我们模拟与验证Kotlin属性的Set调用。

第一步，编写业务代码。

"Properties.kt":

```kotlin
class Properties {
    var age: Int = 20
}
```

第二步，编写测试代码。

"PropertiesTest.kt":

```kotlin
val mock = mockk<Properties>()

// 暂存调用者设置的值
var mockAge = 0
// 对于设置属性的调用，需要使用 `setProperty()` 进行定义，并且可以设置匹配器。
every { mock setProperty ("age") value less(0) } answers {
    // 可以通过 `firstArg<T>()` 获取属性值
    val v = firstArg<Int>()
    println("年龄不能为负数($v)，最低为 `0` 。")
    mockAge = 0
}
every { mock.age } returns mockAge

// 设置属性
println("试图设置 `age` 属性为 -1 。")
mock.age = -1
println("访问 `age` 属性：${mock.age}")

// 验证属性是否已被设置
verify { mock setProperty ("age") value less(0) }
```

对于属性的Set方法，我们需要通过 `setProperty(<属性名称>)` 进行指明，并且可以在 `value` 之后设置参数匹配器。

此时运行示例程序，并查看控制台输出信息：

```text
试图设置 `age` 属性为 -1 。
年龄不能为负数(-1)，最低为 `0` 。
访问 `age` 属性：0
```


# 高级应用
## 参数捕获器
参数捕获器可以帮助我们获取Mock方法被调用时的参数值，以便进一步验证或处理。下文列表展示了一些典型的应用场景：

- 模拟事件触发：被测对象通过回调接口监听依赖组件的事件，此时我们可以模拟依赖组件并使用参数捕获器获取回调实现，然后调用其中的方法模拟事件触发。
- 复杂验证场景： `verify()` 方法只能验证单次方法调用，有时我们希望收集多次调用的参数并进行评估，例如：记录某异步操作执行5次的回调结果，并找出平均值与最大值。

下文示例展示了参数捕获器的具体用法。

🟤 示例二十一：捕获回调接口并模拟事件。

在本示例中，我们捕获被测对象向依赖组件注册的监听器实例，并模拟事件触发。

第一步，编写业务代码。

我们定义LogConfigTool作为LogManager的依赖组件，LogManager通过回调接口监听日志配置变更事件，并同步更新自身的最小日志级别。

"LogConfigTool.kt":

```kotlin
object LogConfigTool {

    private var listener: ConfigListener? = null

    // 注册回调
    fun addConfigListener(l: ConfigListener) {
        listener = l
    }

    // 回调接口：日志配置变更
    fun interface ConfigListener {

        // 回调方法：最小日志级别变更
        fun onLevelChange(level: Level)
    }
}
```

当LogManager初始化时，其向LogConfigTool注册监听器，接收日志级别变更事件并更新自身的全局变量。

"LogManager.kt":

```kotlin
class LogManager {

    var minLevel: Level = Level.INFO
        private set

    init {
        // 注册配置变更监听器，并同步设置最小级别。
        LogConfigTool.addConfigListener { newLevel ->
            minLevel = newLevel
        }
    }
}
```

第二步，编写测试代码。

"CaptorTest.kt":

```kotlin
// 定义行为：当LogConfigTool的 `addConfigListener()` 方法被调用时，捕获调用者传入的实例。
val slot = slot<LogConfigTool.ConfigListener>()
mockkObject(LogConfigTool)
every { LogConfigTool.addConfigListener(capture(slot)) } just runs

// 创建被测类的实例
val manager = LogManager()
println("初始的日志级别：${manager.minLevel}")

// 调用捕获到的监听器方法，模拟事件回调。
slot.captured.onLevelChange(Level.WARNING)

println("事件触发后的日志级别：${manager.minLevel}")
// 验证事件触发是否确实改变了被测对象的属性
assertEquals(Level.WARNING, manager.minLevel)
```

`slot<T>()` 表示创建一个参数捕获容器，用于接收捕获到的参数引用；捕获操作需要在 `every {}` 语句块中定义， `capture(<参数容器>)` 表示捕获其所在位置的参数，当 `addConfigListener()` 方法被调用时，调用者传入的参数实例将会被存储到 `slot` 容器中。

当被测对象注册回调后，该回调实现应当被捕获并存储在 `slot` 容器中，我们可以通过 `slot.captured` 访问捕获到的实例，并调用其中的方法模拟事件触发。

此时运行示例程序，并查看控制台输出信息：

```text
初始的日志级别：INFO
事件触发后的日志级别：WARNING
```

---

`slot<T>()` 容器只能存储单个引用，如果捕获器多次触发，其中只会保留最后一次捕获的引用；如果我们希望收集多次调用的参数，可以使用 `mutableListOf<T>()` 容器。

🔴 示例二十二：捕获多次调用的参数。

在本示例中，我们捕获被测对象向依赖组件注册的监听器实例，并计算平均耗时。

第一步，我们对前文“示例一”中的 `saveLog()` 方法进行修改，每次回调 `onEnd()` 事件时采用随机时长，模拟真实的文件读写场景。

"LogManager.kt":

```kotlin
fun saveLog(callback: StateCallback) {
    // 通知外部监听者操作开始
    callback.onStart()

    // 生成随机耗时以模拟实际操作
    val time = (100..500).random().toLong()
    callback.onEnd(time)
}
```

第二步，编写测试代码。

我们使用MutableList作为捕获容器，并调用5次 `saveLog()` 方法，收集每次 `onEnd()` 回调方法的参数值，最后计算平均耗时。

"CaptorTest.kt":

```kotlin
// 创建列表以接收多次调用参数
val slots = mutableListOf<Long>()
// 创建监听器的Mock对象
val mockListener: LogManager.StateCallback = mockk(relaxed = true)
// 定义行为：当监听器的 `onEnd()` 方法被调用时，捕获传入的参数。
every { mockListener.onEnd(capture(slots)) } just runs

// 创建被测类的实例
val manager = LogManager()
// 调用5次保存日志的方法
repeat(5) {
    manager.saveLog(mockListener)
}

// 查看捕获到的参数
slots.forEachIndexed { i, v ->
    println("第${i + 1}次调用，耗时：${v} ms。")
}
// 计算平均耗时
println("平均耗时：${slots.average()} ms。")
```

此时运行示例程序，并查看控制台输出信息：

```text
第1次调用，耗时：450 ms。
第2次调用，耗时：187 ms。
第3次调用，耗时：162 ms。
第4次调用，耗时：280 ms。
第5次调用，耗时：373 ms。
平均耗时：290.4 ms。
```

---
Kotlin支持将Lambda表达式作为参数，MockK也提供了对应的捕获工具。

🟠 示例二十三：捕获Lambda表达式。

在本示例中，我们捕获Mock方法中的Lambda表达式，并在 `answers {}` 语句块中调用。

第一步，编写业务代码。

我们在LogConfigTool中新增 `prepare()` 方法模拟准备日志目录的功能，当准备完毕后回调 `onComplet` 参数提供的Lambda表达式通知日志路径。

"LogConfigTool.kt":

```kotlin
fun prepare(onComplet: (dir: String) -> Unit) {
    // 模拟耗时操作
    Thread.sleep(5000L)
    // 触发回调方法，通知调用者初始化完成。
    onComplet.invoke("/var/log/2025-12-31/")
}
```

第二步，编写测试代码。

测试环境通常不便访问真实的文件系统，因此我们使用参数捕获器捕获 `prepare()` 方法的Lambda表达式，并传递一个虚拟路径给调用者。

"CaptorTest.kt":

```kotlin
mockkObject(LogConfigTool)
// 定义行为：当LogConfigTool的 `prepare()` 方法被调用时，捕获调用者传入的Lambda表达式。
every { LogConfigTool.prepare(captureLambda()) } answers {
    // 获取捕获到的Lambda表达式，并立即调用它。
    lambda<(String) -> Unit>().invoke("/mock/log/")
}

// 调用Mock方法，并传入Lambda作为回调实现。
LogConfigTool.prepare {
    println("目录准备完成，路径：[$it]")
}
```

`captureLambda()` 表示捕获其所在位置的Lambda表达式，随后我们便可在 `answers {}` 语句块中通过 `lambda<T>()` 访问捕获到的表达式并调用它， `T` 为Lambda表达式的参数与返回值。

此时运行示例程序，并查看控制台输出信息：

```text
目录准备完成，路径：[/mock/log/]
```

---

`captureLambda()` 捕获器具有一些限制，它只能在 `answers {}` 语句块内部被调用，在某些场景中难以实现按需触发。除此之外，如果Mock方法具有多个Lambda参数， `captureLambda()` 方法只能使用一次，如果我们在多个Lambda参数处填写 `captureLambda()` 会导致异常。

对于上述场景，我们可以使用常规的参数捕获器。

🟡 示例二十四：捕获Lambda表达式（方法二）。

在本示例中，我们捕获Mock方法中的Lambda表达式，并在适当的时机调用。

"CaptorTest.kt":

```kotlin
mockkObject(LogConfigTool)
// 使用CapturingSlot承载Lambda表达式
val slot = slot<(String) -> Unit>()
every { LogConfigTool.prepare(capture(slot)) } answers {
    // 可以在 `answers {}` 代码块中调用捕获到的Lambda表达式
    slot.invoke("/mock/log/")
}

// 调用Mock方法，并传入Lambda作为回调实现。
LogConfigTool.prepare {
    println("目录准备完成，路径：[$it]")
}

// 也可以在调用发生后使用Lambda表达式，模拟异步回调。
slot.invoke("/mock/log2/")
```

`slot<T>()` 容器也可以承载Lambda参数，我们只需在泛型 `T` 处填写Lambda表达式的参数与返回值即可。

## Spy模式
Spy模式与Mock模式是相反的，Mock对象的所有方法均为模拟行为，而Spy对象的所有方法均为真实行为，我们可以按需定义模拟行为，适用于仅需要模拟少部分方法的场景。

🟢 示例二十五：Spy模式。

在本示例中，我们创建MemoryInfo的Spy对象，并为 `getFreeMemory()` 方法定义行为，模拟剩余内存较低的场景。

第一步，我们编写业务代码。

MemoryInfo提供了两个方法用于获取JVM内存总量和空闲内存量。

"MemoryInfo.kt":

```kotlin
class MemoryInfo {

    private val runtime = Runtime.getRuntime()

    fun getTotalMemory(): Long = runtime.totalMemory()

    fun getFreeMemory(): Long = runtime.freeMemory()
}
```

第二步，我们编写测试代码。

"SpyTest.kt":

```kotlin
// 创建Spy对象
val spyMemoryInfo = spyk(MemoryInfo())

println("初始状态...")
println("内存总量：${spyMemoryInfo.getTotalMemory()}")
println("空闲内存：${spyMemoryInfo.getFreeMemory()}")

// 定义行为：模拟剩余内存为8KB的情况
every { spyMemoryInfo.getFreeMemory() } returns 8 * 1024L

println("定义行为后...")
println("内存总量：${spyMemoryInfo.getTotalMemory()}")
println("空闲内存：${spyMemoryInfo.getFreeMemory()}")
```

`spyk()` 方法用于创建Spy对象，参数是目标类的实例，该方法将会复制现有实例内的属性值并生成新的Spy对象。

如果目标类具有无参构造方法，我们也可以使用简化的方式创建Spy对象，例如： `spyk<MemoryInfo>()` 。

此时运行示例程序，并查看控制台输出信息：

```text
初始状态...
内存总量：526385152
空闲内存：502728992
定义行为后...
内存总量：526385152
空闲内存：8192
```

## 私有方法
在某些特殊的环境中，我们可能需要为私有方法或无法直接通过代码引用的公开方法设置Mock行为、验证调用情况，例如：Android SDK中的隐藏方法，下文示例展示了具体语法。

🔵 示例二十六：模拟与验证私有方法。

在本示例中，我们为类的私有方法设置Mock行为，并进行调用与验证。

第一步，编写业务代码。

"Privates.kt":

```kotlin
class Privates {
    private fun foo(p0: String): String = "Real!"
}
```

第二步，编写测试代码。

"PrivatesTest.kt":

```kotlin
// 创建Spy对象并开启私有方法调用记录
val mock = spyk<Privates>(recordPrivateCalls = true)

// 定义行为：当私有方法 `foo()` 被调用时，返回 `Mock!` 。
every { mock invoke "foo" withArguments listOf(any<String>()) } returns "Mock!"

// 简化写法
every { mock["foo"].invoke(any<String>()) } returns "Mock!"

// 通过反射调用私有方法
mock.getMethod("foo", String::class.java)?.let {
    val result = it.invoke(mock, "Hello!")
    println("私有方法的返回值：$result")
}

// 验证私有方法是否被调用
verify { mock["foo"].invoke("Hello!") }
```

我们可以模拟Mock对象的私有方法，但它们不会记录调用情况，如果需要验证私有方法，我们应当创建Spy对象，并指定 `recordPrivateCalls = true` 。

在 `every {}` 或 `verify {}` 语句块中，我们可以通过 `<Mock对象> invoke "<目标方法名称>" withArguments listOf(<参数匹配器> ...)` 指明需要模拟或验证的方法，此处Kotlin无法自动推断参数类型，因此参数匹配器必须指明参数的Class。

MockK还提供了一种简化语法： `<Mock对象>["<目标方法名称>"].invoke(<参数匹配器> ...)` ，它与完整语法是等价的。

对于无法在代码中直接引用的类，我们可以通过字符串形式指明KClass，例如： `any<String>()` 等价于 `any(Class.forName("java.lang.String").kotlin)` 。
