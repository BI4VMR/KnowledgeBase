# 简介
Android Test提供了Android SDK环境支持，但其中的测试代码需要在模拟器或真机环境下运行，速度比较缓慢，有时我们希望针对业务逻辑进行测试，不关心与设备强关联的接口，此时可以采用Robolectric工具。

Robolectric通过一系列JAR包模拟Android SDK中的接口，使得应用程序组件可以在标准的JVM中运行，不再依赖Android的JVM，因此我们可以使用JUnit等Java领域的工具测试Android业务代码以及简单的UI逻辑。


本章的相关知识可以参考以下链接：

- [🔗 Robolectric - 官方网站](https://robolectric.org/)
- [🔗 Robolectric - Android开发文档](https://developer.android.com/training/testing/local-tests/robolectric?hl=zh-cn)

本章的示例工程详见以下链接：

- [🔗 示例工程：Robolectric](https://github.com/BI4VMR/Study-Android/tree/master/M02_Tool/C03_Test/S02_Robolectric)





# 基本应用
下文示例展示了Robolectric工具的基本使用方法。

🔴 示例一：Robolectric工具的基本应用。

在本示例中，我们构建Robolectric运行环境，然后访问Android资源。

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
```

在默认配置中，Java单元测试的SourceSet无法访问Android资源，由于后续我们需要在单元测试中读取资源，此处应当在 `unitTests {}` 小节中添加 `includeAndroidResources = true` 配置项。

第二步，我们

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class TestBase {

    private lateinit var mockApplication: Application
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        /* 构建模拟环境 */
        // 获取Robolectric提供的模拟Application实例
        mockApplication = RuntimeEnvironment.getApplication()
        // 获取Context实例
        mockContext = mockApplication.applicationContext
    }

    @Test
    fun test_Env() {
        println("----- TestEnv start -----")

        // 调用Android的API，验证模拟环境是否正常。
        println("获取应用名称:[${mockContext.getString(R.string.app_name)}]")

        println("----- TestEnv end -----")
    }
}
```

