# RPM
## 简介
RPM是"RedHat Package Manager"的缩写，起初作为RedHat系统的基础包管理器，由于其开放的设计理念，后来也被一些其它发行版采用。

RPM软件包的名称遵循一定规则，以"vim-minimal-7.4.629-7.el7.x86_64.rpm"为例，其中"vim-minimal"表示软件包名称；"7.4.629-7"表示版本号与发布号；"el7"表示软件包适用的RHEL版本；"x86_64"表示软件包适用的硬件架构。

RPM基于 `/var/lib/rpm` 目录中的数据库维护软件信息，操作已安装的软件时，可以只填写名称，不必指定版本号等信息。

## 安装软件
RPM命令的语法格式为 `rpm <主要动作> <附加选项>` ，附加选项可以有多个，但主要动作（安装、卸载等）仅能指定一个，不可冲突。例如：安装动作记作 `-i` ，通常我们还会同时携带“显示详细信息( `-v` )”和“显示进度( `-h` )”选项，故安装软件包的命令为： `rpm -ivh <RPM文件>` 。

🔴 示例一

安装软件包"mlocate"，并显示详细信息。

```text
[root@Fedora Packages]# rpm -ivh mlocate-0.26-8.el7.x86_64.rpm
Preparing...                  ################################# [100%]
Updating / installing...
   1:mlocate-0.26-8.el7       ################################# [100%]
```

🟠 示例二

安装MySQL的所有组件，跳过依赖检查。

```text
[root@Fedora MySQL]# ll -h
total 489M
-rw-r--r-- 1 7155 31415  46M Jul  2 02:48 mysql-community-client.rpm
-rw-r--r-- 1 7155 31415 4.5M Jul  2 02:48 mysql-community-client-plugins.rpm
-rw-r--r-- 1 7155 31415 620K Jul  2 02:48 mysql-community-common.rpm
-rw-r--r-- 1 7155 31415 4.1M Jul  2 02:49 mysql-community-libs.rpm
-rw-r--r-- 1 7155 31415 434M Jul  2 02:50 mysql-community-server.rpm

[root@Fedora MySQL]# rpm -ivh --nodeps mysql*
Preparing...                  ################################# [100%]
Updating / installing...
   1:mysql-community-common   ################################# [ 20%]
   2:mysql-community-client-p ################################# [ 40%]
   3:mysql-community-libs     ################################# [ 60%]
   4:mysql-community-client   ################################# [ 80%]
   5:mysql-community-server   ################################# [100%]
```

对于存在依赖关系的多个软件包，可以添加 `--nodeps` 选项跳过依赖检查强制安装。

> ⚠️ 警告
>
> 若操作者不知道软件包所依赖的组件是否齐全，则不能强制进行安装，即使软件安装完毕，也会因为缺少组件而无法正常工作。

## 卸载软件
卸载动作记作 `-e` ，语法为： `rpm -e <软件名称>` 。卸载软件时，若该软件包被其它程序依赖，则显示消息并终止操作，此时可以添加 `--nodeps` 强制执行，但这可能导致其它程序因缺少依赖组件而出错，需要谨慎操作。

🟡 示例三

强制卸载软件包"mariadb-libs"。

```text
[root@Fedora ~]# rpm -evh --nodeps mariadb-libs
Preparing...                      ################################# [100%]
Cleaning up / removing...
   1:mariadb-libs-1:5.5.68-1.el7  ################################# [100%]
```

卸载后依赖此软件包的程序将无法运行，必须安装其它版本或替代品。

## 升级与降级
升级动作记作 `-U` ，语法为： `rpm -Uvh <RPM文件>` 。当指定的RPM包版本高于系统中安装的软件时，将执行升级操作；当指定的RPM包尚未被安装时，则将执行安装操作。

若需要降级某个软件包，我们可以在安装命令后添加 `--force` 选项强制安装旧版本的RPM包，写作 `rpm -ivh --force <旧版RPM文件>` 。

## 查询软件信息
查询动作记为 `-q` ，我们通常添加 `-a` 选项列出系统中所有已安装软件包，再配合管道符和 `grep` 命令筛选出需要的信息。

🟢 示例四

查找系统中所有以"mysql"开头的软件包。

```text
[root@Fedora ~]# rpm -qa | grep "^mysql"
mysql-community-common-8.0.26-1.el7.x86_64
mysql-community-libs-8.0.26-1.el7.x86_64
mysql-community-server-8.0.26-1.el7.x86_64
mysql-community-client-plugins-8.0.26-1.el7.x86_64
mysql-community-client-8.0.26-1.el7.x86_64
```

