# 简介
Mockito是一个在Java语言中被广泛使用的Mock工具，我们可以用它创建Mock对象，为待测接口提供模拟数据，隔离单元测试等场景中的依赖项。

Mockito的早期版本通过继承实现Mock，不支持模拟Final类与静态方法，我们可以使用 `mockito-inline` 和PowerMock等工具进行操作。自从3.4.0版本开始，Mockito新增了通过Byte Buddy修改字节码模拟Final类与静态方法的功能，我们不必再使用其他工具实现此类功能。

本章的示例工程详见以下链接：

- [🔗 示例工程：Mockito](https://github.com/BI4VMR/Study-Java/tree/master/M04_Utils/C04_Test/S04_Mockito)


# 基本应用
## 依赖隔离
Mock工具的基本用途是模拟待测接口的周边接口，提供假数据与行为，使我们专注于待测接口本身，不必关心外围环境的变化。

🔴 示例一：模拟待测接口的依赖项。

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

我们的目标是测试UserManager中的接口是否正常，并不关心DBHelper连接何种数据库、用户信息表具有何种结构，因此我们需要创建DBHelper类的Mock对象，给UserManager提供预设的用户信息。

"UserManagerTest.java":

```java
@Test
public void testGetUserNames() {
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
    try {
        Field field = manager.getClass().getDeclaredField("mDBHelper");
        field.setAccessible(true);
        field.set(manager, mockDBHelper);
    } catch (Exception e) {
        e.printStackTrace();
    }

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

接着，我们创建了待测接口所在类UserManager的对象，并通过反射将Mock对象注入到UserManager中。

此时运行示例程序，并查看控制台输出信息：

```text
Index:[0] Name:[来宾账户]
Index:[1] Name:[用户A]
Index:[2] Name:[用户B]
```

根据上述输出内容可知：

UserManager调用DBHelper模拟对象的接口后，输出内容确实为测试代码提供的模拟数据，并非真实的DBHelper接口数据，说明Mock操作成功。

## 常用注解
在前文示例中，我们使用 `Mockito.mock()` 方法创建了一些Mock对象；如果需要Mock的对象数量较多，我们也可以使用Mockito提供的注解。

🟠 示例二：使用注解创建Mock对象。

在本示例中，我们以JUnit 4平台为例，使用Mockito提供的注解创建Mock对象。

"AnnotationTest.java":

```java
public class AnnotationTest {

    private AutoCloseable mockResources;

    @Mock
    DBHelper mockDBHelper;

    @Before
    public void setup() {
        // 若要使用Mockito注解，需要在执行其他操作前先初始化。
        mockResources = MockitoAnnotations.openMocks(this);
    }

    @After
    public void teardown() throws Exception {
        // 撤销所有Mock
        mockResources.close();
    }

    @Test
    public void testGetUserNames() {
        // 在此处使用Mock对象
    }
}
```

`@Mock` 注解表示被修饰的全局变量是Mock对象，程序运行时Mockito会根据变量类型自动创建对象并完成赋值。

上述注解默认没有任何效果，为了使它们生效，我们需要在测试代码执行前调用 `MockitoAnnotations.openMocks(this)` 方法；测试代码执行完毕后，我们还应该对前述方法返回的对象调用 `close()` 方法撤销所有Mock行为。

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

<!-- TODO

# 行为定义
Mock对象默认没有任何行为，只会返回空值或默认值给调用者。


val mockedList = mock(mutableListOf<String>().javaClass)
// mockedList[0] 第一次返回 first，之后都会抛出异常
`when`(mockedList[0]).thenReturn("first")
        .thenThrow(IllegalArgumentException())
`when`(mockedList[1]).thenThrow(RuntimeException())
`when`(mockedList.set(anyInt(), anyString())).thenAnswer({ invocation ->
    val args = invocation.arguments
    println("set index ${args[0]} to ${args[1]}")
    args[1]
})

// use doThrow when stubbing void methods with exceptions
doThrow(RuntimeException()).`when`(mockedList).clear()
doReturn("third").`when`(mockedList)[2]

需要注意下 stubbing 方法的规则：

    一旦指定了方法的实现后，不管调用多少次，�该方法都是返回指定的返回值或者执行指定的方法

    当以相同的参数指定同一个方法多次时，最后一次指定才会生效

指定方法实现通常使用thenReturn、thenThrow、thenAnswer等，�因为这种方式更直观。�但是�上面的例子中还有doReturn、doThrow、doAnswer等 do 系列方法，它可以�实现 then 系列方法同样的功能，不过在阅读上没有那么直观。�在下面几种情况下必须使用 do 系列方法：

    指定 void 方法

    指定 spy 对象的某些方法时

    �多次指定同一方法，以便在测试中途修改方法实现

其中第二条值得注意，当�使用 then 系列方法，spy 对象的实际方法其实还是会被调用的，然后才执行指定的实现，所以有时使用 then 系列方法会产生异常，这时只能使用 do 系列方法（它会覆盖实际方法实现）。看下面这个例子：

val realList = mutableListOf<String>()
val spyList = spy(realList)
 
// stubbing success
`when`(spyList.size).thenReturn(5)
 
//Impossible: real method is called so spy.get(0) throws IndexOutOfBoundsException (the list is yet empty)
`when`(spyList[0]).thenReturn("first")
 
//You have to use doReturn() for stubbing
doReturn("first").`when`(spyList)[0]

# 行为验证

一旦mock对象被创建了，mock对象会记住所有的交互，然后你就可以选择性的验证你感兴趣的交互，验证不通过则抛出异常。

@Test
public void test1() {
    final List mockList = Mockito.mock(List.class);
    mockList.add("mock1");
    mockList.get(0);
    mockList.size();
    mockList.clear();
    // 验证方法被使用（默认1次）
    Mockito.verify(mockList).add("mock1");
    // 验证方法被使用1次
    Mockito.verify(mockList, Mockito.times(1)).get(0);
    // 验证方法至少被使用1次
    Mockito.verify(mockList, Mockito.atLeast(1)).size();
    // 验证方法没有被使用
    Mockito.verify(mockList, Mockito.never()).contains("mock2");
    // 验证方法至多被使用5次
    Mockito.verify(mockList, Mockito.atMost(5)).clear();
    // 指定方法调用超时时间
    Mockito.verify(mockList, timeout(100)).get(0);
    // 指定时间内需要完成的次数
    Mockito.verify(mockList, timeout(200).atLeastOnce()).size();
}

验证参数值

上面的验证方法调用时，对于参数的校验使用的默认 equals 方法，除此之外也可以使用 argument matchers：

    verify(mockedList).add(anyString())
    verify(mockedList).add(notNull())
    verify(mockedList).add(argThat{ argument -> argument.length > 5 })

# 参数捕获器
在某些场景中，不光要对方法的返回值和调用进行验证，同时需要验证一系列交互后所传入方法的参数。那么我们可以用参数捕获器来捕获传入方法的参数进行验证，看它是否符合我们的要求。

ArgumentCaptor介绍

通过ArgumentCaptor对象的forClass(Class

ArgumentCaptor的Api

argument.capture() 捕获方法参数

argument.getValue() 获取方法参数值，如果方法进行了多次调用，它将返回最后一个参数值

argument.getAllValues() 方法进行多次调用后，返回多个参数值

@Test
public void test9() {
    List mock = mock(List.class);
    List mock1 = mock(List.class);
    mock.add("John");
    mock1.add("Brian");
    mock1.add("Jim");
    // 获取方法参数
    ArgumentCaptor argument = ArgumentCaptor.forClass(String.class);
    verify(mock).add(argument.capture());
    System.out.println(argument.getValue());    //John
 
    // 多次调用获取最后一次
    ArgumentCaptor argument1 = ArgumentCaptor.forClass(String.class);
    verify(mock1, times(2)).add(argument1.capture());
    System.out.println(argument1.getValue());    //Jim
 
    // 获取所有调用参数
    System.out.println(argument1.getAllValues());    //[Brian, Jim]
}

@Mock
private List<String> captorList;
@Captor
private ArgumentCaptor<String> argumentCaptor;
@Test
public void test10() {
    MockitoAnnotations.initMocks(this);
    captorList.add("cap1");
    captorList.add("cap2");
    System.out.println(captorList.size());
    verify(captorList, atLeastOnce()).add(argumentCaptor.capture());
    System.out.println(argumentCaptor.getAllValues());
}


# mock 和 spy

创建 mock 对象是 Mockito 框架生效的基础，有两种方式 mock 和 spy。mock 对象的属性和方法都是默认的，例如返回 null、默认原始数据类型值（0 对于 int/Integer）或者空的集合，简单来说只有类的空壳子。而spy 对象的方法是真实的方法，不过会额外记录方法调用信息，所以也可以验证方法调用。

-->
