# 简介
JUnit是一个流行的单元测试框架，官方网站为：[🔗 JUnit - 官方网站](https://junit.org/) 。

目前JUnit 4和JUnit 5都是正在被广泛应用的版本，后文内容将会首先介绍JUnit 4的使用方法，并在 [🧭 JUnit 5](#junit5) 小节中对比JUnit 4与JUnit 5的差异。

本章的示例工程详见以下链接：

- [🔗 示例工程：JUnit 4](https://github.com/BI4VMR/Study-Java/tree/master/M04_Utils/C04_Test/S02_JUnit4)
- [🔗 示例工程：JUnit 5](https://github.com/BI4VMR/Study-Java/tree/master/M04_Utils/C04_Test/S03_JUnit5)


# 基本应用
下文示例展示了JUnit 4的基本使用方法：

🔴 示例一：构建JUnit 4环境。

在本示例中，我们将为示例工程添加JUnit 4支持。

第一步，我们在构建系统中声明对于JUnit 4组件的依赖。

如果构建系统为Maven，我们可以使用下文代码块中的语句声明依赖：

"pom.xml":

```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

如果构建系统为Gradle，我们可以使用下文代码块中的语句声明依赖：

"build.gradle":

```groovy
dependencies {
    testImplementation 'junit:junit:4.13.2'
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
dependencies {
    testImplementation("junit:junit:4.13.2")
}
```

第二步，我们编写业务代码。

此处我们实现一个简单的除法计算方法，如果除数为 `0` 则返回空值，避免整个程序终止。

"MathUtil.java":

```java
public class MathUtil {

    public static Integer divideSafe(int a, int b) {
        if (b == 0) {
            return null;
        } else {
            return a / b;
        }
    }
}
```

第三步，我们编写测试代码。

按照惯例，我们会在 `src/main` 目录中放置业务代码、在 `src/test` 目录中放置测试代码。

```text
<模块根目录>
└── src
    ├── main
    │   └── java
    │       └── net.bi4vmr.study
    │           └── MathUtil.java
    └── test
        └── java
            └── net.bi4vmr.study
                └── TestMathUtil.java
```

单元测试代码的文件名通常为被测方法所在文件名加上Test后缀，我们需要测试MathUtil类中的方法，因此将测试类命名为MathUtilTest。

"MathUtilTest.java":

```java
public class MathUtilTest {

    @Test
    public void testDivide() {
        // 测试正常的情况
        Integer result1 = MathUtil.divideSafe(32, 8);
        // 断言：结果非空
        Assert.assertNotNull(result1);
        // 断言：结果等于4
        Assert.assertEquals(4L, result1.longValue());

        // 测试除数为"0"的情况
        Integer result2 = MathUtil.divideSafe(100, 0);
        // 断言：结果为空
        Assert.assertNull(result2);
    }
}
```

每个被 `@Test` 注解修饰的方法都是一个测试方法，它们是JUnit中的最小可执行单元，我们在测试方法中调用被测方法并传入预定参数，然后使用断言方法检查返回值是否与预期相符。在一个测试方法中，若所有断言均通过且没有出现异常，则测试结果为通过；若任意断言不通过或出现异常，则测试结果为不通过。

除了上述示例中涉及的注解与方法之外，JUnit还提供了以下实用工具：

- `@Ignore` : 该注解可以被添加在测试方法上，此类方法不会被JUnit执行，通常用于临时调试。
- `@FixMethodOrder` : 该注解可以被添加在测试类上，用于设置类中多个测试方法的执行顺序。该注解的唯一属性用于指定执行顺序，默认值为 `MethodSorters.DEFAULT` ，即一种不确定的顺序；取值为 `MethodSorters.JVM` 时表示以JVM方法顺序为准，通常也是代码中定义方法的顺序；取值为 `MethodSorters.NAME_ASCENDING` 时表示以方法名称的字母顺序为准。


# 验证结果
## 断言方法
断言方法是JUnit提供的一种结果验证工具，我们可以向断言方法传入期望值与实际值，若实际值与期望值相匹配，则返回成功的结果；若实际值与期望值不匹配，则抛出 `AssertionError` 异常提示开发者，并返回失败的结果。

JUnit提供的断言方法如下文列表所示：

- `Assert.assertEquals(int expected, int actual)` : 判断期望值 `expected` 是否与实际值 `actual` 相等。对于基本数据类型，每种类型都有对应的 `assertEquals()` 重载方法可供选择；对于引用数据类型，实质为调用二者的 `equals()` 方法进行比较。
- `Assert.assertNotEquals(int expected, int actual)` : 判断期望值 `expected` 是否与实际值 `actual` 不相等。判等依据与  `assertEquals()` 方法相同。
- `Assert.assertSame(Object expected, Object actual)` : 判断期望值 `expected` 与实际值 `actual` 的内存地址是否相同。
- `Assert.assertNotSame(Object expected, Object actual)` : 判断期望值 `expected` 与实际值 `actual` 的内存地址是否不同。
- `Assert.assertNull(Object object)` : 判断传入值 `object` 是否为空值。
- `Assert.assertNotNull(Object object)` : 判断传入值 `object` 是否为非空值。
- `Assert.assertTrue(boolean condition)` : 判断传入值 `condition` 是否为真。
- `Assert.assertFalse(boolean condition)` : 判断传入值 `condition` 是否为假。
- `Assert.assertArrayEquals(int expecteds, int actuals)` : 判断期望数组 `expecteds` 是否与实际数组 `actuals` 内容相等。每种数据类型都有对应的 `assertArrayEquals()` 重载方法可供选择。

上述方法都有一个能够汇报失败消息的版本，例如： `assertTrue(boolean condition)` 与 `assertTrue(String message, boolean condition)` 对应。这些消息会在断言失败时显示在控制台或测试报告中，以便开发者了解详情。

如果JUnit提供的断言方法都无法满足需求，我们还可以自行编写断言逻辑，并在条件不满足时调用 `Assert.fail()` 方法抛出 `AssertionError` 异常。

## 检测异常
默认情况下，如果被测方法抛出了异常，JUnit就会认为用例执行失败。有时被测方法在某些条件下应当抛出指定的异常，我们可以在 `@Test` 注解中设置 `expected` 属性进行检测。

🟠 示例二：判断被测方法是否抛出指定异常。

在本示例中，我们制造一个ArithmeticException异常，并在声明测试方法时检测该异常。

"MathUtilTest.java":

```java
@Test(expected = ArithmeticException.class)
public void testException() {
    int a = 100 / 0;
}
```

在上述代码中，测试方法 `testException()` 应当抛出ArithmeticException异常，我们在方法体中故意制造了ArithmeticException异常，因此测试用例能够执行成功。

## 检测超时
有时我们需要检测被测方法的耗时是否小于某个值，可以在 `@Test` 注解中设置 `timeout` 属性进行检测。

🟡 示例三：判断被测方法耗时是否满足要求。

在本示例中，我们通过线程休眠模拟耗时操作，并在声明测试方法时检测耗时时长。

"MathUtilTest.java":

```java
@Test(timeout = 1000L)
public void testTimeout() throws InterruptedException {
    Thread.sleep(5000L);
}
```

在上述代码中，测试方法 `testTimeout()` 将会执行失败，因为休眠时长5秒大于注解所要求的1秒。


# 生命周期
JUnit提供了一些生命周期方法，以便我们在测试开始前进行一些准备工作、在测试结束后进行一些收尾工作。我们可以使用下文列表中的注解修饰自定义方法，JUnit将在对应的时机调用这些方法。

<div align="center">

|      注解      |                          调用时机                          |
| :------------: | :--------------------------------------------------------: |
| `@BeforeClass` |       在测试类首次加载时被调用，只能应用于静态方法。       |
|   `@Before`    |               在每个测试方法执行之前被调用。               |
|    `@After`    |               在每个测试方法执行之后被调用。               |
| `@AfterClass`  | 在测试类中的所有方法执行完毕时被调用，只能应用于静态方法。 |

</div>

下文示例展示了每个方法的执行时机：

🟢 示例四：观察JUnit 4生命周期方法的执行顺序。

在本示例中，我们在JUnit 4的方法中输出日志，并观察执行顺序。

"LifeCycleTest.java":

```java
public class LifeCycleTest {

    @BeforeClass
    public static void setupClass() {
        System.out.println("@BeforeClass：在测试类首次加载时被执行");
        // 此处用于创建全局资源，例如：初始化数据库连接。
    }

    @Before
    public void setup() {
        System.out.println("@Before：在每个测试方法之前被执行");
    }

    @After
    public void tearDown() {
        System.out.println("@After：在每个测试方法之后被执行");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("@AfterClass：在测试类中的所有方法执行完毕时被执行");
        // 此处用于释放全局资源，例如：清理数据库连接。
    }

    @Test
    public void testFunction01() {
        System.out.println("@Test：测试方法01");
    }

    @Test
    public void testFunction02() {
        System.out.println("@Test：测试方法02");
    }
}
```

此时运行示例程序，并查看控制台输出信息：

```text
@BeforeClass：在测试类首次加载时被执行
@Before：在每个测试方法之前被执行
@Test：测试方法01
@After：在每个测试方法之后被执行
@Before：在每个测试方法之前被执行
@Test：测试方法02
@After：在每个测试方法之后被执行
@AfterClass：在测试类中的所有方法执行完毕时被执行
```

> 🚩 提示
>
> 我们应当确保每个生命周期注解在所属测试类中仅出现一次，假如我们定义多个 `@Before` 方法，JUnit不能保证它们的执行顺序。


# JUnit 5
## 简介
JUnit 5基于模块化思想重新设计，除了支持JUnit 4的现有功能之外，还提供了一些扩展功能。

JUnit 5分为以下模块：

- `JUnit Platform` : JUnit 5平台，包括API定义、启动器、构建工具集成模块等。
- `JUnit Jupiter` : Jupiter引擎，用于运行JUnit 5的测试代码。
- `JUnit Vintage` : 兼容模块，能够在JUnit 5中运行JUnit 4及更早版本的测试代码。

JUnit 4与JUnit 5的部分类、注解名称相同，我们可以通过所在包区分它们，例如： `org.junit.Test` 是JUnit 4的 `@Test` 注解，而 `org.junit.jupiter.api.Test` 是JUnit 5的 `@Test` 注解

## 环境配置
若要使用JUnit 5，我们首先需要在构建工具中声明相关依赖并添加一些配置。

如果构建系统为Maven，我们可以使用下文代码块中的语句声明依赖：

"pom.xml":

```xml
<!-- Jupiter（JUnit5引擎的实现，将会自动引入Platform。） -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.12.1</version>
    <scope>test</scope>
</dependency>

<!-- Vintage（JUnit4及更早版本的兼容模块，可以运行JUnit4以及更早版本的测试代码，可选。） -->
<dependency>
    <groupId>org.junit.vintage</groupId>
    <artifactId>junit-vintage-engine</artifactId>
    <version>5.12.1</version>
    <scope>test</scope>
</dependency>
```

如果构建系统为Gradle，我们可以使用下文代码块中的语句声明依赖：

"build.gradle":

```groovy
test {
    // Gradle默认只能识别JUnit4，该语句可以使其支持JUnit5。
    useJUnitPlatform()
}

dependencies {
    // Jupiter（JUnit5引擎的实现，将会自动引入Platform。）
    testImplementation 'org.junit.jupiter:junit-jupiter:5.12.1'
    // Vintage（JUnit4及更早版本的兼容模块，可以运行JUnit4以及更早版本的测试代码，可选。）
    testImplementation 'org.junit.vintage:junit-vintage-engine:5.12.1'
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
tasks.withType<Test> {
    // Gradle默认只能识别JUnit4，该语句可以使其支持JUnit5。
    useJUnitPlatform()
}

dependencies {
    // Jupiter（JUnit5引擎的实现，将会自动引入Platform。）
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    // Vintage（JUnit4及更早版本的兼容模块，可以运行JUnit4以及更早版本的测试代码，可选。）
    testImplementation("org.junit.vintage:junit-vintage-engine:5.12.1")
}
```

当我们引入 `junit-jupiter` 组件后，构建系统也会级联引入 `junit-jupiter-engine` 、 `junit-platform-launcher` 等组件，此时我们就可以使用JUnit 5编写测试代码了。

上述配置是官方提供的简化方式，在Maven项目中确实可以正常工作，但在Gradle项目中无法完全正常工作，我们只能在IDE中直接运行测试代码，无法在Gradle命令行运行测试任务。

若要使用Gradle命令行运行测试任务，我们必须使用BOM明确配置 `junit-jupiter-engine` 与 `junit-platform-launcher` 的版本，不可使用 `junit-jupiter` 。

"build.gradle":

```groovy
dependencies {
    // JUnit5 BOM版本配置文件
    testImplementation platform('org.junit:junit-bom:5.12.1')
    // JUnit5 平台启动器
    testImplementation 'org.junit.platform:junit-platform-launcher'
    // Jupiter（JUnit5引擎的实现）
    testImplementation 'org.junit.jupiter:junit-jupiter-engine'
    // Vintage（JUnit4及更早版本的兼容模块，可以运行JUnit4以及更早版本的测试代码，可选。）
    testImplementation 'org.junit.vintage:junit-vintage-engine'
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
dependencies {
    // JUnit5 BOM版本配置文件
    testImplementation(platform("org.junit:junit-bom:5.12.1"))
    // JUnit5 平台启动器
    testImplementation("org.junit.platform:junit-platform-launcher")
    // Jupiter（JUnit5引擎的实现）
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    // Vintage（JUnit4及更早版本的兼容模块，可以运行JUnit4以及更早版本的测试代码，可选。）
    testImplementation("org.junit.vintage:junit-vintage-engine")
}
```

## 基本应用
JUnit 4与JUnit 5提供的注解对比如下文表格所示：

<div align="center">

|    JUnit 4     |    JUnit 5    |                用途                |
| :------------: | :-----------: | :--------------------------------: |
|    `@Test`     |    `@Test`    |           声明测试方法。           |
|   `@Ignore`    |  `@Disabled`  |           停用测试方法。           |
| `@BeforeClass` | `@BeforeAll`  | 生命周期：在所有测试方法之前执行。 |
| `@AfterClass`  |  `@AfterAll`  | 生命周期：在所有测试方法之后执行。 |
|   `@Before`    | `@BeforeEach` | 生命周期：在每个测试方法之前执行。 |
|    `@After`    | `@AfterEach`  | 生命周期：在每个测试方法之后执行。 |

</div>

除了上述注解之外，JUnit 5还提供了一些新的API：

- `@Test` : JUnit 5中的该注解不能设置任何属性，若要检测用例耗时，我们可以添加 `@Timeout(<超时时长（毫秒）>)` 注解；若要检测异常，我们可以使用 `assertThrows()` 断言方法。
- `@DisplayName("<显示名称>")` : 该注解需要添加在测试方法上，可以将自定义内容显示在测试报告上，提高可读性。
- `org.junit.jupiter.api.Assertions.*` : JUnit 5提供的断言库，使用方法与JUnit 4中的Assert断言库相同，但 `message` 参数从第一参数移动至最后一个参数。