我们还可以使用 `-i <软件包名称>` 选项以查看特定软件包的详细信息，包括版本号、适用架构、安装日期、官网等。

🔵 示例五

查询软件包"nano"的相关信息。

```text
[root@Fedora ~]# rpm -qi nano
Name        : nano
Version     : 2.3.1
Release     : 10.el7
Architecture: x86_64
Install Date: Fri 30 Jul 2021 10:49:05 AM CST
Group       : Applications/Editors
Size        : 1715901
License     : GPLv3+
Signature   : RSA/SHA256, Fri 04 Jul 2014 11:53:43 AM CST, Key ID 24c6a8a7f4a80eb5
Source RPM  : nano-2.3.1-10.el7.src.rpm
Build Date  : Tue 10 Jun 2014 12:47:54 PM CST
Build Host  : worker1.bsys.Fedora.org
Relocations : (not relocatable)
Packager    : Fedora BuildSystem <http://bugs.Fedora.org>
Vendor      : Fedora
URL         : http://www.nano-editor.org
Summary     : A small text editor
Description :
GNU nano is a small and friendly text editor.
```

若需要查看本地RPM包的信息，可以添加 `-p <文件>` 选项，此时完整的命令写作： `rpm -qi -p <RPM文件路径>` 。

`-R` 选项可以查询软件包的依赖信息，包括软件包与库文件。

🟣 示例六

查询软件包"tree"的依赖信息。

```text
[root@Fedora ~]# rpm -qR tree 
libc.so.6()(64bit)
libc.so.6(GLIBC_2.14)(64bit)
libc.so.6(GLIBC_2.2.5)(64bit)
libc.so.6(GLIBC_2.3)(64bit)
libc.so.6(GLIBC_2.3.4)(64bit)
libc.so.6(GLIBC_2.4)(64bit)
rpmlib(CompressedFileNames) <= 3.0.4-1
rpmlib(FileDigests) <= 4.6.0-1
rpmlib(PayloadFilesHavePrefix) <= 4.0-1
rtld(GNU_HASH)
rpmlib(PayloadIsXz) <= 5.2-1
```

# YUM
## 简介
YUM(Yellow dog Updater, Modified)是RedHat系列系统的在线软件包管理器，能够从指定的服务器自动下载RPM包并安装，以及自动处理依赖关系。

## 基本应用
### 软件仓库管理
我们可以
<!-- TODO -->
- 查看软件仓库信息 : `yum repolist`
- 
执行 `yum repolist` 命令可以查看软件仓库信息， `yum makecache` 可以在本地生成软件仓库的元数据缓存， `yum clean all` 可以清除所有元数据缓存与软件包下载缓存。

- 执行 `yum list` 命令可列出软件仓库中所有可用的软件包
- `yum search [关键字]` 可以搜索名称或简介中包含该关键字的软件包。


> 🚩 提示
>
> YUM并不能查询已安装的软件包信息，请使用RPM的相关命令进行查询。

### 安装软件
安装命令记作 `yum install <软件包名称 | RPM包路径>` ，使用软件包名称作为输入参数时，将从软件仓库自动下载软件包并安装；使用本地RPM包的路径作为输入参数时，YUM能够解析并处理相关依赖。

若添加 `-y` 选项，将会在交互式问答中自动进行确认，此功能在批量安装多个软件包时可简化操作。

### 更新软件
执行 `yum check-update` 可以检查可更新的软件包，执行 `yum update {软件包名称}` 可以更新指定的软件包，不指定软件包名称则更新所有软件包。

### 降级软件
降级软件包的语法为： `yum downgrade {依赖包I} {依赖包II} ... <软件包名称>` ，若该软件存在依赖，则不会自动完成降级，我们需要将软件的依赖包写在前面，将它们一并进行降级操作。

### 卸载软件
执行 `yum remove <软件包名称>` 命令可以卸载软件包，YUM在执行卸载时不会主动移除相关依赖，因为这些组件可能仍被其它软件包所需要。

### 查找提供命令的软件包
有时系统中缺少某些命令，但我们不知道这些命令对应的软件包名称，此时可以执行 `yum provides <命令>` 进行搜索，系统将会列出相关软件包。

### 软件组管理
YUM内置了一些软件包的集合，可以同时管理一组软件包，此功能对于批量部署系统的场景非常有效，下文中的代码块列出了软件组管理的相关命令。

```text
# 查看可用的软件组
[root@Fedora ~]# yum groups list

# 查看软件组包含的软件包
[root@Fedora ~]# yum groups info "<软件组名称>"

# 安装软件组
[root@Fedora ~]# yum groups install "<软件组名称>"

# 移除软件组
[root@Fedora ~]# yum groups remove "<软件组名称>"
```

