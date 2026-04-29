
Java在1.8中加入了接口的默认实现，只要在接口方法前加一个default关键字，实现方法体即可。
而Koltin中接口默认实现则更加简单，直接实现方法体即可。但是Java类如果要实现Kotlin带默认实现的接口你会发现默认实现失效了？怎么解决？

使用关键字@JvmDefault并在build.gradle文件中进行以下配置。






<!-- 

@JvmField可以消除backing field的get/set，让其在java侧向var一样被访问：


    name : String -> getName()/setName()
    isName: boolean -> isName()/setName()

但是像hasName这样的属性，就没那么智能了，会翻译成getHasName()。

如何指定成hasName()呢？使用@JvmName

val hasName @JvmName("hasName") get() = mNames.isNotEmpty()


也可以针对get/set选择性的指定

@get:JvmName("hasName") val hasName get() = 
	mNames.isNotEmpty()


@JvmOverloads

e: file:///C:/Users/bi4vmr/Project/BaseLib/BaseLib-Android/lib_ui/universal/src/main/kotlin/net/bi4vmr/tool/android/ui/universal/base/BaseFragment.kt:20:1 Platform declaration clash: The following declarations have the same JVM signature (openActivity(Ljava/lang/Class;)V):
    fun openActivity(clazz: Class<*>): Unit defined in net.bi4vmr.tool.android.ui.universal.base.BaseFragment
    fun openActivity(clazz: Class<*>): Unit defined in net.bi4vmr.tool.android.ui.universal.base.BaseFragment

@JvmOverloads
fun openActivity(clazz: Class<*>, intent: Intent? = null, options: Bundle? = null) {
    ...
}

@JvmOverloads
fun openActivity(
        clazz: Class<*>,
        configIntent: Intent.() -> Unit = {},
        configOptions: Bundle.() -> Unit = {}
)

可以为其中一个方法指定独立的JVM方法名称，避免冲突。
@JvmName("openActivityKT")




对java声明抛出异常

@Throws(IOException::class)
  fun send(event: Event)



@JvmSynthetic
java看不到这种方法
可以屏蔽Internal方法
-->
