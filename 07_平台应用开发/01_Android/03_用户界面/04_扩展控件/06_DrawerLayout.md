# 简介
DrawerLayout是一款用于实现侧滑抽屉效果的控件。

# 基本应用
DrawerLayout需要作为XML的根布局，原本的页面根布局放置在第一个子节点，随后的两个节点分别对应左侧抽屉、右侧抽屉，不需要右侧抽屉时也可以省略。

ui_demo_base.xml:

```xml
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/layoutDrawer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center_horizontal"
    android:orientation="vertical"
    tools:ignore="HardcodedText">

    <!-- 主布局（原本页面的根布局） -->
    <LinearLayout
        android:id="@+id/layoutMain"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <Button
            android:id="@+id/btnPopStart"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="20dp"
            android:layout_marginEnd="20dp"
            android:text="弹出侧边栏（左）" />

        <Button
            android:id="@+id/btnPopEnd"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="20dp"
            android:layout_marginEnd="20dp"
            android:text="弹出侧边栏（右）" />
    </LinearLayout>

    <!-- 左侧抽屉 -->
    <LinearLayout
        android:id="@+id/leftLayout"
        android:layout_width="250dp"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        android:background="#F0F"
        android:orientation="horizontal" />

    <!-- 右侧抽屉 -->
    <LinearLayout
        android:id="@+id/rightLayout"
        android:layout_width="250dp"
        android:layout_height="match_parent"
        android:layout_gravity="end"
        android:background="#FF0"
        android:orientation="horizontal" />
</androidx.drawerlayout.widget.DrawerLayout>
```

左右抽屉布局的根节点需要分别添加 `android:layout_gravity="start"` 属性和 `android:layout_gravity="end"` 属性。

我们在主界面中使用两个按钮分别控制左右抽屉的开启。

DemoBaseUI.java:

```java
Button btnPopStart = findViewById(R.id.btnPopStart);
btnPopStart.setOnClickListener(v -> {
    // 弹出左侧抽屉
    layoutDrawer.openDrawer(GravityCompat.START);
});

Button btnPopEnd = findViewById(R.id.btnPopEnd);
btnPopEnd.setOnClickListener(v -> {
    // 弹出右侧抽屉
    layoutDrawer.openDrawer(GravityCompat.END);
});
```

此时我们可以运行示例程序，点击按钮开启抽屉页面。

<!-- TODO -->

# 外观定制
## 常用方法
🔷 `void open()`

弹出页面起始端方向的抽屉。

🔷 `void openDrawer(int gravity)`

弹出参数"gravity"所指定方向的抽屉。

参数可以是 `GravityCompat.START` 或 `GravityCompat.END` 。

🔷 `void openDrawer(int gravity, boolean animate)`

弹出参数"gravity"所指定方向的抽屉，并且设置动画效果是否开启。

🔷 `void close()`

收起页面起始端方向的抽屉。

🔷 `void isDrawerOpen(int gravity)`

判断参数"gravity"所指定方向的抽屉是否已经弹出。

# 监听器
## DrawerListener
DrawerListener用于监听侧滑界面的状态。

```java
// 设置抽屉状态监听器
layoutDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        // 当侧滑界面完全打开时触发一次
        Log.i("myapp", "OnDrawerOpened.");
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        // 当侧滑界面完全关闭时触发一次
        Log.i("myapp", "OnDrawerClosed.");
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        // 当侧滑界面滑动过程中持续触发，参数"slideOffset"为滑动偏移量。
        Log.i("myapp", "OnDrawerSlide. Offset:[" + slideOffset + "]");
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        // 当侧滑界面滑动状态改变时触发。
        Log.i("myapp", "OnDrawerStateChanged. State:[" + newState + "]");
    }
});
```
