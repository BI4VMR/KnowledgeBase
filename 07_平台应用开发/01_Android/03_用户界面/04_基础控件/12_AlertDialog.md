# 简介
AlertDialog是一种对话框控件，继承自Dialog，封装了一些常用的功能，可以向用户展示消息并提供“确认”或“取消”等操作。AlertDialog是一种模态窗口，当它弹出时，处于其下层的界面将不可交互；用户必须先与对话框交互，使对话框关闭，随后才能继续与界面交互。

AlertDialog作为Activity的附属组件而存在，它的显示与隐藏并不会影响Activity的生命周期。AlertDialog在"android"包与"androidx.appcompat"包中均有实现，我们应该优先使用后者。

# 基本应用
对话框有多种预置组件，包括标题、图标、内容、按钮等，这些组件通过构造器模式进行配置。

```java
// 创建对话框构造器并进行配置
AlertDialog.Builder builder = new AlertDialog.Builder(context)
        // 设置标题
        .setTitle("警告对话框")
        // 设置图标
        .setIcon(R.mipmap.funny_256)
        // 设置内容
        .setMessage("此处为消息内容");
// 创建对话框
AlertDialog dialog = builder.create();

// 显示弹窗按钮
Button btShow = findViewById(R.id.btShow);
btShow.setOnClickListener(v -> {
    // 显示对话框
    dialog.show();
});
```

我们首先通过AlertDialog构造器的系列方法配置对话框，其中 `Builder()` 方法的参数是Context对象，需要拥有Theme属性，因此我们通常传入Activity实例，如果传入ApplicationContext等实例则会导致IllegalStateException异常。各项属性配置完毕后，调用构造器的 `create()` 方法即可生成Dialog对象。

当按钮"btShow"的点击事件触发时，对话框的 `show()` 方法被调用，对话框随即显示在界面上，效果如下图所示。

<div align="center">

![AlertDialog示例](./Assets-AlertDialog/基本应用-AlertDialog示例.gif)

</div>

此时我们点击对话框周围的暗色区域，可以关闭该对话框。Dialog的 `setCancelable()` 方法与 `setCanceledOnTouchOutside()` 方法都能控制点击对话框周围区域时，是否关闭对话框。前者设为"false"时，触摸事件与返回键都不能关闭对话框；后者设为"false"时，触摸事件不能关闭对话框，但返回键可以关闭对话框。

AlertDialog除了展示信息外，通常还会询问用户是否执行某项操作。AlertDialog内置了确认、取消、中立三个按钮，它们的位置是固定的，默认不显示，开发者可以通过构造器设置这些按钮的功能。

```java
// 创建对话框构造器并进行配置
AlertDialog.Builder builder = new AlertDialog.Builder(this)
        // 设置标题等属性
        .setTitle("警告对话框")
        // 设置确认按钮
        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //按下确认按钮后的操作
                Log.d("Test", "Positive");
            }
        })
        // 设置取消按钮
        .setNegativeButton("取消", (dialog, which) -> Log.i("Test", "Negative"))
        // 设置中立按钮
        .setNeutralButton("中立", (dialog, which) -> Log.i("Test", "Neutral"));
```

我们在构造器中通过相应的方法依次配置了三个按钮，它们的参数是类似的：第一个参数是按钮文字；第二个参数是按钮的点击监听器实现类，回调方法 `onClick()` 中的"which"参数表示按钮的类型，确认、取消、中立分别对应取值"-1"、"-2"、"-3"。当用户按下任意按钮之后，系统将会自动调用Dialog的 `dismiss()` 方法关闭对话框。

<div align="center">

![AlertDialog的预设按钮](./Assets-AlertDialog/基本应用-AlertDialog的预设按钮.gif)

</div>

# 预设样式
## 列表对话框
对话框构造器的 `setItems()` 方法可以在对话框中显示简单的列表，用户点击某个表项后窗口关闭，并且触发该表项对应的操作。

```java
// 表项数据
final String[] res = {"语文", "数学", "英语", "物理", "化学"};
// 配置对话框
AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setTitle("列表对话框")
        // 设置列表对话框
        .setItems(res, (dialog, which) -> {
            // "which"参数表示选中的列表项索引
            Log.i("Test", "你选择了：" + res[which]);
        });
```

`setItems()` 方法的第一个参数是String数组，系统会将数组中的文本逐个加载至列表中。第二个参数是DialogInterface.OnClickListener，它只有一个回调方法 `onClick()` ，其中"which"参数表示用户点击的表项索引。

<div align="center">

![列表对话框](./Assets-AlertDialog/预设样式-列表对话框.jpg)

</div>

## 选择对话框
我们可以使用对话框显示单选或多选按钮组，以供用户进行选择操作。用户点击此类对话框中的表项后，对话框不会自动关闭，因此我们通常会添加确认按钮，用户点击确认按钮后对话框自动关闭，此时再读取选择状态并执行后续操作。

```java
// 表项数据
final String[] res = {"语文", "数学", "英语", "物理", "化学"};

// 配置单选对话框
AlertDialog.Builder builder1 = new AlertDialog.Builder(this)
        .setTitle("单选对话框")
        // 设置单选组件
        .setSingleChoiceItems(res, 1, (dialog, which) ->
                // 用户每次点击选项都会触发此回调
                Log.i("Test", "你选中了：" + res[which])
        )
        // 设置确认按钮
        .setPositiveButton("确认", (dialog, which) -> {
            // 按下确认按钮时触发此回调，可以从全局变量读取最终选择的项。
        });
```

