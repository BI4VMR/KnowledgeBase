<!-- TODO


aaos导航栏颜色设为透明，仍有灰色遮罩 navigationBarBackground

<item name="android:enforceNavigationBarContrast">false</item>

配置后可变透明

api 29新增




## 版本变更
记录该主题在不同版本之间的变化。

<!-- Hide

# 版本变更
## 索引

<div align="center">

|       序号        |       版本       |       摘要       |
| :---------------: | :--------------: | :--------------: |
| [变更一](#变更一) | 此处填写【版本】 | 此处填写【摘要】 |

</div>

## 变更一
### 摘要
此处填写【摘要】。



# Android 15 状态栏

在强制Edge-to-Edge（targetSdk 35+）下，无法再通过 statusBarColor 为状态栏着色。

旧行为（targetSdk ≤ 34，Android 14 及更低）
- 默认 不 是 edge-to-edge。窗口内容被限制在系统栏（状态栏/导航栏）下方的安全区域内。
- 状态栏默认使用主题里设置的 android:statusBarColor（你的 Theme.Default 设为 ?attr/colorPrimaryVariant，即紫色），并由系统填色。所以状态栏看起来有颜色、不平透明。
- 如果你不主动调用 enableEdgeToEdge() 或 WindowCompat.setDecorFitsSystemWindows(false)，内容不会画到系统栏下面。
新行为（targetSdk ≥ 35）
- 系统强制 edge-to-edge：
- statusBarColor → 强制透明（透明化，透明背景）
- android:windowTranslucentStatus → 强制为 true，如果为 adaptive（默认），则强制忽略
- 内容延伸绘制到状态栏和导航栏之下，所谓的 insets（内边距）需要应用自己处理。
- 你设置的 android:statusBarColor = ?attr/colorPrimaryVariant 被忽略了，因为系统已经把它强制为透明。
所以： 同一主题下，targetSdk 34 时状态栏是紫色；targetSdk 35+ 时它是透明的紫，也就是你看到"状态栏透明"——真正原因是你主界面的根布局没有画出背景色，透明的是状态栏，露出的背景是窗口的默认背景（往往是深色）。




### 详情
此处填写【详情】。

### 兼容方案
此处填写【兼容方案】。

-->

## 疑难解答
记录该主题在实际运用过程中遇到的问题，提供解决问题的思路与经验。

<!-- Hide

# 疑难解答
## 索引

<div align="center">

|       序号        |         摘要         |
| :---------------: | :------------------: |
| [案例一](#案例一) | 此处填写【问题描述】 |

</div>

## 案例一
### 问题描述
此处填写【问题描述】。

### 问题分析
此处填写【问题分析】。

### 解决方案
此处填写【解决方案】。


-->
