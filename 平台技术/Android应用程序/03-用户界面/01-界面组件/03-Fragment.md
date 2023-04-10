# 概述
Fragment是一种UI容器，它拥有自己的生命周期，我们可以将复杂的Activity拆分为多个Fragment，进行模块化管理，避免单一Activity的代码过于臃肿，提高代码复用程度。

Fragment不能单独存在，需要依附于Activity或者其它Fragment。当Activity的生命周期达到STARTED状态及以上时，我们可以动态地添加、替换或移除其中的Fragment，使得页面展示更为灵活。

# 基本应用
## 创建Fragment
Fragment与Activity类似，我们可以使用XML文件描述其布局，并在逻辑代码中进行加载与控制。

此处我们创建一个简单的布局，其中有一个居中放置的文本框。

fragment_test.xml:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#0FF"
    android:gravity="center">

    <TextView
        android:id="@+id/tvContent"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="20sp" />
</LinearLayout>
```

然后编写TestFragment类，在生命周期方法 `onCreateView()` 中将XML渲染为视图实例，并设置文本框的内容。

TestFragment.java:

```java
public class TestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 将XML渲染为View实例
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        // 初始化View中的控件
        TextView tv = view.findViewById(R.id.tvContent);
        tv.setText("TestFragment");
        // 返回View实例
        return view;
    }
}
```

用户自行编写的Fragment应继承系统提供的Fragment类，SDK中存在"android.app.Fragment"和"androidx.fragment.app.Fragment"两个包，"android"包中的Fragment类已经被弃用，我们应该使用"androidx"包中的类。

我们在Fragment中重写了 `onCreateView()` 方法，通过LayoutInflater将布局文件渲染成View对象，并将此对象返回给系统。此处的 `inflate()` 方法第三个参数必须为"false"，因为系统创建View之后会自动将其添加到容器"container"中，如果此处填写"true"，将会导致View对象被重复添加而抛出IllegalStateException。

## 使用Fragment
我们可以将Fragment嵌入到其它布局文件的指定位置，也可以使用逻辑代码动态地将Fragment添加到Activity中。

> ⚠️ 警告
>
> 我们只能在具有Fragment管理能力的Activity中使用Fragment，这种Activity都是FragmentActivity的子类，例如AppCompatActivity。如果我们在不支持Fragment的Activity中使用Fragment，则加载XML时会出现UnsupportedOperationException。

### 静态引用Fragment
在布局XML文件中，我们可以使用 `<fragment>` 标签静态加载Fragment到固定的位置。对于较新的版本，官方推荐使用 `<FragmentContainerView>` 作为Fragment容器。

```xml
<!-- 静态加载Fragment -->
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/container1"
    android:name="net.bi4vmr.study.demo01.TestFragment"
    android:layout_width="match_parent"
    android:layout_height="100dp"
    tools:layout="@layout/fragment_test" />
```

静态加载的Fragment必须设置ID属性，否则程序运行过程中会出现IllegalStateException。`android:name` 属性指明了本容器需要嵌入的Fragment类，需要填写完整的包名。`tools:layout` 属性需填入布局文件的ID，可以在Android Studio的布局预览界面显示目标Fragment，辅助开发者进行布局设计，对程序运行无影响。

Fragment布局文件根元素的宽高属性不能传递给引用者，因此引用Fragment的容器需要明确设置宽高数值或使用"match_parent"，若设置为"wrap_content"等同于"0dp"。

### 动态添加Fragment
我们可以在逻辑代码中动态添加、移除Fragment，这给程序界面设计带来了灵活性。为了嵌入Fragment，我们需要在布局文件中预先准备一个容器，容器类型可以是FragmentContainerView或FrameLayout等，目前官方推荐使用FragmentContainerView。

```xml
<!-- 动态加载Fragment -->
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/container2"
    android:layout_width="match_parent"
    android:layout_height="100dp"
    android:layout_marginTop="10dp" />
```

在Activity中，我们首先需要获取FragmentManager对象，然后操纵Fragment事务，向容器"container2"中添加TestFragment。

```java
// 获取FragmentManager实例
FragmentManager manager = getSupportFragmentManager();
// 获取Fragment事务实例
FragmentTransaction transaction = manager.beginTransaction();
// 添加Fragment
transaction.add(R.id.container2, new TestFragment());
// 提交事务
transaction.commit();
```

FragmentTransaction的 `add()` 方法用于向容器中添加Fragment，第一个参数为目标容器的ID，第二个参数为Fragment实例。

FragmentTransaction的 `commit()` 方法用于提交Fragment事务到主进程，Fragment操作默认是在队列中执行的，这意味着 `commit()` 动作可能不会立刻得到调度。

此时运行示例程序，查看加载了TestFragment的界面：

<div align="center">

**添加图片添加图片添加图片添加图片添加图片添加图片**

</div>

此时静态加载与动态加载的Fragment均呈现在屏幕上。

# 初始参数传递
如果Fragment需要一些初始化信息，我们不能通过构造方法直接传参，因为当屏幕方向改变或类似原因导致Fragment重建时，系统只会调用默认构造方法，然后执行生命周期方法 `onCreate()` ，先前通过含参构造方法传入的参数则会丢失。

正确的传参方法是将数据存入Bundle对象，并使用Fragment的 `setArguments()` 方法传参，Fragment的默认构造方法应当留空，并且我们不应当再定义其他含参构造方法。

```java
public class TestFragment extends Fragment {

    private static final String PARAM_TEXTINFO = "TEXTINFO";
    private String textInfo;

    // 创建Fragment实例的方法，在此处理初始化参数。
    public static TestFragment newInstance(String textInfo) {
        TestFragment fragment = new TestFragment();
        // 将外部参数封装至Bundle
        Bundle args = new Bundle();
        args.putString(PARAM_TEXTINFO, textInfo);
        // 向Fragment传入Bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 从Fragment中获取Bundle对象
        Bundle args = getArguments();
        if (args != null) {
            // 从Bundle中取出参数
            textInfo = args.getString(PARAM_TEXTINFO);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        TextView tv = view.findViewById(R.id.tvContent);
        tv.setText(textInfo);
        return view;
    }
}
```

上述示例代码中，我们通过 `newInstance()` 方法将参数封装进Bundle，并通过 `setArguments()` 方法保存参数，在Fragment的生命周期方法 `onCreate()` 中，我们使用 `getArguments()` 将Bundle取出，保存至全局变量中以便后续使用。

当我们需要使用TestFragment时，就可以调用 `newInstance()` 方法新建实例。

```java
// 使用"newInstance()"方法创建Fragment实例
TestFragment fragment = TestFragment.newInstance("初始参数");
```

当这种方式创建的TestFragment实例被添加到界面上时，其内部能够获取到我们传入的初始化参数，文本框内容将会显示为“初始参数”。
