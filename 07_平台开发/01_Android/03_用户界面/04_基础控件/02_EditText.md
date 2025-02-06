# 概述
EditText是TextView的子类，大部分属性与TextView相同，它能够接受并处理用户输入事件，例如作为登录界面的账号和密码输入框。

# 基本应用
EditText在XML文件中的典型配置如下文所示：

```xml
<EditText
    android:layout_width="250dp"
    android:layout_height="wrap_content"
    android:hint="请输入文本"
    android:inputType="text" />
```

显示效果：

<div align="center">

![EditText样式](./Assets-EditText/基本应用-EditText示例.jpg)

</div>

# 常用属性
🔷 `android:inputType="[文本类型]"`

设置EditText可接受的数据类型，用户点击时输入法会自动打开相应的输入模式。

此属性的常见取值及含义见下表：

<div align="center">

|      取值      |        含义        |
| :------------: | :----------------: |
|      none      |  未设置（默认值）  |
|     text*      |      普通文本      |
| textMultiLine  |      多行文本      |
|  textPassword  | 密码（显示为圆点） |
|    number*     |        整数        |
| numberDecimal  |       浮点数       |
|  numberSigned  |     带符号数字     |
| numberPassword |      数字密码      |
|     phone*     |      电话号码      |
|   datetime*    |     时间与日期     |
|      date      |        日期        |
|      time      |        时间        |

</div>

其中标记为"*"的值是主类型，当我们选定主类型后，还可以设置一个或多个子类型，需要使用"|"分隔。

例如我们需要输入带符号的小数，此时可在XML中配置属性： `android:inputType="number|numberSigned|numberDecimal"` ；如果要在逻辑代码中进行配置，可以使用下文所示的写法：

```java
// 构造文本类型标志位
int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL  | InputType.TYPE_NUMBER_FLAG_SIGNED;
// 设置文本类型
edittext.setInputType(inputType);
```

🔷 `android:hint="[提示信息]"`

设置控件在初始状态下的提示文字，用于告知用户此处要输入的内容。

🔷 `android:textColorHint="[颜色]"`

设置提示文字的颜色。

🔷 `android:digits="[字符列表]"`

设置控件可接受的字符列表。

不在该列表内的字符无法输入，多个字符之间不需要分隔符。例如取值为"ABC"时，控件只接受A、B、C三个大写字母组成的字符串。

🔷 `android:cursorVisible="[true|false]"`

设置编辑光标的可见性。

🔷 `android:textColorHighlight="[颜色]"`

设置长按选择文本时，被选中字符的背景颜色。

🔷 `android:autofillHints="[文本类型]"`

设置自动填充功能允许填入的文本类型。

此功能类似于网页表单的自动补全提示。若我们不设置此属性，Android Studio将会发出警告，我们可以添加 `tools:ignore="Autofill"` 将其忽略，或者设置 `android:importantForAutofill="no"` 属性关闭该功能。
