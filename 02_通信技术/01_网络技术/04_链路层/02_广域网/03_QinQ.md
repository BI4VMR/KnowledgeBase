1.1.1  概述
传统的VLAN并不能跨越广域网边界，使用QinQ技术可以实现广域网传输VLAN帧。
QinQ技术在用户网络的VLAN Tag前再插入一个广域网VLAN Tag，使报文携带两层VLAN Tag穿越公网。
其现在有两个标准：IEEE 802.1ad和IEEE 802.1ah。

1.1.2  工作原理
数据帧在私网中传输时携带私网VLAN Tag，当数据帧到达运营商的接入设备后，再打上一层公网的VLAN Tag，然后公网设备根据外层Tag转发数据帧，数据帧到达目的私网后再把外层VLAN Tag剥除，其为用户提供了一种较为简单的二层VPN隧道。

1.1.3  相关配置
	用户端
1.将出站端口配置为Trunk模式
Cisco(config-if)#switchport mode trunk
2.仅允许内层VLAN通过该接口
Cisco(config-if)#switchport trunk allow vlan [内层VLAN ID]
	运营商端
1.将入站端口配置为dot1q-tunnel模式
Cisco(config-if)#switchport mode dot1q-tunnel
2.封装外层VLAN
Cisco(config-if)#switchport access vlan [外层VLAN ID]
3.更改MTU防止发生分片
Cisco(config)#system mtu 1504
