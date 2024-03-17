# 简介
ViewBinding是Jetpack提供的一种界面开发辅助工具，能够帮助开发者访问布局XML文件中的控件。ViewBinding可以取代 `findViewById()` 方法，有效地避免了因ID填写错误导致空指针异常的问题。

ViewBinding的工作原理是：通过Gradle生成与布局文件对应的ViewBinding类，这些类中包含布局控件的引用；我们可以直接使用 `<ViewBinding实例>.<控件名称>` 的方式访问控件，不必使用 `findViewById()` 方法获取控件，也不必通过全局变量保存控件的引用。

对于一个布局XML文件，ViewBinding类的命名规则是：以下划线("_")为单词定界符，将XML文件名中的每个单词首字母大写，然后移除下划线与扩展名，最后在末尾添加固定后缀"Binding"。若一个布局XML文件名为 `main_activity.xml` ，则对应的ViewBinding类名为"MainActivityBinding"。

ViewBinding类中的控件变量名称由布局XML中的控件ID转换而来，如果一个控件没有静态声明ID，则不会出现在ViewBinding类中。控件变量名称的命名规则是：以下划线("_")为单词定界符，将首个单词以外的单词首字母大写，最后移除下划线等特殊符号。例如某个文本框的ID为 `textview_Title` ，则对应的变量名为"textviewTitle"。

本章示例代码详见以下链接：

- [🔗 示例工程：ViewBinding](https://github.com/BI4VMR/Study-Android/tree/master/M08_Architecture/C02_Component/S03_ViewBinding)

# 基本应用
ViewBinding功能默认是关闭的，Gradle不会生成相关代码，我们首先需要在当前模块的Gradle配置文件中添加一些代码，将ViewBinding功能设为开启。

"build.gradle":

```groovy
android {

    /* 此处省略部分无关代码... */

    // 开启ViewBinding
    viewBinding {
        enabled = true
    }
}
```

接着我们编写一个登录界面布局文件，为了缩短篇幅，此处只列出关键的属性。

"testui_base.xml":

```xml
<!-- 此处省略部分无关属性... -->
<LinearLayout>
    <TextView android:id="@+id/tvTitle" />

    <EditText android:id="@+id/et_username"  />
    <EditText android:id="@+id/et_password" />

    <Button android:id="@+id/btnLogin" />
</LinearLayout>
```

此时我们便可以在Activity代码中使用视图绑定类获取控件的引用了。

"TestUIBase.java":

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 获取当前页面的ViewBinding对象
    TestuiBaseBinding binding = TestuiBaseBinding.inflate(getLayoutInflater());
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

按照ViewBinding类名生成规则， `testui_base.xml` 文件对应的ViewBinding类为 `TestuiBaseBinding` ；我们首先调用它的静态方法 `inflate()` 获得当前Activity的ViewBinding实例 `binding` ，然后就可以通过该实例访问布局中的控件了。

ViewBinding类的 `getRoot()` 方法将返回根布局View实例，我们将其传入Activity的 `setContentView(View view)` 方法以渲染界面。然后我们通过 `binding` 对象中的控件变量，访问各个控件并实现界面逻辑。

# 常见应用场景
## Fragment
当我们在Fragment中使用视图绑定时，可以在 `onCreateView()` 生命周期中获取视图绑定对象并初始化变量，然后在 `onDestroyView()` 方法中将其置空，使得系统能够及时回收View占用的资源。

TestFragment.java:

```java
public class TestFragment extends Fragment {

    private LoginFragmentBinding binding;

    public static TestFragment newInstance() {
        return new TestFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用生命周期方法提供的"inflater"和父容器获取视图绑定对象
        binding = LoginFragmentBinding.inflate(inflater, container, false);

        /* 初始化控件 */
        binding.tvTitle.setText("请输入登录信息");
        binding.btnLogin.setOnClickListener(v -> {
            // 访问用户名与密码输入控件，获取当前内容。
            String name = binding.etUsername.getText().toString();
            String pwd = binding.etPassword.getText().toString();
            Toast.makeText(requireContext(), "名称: " + name + " ;密码: " + pwd, Toast.LENGTH_SHORT)
                    .show();
        });

        // 返回View给系统
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 将视图绑定对象置空，防止内存泄漏。
        binding = null;
    }
}
```

在实际使用中，我们也可以创建一个可空内部变量"_binding"用于获取与置空，然后使用非空断言创建另一个"binding"对象以供访问控件，避免使用过程中每次都要进行空值判断。

## RecyclerView
我们可以在表项的ViewHolder中使用视图绑定，以消除ViewHolder类首部的大量控件全局变量。

MyAdapter.java:

```java

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    /* 此处省略部分变量与方法... */

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        // 获取Item布局文件对应的视图绑定类实例
        ListItemSimpleBinding itemBinding = ListItemSimpleBinding.inflate(inflater, parent, false);
        // 创建ViewHolder实例，并传入视图绑定对象。
        return new MyViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        // 只需要保存视图绑定对象即可，不需要再保存每个控件的引用。
        private final ListItemSimpleBinding binding;

        public MyViewHolder(@NonNull ListItemSimpleBinding binding) {
            super(binding.getRoot());
            // 保存视图绑定对象
            this.binding = binding;
        }

        public void bindData() {
            // 获取当前项的数据
            SimpleVO vo = dataSource.get(getAdapterPosition());
            // 将数据设置到控件中
            binding.tvTitle.setText(vo.getTitle());
        }
    }
}
```

# 特殊XML标签
## "include"标签
当布局文件中存在 `<include>` 标签时，Gradle将会生成具有多个层级的ViewBinding类。我们可以使用 `<根布局ViewBinding实例>.<Include ViewBinding实例>.<控件名称>` 这种方式访问 `<include>` 布局内部的控件。

我们首先创建一个可供复用的标题布局文件：

"title_include.xml":

```xml
<!-- 此处省略部分无关属性... -->
<LinearLayout>
    <Button android:id="@+id/btnBack" />
    <TextView android:id="@+id/tvTitle"  />
</LinearLayout>
```

然后在测试页面的布局文件中使用 `<include>` 标签引用上述布局，为其设置ID与宽高属性。

"testui_include.xml":

```xml
<!-- 此处省略部分无关属性... -->
<LinearLayout>

    <include
        android:id="@+id/layoutTitle"
        layout="@layout/title_include"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
```

随后我们在测试Activity中尝试访问 `<include>` 标签所引用布局内的控件。

"TestUIInclude.java":

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TestuiIncludeBinding binding = TestuiIncludeBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // 访问标题布局中的文本控件，设置标题。
    binding.layoutTitle.tvTitle.setText("请输入登录信息");
    // 访问标题布局中的按钮控件，设置点击事件监听器。
    binding.layoutTitle.btnBack.setOnClickListener(v -> finish());
}
```

从上述示例代码可知，当存在 `<include>` 标签时，Gradle会使用该标签中的ID生成被嵌套的ViewBinding实例名称，以供我们进行访问。

## "merge"标签
当被引用布局文件的根标签为 `<merge>` 时，我们需要单独获取它们的ViewBinding实例。

我们对前文示例中的标题布局略作修改，将根布局 `<LinearLayout>` 改为 `<merge>` 标签。

"title_merge.xml":

```xml
<!-- 此处省略部分无关属性... -->
<merge>
    <Button android:id="@+id/btnBack" />
    <TextView android:id="@+id/tvTitle"  />
