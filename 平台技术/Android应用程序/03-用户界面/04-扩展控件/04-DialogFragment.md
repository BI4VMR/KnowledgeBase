# 简介
DialogFragment是一种以Fragment形式管理的弹窗，Google推荐使用DialogFragment以取代Dialog。

Activity可以保存其中的Fragment状态，当视图恢复时，重新显示Fragment，所以DialogFragment可以在屏幕方向改变后恢复显示，而Dialog则会消失。

# 基本应用
我们需要继承DialogFragment类，并在 `onCreateDialog()` 或 `onCreateView()` 回调方法中绑定控件，实现UI逻辑。

