<!--
# 简介
TODO -->

# 理论基础
# URI与URL
统一资源定位器(Uniform Resource Locator, URL)用于表示网络节点中的资源，最常见的用途就是表示Web页面，例如：我们在浏览器地址栏中输入 `https://www.bing.com/` 能够访问必应主页。除此之外，URL也可以表示其他资源，例如：SSH地址、邮箱地址等。

URL的通用语法如下文代码块所示：

```text
# 语法
<协议>://[<用户>[:<认证口令>]@][目标地址][:<端口>]/[资源路径]/[资源名称][?<查询参数>][#<路由标签>]

# 示例：使用Web访问"www.example.com"网站的"/admin"页面。
http://www.example.com/admin

# 示例：使用Web访问"www.example.com"网站中"/share"目录下的"music.zip"文件。
http://www.example.com/share/music.zip

# 示例：使用FTP访问"www.example.com"网站的"/files"目录。
ftp://www.example.com/files

# 示例：使用FTP访问"www.example.com"网站的"/files"目录，并使用特定的端口。
ftp://www.example.com:2121/files

# 示例：使用FTP访问"www.example.com"网站的"/files"目录，并设置用户名与登录口令。
ftp://user:password@www.example.com/files
```

URL语法中各个部分的含义详见下文内容：

🔷 协议类型

本部分指明了访问该资源应当使用的协议，对于HTTP、HTTPS等协议，浏览器将会显示网页或下载文件；对于浏览器不支持的协议，浏览器将会打开系统中能够处理此协议的软件，并传递地址信息，例如：当我们访问 `mailto: someone@example.com` 时，浏览器将会打开默认的邮件程序，并将 `someone@example.com` 填入收件人栏。

🔷 认证信息

本部分指明了访问者的身份信息，用户名称与认证口令之间以冒号( `:` )分隔。
对于公开的站点，该部分可以省略，例如： `https://www.bing.com/` ；对于受保护的站点，我们通常只在URL中声明用户名称，再通过其他方式进行认证，避免明文导致认证口令泄漏，例如： `ssh://root@192.168.1.1/` 。

🔷 目标地址

本部分指明了资源所在的设备地址，可以是域名或IP地址。

当我们使用URL表示本机的资源时，可以直接省略地址部分：

```text
# 示例：访问本地"/"目录下的"picture.png"文件。
file://127.0.0.1/picture.png

# 示例：访问本地"/"目录下的"picture.png"文件（省略目标地址）。
file:///picture.png
```

在上述示例中，地址格式基于Unix系统；对于Windows系统，我们需要将原始路径中的反斜杠( `\` )转换为斜杠( `/` )，例如：

```text
# 示例：访问D盘"Download"目录下的"Document.pdf"文件。
file:///d:/Download/Document.pdf
```

🔷 端口

本部分指明了访问该资源应当使用的传输层端口。

每种协议都有默认的端口，例如：HTTP使用TCP 80端口、SSH使用TCP 22端口，如果我们省略端口声明，浏览器将根据协议类型访问目标主机的对应端口。

🔷 查询参数

本部分指明了对目标资源的筛选条件等额外信息，Web服务器将会根据参数返回不同的内容。

我们可以同时输入多个参数，此时参数之间以符号 `&` 分隔，例如： `start=100&end=200` 。

🔷 路由标签

本部分指明了网页中的章节或片段，如果我们不指定路由标签，网页通常会从最顶端开始呈现；当我们指定路由标签后，网页将会自动滚动至对应的位置。

<br />

统一资源标识符(Uniform Resource Identifier, URI)是URL的超集，所有的URL都属于URI，但URI不一定符合URL标准。每种URI只在其特定的领域有意义，例如：在Android系统中， [🧭 Content URI](../../../../07_平台应用开发/01_Android/04_系统组件/03_ContentProvider/01_概述.md#uri) 用于表示应用程序数据，我们无法通过Web浏览器访问这种URI。

## MIME
多用途互联网邮件扩展(Multipurpose Internet Mail Extensions, MIME)也被称为“IANA媒体类型”，用于描述电子邮件中各部分内容的格式。相关标准在RFC 2045、RFC 2046、RFC 2047、RFC 2048、RFC 2049等文档中定义与演进。

现今MIME已经成为描述数据类型的通用标准之一，除了电子邮件之外，还被广泛应用于网络协议、Web技术、操作系统等领域。

MIME将媒体类型分为两个层级：第一级为概略类型，描述媒体内容属于文本、图像或音频等；第二级为详细类型，例如：文本中的HTML、图片中的JPEG等。

MIME的语法为： `<概略类型>/<详细类型>` ，概略类型与详细类型之间使用斜杠( `/` )作为分隔符，整个字符串中不能出现空格。MIME并不区分大小写，按照惯例我们会将所有字母小写。

MIME的常见概略类型如下文内容所示：

🔷 `text`

文本数据，通常是人类可读的文字与符号组合。

该类别中常见的详细类型如下文列表所示：

- `text/plain` : 纯文本，任何没有具体格式的文本都可以填写该值。
- `text/html` : HTML文件。
- `text/javascript` : JavaScript文件。

🔷 `image`

图像数据，包括静态图像与动态图像。

该类别中常见的详细类型如下文列表所示：

- `image/jpeg` : JPEG图像。
- `image/png` : PNG图像。
- `image/webp` : WEBP图像。
- `image/gif` : GIF图像。

🔷 `audio`

音频数据。

该类别中常见的详细类型如下文列表所示：

- `audio/wav` : WAV音频。
- `audio/mpeg` : MPEG音频。
- `audio/ogg` : OGG音频。
- `audio/webm` : WEBM音频。

🔷 `video`

视频数据。

该类别中常见的详细类型如下文列表所示：

- `video/x-msvideo` : AVI视频。
- `video/webm` : WEBM视频。

🔷 `application`

应用程序数据，包括不在前文标准类型中的文本数据、音视频数据、其他二进制数据以及程序之间约定的自定义类型。

该类别中常见的详细类型如下文列表所示：

- `application/octet-stream` : 格式未知的字节数据流。当数据格式通过其他方式另行约定时，MIME应当填写该值。
- `application/pdf` : PDF文件。
- `application/pkcs12` : SSL证书。
- `application/vnd.mspowerpoint` : Microsoft PowerPoint演示文稿。

自定义MIME类型也被称为"Vendor-Specific MIME Types"，因此非标准类型通常以"vnd"字样开头。

<br />

MIME的作用与文件扩展名类似，但它们之间并不能直接转换，例如：MP3文件所对应的MIME不是 `audio/mp3` ，应当写作 `audio/mpeg` 。MIME与常见文件格式的对应关系可参考 [🔗 Mozilla - MIME与常见文件格式的对应关系](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types) 页面。



<!-- TODO





# 认证

basic
    devops二开，用到nexusAPI ，之前get获取列表没有鉴权，新客户get也需要session，网上api资料较少，官网看到
    curl -u admin:admin123 -X GET 'http://localhost:8081/service/rest/v1/components?repository=maven-central' ; 链接后又查了 curl 转postman的地址链接；
    要添加请求头属性Authorization，value为"Basic "加 “admin:admin123” base64加密后的字符串 。注意Basic后有一个空格。



# API风格
## RPC


## RESTful



-->
