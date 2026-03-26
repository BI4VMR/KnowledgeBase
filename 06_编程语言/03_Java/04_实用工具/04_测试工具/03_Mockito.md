# 简介
Mockito是一个在Java语言中被广泛使用的Mock工具，我们可以用它创建Mock对象，为待测接口提供模拟数据，隔离单元测试等场景中的依赖项，其官方网站为： [🔗 Mockito](https://site.mockito.org/) 。

Mockito的早期版本通过继承实现Mock，不支持模拟 `final` 类与静态方法，我们可以使用 `mockito-inline` 和PowerMock等工具进行操作。自从3.4.0版本开始，Mockito新增了通过Byte Buddy修改字节码模拟 `final` 类与静态方法的功能，我们不必再使用其他工具实现此类功能。

本章的示例工程详见以下链接：

- [🔗 示例工程：Mockito](https://github.com/BI4VMR/Study-Java/tree/master/M04_Utils/C04_Test/S03_Mockito)


# 基本应用
## 依赖隔离
Mock工具的基本用途是模拟被测组件所依赖的外部接口，提供测试数据与行为，使我们专注于被测组件本身的业务逻辑，不必关心外围环境的变化。

🔴 示例一：模拟待测组件的依赖项。

在本示例中，我们创建Mock对象，并将它们注入到待测对象中，实现依赖隔离。

第一步，我们在构建系统中声明对于Mockito组件的依赖。

如果构建系统为Maven，我们可以使用下文代码块中的语句声明依赖：

"pom.xml":

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.16.1</version>
    <scope>test</scope>
</dependency>
```

如果构建系统为Gradle，我们可以使用下文代码块中的语句声明依赖：

"build.gradle":

```groovy
dependencies {
    testImplementation 'org.mockito:mockito-core:5.16.1'
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
dependencies {
    testImplementation("org.mockito:mockito-core:5.16.1")
}
```

第二步，我们编写业务代码。

此处以用户管理功能为例，我们编写一个UserManager类提供用户信息查询接口，其依赖于数据库工具类DBHelper提供的接口。

"DBHelper.java":

```java
public class DBHelper {

    // 查询用户信息；返回用户信息Map，键为ID，值为名称。
    public Map<Long, String> queryUsers() {
        Map<Long, String> map = new HashMap<>();
        map.put(1L, "张三");
        map.put(2L, "李四");
        return map;
    }
}
```

"UserManager.java":

```java
public class UserManager {

    // 依赖项：数据库
    private final DBHelper mDBHelper = new DBHelper();

    // 获取所有用户名称
    public List<String> getUserNames() {
        Collection<String> collection = mDBHelper.queryUsers()
                .values();
        return new ArrayList<>(collection);
    }
}
```

第三步，我们编写测试代码。

我们的目标是测试UserManager功能是否正常，并不关心DBHelper连接何种数据库、用户信息表具有何种结构，因此我们需要创建DBHelper类的Mock对象，给UserManager提供模拟的用户信息。

"UserManagerTest.java":

```java
@Test
public void test_GetUserNames() {
    // 模拟数据
    Map<Long, String> mockDatas = new HashMap<>();
    mockDatas.put(1L, "来宾账户");
    mockDatas.put(2L, "用户A");
    mockDatas.put(3L, "用户B");

    // 创建DBHelper的Mock对象
    DBHelper mockDBHelper = Mockito.mock();
    // 定义行为：如果 `queryUsers()` 方法被调用，则返回模拟数据。
    Mockito.when(mockDBHelper.queryUsers()).thenReturn(mockDatas);

    // 构造待测类的对象，并注入Mock对象作为依赖。
    UserManager manager = new UserManager();
    ReflectUtil.setFieldValue(manager, "mDBHelper", mockDBHelper);

    // 调用待测方法
    List<String> users = manager.getUserNames();

    // 查看返回的内容
    for (String user : users) {
        System.out.println(user);
    }
    // 验证Mock对象的方法是否被调用
    Mockito.verify(mockDBHelper).queryUsers();
    // 验证待测方法的返回值是否与预期一致
    Assert.assertTrue(mockDatas.values().containsAll(users));
}
```

我们首先通过 `Mockito.mock()` 方法创建Mock对象，并通过 `Mockito.when(mockDBHelper.queryUsers()).thenReturn(mockDatas)` 语句定义Mock对象的行为：如果 `mockDBHelper` 对象的 `queryUsers()` 方法被调用，则返回 `mockDatas` 给调用者。

接着，我们创建了待测接口所在类UserManager的对象，并通过反射将Mock对象注入到UserManager中，工具方法 `setFieldValue()` 的具体实现详见示例工程。

Mockito的 `mock()` 和 `when()` 等都是静态方法，我们可以在测试类中添加静态导入语句： `import static org.mockito.Mockito.*;` ，后续调用这些方法就无需添加 `Mockito.` 前缀了。

此时运行示例程序，并查看控制台输出信息：

```text
Index:[0] Name:[来宾账户]
Index:[1] Name:[用户A]
Index:[2] Name:[用户B]
```

根据上述输出内容可知：

UserManager调用DBHelper中的接口后，输出内容确实为测试代码提供的模拟数据，并非DBHelper的内置数据，表明Mock操作成功，测试代码已经隔离了UserManager的外部依赖DBHelper。

## 常用注解
在前文示例中，我们使用 `Mockito.mock()` 方法创建了一些局部Mock对象，它们仅能在单一测试用例内部被访问；若我们希望在JUnit的 `setUp()` 方法中统一配置Mock对象及行为，并在多个测试用例中共享Mock对象，此时可以使用Mockito提供的注解。

🟠 示例二：使用注解创建Mock对象。

在本示例中，我们以JUnit 4平台为例，使用Mockito提供的注解创建Mock对象。

"AnnotationTest.java":

```java
public class AnnotationTest {

    @Mock
    DBHelper mockDBHelper;

    @Before
    public void setup() {
        // 若要使用Mockito注解，需要在执行其他操作前先初始化。
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_GetUserNames() {
        // 在此处使用Mock对象
    }
}
```

`@Mock` 注解表示被修饰的全局变量是Mock对象，程序运行时Mockito会根据变量类型自动创建对象并完成赋值。

Mockito的注解默认不会生效，为了使注解生效并创建Mock对象，我们需要在每个测试用例执行前调用 `MockitoAnnotations.openMocks(this)` 方法。

Mockito针对JUnit框架提供了预设的Runner，我们可以在测试类上通过 `@RunWith` 注解进行配置，省略 `MockitoAnnotations.openMocks(this)` 等语句，进一步简化代码。

"AnnotationTest2.java":

```java
@RunWith(MockitoJUnitRunner.class)
public class AnnotationTest2 {

    // 创建一个DBHelper类的Mock对象
    @Mock
    DBHelper mockDBHelper;

    @Test
    public void testGetUserNames() {
        // 在此处使用Mock对象
    }
}
```


# 定义行为
## 基本应用
为了观察被测实例在不同环境中的行为是否符合预期，我们可以通过 `Mockito.when()` 语句定义其依赖的Mock对象被访问时的动作。

在前文示例中，我们已经初步应用了指定方法返回值的 `thenReturn(<返回值>)` 语句，下文列表展示了 `when()` 语句后可填写的后继语句：

- `thenReturn(<返回值>)` : 该方法被调用时，始终返回固定的值。
- `thenReturn(<返回值1>, <返回值2>...)` : 该方法被多次调用时，依次返回指定的值序列。
- `thenAnswer(<语句块>)` : 该方法被调用时，执行自定义逻辑。
- `thenThrow(<异常对象>)` : 该方法被调用时，抛出指定的异常。
- `thenThrow(<异常对象1>, <异常对象2>...)` : 该方法被多次调用时，依次抛出列表中的异常。

前置的 `when()` 语句必须有返回值，对于无返回值方法，Mockito提供了 `doNothing().when(<Mock对象>).<无返回值方法>` 语句。除此之外，还有 `doReturn()` 等语句，它们的作用与 `thenReturn()` 等语句相同，只是语序相反：

- `doNothing()` : 该方法被调用时，“什么都不做”，仅适用于无返回值的方法。
- `doReturn(<返回值>)` : 效果同 `thenReturn(<返回值>)` 。
- `doReturn(<返回值1>, <返回值2>...)` : 效果同 `thenReturn(<返回值1>, <返回值2>...)` 。
- `doAnswer(<实现>)` : 效果同 `thenAnswer(<语句块>)` 。
- `doThrow(<异常对象>)` : 效果同 `thenThrow(<异常对象>)` 。
- `doThrow(<异常对象1>, <异常对象2>...)` : 效果同 `thenThrow(<异常对象1>, <异常对象2>...)` 。

下文示例展示了行为定义语句的具体用法。

🟢 示例三：模拟固定返回值。

在本示例中，我们为Mock对象定义行为，每当指定方法被调用时，返回测试用例中指定的值。

"DefineBehaviorTest.java":

```java
// 创建Mock对象
File mockFile = mock(File.class);
// 定义行为：当 `mockFile` 的 `getCanonicalPath()` 方法被访问时，返回 `/data/file1` 。
when(mockFile.getCanonicalPath()).thenReturn("/data/file1");
// 上一条语句的等价语法
doReturn("/data/file1").when(mockFile).getCanonicalPath();

// 调用Mock对象的 `getCanonicalPath()` 方法并输出结果
System.out.println("File Path:[" + mockFile.getCanonicalPath() + "]");
// 再次调用Mock对象的 `getCanonicalPath()` 方法并输出结果
System.out.println("File Path:[" + mockFile.getCanonicalPath() + "]\n");


// 修改行为：当 `mockFile` 的 `getCanonicalPath()` 方法被访问时，返回 `/data/file2` 。
when(mockFile.getCanonicalPath()).thenReturn("/data/file2");

// 调用Mock对象的 `getCanonicalPath()` 方法并输出结果
System.out.println("File Path:[" + mockFile.getCanonicalPath() + "]");
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

🔵 示例四：模拟序列返回值。

在本示例中，我们为Mock对象定义行为，每当指定方法被调用时，依次返回不同的值。

"DefineBehaviorTest.java":

```java
// 创建Mock对象
File mockFile = mock(File.class);
// 定义行为：当 `mockFile` 的 `length()` 方法被访问时，依次返回指定的值序列。
when(mockFile.length()).thenReturn(100L, 200L, 1024L);

// 多次访问Mock对象的属性并输出结果
for (int i = 1; i <= 5; i++) {
    System.out.println("第 " + i + " 次调用： Length:[" + mockFile.length() + "]");
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

🟣 示例五：自定义行为。

在本示例中，我们为Mock对象定义行为，每当指定方法被调用时，输出控制台消息。

"DefineBehaviorTest.java":

```java
// 创建Mock对象
File mockFile = mock(File.class);
// 定义行为：当 `mockFile` 的 `getCanonicalPath()` 方法被访问时，执行自定义逻辑。
when(mockFile.getCanonicalPath()).thenAnswer(invocation -> {
    // 输出消息
    System.out.println(invocation.getMock() + " `getCanonicalPath()` was called.");

    // 返回值
    return "/data/file1";
});

// 调用Mock对象的 `getCanonicalPath()` 方法并输出结果
System.out.println("File Path:[" + mockFile.getCanonicalPath() + "]");
```

此时运行示例程序，并查看控制台输出信息：

```text
Mock for File, hashCode: 1476245668 `getCanonicalPath()` was called.
File Path:[/data/file1]
```

根据上述输出内容可知：

当Mock对象的 `getCanonicalPath()` 方法被调用时，`thenAnswer()` 回调方法中的语句被执行了，首先输出控制台消息，然后将回调方法的返回值作为Mock对象返回值传递给调用者。

---

🟤 示例六：模拟异常。

在本示例中，我们为Mock对象定义行为，每当指定方法被调用时，抛出测试用例指定的异常。

"DefineBehaviorTest.java":

```java
@Test(expected = IOException.class)
public void test_Define_Exception() throws IOException {
    // 创建Mock对象
    File mockFile = mock(File.class);
    // 定义行为：当 `mockFile` 的 `getCanonicalPath()` 方法被访问时，抛出异常。
    when(mockFile.getCanonicalPath()).thenThrow(new IOException("This is a mock exception!"));

    // 调用Mock对象的方法
    mockFile.getCanonicalPath();
}
```

当Mock对象的 `getCanonicalPath()` 方法被调用时，其会抛出IOException异常，此处我们通过JUnit捕获了异常，因此测试用例不会执行失败。

## 参数匹配器
有参方法的行为与调用者传入的参数密切相关，因此我们可以使用参数匹配器定义Mock对象的行为，以便模拟较为复杂的环境。

下文列表展示了常用的参数匹配器：

- `<参数值>` : 精确匹配调用者传入参数与指定参数值一致的调用。
- `eq(<参数值>)` : 精确匹配调用者传入参数与指定参数值一致的调用。
- `any()` : 匹配任意参数值。
- `anyInt()` : 匹配任意整型参数值，类似的匹配器还有 `anyString()` 等，用于标识重载方法。
- `isNull()` : 匹配传入参数为空值的调用。
- `isNotNull()` : 匹配传入参数非空值的调用。
- `isA(<Class>)` : 匹配传入参数类型与指定类型一致的调用。
- `argThat(<匹配器实现>)` : 自定义匹配规则。

下文示例展示了参数匹配器的具体用法。

🔴 示例七：参数匹配器。

在本示例中，我们使用参数匹配器定义Mock方法接收到不同参数时的行为。

第一步，我们定义DBHelper类，并声明一些方法以便进行Mock测试。

"DBHelper.java":

```java
public class DBHelper {

    public String queryUserName(int id) {
        return "Real Name";
    }

    public String queryUserName(String cardID) {
        return "Real Name";
    }

    public List<String> queryUserNames(int age, boolean male) {
        List<String> real = new ArrayList<>();
        real.add("Real Name");
        return real;
    }
}
```

第二步，我们使用参数匹配器定义 `queryUserName(id: Int)` 方法在不同参数下的行为。

"DefineBehaviorTest.java":

```java
DBHelper mockDBHelper = mock(DBHelper.class);
// 定义不同条件下 `queryUserName()` 方法的返回值
when(mockDBHelper.queryUserName(anyInt())).thenReturn("MockUser");
when(mockDBHelper.queryUserName(1)).thenReturn("Alice");
when(mockDBHelper.queryUserName(2)).thenReturn("Bob");

// 查看返回值
System.out.println("QueryUserName of ID=1:[" + mockDBHelper.queryUserName(1) + "]");
System.out.println("QueryUserName of ID=2:[" + mockDBHelper.queryUserName(2) + "]");
System.out.println("QueryUserName of ID=3:[" + mockDBHelper.queryUserName(3) + "]");
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

在DBHelper类中存在 `queryUserName(int id)` 方法和 `queryUserName(String cardID)` 方法，若我们直接在 `when()` 中填入 `queryUserName(any())` ，就会产生歧义，此时Mockito无法确定我们想要定义哪个方法的行为。

Mockito针对每种数据类型提供了对应的匹配器，例如 `anyInt()` 、 `anyBoolean()` 、 `anyString()` 、 `any(Class<T> type)` 等，我们可以根据目标方法参数类型选择匹配器，解决重载方法的匹配问题。

🟠 示例九：匹配重载方法。

在本示例中，我们使用参数匹配器定义不同重载方法的行为。

"DefineBehaviorTest.kt":

```kotlin
// 匹配参数为Int类型的方法
when(mockDBHelper.queryUserName(anyInt())).thenReturn("MockUserA");

// 匹配参数为String类型的方法
when(mockDBHelper.queryUserName(anyString())).thenReturn("MockUserB");
```

---

`<参数值>` 与匹配器 `eq(<参数值>)` 都表示精确匹配参数值，但它们的适用场景有一些区别，我们通过下文示例进行说明。

🟡 示例九：具体值与 `eq()` 匹配器。

在本示例中，我们演示参数具体值与 `eq()` 匹配器的区别。

"DefineBehaviorTest.java":

```java
DBHelper mockDBHelper = mock(DBHelper.class);

// 错误用法：字面量与匹配器混用
when(mockDBHelper.queryUserNames(20, anyBoolean())).thenReturn(new ArrayList<>());

// 正确用法：全部使用匹配器
when(mockDBHelper.queryUserNames(eq(20), anyBoolean())).thenReturn(new ArrayList<>());
// 正确用法：全部使用字面量
when(mockDBHelper.queryUserNames(20, false)).thenReturn(new ArrayList<>());
```

在同一条行为定义语句中，要么全部使用具体值，要么全部使用匹配器。我们不应将二者混用，虽然有时混用不会导致错误，但这属于未定义行为，应当尽量避免。

## 偏函数模拟
默认情况下，Mock对象的所有方法实现均为模拟行为，要么执行我们通过 `when()` 语句块定义的行为，要么返回默认值。有时我们希望执行原始方法的逻辑，或者获取原始方法的返回值并进行处理，这种需求被称为“偏函数模拟”。

在 `thenAnswer()` 语句块中，我们可以调用 `callRealMethod()` 方法，此方法将会调用 `thenAnswer()` 语句中的原始方法；若原始方法具有返回值，也会通过此方法返回，以便我们实现偏函数模拟。

🟢 示例十：偏函数模拟。

在本示例中，我们调用Mock对象的原始方法，对返回值进行处理后再传递给调用者。

"DefineBehaviorTest.java":

```java
DBHelper mockDBHelper = mock(DBHelper.class);
when(mockDBHelper.queryUserNames(anyInt(), anyBoolean())).thenAnswer(invocation -> {
    // 调用原始方法
    Object rawResult = invocation.callRealMethod();
    List<String> rawList = (List<String>) rawResult;
    System.out.println("真实调用的返回值：" + rawList);

    // 追加一些模拟数据
    List<String> result = new ArrayList<>(rawList);
    result.add("MockUser1");
    return result;
});
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

Mockito提供的行为验证方法是 `verify(<Mock对象>， [验证器])` ，常用的验证器如下文列表所示：

- `atLeast(n)` : 指定目标方法被调用的最少次数。
- `atMost(n)` : 指定目标方法被调用的最多次数。
- `times(n)` : 指定目标方法被调用的次数，例如：数值 `1` 表示目标方法必须被调用一次，未被调用或被调用次数大于 `1` 都会导致验证失败。
- `never()` : 要求目标方法未被调用。
- `timeout(ms)` : 指定目标方法最大允许执行的时长。

🔵 示例十一：基本的行为验证。

在本示例中，我们使用 `verify()` 方法验证被测接口是否正确地调用了依赖组件。

第一步，我们编写业务代码。

LogManager利用Logger实现日志输出功能，其中 `printInfo()` 方法能够将一组消息分行输出。

"LogManager.java":

```kotlin
public class LogManager  {

    // 外部依赖
    private Logger logger = Logger.getAnonymousLogger();

    // 业务方法：将一组消息分行输出
    public void printInfo(List<String> messages) {
        for (String message : messages) {
            logger.log(Level.INFO, message);
        }
    }
}
```

第二步，我们编写测试代码。

"LogManagerTest.java":

```java
// 创建Logger的Mock对象
Logger mockLogger = mock(Logger.class);

// 创建待测类的实例并注入Mock对象
LogManager manager = new LogManager();
manager.setLogger(mockLogger);

// 执行业务方法
List<String> text = Arrays.asList("LineA", "LineB");
manager.printInfo(text);

// 验证行为：Logger的记录方法应当被调用2次
verify(mockLogger, times(2)).log(eq(Level.INFO), anyString());
```

此处我们将长度为2的List传递给 `printInfo()` 方法，预期输出两行日志，因此Logger的 `log()` 方法应当被调用两次， `verify()` 方法的参数为 `times(2)` 。

随后，我们在同一个测试用例中再次调用 `printInfo()` 方法，传入一个长度为3的List。

"LogManagerTest.java":

```java
// 创建Logger的Mock对象
Logger mockLogger = mock(Logger.class);

// 创建待测类的实例并注入Mock对象
LogManager manager = new LogManager();
manager.setLogger(mockLogger);

// 执行业务方法
List<String> text = Arrays.asList("LineA", "LineB");
manager.printInfo(text);

// 验证行为：Logger的记录方法应当被调用2次
verify(mockLogger, times(2)).log(eq(Level.INFO), anyString());
```

随后，我们在同一个测试用例中再次调用 `printInfo()` 方法，传入一个长度为3的List。

"LogManagerTest.java":

```java
// 再次执行业务方法
List<String> text2 = Arrays.asList("Line1", "Line2", "Line3");
manager.printInfo(text2);

// 验证行为：Logger的记录方法应当被调用2次 + 3次
verify(mockLogger, times(2 + text2.size())).log(eq(Level.INFO), anyString());
```

此时预期的调用次数应当为5，即首次打印的2次及第二次打印的3次之和；这是因为我们使用了同一个Mock对象， `verify()` 方法不会重置调用记录，其中的调用次数会不断累加。

## 验证调用顺序
Mockito提供了 `InOrder` 接口用于验证多个方法被调用的顺序。

🟣 示例十二：验证一组方法的调用顺序。

"VerifyBehaviorTest.java":

```java
// 创建监听器的Mock对象
LogManager.StateCallback mockListener = mock(LogManager.StateCallback.class);

// 执行业务方法并传入监听器
new LogManager().saveLog(mockListener);

// 验证行为：先执行 onStart，后执行 onEnd。
InOrder inOrder = inOrder(mockListener);
inOrder.verify(mockListener).onStart();
inOrder.verify(mockListener).onEnd(anyLong());
```


# 高级应用
## 参数捕获器
参数捕获器 can help us obtain the parameter values when the mock method is called, for further verification or processing.

🟤 示例十三：捕获参数。

"CaptorTest.java":

```java
// 创建监听器的Mock对象
LogManager.StateCallback mockListener = mock(LogManager.StateCallback.class);

// 创建ArgumentCaptor
ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

// 创建被测类的实例
LogManager manager = new LogManager();
manager.saveLog(mockListener);

// 验证并捕获参数
verify(mockListener).onEnd(captor.capture());

System.out.println("捕获到的耗时：" + captor.getValue());
assertEquals(150L, (long) captor.getValue());
```

如果方法被多次调用，可以使用 `captor.getAllValues()` 获取所有捕获的值。

## Spy模式
Spy模式与Mock模式是相反的，Mock对象的所有方法均为模拟行为，而Spy对象的所有方法均为真实行为，我们可以按需定义模拟行为，适用于仅需要模拟少部分方法的场景。

🔴 示例十四：Spy模式。

在本示例中，我们创建MemoryInfo的Spy对象，并为 `getFreeMemory()` 方法定义行为，模拟剩余内存较低的场景。

第一步，我们编写业务代码。

MemoryInfo提供了两个方法用于获取JVM内存总量和空闲内存量。

"MemoryInfo.java":

```java
public class MemoryInfo {

    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
}
```

第二步，我们编写测试代码。

"SpyTest.java":

```java
// 创建Spy对象
MemoryInfo spyMemoryInfo = spy(new MemoryInfo());

System.out.println("初始状态...");
System.out.println("内存总量：" + spyMemoryInfo.getTotalMemory());
System.out.println("空闲内存：" + spyMemoryInfo.getFreeMemory());

// 定义行为：模拟剩余内存为8KB的情况
when(spyMemoryInfo.getFreeMemory()).thenReturn(8 * 1024L);

System.out.println("定义行为后...");
System.out.println("内存总量：" + spyMemoryInfo.getTotalMemory());
System.out.println("空闲内存：" + spyMemoryInfo.getFreeMemory());
```

`spy()` 方法用于创建Spy对象，参数是目标类的实例，该方法将会复制现有实例内的属性值并生成新的Spy对象。

如果目标类具有无参构造方法或是抽象类，我们也可以使用简化方式创建Spy对象，例如： `spy(MemoryInfo.class)` 。

此时运行示例程序，并查看控制台输出信息：

```text
初始状态...
内存总量：526385152
空闲内存：502728992
定义行为后...
内存总量：526385152
空闲内存：8192
```



