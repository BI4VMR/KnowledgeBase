# 简介
DialogFragment是一种以Fragment形式管理的弹窗，Google推荐使用DialogFragment以取代普通的Dialog。

Activity可以保存其中的Fragment状态，当视图恢复时，重新显示Fragment，所以DialogFragment可以在屏幕方向改变后恢复显示，而Dialog则会消失，需要开发者自行处理恢复逻辑。

# 基本应用
我们需要继承DialogFragment类，并在 `onCreateDialog()` 或 `onCreateView()` 回调方法中绑定控件，实现UI逻辑。

## 使用系统内置样式
如果系统内置的AlertDialog样式能够满足要求，我们可以重写 `onCreateDialog()` 回调方法；该方法要求返回一个Dialog实例，我们可以构建AlertDialog并配置各项属性之后，将其作为返回值返回给系统。

```java
public class MyDialog extends DialogFragment {

    /* 获取Fragment实例 */
    public static MyDialog newInstance() {
        return new MyDialog();
    }

    /* 使用AlertDialog构建对话框 */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 使用AlertDialog构建界面
        return new AlertDialog.Builder(requireContext())
                .setTitle("提示")
                .setMessage("这是一个提示对话框")
                .create();
    }
}
```

此时一个简单的DialogFragment就创建好了，接着我们在测试Activity中将其显示出来：

```java
// 获取MyDialog实例
MyDialog dialog = MyDialog.newInstance();
// 显示MyDialog
dialog.show(getSupportFragmentManager(), null);
```

DialogFragment的 `show(FragmentManager fm, String tag)` 方法用于显示对话框，参数"fm"是FragmentManager对象，参数"tag"是Fragment的标识符，不需要使用时可以传入空值。

DialogFragment也是Fragment，所以同一实例同时只能被添加一次，否则会导致异常。对于全局复用的DialogFragment，为了防止并发提交导致异常，我们应该使用同步方法 `showNow()` 。

此时我们运行测试Activity，可以观察到DialogFragment的样式，其外观即为先前定义的AlertDialog的样式。

<div align="center">

<!-- TODO ![](./Assets-DialogFragment/) -->

</div>

## 使用自定义样式
如果我们希望使用自定义的布局，可以重写 `onCreateView()` 方法，这种用法与普通的Fragment没什么不同。

```java
public class MyDialog extends DialogFragment {

    /* 获取Fragment实例 */
    public static MyDialog newInstance() {
        return new MyDialog();
    }

    /* 使用自定义View构建对话框 */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mydialog, container, false);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("标题");
        return view;
    }
}
```

"mydialog.xml"描述了弹窗的样式，我们简单的放置两个文本框作为标题和内容，此处省略具体代码。

此时运行测试Activity，显示该DialogFragment：

<div align="center">

<!-- TODO ![](./Assets-DialogFragment/) -->

</div>

这种Dialog默认显示在屏幕中间，并且布局文件中根容器的宽高属性会失效，实际宽高由内容决定。如果我们需要定制大小与位置等，方法参见后文章节。

# 外观定制
## 基本样式
DialogFragment的宽高、位置等属性，可以在 `onStart()` 生命周期中进行设置，此处我们将MyDialog的宽度设为与屏幕等宽，位置设为对齐到屏幕顶端。

```java
@Override
public void onStart() {
    super.onStart();
    // 获取窗体属性对象
    WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
    // 设置宽高属性
    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
    // 设置位置属性
    params.gravity = Gravity.TOP;
    // 设置窗口周围阴影不透明度
    params.dimAmount = 0.3F;
    // 应用修改
    getDialog().getWindow().setAttributes(params);
}
```

此时运行测试Activity，显示该DialogFragment并查看其外观：

<div align="center">

<!-- TODO ![](./Assets-DialogFragment/) -->

</div>

## 窗体外观
如果我们需要使用样式更改窗口的外观，可以在 `onCreate()` 生命周期中使用 `setStyle()` 方法进行设置。

首先创建一个Style配置，继承自系统提供的对话框样式：

```xml
<!-- 自定义弹窗样式 -->
<style name="DialogFullScreen" parent="Theme.AppCompat.Dialog">
    <!-- 窗口背景（必选，否则以下两个配置项是无效的。） -->
    <item name="android:windowBackground">@android:color/white</item>
    <!-- 全屏属性 -->
    <item name="android:windowFullscreen">true</item>
    <!-- 内边距 -->
    <item name="android:padding">0dp</item>
</style>
```

DialogFragment默认拥有一个非0的内边距数值，所以前文我们将窗口宽度设为与屏幕等宽，仍然不能铺满屏幕；此处我们使用Style配置使窗口宽度铺满屏幕：可以将Padding值设为0，或者指定全屏属性"windowFullscreen"，这两种方式任选一个即可。

然后我们在DialogFragment中应用该样式，在创建Fragment时设置前文的Style。

```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NORMAL, R.style.DialogFullScreen);
}
```

此时运行测试Activity，显示该DialogFragment并查看其外观：

<div align="center">

<!-- TODO ![](./Assets-DialogFragment/) -->

</div>

## 设置背景
我们经常在布局文件中设置整个弹窗的背景，但有时显示的圆角角度与素材不符，这是因为Dialog本身的背景遮盖View背景，我们可以将DecorView背景设为透明解决此问题。

```java
@Override
public void onStart() {
    super.onStart();
    getDialog().getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));
}
```

# 通知窗口关闭事件
DialogFragment没有对外暴露关闭事件监听器，但它被关闭时，内部将会触发 `onDismiss()` 回调方法，我们可以自行暴露一个接口给外部使用。

```java
public class MyDialog extends DialogFragment {

    /* 此处省略部分变量与方法... */

    private DismissListener dismissListener = null;

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onClose();
        }
    }

    /* 对外接口：弹窗消失事件监听器 */
    interface DismissListener {
        void onClose();
    }

    // 设置弹窗关闭监听器
    public void setDismissListener(DismissListener l) {
        dismissListener = l;
    }
}
```

我们在使用DialogFragment的组件中注册此回调，即可监听到关闭事件。

```java
// 创建MyDialog实例
MyDialog dialog = MyDialog.newInstance();
// 注册弹窗关闭监听器
dialog.setDismissListener(() -> Log.i("myapp", "Dialog is closed."));
```
