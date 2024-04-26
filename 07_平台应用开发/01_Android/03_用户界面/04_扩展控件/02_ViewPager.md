# 简介
ViewPager是一种多页面组合翻页显示控件，我们向ViewPager中填充多个Fragment后，用户可以使用滑动手势在Fragment之间切换。

# 基本应用
我们首先创建 `TestFragment` ，其中包括一个文本框，用于显示构造实例时传入的名称标识，此处省略相关代码，详见示例程序。

ViewPager使用适配器模式组织页面，我们通常继承FragmentStatePagerAdapter并重写其中的一些方法。

"MyVPAdapter.java":

```java
public class MyVPAdapter extends FragmentStatePagerAdapter {

    // 数据源List
    private final List<TestFragment> pages;

    // 构造方法
    public MyVPAdapter(@NonNull FragmentManager fm, List<TestFragment> pages) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.pages = pages;
    }

    // 获取当前位置的Fragment
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return pages.get(position);
    }

    // 获取页面数量
    @Override
    public int getCount() {
        return pages.size();
    }
}
```

我们在初始化MyPagerAdapter时使用List存放Fragment实例；当系统加载ViewPager时，首先调用 `getCount()` 方法获取总的页面数量；然后判断需要预加载的页面数量，并调用 `getItem()` 方法获取对应位置的Fragment实例，将其显示到屏幕上。

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

我们在Activity中初始化10个TestFragment，将它们通过适配器添加到ViewPager中，此时运行示例程序，查看显示效果。

<!-- TODO 添加GIF -->

# 监听器
## OnPageChangeListener
OnPageChangeListener用于监听ViewPager的页面滑动事件，每当页面进行切换时其中的事件将被触发。

我们可以使用ViewPager的 `addOnPageChangeListener(ViewPager.OnPageChangeListener l)` 方法注册监听器，同一个ViewPager支持注册多个监听器。当我们不再需要某个监听器时，可以调用ViewPager的 `removeOnPageChangeListener(ViewPager.OnPageChangeListener l)` 方法注销监听器。

OnPageChangeListener拥有三个回调方法，它们的详情分别如下文内容所示：

🔷 `onPageScrollStateChanged(int state)`

当滑动状态改变时，该方法将被触发。

唯一参数 `state` 表示滑动状态，数值对应ViewPager的"SCROLL_STATE"系列常量。

- `SCROLL_STATE_IDLE` (0) : 表示滑动完毕，动画播放结束，目标页面已经完全显示。
- `SCROLL_STATE_DRAGGING` (1) : 表示用户用手指按住屏幕，当前正在进行拖拽。
- `SCROLL_STATE_SETTLING` (2) : 表示用户手指已离开屏幕，ViewPager向目标页面自动滑动，以使其完全显示。

🔷 `onPageScrolled(int position, float positionOffset, int positionOffsetPixels)`

当页面正在切换时，该方法以较高频率被触发。

第一参数 `position` 表示当前正在滑动的页面索引。第二参数
`positionOffset` 表示当前页面滑动至目标位置的进度，取值范围为 `[0, 1)` ，数值越大则离目标位置越近。第三参数 `positionOffsetPixels` 表示当前页面滑过的像素数量，取值范围为 `[0, <ViewPager宽度>)` 。

🔷 `onPageSelected(int position)`

当用户滑动完毕最终选定某个页面时，该方法将被触发。

唯一参数 `position` 表示目标页面在ViewPager中的索引。

<br />

当我们用手指按住屏幕并开始滑动时，控件首先触发一次 `onPageScrollStateChanged(1)` ，然后不停的触发 `onPageScrolled()` 汇报页面滑动位置；当我们松开手指时，控件将会触发一次 `onPageScrollStateChanged(2)` ，然后触发一次 `onPageSelected()` 汇报目标页面索引，随后不停的触发 `onPageScrolled()` 直到页面完全进入目标位置，最后触发一次 `onPageScrollStateChanged(0)` 表示滑动终止。