</merge>
```

然后在测试页面布局文件中引用它，无需添加ID属性。

"testui_merge.xml":

```xml
<!-- 此处省略非必要节点与属性... -->
<LinearLayout>
    <include
        layout="@layout/title_merge"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
```

此时我们在测试Activity中尝试访问标题布局内的控件。

"TestUIMerge.java":

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TestuiMergeBinding binding = TestuiMergeBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // 使用Merge布局ViewBinding类的"bind()"方法获取实例
    TitleMergeBinding titleBinding = TitleMergeBinding.bind(binding.getRoot());
    // 访问标题布局中的文本控件，设置标题。
    titleBinding.tvTitle.setText("请输入登录信息");
    // 访问标题布局中的按钮控件，设置点击事件监听器。
    titleBinding.btnBack.setOnClickListener(v -> finish());

    /* 此处省略其他语句... */
}
```

当被引用的布局根标签为 `<merge>` 时，我们不能通过定义ID的方式访问它，此时需要使用布局绑定类的 `bind()` 方法，获取Merge布局文件对应的ViewBinding实例，然后通过该实例访问其中的控件。

# 忽略布局文件
在默认情况下，一旦我们开启ViewBinding功能，所有布局文件都会生成对应的ViewBinding类。有些布局文件可能无需在代码中被访问，此时我们可以在它们的根标签上添加 `tools:viewBindingIgnore="true"` 属性。

```xml
<LinearLayout
    tools:viewBindingIgnore="true">

    <!-- 此处省略子节点... -->
</LinearLayout>
```

拥有该属性的布局文件会被Gradle忽略，适当地使用该属性可以减少工程中的冗余代码、提高编译速度。

上述属性也可以被设置到控件上，如果某个控件拥有ID属性，并且同时被设置了 `tools:viewBindingIgnore="true"` ，则ViewBinding类中不会包含对应的变量。
