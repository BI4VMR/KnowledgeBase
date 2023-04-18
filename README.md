# 简介
文档仓库，使用Markdown格式进行归档，建议下载到本地进行全文检索，以便查阅感兴趣的内容。

# 任务清单
## Linux
- [ ] 存储管理 - 伸缩分区
- [ ] 存储管理 - LVM

## Java
- [ ] 高级特性 - 封装
- [ ] 高级特性 - 继承
- [ ] 高级特性 - 多态
- [ ] 高级特性 - 接口

## Android
- [ ] 用户界面 - 控件与布局
- [ ] 用户界面 - 图像素材
- [ ] 用户界面 - Activity
- [ ] 用户界面 - Fragment
- [ ] 用户界面 - 基础控件
- [ ] 用户界面 - 扩展控件
- [ ] 用户界面 - Material控件 - MaterialButton
- [ ] 用户界面 - Material控件 - TextInputEditText
- [ ] 用户界面 - Material控件 - ShapeableImageView
- [ ] 用户界面 - Material控件 - Slider
- [ ] 用户界面 - Material控件 - TabLayout
- [ ] 用户界面 - Material控件 - Chip
- [ ] 系统组件 - Intent
- [ ] 系统组件 - Context
- [ ] 系统组件 - Application
- [ ] 系统组件 - Content Provider
- [ ] 数据存储 - SQLite
- [ ] 数据存储 - Room
- [ ] 多媒体 - MediaStore

## 网络技术
- [ ] 链路层
- [ ] 网络层
- [ ] 传输层
- [ ] 应用层
- [ ] 高可用性
- [ ] 网络安全
- [ ] 网络管理
- [ ] 流量工程

# 提交命令

```text
msg=$(uuidgen | awk '{print toupper($0)}'); git add .; git commit -m "$msg"; git push gitee; git push github;
```

# 文档模板
通用章节：

## 疑难解答
本节用于记录技术在实际运用过程中遇到的问题，提供解决问题的思路与经验。

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
