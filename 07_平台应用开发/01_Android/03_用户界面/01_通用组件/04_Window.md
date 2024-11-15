# 简介



# 版本变更
## 索引

<div align="center">

|       序号        |    版本    |                           摘要                           |
| :---------------: | :--------: | :------------------------------------------------------: |
| [变更一](#变更一) | Android 12 | 新增特性：禁止触摸事件穿透至不受信任窗口底部的其他窗口。 |

</div>

## 变更一
### 摘要
自从Android 12开始，系统新增特性：禁止触摸事件穿透至不受信任窗口底部的其他窗口。

### 详情
当窗口A覆盖在窗口B之上、且窗口A设置了 `FLAG_NOT_TOUCHABLE` 标志位时，系统可能会拦截从窗口A穿透至窗口B的触摸事件。此处的窗口A可能是一个不透明窗口，用户看不见底部其他窗口的内容，如果窗口A属于恶意应用程序，它可以利用触摸事件穿透机制诱导用户点击底部的其他窗口，详情可参考 [🧭 Android 12 - 不受信任的触摸事件被屏蔽](https://developer.android.com/about/versions/12/behavior-changes-all#untrusted-touch-events) 网页中的相关内容。

如果我们为窗口设置 `FLAG_NOT_TOUCHABLE` 标志位但实际不生效，可以通过查看系统日志确认是否被该机制拦截。当我们触摸窗口时，若该机制触发，则会产生 `Untrusted touch due to occlusion by <包名>` 日志消息：

```text
18:02:29.009   617   763 W InputDispatcher: Untrusted touch due to occlusion by net.bi4vmr.study.ui.common.window/10137
18:02:29.009   617   763 D InputDispatcher: Stack of obscuring windows during untrusted touch (349, 1129):
18:02:29.009   617   763 D InputDispatcher:     * package=net.bi4vmr.study.ui.common.window/10137, id=1667, mode=BLOCK_UNTRUSTED, alpha=1.00, frame=[0,60][1080,2040], touchableRegion=[0,60][1080,2040], window={c7676f net.bi4vmr.study.ui.common.window}, inputConfig={NOT_FOCUSABLE | NOT_TOUCHABLE | PREVENT_SPLITTING}, hasToken=true, applicationInfo.name=, applicationInfo.token=<null>
18:02:29.009   617   763 D InputDispatcher:     * [TOUCHED] package=com.android.launcher3/10107, id=1668, mode=BLOCK_UNTRUSTED, alpha=1.00, frame=[0,0][1080,2160], touchableRegion=[0,0][1080,2160], window={cdf8926 com.android.launcher3/com.android.launcher3.uioverrides.QuickstepLauncher}, inputConfig={DUPLICATE_TOUCH_TO_WALLPAPER}, hasToken=true, applicationInfo.name=ActivityRecord{8d4ab99 u0 com.android.launcher3/.uioverrides.QuickstepLauncher} t8, applicationInfo.token=0x7dcca98ddd70
18:02:29.009   617   763 W InputDispatcher: Dropping untrusted touch event due to net.bi4vmr.study.ui.common.window/10137
18:02:29.009   617   763 I InputManager: Suppressing untrusted touch toast for net.bi4vmr.study.ui.common.window
```

下文列表详细地描述了该机制的触发条件：

- 除了后续表项所描述的特例之外，系统将会拦截所有穿透窗口的触摸事件。
- 当顶层窗口与底部窗口属于同一应用程序时，允许穿透。
- 当顶层窗口是受信任的窗口（目前包括输入法 `TYPE_INPUT_METHOD` 、无障碍 `TYPE_ACCESSIBILITY_OVERLAY` 、语音助手 `TYPE_VOICE_INTERACTION` 窗口）时，允许穿透。
- 当顶层窗口不可见（不透明度为"0"或Root View的可见性不是"VISIBLE"）时，允许穿透。
- 当顶层窗口的类型为 `TYPE_SYSTEM_ALERT` ，且不透明度处于： `[0, 0.8]` 之间时，允许穿透。


### 兼容方案
如果是应用开发者，可以通过反射获取并修改窗口的PrivateFlags属性，添加 `TRUSTED_OVERLAY` 标志位，使窗口成为受信任的窗口。

如果是测试人员，可以通过ADB命令禁用该机制。

```text
# A specific app
adb shell am compat disable BLOCK_UNTRUSTED_TOUCHES com.example.app

# All apps
# If you'd still like to see a Logcat message warning when a touch would be
# blocked, use 1 instead of 0.
adb shell settings put global block_untrusted_touches 0

如需将行为还原为默认设置（不受信任的触摸操作被屏蔽），请运行以下命令：

# A specific app
adb shell am compat reset BLOCK_UNTRUSTED_TOUCHES com.example.app

# All apps
adb shell settings put global block_untrusted_touches 2
```
