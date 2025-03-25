# 简介
<!-- TODO

Mockito是一个针对Java语言的模拟工具，即使有KT扩展，对于Kotlin的语法支持仍然有限，因此更建议使用MockK。

-->

# 基本应用





# 行为验证



# 参数捕获器

前文章节中，我们通过verify方法对待测方法的参数进行验证，但仅限首次调用时，且匹配器只能匹配预设的类型，对于复杂条件无法处理，此时我们可以使用参数捕获器。

常见的场景：

参数为引用类型的回调方法： result(a: List<String>) 我们希望验证列表长度为5，匹配器无法处理这种场景。
参数为引用类型的普通方法：proc(a: SS) 该方法会将变量的值进行修改，我们希望在方法结束后校验参数值的变化。

🔴 示例一：使用参数捕获器验证回调方法。

在本示例中，我们。

第一步，我们在构建系统中声明对于JUnit4组件的依赖。

```kotlin
interface FileCallback {

    /**
     * 回调方法。
     *
     * @param result  执行结果。
     * @param message 消息。
     */
    fun onResult(result: Boolean, message: String)
}
```

"FileHelper.kt":

```kotlin
class FileHelper {

    fun saveFile(path: String, callback: FileCallback) {
        try {
            File(path).createNewFile()
            callback.onResult(true, "OK!")
        } catch (e: Exception) {
            callback.onResult(false, "Error!")
            System.err.println("Save file failed! Reason:[${e.message}]")
        }
    }
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
