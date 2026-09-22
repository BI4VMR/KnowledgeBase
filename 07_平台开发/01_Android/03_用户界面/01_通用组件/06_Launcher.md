<!-- TODO

LauncherApps: Launcher应用信息查询工具，是对PackageManager的封装，可以直接查询具有launche属性的应用信息，并且包含名称、图标等属性，便于Launcher应用开发。

LauncherApps.Callback() : 监听应用卸载安装等事件，但该接口是全局的，某个User注册后，其他User的应用变化也会回调给该User，我们一般只关心当前User的应用事件，可以忽略其他User的事件。



launcher activity:

<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTask"
    android:stateNotNeeded="true"
    android:clearTaskOnLaunch="true">

    <intent-filter>
        <!-- 程序的入口点 -->
        <action android:name="android.intent.action.MAIN" />

        <!-- 关键：标识该 Activity 为桌面应用 (Home Screen) -->
        <category android:name="android.intent.category.HOME" />
        <!-- 关键：允许处理隐式 Intent -->
        <category android:name="android.intent.category.DEFAULT" />

        <!-- 可选：在其他 Launcher 的应用列表中也显示图标（方便调试） -->
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>



启动其他应用

Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
if (intent != null) {
    // getLaunchIntentForPackage 默认已包含 FLAG_ACTIVITY_NEW_TASK
    startActivity(intent);
}

多个入口
IntentFilter 优先级：如果某个入口 Activity 的 <intent-filter> 中配置了 android:priority 且数值更高，它会排在前面。

Manifest 声明顺序：在优先级相同时，系统解析清单文件会保留声明顺序，即在 AndroidManifest.xml 中最先被写出的那个 Activity 会排在第 1 位被选中。

不设置setpackage如果应用已经启动过会再次显示splash activity
为什么 setPackage(null) 也能解决
setPackage(null) 能解决，原理和 getLaunchIntentForPackage 一样——都是让两次启动的 intent 的 setPackage 状态一致，从而保证系统能复用到旧 task，而不是走"找到的 task 不同 → differentTopTask → 加 FLAG_ACTIVITY_BROUGHT_TO_FRONT + moveTaskToFront"的分支。

关键在于 Task.isSameIntentFilter()（Task.java:1113）：

if (Objects.equals(realActivity, r.mActivityComponent) && this.intent != null) {
    intent.setComponent(this.intent.getComponent());
    if (intent.getSelector() == null) {
        intent.setPackage(this.intent.getPackage());  // ★ 强制对齐 package
    }
}
return intent.filterEquals(this.intent);   // filterEquals 比较 mPackage



包含 FLAG_ACTIVITY_NEW_TASK 自动复用现有任务

与
FLAG_ACTIVITY_RESET_TASK_IF_NEEDED（栈控制指令）

    谁来设置：Launcher（桌面应用）或开发者。通常与 FLAG_ACTIVITY_NEW_TASK 配合使用。

    核心作用：指示系统在切回或启动 Task 时，“如果满足条件，就按照 Manifest 中的规则重置该任务栈”。

    典型场景：
    当用户在桌面点击应用图标启动应用时，桌面 Launcher 默认会加上这个 Flag。如果应用已经在后台运行（栈内可能堆叠了多个页面），系统会检查是否需要根据 AndroidManifest.xml 中的配置清理任务栈。

    结合 Manifest 的重置规则：
    系统会依据清单文件中 Activity 的以下属性来决定如何重置栈：

        clearTaskOnLaunch="true"：每次从桌面重新进入时，强制清除根 Activity 之上的所有 Activity，回到主界面。

        alwaysRetainTaskState="true"：无论后台放置多久，都不重置，始终保留用户离开时的页面状态。

        finishOnTaskLaunch="true"：只要离开该 Task，再次启动时该 Activity 就会被销毁。




FLAG_ACTIVITY_BROUGHT_TO_FRONT（系统设置的通知标记）

    谁来设置：Android 系统本身。开发者通常不需要在 startActivity() 时手动添加此标记。

    核心作用：用于告知 Activity“你是因为重用已存在的实例而被带到了前台”。

    典型场景：
    当一个 Activity 的启动模式为 singleTask 或 singleTop 时，如果系统中已经存在该 Activity 的实例，系统不会重新创建它，而是把它所在的 Task 切到前台，并触发其 onNewIntent(Intent intent)。此时系统会自动在传入的 intent 中加上 FLAG_ACTIVITY_BROUGHT_TO_FRONT 标志。

    实际用途：
    在 Activity 的 onNewIntent() 或 onResume() 中，可以通过检查该标志得知页面是新创建的，还是从后台被“拉”回前台的：


if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
    // 页面是从后台被带到前台的
}

-->