`setSingleChoiceItems()` 方法用于设置单选对话框，它与列表对话框十分相似，第一个参数是数据源；第二个参数是默认选中的表项索引；第三个参数是点击监听器，可以在此处将选中的项记录至全局变量，当用户点击确认时，读取全局变量的值作为最终结果。

<div align="center">

![单选对话框](./Assets-AlertDialog/预设样式-单选对话框.jpg)

</div>

```java
// 表项数据
final String[] res = {"语文", "数学", "英语", "物理", "化学"};
// 选项状态标记
boolean[] status = {false, true, false, true, false};

// 配置多选对话框
AlertDialog.Builder builder2 = new AlertDialog.Builder(this)
        .setTitle("多选对话框")
        // 设置多选组件
        .setMultiChoiceItems(res, status, (dialog, which, isChecked) -> {
            // 选项状态变更时，修改状态标记。
            status[which] = isChecked;
        })
        // 设置确认按钮
        .setPositiveButton("确认", (dialog, which) -> {
            // 按下确认按钮时触发此回调，可以从全局变量读取最终选择的项。
        });
```

`setMultiChoiceItems()` 方法用于设置多选对话框，第二个参数表示初始选中状态；第三个参数是多选监听器，其回调方法的"which"参数表示当前选中的表项索引，"isChecked"参数表示当前表项状态是否为选中。

<div align="center">

![多选对话框](./Assets-AlertDialog/预设样式-多选对话框.jpg)

</div>

# 外观定制
## 自定义布局
除了使用系统提供的对话框功能模版，我们还可以向对话框中加载自己定义的界面。

此处我们创建了一个ImageView与Button，并将它们放置在线性布局中，最后使用对话框的 `setView()` 方法，将组装好的界面放入窗口中。

```java
// 配置对话框
AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setTitle("自定义对话框");
AlertDialog dialog = builder.create();

// 创建图片展示框
Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.funny_256);
ImageView imageView = new ImageView(this);
imageView.setImageBitmap(bmp);
// 创建按钮
Button button = new Button(this);
button.setText("关闭");
button.setOnClickListener(v -> dialog.dismiss());
// 创建布局管理器并添加上述控件
LinearLayout layout = new LinearLayout(this);
layout.setGravity(Gravity.CENTER_HORIZONTAL);
layout.setOrientation(LinearLayout.VERTICAL);
layout.addView(imageView);
layout.addView(button);

// 将自定义View设置到对话框上
dialog.setView(layout);
// 显示弹窗
dialog.show()
```

<div align="center">

![自定义布局](./Assets-AlertDialog/外观定制-自定义布局.jpg)

</div>

对于自定义对话框，用户点击窗口四周的区域时，窗口不会自行关闭，因此我们通常需要绘制一个关闭按钮并在它的点击事件中调用 `dialog.dismiss()` 。

## 自定义窗体
如果我们希望改变对话框框架的样式，例如修改背景形状、使边框外围不变暗等，可以使用自定义Style。

首先创建一个Style配置，继承自系统提供的对话框样式：

```xml
<!-- 自定义弹窗样式 -->
<style name="DialogCustom" parent="@style/Theme.AppCompat.Dialog">
    <!-- Dialog的背景，可以引用Color、Drawable，直接设置"#RGB"是无效的，填入"@null"表示透明。 -->
    <item name="android:windowBackground">@drawable/dialog_bg</item>
    <!-- Dialog外围区域是否变暗，"true"表示变暗，"false"表示不变暗。 -->
    <item name="android:backgroundDimEnabled">false</item>
</style>
```

创建对话框时，我们可以使用具有两个参数的构造方法，传入自定义样式的ID：

```java
// 创建对话框并指定Style
AlertDialog dialog = new AlertDialog.Builder(this, R.style.DialogCustom)
        .setTitle("警告对话框")
        .setMessage("此处为消息内容")
        .create();
```

除此之外，我们还可以通过修改窗体属性控制对话框的内边距、位置、尺寸等属性。

```java
// 获取Window对象
Window window = dialog.getWindow();
// 更改内边距
window.getDecorView().setPadding(10, 10, 10, 10);
// 获取布局参数
WindowManager.LayoutParams layoutParam = window.getAttributes();
// 设置尺寸与位置
layoutParam.width = 600;
layoutParam.height = 300;
// 设置窗口与父布局的对齐方式
layoutParam.gravity = Gravity.TOP | Gravity.START;
// 设置窗口在"gravity"所设置的对齐方向上的偏移量
layoutParam.x = 100;
layoutParam.y = 150;
// 应用布局参数
window.setAttributes(layoutParam);
```

<div align="center">

![自定义窗体](./Assets-AlertDialog/外观定制-自定义窗体.jpg)

</div>

# 监听器
## OnDismissListener
OnDismissListener用于监听弹窗隐藏事件，它只有一个回调方法 `onDismiss()` ，弹窗被自身或外部程序调用 `dismiss()` 方法后，此回调方法将会触发。

```java
dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.i("myapp", "弹窗隐藏");
    }
});
```

弹窗隐藏后并不会销毁，如果再次调用 `show()` 使其显示，内容仍会保持之前的状态，因此该方法可以用于重置弹窗内容。

## OnCancelListener
OnCancelListener用于监听弹窗的取消事件，它只有一个回调方法 `onCancel()` ，弹窗被自身或外部程序调用 `cancel()` 方法后，此回调方法将会触发。

```java
dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.i("myapp", "弹窗取消");
    }
});
```

AlertDialog的`cancel()`方法执行后，首先通知OnCancelListener，然后调用 `dismiss()` 使弹窗隐藏，所以我们使用此方法时需要注册OnCancelListener，否则是无意义的。
