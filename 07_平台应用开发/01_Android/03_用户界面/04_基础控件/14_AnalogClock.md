
# 简介

AnalogClock是模拟时钟,继承了view类,重写了OnDraw函数实现显示时钟.

使用时,在xml文件中添加:
代码语言：javascript
复制

    <AnalogClock
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

运行后效果:

image.png

下面来修改三个属性来设定模拟时钟的效果.
AnalogClock的属性

属性
	

描述

android:dial
	

模拟时钟的表背景。

android:hand_hour
	

模拟时钟的表时针。

android:hand_minute
	

模拟时钟的表分针。

根据需求设定这两个属性:
代码语言：javascript
复制

    <AnalogClock
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:dial="@drawable/timg"
        android:hand_minute="@drawable/handle" />