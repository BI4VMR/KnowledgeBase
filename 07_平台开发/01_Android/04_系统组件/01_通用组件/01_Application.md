# 简介
Application代表应用程序本身，也是应用程序的一个必备组件，每个应用程序都有Application对象，可以用来初始化全局组件以及存放全局属性。

默认情况下，系统将使用内置的Application类初始化，并且全局只会存在一个实例，它的生命周期跟随整个应用程序。当应用程序拥有多个进程时，每个进程都会拥有一个Application对象。

# 自定义Application
我们可以通过自定义Application实现一些定制化的逻辑，首先需要创建一个类并继承Application。


```java
public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("myapp", "OnCreate.");
    }
}
```

然后在Manifest文件中添加配置，指定使用我们自定义的类。

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="net.bi4vmr.study">

    <application
        android:name=".base.CustomApplication">

    </application>
</manifest>
```

# 常用方法
🔷 `onCreate()` 方法

在Application创建的时候被调用，用于执行初始化操作。

🔷 `onLowMemory()` 方法

当后台程序都被清理但内存仍不充足时，前台应用将会收到此回调，此处可以释放一些资源或提醒用户系统资源不足。

🔷 `onTrimMemory(int level)` 方法

当系统准备清理内存时，会向各应用程序（包括前台与后台）回调此事件，参数表示回收优先级。

🔷 `registerActivityLifecycleCallbacks()` 方法

注册Activity生命周期监听器，此处可以收到应用程序中所有Activity的生命周期变化事件。

🔷 `unregisterActivityLifecycleCallbacks()` 方法

注销Activity生命周期监听器。
