<!-- TODO
# 简介


HTTP1.0/HTTP1.1的区别：

    HTTP1.0对于每个连接都只能传送一个请求和响应，请求完服务器返回响应就会关闭，HTTP1.0没有Host字段。

    而HTTP1.1在同一个连接中可以传送多个请求和响应，多个请求可以重叠和同时进行，HTTP1.1必须有Host字段。

-->


# 理论基础
## UserAgent
用户代理(User Agent, UA)是HTTP请求报文头部的一个字段，用于声明客户端与操作系统的环境信息；Web服务器可以根据UA动态返回相应的内容，以提升用户体验，例如：当我们用PC浏览器与手机浏览器访问同一网站时，服务器所返回的页面布局是不同的；当我们用Windows与Linux系统访问同一软件的下载地址时，服务器所返回的软件包格式也是不同的。

有时服务器会利用UA判断请求者是人类用户还是爬虫等自动化工具，但这种方式无法精确区分用户类型，因为自动化工具可以将UA设为常见浏览器的值，以此伪装为人类。

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


# 报文结构
HTTP报文由报文头部与报文体组成，报文头部包含控制信息，每个字段的长度都是可变的，末尾固定为换行符CRLF( `\r\n` )；报文体包含业务数据，报文头部与报文体之间以一个空行作为分隔符。

HTTP报文体可以根据业务需要使用任意字符编码表示，但报文头部则必须使用ASCII编码，因此若URI中包含中文等非ASCII字符，客户端必须首先使用RFC 3986中定义的“百分号编码”方式将非ASCII字符编码，才能将报文发送给服务端。

“百分号编码”的转换规则为： `%<原始数据的十六进制数值>` ，例如：中文字符串“网站”的UTF-8编码为： `E7BD91 E7AB99` ，经过“百分号编码”后表示为： `%E7%BD%91%E7%AB%99` 。

> 🚩 提示
>
> RFC 3986只是推荐客户端以UTF-8编码的十六进制数值进行百分号编码，有些客户端可能会使用GB2312等编码进行转换，服务端应当采取一些兼容性措施，防止程序发生异常。

