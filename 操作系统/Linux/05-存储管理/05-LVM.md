
# 简介
逻辑卷管理器(Logical Volume Manager, LVM)是一种虚拟磁盘管理机制，通过Linux内核的Device Mapper实现，其创建的虚拟存储器位于存储设备和文件系统层之间。LVM可以将多个物理磁盘上空间映射至虚拟存储器，并且支持空间在线伸缩，使得磁盘管理更灵活。

目前正被广泛使用的版本是LVM 2，对应的软件包名称为："lvm2"，如果系统安装时未采用LVM方式配置分区，可能需要手动安装此软件包。

# 理论基础
## 物理卷(PV)
非LVM体系的存储区域可作为物理卷(Physical Volume, PV)，例如磁盘、磁盘分区、RAID卷等。物理卷是LVM的基本存储单元，其中含有LVM配置参数。

## 卷组(VG)
卷组(Volume Group, VG)是LVM中的存储池，可由一个或多个物理卷构成，其中的存储空间可以被划分为多个逻辑卷。

## 逻辑卷(LV)
逻辑卷(Logical Volume, LV)是供用户使用的虚拟设备，可以建立文件系统、挂载并存取文件。

## 物理块(PE)
物理块(Physical Extent, PE)是可被LVM寻址的最小存储单元，默认大小为4MB，将物理卷加入卷组时会为其划分PE。

## 逻辑块(LE)
逻辑卷中的存储单元称为逻辑块(Logical Extent, LE)，同一个卷组中的PE与LE大小相同。

<div align="center">

![LVM体系结构](./Assets-LVM/理论基础-LVM体系结构.jpg)

</div>

<!-- TODO

创建逻辑卷的基本操作步骤如下：

1.将普通存储区域转换为物理卷。

2.创建卷组，并将物理卷添加至卷组中。

3.创建逻辑卷，将卷组中的空间分配给逻辑卷。

4.格式化逻辑卷并将其挂载。

示例一：

现有两个100GB的磁盘"sdb"和"sdc"，我们需要将全部空间划入卷组"default"中，然后创建一个150GB的逻辑卷，并格式化为EXT4。

首先在磁盘上创建分区，并且为其设置LVM标记。

```text
[root@Fedora ~]# parted /dev/sdb
GNU Parted 3.2
Using /dev/sdc
Welcome to GNU Parted! Type 'help' to view a list of commands.
(parted) mktable gpt
# 创建分区，编号为1，尺寸为100%可用空间。
(parted) mkpart 1 1 100%
# 启用LVM标记
(parted) set 1 lvm on
(parted) quit
Information: You may need to update /etc/fstab.
"sdc"相关操作同上，此处省略。分区创建完毕后，将它们转换为LVM物理卷，并加入卷组"default"中。
# 将分区转为LVM物理卷
[root@Fedora ~]# pvcreate /dev/sd[b-c]1
  Physical volume "/dev/sdb1" successfully created.
  Physical volume "/dev/sdc1" successfully created.
# 查看物理卷信息
[root@Fedora ~]# pvdisplay /dev/sdb1
  "/dev/sdb1" is a new physical volume of "<100.00 GiB"
  --- NEW Physical volume ---
  PV Name               /dev/sdb1
  VG Name               
  PV Size               <100.00 GiB
  Allocatable           NO
  PE Size               0   
  Total PE              0
  Free PE               0
  Allocated PE          0
  PV UUID               g2tEhT-MO1F-M0zb-fQ5d-SxeG-cVyA-4m67U8

# 创建卷组"default"，指定PE大小为8MB。
[root@Fedora ~]# vgcreate default -s 8m /dev/sd[b-c]1
  Volume group "default" successfully created
# 查看卷组信息
[root@Fedora ~]# vgdisplay default
  --- Volume group ---
  VG Name               default
  System ID             
  Format                lvm2
  Metadata Areas        2
  Metadata Sequence No  1
  VG Access             read/write
  VG Status             resizable
  MAX LV                0
  Cur LV                0
  Open LV               0
  Max PV                0
  Cur PV                2
  Act PV                2
  VG Size               199.98 GiB
  PE Size               8.00 MiB
  Total PE              25598
  Alloc PE / Size       0 / 0
  Free  PE / Size       25598 / 199.98 GiB
  VG UUID               UgvrMy-ny11-RIfN-j7IH-WHy0-DPar-zVxkWO
接下来创建逻辑卷，然后将其格式化并挂载。
[root@Fedora ~]# lvcreate -L 150G -n data default
  Logical volume "data" created.
[root@Fedora ~]# lvdisplay
  --- Logical volume ---
  LV Path                /dev/default/data
  LV Name                data
  VG Name                default
  LV UUID                Ij4uGj-Md9r-JRR7-yBu3-tW2Z-gi4E-CY9bOr
  LV Write Access        read/write
  LV Creation host, time CentOS-8-Exp, 2021-08-31 11:17:07 +0800
  LV Status              available
  # open                 0
  LV Size                150.00 GiB
  Current LE             19200
  Segments               2
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     8192
  Block device           253:0

# 创建文件系统
[root@Fedora ~]# mkfs.ext4 /dev/mapper/default-data
# 挂载虚拟存储设备
[root@Fedora ~]# mount /dev/mapper/default-data /mnt

            4.5.4   扩容逻辑卷
LVM支持在线扩容功能，对于非系统分区所在的逻辑卷，可以实现动态增容而不必将其卸载。实施逻辑卷扩容的步骤如下：
    • 将新添加的存储区域转换为物理卷。
    • 将物理卷加入卷组。
    • 扩展虚拟卷。
    • 刷新文件系统，使其适应新的逻辑卷。
                • 示例
示例一：在前文基础上，添加磁盘"sdd"，将其加入卷组，并将逻辑卷"data"扩容。
首先在磁盘上创建分区，并将其加入卷组"default"。
# 创建分区
[root@Fedora ~]# parted /dev/sdd
GNU Parted 3.2
Using /dev/sdd
Welcome to GNU Parted! Type 'help' to view a list of commands.
(parted) mktable gpt
(parted) mkpart 1 1 100%
(parted) set 1 lvm on
(parted) quit
Information: You may need to update /etc/fstab.

# 将分区转为物理卷
[root@Fedora ~]# pvcreate /dev/sdd1
  Physical volume "/dev/sdd1" successfully created.
# 将分区加入卷组
[root@Fedora ~]# vgextend default /dev/sdd1
  Volume group "default" successfully extended
使用"lvextend"命令可以动态扩容逻辑卷，操作完成后应根据逻辑卷的文件系统选择对应工具，刷新文件系统参数，使其适应新的虚拟存储器。
# 扩展逻辑卷
[root@Fedora ~]# lvextend -L +100G /dev/default/data
  Size of logical volume default/data changed from 150.00 GiB (19200 extents) to 250.00 GiB (32000 extents).
  Logical volume default/data successfully resized.
# 刷新EXT4文件系统
[root@Fedora ~]# resize2fs /dev/default/data
# 查看逻辑卷容量
[root@Fedora ~]# df -h /dev/mapper/default-data
Filesystem                Size  Used Avail Use% Mounted on
/dev/mapper/default-data  246G   60M  235G   1% /mnt

-->
