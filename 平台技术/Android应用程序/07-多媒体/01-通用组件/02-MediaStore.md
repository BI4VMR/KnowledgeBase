# 概述
MediaStore是Android Framework提供的媒体库服务，系统会在开机或存储介质挂载时自动扫描特定目录下的文件，并根据媒体类型检索元数据（例如音乐的标题、艺术家、专辑等），然后将这些数据存入SQLite数据库中，如此一来，其它程序不必各自维护媒体数据，可以直接利用系统提供的公共接口。

MediaStore包含以下几种媒体类型：

🔷 MediaStore.Images

图片文件，包括存储设备根目录下的DCIM和Pictures目录。

🔷 MediaStore.Video

视频文件，包括存储设备根目录下的DCIM、Pictures和Movies目录。

🔷 MediaStore.Audio

音频文件，包括存储设备根目录下的Music、Recordings、Alarms、Ringtones、Notifications、Podcasts目录。

🔷 MediaStore.Downloads

下载文件，包括存储设备根目录下的Download目录，此属性仅Android 10及以上系统拥有。

🔷 MediaStore.Files

若应用程序开启分区存储，此类型将包括Images、Video、Audio三种类型的文件；若应用关闭分区存储，此类型将包括存储设备的所有文件。
