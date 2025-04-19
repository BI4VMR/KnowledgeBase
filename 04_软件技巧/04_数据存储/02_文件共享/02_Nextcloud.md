# 疑难解答
## 索引

<div align="center">

|       序号        |                         摘要                          |
| :---------------: | :---------------------------------------------------: |
| [案例一](#案例一) | PHP版本更新后，点击登录按钮片刻后又重新回到登录界面。 |
| [案例二](#案例二) |        数据库容量增长过快，导致磁盘空间不足。         |

</div>

## 案例一
### 问题描述
PHP版本更新后，点击登录按钮片刻后又重新回到登录界面。

### 问题分析
首先进入NextCloud数据目录，查看 `nextcloud.log` 日志文件的内容。

```text
# 进入NextCloud数据目录（路径视具体情况而定）
[root@Fedora ~]# cd /srv/nextcloud

# 查看最近的日志消息
[root@Fedora nextcloud]# tail -f -n 10 nextcloud.log
{"reqId":"mZBEu0PHYGo3Wd09cjep","level":3,"time":"2024-02-01T14:41:16+00:00","remoteAddr":"172.18.1.1","user":"--","app":"PHP","method":"GET","url":"/login?direct=1&user=bi4vmr","message":"session_start(): Failed to read session data: files (path: /var/lib/php/session) at /usr/share/nginx/html/nextcloud/lib/private/Session/Internal.php#222","userAgent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36","version":"27.0.1.2","data":{"app":"PHP"}}

# 此处省略部分输出内容...
```

从上述输出信息可知，用户登录时，PHP无法读写Session目录创建会话记录文件，因此导致登录失败。

接着我们查看Session目录的权限信息：

```text
# 查看PHP-FPM的Session目录权限
[root@Fedora ~]# ls -lh /var/lib/php
total 4.0K
drwxrwx---   2 root apache    6 Jan 16 08:00 opcache
drwxr-xr-x   2 root root    192 Feb  1 21:12 peclxml
drwxrwx---   2 root apache    6 Feb  1 22:25 session
drwxrwx---   2 root apache    6 Jan 16 08:00 wsdlcache
```

本案例中Web服务通过Nginx的专有用户"nginx"提供；由于系统升级时更新了PHP组件，它的Session目录属性被还原至"root apache"，从而导致Nginx无法在此创建会话记录文件。

### 解决方案
我们需要将Session目录的所有者与所属组重新改为Web服务提供程序的用户，以解决权限问题。

```text
# 修改Session目录的权限
[root@Fedora ~]# chown nginx:nginx /var/lib/php/session/

# 重启服务
[root@Fedora ~]# systemctl restart nginx.service php-fpm.service
```

此时客户端应当可以正常登录了；如果个别客户端仍然无法登录，我们可以尝试清空本地缓存后再次进行尝试。

## 案例二
### 问题描述
数据库增长过快，导致磁盘空间不足。

### 问题分析
首先登入数据库，执行以下SQL脚本，查询NextCloud数据库各表所占用的磁盘容量。

```sql
SELECT
    table_schema AS '数据库',
    table_name AS '表名',
    table_rows AS '记录数',
    TRUNCATE ( data_length / 1024 / 1024, 2 ) AS '数据容量(MB)',
    TRUNCATE ( index_length / 1024 / 1024, 2 ) AS '索引容量(MB)'
FROM
    information_schema.TABLES
WHERE
    table_schema = 'NextCloud' AND
    data_length > ( 1024 * 1024 ) AND
    index_length > ( 1024 * 1024 )
ORDER BY
    data_length DESC,
    index_length DESC;
```

本案例中 `oc_filecache` 表占用了很多磁盘空间，NextCloud的定时任务将会不停地扫描文件信息并缓存至该表，包括内置存储空间与外部存储设备上的文件。

### 解决方案
`oc_filecache` 表中记录的是缓存信息，我们可以直接清空其中的信息。

```sql
USE NextCloud;
TRUNCATE TABLE oc_filecache;
```
