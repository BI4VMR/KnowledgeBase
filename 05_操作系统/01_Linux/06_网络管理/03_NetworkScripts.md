# 简介
CentOS 7以及更早的系统通过 `/etc/init.d/network` 配置文件进行网络管理，服务名称为"network"，该程序通过读取配置文件设置网络参数，管理员修改配置文件后，重启"network"服务即可更新配置，使变更生效。

对于使用Systemd的系统，更新配置的命令为： `systemctl restart network` ；对于使用Service的系统，更新配置的命令为： `service network restart` 。

> 🚩 提示
>
> 在CentOS 8以及更新的版本中，默认仅使用NetworkManager进行网络管理，如果我们希望使用"network"服务，请自行安装软件包"network-scripts"并停止NetworkManager的服务。

# 配置文件
## 简介
网卡配置文件存放在 `/etc/sysconfig/network-scripts/` 目录中，文件名为 `ifcfg-<接口名称>` 。系统安装过程中将为每张网卡自动生成一份配置文件，在系统安装完成后添加的网卡则需要管理员手工编写配置文件。

## 文件格式
网卡配置文件的每行为一个配置项，参数名称均为大写字母，参数值可以被双引号包围，也可以直接书写。

下文代码块展示了一个以太网卡配置文件的内容：

"ifcfg-ens32":

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
该配置文件的常用字段如下文内容所示：

🔷 `TYPE`

接口类型。

常见的以太网卡为"Ethernet"，虚拟设备为"Bridge"、"Bond"等。

🔷 `BOOTPROTO`

动态地址获取协议。

取值为"none"表示禁用；"static"表示仅使用静态地址；"bootp"表示使用BOOTP协议；"dhcp"表示使用DHCP协议。

🔷 `ONBOOT`

开机是否自动加载。

取值为"yes"或"no"。

🔷 `DEFROUTE`

是否生成默认路由。

取值为"yes"或"no"。

🔷 `PEERDNS`

是否将DNS同步写入 `/etc/resolv.conf` 中。

取值为"yes"或"no"，自动获取、手工配置的服务器地址均可作为有效值被写入 `resolv.conf` 。

🔷 `USERCTL`

普通用户是否有权控制该网卡。

取值为"yes"或"no"，默认值为"no"。

🔷 `NM_CONTROLLED`

是否允许该网卡被NetworkManager管理。

取值为"yes"或"no"。

🔷 `DEVICE`

设备名称。

该字段必须与 `/dev` 下的设备文件名对应， `NAME` 属性的值也应当与其保持一致。

🔷 `UUID`

设备标识。

同一台机器上的网卡ID不可冲突。

🔷 `HWADDR`

硬件MAC地址。

该字段仅供管理员查看配置所用，取值必须和硬件MAC地址保持一致，如果随意填写会导致配置无法被成功加载。

🔷 `MACADDR`

自定义MAC地址。

该字段可以覆盖硬件MAC地址，网卡通信时将采用此处设置的值。

# 配置基本网络参数
以下示例展示了使用NetworkScripts进行基本网络参数配置的方法。

🔴 示例一：配置IPv4静态地址。

在本示例中，我们为网卡配置IPv4静态地址。

第一步，我们将网卡配置文件中 `BOOTPROTO` 配置项的值设为"none"或"static"，防止该网卡通过DHCP协议获取到多个IP地址。

第二步，我们在网卡配置文件中添加或修改以下配置项：

"ifcfg-ens32":

```text
# ----- 以太网卡配置文件示例 -----

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

配置项 `PREFIX` 也可以更换为 `NETMASK` ，它以点分十进制格式表示子网掩码。

IPv4的DNS服务器可以指定0至3个，每条记录都会被同步写入至 `/etc/resolv.conf` 文件。

此时我们重启"network"服务使得新的配置生效，IPv4静态地址配置就完成了。

🟠 示例二：配置IPv4动态地址。

在本示例中，我们为网卡配置IPv4动态地址。

"ifcfg-ens32":

```text
# ----- 以太网卡配置文件示例 -----

# 开启DHCP协议
BOOTPROTO=dhcp

# 开机自动启动
ONBOOT=yes
```

动态地址可以和静态地址并存，我们可以根据实际需要删除静态地址的相关配置。

🟡 示例三：配置IPv6静态地址。

在本示例中，我们为网卡配置IPv6静态地址。

我们需要编辑配置文件，添加或修改以下配置项：

"ifcfg-ens32":

```text
# ----- 以太网卡配置文件示例 -----

# 初始化IPv6
IPV6INIT=yes

# 禁用DHCP
DHCPV6C=no

# 使用SLAAC地址
IPV6_AUTOCONF=no

# 主要地址
IPV6ADDR=FEC0::1/64

# 次要地址
IPV6ADDR_SECONDARIES=FD00::1/64

# 默认网关
IPV6_DEFAULTGW=FEC0::FFFF

# 生成默认路由
IPV6_DEFROUTE=yes

# DNS服务器地址
DNS3=2001:da8::666
DNS4=2400:3200::1
```

IPv6可以通过SLAAC方式生成主要地址，我们也可以手动指定主要地址，这两种方式对应的配置项分别为 `IPV6_AUTOCONF` 和 `IPV6ADDR` ，二者互斥，不能同时配置。

IPv6允许在单个接口上配置多个次要地址，我们可以通过配置项 `IPV6ADDR_SECONDARIES` 进行配置，多个地址之间需要使用空格分隔。

IPv6的域名解析服务器也使用配置项 `DNS` 进行配置，编号自IPv4向后顺延即可。

🟢 示例四：配置IPv6动态地址。

在本示例中，我们为网卡配置IPv6动态地址。

"ifcfg-ens32":

```text
# ----- 以太网卡配置文件示例 -----

# 开启DHCPv6协议
DHCPV6C=yes

# 开机自动启动
ONBOOT=yes
```

动态地址可以和静态地址并存，我们可以根据实际需要删除静态地址的相关配置。
