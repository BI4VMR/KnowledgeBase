# 简介
NetworkManager目前是Linux系统中使用最广泛的网络管理工具，它可以统一管理各种物理、虚拟、有线、无线设备。该工具作为独立软件包存在，在使用时须保证"NetworkManager.service"服务正常运行，用户更改网络配置并提交后，服务会将变更同步至系统。

NetworkManager主要包括 `nmcli` 和 `nmtui` 两个命令，前者为CLI配置方式，后者为TUI配置方式。 `nmcli` 命令最常用的子菜单为 `device` 和 `connection` ， `device` 对应网络设备， `connection` 对应连接配置，它们可以被简写为 `nmcli d` 和 `nmcli c` 。

# 网络设备管理
## 查看设备信息
`nmcli device` 命令可以用于查看网络设备的概要信息：

```text
[root@Fedora ~]# nmcli device
DEVICE  TYPE      STATE      CONNECTION
ens32   ethernet  connected  ens32
wlan0   wifi      connected  Guest
lo      loopback  unmanaged  --
```

`DEVICE` 列表示设备名称； `TYPE` 列表示设备类型； `CONNECTION` 列表示当前与该设备关联的配置文件名称。

`STATE` 列表示设备状态，共有以下四种取值：

🔷 `connected`

已被NetworkManager管理，且连接已激活。

🔷 `disconnected`

已被NetworkManager管理，但连接未激活。

🔷 `unmanaged`

不允许NetworkManager管理此设备。

🔷 `unavailable`

设备不可用，例如链路状态为"Down"时。

<br />

我们可以执行 `nmcli device show [设备名称]` 命令，查看指定设备的详细信息：

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

## 激活或断开连接
一个设备只能激活一个连接配置，但可以拥有多个备选配置。

我们可以执行 `nmcli device connect <设备名称>` 命令使设备激活连接，若设备拥有多个连接配置，优先选择上次使用的配置，其次选择优先级数值较低的配置；若没有连接配置，则自动生成一个空的配置并将其激活。

我们可以执行 `nmcli device disconnect <设备名称>` 命令使设备断开连接，此时该设备不可用。

> ⚠️ 警告
>
> 在关闭一个接口前，请确认此设备还有其它管理方式，否则该设备将无法再被远程管理。

# 连接配置管理
## 查看配置信息
我们可以执行 `nmcli connection` 命令，查看所有连接配置的状态：

```text
[root@Fedora ~]# nmcli connection
NAME   UUID                                  TYPE      DEVICE
ens32  efb3377e-7bcf-4594-8c6d-e6880cbaa150  ethernet  ens32
```

我们还可以执行 `nmcli connection show [配置名称]` 命令，查看指定配置的详细信息。

## 修改配置
我们可以执行 `nmcli connection edit <配置名称>` 命令，进入交互式命令行，然后根据文本提示操作即可。这种方式类似于网络设备的CLI， `goto <菜单名称>` 表示进入子菜单； `back` 表示返回上一级； `change` 表示更改当前属性； `save` 表示保存所有变更； `quit` 表示退出交互式命令行。

```text
[root@Fedora ~]# nmcli connection edit E1

# 进入IPv4配置菜单
nmcli> goto ipv4

# 进入地址配置菜单
nmcli ipv4> goto addresses

# 修改当前配置
nmcli ipv4.addresses> change
编辑 "addresses" 值：172.18.6.2/16

# 返回上一级菜单
nmcli ipv4.addresses> back

# 返回上一级菜单
nmcli ipv4> back

# 保存配置
nmcli> save
成功地更新了连接 "E1" (fe093b1e-ffd4-3ff5-b64d-d4546126c02f)。

# 退出命令行
nmcli> quit
```

除了交互式命令行，我们还可以使用 `nmcli connection modify <配置名称> <属性> <值>` 命令，直接修改指定属性，这种方式适合在脚本中批量操作，常用的属性与输入格式可参考下文代码块：

```text
# 修改配置名称
[root@Fedora ~]# nmcli connection modify <配置名称> connection.id <新的名称>

# 修改地址配置方式
[root@Fedora ~]# nmcli connection modify <配置名称> ipv4.method <auto | manual>

# 修改IP地址
[root@Fedora ~]# nmcli connection modify <配置名称> ipv4.addresses <IP地址>/<前缀长度>

# 添加次要IP地址
[root@Fedora ~]# nmcli connection modify <配置名称> +ipv4.addresses <IP地址>/<前缀长度>

# 移除次要IP地址
[root@Fedora ~]# nmcli connection modify <配置名称> -ipv4.addresses <IP地址>/<前缀长度>

# 修改默认网关
[root@Fedora ~]# nmcli connection modify <配置名称> ipv4.gateway <网关地址>

# 修改DNS服务器
[root@Fedora ~]# nmcli connection modify <配置名称> ipv4.dns <服务器I>[,<DNS服务器II>]

# 修改网卡MAC地址
[root@Fedora ~]# nmcli connection modify <配置名称> 802-3-ethernet.cloned-mac-address <MAC地址（使用":"分隔）>
```

## 应用配置
我们可以使用 `nmcli connection <up | down> <配置名称>` 命令，启用或禁用配置，"up"操作可以更新设备已加载的配置，使管理员的修改立即生效。

## 新建配置
我们可以使用 `nmcli connection add` 命令新建连接配置：

```text
# 新建配置
[root@Fedora ~]# nmcli connection add con-name <配置名称> ifname <设备名称> type <类型>
```

新建配置时，我们必须指定连接名称、关联的设备和设备类型三个属性，其它属性可以在创建配置之后再按需修改。

## 删除配置
我们可以使用 `nmcli connection delete` 命令删除连接配置，若某个设备的所有配置均被移除，则状态立即变为"disconnected"，无法正常使用。
