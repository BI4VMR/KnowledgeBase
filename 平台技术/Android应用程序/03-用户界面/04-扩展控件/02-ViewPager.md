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
        android:layout_height="wrap_content" />
</FrameLayout>
```

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

