# 概述
增强型内部网关路由协议(Enhanced Interior Gateway Routing Protocol)是Cisco开发的私有路由协议。EIGRP结合了距离矢量协议和链路状态协议的特点，采用弥散更新算法(DUAL)来实现快速收敛，可以不发送定期的路由更新报文以减少带宽的占用。
EIGRP使用单播和组播（组播地址：224.0.0.10）方式发送协议报文。其兼容模块可以同时支持IP、Appletalk、Novell和NetWare等多种网络层协议。


 EIGRP是无类协议，支持VLSM和CIDR。
 EIGRP能路由多种网络层协议，最大可支持100跳的网络。
 EIGRP的机制使其绝对不会产生环路。
 EIGRP使用增量更新，带宽占用少。
 EIGRP可以支持等价、非等价负载均衡。
