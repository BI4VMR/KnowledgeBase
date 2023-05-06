# 简介
Button是按钮控件，它是用户与系统交互最为常用组件的之一，用户通过点击按钮发出指令，系统执行对应的操作。

Button继承自TextView，因此具有TextView的所有属性，我们可以更改按钮中文字的字号、样式等属性。

# 基本应用
Button在XML文件中的典型配置如下：

```xml
<Button
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Click This" />
```

显示效果：

<div align="center">

![Button示例](./Assets-Button/基本应用-Button示例.jpg)

</div>

# 外观定制
## 基本样式
🔷 `android:textAllCaps="[true|false]"`

按钮文本是否显示为大写字母。

该属性的值默认为"true"，因此前文示例中设置的文本均被转换为大写字母。

🔷 `android:enabled="[true|false]"`

按钮是否可以被用户点击。

设为"false"时控件不可与用户交互，此属性也适用于其它交互型控件。

🔷 `android:background="[图形资源]"`

按钮的背景素材。

该属性通常配合选择器使用，普通状态与用户正在按压时产生不同的显示效果。

有时主题色优先级高于此属性，因此即使该属性被设置，按钮背景仍为主题色，解决方法详见后文小节 [🧭 疑难解答 - 设置背景后未生效](#疑难解答) 。

## 选择器
选择器可以设置控件在各种状态下需要加载的资源，例如我们可以为按钮配置按压状态与默认状态的不同背景素材。

首先在"res/drawable"目录中创建选择器描述文件"selector_button.xml"：

selector_button.xml:

```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/ic_volume_off_24" android:state_pressed="true" />
    <item android:drawable="@drawable/ic_volume_up_24" />
</selector>
```

然后在按钮的XML配置中应用该选择器：

```xml
<Button
    android:id="@+id/btSelector"
    android:layout_width="64dp"
    android:layout_height="64dp"
    android:background="@drawable/selector_button" />
```

默认状态下按钮会显示图标"ic_volume_up_24"，当用户按下按钮时，匹配到状态： `android:state_pressed="true"` ，此时图标被替换为"ic_volume_off_24"。效果如图所示：

<div align="center">

![使用选择器](./Assets-Button/外观定制-使用选择器.gif)

</div>

## 阴影效果
有时按钮从主题继承了阴影效果，即使我们更改按钮背景，也会存在阴影样式，此时我们可以为按钮设置属性： `style="?android:attr/borderlessButtonStyle"` ，消除默认的阴影效果。

若我们需要通过"style"属性配置按钮的样式，可以使该样式继承"Widget.AppCompat.Button.Borderless"等无边界样式，以实现移除默认阴影的目的。

```xml
<style name="BtnStyle" parent="@style/Widget.AppCompat.Button.Borderless">
    <item name="android:textColor">@color/white</item>
    <item name="android:textSize">20sp</item>
</style>
```

# 监听器
## 点击监听器
点击事件是所有View组件都支持的事件，但在按钮上最为常用，用户点击按钮时触发点击事件并执行对应的操作。

按钮的 `setOnClickListener()` 方法用于设置点击事件监听器，此监听器仅有一个回调方法 `onClick()` ，参数"v"表示被点击的控件，对于按钮来说就是Button本身。

```java
// 绑定控件
Button btBase = findViewById(R.id.btBase);
// 设置点击监听器
btBase.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // 用户点击按钮时触发此回调方法
        v.setBackgroundColor(Color.parseColor("#00FFFF"));
    }
});
```

上述代码片段中，我们在点击事件触发时，改变了按钮的背景颜色，显示效果如图所示：

<div align="center">

![Button的点击事件](./Assets-Button/监听器-Button的点击事件.gif)

</div>

# 疑难解答
## 索引

<div align="center">

|       序号        |                摘要                |
| :---------------: | :--------------------------------: |
| [案例一](#案例一) | 设置按钮背景后，实际运行时不生效。 |

</div>

## 案例一
### 问题描述
设置按钮背景后，实际运行时不生效。

### 问题分析
Button样式会受到APP主题的影响，如果APP的主题继承自"Theme.MaterialComponents.DayNight.DarkActionBar"，将会给Button带来颜色和点击涟漪效果。

### 解决方案
我们可以将主题改为"Theme.MaterialComponents.DayNight.DarkActionBar.Bridge"等，避免主题改写按钮的样式。

```xml
<resources>
    <!-- 修改APP主题的继承关系 -->
    <style name="Theme.DEFAULT" parent="Theme.MaterialComponents.DayNight.DarkActionBar.Bridge">
        <!-- 省略部分内容... -->
    </style>
</resources>
```
