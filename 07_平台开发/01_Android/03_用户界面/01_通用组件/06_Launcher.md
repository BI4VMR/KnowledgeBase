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


-->
