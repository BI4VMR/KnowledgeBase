# 概述
路由信息协议(Routing Information Protocol, RIP)是IGP中最早得到广泛应用的路由协议。

RIP协议基于距离矢量算法——贝尔曼-福特(Bellman-Ford)算法，在带宽、配置和管理方面要求较低，主要适用于规模较小的网络。



RIP协议报文封装在UDP中，使用520端口进行通信。



<div align="center">

| 协议号 | 协议类型 | 协议号 |
| :----: | :------: | :----: |
|   1    |   ICMP   |   2    |
|   4    |    IP    |   6    |
|   8    |   EGP    |   9    |
|   17   |   UDP    |   50   |
|   51   |    AH    |   89   |
|  111   |   IPX    |  112   |
|  115   |   L2TP   |  124   |

</div>
