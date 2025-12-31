# 概述
增强型内部网关路由协议(Enhanced Interior Gateway Routing Protocol, EIGRP)是Cisco开发的私有路由协议。EIGRP结合了距离矢量协议和链路状态协议的特点，采用弥散更新算法(DUAL)来实现快速收敛，可以不必定期发送路由更新报文，以此减少带宽的占用。

EIGRP使用单播和组播（组播地址：224.0.0.10）方式发送协议报文，其兼容模块可以同时支持IP、Appletalk、Novell和NetWare等多种网络层协议。

EIGRP具有以下特点：

- EIGRP是无类协议，支持VLSM和CIDR。
- EIGRP能路由多种网络层协议，最大可支持100跳的网络。
- EIGRP的机制使其绝对不会产生环路。
- EIGRP使用增量更新，带宽占用少。
- EIGRP可以支持等价、非等价负载均衡。

# 术语
## EIGRP进程
EIGRP用AS号来区分不同的进程，取值范围为 `[0, 65535]` ，这个AS号并不是真正意义上的自治系统号，只是用于区分不同的进程。

每个EIGRP进程只能路由一种网络层协议，并且两个路由器必须持有相同的AS号才能建立邻居关系。

## 通告距离(Advertised Distance, AD)
通告距离，是指邻居路由器通告的到达某个网段的度量值。

## 可行距离(Feasible Distance, FD)
可行距离，是指本地路由器到达某个网段的度量值。

$$
FD=AD+自己到邻居的度量值
$$

此处以一个实际场景说明AD与FD值的概念。

<div align="center">

![EIGRP的AD与FD概念](./Assets-EIGRP/术语-EIGRP的AD与FD概念.jpg)

</div>

在上图场景中，网段 `10.0.0.0/24` 的链路度量值为"5"，R2将该路由信息分别通告给R1与R3。

R1收到AD为"5"的路由信息后，再加上接收链路的度量值"5"，最终FD为"10"。R3方向通告的路由信息AD为"10"，R1从该路径计算出的最终FD为"15"。

最终R1到达 `10.0.0.0/24` 网段的主要路径为"R1 → R2"，FD值较小；次要路径为"R1 → R3 → R2"，FD值较大。

## 可行性条件(Feasible Condition)
如果到达同一目的网段存在多条路由，那么其中FD值较小的为最优路由；FD值较大的为次优路由，为最优路由的备份。

## 后继路由器(Successor)
到达目标网段的最佳下一跳路由器。

## 可行后继路由器(Feasible Successor)
到达目标网段的备份下一跳路由器。

## 主动路由
表明路由正在收敛，主动向邻居查询路由信息。

在路由器上使用 `show ip eigrp topology` 命令可以查看EIGRP的拓扑信息，其中标记为A的条目就是主动状态。

## 被动路由
表明路由收敛完毕，相关网段已经选定了后继路由器。

拓扑信息中标记为P的条目就是被动状态。

# 报文类型
## Hello
用于邻居发现与邻接关系维护，支持组播和单播方式，默认使用组播。

链路速度小于1.544Mbps时间隔为60秒，大于1.544Mbps时间隔为5秒。当一台路由器在3倍Hello时间后仍未收到邻居的Hello报文，则判定邻接关系失效。

## Update
用于发送路由更新，支持组播和单播方式，默认使用组播方式发送。

## Query
用于向邻居查询路由信息，支持组播和单播方式，默认使用组播方式发送。

## Reply
用于回复查询报文，只支持以单播方式发送。

## ACK
用于确认收到的更新、查询和回复报文。

该报文格式与Hello报文相同，只需要将ACK位置"1"，并且以单播方式发送。

## SIA Query
用于询问发送过查询报文但未回复的路由器是否仍然在工作。

## SIA Reply
用于回复SIA Query报文。

## Goodbye
当一台路由器关闭EIGRP进程时，将会向之前宣告路由的接口发送Goodbye报文，使邻居删除从自己学习到的路由条目。

# 度量值
## 简介
EIGRP使用复合度量值，可以同时考虑带宽、延迟、负载、可靠性和MTU五种因素，更精确的描述路径好坏。

## 取值方式
- 带宽取值为控制层面各个入接口的最小带宽：$BW=256*[10^7/BW_{Min}(Kbit/s)]$。
- 延迟取值为控制层面各个入接口的总延迟：$Delay=256*[Delay_{Sum}(微秒)/10]$。
- 负载取值为链路中各个分段的最大值。
- 可靠性取值为链路中各个分段的最小值。
- MTU取值为链路中各个分段的最小值。

## 度量值的计算
EIGRP使用K值控制影响度量值计算的因素，一共有5个K值，分别对应上述的5种因素。K值还会影响邻接关系的建立，管理域内的路由器K值设置应保持一致。

