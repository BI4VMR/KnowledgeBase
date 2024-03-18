1.1  PPP
1.1.1  概述
PPP(点到点协议)是为在同等单元之间传输数据包这样的简单链路设计的链路层协议。提供全双工操作，并且按照顺序传递数据包，具有错误检测以及纠错能力，支持数据压缩；还具有身份验证功能。其设计目的主要是用来通过拨号或专线方式建立点到点连接，使其成为各种主机和路由器之间简单连接的一种共通的解决方案。
在 TCP/IP 协议集中它是一种用来同步调制连接的数据链路层协议，替代了原来非标准的第二层协议：SLIP。PPP还可以携带其它协议，包括NetBEUI、NWLINK和IPX。

1.1.2  帧格式

Flag	Address	Control	Protocol	Data	FCS	Flag

1.	Flag：帧起止定界符，规定为01111110。
2.	Address：地址字段，值为11111111，PPP链路的一个方向上只有一个接收方。
3.	Control：控制字段，默认为00000011。
4.	Protocol：协议字段，用于标明携带的网络层协议类型。

1.1.3  协商过程
1.LCP协商物理层与数据链路层参数。
2.协商加密方式(可选)。
3.NCP协商网络层协议。

1.1.4  链路认证
	简介
PPP支持两种认证方式，一种是PAP，一种是CHAP。相对来说PAP的认证方式安全性没有CHAP高。PAP使用明文传输密码，而CHAP传输的是密码的哈希值。

	配置PAP认证
5.	在被认证端设置用户名和密码
Cisco(config-if)#ppp pap sent-username [用户名] password [密码]
6.	在主认证端数据库中添加用户名和密码记录
Cisco(config)#username [用户名] password [密码]
7.	进入主认证端的端口开启PAP认证功能
Cisco(config-if)#ppp authentication pap

	配置CHAP认证
8.	在被认证端设置用户名和密码
Cisco(config-if)#ppp chap hostname [用户名]
Cisco(config-if)#ppp chap password [密码]
9.	在主认证端数据库中添加用户名和密码记录
Cisco(config)#username [用户名] password [密码]
10.	进入主认证端的端口开启PAP认证功能
Cisco(config-if)#ppp authentication chap
注意：CHAP认证在被认证端只定义密码时，默认发送主机名作为认证用户名。

	配置双向认证
如果要使用双向认证，只要在两端的设备上都启用认证并创建用户名和密码，在各自的对端发送用户名和密码即可。

1.1.5  Multilink
	简介
PPP Multilink特性可以将多条PPP链路捆绑成一条带宽更大的逻辑链路。主要起到增加带宽，减少延时，线路冗余的作用，另外一个作用是可以将不同类型的接口捆绑为一个逻辑接口，多链路PPP常用于同步接口、异步接口。
PPP Multilink是由LCP在初始化时设置的一个功能选项。本端LCP将Packet分成多个小的片段同时送到对端设备，对端LCP再将它们恢复成完整的Packet。

	配置步骤
1.更改接口封装模式为PPP
Cisco(config-if)#encapsulation ppp
2.将接口加入Multilink组
Cisco(config-if)#ppp multilink group [逻辑接口编号]
3.进入逻辑接口，设置IP地址
Cisco(config-if)#ip address [IP地址] [子网掩码]
4.查看Multilink状态
Cisco#show ppp multilink

	分片与交错功能
分片功能可以将大的报文切分成较小的分片，交错功能可以使分片夹杂在普通报文中发送。分片与交错功能结合可以减缓拥塞发生时产生的抖动。
11.	启用分片
Cisco(config-if)#ppp multilink fragment delay [毫秒]
推荐取值为10毫秒。
12.	启用交错
Cisco(config-if)#ppp multilink interleave

1.1.6  QoS支持
	压缩功能
压缩功能有助于节约广域网链路的带宽。
13.	启用压缩
Cisco(config-if)#compress [压缩算法]
[压缩算法]：
可选算法为lzs、mppc、predictor或stac，推荐使用stac。
