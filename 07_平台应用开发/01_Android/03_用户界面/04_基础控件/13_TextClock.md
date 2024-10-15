# 简介

TextClock是在Android API17(4.2.0)之后出现的，由系统托管，不会出现停止的现象。

TextClock中使用了DateFormat来格式化时间，格式是根据系统的语言来变化的，你系统语言是中文就是中文的格式，英文就是英文。
还有就是中文的星期的叫法有两种，既：星期一、周一。例 测试的机器中EEEE代表使用“星期一”这样的格式，E、EE或EEE都是代表“周一”，部分机型有所不同。



在Android上实现数字时钟，还在自己使用TextView实现？在应用界面还容易，如果在AppWidget中，时间更新将会是头疼的问题，为了解决这些烦恼，Android官方提供了TextClock类，只需要将控件放在不居中，设置自己需要时间格式即可。



TextClocks是数字时钟,就是通过文本来显示时钟.只需要添加上控件即可,基本不用编写代码.


# 基本应用
TextClock在布局文件中的典型配置如下文代码块所示：

"testui_base.xml":

```xml
<TextClock
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:format12Hour="hh:mm:ss"
    android:format24Hour="HH:mm:ss" />
```

此时运行示例程序，并查看界面外观：

<div align="center">

![默认样式](./Assets_TextView/基本应用_默认样式.jpg)

</div>

# 外观定制
## 基本样式
### 文本内容
以下属性与方法用于获取与设置TextView中的文字内容：

- XML - 设置文本内容 : `android:text="<文本内容 | 字符串资源ID>"`
- Java - 设置文本内容 : `void setText(CharSequence text)`
- Java - 获取文本内容 : `CharSequence getText()`
- Java - 获取文本长度 : `int length()`

设置文本时，我们可以直接填入文本内容，但系统并不推荐这样做，将会产生硬编码(HardcodedText)警告。我们应当将内容写入到 `res/values/strings.xml` 等文件中，然后在此处引用其资源ID，系统将根据语言环境自动选择合适的文本，以实现多语言适配。
 android:format12Hour是指定时钟显示的文字格式.当然这个是以12小时来显示,还有以24小时来显示的android:format24Hour.

 android:timeZone是指定要使用的时区

### 样式
TextView,可以设置。