默认情况下K1=1, K2=0, K3=1, K4=0, K5=0，即使用带宽与延迟计算计算度量值。

无论K5设置为0或1，MTU均不参与计算。

🔷 默认情况（K=10100）

$$
Metric=256*(\frac{10^7}{BW}+\frac{Delay}{10})
$$

🔷 完整计算公式

$$
Metric=[(K1*BW)+(\frac{K2*BW}{256-Load})+(K3*Delay)]*(\frac{K5}{Reliability+K4})
$$

## 更改K值
我们可以通过以下命令更改K值：

```text
Cisco(config-router)# metric weights <TOS> <K1> <K2> <K3> <K4> <K5>
```

"TOS"参数用于QoS策略，当前并未使用，我们只能填写为"0"。

# 可靠性机制
## 邻居发现/恢复机制
使用Hello报文探测邻居状态，如果3倍Hello时间内没有收到邻居的Hello报文，则视为邻接关系失效，并且删除所有从该邻居学习到的路由条目。

## 可靠传输协议
接收方收到重要报文后会回复ACK消息，如果一定时间后发送方没有收到ACK消息，则进行重传，最多重传16次，超过16次则重置邻接关系。

## DUAL弥散更新算法
使用DUAL算法可以实现绝对无环和高速收敛。

## 协议相关模块(PDM)
EIGRP可以兼容多种不同的三层协议。

# DUAL算法
## 简介
EIGRP维护三张表：邻居表、拓扑表、路由表。其中拓扑表包含目的网段、FD值、AD值和发送的邻居信息。

DUAL算法分为本地计算与扩散更新计算；本地计算使EIGRP实现高速收敛，扩散更新计算

## 本地计算
1.试图为一个路由条目在拓扑表中寻找一条备份路由（满足FC条件）。
2.一旦某个条目失效，则立即切换到备份条目，并发送更新通告给邻居。

## 扩散更新计算
当本地没有去往目的网段的任何路由时，将会使用扩散更新计算。
1.将要计算的路由条目从Passive状态迁移为Active（不再用来转发流量），并将AD设为无穷大，向所有邻居发送更新报文通告该网段不可达。
2.向所有邻居发送查询报文
3.有邻居回复时，计算FD，将最优条目加表；所有邻居都回复该网段不可达时，将该条目删除。

当邻居收到查询报文时

1.本身即为查询条目的后继站，且有备份路由。
删除本地最优条目，切换为备份路由，并将备份路由回复给查询者。
2.本身即为查询条目的后继站，但无备份路由。
删除本地最优条目，把条目置为Active，本地向其它邻居发送查询报文，暂时不回复初始查询者。如果没有其它EIGRP邻居，则本地删除该条目，并告知查询者目的网段不可达。
3.本身不是查询条目的后继站
本地有去往该网段路由时，回复信息给查询者，本地没有去往该网段路由时，回复目的网段不可达。

## Stuck in Active
扩散更新算法需要等待所有邻居回复查询，查询者等待时间达到90秒时，会向邻居发送SIA Query报文，邻居回复SIA Reply报文，最多重传7次，重传7次之后如果邻居仍然没有任何回复，将会再等待180秒，如果仍没有回复，将重置邻接关系。
该特性仅较新版协议支持。


# 默认路由
🔷 方法一

在EIGRP区域的边界路由器上配置默认路由，然后宣告默认路由进入EIGRP进程。

```text
# 设置默认路由
Cisco(config)# ip route 0.0.0.0 0.0.0.0 <出站地址 | 出站接口>

# 将默认路由宣告至EIGRP进程中
Cisco(config-router)# router eigrp <AS号>
Cisco(config-router)# network 0.0.0.0
```

🔷 方法二

在EIGRP区域的边界路由器上配置默认路由，并重分发到EIGRP进程中。

```text
# 设置默认路由
Cisco(config)# ip route 0.0.0.0 0.0.0.0 <出站地址 | 出站接口>

# 将默认路由重分发至EIGRP协议
Cisco(config-router)# router eigrp <AS号>
Cisco(config-router)# redistribute static metric <度量值>
```

🔷 方法三

在EIGRP区域边界路由器的连接内部区域的接口上，进行极限CIDR汇总。

```text
Cisco(config)# interface <接口ID>
Cisco(config-if)# ip summary-address eigrp <AS号> 0.0.0.0/0
```

# 路由汇总
## 自动汇总
EIGRP的自动汇总特性与RIPv2相同，但是它只会汇总本地宣告的路由条目，而不汇总邻居传递过来的路由条目。

汇总后本地将会生成汇总网段指向Null0的路由，用于避免路由黑洞。如果收到本地不存在的网段的数据包，将会匹配到Null0路由而被丢弃，不会再转发到外部。

## 手动汇总
手工汇总不仅可以汇总本地产生的路由条目，还可以汇总邻居传递的路由条目。

