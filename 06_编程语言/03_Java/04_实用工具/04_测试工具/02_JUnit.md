# 简介
<!-- TODO

JUnit 是一个编写可重复测试的简单框架。它是单元测试框架的 xUnit 架构的一个实例。

Junit 官网：http://junit.org/

目前JUnit4和JUnit5都是被广泛应用的版本


-->

本章的示例工程详见以下链接：

- [🔗 示例工程：JUnit4](https://github.com/BI4VMR/Study-Java/tree/master/M04_Utils/C04_Test/S02_JUnit4)
- [🔗 示例工程：JUnit5](https://github.com/BI4VMR/Study-Java/tree/master/M04_Utils/C04_Test/S03_JUnit5)


# 基本应用
下文示例展示了JUnit4的基本使用方法：

🔴 示例一：构建JUnit4环境。

在本示例中，我们将为示例工程添加JUnit4支持。

第一步，我们在构建系统中声明对于JUnit4组件的依赖。

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

第二步，编写业务代码。

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

第三步，编写测试代码。

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


# 条件判断
<!-- TODO

断言方法检测预期与实际值是否相同，不相同则抛出 `AssertionError` 异常。
例如上文的 `assertEquals(s ,b)` ，系统将会比较a与b是否相等，若相等则不做任何处理，继续执行后续代码，若不相等则抛出异常提示用户。


assertArrayEquals(expecteds, actuals)	查看两个数组是否相等。
assertEquals(expected, actual)	查看两个对象是否相等。类似于字符串比较使用的equals()方法。
assertNotEquals(first, second)	查看两个对象是否不相等。
assertNull(object)	查看对象是否为空。
assertNotNull(object)	查看对象是否不为空。
assertSame(expected, actual)	查看两个对象的引用是否相等。类似于使用“==”比较两个对象。
assertNotSame(unexpected, actual)	查看两个对象的引用是否不相等。类似于使用“!=”比较两个对象。
assertTrue(condition)	查看运行结果是否为true。
assertFalse(condition)	查看运行结果是否为false。
assertThat(actual, matcher)	查看实际值是否满足指定的条件。
fail()	让测试失败。




## 其他功能
@Test：	标识一条测试用例。 (A) (expected=XXEception.class)   (B) (timeout=xxx)
@Ignore: 	忽略的测试用例。表来标识该用例跳过，不管用例运行成功还是失败。
要在 JUnit5 中禁用测试，您将需要使用@Disabled注解。 它等效于 JUnit4 的@Ignored注解。

@Disabled注解可以应用于测试类（禁用该类中的所有测试方法）或单个测试方法。




-->

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

🔴 示例一：观察JUnit4生命周期方法的执行顺序。

在本示例中，我们在JUnit4的方法中输出日志，并观察执行顺序。

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


# JUnit5
<!-- TODO
不像以往的版本，JUnit 5 现在是三个模块的合体 JUnit 5 = JUnit Platform + JUnit Jupiter + JUnit Vintage

    JUnit Platform: 运行测试的基础平台。还定义了开发测试框架的 TestEngine API。并提供了命令行执行测试以及与 Gradle, Maven, JUnit4 Runner 的集成
    JUnit Jupiter: 包含了新的编程和扩展模型。它还提供了一个运行新型测试的 TestEngine 实现
    JUnit Vintage: 提供了一个让 JUnit Platform 运行 JUnit 3 和 JUnit 4 的 TestEngine 实现

注意：是 JUnit 4 测试类还是 JUnit 5 测试类，关键看注解 @Test 是来自于哪个包，比如说

    @Test 是 org.junit.Test，那么它是老的 JUnit 4 的测试类(也可能是 JUnit 3 的)
    @Test 是 org.junit.jpiter.api.Test, 那么它是 JUnit 5 的测试类


-->
