# 概述
多生成树协议(Multiple Spanning Tree Protocol, MSTP)属于IEEE 802.1s标准，来自Cisco的私有协议MISTP(Multiple Instances Spanning Tree Protocol)。

MSTP基于“实例”创建进程，每个实例可以关联多个VLAN，实例中的VLAN共享同一棵生成树，不必像PVST协议那样，为每个VLAN创建单独的进程，减少了硬件资源的占用，并且能够实现基于实例的负载均衡，充分利用所有链路。

# 术语
## MST域
一组拥有相同MST域名（不能为空）的交换机集合。

## 修订号
只有域名与修订号都相同才能组成同一个MST域。

## 实例
每个实例(Instance)对应独立的生成树进程，将VLAN与实例关联后它们将共享这棵生成树。

## 内部生成树
默认配置中存在一个Instance 0，所有VLAN都映射到其中，该实例就是内部生成树(IST)，可以与MST区域外的其它区域交互，兼容不支持MSTP的设备。

# 工作原理
MSTP将网络划分为一个或多个区域，一个MST区域是一组具有相同MST参数的交换机，拥有相同数量的实例，并且拥有相同的VLAN映射。交换机为每个实例分别维护一棵生成树，每个实例分别独立地使用RSTP机制创建各自的生成树。

MSTP使用系统扩展ID的设计思想，将实例编号嵌入优先级字段中，这样交换机就能够区分不同实例的BPDU了。系统扩展ID为12位，所以实例编号范围为： `[0, 4096]` 。

要使MSTP正常工作，各设备上的MST域名、修订号、实例映射完全一致。在同一个MST域中，即使交换机上根本没有某些VLAN，也需要在实例配置中声明，否则将会导致协商失败。

<!-- TODO
基础配置
    • 设置生成树协议为MSTP
Cisco(config)#spanning-tree mode mst
    • 进入MSTP配置模式
Cisco(config)#spanning-tree mst config
    • 配置MSTP域名
Cisco(config-mst)#name [域名]
    • 配置MSTP修订号
Cisco(config-mst)#revision [修订号]
    • 创建实例
Cisco(config-mst)#instance [实例ID] vlan [VLAN ID]
    • 修改实例的优先级
Cisco(config)#spanning-tree mst [实例ID] priority [优先级]

查询相关信息
    • 查看MST配置信息
Cisco#show spanning-tree mst configuration
-->
