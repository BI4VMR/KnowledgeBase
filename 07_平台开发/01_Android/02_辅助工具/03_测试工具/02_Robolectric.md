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


# 实用技巧
## 配置Maven镜像
Robolectric会在测试用例执行前从Maven中心仓库下载其所需的环境模拟组件，这些文件体积较大，网络环境不佳时会导致运行困难。为了适应多种网络环境，Robolectric提供了一些配置项，详细信息可参考官方论坛： [🔗 Robolectric Blog - 改进下载速度](https://robolectric.org/blog/2023/11/11/improving-android-all-downloading/) 。

Robolectric的环境模拟组件存储在本地Maven仓库中，默认路径为： `~/.m2/repository/org/robolectric/android-all-instrumented/` ，而不是Gradle缓存目录，因此我们在Gradle配置文件中所设置的仓库镜像不会生效。我们可以从其他设备上复制已有的环境模拟组件到目标设备，保持相同的目录结构即可生效。

除了直接复制文件之外，我们还可以通过配置项使Robolectric从Maven镜像服务器下载组件，或者通过代理服务器访问网络，以下两种方式通常只需配置一种即可：

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

知名的Maven镜像仓库及地址可参考相关章节： [🧭 Gradle - 镜像仓库](../../../../04_软件技巧/06_软件开发/02_编译构建/02_Gradle/04_依赖管理.md#镜像仓库) 。

## 离线模式
不再使用远程仓库，直接使用指定目录中的android-all-instrumented文件，适用于无法联网的服务器环境。


central.sonatype.com
android-all-instrumented
Versions Tab列出了所有版本

16-robolectric-13921718-i7	2025-08-16	0	0	

16-robolectric-13785805-i7	2025-08-05	0	0	

15-robolectric-13954326-i7	2025-08-23	0	0	

14-robolectric-10818077-i7

版本号开头的数字对应Android版本，后缀对应小版本，通常选择每个Android版本的最新一个即可

通常需要测试用例指明的版本与当前最新的版本


## 私有API支持
原版运行环境只支持标准API，如果需要一些自定义的API，有两种方法：

新增API与SDK未重名：例如新增了VehicleConfigManager，标准SDK里没有这个类，如果是JAR引入可以直接testImpletion打包到测试组件，或者在测试set中创建空实现。

新增API与SDK重名：需要将Robolector改为离线模式，并修改运行环境，解压、将新增API的Class合并，替换原有CLass，再使用jar cf aaa.jar ./ 命令或压缩软件（关闭压缩）重新打JAR包。

首先以在线模式运行一次，使得Robo下载原版运行环境组件。

/home/bi4vmr/.m2/repository/org/robolectric/android-all-instrumented/13-robolectric-9030017-i7/

使用jar或压缩工具解压，添加、删除或替换要修改的Class文件。

重新打包并放在offline目录
jar cf android-all-instrumented-13-robolectric-9030017-i7.jar -C android-all-instrumented-13-robolectric-9030017-i7/ ./


test sourceset不会打包，所以其中的类无法加入运行环境，我们可以单独建立模块，将编译后的aar作为testRuntimeonly使用。


# 疑难解答
## 索引

<div align="center">

|       序号        |                                摘要                                |
| :---------------: | :----------------------------------------------------------------: |
| [案例一](#案例一) |          在本地测试中，构造ComponentName实例得到了空值。           |
| [案例二](#案例二) | 在Robolectric环境中，Application代码出现异常导致测试用例运行失败。 |
| [案例三](#案例三) |   在Robolectric环境中，测试用例运行时出现NoSuchMethodError异常。   |
| [案例四](#案例四) |   在Robolectric环境中，测试用例运行时出现ClassFormatError异常。    |

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

## 案例二
### 问题描述
在Robolectric环境中，Application代码出现异常导致测试用例运行失败。

```text
net.bi4vmr.Test > test FAILED
    java.lang.NullPointerException
        at java.base/java.io.File.<init>(File.java:278)
        at com.facebook.soloader.ApplicationSoSource.<init>(ApplicationSoSource.java:45)
        at com.facebook.soloader.SoLoader.initSoSources(SoLoader.java:273)
        at com.facebook.soloader.SoLoader.init(SoLoader.java:193)
        at com.facebook.soloader.SoLoader.init(SoLoader.java:175)
        at net.bi4vmr.MyApp.setupProfilo(LauncherApp.kt:109)
        at net.bi4vmr.MyApp.onCreate(LauncherApp.kt:55)
        at android.app.Instrumentation.callApplicationOnCreate(Instrumentation.java:1266)
```

### 问题分析
我们根据堆栈信息查看ApplicationSoSource类的源码，发现其构造方法通过 `applicationContext.getApplicationInfo().nativeLibraryDir` 语句获取路径并构造File实例，然而File类要求路径参数不可为空，Robolectric环境中 `nativeLibraryDir` 属性是空值，因此出现了NullPointerException异常。

"ApplicationSoSource.class":

```java
public ApplicationSoSource(Context context, int flags) {
    this.applicationContext = context.getApplicationContext();

    // 此处已省略部分代码...

    this.soSource = new DirectorySoSource(new File(this.applicationContext.getApplicationInfo().nativeLibraryDir), flags);
}
```

### 解决方案
我们首先尝试在测试代码初始化时将该属性修改为非空的值，解决运行时出现的NPE。

"Test.kt":

```kotlin
@Before
fun setup() {
    mockApplication = RuntimeEnvironment.getApplication()
    mockApplication.applicationInfo.nativeLibraryDir = "/"
}
```

实测此方法不可行，因为Robolectric运行时将会首先执行Application类的代码，再执行测试代码，初始化阶段已经出现了NPE导致测试任务终止。

Robolectric环境中 `Build.FINGERPRINT` 属性的值固定为 `robolectric` ，因此我们可以在Application中添加判断语句，如果处于Robolectric环境中则忽略Profilo工具的初始化逻辑，避免NPE。

"MyApp.kt":

```kotlin
private fun setupProfilo() {
    if (Build.FINGERPRINT == "robolectric") {
        println("Run in Java unit test environment, skip init Profilo!")
        return
    }

    // Profilo工具的初始化代码...
}
```

## 案例三
### 问题描述
在Robolectric环境中，测试用例运行时出现NoSuchMethodError异常，错误消息如下文代码块所示：

```text
Test > testOperations FAILED
    java.lang.NoSuchMethodError: 'android.car.hardware.property.VehicleConfigHelper android.car.hardware.property.VehicleConfigHelper.getInstance(android.content.Context)'
```

### 问题分析
Robolectric模拟环境只提供了标准的Android系统API，在本案例中，测试用例引入的某个库调用了非标准的VehicleConfigHelper API，因此出现了NoSuchMethodError异常。

### 解决方案
如果我们能够获取非标准API所在的JAR文件，可以将其作为测试模块的依赖，使运行环境拥有相应的API。

如果我们无法获取对应的JAR文件，也可以在测试模块中创建同名类并编写空的方法，确保运行时不会出现异常。

```kotlin
package android.car.hardware.property

class VehicleConfigHelper {

    companion object {

        @JvmStatic
        fun getInstance(context: Context): VehicleConfigHelper {
            println("FakeAPI-VehicleConfigHelper#getInstance($context)")
            return VehicleConfigHelper()
        }
    }
}
```



## 案例四
### 问题描述
在Robolectric环境中，测试用例运行时出现ClassFormatError异常，错误消息如下文代码块所示：

```text
Absent Code attribute in method that is not native or abstract in class file android/car/CarProjectionManager
java.lang.ClassFormatError: Absent Code attribute in method that is not native or abstract in class file android/car/CarProjectionManager
	at java.base/java.lang.ClassLoader.defineClass1(Native Method)
	at java.base/java.lang.ClassLoader.defineClass(ClassLoader.java:1017)
	at java.base/java.security.SecureClassLoader.defineClass(SecureClassLoader.java:150)
	at java.base/jdk.internal.loader.BuiltinClassLoader.defineClass(BuiltinClassLoader.java:862)
	at java.base/jdk.internal.loader.BuiltinClassLoader.findClassOnClassPathOrNull(BuiltinClassLoader.java:760)
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClassOrNull(BuiltinClassLoader.java:681)
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:639)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:525)
```

### 问题分析
该类是通过Jar包引入的，其中构造方法没有调用 `super()` 、 普通方法没有返回值，因为Jar已被编译为字节码，因此引用它们时不会再次编译，编译器也不会提示我们错误，直到运行时缺少部分指令才会抛出异常。

```java
public final class CarProjectionManager extends CarManagerBase {

    public CarProjectionManager(Car car, IBinder service) {
        // 此处应当有 `super(car)` 语句
    }

    public boolean requestBluetoothProfileInhibit(BluetoothDevice device, int profile) {
        // 此处应当有 `return` 语句
    }

    public boolean releaseBluetoothProfileInhibit(BluetoothDevice device, int profile) {
        // 此处应当有 `return` 语句
    }
}
```

### 解决方案
如果能够获取Jar包源码，则可以从源头上修复，修正代码更新Jar包即可。

如果无法修改Jar包，可以修改运行环境。