## 软件源配置
YUM的软件源配置文件位于 `/etc/yum.repos.d/` ，文件后缀为".repo"，若将后缀更改为其它字符，则不会被YUM读取。

每个配置文件可以包含若干软件源，每个软件源以方括号包围的名称作为首行，随后包含若干参数项：

```text
# ----- YUM源配置文件示例 -----
# 软件源名称
[base]
# 相关信息
name=Fedora-$releasever - Base
# 镜像列表
mirrorlist=http://mirrorlist.Fedora.org/?release=$releasever&arch=$basearch&repo=Fedoraplus&infra=$infra
# 源路径
baseurl=http://mirror.Fedora.org/Fedora/$releasever/Fedoraplus/$basearch/
# 是否启用
enabled=1
# 是否校验公钥
gpgcheck=1
# 公钥文件路径
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-Fedora-7
```

镜像列表指定了一系列镜像站地址，系统将会从中选择一个使用；源路径则指向特定的服务器或文件，我们在二者中选择一项即可。

软件源默认为启用状态，仅在明确配置 `enabled=0` 时被禁用。

校验参数用于控制安装前是否检查软件包的完整性，可以提升安全性。

## 本地软件源
当系统不能访问互联网时，我们可以使用本地文件或局域网内的服务器作为软件源，若本地软件仓库内容较为丰富，YUM的自动依赖处理仍能够发挥作用，免去手动安装软件的麻烦。

最常用的本地源是系统安装镜像文件，其中包含软件仓库的元数据，可以直接使用。

🔴 示例一

使用Fedora系统镜像搭建本地YUM源。

```text
# 挂载镜像文件
[root@Fedora ~]# mkdir /mnt/Fedora
[root@Fedora ~]# mount -o loop Fedora-7-x86_64-DVD-2009.iso /mnt/Fedora/

# 将原有仓库配置文件移动到"backup"文件夹
[root@Fedora ~]# cd /etc/yum.repos.d
[root@Fedora yum.repos.d]# mkdir backup
[root@Fedora yum.repos.d]# mv *.repo backup

# 创建新的仓库配置文件
[root@Fedora yum.repos.d]# vim Local.repo

# 在配置文件中新增以下内容并保存
[LocalCD]
name=Local CD
baseurl=file:///mnt/Fedora
gpgcheck=0

# 清除并重建YUM缓存
[root@Fedora ~]# yum clean all
[root@Fedora ~]# yum makecache
Loaded plugins: fastestmirror
Determining fastest mirrors
LocalCD                                               | 3.6 kB  00:00:00
(1/4): LocalCD/group_gz                               | 153 kB  00:00:00
(2/4): LocalCD/primary_db                             | 3.3 MB  00:00:00
(3/4): LocalCD/filelists_db                           | 3.3 MB  00:00:00
(4/4): LocalCD/other_db                               | 1.3 MB  00:00:00
Metadata Cache Created
```

## 禁止更新特定组件
有时我们不希望特定的组件被频繁更新，例如系统内核，此时我们可以通过配置文件定义忽略规则，使YUM不对这些组件进行操作。

```text
# 编辑文件并添加内容
[root@Fedora ~]# vim /etc/dnf/dnf.conf

# 在"main"小节中添加以下配置项
[main]
# 指定要排除的组件
exclude=kernel-*
```

在上述配置文件示例中，我们通过"exclude"语句排除了所有开头为"kernel-"的组件，这条规则能够匹配到与内核相关的组件。

排除语句可以指明单个组件的名称，也可以使用通配符。这些组件不会被YUM进行检查更新、更新、移除等操作。

## 下载安装包
为安装命令添加 `--downloadonly` 选项可以仅下载软件包，而不进行安装。这在离线部署时非常有用，我们可以通过已联网的设备下载软件包与相关依赖，并拷贝至离线设备进行安装。

```text
# 下载本机未安装的软件包
[root@Fedora ~]# yum install -y --downloadonly --downloaddir=./ <软件包名称>

# 下载本机已安装的软件包
[root@Fedora ~]# yum reinstall -y --downloadonly --downloaddir=./ <软件包名称>
```

系统会将软件包保存在默认的缓存目录中，添加 `--downloaddir` 选项后可以指定目的文件夹。

对于已安装的软件包，YUM不会自动下载其依赖，此时我们可以使用软件包"yum-utils"中的 `yumdownloader` 工具进行操作。

```text
# 安装"yum-utils"工具
[root@Fedora ~]# yum install -y yum-utils

# 下载软件包及其依赖组件
[root@Fedora ~]# yumdownloader --resolve --destdir=./ <软件包名称>
```
