# 简介
ViewPager2是ViewPager的升级版本，与ViewPager相比，它可以设置布局方向，拥有可控预加载、模拟拖拽等方便的功能。ViewPager2基于RecyclerView实现，因此它也具有RecyclerView的所有特性。

ViewPager2是AndroidX中的组件，如果使用指定版本，需要在Gradle配置文件中声明依赖：

```groovy
dependencies {
    implementation 'androidx.viewpager2:viewpager2:1.0.0'
}
```

# 基本应用
首先我们创建一个TestFragment作为页面模板，其中仅有一个居中的TextView，初始化参数将被设置在该TextView中，相关逻辑代码略。

fragment_test.xml:

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/tvContent"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center" />
</FrameLayout>
```

ViewPager2所使用的适配器是FragmentStateAdapter，我们创建它的子类Adapter01并覆盖它的部分方法。

```java
public class Adapter01 extends FragmentStateAdapter {

    // 数据源List
    private final List<TestFragment> pages;

    // 构造方法
    public Adapter01(FragmentActivity activity, List<TestFragment> pages) {
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

ViewPager2初始加载时，将会调用"getItemCount()"方法，我们需要在此返回Fragment的总数量。需要显示某个页面时，ViewPager2会调用"createFragment()"方法，"position"参数表示页面索引号，我们根据索引取出Fragment实例，并通过返回值传递给ViewPager2。

接着在Activity的布局文件中添加ViewPager2控件。FragmentStateAdapter实例化布局文件时调用了具有两个参数的"inflate()"方法，因此"fragment_test.xml"根布局的宽高属性将会失效，需要在ViewPager2标签处声明，此处我们为ViewPager2设置固定高度："150dp"。

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/vp2Content"
        android:layout_width="match_parent"
        android:layout_height="150dp" />
</FrameLayout>
```

最后我们在Activity中创建Fragment实例与适配器，完成ViewPager2的配置。

```java
// 创建测试页面
List<TestFragment> pages = new ArrayList<>();
for (int i = 0; i < 6; i++) {
    pages.add(TestFragment.newInstance("页面" + (i + 1)));
}

// 初始化
ViewPager2 viewPager2 = findViewById(R.id.vp2Content);
// 为ViewPager设置适配器
Adapter01 adapter = new Adapter01(this, pages);
viewPager2.setAdapter(adapter);
```

运行程序，查看显示效果：

<div align="center">

![ViewPager2的基本应用](./03-Assets/基本应用-01-ViewPager2的基本应用.gif)

</div>

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

![纵向布局](./03-Assets/外观定制-01-纵向布局.gif)

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

# 监听器
## 页面滑动监听器
ViewPager2的页面滑动监听器与ViewPager一致，使用 `registerOnPageChangeCallback()` 方法进行设置，此处省略相关描述。

与ViewPager不同的是，ViewPager2的页面滑动监听器是抽象类，我们可以按需实现其中的方法，不必全部实现。

# 预加载
ViewPager2可以预加载可见页面两侧的页面，以提升切换时画面的连续性。ViewPager2的"setOffscreenPageLimit()"方法用于控制预加载的页面数量，默认值为"-1"，即不进行预加载。当该数值设为"N"时，将会预加载可见页面两侧的N个页面，使它们的生命周期达到"onStart()"。

我们为Fragment的生命周期方法添加日志，然后使用默认预加载参数，运行程序并查看日志。

```log
2022-06-13 23:57:16.106 2553-2553/net.bi4vmr.study I/myapp: 页面1-onCreate()
2022-06-13 23:57:16.106 2553-2553/net.bi4vmr.study I/myapp: 页面1-onCreateView()
2022-06-13 23:57:16.110 2553-2553/net.bi4vmr.study I/myapp: 页面1-onViewCreated()
2022-06-13 23:57:16.111 2553-2553/net.bi4vmr.study I/myapp: 页面1-onViewStateRestored()
2022-06-13 23:57:16.111 2553-2553/net.bi4vmr.study I/myapp: 页面1-onStart()
2022-06-13 23:57:16.112 2553-2553/net.bi4vmr.study I/myapp: 页面1-onResume()
```

此时仅有“页面1”的生命周期到达"onResume()"，其它页面都没有被加载。我们将ViewPager2滑动至第二个页面，再次查看日志。

```log
2022-06-13 23:57:28.181 2553-2553/net.bi4vmr.study I/myapp: 页面2-onCreate()
2022-06-13 23:57:28.181 2553-2553/net.bi4vmr.study I/myapp: 页面2-onCreateView()
2022-06-13 23:57:28.184 2553-2553/net.bi4vmr.study I/myapp: 页面2-onViewCreated()
2022-06-13 23:57:28.185 2553-2553/net.bi4vmr.study I/myapp: 页面2-onViewStateRestored()
2022-06-13 23:57:28.185 2553-2553/net.bi4vmr.study I/myapp: 页面2-onStart()
2022-06-13 23:57:28.995 2553-2553/net.bi4vmr.study I/myapp: 页面1-onPause()
2022-06-13 23:57:28.995 2553-2553/net.bi4vmr.study I/myapp: 页面2-onResume()
```

此时可以发现仅“页面2”加载并可见，没有预加载右侧的“页面3”。

我们将预加载页面数量设为"1"，重新运行程序并再次查看日志。

```log
2022-06-14 00:06:42.188 2644-2644/net.bi4vmr.study I/myapp: 页面1-onCreate()
2022-06-14 00:06:42.189 2644-2644/net.bi4vmr.study I/myapp: 页面1-onCreateView()
2022-06-14 00:06:42.192 2644-2644/net.bi4vmr.study I/myapp: 页面1-onViewCreated()
2022-06-14 00:06:42.193 2644-2644/net.bi4vmr.study I/myapp: 页面1-onViewStateRestored()
2022-06-14 00:06:42.193 2644-2644/net.bi4vmr.study I/myapp: 页面1-onStart()
2022-06-14 00:06:42.194 2644-2644/net.bi4vmr.study I/myapp: 页面1-onResume()
2022-06-14 00:06:42.198 2644-2644/net.bi4vmr.study I/myapp: 页面2-onCreate()
2022-06-14 00:06:42.198 2644-2644/net.bi4vmr.study I/myapp: 页面2-onCreateView()
2022-06-14 00:06:42.201 2644-2644/net.bi4vmr.study I/myapp: 页面2-onViewCreated()
2022-06-14 00:06:42.201 2644-2644/net.bi4vmr.study I/myapp: 页面2-onViewStateRestored()
2022-06-14 00:06:42.202 2644-2644/net.bi4vmr.study I/myapp: 页面2-onStart()
```

此时“页面1”被加载，与它相邻的“页面2”也被加载了，生命周期到达"onStart()"。

我们将ViewPager2滑动至第二个页面，再次查看日志。

```log
2022-06-14 00:07:04.129 2644-2644/net.bi4vmr.study I/myapp: 页面3-onCreate()
2022-06-14 00:07:04.129 2644-2644/net.bi4vmr.study I/myapp: 页面3-onCreateView()
2022-06-14 00:07:04.132 2644-2644/net.bi4vmr.study I/myapp: 页面3-onViewCreated()
2022-06-14 00:07:04.132 2644-2644/net.bi4vmr.study I/myapp: 页面3-onViewStateRestored()
2022-06-14 00:07:04.133 2644-2644/net.bi4vmr.study I/myapp: 页面3-onStart()
2022-06-14 00:07:04.611 2644-2644/net.bi4vmr.study I/myapp: 页面1-onPause()
2022-06-14 00:07:04.611 2644-2644/net.bi4vmr.study I/myapp: 页面2-onResume()
```

此时“页面2”的生命周期到达"onResume()"，“页面3”进行了预加载，生命周期到达"onStart()"。

# 缓存与复用
由于ViewPager2基于RecyclerView实现，它也具有页面缓存与复用机制，如果我们使用不当，页面显示就可能会与预期不符。

使用FragmentStateAdapter时，我们只重写"getItemCount()"与"createFragment()"方法，先加载数据源FragmentListA，并从第一页翻至第二页；然后替换数据源为FragmentListB，再翻回第一页，就会发现第一页仍然是FragmentListA中的页面。

FragmentStateAdapter中有"getItemId()"方法，ViewPager2使用此方法的返回值作为Key缓存Fragment实例，默认实现中返回Fragment对应的Position，我们替换数据源并翻回第一页时，ViewPager2发现缓存中已有"ID=Position=0"的Fragment实例，就会直接进行复用，并不会执行"createFragment()"方法创建新的Fragment。

如果FragmentStateAdapter的数据集需要动态变更，我们一定要重写"getItemId()"和"containsItem()"方法，防止页面复用造成显示异常。

```java
public class Adapter01 extends FragmentStateAdapter {

    // 数据源List
    private final List<TestFragment> pages;
    // IDList
    private final List<Long> idList;

    【此处省略部分方法...】

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

开始模拟拖拽前我们需要调用"beginFakeDrag()"方法，然后调用"fakeDragBy(offsetPX)"方法设置偏移量，参数的的单位是像素，数值为正表示向前一个页面滑动，数值为负表示向后一个页面滑动。滑动结束后，我们应当调用"endFakeDrag()"方法结束模拟拖拽状态。

```java
// 开始模拟拖拽
viewPager2.beginFakeDrag();
// 向后一个页面偏移1080像素
viewPager2.fakeDragBy(-1080);
// 终止模拟拖拽
viewPager2.endFakeDrag();
```

模拟拖拽方法执行后页面会瞬间偏移到相应的位置，没有过渡动画，如果需要页面缓慢移动的视觉效果，我们可以使用定时任务进行小步长的多次偏移。