# 简介
ViewBinding是Jetpack库中的一个组件，它能帮助开发者访问布局XML中的控件，减少"findViewById()"方法的使用，防止ID填写错误导致空指针异常等问题。

ViewBinding的工作原理是通过Gradle生成与布局XML对应的视图绑定Java类，该类包含布局中所有控件的引用，我们可以直接使用"<绑定对象>.<控件对象>"的方式进行访问，而不必再使用"findViewById()"方法和全局变量获取控件对象。

对于一个布局XML文件，视图绑定类的命名规则是：将XML文件名中的每个单词首字母大写，移除下划线("_")与扩展名，然后在末尾添加固定后缀"Binding"。例如XML文件名为："main_activity.xml"，则对应的视图绑定类名为："MainActivityBinding"。

视图绑定类中的控件变量名称由XML中的控件ID转换而来，如果一个控件没有静态声明ID，则不会出现在视图绑定类中。

视图绑定类中的控件变量名称生成规则是：以下划线为单词定界符，将首个单词以外的单词首字母大写，最后移除下划线等特殊符号。例如文本框控件ID为："textview_Title"，则对应的变量名为："textviewTitle"。

# 基本应用
我们首先编写一个登录界面布局文件，为了节约篇幅，此处只列出关键属性。

ui_demo_base.xml:

```xml
<LinearLayout>
    <TextView android:id="@+id/tvTitle" />

    <EditText android:id="@+id/et_username"  />
    <EditText android:id="@+id/et_password" />

    <Button android:id="@+id/btnLogin" />
</LinearLayout>
```

若要使用ViewBinding，首先需要在当前项目的Gradle配置中将其启用：

```groovy
android {
    // 省略其他内容...

    viewBinding {
        enabled = true
    }
}
```

接着我们在Android Studio的项目列表中选中当前模块，并执行一次Make操作，使Gradle生成布局文件对应的视图绑定类。

<div align="center">

![执行Make生成视图绑定类](./Assets-ViewBinding/基本应用-执行Make生成视图绑定类.jpg)

</div>

Make完成后，我们就可以在Activity代码中使用视图绑定类了。

DemoBaseUI.java:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 获取当前页面的ViewBinding对象
    UiDemoBaseBinding binding = UiDemoBaseBinding.inflate(getLayoutInflater());
    // 使用"setContentView(View view)"方法加载布局。
    setContentView(binding.getRoot());

    // 访问标题控件，设置标题
    binding.tvTitle.setText("请输入登录信息");
    // 访问登录按钮控件，设置点击事件监听器。
    binding.btnLogin.setOnClickListener(v -> {
        // 访问用户名与密码输入控件，获取当前内容。
        String name = binding.etUsername.getText().toString();
        String pwd = binding.etPassword.getText().toString();
        Toast.makeText(this, "名称: " + name + " ;密码: " + pwd, Toast.LENGTH_SHORT)
            .show();
    });
}
```

按照类名生成规则，"ui_demo_base.xml"对应的视图绑定类为"UiDemoBaseBinding"；我们首先调用它的 `inflate()` 方法获得当前Activity的视图绑定对象"binding"，然后就可以操作实例化后的视图了。

视图绑定类的 `getRoot()` 方法将返回根布局View对象，我们将其传入Activity的 `setContentView()` 方法以展示界面。然后我们通过"binding"对象中各控件的引用，访问控件属性并设置监听器，实现界面逻辑。

# 其他应用场景

# 特殊标签用法
## "include"标签
当布局文件中存在 `<include>` 标签时，我们可以使用 `<根布局binding对象>.<内层布局对象>.<控件名称>` 这种方式访问引入布局内部的控件。

我们首先创建一个标题布局文件：

title_include.xml:

```xml
<!-- 此处省略非必要节点与属性... -->
<LinearLayout>
    <Button android:id="@+id/btnBack" />
    <TextView android:id="@+id/tvTitle"  />
</LinearLayout>
```

然后在测试页面的布局文件中使用 `<include>` 标签引用上述布局，为其设置ID与宽高属性。

ui_demo_include.xml:

```xml
<!-- 此处省略非必要节点与属性... -->
<LinearLayout>
    <include
        android:id="@+id/layoutTitle"
        layout="@layout/title_include"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
```

随后我们在测试Activity中尝试访问被 `<include>` 标签引入布局中的控件。

DemoIncludeUI.java:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    UiDemoIncludeBinding binding = UiDemoIncludeBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // 访问标题布局中的文本控件，设置标题。
    binding.layoutTitle.tvTitle.setText("请输入登录信息");
    // 访问标题布局中的按钮控件，设置点击事件监听器。
    binding.layoutTitle.btnBack.setOnClickListener(v -> finish());

    /* 此处省略其他语句... */
}
```

由上述示例代码可知，当存在 `<include>` 标签时，Gradle会使用该标签中指定的ID生成绑定对象名，以供我们进行访问。

## "merge"标签
当被引用布局文件的根标签为 `<merge>` 时，我们需要单独获取其对应的视图绑定对象。

我们将前文示例中的标题布局略作修改，将根布局改为 `<merge>` 标签。

title_merge.xml:

```xml
<!-- 此处省略非必要节点与属性... -->
<merge>
    <Button android:id="@+id/btnBack" />
    <TextView android:id="@+id/tvTitle"  />
</merge>
```

然后在测试页面布局文件中引用它，并移除ID属性。

ui_demo_merge.xml:

```xml
<!-- 此处省略非必要节点与属性... -->
<LinearLayout>
    <include
        layout="@layout/title_merge"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
```

随后我们可以在逻辑代码中访问标题布局中的控件。

DemoMergeUI.java:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    UiDemoMergeBinding binding = UiDemoMergeBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // 使用Merge布局的视图绑定类的"bind()"方法获取绑定对象
    TitleMergeBinding titleBinding = TitleMergeBinding.bind(binding.getRoot());
    // 访问标题布局中的文本控件，设置标题。
    titleBinding.tvTitle.setText("请输入登录信息");
    // 访问标题布局中的按钮控件，设置点击事件监听器。
    titleBinding.btnBack.setOnClickListener(v -> finish());

    /* 此处省略其他语句... */
}
```

当被引用的布局为 `<merge>` 标签时，我们不能通过定义ID的方式进行访问，此时需要通过布局绑定类的 `bind()` 方法，获取Merge布局对应的视图绑定对象，然后通过该对象访问其中的控件。

# 忽略指定的布局文件
默认情况下，所有布局XML文件都会生成对应的视图绑定类，有些文件我们可能不需要用于视图绑定，此时可以在其根布局上添加 `tools:viewBindingIgnore="true"` 属性。

```xml
<LinearLayout 省略其他属性...
    tools:viewBindingIgnore="true">
    <!-- 此处省略子节点... -->
</LinearLayout>
```

拥有这种属性的XML文件会被Gradle忽略，适当的配置该属性可以减少冗余代码、提高编译速度。

该属性也可以被设置到控件上，如果我们不需要在逻辑代码中访问某个控件，但其又需要通过ID进行布局定位，就可以为其设置 `tools:viewBindingIgnore="true"` 属性，使Gradle不生成该控件的引用。
