# 概述
Context的中文名称为上下文、环境，是指与当前应用相关的全局信息，通过Context我们可以获取资源、获取系统服务、显示Toast等。

Context相关组件的继承关系如下图所示：

<div align="center">

<!-- TODO ![]() -->

</div>

Context类本身是一个抽象类，它有两个具体的实现类ContextImpl和ContextWrapper。其中ContextWrapper类只是一个外层包装，其构造方法必须包含一个Context实现对象，它有三个具体实现。其中ContextThemeWrapper包含与主题相关的接口，典型实现类为Activity；而Application和Service则没有界面，不需要主题。

# 作用域
Context的具体类型将会限制其功能，我们在使用时需要注意。

当我们使用非Activity的Context开启新Activity时，将会产生错误，因为系统找不到与Context相关联的Task，此时需要增加FLAG_ACTIVITY_NEW_TASK标志位。

当我们初始化Dialog时，只能使用Activity，因为系统不允许弹窗随意出现，必须依附于某个Activity。

当我们初始化XML时，应当使用Activity，否则可能导致自定义的主题不生效，因为其他Context没有主题属性。

下文表格展示了各种类型Context的能力：

<div align="center">

|   用途/类型    | Application | Service | Activity |
| :------------: | :---------: | :-----: | :------: |
|    显示弹窗    |      ×      |    ×    |    √     |
|  启动Activity  |   不建议    | 不建议  |    √     |
|    渲染布局    |   不建议    | 不建议  |    √     |
|  启动Service   |      √      |    √    |    √     |
|    发送广播    |      √      |    √    |    √     |
| 注册广播接收器 |      √      |    √    |    √     |
|    加载资源    |      √      |    √    |    √     |

</div>

# 生命周期
## 简介
各种Context实现类的生命周期是不同的，如果我们使用不当，将会干扰资源回收机制，导致内存泄露。

## 异步请求
如果我们要在一个网络请求中使用Context，则应当使用Application，它的生命周期是整个应用程序。

假如我们使用Activity进行操作，当界面关闭但网络请求一直挂起时，Activity实例就被请求方法持有，导致无法被回收。

## 静态变量
部分工具类需要持有一个静态的Context变量，例如网络请求工具。

当我们初始化这种工具类时，应当使用 `getApplicationContext()` 方法获取Application，因为应用级的Context与工具类生命周期一致，不会干扰资源回收机制；如果使用Activity等，则会导致它们无法被正常回收。

## 总结
当我们使用Context时，应当遵循以下规则：

1. 尽量使用Application的Context。
2. 当工具类封装了其他需要Context的方法调用时，最好使用参数透传Context，不要使用全局变量存储Context对象。
3. 当工具类需要持有静态变量时，应当调用 `getApplicationContext()` 方法获取Application，防止使用者误操作传入Activity或Service。
