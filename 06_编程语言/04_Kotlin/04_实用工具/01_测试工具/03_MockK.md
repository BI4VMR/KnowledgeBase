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

在本示例中，我们使用参数捕获器验证回调方法的参数是否符合预期。

第一步，编写业务代码。

我们首先定义一个接口， `onResult()` 方法用于回调事件。

"FileCallback.kt":

```kotlin
interface FileCallback {
    fun onResult(result: Boolean, message: String)
}
```

接下来，我们编写业务逻辑。

"FileHelper.kt":

```kotlin
class FileHelper {

    fun saveFile(path: String, callback: FileCallback) {
        try {
            File(path).createNewFile()
            callback.onResult(true, "OK!")
        } catch (e: Exception) {
            callback.onResult(false, "${e.message}")
            System.err.println("Save file failed! Reason:[${e.message}]")
        }
    }
}
```

此处 `saveFile()` 方法的第二参数为FileCallback接口实现，文件操作结果将会通过 `onResult()` 方法反馈给调用者。

第二步，编写测试代码。

"FileHelperTest.kt":

```kotlin
@Test
fun testSaveFile() {
    val fileHelper = FileHelper()
    // 创建Callback的Mock对象
    val mockCallback: FileCallback = mockk(relaxed = true)

    // 调用待测方法，传入Callback的Mock对象
    val invalidPath = "/invalid_path.txt"
    fileHelper.saveFile(invalidPath, mockCallback)

    // 定义捕获器， `slot()` 方法的泛型即参数类型。
    val captorResult = slot<String>()

    // 验证回调方法已触发，并使用 `capture()` 方法捕获第二个参数。
    verify {
        mockCallback.onResult(any(), capture(captorResult))
    }

    // 查看捕获到的参数值
    val capturedValue: String = captorResult.captured
    println("捕获到的参数值:[$capturedValue]")
    // 进一步验证该参数值
    assertFalse(capturedValue.startsWith("OK"))
}
```




当回调方法被触发后，我们可以在 `verify()` 方法中使用 `capture()` 方法配置参数捕获器，检测


```text
捕获到的参数值:[拒绝访问。]
```




🔴 示例一：使用参数捕获器记录所有参数。

在本示例中，我们使用参数捕获器记录所有的参数值。

我们在前文“示例”的基础上进行修改，在 `onResult()` 方法上设置捕获器。

"FileHelperTest.kt":

```kotlin
@Test
fun `testSaveFile2`() {
    val capturedValues = mutableListOf<String>()

    val fileHelper = FileHelper()
    // 创建Callback的Mock对象
    val mockCallback: FileCallback = mockk()
    // Mock回调方法，使用List作为捕获接收器。
    every { mockCallback.onResult(any(), capture(capturedValues)) } returns Unit

    // 多次调用待测方法，传入Callback的Mock对象
    val invalidPath = "/invalid_path.txt"
    fileHelper.saveFile(invalidPath, mockCallback)
    fileHelper.saveFile(invalidPath, mockCallback)
    val validPath = "/tmp/valid_path.txt"
    fileHelper.saveFile(validPath, mockCallback)
    File(validPath).deleteOnExit()

    // 查看捕获到的参数值
    capturedValues.forEachIndexed { index, s ->
        println("Index:[$index] Value:[$s]")
    }
}
```

此处我们需要在Mock回调方法时设置捕获器，与先前在 `verify()` 中设置捕获器的操作不同，因为我们需要捕获所有参数，`verify()` 只能单次验证。



```text
Index:[0] Value:[拒绝访问。]
Index:[1] Value:[拒绝访问。]
Index:[2] Value:[系统找不到指定的路径。]
```
