# 简介
AndroidTest需要在模拟器或真机环境下运行，速度比较缓慢，有时我们希望针对业务逻辑进行测试，不关心与设备强关联的接口，此时可以采用Robolectric工具。

Robolectric通过一系列JAR包模拟Android SDK中的接口，使得应用程序组件可以在标准的JVM中运行，不再依赖Android的JVM，因此我们可以使用JUnit等Java领域的工具测试Android业务代码以及简单的UI逻辑。


https://developer.android.google.cn/training/testing/local-tests/robolectric?hl=zh-cn

    // Robolectric
    testOptions {
        // 加载Android资源
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("robolectric.logging.enabled", true)
        }
    }





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