除了非ASCII字符之外，部分特殊符号也需要进行编码，例如：当查询参数的值中出现空格、 `&` 等符号时，就会和参数分隔符产生歧义，这些符号也应当进行编码，相关编码规则可参考 [🔗 Mozilla - 百分号编码](https://developer.mozilla.org/zh-CN/docs/Glossary/Percent-encoding) 页面。


# 报文类型
## 请求报文
客户端发起的HTTP请求报文格式如下文图片所示：

<div align="center">

![HTTP请求报文格式](./Assets_HTTP/报文类型_HTTP请求报文格式.jpg)

</div>

上述报文中的各个字段含义详见下文内容：

🔷 请求行

该部分包含请求方法、URI、协议版本三个字段，每个字段之间以“空格”作为分隔符，各部分的含义详见下文列表：

- 请求方法：表示对资源的操作类型。通常获取资源的方法是"GET"、更新资源的方法是"POST"，但每种方法的实际动作是由HTTP服务端程序定义的，客户端应当根据服务端API文档选择所需的方法。
- URI：表示资源的路径。当我们使用浏览器请求 `http://www.example.com/share/music.zip` 时，该字段的值为 `/share/music.zip` ，并不包含主机地址，因为主机地址在请求头中另有字段表示。
- 协议版本：表示HTTP协议版本，常见的值为： `HTTP/1.1` 。

🔷 请求头
<!-- TODO
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
-->

🔷 请求体

客户端发送给服务器的消息内容，该部分与请求头之间以一个空行作为分界符，并且可以为空。

## 响应报文
服务端发送给客户端的HTTP响应报文格式如下文图片所示：

<div align="center">

![HTTP响应报文格式](./Assets_HTTP/报文类型_HTTP响应报文格式.jpg)

</div>

上述报文中的各个字段含义详见下文内容：

🔷 响应行

该部分包含协议版本、状态码、状态描述三个字段，每个字段之间以“空格”作为分隔符，各部分的含义详见下文列表：

- 协议版本：表示HTTP协议版本，常见的值为： `HTTP/1.1` 。
- 状态码：表示服务器对请求的处理结果，最常见的值为“200(OK)”，意为请求处理成功。
- 状态描述：对状态码的详细文本描述，常见的状态码及描述信息可参考后文章节： [🧭 附录：响应报文的状态码](#响应报文的状态码) 。

> 🚩 提示
>
> HTTP状态码仅用于表示协议本身的工作状态，常见取值的含义是固定的，我们在设计服务端程序时，不应随意填写HTTP状态码以表示业务状态，应当在响应体中定义字段表示业务状态。

🔷 响应头

<!-- TODO
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
-->


🔷 响应体

服务器反馈给客户端的消息内容，该部分与响应头之间以一个空行作为分界符，并且可以为空。


# 请求方法

<div align="center">

|  方法   |         说明         |         HTTP版本          |
| :-----: | :------------------: | :-----------------------: |
|   GET   |       获取资源       |            1.0            |
|  POST   |       提交资源       |            1.0            |
|  HEAD   |     请求报文头部     |            1.0            |
|   PUT   |       提交资源       |            1.1            |
| DELETE  |       删除资源       |            1.1            |
| OPTIONS | 查询服务器支持的方法 |            1.1            |
|  TRACE  |       追踪路径       |            1.1            |
| CONNECT |     建立隧道连接     |            1.1            |
|  LINK   |    与资源建立联系    | 1.0（更高版本中已被弃用） |
| UNLINK  |   断开与资源的联系   | 1.0（更高版本中已被弃用） |

</div>

<!-- TODO

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



Get

简单点说：GET可以说是最常见的了,它本质就是发送一个请求来取得服务器上的某一资源。资源通过一组HTP头和呈现数据(如HTML文本,或者图片或者视频等)返回给客户端。GET请求中,永远不会包含呈现数据。
 
复杂点说：GET方法用来请求访问已被URI 识别的资源。指定的资源经服务器端解析后返回响应内容。也就是说，如果请求的资源是文本，那就保持原样返回；如果是像CGI（Common Gateway Interface，通用网关接口）那样的程序，则返回经过执行后的输出结果。


POST

简单点说：POST 方法用来传输实体的主体。


复杂点说：虽然用GET 方法也可以传输实体的主体，但一般不用GET 方法进行传输，而是用POST 方法。虽说POST 的功能与GET 很相似，但POST 的主要目的并不是获取响应的主体内容。POST方法期初是用来向服务器输入数据的，实际上，通常会用它来支持HTML的表单。表单中填好的数据通常会被送给服务器，然后由服务器将其发送到它要去的地方。


PUT

简单点说：PUT:这个方法比较少见。HTML表单也不支持这个。本质上来讲,PUT和POST极为相似,都是向服务器发送数据,但它们之间有一个重要区别,PUT通常指定了资源的存放位置,而POST则没有POST的数据存放位置由服务器自己决定。


举个例子:如一个用于提交博文的URL,/addBlog。如果用PUT,则提交的URL会是像这样的/addBlogabc123,其中abc123就是这个博文的地址。而如果用POST,则这个地址会在提交后由服务器告知客户端。目前大部分博客都是这样的。显然,PUT和POST用途是不一样的。具体用哪个还取决于当前的业务场景。
 
复杂点说：PUT 方法用来传输文件。就像FTP 协议的文件上传一样，要求在请求报文的主体中包含文件内容，然后保存到请求URI 指定的位置。但是，鉴于HTTP/1.1 的PUT 方法自身不带验证机制，任何人都可以上传文件, 存在安全性问题，因此一般的Web 网站不使用该方法。若配合Web 应用程序的验证机制，或架构设计采用REST（REpresentational State Transfer，表征状态转移）标准的同类Web 网站，就可能会开放使用PUT 方法。


HEAD

简单点说：HEAD 方法和 GET 方法一样，只是不返回报文主体部分。用于确认URI 的有效性及资源更新的日期时间等。
 
复杂点说：HEAD和GET本质是一样的,区别在于HEAD不含有呈现数据,而仅仅是HTTP头信息。有的人可能觉得这个方法没什么用,其实不是这样的。想象一个业务情景:欲判断某个资源是否存在,我们通常使用GET,但这里用HEAD则意义更加明确。


DELETE

简单点说：删除某一个资源。基本上这个也很少见,不过还是有一些地方比如amazon的S3云服务里面就用的这个方法来删除资源。
 
复杂点说：DELETE方法用来删除文件，是与PUT相反的方法。DELETE方法按请求URI 删除指定的资源。但是，HTTP/1.1 的 DELETE 方法本身和PUT 方法一样不带验证机制，所以一般的Web 网站也不使用DELETE 方法。当配合Web 应用程序的验证机制，或遵守REST 标准时还是有可能会开放使用的。


OPTIONS

简单点说：OPTIONS方法用来查询针对请求 URI 指定的资源支持的方法。
 
复杂点说：OPTIONS:这个方法很有趣,但极少使用。它用于获取当前URL所支持的方法。若请求成功,则它会在HTTP头中包含一个名为Atow的头,值是所支持的方法,如GET，POST。


TRACE



简单点说：TRACE请求服务器回送收到的请求信息,主要用于测试和诊断,所以是安全的。
 
 
复杂点说：TRACE方法是让Web 服务器端将之前的请求通信环回给客户端的方法。


发送请求时，在Max-Forwards 首部字段中填入数值，每经过一个服务器端就将该数字减1，当数值刚好减到0 时，就停止继续传输，最后接收到请求的服务器端则返回状态码200 OK 的响应。


客户端通过TRACE方法可以查询发送出去的请求是怎样被加工修改/ 篡改的。这是因为，请求想要连接到源目标服务器可能会通过代理 中转，TRACE 方法就是用来确认连接过程中发生的一系列操作。
但是，TRACE方法本来就不怎么常用，再加上它容易引发
XST（Cross-Site Tracing，跨站追踪）攻击，通常就更不会用到了。



CONNECT

CONNECT这个方法的作用就是把服务器作为跳板，让服务器代替用户去访问其它网页，之后把数据原原本本的返回给用户。


复杂点说：CONNECT 方法要求在与代理服务器通信时建立隧道，实现用隧道协议进行TCP 通信。主要使用SSL（Secure Sockets Layer，安全套接层）和TLS（Transport Layer Security，传输层安全）协议把通信内容加密后经网络隧道传输。


CONNECT 方法的格式如下所示。


CONNECT 代理服务器名:端口号HTTP版本

-->

# 数据存储
## Cookie
### 简介
HTTP协议是无状态的，这意味着服务端无法判断多个请求是否来自同一个客户端，为了解决此问题，Netscape公司首先提出了Cookie技术方案，该方案后来被IETF标准化，相关标准在RFC 2109、RFC 2965等文档中定义与演进。

Cookie是一种轻量级键值对数据，通常由服务端下发给客户端，客户端需要将它们保存在本地，后续请求同一URI时，再次携带这些数据。

Cookie技术被广泛应用于用户登录状态保持、用户偏好记忆等功能，例如：当我们登录某个网站之后，服务端可能会通过Cookie发送会话标识符，此时我们可以关闭浏览器；后续我们再次访问该网站时，会话标识符将被自动发送至服务端，服务端通过此标识符即可识别请求来源，避免反复要求用户登录，改善用户体验。

Cookie仅能用于存取少量数据，客户端会忽略超过数量限制的Cookie。对于每个域名，所有键值对的总长度限制通常为4096字节，不同客户端对Cookie数量的限制如下文列表所示：

- Firefox：每个域名最多保存50条Cookie。
- Chrome：每个域名最多保存53条Cookie。
- Opera：每个域名最多保存30条Cookie。
- Internet Explorer：每个域名最多保存50条Cookie。

### 工作流程
Cookie的下发与上报流程如下文图片所示：

<div align="center">

![Cookie技术的工作流程](./Assets_HTTP/数据存储_Cookie技术的工作流程.jpg)

</div>

上述流程图中的步骤详情如下文内容所示：

🔷 客户端首次发送请求报文

此时客户端还没有Cookie信息，因此请求报文中也不会携带任何Cookie数据。

🔷 服务端首次发送响应报文

服务端希望客户端保存一些Cookie数据，因此在响应头中使用 `Set-Cookie` 属性下发数据，格式示例详见下文图片：

<div align="center">

![服务端下发Cookie数据](./Assets_HTTP/数据存储_服务端下发Cookie数据.jpg)

</div>

每条 `Set-Cookie` 属性的开头是Cookie的键名与键值，二者以等号( `=` )作为分隔符，以分号( `;` )作为结束符；后续的内容为该Cookie的属性，例如：作用域、有效时间等。

如果服务器需要一次性下发多条Cookie数据，则HTTP报文中会包含多条 `Set-Cookie` 属性。

🔷 客户端再次发送请求报文

当客户端请求某个URI时，其本地已有关联到该URI的若干Cookie数据，此时客户端会在请求报文中携带这些数据，格式示例详见下文图片：

<div align="center">

![客户端发送携带Cookie的请求报文](./Assets_HTTP/数据存储_客户端发送携带Cookie的请求报文.jpg)

</div>

请求头属性 `Cookie` 用于表示客户端拥有的Cookie数据，每个键值对以分号( `;` )作为分隔符。

### 属性
除了键名与键值以外，Cookie还有一些属性，我们可以在浏览器的控制台中查看它们，样例详见下文图片：

<div align="center">

![Cookie在浏览器中的形态](./Assets_HTTP/数据存储_Cookie在浏览器中的形态.jpg)

</div>

各个属性的作用如下文内容所示：

🔷 域名(Domain)

域名属性是决定是否发送某条Cookie的先决条件，默认情况下该属性的值即当前URI中的域名。

域名属性能够匹配当前域名与子域名，例如：域名属性为 `example.com` 的Cookie能够匹配 `www.example.com` 与 `news.example.com` 等。

🔷 路径(Path)

路径属性是决定是否发送某条Cookie的次要条件，仅当域名属性已确认匹配后，客户端才会尝试匹配路径。

路径属性的匹配规则为前缀匹配，例如：路径属性为 `/admin` 的Cookie能够匹配 `/admin` 与 `/admin/login` 等。

🔷 有效期

Cookie的到期时刻。

以客户端时间为准，在该时刻到达之前，客户端请求报文均会携带该Cookie。如果服务器下发Cookie时没有指明有效期，则表示该Cookie仅对当前会话生效，客户端不会将其写入磁盘，会话结束后Cookie将被立刻销毁。

🔷 Secure

安全协议标志位。

该属性没有具体的值，当Cookie具有该属性时，仅当连接基于HTTPS协议，客户端才会随请求发送本Cookie；当Cookie没有该属性时，连接基于HTTP或HTTPS协议，客户端都会随请求发送本Cookie。

🔷 HttpOnly

仅限HTTP访问标志位。

该属性没有具体的值，当Cookie具有该属性时，不允许JavaScript等脚本访问本Cookie；当Cookie没有该属性时，则没有访问限制。该属性通常用于保护用户认证Token等关键信息，避免XSS等恶意攻击获取敏感信息。


# 认证
## Basic认证
### 简介
Basic认证是一种简单的认证方式，客户端将用户ID与认证口令直接发送至服务器，服务器通过对比自身存储的用户信息与客户端发送的用户信息是否相同，从而判断是否为已授权用户。

Basic认证是一种静态认证机制，用户一旦登录成功，服务端无法迫使特定的客户端登出；由于用户信息未经加密、可被放置在URL中，容易通过HTTP明文传输、用户在公共网站发布URL等途径泄漏。

这种方式安全性低、不支持跨域、可扩展性差，通常用于仅对局域网内部开放的应用，例如：文件共享服务、Tomcat控制台等。

### 工作流程
Basic认证的工作流程如下文图片所示：

<div align="center">

![Basic认证的工作流程](./Assets_HTTP/认证_Basic认证的工作流程.jpg)

</div>

上述流程图中的步骤详情如下文内容所示：

🔷 客户端首次发送请求报文

此时报文中并未携带认证信息。

🔷 服务端首次发送响应报文

服务端尝试校验请求报文中的认证信息，由于此时并没有认证信息，故向客户端发送"401 Unauthorized"报文。

<div align="center">

![服务端发送401报文](./Assets_HTTP/认证_服务端发送401报文.jpg)

</div>

响应头中的 `WWW-Authenticate` 属性指明了服务端所要求的认证参数，其中"Basic"表示认证方式为Basic认证；"realm"表示站点的相关描述。

🔷 客户端再次发送请求报文

客户端收到401报文后已知晓需要使用Basic认证，此时将会弹出对话框，用户可以输入ID与认证口令。

当用户输入完毕点击确认按钮后，浏览器将会再次发送请求报文，并在请求头中携带认证信息。

<div align="center">

![客户端发送携带认证信息的报文](./Assets_HTTP/认证_客户端发送携带认证信息的报文.jpg)

</div>

请求头中的 `Authorization` 属性表示认证信息，其中"Basic"表示认证方式为Basic认证；"Basic"之后有一个空格作为分隔符，随后直到行尾的内容为用户ID与登录口令经过编码得到的文本。

用户信息的编码规则为： `<用户ID>:<登录口令>` 字符串使用Base64算法进行编码，例如： `admin:Qwer1234.` 经过编码后的文本为： `YWRtaW46UXdlcjEyMzQu` 。

🔷 服务端再次发送响应报文

服务端收到携带认证信息的请求报文后，将会读取 `Authorization` 属性，并使用Base64算法进行解码，查找数据库中是否有此用户ID、认证口令是否与客户端提交的值一致。

当用户身份验证通过后，服务端向客户端发送响应报文，其中包含URL对应的资源；若用户提供的身份信息不正确，则服务端向客户端发送"403 Forbiddden"等响应报文，向用户展示错误详情。

<br />

在上述示例中，客户端是具有GUI的普通浏览器，能够自动弹出用户信息对话框引导用户完成认证；如果客户端是OkHttp等无GUI的工具库，开发者需要自行实现对话框，并将用户输入的信息传递给工具库，部分工具库支持在URL中携带认证信息，另一部分工具库则支持通过特定的API进行设置，具体的传递方式视工具库的说明文档而定。

## Session
Session是Web技术发展早期就已经出现的认证技术，用户登录成功后，由服务端记录登录状态。

Session认证的工作流程如下文图片所示：

<div align="center">

![客户端发送携带认证信息的报文](./Assets_HTTP/认证_Session认证的工作流程.jpg)

</div>

客户端想要访问某些受保护的资源，服务端发现请求报文未携带Session ID，于是返回401报文要求登录。

客户端跳转至登录界面，用户输入身份信息并确认；服务端在数据库中查找用户信息并比对登录口令，匹配无误后生成一个随机数作为Session ID，将其记录在内存中并返回给客户端。

后续客户端访问服务器时，请求报文均携带Session ID；服务端收到请求后检查内存中是否有该Session ID，记录存在则认为登录状态有效，否则认为登录状态过期失效。

> 🚩 提示
>
> Session是一种设计思想，而不是具体的实现标准。
>
> 客户端既可以使用Basic认证发送身份信息，也可以使用自定义JSON报文发送身份信息；即可以利用Cookie自动存储与发送Session ID，也可以由JavaScript控制LocalStorage存储Session ID以及自定义发送逻辑。
>
> 服务端既可以在应用程序内部简单地使用集合记录Session ID，也可以借助Redis等工具存储Session ID。



<!-- TODO

基于Token的认证

基于令牌的认证已经变得更加普遍最近随着RESTful API的应用，单页应用程序和微服务的兴起。
什么是token？


基于 token 的用户认证是一种服务端无状态的认证方式，服务端不用存放 token 数据。用解析 token 的计算时间换取 session 的存储空间，从而减轻服务器的压力，减少频繁的查询数据库

token 完全由应用管理，所以它可以避开同源策略


token是一小块数据。

利用基于Token的认证的认证系统意味着用户向服务器发出的请求携带token以执行认证逻辑。当发出HTTP请求时， token是验证用户是否有资格访问资源的凭证。
这与基于Cookie的身份验证有何不同？

token认证是无状态的，而基于session的认证意味着在您的服务器（或在Redis等）中的某个地方保存着状态用以识别用户。

Auth0的博客文章Cookies vs Tokens：The Definitive 描述了cookie和令牌之间的身份验证流程的差别的：
基于session的认证流程：

1. 用户输入其登录信息
2. 服务器验证信息是否正确，并创建一个session，然后将其存储在数据库中
3. 具有sessionID的Cookie将放置在用户浏览器中
4. 在后续请求中，会根据数据库验证sessionID，如果有效，则接受请求
5. 一旦用户注销应用程序，会话将在客户端和服务器端都被销毁

基于令牌的认证流程：

1. 用户输入其登录信息
2. 服务器验证信息是否正确，并返回已签名的token
3. token储在客户端，最常见的是存储在`local storage`中，但也可以存储在session存储或cookie中
4. 之后的HTTP请求都将token添加到请求头里
5. 服务器解码JWT，并且如果令牌有效，则接受请求
6. 一旦用户注销，令牌将在客户端被销毁，不需要与服务器进行交互一个关键是，令牌是无状态的。后端服务器不需要保存令牌或当前session的记录。

哇标记听起来很酷。他们比基于session的身份验证更好吗？

问错人了，伙计。我只是告诉你存在这个验证方式。我不会比较没有意义的比较，我尽最大努力做到这一点。有关更多有趣的免责声明，请访问上面的免责声明部分。令牌的类型一些常见的令牌包括JWT（下面讨论），SWT（简单网络令牌）和SAML（安全断言标记语言）
链接

    Token Based Authentication - Implemenation Demonstration - W3

    What is token based Authentication - SO

    Token Based Authentication Made Easy

    The Ins and Outs of Token Based Authentication

    Cookies vs Tokens: The Definitive Guide (opinionated)

JWT

JWT代表“JSON Web Token”。 JWT是一种基于Token的认证。 JWT基于Web标准。现在JWT用的越来越多;JWT是Token认证的一种，所以说JWT基于Token的认证。再次，基于Token的认证的不同方法具有不同的优点和缺点。因此，上面的基于令牌的认证部分中的很多信息适用于此。

来自JWT RFC 7519标准化的摘要说明：JSON Web Token（JWT）是一种紧凑的，URL安全的方式，表示要在双方之间传输的声明。

JSON Web令牌是一个字符串。它可能看起来像这样：

    eyJhbGciOIJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ

上面的字符串是你在使用JWT执行身份验证时可能看到的;它是在认证时从服务器返回的凭证。JWT经过验证并且安全，因为它们使用私钥进行“数字签名”，并使用密钥进行身份验证。
JWT的结构？

JWT是一个自包含的数据块。每个JWT由payload和signature组成。当您的服务器创建token时，您还可以为token分配唯一的数据，可以在前端使用。这可以用于保存稍后进行其他数据库调用的需要。你仍然应该警惕在发送给客户的令牌中发布机密信息(比如说用户密码等等)。
在Python中创建JWT令牌的示例函数：

def create_token(user):
    """Create a JWT token, set expiry, iat, etc"""
    payload = {
        'sub': user.id,
        'name': user.first_name,
        'role_id': user.role_id,
        'iat': datetime.utcnow(),
        'exp': datetime.utcnow() + timedelta(days=1)
    }
    token = jwt.encode(payload, MY_SECRET_KEY_SHHH_DONT_TELL_ANYONE, algorithm='HS256')
    return token.decode('unicode_escape')

上面的键sub，iat和exp跟随保留的JWT键，但我也添加了用户的名称和role_id。你将需要一个库来编码/解码JWT令牌。 JWT.io列出了许多语言的库。





# API风格
## RPC


## RESTful


-->



# 附录
## 响应报文的状态码
下文内容展示了常见的状态码及其含义。

🔷 `1XX` : 服务器接受请求，客户端应当进一步执行操作。

- `100(Continue)` : 继续。客户端应继续其请求。
- `101(Switching Protocols)` :  切换协议。服务器根据客户端的请求切换协议。只能切换到更高级的协议，例如，切换到HTTP的新版本协议

🔷 服务器接受请求，已经操作完毕。(2XX)

- `200(OK)` : 请求已被正确处理。OK 请求成功。一般用于GET与POST请求
- `201(Created)` : 提示知道新文件的URL。已创建。成功请求并创建了新的资源
- `202` : 接受和处理、但处理未完成 Accepted 已接受。已经接受请求，但未处理完成
- `203` : Non-Authoritative Information 非授权信息。请求成功。但返回的meta信息不在原始的服务器，而是一个副本。

- `204(No Content)` : 无内容。服务器成功处理，但未返回内容。在未更新网页的情况下，可确保浏览器继续显示当前文档  请求处理成功但没有资源可返回
- `205(Reset Content)` : 重置内容。服务器处理成功，用户终端（例如：浏览器）应重置文档视图。可通过此返回码清除浏览器的表单域  服务器完成了请求，用户代理必须复位当前已经浏览过的文件
- `206(Partial Content)` : 服务器已经完成了部分用户的GET请求 部分内容。服务器成功处理了部分GET请求





🔷 服务器接受请求，客户端应当进一步操作。(3XX)


- `300(Multiple Choices)` : 多种选择。请求的资源可包括多个位置，相应可返回一个资源特征与地址的列表用于用户终端（例如：浏览器）选择
- `301(Moved Permanently)` : 永久移动。请求的资源已被永久的移动到新URI，返回信息会包括新的URI，浏览器会自动定向到新URI。今后任何新的请求都应使用新的URI代替
请求资源的URI已经更新（永久移动），客户端会同步更新URI。
- `302(Found)` : 临时移动。与301类似。但资源只是临时被移动。客户端应继续使用原有URI 资源的URI已临时定位到其他位置，客户端不会更新URI。
- `303(See Other)` : 查看其它地址。与301类似。使用GET和POST请求查看 资源的URI已更新，明确表示客户端要使用GET方法获取资源。
- `304(Not Modified)` : 未修改。所请求的资源未修改，服务器返回此状态码时，不会返回任何资源。客户端通常会缓存访问过的资源，通过提供一个头信息指出客户端希望只返回在指定日期之后修改的资源
- `305(Use Proxy)` : 使用代理。所请求的资源必须通过代理访问 请求的资源必须从服务器指定的地址得到
- `306(Unused)` : 已经被废弃的HTTP状态码。前一版本HTTP中使用的代码，现行版本中不再使用
- `307(Temporary Redirect)` : 临时重定向。与302类似。使用GET请求重定向。临时重定向 申明请求的资源临时性删除


🔷 客户端错误(4XX)

- `400(Bad Request)` : 服务端无法解析客户端发送的参数。
- `401(Unauthorized)` : 请求要求用户的身份认证。
- `402(Payment Required)` : 保留，将来使用。
- `403(Forbidden)` : 禁止访问该资源。
- `404(Not Found)` : 客户端请求的资源不存在。
- `405(Method Not Allowed)` : 请求的方式不支持。
- `406(Not Acceptable)` : 服务器无法根据客户端请求的内容特性完成请求
- `407(Proxy Authentication Required)` : 类似401，用户必须首先在代理服务器上得到授权 请求要求代理的身份认证，与401类似，但请求者应当使用代理进行授权
- `408(Request Time-out)` : 服务器等待客户端发送的请求时间过长，超时
- `409(Conflict)` : 服务器完成客户端的PUT请求是可能返回此代码，服务器处理请求时发生了冲突
- `410(Gone)` : 客户端请求的资源已经不存在。410不同于404，如果资源以前有现在被永久删除了可使用410代码，网站设计人员可通过301代码指定资源的新位置
- `411(Length Required)` : 服务器无法处理客户端发送的不带Content-Length的请求信息
- `412(Precondition Failed)` : 客户端请求信息的先决条件错误 个或多个请求头字段在当前请求中错误
- `413(Request Entity Too Large)` : 由于请求的实体过大，服务器无法处理，因此拒绝请求。为防止客户端的连续请求，服务器可能会关闭连接。如果只是服务器暂时无法处理，则会包含一个Retry-After的响应信息
- `414(Request-URI Too Large)` : 请求的URI过长（URI通常为网址），服务器无法处理
- `415(Unsupported Media Type)` : 服务器无法处理请求附带的媒体格式
- `416(Requested range not satisfiable)` : 客户端请求的范围无效
- `417(Expectation Failed)` : 服务器无法满足Expect的请求头信息

🔷 服务器错误(5XX)

- `500(Internal Server Error)` : 服务器内部错误，无法完成请求。
- `501(Not Implemented)` : 服务器未实现客户端请求的功能。
- `502(Bad Gateway)` : 代理服务器尝试转发客户端的请求时，从目标服务器收到了无效的响应。
- `503(Service Unavailable)` : 服务器资源不足或正在维护状态，无法处理请求。
- `504(Gateway Time-out)` : 代理服务器未在超时时间内从目标服务器收到响应。
- `505(HTTP Version not supported)` : 服务器不支持客户端所指定的HTTP协议版本。
