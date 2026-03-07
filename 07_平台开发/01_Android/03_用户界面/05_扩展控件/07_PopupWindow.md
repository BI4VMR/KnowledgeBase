<!-- TODO
# PopupWindow
AlertDialog：以“对话框”语义居中/固定区域显示，通常不依赖某个控件作为锚点。
PopupWindow：以某个 View 为锚点显示（如 showAsDropDown / showAtLocation），适合做下拉菜单、气泡提示。




PopupWindow ()	// 创建一个空的PopupWindow

PopupWindow (View contentView)	

PopupWindow (int width, 
                int height)

PopupWindow (View contentView, 	// PopupWindow的内容View, 相当于setContentView
                int width, 	// 宽, 相当于setwidth()
                int height,  // 高, 相当于setHeight
                boolean focusable) // 是否可获取焦点, 相当于setFocusable()


PopupWindow需要设置宽高和View
创建时需要指定宽高，View 根布局的layoutwidth/layoutheight属性无效。


相对于锚点View的位置

window.setOverlapAnchor(true)

是否允许覆盖锚点View，默认 false。
未覆盖状态默认与锚点View左侧下方对齐，覆盖状态则从锚点View左侧顶部开始显示。


window.showAsDropDown(v)

以View v 作为锚点显示PopupWindow，默认左下角对齐。

window.showAsDropDown(v,100,100);

左下角对齐并额外偏移x和y像素


window.showAsDropDown(v,0,0,Gravity.END)

右下角对齐

只能填写END/START/其他数值是无效的。


相对于当前窗口

window.showAtLocation(v, Gravity.END, 0, 0)

在v所在窗口显示PopupWindow，位置由Gravity参数决定。

offset与Gravity相关

Gravity.Start
offsetX 为正向右偏移

Gravity.END
offsetX 为正向左偏移

NO_GRAVITY等同于Gravity.TOP | Gravity.START

offset最多与屏幕对齐，更大的值是无效的，弹窗不会超出屏幕边界。






void setOutsideTouchable (boolean touchable) // 设置外部是否可被点击
boolean isOutsideTouchable () 


可获取焦点
一般控件都不需要焦点. 但是输入框EditText需要先获取焦点才能输入. 最重要的是当PopupWindow可获取焦点时按下手机返回键将不会销毁当前Activity而是关闭当前PopupWindow. 所以我们一般还是设置为true. 更加符合用户操作逻辑. 该方法为true时同时拥有setOutsideTouchable(true)的作用.


弹出后更新位置
window.update(v, 200, ViewGroup.LayoutParams.WRAP_CONTENT);




android.view.WindowLeaked: Activity net.bi4vmr.bookkeeper.ui.debt.DebtManageActivity has leaked window android.widget.PopupWindow$PopupDecorView{7d44ac2 V.E...... .......D 0,0-233,399} that was originally added here


activity关闭需要dismiss


-->
