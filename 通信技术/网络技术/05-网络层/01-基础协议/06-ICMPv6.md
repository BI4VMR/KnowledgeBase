# 概述
ICMPv6是IPv6的一个基础协议，由RFC 2463定义。ICMPv6整合了IPv4中的ARP、ICMP和IGMP协议，除此之外还实现了无状态地址自动配置、重复地址检测、邻居不可达检测等功能。

# 报文格式
ICMPv6包括差错和信息两类报文，它们拥有相同的报文头部，消息内容使用TLV格式表示。

<div align="center">

![ICMPv6报文格式](./Assets-ICMPv6/报文格式-ICMPv6报文格式.jpg)

</div>

🔷 Type

消息类型，长度1字节。

用于对各种消息进行粗略的分类，取值为 `[0, 127]` 时表示差错报文；取值为 `[128, 255]` 时表示信息报文。

🔷 Code

消息代码，长度1字节。

用于对某种类型的报文进行细分，描述更详细的信息。

🔷 Checksum

校验和，长度2字节。

用于校验报文的完整性，包括IPv6源地址、目的地址、载荷长度、下一头部和整个ICMPv6报文。

# 报文类型
ICMPv6信息报文将在后文中详细介绍，常见的差错报文如下表所示：

<table align="center">
    <tr>
        <td align="center">Type</td>
        <td align="center">含义</td>
        <td align="center">Code</td>
        <td align="center">含义</td>
    </tr>
    <tr>
        <td align="center" rowspan="5">1</td>
        <td align="center" rowspan="5">目标不可达</td>
        <td align="center">0</td>
        <td align="center">无目标路由</td>
    </tr>
    <tr>
        <td align="center">1</td>
        <td align="center">通信管理禁止</td>
    </tr>
    <tr>
        <td align="center">2</td>
        <td align="center">未指定错误</td>
    </tr>
    <tr>
        <td align="center">3</td>
        <td align="center">目标地址不可达</td>
    </tr>
    <tr>
        <td align="center">4</td>
        <td align="center">目标端口不可达</td>
    </tr>
    <tr>
        <td align="center">2</td>
        <td align="center">数据包过长</td>
        <td align="center">0</td>
        <td align="center">数据包过长</td>
    </tr>
    <tr>
        <td align="center" rowspan="2">3</td>
        <td align="center" rowspan="2">超时</td>
        <td align="center">0</td>
        <td align="center">TTL耗尽</td>
    </tr>
    <tr>
        <td align="center">1</td>
        <td align="center">分片重组超时</td>
    </tr>
    <tr>
        <td align="center" rowspan="3">4</td>
        <td align="center" rowspan="3">参数错误</td>
        <td align="center">0</td>
        <td align="center">包头字段错误</td>
    </tr>
    <tr>
        <td align="center">1</td>
        <td align="center">无法识别下一头部字段</td>
    </tr>
    <tr>
        <td align="center">2</td>
        <td align="center">无法识别扩展头部</td>
    </tr>
</table>

# 邻居发现协议
## 简介
邻居发现协议(Neighbor Discovery Protocol, NDP)由RFC 2461定义，使用ICMPv6的5种消息实现了地址解析、重复地址检测、邻居状态检测、路由器发现和重定向功能。

NDP取代了ARP协议，在以太网环境中，获取其它节点MAC地址时不再广播发送请求报文，而是使用组播点对点通信，不会占用无关节点的处理能力，提升了通信效率。这种优化机制在不支持组播的低端交换机中不会生效，此时交换机仍然会采用广播方式转发报文，但无关客户端发现组播MAC地址并未被自身监听，就可以直接丢弃报文，无需解开链路层封装进一步判断。

## 工作流程
IPv6节点会为每个IPv6单播地址各加入一个组播组，前缀固定为 `FF02:0:0:0:0:1:FFXX:XXXX/104` ，"X"对应接口地址中的后24位。

请求者知道被请求节点的IPv6地址，根据映射关系便可得知被请求节点组播地址。请求者发送邻居请求(Neighbor Solicitation Message, NS)报文(Type 135)，源地址是自身的IPv6地址，目的地址是被请求节点组播地址，内容为查询被请求者的MAC地址，并携带自身的MAC地址。

被请求者加入了被请求节点组播组，因此能够收到NS请求报文，将会向请求者回复邻居通告(Neighbor Advertisement Message, NA)报文(Type 136)，其中包含自身的MAC地址。

<div align="center">

![邻居查询报文](./Assets-ICMPv6/邻居发现协议-邻居查询报文.jpg)

</div>

PC1欲发送数据包给PC2，需要获得其MAC地址，首先发送NS报文进行查询，目的地址为PC2的被请求节点组播地址(FF02::1:FF00:0002)。若交换机支持组播，报文只会转发给PC2；若交换机不支持组播，则会将该报文广播到其它接口。

PC3根据组播地址在链路层的映射关系，发现自身并没有加入组播组3333:FF00:0002，所以直接丢弃报文，无需解开三层封装并比较。

<div align="center">

![邻居应答报文](./Assets-ICMPv6/邻居发现协议-邻居应答报文.jpg)

</div>

PC2接收NS报文后，单播发送NA报文给PC1回应自身的MAC地址，同时记录下NS报文中PC1的MAC地址，此时双方就知晓了对方的MAC地址。

# 邻居状态检测
NDP协议不仅能够发现邻居，还能探测邻居的状态。

<div align="center">

