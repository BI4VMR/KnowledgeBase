# GNU/Linux的诞生
Unix是早期被广泛应用的操作系统之一，1969年由Ken Thompson在贝尔实验室编写完成，1973年Ken Thompson与Dennis Ritchie用C语言重写了Unix的第三版内核。1979年AT&T成立了专门的Unix实验室(USL)，同时宣布Unix商业化。

Tanenbaum教授觉得没有良好的操作系统做为教学实例，于1984年起主导开发操作系统Minix，它参考Unix系统实现，基于微内核设计，起初只随教材附赠。

芬兰人Linus Torvalds在赫尔辛基大学就读期间，出于个人爱好编写了Linux内核，其参考了Minix系统，可以在386架构的PC上运行，1991年10月5日Linux以GPL许可证正式发布。

由于不满Unix的商业化，Richard Stallman在1983年9月27日发起了GNU计划，目标是创建一套完全自由的操作系统。到了1991年，大部分应用程序已经可以使用，但其内核HURD迟迟未能开发完成，恰好Linux内核与GNU软件相结合，正式诞生了完全自由的操作系统"GNU/Linux"。

# Linux内核版本
Linux内核版本号由3部分组成： `x.y.z` ，第一位表示主版本号，只有大版本更新时才会变化；第二位表示次版本号，奇数表示测试版，偶数表示稳定版；第三位表示修订号，有较小的变更时进行增长。

Linux内核还有实时补丁，安装后可以使Linux以实时操作系统方式运行。

# 发行版
## 简介
组织或个人根据自己的设计理念，修改内核并整合软件包后发布的系统，称为Linux发行版(Linux Distribution)，通常包含桌面环境、办公套件、媒体播放器、数据库等应用软件，各发行版有自己的特点，我们应该根据实际需要选择合适的版本。

目前主要的Linux系统发行版有Redhat系列、Debian系列和Arch系列。

## RedHat系列
下文内容介绍了RedHat系列发行版的基本信息：

🔷 RHEL

RedHat Enterprise Linux简称RHEL，是红帽公司为企业应用开发的Linux系统，其特点是稳定与收费，非常适合服务器使用，大型企业的重要服务器较多使用RHEL。其软件包格式为"rpm"，软件包管理器为"yum"与"dnf"。

🔷 CentOS

Community Enterprise Operating System是一款由社区维护的操作系统，其使用RHEL公布的源代码编译。CentOS是完全开源免费的系统，功能与RHEL十分相似，部分有自主维护能力的企业也会选择使用CentOS。

🔷 Fedora

Fedora是由社区开发、红帽公司赞助的操作系统，其目标是创建一套新颖、多功能并且自由的开源操作系统。它是红帽公司的新技术测试平台，被认为可用的技术最终会被合并进入RHEL。

## Debian系列
下文内容介绍了Debian系列发行版的基本信息：

🔶 Debian

Debian是社区Linux中的典范，提供了丰富的开源软件支持，拥有很高的认可度和使用率。其对于各类架构支持良好，稳定性、安全性强。Debian的软件包管理工具主要有"dpkg"和"apt"。

🔶 Ubuntu

Ubuntu是一个以桌面应用为主的Linux操作系统，基于Debian，由Canonical公司运营，其特点是界面友好，容易上手，对硬件的支持非常全面，非常适合做为桌面系统。

🔶 Deepin

Deepin是由武汉深之度科技有限公司开发的Linux发行版，是Debian的衍生版。Deepin专注于使用者对日常办公、学习、生活和娱乐的体验，包含常用的应用程序，上手难度低，适合日常使用。

Deepin拥有自研的DDE桌面，美观程度极佳，但稳定性不如其它成熟的桌面环境。

## Arch Linux系列
下文内容介绍了Arch Linux系列发行版的基本信息：

🔷 Arch Linux

Arch Linux是起源于加拿大的操作系统，致力于使用简单、系统轻量、软件更新速度快。创始人Judd Vinet出于对Debian与Red Hat包管理体系的不满而创立。

Arch Linux系统设计以KISS（保持简单和愚蠢）为总体指导原则，注重代码优雅并遵循极简主义。Arch Linux系统的软件包管理器是"pacman"。

🔷 Manjaro

Manjaro是一款基于Arch的Linux发行版，相比Arch整合了许多图形化工具、驱动管理器等，易用性更好，适合作为桌面系统使用。Manjaro官方提供集成了KDE、Gnome、XFCE等各种桌面环境的安装包，可以根据个人喜好进行选择。
