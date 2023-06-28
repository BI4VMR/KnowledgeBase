# 简介
应用程序有时

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
