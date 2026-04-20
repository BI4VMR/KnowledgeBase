<!-- TODO

LauncherApps: Launcher应用信息查询工具，是对PackageManager的封装，可以直接查询具有launche属性的应用信息，并且包含名称、图标等属性，便于Launcher应用开发。

LauncherApps.Callback() : 监听应用卸载安装等事件，但该接口是全局的，某个User注册后，其他User的应用变化也会回调给该User，我们一般只关心当前User的应用事件，可以忽略其他User的事件。

-->
