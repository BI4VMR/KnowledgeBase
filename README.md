# 简介
文档仓库，使用Markdown格式进行归档，建议下载到本地进行全文检索，以便查阅感兴趣的内容。


# 文档模板
## 简介
简要描述该主题以及相关资源的链接。

## 理论基础
介绍该主题相关的术语、概念。

## 基本应用
介绍该主题的基本使用方法。

## 版本变更
记录该主题在不同版本之间的变化。

<!--

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

### 详情
此处填写【详情】。

### 兼容方案
此处填写【兼容方案】。

-->

## 疑难解答
记录该主题在实际运用过程中遇到的问题，提供解决问题的思路与经验。

<!--

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

## 附录
数量较多的额外信息列表。


# 标识信息格式
## 链接

[🧭 【链接名称】](【链接地址】)

## 提示

> 🚩 提示
>
> 提示信息，用于对正文进行额外的补充、提示操作技巧或者标明重点。

## 警告

> ⚠️ 警告
>
> 警告信息，用于说明关键的操作步骤，如果不遵守这些规则，可能导致性能降低或业务中断，需要引起注意。

## 禁止

> ⛔ 禁止
>
> 禁止信息，用于声明安全相关的信息，如果不遵守这些规则，可能导致人身危险、设备损坏或业务故障，需要引起高度注意。


# 命令及代码格式
## 命令格式
示例格式：

```text
# 注释内容
[root@Fedora ~]# telnet [-d] {<IP地址> | <域名>} <端口>

# 此处省略部分输出内容...
```

🔷 `提示符([root@Fedora ~]#)`

命令提示符。

表示设备信息、用户名称、当前所在的功能菜单等信息。

🔷 `尖括号(<>)`

必选内容占位符。

表示该项为必填项，其中的内容需要根据实际情况进行填写。

🔷 `方括号([])`

可选内容占位符。

表示该项为可选项，其中的内容需要根据实际情况进行填写，或者忽略此内容。

🔷 `竖线(|)`

内容分隔符。

分隔多个互斥的内容。

🔷 `组合选项({<选项I> | <选项II> | ...})`

必须选择其中的一个选项。

🔷 `组合选项([<选项I> | <选项II> | ...])`

可以选择其中的一个选项，也可以一个都不选择，忽略此组合。

## 示例程序

<!--

"<此处填写【文件名称】>":

```text
TODO 此处填写【代码内容】
```

此时运行示例程序，并查看控制台输出信息：

```text
TODO 此处填写【运行结果】
```

根据上述输出内容可知：

TODO 此处填写【结果分析】

-->

## 函数或方法说明

🔶 `<返回值> <方法名称>(参数列表 ...)`

功能简述：

<!-- TODO 此处填写【功能简述】 -->

参数列表：

- `<参数1>` : <参数说明>
- `<参数2>` : <参数说明>

返回值：

<!-- TODO 此处填写【返回值说明】 -->

异常情况：

- `<异常名称>` : <异常说明>


# 项目符号
## 项目符号：示例
🔴
🟠
🟡
🟢
🔵
🟣
🟤

## 项目符号：列表
🔷
🔶
✴️
🔺
🔻

## 项目符号：信息
🧭 内部链接
🔗 外部链接
🚩 提示
⚠️ 警告
⛔ 禁止
☢️ 辐射
☣️ 生化
√ 正确/可用
× 错误/不可用


<!-- Hide

# 提交命令
常用：

```text
msg=$(uuidgen | awk '{print toupper($0)}'); git add .; git commit -m "$msg"; git push;
```

完整：

```text
msg=$(uuidgen | awk '{print toupper($0)}'); git add .; git commit -m "$msg";git push github; git push private;
```

-->
