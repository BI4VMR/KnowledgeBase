<!-- TODO


bridge-utils


```text
# 启用IPv6
sysctl net.ipv6.conf.br0.disable_ipv6=0

# 开启自动配置
sysctl net.ipv6.conf.br0.autoconf=1

# 接受路由器的RA报文
sysctl net.ipv6.conf.br0.accept_ra=1

# 生成默认路由
sysctl net.ipv6.conf.br0.accept_ra_defrtr=1
```
-->
$ brctl addbr <name>     ## 创建一个名为 name 的桥接网络接口
$ brctl delbr <name>     ## 删除一个名为 name 的桥接网络接口，桥接网络接口必须先 down 掉后才能删除
$ brctl show             ## 显示目前所有的桥接接口

$ brctl addif <brname> <ifname>  
#把一个物理接口 ifname 加入桥接接口 brname 中，所有从 ifname 收到的帧都将被 brname 处理
#就像网桥处理的一样。所有发往 brname 的帧，ifname 就像输出接口一样。当物理以太网接口加入网桥后，处于混杂模式了，所以不需要配置IP
$ brctl delif <brname> <ifname>    ## 从 brname 中脱离一个ifname接口
$ brctl show <brname>              ## 显示一些网桥的信息
brctl addbr newbridge			 #添加新网桥
brctl show
brctl delbr newbridge      #删除网桥