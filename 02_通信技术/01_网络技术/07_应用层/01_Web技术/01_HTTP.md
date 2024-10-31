<!--
# 简介
TODO -->

HTTP1.0/HTTP1.1的区别：

    HTTP1.0对于每个连接都只能传送一个请求和响应，请求完服务器返回响应就会关闭，HTTP1.0没有Host字段。

    而HTTP1.1在同一个连接中可以传送多个请求和响应，多个请求可以重叠和同时进行，HTTP1.1必须有Host字段。



# 理论基础
## URI与URL
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

对于公开的站点，该部分可以省略，例如： `https://www.bing.com/` ；对于受保护的站点，我们通常只在URL中声明用户名称，再通过其他方式进行认证，避免明文的URL导致认证口令泄漏，例如： `ssh://root@192.168.1.1/` 。

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

## UserAgent
用户代理(User Agent, UA)是HTTP请求报文头部的一个字段，用于声明客户端与操作系统的环境信息；Web服务器可以根据UA动态返回相应的内容，以提升用户体验，例如：当我们用PC浏览器与手机浏览器访问同一网站时，服务器所返回的页面布局是不同的；当我们用Windows与Linux系统访问同一软件的下载地址时，服务器所返回的软件包格式也是不同的。

有时服务器会利用UA判断请求者是人类用户还是爬虫等自动化工具，但这种方式无法精确区分用户类型，因为自动化工具可以将UA设为常见浏览器的值，以此伪装自己。

UA的语法如下文代码块所示：

```text
# 语法
Mozilla/5.0 ([平台信息1]; [平台信息2]; [平台信息...]) [<引擎名称>/<引擎版本>] [<浏览器名称>/<浏览器版本>]

# Chrome
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36

# Firefox
Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0
Mozilla/5.0 (X11; Linux x86_64; rv:128.0) Gecko/20100101 Firefox/128.0

# Safari
Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50

# Android
Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Mobile Safari/537.36

# iPhone
Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1

# iPad
Mozilla/5.0 (iPad; CPU OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1
```

UA语法中各个部分的含义详见下文内容：

🔷 Mozilla/5.0

这是所有浏览器的通用标记，表示浏览器与Mozilla兼容。

由于历史遗留问题，不论是Mozilla Firefox、Google Chrome还是其他浏览器，它们都会将自己声明为"Mozilla/5.0"。对于主要功能并非显示网页的工具，它们不会携带该标记，只是简单的声明工具名称与版本号，例如： `qBittorrent/4.4.5.10` 、 `python-requests/2.18.4` 。

🔷 平台信息

本部分指明了客户端的操作系统与硬件架构等信息，可能包含多个子项，子项之间以分号( `;` )分隔。

🔷 引擎信息

本部分指明了客户端Web渲染引擎的相关信息。

🔷 浏览器信息

本部分指明了客户端的相关信息。

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

# 报文结构
HTTP报文由报文头部与报文体组成，报文头部包含控制信息，每个字段的长度都是可变的，末尾固定为换行符CRLF( `\r\n` )；报文体包含业务数据，报文头部与报文体之间以一个空行作为分隔符。

HTTP报文体可以根据业务需要使用任意字符编码表示，但报文头部则必须使用ASCII编码，因此若URI中包含中文等非ASCII字符，客户端必须首先使用RFC 3986中定义的“百分号编码”方式将非ASCII字符编码，才能将报文发送给服务端。

“百分号编码”的转换规则为： `%<原始数据的十六进制数值>` ，例如：中文字符串“网站”的UTF-8编码为： `E7BD91 E7AB99` ，编码后表示为： `%E7%BD%91%E7%AB%99` 。

> 🚩 提示
>
> RFC 3986只是推荐客户端以UTF-8编码的十六进制数值进行百分号编码，有些客户端可能会使用GB2312等编码进行转换，服务端应当采取一些兼容性措施，防止程序发生异常。

