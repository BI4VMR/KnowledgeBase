# 简介
ViewBinding是Jetpack库中的一个组件，它能帮助开发者访问布局XML中的控件，减少"findViewById()"方法的使用，防止ID填写错误导致空指针异常等问题。

ViewBinding的工作原理是通过Gradle生成与布局XML对应的视图绑定Java类，该类包含布局中所有控件的引用，我们可以直接使用"<绑定对象>.<控件对象>"的方式进行访问，而不必再使用"findViewById()"方法和全局变量获取控件对象。

对于一个布局XML文件，视图绑定类的命名规则是：将XML文件名中的每个单词首字母大写，移除下划线("_")与扩展名，然后在末尾添加固定后缀"Binding"。例如XML文件名为："main_activity.xml"，则对应的视图绑定类名为："MainActivityBinding"。

视图绑定类中的控件变量名称由XML中的控件ID转换而来，如果一个控件没有静态声明ID，则不会出现在视图绑定类中。

视图绑定类中的控件变量名称生成规则是：以下划线为单词定界符，将首个单词以外的单词首字母大写，最后移除下划线等特殊符号。例如文本框控件ID为："textview_Title"，则对应的变量名为："textviewTitle"。

# 基本应用
我们首先编写一个登录界面布局文件，为了节约篇幅，此处只列出关键属性。

ui_demo_base.xml:

```xml
<LinearLayout>
    <TextView android:id="@+id/tvTitle" />

    <EditText android:id="@+id/et_username"  />
    <EditText android:id="@+id/et_password" />

    <Button android:id="@+id/btnLogin" />
</LinearLayout>
```

若要使用ViewBinding，首先需要在当前项目的Gradle配置中将其启用：

```groovy
android {
    // 省略其他内容...

    viewBinding {
        enabled = true
    }
}
```
