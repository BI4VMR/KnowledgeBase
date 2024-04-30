# 简介
ViewPager2是ViewPager的升级版本，与ViewPager相比，它新增了可配置的预加载、RTL支持、模拟拖拽等特性。ViewPager2基于RecyclerView实现，因此它也具有RecyclerView的所有特性。

ViewPager2是AndroidX中的组件，如果我们需要使用指定的版本，可以在Gradle配置文件中声明组件依赖：

```groovy
dependencies {
    implementation("androidx.viewpager2:viewpager2:1.0.0")
}
```

本章示例代码详见以下链接：

- [🔗 示例工程：ViewPager2](https://github.com/BI4VMR/Study-Android/tree/master/M03_UI/C04_CtrlExt/S03_ViewPager2)

# 基本应用
我们首先创建 `TestFragment` ，其中包括一个文本框，用于显示构造实例时传入的名称标识，此处省略相关代码，详见示例程序。

ViewPager2所使用的适配器是FragmentStateAdapter，我们创建它的子类MyVPAdapter并重写其中的一些方法。

"MyVPAdapter.java":

```java
public class MyVPAdapter extends FragmentStateAdapter {

    // 数据源List
    private final List<TestFragment> pages;

    // 构造方法
    public MyVPAdapter(FragmentActivity activity, List<TestFragment> pages) {
        super(activity);
        this.pages = pages;
    }

    /* 创建Fragment实例 */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return pages.get(position);
    }

    /* 获取页面数量 */
    @Override
    public int getItemCount() {
        return pages.size();
    }
}
```

我们在初始化MyVPAdapter时使用List存放Fragment实例；当系统加载ViewPager2时，首先回调 `getItemCount()` 方法获取总的页面数量；然后回调 `createFragment()` 方法加载当前显示的页面及需要预加载的页面。

然后我们在测试Activity的布局文件中放置ViewPager2控件：

"testui_base.xml":

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewpager2"
        android:layout_width="match_parent"
        android:layout_height="150dp" />
</FrameLayout>
```

ViewPager2的高度通常是"match_parent"或者确定的数值，如果设为"wrap_content"可能无法达到预期的效果。

我们在测试Activity中初始化10个TestFragment，将它们通过适配器添加到ViewPager2中。

"TestUIBase.java":

```java
// 创建测试页面
List<TestFragment> pages = new ArrayList<>();
for (int i = 0; i < 10; i++) {
    pages.add(TestFragment.newInstance("页面" + (i + 1)));
}

ViewPager2 viewPager2 = findViewById(R.id.vp2Content);
// 为ViewPager设置适配器
MyVPAdapter adapter = new MyVPAdapter(this, pages);
viewPager2.setAdapter(adapter);
```

此时运行示例程序，并查看界面外观：

<div align="center">
<!-- TODO 添加GIF -->
<!-- ![ViewPager2示例](./Assets-ViewPager2/基本应用-ViewPager2示例.gif) -->

</div>

<!-- TODO
# 外观定制
## 页面排列方向
ViewPager2默认将页面按水平方向排列，它提供了简单易用的方向变更API，调整页面排列方向十分方便。

我们在XML中使用 `android:orientation="<vertical | horizontal>"` 属性可以调整ViewPager2的页面排列方向，对应的Java方法如下文所示。

```java
// 获取排列方向
viewPager2.getOrientation();

// 设置排列方向为垂直
ViewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
// 设置排列方向为水平
ViewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
```

纵向滚动样式：

<div align="center">

![纵向布局](./Assets-ViewPager2/外观定制-纵向布局.gif)

</div>

## RTL支持
ViewPager2在横向排列时，能够适应RTL布局，我们可以通过 `android:layoutDirection="<横向排列方式>"` 属性进行配置。该属性默认值为"ltr"，即从左到右排列；设置为"rtl"表示从右到左排列；设置为"inherit"表示继承父级容器的属性；设置为"locale"表示跟随系统语言。

布局方向属性对应的Java方法如下文所示：

```java
// 获取布局方向
viewPager2.getLayoutDirection();
// 设置布局方向
viewPager2.setLayoutDirection(View.LAYOUT_DIRECTION_*);
```

## 边缘阴影效果
当滑动至首尾页面的边缘时，ViewPager2与RecyclerView的行为是一致的，它会产生一个阴影效果提醒用户滑动至边界了。ViewPager2并没有提供方法控制阴影的开关，在XML中设置 `android:overScrollMode="never"` 属性是无效的，但我们可以获取ViewPager2内部的RecyclerView实例并设置相关属性。

```java
// 获取RecyclerView实例
View rv = viewPager2.getChildAt(0);
if (rv instanceof RecyclerView) {
    // 关闭边缘阴影效果
    rv.setOverScrollMode(View.OVER_SCROLL_NEVER);
}
```
-->

# 监听器
## OnPageChangeCallback
OnPageChangeCallback用于监听ViewPager2的页面滑动事件，每当页面进行切换时其中的事件将被触发。

我们可以使用ViewPager2的 `registerOnPageChangeCallback(ViewPager2.OnPageChangeCallback cb)` 方法注册监听器，同一个ViewPager2支持注册多个监听器。当我们不再需要某个监听器时，可以调用ViewPager的 `unregisterOnPageChangeCallback(ViewPager2.OnPageChangeCallback cb)` 方法注销监听器。

ViewPager2的页面滑动事件与ViewPager完全一致，此处省略具体描述，详见相关章节： [🧭 ViewPager - OnPageChangeListener](./02_ViewPager.md#onpagechangelistener) 。

ViewPager2的OnPageChangeCallback监听器不是抽象的，我们可以按需重写其中的方法，不必全部实现一遍。

<!-- TODO
# 预加载
ViewPager2可以预加载可见页面两侧的页面，以提升切换时画面的连续性。ViewPager2的 `setOffscreenPageLimit()` 方法用于控制预加载的页面数量，默认值为"-1"，即不进行预加载；当该数值设为"N"时，将会预加载可见页面两侧的N个页面，使它们的生命周期达到"Started"。

我们为Fragment的生命周期方法添加日志，然后使用默认预加载参数，运行程序并查看日志。

```text
2022-06-13 23:57:16.106 2553-2553/? I/myapp: 页面1-onCreate()
2022-06-13 23:57:16.106 2553-2553/? I/myapp: 页面1-onCreateView()
2022-06-13 23:57:16.110 2553-2553/? I/myapp: 页面1-onViewCreated()
2022-06-13 23:57:16.111 2553-2553/? I/myapp: 页面1-onViewStateRestored()
2022-06-13 23:57:16.111 2553-2553/? I/myapp: 页面1-onStart()
2022-06-13 23:57:16.112 2553-2553/? I/myapp: 页面1-onResume()
```

此时仅有“页面1”的生命周期到达"Resumed"，其它页面都没有被加载。

我们将ViewPager2滑动至第二个页面，再次查看日志。

```text
2022-06-13 23:57:28.181 2553-2553/? I/myapp: 页面2-onCreate()
2022-06-13 23:57:28.181 2553-2553/? I/myapp: 页面2-onCreateView()
2022-06-13 23:57:28.184 2553-2553/? I/myapp: 页面2-onViewCreated()
2022-06-13 23:57:28.185 2553-2553/? I/myapp: 页面2-onViewStateRestored()
2022-06-13 23:57:28.185 2553-2553/? I/myapp: 页面2-onStart()
2022-06-13 23:57:28.995 2553-2553/? I/myapp: 页面1-onPause()
2022-06-13 23:57:28.995 2553-2553/? I/myapp: 页面2-onResume()
```

此时可以发现仅“页面2”加载并可见，没有预加载右侧的“页面3”。

接着我们再将预加载页面数量设为"1"，重新运行程序并查看日志。

```text
2022-06-14 00:06:42.188 2644-2644/? I/myapp: 页面1-onCreate()
2022-06-14 00:06:42.189 2644-2644/? I/myapp: 页面1-onCreateView()
2022-06-14 00:06:42.192 2644-2644/? I/myapp: 页面1-onViewCreated()
2022-06-14 00:06:42.193 2644-2644/? I/myapp: 页面1-onViewStateRestored()
2022-06-14 00:06:42.193 2644-2644/? I/myapp: 页面1-onStart()
2022-06-14 00:06:42.194 2644-2644/? I/myapp: 页面1-onResume()
2022-06-14 00:06:42.198 2644-2644/? I/myapp: 页面2-onCreate()
2022-06-14 00:06:42.198 2644-2644/? I/myapp: 页面2-onCreateView()
2022-06-14 00:06:42.201 2644-2644/? I/myapp: 页面2-onViewCreated()
2022-06-14 00:06:42.201 2644-2644/? I/myapp: 页面2-onViewStateRestored()
2022-06-14 00:06:42.202 2644-2644/? I/myapp: 页面2-onStart()
```

此时“页面1”被完全加载至可见，与它相邻的“页面2”也被加载了，其生命周期到达"Started"。

我们将ViewPager2滑动至第二个页面，再次查看日志。

```text
2022-06-14 00:07:04.129 2644-2644/? I/myapp: 页面3-onCreate()
2022-06-14 00:07:04.129 2644-2644/? I/myapp: 页面3-onCreateView()
2022-06-14 00:07:04.132 2644-2644/? I/myapp: 页面3-onViewCreated()
2022-06-14 00:07:04.132 2644-2644/? I/myapp: 页面3-onViewStateRestored()
2022-06-14 00:07:04.133 2644-2644/? I/myapp: 页面3-onStart()
2022-06-14 00:07:04.611 2644-2644/? I/myapp: 页面1-onPause()
2022-06-14 00:07:04.611 2644-2644/? I/myapp: 页面2-onResume()
```

此时“页面2”的生命周期到达"Resumed"，“页面3”进行了预加载，其生命周期到达"Started"。

# 缓存与复用
由于ViewPager2基于RecyclerView实现，它也具有页面缓存与复用机制，如果我们使用不当，页面显示就可能会与预期不符。

当我们需要动态切换数据源时，如果在FragmentStateAdapter中只重写 `getItemCount()` 与 `createFragment()` 方法，先加载数据源FragmentListA，并从第一页翻至第二页；然后替换数据源为FragmentListB，再翻回第一页，就会发现第一页仍然是FragmentListA中的页面。

FragmentStateAdapter中有 `getItemId()` 方法，ViewPager2使用此方法的返回值作为Key缓存Fragment实例，默认实现中返回Fragment对应的Position，我们替换数据源并翻回第一页时，ViewPager2发现缓存中已有"ID=Position=0"的Fragment实例，就会直接进行复用，并不会执行 `createFragment()` 方法创建新的Fragment。

如果FragmentStateAdapter的数据集需要动态变更，我们一定要重写 `getItemId()` 和 `containsItem()` 方法，防止页面复用造成显示异常。

```java
public class MyVPAdapter extends FragmentStateAdapter {

    // 数据源List
    private final List<TestFragment> pages;
    // IDList
    private final List<Long> idList;

    /* 此处省略部分变量与方法... */

    // 系统回调：获取当前项的ID
    @Override
    public long getItemId(int position) {
        return idList.get(position);
    }

    // 系统回调：判断当前页面是否在数据源中
    @Override
    public boolean containsItem(long itemId) {
        return idList.contains(itemId);
    }
}
```

# 滑动控制
ViewPager2提供了滑动功能开关，我们将其关闭后控件不再接受用户输入事件。

```java
// 禁止用户滑动
ViewPager2.setUserInputEnabled(false);,
// 允许用户滑动
ViewPager2.setUserInputEnabled(true);
```

ViewPager2具有模拟拖拽功能，可以通过代码模拟用户滑动页面的过程，此功能即使用户输入被关闭也可以使用。

开始模拟拖拽前我们需要调用 `beginFakeDrag()` 方法，然后调用 `fakeDragBy(offsetPX)` 方法设置偏移量，参数的的单位是像素，数值为正表示向前一个页面滑动，数值为负表示向后一个页面滑动。滑动结束后，我们应当调用 `endFakeDrag()` 方法结束模拟拖拽状态。

```java
// 开始模拟拖拽
viewPager2.beginFakeDrag();
// 向后一个页面偏移1080像素
viewPager2.fakeDragBy(-1080);
// 终止模拟拖拽
viewPager2.endFakeDrag();
```

模拟拖拽方法执行后页面会瞬间偏移到相应的位置，没有过渡动画，如果需要页面缓慢移动的视觉效果，我们可以使用定时任务进行小步长的多次偏移。

-->
