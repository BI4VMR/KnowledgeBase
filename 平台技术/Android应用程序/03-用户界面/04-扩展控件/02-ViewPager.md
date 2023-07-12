# 简介
ViewPager是一种多页面翻页显示控件，我们可以通过适配器向ViewPager填充多个Fragment，以显示不同的页面。

# 基本应用
首先我们创建一个测试Fragment，在其中放置一个文本框，此处省略相关代码，详见示例程序。

ViewPager使用适配器组织页面，我们通常继承FragmentStatePagerAdapter并重写其中的方法。

```java
public class MyPagerAdapter extends FragmentStatePagerAdapter {

    // 数据源List
    private final List<TestFragment> pages;

    // 构造方法
    public MyPagerAdapter(@NonNull FragmentManager fm, List<TestFragment> pages) {
        super(fm);
        this.pages = pages;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return pages.get(position);
    }

    @Override
    public int getCount() {
        return pages.size();
    }
}
```

此处假设每个页面都是固定的，我们在初始化MyPagerAdapter时使用List存放Fragment实例；当系统加载ViewPager时，首先调用 `getCount()` 方法获取总的页面数量；然后判断需要预加载的页面数量，调用 `getItem()` 方法获取对应位置的Fragment实例，将其显示到屏幕上。

准备好适配器后，我们创建一个测试Activity，并在布局中放置ViewPager并加载数据。

ui_demo_base.xml:

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.viewpager.widget.ViewPager
        android:id="@+id/vpTest"
        android:layout_width="match_parent"
        android:layout_height="150dp" />
</FrameLayout>
```

ViewPager的高度只能是"match_parent"或者指定的数值，如果设为"wrap_content"效果等同于"match_parent"。

DemoBaseUI.java:

```java
// 创建测试页面
List<TestFragment> pages = new ArrayList<>();
for (int i = 0; i < 10; i++) {
    pages.add(TestFragment.newInstance("页面" + (i + 1)));
}

ViewPager vpTest = findViewById(R.id.vpTest);
// 将适配器与ViewPager绑定
vpTest.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), pages));
```

我们在Activity中初始化10个TestFragment，将其通过适配器依次添加到ViewPager中，此时运行示例程序，查看显示效果。

<!-- TODO 添加GIF -->

# 监听器
## OnPageChangeListener
OnPageChangeListener用于监听ViewPager的页面滑动过程，每当页面进行切换时其中的事件被触发。

我们可以使用ViewPager的 `addOnPageChangeListener(ViewPager.OnPageChangeListener l)` 方法注册监听器，同一个ViewPager支持注册多个监听器。当我们不再需要某个监听器时，我们可以调用ViewPager的 `removeOnPageChangeListener(ViewPager.OnPageChangeListener l)` 方法注销监听器。

OnPageChangeListener拥有三个回调方法，它们的用途可参考下文：

🔷 `onPageScrollStateChanged(int state)`

当滑动状态改变时，此事件触发。

参数"state"表示滑动状态，数值对应ViewPager的"SCROLL_STATE"系列常量。

- SCROLL_STATE_IDLE(0)表示滑动执行完毕，动画播放结束，目标页面已经完全显示。
- SCROLL_STATE_DRAGGING(1)表示用户正在用手指按住屏幕，正在进行拖拽。
- SCROLL_STATE_SETTLING(2)表示用户抬起手指，随后ViewPager将向目标页面自动滑动使其完全显示。

🔷 `onPageScrolled(int position, float positionOffset, int positionOffsetPixels)`

当页面正在切换时，此事件以较高频率被触发。

参数"position"表示当前正在滑动的页面索引。参数"positionOffset"表示当前页面滑动至目标位置的进度，取值范围为 `[0, 1)` ，数值越大则离目标位置越近。参数"positionOffsetPixels"表示当前页面滑过的像素，取值范围为 `[0, <ViewPager宽度>)` 。

🔷 `onPageSelected(int position)`

当滑动完毕最终选定某个页面时，此事件触发。

参数"position"表示页面在ViewPager中的索引。

<br />

当我们用手指按住屏幕并开始滑动时，控件首先触发一次 `onPageScrollStateChanged(1)` ，然后不停的触发 `onPageScrolled()` 汇报页面滑动位置；当我们松开手指时，控件将会触发一次 `onPageScrollStateChanged(2)` ，然后触发一次 `onPageSelected()` 汇报目标页面索引，随后不停的触发 `onPageScrolled()` 直到页面完全进入目标位置，最后触发一次 `onPageScrollStateChanged(0)` 表示滑动终止。

ViewPager除了可以由用户手动逐页滑动，还可以通过 `setCurrentItem(int index, false)` 方法禁止动画直接显示目标页面，这种方式只会触发一次 `onPageSelected()` 汇报目标页面，以及一次 `onPageScrolled()` 事件，"index"值为目标页面，其余值均为"0"。

# 适配器
ViewPager提供了两种内置Adapter用于容纳Fragment，它们的接口是相同的，但是页面缓存行为有差异。

当页面滑出预加载区域之后，FragmentStatePagerAdapter会使用视图数据保持机制保存数据，然后销毁Fragment实例，当用户再次进入页面时，再重新创建Fragment实例并恢复数据。事务提交后， activity的FragmentManager中的fragment会被彻底移除。 FragmentStatePagerAdapter类名中的“state”表明：在销毁fragment时，可在onSaveInstanceState(Bundle)方法中保存fragment的Bundle信息。用户切换回来时，保存的实例状态可用来恢复生成新的fragment

FragmentPagerAdapter有不同的做法。对于不再需要的fragment， FragmentPagerAdapter会选择调用事务的detach(Fragment)方法来处理它，而非remove(Fragment)方法。也就是说， FragmentPagerAdapter只是销毁了fragment的视图， fragment实例还保留在FragmentManager中。因此，FragmentPagerAdapter创建的fragment永远不会被销毁

也就是：在destroyItem()方法中，FragmentStatePagerAdapter调用的是remove()方法，适用于页面较多的情况；FragmentPagerAdapter调用的是detach()方法，适用于页面较少的情况。但是有页面数据需要刷新的情况，不管是页面少还是多，还是要用FragmentStatePagerAdapter，否则页面会因为没有重建得不到刷新
