# 简介
<!-- TODO

基于dagger
对于android场景化，更方便

-->


# 基本应用
下文示例展示了Hilt的基本使用方法：

🔴 示例一：Hilt的基本应用。

在本示例中，我们通过Hilt管理业务组件实例，并调用其中的业务方法。

第一步，我们为当前模块引入Hilt的依赖组件：

"build.gradle":

```groovy
dependencies {
    // Hilt核心
    implementation 'com.google.dagger:hilt-android:2.51.1'

    // Hilt注解处理器(Java)
    annotationProcessor 'com.google.dagger:hilt-compiler:2.51.1'
    // Hilt注解处理器(Kotlin-KAPT)
    kapt 'com.google.dagger:hilt-compiler:2.51.1'
    // Hilt注解处理器(Kotlin-KSP)
    ksp 'com.google.dagger:hilt-compiler:2.51.1'
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
dependencies {
    // Hilt核心
    implementation("com.google.dagger:hilt-android:2.51.1")

    // Hilt注解处理器(Java)
    annotationProcessor("com.google.dagger:hilt-compiler:2.51.1")
    // Hilt注解处理器(Kotlin-KAPT)
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    // Hilt注解处理器(Kotlin-KSP)
    ksp("com.google.dagger:hilt-compiler:2.51.1")
}
```

`hilt-runtime` 是Hilt的核心组件， `hilt-compiler` 是Hilt的注解处理器，一个应用程序至少需要引入这些组件才能使用Hilt框架。上述的三条注解处理器声明语句不可同时声明，我们需要根据项目所使用的语言进行选择。

第二步，我们在自定义Application类上添加 `@HiltAndroidApp` 注解。

"MyApplication.java":

```java
@HiltAndroidApp
public class MyApplication extends Application {
}
```

上述内容也可以使用Kotlin语言编写：

"MyApplicationKT.kt":

```kotlin
@HiltAndroidApp
class MyApplicationKT : Application()
```

`@HiltAndroidApp` 注解是Hilt相关功能的全局开关，只有正确配置自定义Application与该注解后我们才能正常使用Hilt。

第三步，我们使用依赖注入风格编写业务组件。

"HTTPManager.java":

```java
public class HTTPManager {

    private final Context context;

    // 构造方法
    @Inject
    public HTTPManager(
            // 声明依赖项
            @ApplicationContext Context context
    ) {
        this.context = context;
    }

    // 业务接口：登录
    public void login() {
        Log.i("HTTPManager", "Login. App:[" + context.getPackageName() + "]");
    }
}
```

上述内容也可以使用Kotlin语言编写：

"HTTPManagerKT.kt":

```kotlin
class HTTPManagerKT @Inject constructor(

    // 声明依赖项
    @param:ApplicationContext private val context: Context
) {

    // 业务接口：登录
    fun login() {
        Log.i("HTTPManager", "Login. App:[$context]")
    }
}
```

`@ApplicationContext` 表示使用应用级Context，即带有 `@HiltAndroidApp` 注解的Application实例。

为了实现依赖注入，我们将业务组件所依赖的外部组件作为构造方法参数，在程序运行时由Hilt等工具传入依赖组件并实例化当前组件，在测试环境中我们可以传入依赖组件的Mock对象以便进行测试。

我们需要在构造方法上添加 `@Inject` 注解，标记该类可被Hilt管理。当我们在其他组件中引入HTTPManager时，Hilt会递归解析所有构造方法参数并创建它们的实例，集齐所有依赖组件实例后，再调用HTTPManager的构造方法创建其实例，随后我们就可以调用其中的业务方法了。

以此处的Context参数为例，Hilt解析依赖时，发现我们添加了 `@ApplicationContext` 注解，表示此处需要应用级别的Context实例，该实例即应用进程初始化时创建的MyApplication实例，无需重复创建，因此直接传入MyApplication实例，完成HTTPManager的实例化。

第四步，我们在Activity中引入业务组件实例，并调用其中的业务方法。

"TestUIBase.java":

```java
@AndroidEntryPoint
public class TestUIBase extends AppCompatActivity {

    // 业务组件实例
    @Inject
    HTTPManager httpManager;

    private TestuiBaseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TestuiBaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> {
            // 调用业务方法
            httpManager.login();
        });
    }
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIBaseKT.kt":

```kotlin
@AndroidEntryPoint
class TestUIBaseKT : AppCompatActivity() {

    // 业务组件实例
    @Inject
    lateinit var httpManager: HTTPManagerKT

    private val binding: TestuiBaseBinding by lazy {
        TestuiBaseBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { 
            // 调用业务方法
            httpManager.login()
        }
    }
}
```

Activity、Service等组件由系统控制实例的创建，因此我们不能通过构造方法声明依赖组件，而是在类上添加 `@AndroidEntryPoint` 注解，并将依赖组件声明为非私有成员变量，Hilt会生成该类的子类，并在运行时通过子类注入依赖实例。

此时运行示例程序，并点击 `btnLogin` 按钮，控制台应当能够输出对应的日志消息，说明Hilt已经成功为示例Activity注入了HTTPManager实例，通过按钮调用其中的业务方法也能够工作。
