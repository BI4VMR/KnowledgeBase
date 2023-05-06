# 简介
CentOS 7以及更早的系统通过"/etc/init.d/network"进行网络管理，服务名称为"network"，该程序通过读取配置文件设置网络参数，管理员修改配置文件后，重启"network"服务即可更新配置，使变更生效。对于使用Systemd的系统，命令为： `systemctl restart network` ；对于使用Service的系统，命令为： `service network restart` 。

> 🚩 提示
>
> 在CentOS 8以及更新的版本中，默认仅使用NetworkManager进行网络管理，如果仍需要使用"network"服务，请自行安装软件包"network-scripts"。

# 配置文件
网卡配置文件存放在"/etc/sysconfig/network-scripts/"目录中，文件名为"ifcfg-*"。系统安装过程中将为每张网卡自动生成一份配置文件，在系统安装完成后添加的网卡则需要管理员手工编写配置文件。

## 文件格式
网卡配置文件的每行为一个配置项，参数名称均为大写字母，参数值可以被引号包围，也可以直接书写。

```text
# ----- 以太网卡配置文件示例 -----
NAME=ens32
DEVICE=ens32
UUID=7adbbdef-ca58-47e4-bd5f-3f3e62b6a996
TYPE=Ethernet
BOOTPROTO=dhcp
DEFROUTE=yes
ONBOOT=yes
```

## 常用字段
🔷 `TYPE`

接口类型。常见的以太网卡为"Ethernet"，虚拟设备为"Bridge"、"Bond"等。

🔷 `BOOTPROTO`

动态地址获取协议。取值为"none"表示禁用；"static"表示仅使用静态地址；"bootp"表示使用BOOTP协议；"dhcp"表示使用DHCP协议。

🔷 `ONBOOT`

开机是否自动加载。取值为"yes"或"no"。

🔷 `DEFROUTE`

是否生成默认路由。取值为"yes"或"no"。

🔷 `PEERDNS`

是否将DNS同步写入"/etc/resolv.conf"中。取值为"yes"或"no"，自动获取、手工配置的服务器地址均可作为有效值被写入"resolv.conf"。

🔷 `USERCTL`

普通用户是否有权控制该网卡。取值为"yes"或"no"，默认为"no"。

🔷 `NM_CONTROLLED`

是否允许该网卡被NetworkManager管理。取值为"yes"或"no"。

🔷 `DEVICE`

设备名称。此字段与"/dev"下的设备文件名对应，NAME参数值应与其保持一致。

🔷 `UUID`

设备标识。同一台机器上的网卡ID不可冲突，复制虚拟机后必须重新生成该参数。

🔷 `HWADDR`

硬件MAC地址。此字段仅供管理员查看配置所用，取值必须和硬件MAC地址保持一致，否则会导致配置无法成功应用。

🔷 `MACADDR`

自定义MAC地址。此字段可以覆盖硬件MAC地址，网卡通信时采用此处设置的值。

## 设置IPv4地址
### 动态获取地址
将配置项"BOOTPROTO"的值设为"dhcp"，并确保配置项"ONBOOT"的值为"yes"即可。

### 配置静态地址
首先将配置项"BOOTPROTO"的值设为"none"或"static"，否则接口可能会通过动态方式获取到多个地址，然后添加以下配置：

```text
# IPv4地址
IPADDR=192.168.254.101

# 前缀长度
PREFIX=24

# 网关地址
GATEWAY=192.168.254.2

# 生成默认路由
DEFROUTE=yes

# DNS服务器地址
DNS1=101.6.6.6
DNS2=223.5.5.5
```

配置项"PREFIX"也可以更换为"NETMASK"，它以点分十进制格式设置子网掩码；IPv4的DNS服务器可以指定0至3个，每条记录将会同步写入至"/etc/resolv.conf"。

## 设置IPv6地址
### 动态获取地址
将DHCPV6C参数的值设为"yes"即可。

### 配置静态地址
请编辑配置文件，添加或修改以下配置：

```text
# 初始化IPv6
IPV6INIT=yes

# 禁用DHCP
DHCPV6C=no

# 使用SLAAC地址
IPV6_AUTOCONF=no

# 主要地址
IPV6ADDR=FEC0::1/64

# 次要地址
IPV6ADDR_SECONDARIES=FD00::1/64 FD00::1/64

# 默认网关
IPV6_DEFAULTGW=FEC0::FFFF

# 生成默认路由
IPV6_DEFROUTE=yes

# DNS服务器地址
DNS3=2001:da8::666
DNS4=2400:3200::1
```

IPv6可以通过SLAAC方式生成地址，也可以手工指定地址，这两种方式对应的配置项分别为"IPV6_AUTOCONF"和"IPV6ADDR"，二者互斥，不能同时配置。

IPv6允许在接口上配置多个次要地址，通过配置项"IPV6ADDR_SECONDARIES"的参数进行配置，多个地址之间请使用空格分隔。

IPv6的域名解析服务器也使用"DNS"配置项进行配置，编号自IPv4向后顺延即可。