![邻居状态机](./Assets-ICMPv6/邻居状态检测-邻居状态机.jpg)

</div>

🔷 Incomplete

未完成状态，是发送NS报文但还未收到NA报文时的状态。

🔷 Reachable

可达状态，收到NA报文后的邻居状态，若发送NS报文10秒后还未收到NA报文，则删除条目。

🔷 Stale

稳定状态，邻居状态为可达30秒后，进入该状态。

🔷 Delay

延迟状态，本端给邻居发送数据时，将会保持在该状态，收到邻居的NA报文或应用层响应后，回到可达状态；5秒内未收到任何响应，则进入探查状态。

🔷 Probe

探查状态，每隔1秒单播发送NS报文给邻居，收到NA报文后回到可达状态，若连续发送3个NS报文后仍未收到NA报文，则再等待一段时间，还未收到响应就删除条目。

<br />

# 重复地址检测
重复地址检测(Duplicate Address Detection, DAD)机制使用NS和NA报文实现。

设备接口配置新的IPv6地址后，将会发送一个NS报文，源地址为"::"，目的地址为自身的被请求节点组播地址，ICMP报文中的目标字段为自身IPv6地址。正常情况下，只有本节点会监听自己的被请求节点组播地址，不应该收到NA报文；若已有节点使用该地址，它们将回复NA报文，这样就可以得知发生了地址冲突。

# 无状态地址自动配置
## 简介
IPv6支持无状态地址自动配置(Stateless Address Autoconfiguration, SLAAC)机制，此功能集成在ICMPv6协议中，无需DHCP就能自行完成地址配置。

## 工作流程
网段内的路由器默认每隔200秒发送路由器通告(Router Advertisement, RA)报文(Type 134)，向其它节点通告本网段的前缀信息，其它节点使用EUI-64生成接口标识符，就可以组成自己的IPv6地址。

当一个接口配置了SLAAC IPv6地址后，将会主动发送路由器请求(Router Solicitation, RS)报文(Type 133)请求网络前缀，路由器收到后将立即回复RA报文。如果该节点是一台终端设备，还会将通告前缀的路由器设置为自身的默认网关。

## SLAAC地址的生命周期
SLAAC地址的生命周期如下图所示：

<div align="center">

![SLAAC地址生命周期](./Assets-ICMPv6/无状态地址自动配置-SLAAC地址生命周期.jpg)

</div>

节点刚获取SLAAC地址时需要进行DAD检测，此阶段地址不可用；若没有发现地址冲突，则地址可用。RA报文中含有有效时间和推荐时间两个字段，每当收到新的RA报文时计时器将会重置。推荐时间结束后，地址进入不建议使用状态，此时可作为目的地址，但不推荐作为源地址使用，除非不得不用（详见RFC 3484）；有效时间结束后，地址将被删除。

# 路径MTU侦测
IPv6不支持通过中间节点进行分片，当始发节点要发送数据时，首先通过路径MTU侦测(Path MTU Discovery, PMD)机制，探测路径上的最小MTU，然后以最小MTU执行分片再发送报文。

始发节点首先假设路径MTU为数据包出站接口的MTU，发送数据包进行测试；若中间节点存在MTU较小的接口，则会发送ICMPv6 Type 2报文给始发节点，通知其数据包过大，并告知应当使用的MTU值；始发节点再使用中间节点通告的MTU值分片并发送报文，以此类推，直到能够抵达目标节点为止，此时的MTU值即路径MTU值。

当两端的节点需要双向通信时，数据包来回路径不一定相同，因此路径MTU是单向的，目的节点发送回应报文时，需要重新进行侦测过程。

<div align="center">

![侦测过程](./Assets-ICMPv6/路径MTU侦测-侦测过程.jpg)

</div>

默认情况下，MTU值由接口类型决定，我们也可以手动指定接口的MTU值，最小值规定为1280字节，最大值由链路类型决定。


<!-- TODO
使用以下命令可以查看IPv6邻居信息：
Cisco#show ipv6 neighbors


                • 计时器配置
    • 配置Reachable到Stale状态计时器
Cisco(config-if)#ipv6 nd reachable-time [时间/毫秒]
    • 配置NS重传计时器
Cisco(config-if)#ipv6 nd ns-interval [时间/毫秒]
    • 配置NS重传次数
Cisco(config-if)#ipv6 nd nud retry [1-3]

我们可以修改接口发送的DAD探测报文数量：
Cisco(config-if)#ipv6 nd dad attempts [探测报文数量]

                • 通告额外的IPv6前缀
默认情况下设备只通告接口上配置的IPv6地址前缀，也可以通告本地没有的前缀。
Cisco(config-if)#ipv6 nd prefix [IPv6前缀]/[前缀长度] [推荐时间] [有效时间]
                • 参数调整
    • 配置RA报文有效时间(默认1800秒)
Cisco(config-if)#ipv6 nd ra lifetime [时间/秒]
    • 配置RA报文发送间隔(默认200秒)
Cisco(config-if)#ipv6 nd ra interval [时间/秒]
    • 配置RA报文分发的默认路由优先级
Cisco(config-if)#ipv6 nd router-preference [High|Medium|Low]



在Cisco设备上，开启IPv6路由功能后将会发送RA报文，可以使用以下命令关闭：
Cisco(config-if)#ipv6 nd suppress-ra

MTU
Cisco(config-if)#ipv6 mtu [MTU值/字节]
-->