除了非ASCII字符之外，部分特殊符号也需要进行编码，例如：当查询参数的值中出现空格、 `&` 等符号时，就会和参数分隔符产生歧义，这些符号也应当进行编码，相关编码规则可参考 [🔗 Mozilla - 百分号编码](https://developer.mozilla.org/zh-CN/docs/Glossary/Percent-encoding) 页面。

# 报文类型
## 请求报文
客户端发起的HTTP请求报文格式如下文图片所示：

<div align="center">

![RIP报文格式](./Assets_HTTP/报文类型_HTTP请求报文格式.jpg)

</div>

上述报文中的各个字段含义详见下文内容：

🔷 请求行

该部分包含请求方法、URI、协议版本三个字段，每个字段之间以“空格”作为分隔符，各部分的含义详见下文列表：

- 请求方法：表示对资源的操作类型。通常获取资源的方法是"GET"、更新资源的方法是"POST"，但每种方法的实际动作是由HTTP服务端程序定义的，客户端应当根据服务端API文档选择所需的方法。
- URI：表示资源的路径。当我们使用浏览器请求 `http://www.example.com/share/music.zip` 时，该字段的值为 `/share/music.zip` ，并不包含主机地址，因为主机地址在请求头中另有字段表示。
- 协议版本：表示HTTP协议版本，常见的值为： `HTTP/1.1` 。

🔷 请求头

是HTTP的报文头 ，报文头包含若干个属性，格式为“属性名:属性值”，服务端据此获取客户端的信息。

Client-IP：提供了运行客户端的机器的IP地址
From：提供了客户端用户的E-mail地址
Host：给出了接收请求的服务器的主机名和端口号

Referer：提供了包含当前请求URI的文档的URL
UA-Color：提供了与客户端显示器的显示颜色有关的信息
UA-CPU：给出了客户端CPU的类型或制造商
UA-OS：给出了运行在客户端机器上的操作系统名称及版本
User-Agent：将发起请求的应用程序名称告知服务器
Accept：告诉服务器能够发送哪些媒体类型
Accept-Charset：告诉服务器能够发送哪些字符集
Accept-Encoding：告诉服务器能够发送哪些编码方式
Accept-Language：告诉服务器能够发送哪些语言
TE：告诉服务器可以使用那些扩展传输编码
Expect：允许客户端列出某请求所要求的服务器行为
Range：如果服务器支持范围请求，就请求资源的指定范围
Cookie：客户端用它向服务器传送数据
Cookie2：用来说明请求端支持的cookie版本

🔷 请求体

客户端需要发送给服务器的消息内容，该部分与请求头之间以一个空行作为分界符，并且可以为空。



## 响应报文


服务端发送给客户端的HTTP响应报文格式如下文图片所示：

<div align="center">

![RIP报文格式](./Assets_HTTP/报文类型_HTTP响应报文格式.jpg)

</div>

上述报文中的各个字段含义详见下文内容：

🔷 响应行

该部分包含协议版本、状态码、状态描述三个字段，每个字段之间以“空格”作为分隔符，各部分的含义详见下文列表：

- 协议版本：表示HTTP协议版本，常见的值为： `HTTP/1.1` 。
- 状态码：表示服务器对请求的处理结果，最常见的值为“200(OK)”，意为请求处理成功。
- 状态描述：对状态码的详细文本描述。

🔷 响应头

响应报文头，也是由多个属性组成；

Age：(从最初创建开始)响应持续时间
Public：服务器为其资源支持的请求方法列表
Retry-After：如果资源不可用的话，在此日期或时间重试
Server：服务器应用程序软件的名称和版本
Title：对HTML文档来说，就是HTML文档的源端给出的标题
Warning：比原因短语更详细一些的警告报文
Accept-Ranges：对此资源来说，服务器可接受的范围类型
Vary：服务器会根据这些首部的内容挑选出最适合的资源版本发送给客户端
Proxy-Authenticate：来自代理的对客户端的质询列表
Set-Cookie：在客户端设置数据，以便服务器对客户端进行标识
Set-Cookie2：与Set-Cookie类似
WWW-Authenticate：来自服务器的对客户端的质询列表

🔷 响应体

服务器需要反馈给客户端的消息内容，该部分与响应头之间以一个空行作为分界符，并且可以为空。






<!-- TODO



# 方法



GET和POST的区别：

    （1）get是从服务器上获取数据（即下载），post是向服务器传送数据（即上传）。

    （2）生成方式不同：

    Get：URL输入；超连接；Form表单中method属性为get；Form表单中method为空。

    Post只有一种：Form表单中method为Post。

    （3）数据传送方式：Get传递的请求数据按照key-value的方式放在URL后面，在网址中可以直接看到，使用?分割URL和传输数据，传输的参数之间以&相连，如：login.action?name=user&password=123。所以安全性差。

    POST方法会把请求的参数放到请求头部和空格下面的请求数据字段就是请求正文（请求体）中以&分隔各个字段，请求行不包含参数，URL中不会额外附带参数。所以安全性高。

    （3）发送数据大小的限制：通常GET请求可以用于获取轻量级的数据，而POST请求的内容数据量比较庞大些。

    Get：1~2KB。get方法提交数据的大小直接影响到了URL的长度，但HTTP协议规范中其实是没有对URL限制长度的，限制URL长度的是客户端或服务器的支持的不同所影响。

    Post：没有要求。post方式HTTP协议规范中也没有限定，起限制作用的是服务器的处理程序的能力。

    （4）提交数据的安全：POST比GET方式的安全性要高。Get安全性差，Post安全性高。

    通过GET提交数据，用户名和密码将明文出现在URL上，如果登录页面有浏览器缓存，或者其他人查看浏览器的历史记录，那么就可以拿到用户的账号和密码了。安全性将会很差。


# cookie和session

cookie和session配合方式，客户端使用浏览器的cookie来存sessionId，服务端存session。由于结构可以泛化成两种：
（1）单服务器的服务端，这种多在小型服务中，私有化部署，toB项目的服务中
（2）分布式架构下的服务端，这种多是toC，大型服务，复杂系统，这种方式需要共享session，多个服务器都能访问，这样对于共享session的方式，可以是同步session，也可以是单独的服务做认证，类似redis集群存放认证信息，这种类似与网关的服务。就是说
 



# 数据存储
## Cookie



Cookie是什么

        cookie的中文翻译是曲奇，小甜饼的意思。cookie其实就是一些数据信息，类型为“小型文本文件”，存储于电脑上的文本文件中。
        Cookie最早由网景公司设计并运用到Web通讯中，后被作为规范纳入到RFC2965中。
Cookie有什么用

        我们想象一个场景，当我们打开一个网站时，如果这个网站我们曾经登录过，那么当我们再次打开网站时，发现就不需要再次登录了，而是直接进入了首页。例如bilibili，csdn等网站。

        这是怎么做到的呢？其实就是游览器保存了我们的cookie，里面记录了一些信息，当然，这些cookie是服务器创建后返回给游览器的。游览器只进行了保存。下面展示bilibili网站保存的cookie。



Cookie的属性

通常情况下，Cookie会包含如下信息：name expires domain path secure

    name:cookie 的名字

    expires:过期时间。值是一个日期，一个时刻，而不是一个时长。在OkHttp中，你可以使用该字段在端上建立逻辑，也可以忽略该字段依靠server实现过期的逻辑。

    domain：cookie的作用域，指定了cookie将要被发送至哪个域中。默认情况下，domain会被设置为创建该cookie的url所在的域名。但在OkHttp中默认是不存在Cookie机制的，因此这一点需要你来亲自实现完善。像百度这样的网站，会有很多name.baidu.com形式的站点，他们的顶级域名是一致的，但二级域名会有很多，比如waimai.baidu.com，bzclk.baidu.com等。domain的匹配通常是从域名的末尾开始匹配，并将命中的cookie作为有效cookie存储。

    path:另一个控制cookie的发送时机的选项。类似于domain，path选项要求请求资源URL中必须存在指定的路径，才会发送cookie。通常是将path的值与请求的URL从开头开始逐个字符串比较完成匹配。如：Set-Cookie：name=Ghost;path=/ghost就要求URL的路径以/ghost开头，如/ghost,/ghostinmatrix都是命中的url。

需要注意的是：cookie匹配验证的顺序首先是domain，然后才会匹配path。

    secure：该选项只是一给标记而没有值。只有当一个请求通过SSL或者HTTPS创建的时候，包含secure的cookie才能被发送至服务器。这种cookie内容具有很高价值，如果一纯文本形式传递很有可能被篡改。事实上，机密且敏感的数据是不应该再cookie中存储的，因为cookie整个机制本身就是不安全的。



# 认证


    devops二开，用到nexusAPI ，之前get获取列表没有鉴权，新客户get也需要session，网上api资料较少，官网看到
    curl -u admin:admin123 -X GET 'http://localhost:8081/service/rest/v1/components?repository=maven-central' ; 链接后又查了 curl 转postman的地址链接；
    要添加请求头属性Authorization，value为"Basic "加 “admin:admin123” base64加密后的字符串 。注意Basic后有一个空格。



## Basic




1.客户端向服务器请求数据；
2.服务器认为没有通过认证，向客户端发送401 （WWW-Authenticate: Basic realm=“XXXXXX”）；
3.客户端将自动弹出一个登录窗口，要求用户输入用户名和密码；
4.用户输入用户名和密码后，客户端将用户名及密码以BASE64编码加密，发请求（Authorization: Basic xxxxxxxxx）；
5.服务器收到上述请求信息后，将Authorization字段后的用户信息取出、解密，将解密后的用户名及密码与用户数据库进行比较验证；





# API风格
## RPC


## RESTful





# 附录
## HTTP响应报文的状态码

状态码告知从服务器端返回的请求的状态，一般由一个三位数组成,分别以整数1～5开头组成。各个响应的类型对应的含义:

常见状态码：

1XX 请求正在处理

2XX 请求成功 200 OK 正常处理 204 no content 请求处理成功但没有资源可返回 206 Partial Content 对资源的某一部分请求

3XX 重定向 301 Moved Permanenly请求资源的URI已经更新（永久移动），客户端会同步更新URI。

302 Found 资源的URI已临时定位到其他位置，客户端不会更新URI。

303 See Other 资源的URI已更新，明确表示客户端要使用GET方法获取资源。

304 Not Modified 当客户端附带条件请求访问资源时资源已找到但未符合条件请求。

307 Temporary Redirect临时重定向

4XX 客户端错误 400 Bad Request 请求报文中存在语法错误，一般为参数异常。401 Unauthorized 发送的请求需要HTTP认证。

403 Forbiddden 不允许访问，对请求资源的访问被服务器拒绝

404 Not Found 无法找到请求的资源，请求资源不存在。

405 请求的方式不支持。

5XX 服务器错误 500 Internal Server Error 服务器的内部资源出故障，服务器在执行请求时发生了错误。

503 Service Unavailable 服务器暂时处于超负载状态或正在进行停机维护，无法处理请求，服务器正忙。


HTTP的状态响应码
1**：请求收到，继续处理

100——客户必须继续发出请求

101——客户要求服务器根据请求转换HTTP协议版本
2**：操作成功收到，分析、接受

200——交易成功

201——提示知道新文件的URL

202——接受和处理、但处理未完成

203——返回信息不确定或不完整

204——请求收到，但返回信息为空

205——服务器完成了请求，用户代理必须复位当前已经浏览过的文件

206——服务器已经完成了部分用户的GET请求
3**：完成此请求必须进一步处理

300——请求的资源可在多处得到

301——删除请求数据

302——在其他地址发现了请求数据

303——建议客户访问其他URL或访问方式

304——客户端已经执行了GET，但文件未变化

305——请求的资源必须从服务器指定的地址得到

306——前一版本HTTP中使用的代码，现行版本中不再使用

307——申明请求的资源临时性删除
4**：请求包含一个错误语法或不能完成

400——错误请求，如语法错误

401——未授权

HTTP 401.1 - 未授权：登录失败

HTTP 401.2 - 未授权：服务器配置问题导致登录失败

HTTP 401.3 - ACL 禁止访问资源

HTTP 401.4 - 未授权：授权被筛选器拒绝

HTTP 401.5 - 未授权：ISAPI 或 CGI 授权失败

402——保留有效ChargeTo头响应

403——禁止访问

HTTP 403.1 禁止访问：禁止可执行访问

HTTP 403.2 - 禁止访问：禁止读访问

HTTP 403.3 - 禁止访问：禁止写访问

HTTP 403.4 - 禁止访问：要求 SSL

HTTP 403.5 - 禁止访问：要求 SSL 128

HTTP 403.6 - 禁止访问：IP 地址被拒绝

HTTP 403.7 - 禁止访问：要求客户证书

HTTP 403.8 - 禁止访问：禁止站点访问

HTTP 403.9 - 禁止访问：连接的用户过多

HTTP 403.10 - 禁止访问：配置无效

HTTP 403.11 - 禁止访问：密码更改

HTTP 403.12 - 禁止访问：映射器拒绝访问

HTTP 403.13 - 禁止访问：客户证书已被吊销

HTTP 403.15 - 禁止访问：客户访问许可过多

HTTP 403.16 - 禁止访问：客户证书不可信或者无效

HTTP 403.17 - 禁止访问：客户证书已经到期或者尚未生效

404——没有发现文件、查询或URl

405——用户在Request-Line字段定义的方法不允许

406——根据用户发送的Accept拖，请求资源不可访问

407——类似401，用户必须首先在代理服务器上得到授权

408——客户端没有在用户指定的饿时间内完成请求

409——对当前资源状态，请求不能完成

410——服务器上不再有此资源且无进一步的参考地址

411——服务器拒绝用户定义的Content-Length属性请求

412——一个或多个请求头字段在当前请求中错误

413——请求的资源大于服务器允许的大小

414——请求的资源URL长于服务器允许的长度

415——请求资源不支持请求项目格式

416——请求中包含Range请求头字段，在当前请求资源范围内没有range指示值，请求也不包含If-Range请求头字段

417——服务器不满足请求Expect头字段指定的期望值，如果是代理服务器，可能是下一级服务器不能满足请求长。
5**：服务器执行一个完全有效请求失败

HTTP 500 - 内部服务器错误

HTTP 500.100 - 内部服务器错误 - ASP 错误

HTTP 500-11 服务器关闭

HTTP 500-12 应用程序重新启动

HTTP 500-13 - 服务器太忙

HTTP 500-14 - 应用程序无效

HTTP 500-15 - 不允许请求 global.asa

Error 501 - 未实现

HTTP 502 - 网关错误



-->
