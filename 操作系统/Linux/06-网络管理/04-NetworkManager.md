# 简介
NetworkManager是目前使用最广泛的网络管理工具，它可以统一管理各种物理、虚拟、有线、无线接口。该工具作为独立软件包存在，使用时须保证服务"NetworkManager.service"正常运行，用户更改网络配置并提交后，后台服务会将变更同步至系统。

NetworkManager主要包括"nmcli"和"nmtui"两个命令，前者为CLI配置方式，后者为TUI配置方式。"nmcli"最常用的子菜单为"device"和"connection"，"device"对应网络设备，"connection"对应连接配置，可以简写为 `nmcli d` 和 `nmcli c` 。

# 网络设备管理
## 查看设备信息
执行 `nmcli device` 可以查看网络设备概要信息：

```text
[root@Fedora ~]# nmcli device
DEVICE  TYPE      STATE      CONNECTION
ens32   ethernet  connected  ens32
wlan0   wifi      connected  Guest
lo      loopback  unmanaged  --
```

DEVICE列表示设备名称；TYPE列表示设备类型；CONNECTION列表示当前与该设备关联的配置文件名称。

STATE列表示设备的状态，共有以下四种取值：

🔷 `connected`

已被NM管理，已激活连接。

🔷 `disconnected`

已被NM管理，未激活连接。

🔷 `unmanaged`

不允许NM管理此设备。

🔷 `unavailable`

设备不可用，例如链路状态为Down时。

执行 `nmcli device show {设备名称}` 可以查看指定设备的详细信息：

```text
[root@Fedora ~]# nmcli device show lo
GENERAL.DEVICE:                    lo
GENERAL.TYPE:                      loopback
GENERAL.HWADDR:                    00:00:00:00:00:00
GENERAL.MTU:                       65536
GENERAL.STATE:                     10 (unmanaged)
GENERAL.CONNECTION:                --
GENERAL.CON-PATH:                  --
IP4.ADDRESS[1]:                    127.0.0.1/8
IP4.GATEWAY:                       --
```

## 激活/断开连接
一个设备只能激活一个连接配置，但可以拥有多个备选配置。

执行 `nmcli device connect [设备名称]` 可以使设备激活连接，若设备拥有多个连接配置，优先选择上次使用的配置，其次选择优先级数值较低的配置；若没有连接配置，则自动生成一个空的配置并将其激活。

执行 `nmcli device disconnect [设备名称]` 可以使设备断开连接。

> ⚠️ 警告
>
> 关闭一个接口前，请确认此设备有其它管理方式，否则该设备将无法被远程管理。

# 连接配置管理
## 查看配置信息
执行 `nmcli connection` 可以查看连接配置的状态：

```text
[root@Fedora ~]# nmcli connection
NAME   UUID                                  TYPE      DEVICE
ens32  efb3377e-7bcf-4594-8c6d-e6880cbaa150  ethernet  ens32
```

执行 `nmcli connection show {配置名称}` 可以查看指定配置的详细信息。

## 修改配置
执行 `nmcli connection edit [配置名称]` 可以进入交互式命令行，根据文本提示操作即可。这种方式类似于网络设备的CLI，"goto"表示进入子菜单；"back"表示返回上一级；"change"表示更改当前属性；"save"表示保存所有变更；"quit"表示退出程序。

执行 `nmcli connection modify [配置名称] [属性] [取值]` 可以直接修改指定属性，这种方式适合在脚本中进行操作，常用的属性与输入格式见下文。

```text
# 修改配置名称
[root@Fedora ~]# nmcli connection modify [配置名称] connection.id [新的名称]
# 修改地址配置方式
[root@Fedora ~]# nmcli connection modify [配置名称] ipv4.method [auto|manual]
# 修改IP地址
[root@Fedora ~]# nmcli connection modify [配置名称] ipv4.addresses [IP地址]/[前缀长度]
# 添加次要IP地址
[root@Fedora ~]# nmcli connection modify [配置名称] +ipv4.addresses [IP地址]/[前缀长度]
# 移除次要IP地址
[root@Fedora ~]# nmcli connection modify [配置名称] -ipv4.addresses [IP地址]/[前缀长度]
# 修改默认网关
[root@Fedora ~]# nmcli connection modify [配置名称] ipv4.gateway [网关地址]
# 修改DNS服务器
[root@Fedora ~]# nmcli connection modify [配置名称] ipv4.dns [服务器I],[DNS服务器II]
# 修改网卡MAC地址
[root@Fedora ~]# nmcli connection modify [配置名称] 802-3-ethernet.cloned-mac-address [MAC地址（使用":"分隔）]
```

## 应用配置
使用 `nmcli connection [up|down] [配置名称]` 可以启用或禁用配置，"up"操作还可以更新设备已加载的配置，使管理员的修改立即生效。

## 新建配置
使用 `nmcli connection add` 命令可以新建配置，我们必须指定连接名称、关联的设备和链路层类型三个属性，其它属性可以创建配置后按需修改。

```text
# 新建配置
[root@Fedora ~]# nmcli connection add con-name [配置名称] ifname [设备名称] type [类型]
```

## 删除配置
使用 `nmcli connection delete` 命令可以删除配置，若某个设备的所有配置均被移除，则状态立即变为"disconnected"，无法正常使用。
