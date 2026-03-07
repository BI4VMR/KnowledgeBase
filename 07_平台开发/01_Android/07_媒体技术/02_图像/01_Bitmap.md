<!-- TODO

## 简介

Bitmap是Android系统中的图像处理中最重要类之一。Bitmap可以获取图像文件信息，对图像进行剪切、旋转、缩放，压缩等操作，并可以以指定格式保存图像文件。

既然不能直接通过构造方法创建Bitmap，那怎样才能创建Bitmap对象。通常我们可以利用Bitmap的静态方法createBitmap()和BitmapFactory的decode系列静态方法创建Bitmap对象。

    Bitmap的静态方法createBitmap()
    BitmapFactory的decode系列静态方法



Config解析：

    Bitmap.Config.ALPHA_8：颜色信息只由透明度组成，占8位。
    Bitmap.Config.ARGB_4444：颜色信息由透明度与R（Red），G（Green），B（Blue）四部分组成，每个部分都占4位，总共占16位。
    Bitmap.Config.ARGB_8888：颜色信息由透明度与R（Red），G（Green），B（Blue）四部分组成，每个部分都占8位，总共占32位。是Bitmap默认的颜色配置信息，也是最占空间的一种配置。
    Bitmap.Config.RGB_565：颜色信息由R（Red），G（Green），B（Blue）三部分组成，R占5位，G占6位，B占5位，总共占16位。

通常我们优化Bitmap时，当需要做性能优化或者防止OOM（Out Of Memory），我们通常会使用Bitmap.Config.RGB_565这个配置，因为Bitmap.Config.ALPHA_8只有透明度，显示一般图片没有意义，Bitmap.Config.ARGB_4444显示图片不清楚，Bitmap.Config.ARGB_8888占用内存最多。
CompressFormat解析：

    Bitmap.CompressFormat.JPEG：表示以JPEG压缩算法进行图像压缩，压缩后的格式可以是".jpg"或者".jpeg"，是一种有损压缩。
    Bitmap.CompressFormat.PNG：表示以PNG压缩算法进行图像压缩，压缩后的格式可以是".png"，是一种无损压缩。
    Bitmap.CompressFormat.WEBP：表示以WebP压缩算法进行图像压缩，压缩后的格式可以是".webp"，是一种有损压缩，质量相同的情况下，WebP格式图像的体积要比JPEG格式图像小40%。美中不足的是，WebP格式图像的编码时间“比JPEG格式图像长8倍”。




## 优化

注意：

不要缓存Drawable，部分Drawable内部实现会缓存Context的实例的Resource和Theme等信息，缓存它们会导致Activity销毁后无法被回收、主题变更后图像未跟随主题切换等，应当缓存Bitmap。


-->