在对外接口上使用以下命令可以部署手工汇总：

```text
Cisco(config-if)# ip summary-address eigrp <AS号> <汇总网络ID> <汇总子网掩码>
```

<!-- TODO


# 末节路由器
 简介
将路由器置为末节路由器时，其它路由器不会再向它发送查询报文，但它本身仍然可以向其他站点发送查询。

 配置方法
Cisco(config-router)#eigrp stub [附加参数]
[附加参数]:
参数	含义
receive-only	不通告路由条目，只接收路由更新
connected	只通告本地进程中的直连路由
summary	只通告本地产生的汇总路由
static	只通告本地重分发进EIGRP的静态路由
redistributed	只通告本地重分发进EIGRP的路由
leak-map	发送leak-map匹配的明细路由

# 负载均衡
 等价负载均衡
默认支持等价负载均衡，支持4条路径，最大支持16条。
 非等价负载均衡
通过更改Variance倍率，使非最优路由条目可以加入路由表进行负载均衡。
 更改倍率
Cisco(config-router)#variance multiplier [倍率]
 必要条件
	1.次优路由AD值小于最优路由的FD值(满足FC原则)。
	2.次优路由的FD小于最优路由的(FD*倍率)值。

# 更改占用带宽比例
 简介
EIGRP默认使用接口50%带宽发送协议报文，如果没有设置管理带宽，则使用物理带宽计算。
 改变比例
Cisco(config-if)#ip bandwidth-percent eigrp [AS号] [比例]

# 被动接口
 简介
当一个接口被置为被动接口时，将不能接收/发送任何EIGRP报文，包括单播报文。
 设置被动接口
Cisco(config-router)#passive-interface [端口ID]

# 单播更新
 简介
通过手动指定邻居，可以使接口只以单播形式发送路由更新，两端必须都开启。
 指定邻居
Cisco(config-router)#neighbor [对端IP] [出接口]

# 水平分割
## 简介
EIGRP不仅有链路级水平分割，还定义了进程级水平分割，两者要同时关闭才能真正关闭水平分割特性。

 相关配置
 开启/关闭链路级水平分割
Cisco(config-if)#{no} ip split-horizon
 开启/关闭进程级水平分割
Cisco(config-if)#{no} ip split-horizon eigrp [AS号]

# 认证
## 简介
EIGRP只支持链路级密文认证，并且必须使用钥匙链，发送端默认只使用序号最小的密钥，接收端按顺序匹配，一旦成功即通过认证。

## 配置方法

```text
# 定义钥匙链
Cisco(config)# key chain <钥匙链名称>
# 定义密钥
Cisco(config-keychain)# key <密钥序号>
# 定义密钥字符串
Cisco(config-keychain-key)# key-string <密钥字符串>
# 退出密钥配置菜单
Cisco(config-keychain-key)# exit
# 退出钥匙链配置菜单
Cisco(config-keychain)# exit

# 在链路上启用认证
Cisco(config-if)# ip authentication mode eigrp <AS号> md5
# 指定使用的钥匙链
Cisco(config-if)# ip authentication key-chain eigrp <AS号> <钥匙链名称>
```

-->

<!-- TODO
# 泄露列表
 简介
Leak-Map
用于控制路由汇总，使Route-Map匹配的条目被精确宣告出去而不会被抑制。
 使用步骤
1.定义ACL用于匹配路由条目
2.将ACL与Route-Map关联
3.进行汇总时使用Leak-Map调用Route-Map
Cisco(config-if)#ip summary-address eigrp [AS号] [汇总网段] [子网掩码]  leak-map [Route-Map]

-->




<!-- TODO

1.1.8  相关配置
 基本配置
 启用EIGRP进程
Cisco(config)#router eigrp [AS号]
 设置路由器ID
Cisco(config-router)#eigrp router-id [Router ID]
 宣告网段（主类方式）
Cisco(config-router)#network [主类网络号]
 宣告网段（精确方式）
Cisco(config-router)#network [网络号] [通配符掩码]

 参数调整
 启用/关闭自动汇总
Cisco(config-router)#auto-summary
 设置负载均衡的最大条数
Cisco(config-router)#maximum-paths [1-16]
 修改Hello时间
Cisco(config-if)#ip hello-interval eigrp [AS号] [Hello时间/秒]
 修改Hold时间
Cisco(config-if)#ip hello-interval eigrp [AS号] [Hold时间/秒]
 修改弥散更新等待时间
Cisco(config-router)#timers active-time [Active时间/分|Disable]

 状态查看
 查看IP路由协议信息
Cisco#show ip protocol
 查看EIGRP邻居信息
Cisco#show ip eigrp neighbors
 查看启用EIGRP的接口信息
Cisco#show ip eigrp interfaces
 查看拓扑表
Cisco#show ip eigrp topology



-->