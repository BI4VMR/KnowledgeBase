<!-- TODO
            8.1.1   概述(TODO)
HSRP协议允许多台路由器共享一个虚拟的IP地址和MAC地址，当一台路由器发生故障时，数据转发将会自动切换至其它路由器，终端用户并不会感知到故障的存在。
HSRP协议是Cisco开发的私有协议，使用UDP的1985端口。
            8.1.2   术语
                • HSRP路由器
是指运行HSRP协议的路由器，是物理实体。
                • 虚拟路由器
是指HSRP进程虚拟出的逻辑路由器，其拥有虚拟IP地址、MAC地址，充当终端用户的默认网关。
                • HSRP组
HSRP使用组来标识虚拟路由器，组号取值范围：[0,255]。将接口加入到同一个组中，HSRP将会通过协议报文选举主控设备与备份设备，一旦主控设备失效，备份接口将会接替其工作。
                • 虚拟IP/MAC地址
HSRP组需要手动配置一个虚拟IP地址，终端以此地址作为自己的网关，每个IP对应一个MAC地址，格式为0000.0C07.ACXX，其中XX代表组号。
                • 优先级
优先级用于主路由器的选举，值越大优先级越高，HSRPv1取值范围为[0,255]，默认值为100，HSRPv2取值范围为[0,4096]。
            8.1.3   报文类型
                • Hello报文
HSRP通过周期性的收发Hello报文检测邻居状态，目的地址为224.0.0.2，周期为3.3秒。如果三倍Hello时间内没有收到Hello报文，则判定邻居离线。
            8.1.4   HSRP状态机
                • Initial
初始状态，刚启用HSRP进程，还没有收发任何协议报文。
                • Learn
当节点没有配置虚拟IP时，向邻居学习虚拟IP的过程。
                • Listen
节点处于监听状态，监控Standby路由器状态。
                • Speak
节点开始发送Hello报文。
                • Standby
发送完Hello后进入此状态，根据收到的Hello报文确定进入Active状态、停留在StandBy状态还是进入Listen状态，如果保持在此状态，则监控Active路由器的状态。
                • Active
节点通过报文交互后成为主控路由器并开始转发数据包的状态。
            8.1.5   工作流程
首先比较优先级，优先级最高的路由器进入Active状态，优先级次高的进入Standby状态，其它路由器处于Listen状态。如果优先级相同，则比较接口持有的IP地址，数值较大的优先。
            8.1.6   配置方法
                • 基础配置
    • 将接口加入组中并启用HSRP
Cisco(config-if)#standby [组ID] ip [虚拟IP]
    • 设置优先级
Cisco(config-if)#standby [组ID] priority [优先级]
                • 参数调整
    • 修改Hello和Hold计时器
Cisco(config-if)#standby [组ID] timers [Hello时间] [Hold时间]
                • 查询相关信息
    • 查看HSRP摘要信息
Cisco#show standby brief
            8.1.7   抢占模式
HSRP协议默认Active路由器不能被抢占，但是Standby路由器可以被抢占。如果允许抢占Active路由器，还可以设置抢占延时，以便其它控制层面协议准备就绪。
                • 相关配置
    • 开启抢占模式
Cisco(config-if)#standby [组ID] preempt {delay minimum [抢占延时/秒]}
            8.1.8   上行链路追踪
默认情况下上行链路失效后HSRP不会进行切换，将会产生次优路径，配置上行链路追踪后，上行链路失效时设备将会降低自身优先级，配合抢占模式可实现路径自动切换。
                • 相关配置
    • 配置接口状态追踪
Cisco(config-if)#standby [组ID] track [接口ID] {优先级偏移量}
优先级偏移量：当检测到链路失效后，本地优先级降低的数值，默认为10。
    • 配置高级追踪（示例：追踪路由条目）
1.创建追踪对象。
Cisco(config)#track [对象编号] ip route [网络ID] [子网掩码] reachability
2.在组中关联追踪对象。
Cisco(config-if)#standby [组ID] track [对象编号] {decrement [优先级偏移量]}
 -->
 