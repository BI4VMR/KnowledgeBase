# 简介
AnalogClock是Android SDK提供的模拟时钟控件，我们只能设置表盘、时针、分针样式，可定制程度较低。

为了实现显示秒针等更多功能，我们可以创建自定义控件，详见相关章节： [🧭 自定义控件 - AnalogClock](【TODO_链接地址】) 。

本章的示例工程详见以下链接：

- [🔗 示例工程：AnalogClock](https://github.com/BI4VMR/Study-Android/tree/master/M03_UI/C04_CtrlBase/S14_AnalogClock)

# 基本应用
TextClock在布局文件中的典型配置如下文代码块所示：

"testui_base.xml":

```xml
<AnalogClock
    android:layout_width="200dp"
    android:layout_height="200dp" />
```

此时运行示例程序，并查看界面外观：

<div align="center">

<!-- TODO ![默认样式](./Assets_TextView/基本应用_默认样式.jpg) -->

</div>

# 外观定制
## 基本样式
以下属性用于设置AnalogClock中的表盘、指针样式：

- XML - 设置表盘样式 : `android:dial="<图像资源>"`
- XML - 设置表盘样式 : `android:hand_hour="<图像资源>"`
- XML - 设置表盘样式 : `android:hand_minute="<图像资源>"`
