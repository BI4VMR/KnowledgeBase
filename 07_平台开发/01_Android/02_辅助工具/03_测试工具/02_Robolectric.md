# 简介
虽然仪器化测试环境提供了Android SDK支持，但是其中的测试代码需要在模拟器或实体机中运行，速度缓慢且消耗大量硬件资源。有时我们希望只针对业务逻辑进行测试，不关心与设备特性强关联的接口，此时可以选用Robolectric工具。

Robolectric通过一系列JAR包模拟Android SDK中的接口，使得应用程序可以在标准的JVM中运行，不再依赖Android特有的JVM。当项目配置了Robolectric之后，我们便可通过本地测试环境测试Android业务代码以及简单的UI逻辑。

本章的相关知识可以参考以下链接：

- [🔗 Robolectric - 官方网站](https://robolectric.org/)
- [🔗 Robolectric - Android开发文档](https://developer.android.com/training/testing/local-tests/robolectric?hl=zh-cn)

本章的示例工程详见以下链接：

- [🔗 示例工程：Robolectric](https://github.com/BI4VMR/Study-Android/tree/master/M02_Tool/C03_Test/S02_Robolectric)


# 基本应用
下文示例展示了Robolectric工具的基本使用方法。

🔴 示例一：Robolectric工具的基本应用。

在本示例中，我们构建Robolectric运行环境，然后访问Android中的文本资源。

第一步，我们为当前模块添加Robolectric依赖声明与相关配置项。

"build.gradle":

```groovy
android {
    testOptions {
        unitTests {
            // 为单元测试添加Android资源
            includeAndroidResources = true

            all {
                // Robolectric：启用日志输出
                systemProperty 'robolectric.logging.enabled', 'true'
            }
        }
    }
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
android {
    testOptions {
        unitTests {
            // 为单元测试添加Android资源
            isIncludeAndroidResources = true
        }

        unitTests.all {
            // Robolectric：启用日志输出
            it.systemProperty("robolectric.logging.enabled", true)
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    // 声明Robolectric依赖项
    testImplementation("org.robolectric:robolectric:4.14.1")
}
```

在默认配置中，本地测试所在的SourceSet无法访问Android资源，由于后续我们需要在本地测试中读取资源，此处应当在 `unitTests {}` 小节中添加 `includeAndroidResources = true` 配置项。

第二步，我们编写测试代码，获取Robolectric提供的模拟Application，并获取文本资源。

"TestBase.java":

```java
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.TIRAMISU})
public class TestBase {

    private Application mockApplication;
    private Context mockContext;

    @Before
    public void setup() {
        /* 构建模拟环境 */
        // 获取Robolectric提供的模拟Application实例
        mockApplication = RuntimeEnvironment.getApplication();
        // 获取Context实例
        mockContext = mockApplication.getApplicationContext();
    }

    @Test
    public void test_Env() {
        // 调用Android的API，验证模拟环境是否正常。
        System.out.println("获取应用名称:[" + mockContext.getString(R.string.app_name) + "]");
    }
}
```

上述内容也可以使用Kotlin语言编写：

"TestBaseKT.kt":

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class TestBase {

    private lateinit var mockApplication: Application
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        /* 构建模拟环境 */
        // 获取Robolectric提供的模拟Application实例
        mockApplication = RuntimeEnvironment.getApplication()
        // 获取Context实例
        mockContext = mockApplication.applicationContext
    }

    @Test
    fun test_Env() {
        // 调用Android的API，验证模拟环境是否正常。
        println("获取应用名称:[${mockContext.getString(R.string.app_name)}]")
    }
}
```

我们首先在测试类上添加 `@RunWith(RobolectricTestRunner::class)` 注解，使JUnit框架初始化Robolectric环境，然后添加 `@Config` 注解，通过 `sdk` 属性指明需要测试的Android SDK版本。

在初始化方法 `setup()` 中，我们可以调用 `RuntimeEnvironment.getApplication()` 获取Robolectric提供的模拟Application实例，然后就可以在测试代码中访问Android相关接口了。


<!-- TODO
## 实用技巧
Robolectric会在测试用例运行过程中下载 `@Config` 注解所指定SDK版本对应的模拟文件，这些文件体积较大，如果网络环境不佳可能会导致运行缓慢。

首先这些文件是存储在本地Maven仓库中的 `~/.m2/repository` ，不使用Gradle缓存目录，因此在项目的Gradle配置文件中添加镜像地址无效。

https://robolectric.org/blog/2023/11/11/improving-android-all-downloading/

我们可以通过SystemProperty指明Robolectric下载模拟文件所使用的仓库，或者配置代理。

[🧭 Gradle - 镜像仓库](../../../../04_软件技巧/06_软件开发/02_编译构建/02_Gradle/04_依赖管理.md#镜像仓库)

"build.gradle":

```groovy
android {
    testOptions {
        unitTests {
            all {
                // Robolectric：指定模拟API的JAR包下载仓库
                systemProperty 'robolectric.dependency.repo.id', '<仓库ID（随意设置即可）>'
                systemProperty 'robolectric.dependency.repo.url', '<服务器地址>'
                systemProperty 'robolectric.dependency.repo.username', '<用户名称>'
                systemProperty 'robolectric.dependency.repo.password', '<密码>'

                // Robolectric：指定代理服务器（需要Robolectric v4.9.1或更高版本。）
                systemProperty 'robolectric.dependency.proxy.host', System.getenv("ROBOLECTRIC_PROXY_HOST")
                systemProperty 'robolectric.dependency.proxy.port', System.getenv("ROBOLECTRIC_PROXY_PORT")
            }
        }
    }
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
android {
    testOptions {
        unitTests.all {
            // Robolectric：指定模拟API的JAR包下载仓库
            it.systemProperty("robolectric.dependency.repo.id", "<仓库ID（随意设置即可）>")
            it.systemProperty("robolectric.dependency.repo.url", "<服务器地址>")
            it.systemProperty("robolectric.dependency.repo.username", "<用户名称>")
            it.systemProperty("robolectric.dependency.repo.password", "<密码>")

            // Robolectric：指定代理服务器（需要Robolectric v4.9.1或更高版本。）
            it.systemProperty("robolectric.dependency.proxy.host", System.getenv("ROBOLECTRIC_PROXY_HOST"))
            it.systemProperty("robolectric.dependency.proxy.port", System.getenv("ROBOLECTRIC_PROXY_PORT"))
        }
    }
}
```

-->

# 疑难解答
## 索引

<div align="center">

|       序号        |                      摘要                       |
| :---------------: | :---------------------------------------------: |
| [案例一](#案例一) | 在本地测试中，构造ComponentName实例得到了空值。 |

</div>

## 案例一
### 问题描述
在本地测试中，构造ComponentName实例得到了空值。

```kotlin
@Test
fun test() {
    val cmpName = ComponentName("net.bi4vmr.test", "net.bi4vmr.test.MainActivity")
    println("ComponentName:[$cmpName]")
}
```

以常理推断，ComponentName的构造方法执行成功且没有错误， `cmpName` 变量是不可能为空值的，但上述代码运行时控制台输出内容为：

```text
ComponentName:[null]
```

### 问题分析
在非Robolectric环境中，若本地测试代码调用了Android SDK，但未设置任何Mock，则会出现 `RuntimeException: Method toString in android.content.ComponentName not mocked.` 等异常信息。

在本案例中，我们配置了Robolectric环境，但没有为当前测试类添加 `@RunWith(RobolectricTestRunner::class)` 注解，导致构造ComponentName实例的行为返回了空值，且Robolectric不会显示任何报错信息，对我们排查问题造成了误导。

### 解决方案
如果测试用例访问了Android SDK，一定要在类上添加 `@RunWith(RobolectricTestRunner::class)` 注解，切勿遗漏。

```kotlin
// 不要遗忘Robolectric初始化注解！
@RunWith(RobolectricTestRunner::class)
class TestBaseKT {

    @Test
    fun test() {
        val cmpName = ComponentName("net.bi4vmr.test", "net.bi4vmr.test.MainActivity")
        println("ComponentName:[$cmpName]")
    }
}
```
