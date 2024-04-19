# 概述
ToggleButton是一种自锁按钮，它拥有两种状态：“开启”和“关闭”，用户点击后按钮不会立即弹起，而是保持新的状态，直到再次被点击才会再次切换状态。

# 基本应用
TextView在布局文件中的典型配置如下文代码块所示：

"testui_base.xml":

```xml
<ToggleButton
    android:id="@+id/toggleButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

此时运行示例程序，并查看界面外观：

<div align="center">

<!-- TODO ![ToggleButton示例](./Assets-ToggleButton/基本应用-ToggleButton示例.jpg) -->

</div>

<!-- TODO
# 外观定制
## 基本样式
### 文本内容

# 常用属性
🔷 `android:textOn="[文本]"`

设置按钮在选中状态的文字，默认为"ON"。

🔷 `android:textOff="[文本]"`

设置按钮在未选中状态的文字，默认为"OFF"。

🔷 `android:checked="[true|false]"`

设置按钮的初始状态，默认为“未选中”。

# 常用方法
🔶 `boolean isChecked()`

获取当前按钮的选中状态。

🔶 `void setChecked(boolean state)`

设置按钮的选中状态。

🔶 `void toggle()`

反转当前选中状态。

-->

# 事件监听器
## OnCheckedChangeListener
当ToggleButton的开关状态发生改变时，将会触发OnCheckedChangeListener监听器。

该监听器仅有一个 `void onCheckedChanged(CompoundButton buttonView, boolean isChecked)` 回调方法，第一参数 `buttonView` 是ToggleButton实例；第二参数 `isChecked` 是新的开关状态。

"TestUIEvent.java":

```java
toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        boolean userInput = buttonView.isPressed();
        Log.i(TAG, "OnCheckedChanged. State:[" + isChecked + "] UserInput:[" + userInput + "]");
    }
});
```

当我们调用ToggleButton的 `setChecked(boolean state)` 或 `toggle()` 方法设置开关状态时，回调方法 `onCheckedChanged()` 也会触发，这在某些场景下可能导致逻辑错误。我们可以在回调方法中使用控件的 `isPressed()` 方法判断当前事件是否为用户输入。
