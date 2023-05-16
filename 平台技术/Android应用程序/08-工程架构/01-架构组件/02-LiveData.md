# 简介

LiveData 是一种可观察的数据存储器类。与常规的可观察类不同，LiveData 具有生命周期感知能力
如果观察者（由 Observer 类表示）的生命周期处于 STARTED 或 RESUMED 状态，则 LiveData 会认为该观察者处于活跃状态。。LiveData 只会将更新通知给活跃的观察者。为观察 LiveData 对象而注册的非活跃观察者不会收到更改通知。
您可以注册与实现 LifecycleOwner 接口的对象配对的观察者。有了这种关系，当相应的 Lifecycle 对象的状态变为 DESTROYED 时，便可移除此观察者。这对于 Activity 和 Fragment 特别有用，因为它们可以放心地观察 LiveData 对象，而不必担心泄露


    数据符合页面状态
    不会发生内存泄露
    不会因 activity 停止而导致崩溃
    不再需要手动处理生命周期
    数据始终保持最新状态
    可以用来做资源共享


# 基本应用
我们首先创建一个ViewModel类，用来承载LiveData。

MyViewModel.java:

```java
public class MyViewModel extends ViewModel {

    // 基本类型数值
    private int num = 0;

    // 可变LiveData，其中的数值可以被修改。
    private final MutableLiveData<Integer> numberData = new MutableLiveData<>();
    // 不可变LiveData，仅可被外部观察。
    public final LiveData<Integer> roNumberData = numberData;

    // 数值增加
    public void plus() {
        // 改变数值
        num += 10;
        // 通知观察者数值发生变化
        numberData.setValue(num);
    }

    // 数值减少
    public void minus() {
        // 改变数值
        num -= 10;
        // 通知观察者数值发生变化
        numberData.setValue(num);
    }
}
```

此处我们声明了一个基本数据类型的数值变量"num"，并提供对应的数值增加方法 `plus()` 和数值减少方法 `minus()` 用于修改该变量的值。

我们还声明了两个LiveData变量以供外部观察普通变量"num"的数值变化，它们的泛型为Integer，对应普通变量"num"的类型。其中私有变量"numberData"的类型为MutableLiveData，它的值可以被改变；而公开变量"roNumberData"的类型为LiveData，它的值只能被外部观察但不能修改，我们在变量名前添加"Read Only"的缩写，以便与可变的变量作区分。每当"num"的数值被改变时，我们调用MutableLiveData的 `setValue()` 方法将最新数值通告给观察者。

我们在测试Activity中获取MyViewModel实例，并观察公开的LiveData变量"roNumberData"，当"num"的数值发生变化时，将最新数值刷新到界面上。

DemoBaseUI.java:

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ui_demo_base);

    /* 此处省略部分变量与方法... */

    // 获取当前Activity对应的ViewModel实例
    MyViewModel vm = new ViewModelProvider(this).get(MyViewModel.class);

    // 注册按钮监听器
    btnPlus.setOnClickListener(v -> vm.plus());
    btnMinus.setOnClickListener(v -> vm.minus());

    // 读取LiveData的初始值
    Log.i("myapp", "LiveData初始值：" + vm.roNumberData.getValue());

    // 调用LiveData的"observe()"方法，注册本Fragment为该LiveData的观察者。
    vm.roNumberData.observe(this, new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            Log.i("myapp", "LiveData数值改变：" + integer);
            // 观察到数值改变时，将其更新到控件上。
            tvContent.setText("Num:" + integer);
        }
    });
}
```

我们运行测试Activity，并点击增减按钮改变"num"的数值，然后观察界面控件的变化。

<div align="center">

<!-- ![执行Make生成视图绑定类](./Assets-ViewBinding/基本应用-执行Make生成视图绑定类.jpg) -->

</div>

每当我们点击按钮触发ViewModel中的数值改变方法时，"numberData"就会向所有观察者发送通知，Activity初始化时将自身注册为观察者，因此收到通知时 `onChanged()` 回调就会触发，获得"nun"的最新数值。

# LiveData的特性


如果我们直接对外提供公开的MutableLiveData变量也是可行的，但是这种设计并不被推荐，因为任何组件都可以修改变量的值，不便于排错

# 共享数据

# 生命周期感知

