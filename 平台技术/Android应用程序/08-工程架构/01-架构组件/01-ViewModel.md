# 简介



# 基本应用
我们首先编写一个MyViewModel类，在它的构造方法中获取一个随机数作为实例标记：

MyViewModel.java:

```java
public class MyViewModel extends ViewModel {

    private String name = null;

    public MyViewModel() {
        Log.i("myapp", "MyViewModel-VM created. Name:" + name);
        name = genRandomID();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.i("myapp", "MyViewModel-OnCleared. Name:" + name);
    }

    public String getVMName() {
        return name;
    }

    // 获取随机ID
    private String genRandomID() {
        return UUID.randomUUID()
                .toString()
                .toUpperCase()
                .substring(0, 6);
    }
}
```

然后我们在测试Activity中获取该ViewModel的实例，并调用其中的方法获取数据。

DemoBaseUI.java:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ui_demo_base);

    // 从当前Activity中获取VM实例
    MyViewModel vm = new ViewModelProvider(this).get(MyViewModel.class);
    Log.i("myapp", "DemoBaseUI-Get VM in Activity:" + vm.getVMName());

    // 初始化Fragment
    getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.container, new TestFragment())
            .commit();
}
```

我们需要使用ViewModelProvider的 `get()` 方法获取ViewModel实例，构造ViewModelProvider时传入的参数是ViewModelStoreOwner接口实现类，这些类拥有存储ViewModel对象的能力； `get()` 方法的参数是我们指定的ViewModel类。此处我们获取了一个MyViewModel实例，存储在DemoBaseUI(Activity)中。

该Activity中包含一个Fragment，我们在其内部也可以尝试获取Activity级别与Fragment级别的MyViewModel实例。

TestFragment.java:

```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 获取Activity对应的ViewModel实例
    MyViewModel activityVM = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
    Log.i("myapp", "TestFragment-Get VM in Activity:" + activityVM.getVMName());

    // 获取Fragment对应的ViewModel实例
    MyViewModel vm = new ViewModelProvider(this).get(MyViewModel.class);
    Log.i("myapp", "TestFragment-Get VM in Fragment:" + vm.getVMName());
}
```

我们首先通过 `requireActivity()` 获取Activity实例，然后通过ViewModelProvider获取到Activity级别的MyViewModel实例；最后获取了Fragment级别的MyViewModel实例。

我们运行示例程序，打开测试Activity，并查看控制台日志输出内容：

```text
2023-05-11 22:26:08.816 11268-11268/net.bi4vmr.study I/myapp: MyViewModel-VM created. Name:C96AC9
2023-05-11 22:26:08.816 11268-11268/net.bi4vmr.study I/myapp: DemoBaseUI-Get VM in Activity:C96AC9
2023-05-11 22:26:08.821 11268-11268/net.bi4vmr.study I/myapp: TestFragment-Get VM in Activity:C96AC9
2023-05-11 22:26:08.821 11268-11268/net.bi4vmr.study I/myapp: MyViewModel-VM created. Name:35355A
2023-05-11 22:26:08.821 11268-11268/net.bi4vmr.study I/myapp: TestFragment-Get VM in Fragment:35355A
```

我们可以观察到Activity首先以自身为容器获取了MyViewModel(C96AC9)实例，然后Fragment以Activity为容器也获取了该实例，系统并未重新进行创建；最后Fragment以自身为容器获取到了MyViewModel(35355A)实例。

接着我们将Fragment从Activity中移除，并查看日志输出内容：

```text
2023-05-11 22:26:18.291 11268-11268/net.bi4vmr.study I/myapp: MyViewModel-OnCleared. Name:35355A
2023-05-11 22:26:18.291 11268-11268/net.bi4vmr.study I/myapp: TestFragment-OnDestroy.
```

我们可以观察到Fragment回收前，其中存储的MyViewModel(35355A)实例被系统回收了， `onCleared()` 方法被系统回调。
