# 简介

# 基本应用

# 外观定制
## 基本样式


🔷 `app:tabTextAppearance=[样式ID]`
<br />
设置Tab中文字的样式。





# 监听器
## 控件点击事件监听器
Chip每次被点击都会触发点击事件，这与其他控件是类似的，此处省略相关描述。

## 选中状态监听器
Chip具有“已选中/未选中”两种自锁状态，每当被用户点击时，都会切换为相反的状态并保持。如果一个Chip的"checkable"属性为"true"，我们就可以通过注册OnCheckedChangeListener监听Chip选中状态的变化。

```java
// 注册选中状态监听器
chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
    // 打印日志
    Log.i("myapp", "Chip的选中状态发生改变，新状态：" + isChecked);
});
```

该监听器只有一个 `onCheckedChanged(CompoundButton buttonView, boolean isChecked)` 回调方法，每当按钮选中状态发生改变时触发，其中参数"buttonView"表示被点击的控件实例；参数"isChecked"表示Chip被点击后的选中状态。

## 关闭按钮点击事件监听器
如果一个Chip的"closeIconVisible"属性为"true"，我们就可以注册OnCloseIconClickListener监听关闭按钮的点击事件。

```java
chipEntry.setOnCloseIconClickListener(v -> {
    // 打印日志
    Log.i("myapp", "Chip的关闭按钮被点击。");
});
```

# ChipGroup
## 简介
ChipGroup是用于容纳多个Chip控件的容器，可以控制Chip的排布方式，也可以实现单选与多选的控制逻辑。

> ⚠️ 警告
>
> ChipGroup是ViewGroup的子类，我们甚至能够向其中添加非Chip控件，这可能会引起显示错误与其他不可预知的异常，因此不建议这样操作。

## 排布方式
默认情况下ChipGroup采用流式布局，所有Chip按顺序横向排列，当本行宽度不足时，则换行进行排列。

<div align="center">

**插入图片插入图片插入图片插入图片插入图片插入图片插入图片插入图片插入图片插入图片插入图片**

<!-- ![TabLayout示例](./Assets-TabLayout/基本应用-TabLayout示例.gif) -->

</div>

如果我们需要单行显示所有Chip，可以为ChipGroup设置属性 `app:singleLine=true` ，此时ChipGroup的宽度为所有Chip以及间距之和，超出屏幕宽度的内容将被截断。我们应当将这种ChipGroup放置在HorizontalScrollView中，允许用户左右滑动以查看所有Chip。

```xml
<!-- 水平滚动容器 -->
<HorizontalScrollView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">

    <!-- 单行ChipGroup -->
    <com.google.android.material.chip.ChipGroup
        android:id="@+id/chipgroupSingle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:singleLine="true">

        <com.google.android.material.chip.Chip
            style="@style/Widget.MaterialComponents.Chip.Filter"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="#00000001" />

        <!-- 省略其他子元素... -->

    </com.google.android.material.chip.ChipGroup>
</HorizontalScrollView>
```

此时我们可以通过左右滑动手势查看所有Chip。

<div align="center">

**插入图片插入图片插入图片插入图片插入图片插入图片插入图片插入图片插入图片插入图片插入图片**

<!-- ![TabLayout示例](./Assets-TabLayout/基本应用-TabLayout示例.gif) -->

</div>

## 多选模式
ChipGroup的默认状态即多选模式，我们可以向ChipGroup注册OnCheckedStateChangeListener以监听所有Chip的选中状态。

```java
// 设置Chip选中状态改变监听器
chipgroupMulti.setOnCheckedStateChangeListener((group, checkedIds) -> {
    // 参数"checkedIds"是当前所有选中项ID的列表，并不是项的索引。
    Log.i("myapp", "多选列表选中状态改变。 CheckedList:" + checkedIds);
});
```

OnCheckedStateChangeListener只有一个 `onCheckedChanged(ChipGroup group, List<Integer> checkedIds);` 回调方法，每当有Chip的选中状态发生改变时都会触发，其中参数"group"表示ChipGroup实例，参数"checkedIds"表示当前所有被选中的控件ID列表。

除了使用监听器，我们还可以调用ChipGroup的 `List<Integer> getCheckedChipIds()` 方法实时获取所有选中Chip的控件ID。

## 单选模式
ChipGroup的 `setSingleSelection()` 方法用于控制是否为单选模式，我们可以传入"true"从多选模式切换至单选模式。

单选模式的选中状态监听器也是OnCheckedStateChangeListener，但此时参数"checkedIds"最多只有一个控件ID，没有Chip被选中时，将会返回空列表。

我们也可以通过ChipGroup的 `int getCheckedChipId()` 方法实时获取当前被选中Chip的控件ID，没有Chip被选中时，将会返回"-1"。该方法不应当在多选模式下使用，如果被调用则始终返回"-1"。

有时我们需要使ChipGroup始终有被选中的Chip，此时可以在选中表项长度为0时，主动将某个Chip选中，此处以默认选中第一项为例。

```java
// 设置单选模式
chipgroupSingle.setSingleSelection(true);
// 设置Chip选中状态改变监听器
chipgroupSingle.setOnCheckedStateChangeListener((group, checkedIds) -> {
    // 参数"checkedIds"是当前所有选中项ID的列表，并不是项的索引。
    Log.i("myapp", "单选列表选中状态改变。 CheckedList:" + checkedIds);
    // 单选列表默认允许全都不选中，如果我们希望永远有选中的Item，可以在List长度为0时选中某一项。
    if (checkedIds.size() == 0) {
        // 此处以选中第一项为例
        View view = group.getChildAt(0);
        if (view instanceof Chip) {
            Chip chip = (Chip) view;
            chip.setChecked(true);
        }
    }
});
```

## 动态增减表项
我们有时需要从服务器动态获取数据并通过Chip展示，由于ChipGroup的封装程度不够高，我们需要通过ViewGroup的相关接口进行操作。


