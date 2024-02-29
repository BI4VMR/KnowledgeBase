# 概述
ImageView用于展示图片资源，并且可以对图片进行缩放、着色等处理。

# 基本应用
ImageView在XML文件中的典型配置如下：

```xml
<ImageView
    android:id="@+id/ivTest"
    android:layout_width="100dp"
    android:layout_height="100dp"
    android:background="#DDD"
    android:src="@drawable/<filename>" />
```

显示效果：

<div align="center">

![ImageView示例](./Assets-ImageView/基本应用-ImageView示例.jpg)

</div>

# 外观定制
## 基本样式
🔷 `android:src="[图像资源]"`

设置需要加载的图像资源。

可以引用"src/drawable"等路径下的文件或设置颜色值。若使用图片，图片将被等比缩放并居中放置。

🔷 `android:foreground="[图像资源]"`

指定控件的前景，位于"src"属性上层。若使用图片，图片将被拉伸至与控件等高且等宽。

🔷 `android:background="[图像资源]"`

指定控件的背景，位于"src"属性下层。若使用图片，图片将被拉伸至与控件等高且等宽。

🔷 `android:contentDescription="[内容描述]"`

使用文本描述图片内容。

此功能为视障人士设计，当系统的“无障碍”功能开启后，用户点击控件时系统将朗读此处设置的文本，帮助用户了解图片内容。

若我们不设置此属性，Android Studio将会产生警告，如果确实不需要无障碍功能，可以添加配置： `tools:ignore="ContentDescription"` 将抑制该经过。

🔷 `android:alpha="[透明度系数]"`

设置控件透明度。

指定整个控件的透明度，取值范围：[0,1]，可以填入小数。

🔷 `android:tint="[颜色]"`

对图片进行着色。

取值为颜色字符串或引用。

🔷 `android:scaleType="[缩放模式]"`

缩放控制，默认值为"fitCenter"，各有效值的含义见下文：

🔺 `matrix`

不做任何缩放处理，将图片放置在控件左上角，如果图片大于控件，超出边界的部分将被截断。

🔺 `center`

不做任何缩放处理，将图片中心对齐到控件中心点，如果图片大于控件，超出边界的部分将被截断。

🔺 `centerCrop`

等比缩放图片，直到图片填满控件，如果图片大于控件，超出边界的部分将被截断。

🔺 `centerInside`

等比缩放图片，直到控件可以完全容纳图片，然后将图片居中放置。

🔺 `fitXY`

拉伸图片直至铺满控件，无视图片的比例。

🔺 `fitStart`

等比缩放图片直到匹配控件较短的边，并与控件的首端对齐。

🔺 `fitEnd`

等比缩放图片直到匹配控件较短的边，并与控件的末端对齐。

🔺 `fitCenter`

等比缩放图片直到匹配控件较短的边，并居中放置。

## 常用方法
🔶 `void setImageResource(int resId)`

设置图片资源，参数为资源的ID。

🔶 `void setBackgroundColor(Color color)`

设置背景颜色。

🔶 `void setAlpha(float alpha)`

设置整个控件透明度，取值范围：[0,1]。

🔶 `void setImageAlpha(int alpha)`

设置图片的透明度，取值范围：[0,255]。