ViewPager除了可以由用户手动逐页滑动，还可以通过 `setCurrentItem(int index, false)` 方法禁止动画直接显示目标页面，这种方式只会触发一次 `onPageSelected()` 汇报目标页面，以及一次 `onPageScrolled()` 事件，"index"值为目标页面，其余值均为"0"。

当ViewPager的惯性滑动时，用户可以

# 适配器
ViewPager提供了两种内置Adapter用于容纳Fragment，它们的接口形式是相同的，但是页面缓存行为有所不同。

对于FragmentStatePagerAdapter，当页面移至预加载区域之外后，Adapter将会使用视图数据保持机制暂存数据，然后销毁整个Fragment实例；当用户再次进入该页面时，Adapter重新创建新的Fragment实例并恢复数据。视图数据保持机制的相关细节可参考以下章节： [🧭 Activity - 视图数据保持](../01-通用组件/02-Activity.md#视图数据保持) 。

对于FragmentPagerAdapter，当页面移至预加载区域之外后，Adapter不会销毁整个Fragment实例，而是将其从视图中Detach，Fragment实例仍然驻留在内存中；当用户再次进入该页面时，Adapter将使用旧的Fragment实例重新创建视图。

如果我们需要显示大量的Fragment，应当使用FragmentStatePagerAdapter；如果我们只需要显示少量固定的页面，则可以使用FragmentPagerAdapter。

# 限制连续滑动




 * ViewPager触摸遮罩控件。
 * <p>
 * 用户可以在ViewPager仍在滑动状态（"onPageScrollStateChanged()"状态不为"SCROLL_STATE_IDLE"）时再次触摸屏幕继续滑动，这在短视频
 * 播放器、复杂转场动效等场景下会导致诸多问题。我们可以通过该控件限制ViewPager每次触摸最多仅能翻一页，禁止连续滑动翻页。
 * <p>
 * 该控件覆盖在ViewPager上层，并保存ViewPager的滑动状态，如果前一次滑动仍未结束，则消耗新到达的触摸事件，实现每次滑动只能翻一页的效果。

"VPTouchMaskView.java":

```java
public class VPTouchMaskView extends View {

    // ViewPager滑动状态
    private boolean vpScrolling = false;

    public VPTouchMaskView(Context context) {
        this(context, null);
    }

    public VPTouchMaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (vpScrolling) {
            // ViewPager滑动时，该View消耗触摸事件，不再分发给下层的ViewPager。
            return true;
        } else {
            return super.dispatchTouchEvent(event);
        }
    }

    /**
     * 更新ViewPager的滑动状态。
     *
     * @param state ViewPager的"SCROLL_STATE_X"系列常量。
     */
    public void updateVPScrollState(int state) {
        // 当ViewPager滑动状态不为SCROLL_STATE_IDLE(0)时，说明滑动未终止。
        vpScrolling = (state != 0);
    }
}
```






"testui_swipe1page.xml":

```xml
        <androidx.viewpager.widget.ViewPager
            android:id="@+id/viewpager"
            android:layout_width="match_parent"
            android:layout_height="150dp" />

        <net.bi4vmr.study.swipe1page.VPTouchMaskView
            android:id="@+id/touchMaskView"
            android:layout_width="match_parent"
            android:layout_height="150dp" />
```





"testui_swipe1page.xml":

```xml
<androidx.viewpager.widget.ViewPager
    android:id="@+id/viewpager"
    android:layout_width="match_parent"
    android:layout_height="150dp" />

<net.bi4vmr.study.swipe1page.VPTouchMaskView
    android:id="@+id/touchMaskView"
    android:layout_width="match_parent"
    android:layout_height="150dp" />
```



"TestUISwipe1Page.java":

```java
// 注册页面滚动监听器
viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

    /* 此处省略部分代码... */

    @Override
    public void onPageScrollStateChanged(int state) {
        binding.touchMaskView.updateVPScrollState(state);
    }
});
```