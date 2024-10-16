# 简介
TextClock是Android SDK提供的数字时钟控件，其内部实现了自动刷新功能，我们只需在布局中放置控件并设置时间格式，该控件就能显示实时时间。

本章的示例工程详见以下链接：

- [🔗 示例工程：TextClock](https://github.com/BI4VMR/Study-Android/tree/master/M03_UI/C04_CtrlBase/S13_TextClock)

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

<!-- TODO ![默认样式](./Assets_TextView/基本应用_默认样式.jpg) -->

</div>

# 外观定制
## 基本样式
### 文本内容
以下属性与方法用于获取与设置TextView中的文字内容：

- XML - 设置12小时制的时间格式 : `android:format12Hour="<时间格式占位符>"`
- XML - 设置24小时制的时间格式 : `android:format24Hour="<时间格式占位符>"`
- XML - 设置24小时制的时间格式 : `android:timeZone="<时区名称>"`
- Java - 设置文本内容 : `void setText(CharSequence text)`

### 文本样式
TextClock继承自TextView，因此我们可以使用TextView提供的属性与方法设置文本属性。

