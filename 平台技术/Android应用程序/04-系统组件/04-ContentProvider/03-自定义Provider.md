# 简介
应用程序有时需要对外部提供数据接口，此时就可以通过自定义的ContentProvider实现。

# 基本应用
ContentProvider是一个抽象类，我们必须实现一些方法，它们的作用见下文：

🔷 `boolean onCreate()`

当ContentProvider实例化时被调用，用于执行初始化操作，此处不应执行耗时操作。

<!-- ContentProvider的实例化时机 -->

🔷 `String getType(Uri uri)`

外部传入URI，此处返回对应资源的类型，格式通常为MIME；如果不需要使用此功能，也可以返回空值。

🔷 `Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)`

查询方法，将在Binder中被调用，可能同时存在多个调用线程。

<!-- TODO -->

# 启动时机与进程
在默认配置下，ContentProvider将由所在应用程序的进程初始化，并且在该程序启动时自动初始化，系统全局只会生成一个实例。

通过在Manifest文件中添加配置项 `android:multiprocess="<true | false>"` ，可以控制是否允许生成多个实例，它与多进程配置项 `android:process=":<进程名称>"` 组合后，有以下情况：

🔷 存在"process"且"multiprocess"为"true"

ContentProvider不随所在应用程序启动。

当外部组件进行访问时，将在调用者进程中初始化多个实例，不必启动所在应用程序。

🔷 存在"process"且"multiprocess"为"false"

ContentProvider不随所在应用程序启动，当它被调用时，在"process"指定的进程中初始化，并且不会产生多个实例。

🔷 无"process"且"multiprocess"为"true"

ContentProvider随所在应用程序启动，并在主线程初始化一个实例。

当外部组件进行访问时，将在调用者进程中初始化多个实例，不必启动所在应用程序。

🔷 无"process"且"multiprocess"为"false"（默认情况）

ContentProvider随所在应用程序启动，并在主线程初始化一个实例。

当外部组件进行访问时，不会生成新的实例，使用跨进程方式访问。
