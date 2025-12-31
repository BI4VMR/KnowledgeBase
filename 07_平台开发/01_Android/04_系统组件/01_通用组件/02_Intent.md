<!-- TODO


# 简介

TODO



Intent 是意图的意思。Android 中的　Intent 正是取自这个意思，它是一个消息对象，通过它，Android 系统的四大组件能够方便的通信，并且保证解耦。

Intent 可以说明某种意图，携带一种行为和相应的数据，发送到目标组件。



# 基本应用

setpackage

setcompentname





# IntentFilter

IntentFilter翻译成中文就是“意图过滤器”，主要用来过滤隐式意图。当用户进行一项操作的时候，Android系统会根据配置的 “意图过滤器” 来寻找可以响应该操作的组件，服务。

例如：当用户点击PDF文件的时候，Android系统就会通过设定好的意图过滤器，进行匹配测试。找到能够打开PDF文件的APP程序。

代码：

    <activity android:name="com.example.testmain.ShowActivity" >
          <intent-filter>
                <action android:name="test.update.mydata" />
                <category android:name="my.test.show" />
                <data android:pathPattern=".*\\.jpg" android:scheme="http" />
          </intent-filter>
     </activity>




2.IntentFilter 如何过滤隐式意图？

Android系统会根据我们配置的Intent Filter（意图过滤器），来进行匹配测试。匹配的时候，只会考虑三个方面：动作、数据（URI以及数据类型）和类别。也就是说Android系统会进行“动作测试”，“数据测试”，“类别测试”，来寻找可以响应隐式意图的组件或服务。

另外，当对其他App程序开放组件和服务的时候也需要配置Intent Filter（意图过滤器），一个Activity可以配置多个<intent-filter>。

3.动作测试：

对应<intent-filter>中的<action/>标签；

(1) 如果<intent-filter>标签中有多个<action/>，那么Intent请求的Action，只要匹配其中的一条<action/>就可以通过了这条<intent-filter>的动作测试。

(2) 如果<intent-filter>中没有包含任何<action/>，那么无论什么Intent请求都无法和这条<intent-filter>匹配。

(2) 如果Intent请求中没有设定Action(动作)，那么这个Intent请求就将顺利地通过<intent-filter>的动作测试（前提是<intent-filter>中必须包含有<action/>，否则与第二条冲突）。



4.类别测试：

对应<intent-filter>中的<category />标签；

（1）Intent中的类别必须全部匹配<intent-filter>中的<category />，但是<intent-filter>中多余的<category />将不会导致匹配失败。

例如：Intent中有3个类别，而意图过滤器中定义了5个，如果Intent中的3个类别都与过滤器中的匹配，那么过滤器中的另外2个，将不会导致类别测试失败。

                                                       

 

注意：有一个例外，Android把所有传给startActivity()的隐式意图当作他们包含至少一个类别："android.intent.category.DEFAULT" （CATEGORY_DEFAULT常量）。 因此，想要接收隐式意图的活动必须在它们的意图过滤器中包含"android.intent.category.DEFAULT"。（带"android.intent.action.MAIN"和"android.intent.category.LAUNCHER"设置的过滤器是例外）

5.数据测试：

对应<intent-filter>中的<data>标签；

<data>元素指定了可以接受的Intent传过来的数据URI和数据类型，当一个意图对象中的URI被用来和一个过滤器中的URI比较时，比较的是URI的各个组成部分。

例如：

如果过滤器仅指定了一个scheme，所有该scheme的URIs都能够和这个过滤器相匹配；

如果过滤器指定了一个scheme、主机名但没有路经部分，所有具有相同scheme和主机名的URIs都可以和这个过滤器相匹配，而不管它们的路经；

如果过滤器指定了一个scheme、主机名和路经，只有具有相同scheme、主机名和路经的URIs才可以和这个过滤器相匹配。当然，一个过滤器中的路径规格可以包含通配符，这样只需要部分匹配即可。
比较规则如下：

（1） 一个既不包含URI也不包含数据类型的意图对象，仅在过滤器也同样没有指定任何URI和数据类型的情况下才能通过测试。

（2）一个包含URI但没有数据类型的意图对象，仅在它的URI和一个同样没有指定数据类型的，过滤器里的URI匹配时才能通过测试。这通常发生在类似于mailto:和tel：这样的URIs上：它们并不引用实际数据。

（3）一个包含数据类型但不包含URI的意图对象，仅在这个过滤器列举了同样的数据类型，而且也没有指定一个URI的情况下才能通过测试。

（4）一个同时包含URI和数据类型（或者可从URI推断出数据类型）的意图对象可以通过测试，如果它的类型和过滤器中列举的类型相匹配的话。如果它的URI和这个过滤器中的一个URI相匹配或者它有一个内容

content:或者文件file: URI，而且这个过滤器没有指定一个URI，那么它也能通过测试。换句话说，一个组件被假定为支持　”content: 数据“　和　“file: 数据”，如果它的过滤器仅列举了一个数据类型。

例如AndroidManifest.xml中有：

对于<intent-filter>中的action项可以有多个只要匹配其中一个就可以了

intent.setAction("com.nanlove.wangshiming");//中的action也可以为wangshiming

intent.addCategory("wangshiming.intent.category")代码中的addCategory并不用写因为android他有默认的category 只要配置清单中存在<category android:name="android.intent.category.DEFAULT" />就可以了.

没有  "数据参数"　的情况下只要意图对象中的设置动作和类别都出现在intent-filter就能跟filter匹配，但是有数据<data android:scheme="love" android:host="hao123.com"  android:port="888" android:path="/MM" />数据项一定要完全匹配。

当数据和数据类型 android:mimeType="text/plain"同时存在的时候，不能使用intent.setData(Uri.parse("love://hao123.com:888/MM")) ；

因为setData的方法会自动清除前面的数据类型：This method automatically clears any type that was previously set by setType；

所以后面的setType就无法匹配，应该使用intent.setDataAndType(Uri.parse("love://hao123.com:888/MM"), "text/plain");

提示：在同一个应用内，能使用显示意图，就尽量使用显示意图，增加程序的效率，理论上隐式意图匹配规则是需要花时间寻找的。






# PendingIntent


PendingIntent是对Intent的封装，但它不是立刻执行某个行为，而是满足某些条件或触发某些事件后才执行指定的行为

关于PendingIntent的使用场景主要用于闹钟、通知、桌面部件。

大体的原理是: A应用希望让B应用帮忙触发一个行为，这是跨应用的通信，需要 Android 系统作为中间人，这里的中间人就是 ActivityManager。 A应用创建建 PendingIntent，在创建 PendingIntent 的过程中，向 ActivityManager 注册了这个 PendingIntent，所以，即使A应用死了，当它再次苏醒时，只要提供相同的参数，还是可以获取到之前那个 PendingIntent 的。当 A 将 PendingIntent 调用系统 API 比如 AlarmManager.set()，实际是将权限给了B应用，这时候， B应用可以根据参数信息，来从 ActivityManager 获取到 A 设置的 PendingIntent







2.2 获取PendingIntent

关于PendingIntent的实例获取一般有以下五种方法，分别对应Activity、Broadcast、Service

    getActivity()
    getActivities()
    getBroadcast()
    getService()
    getForegroundService()

    它们的参数都相同，都是四个：Context， requestCode, Intent, flags，分别对应上下文对象、请求码、请求意图用以指明启动类及数据传递、关键标志位。
    前面三个参数共同标志一个行为的唯一性，而第四个参数flags：

        FLAG_CANCEL_CURRENT：如果当前系统中已经存在一个相同的PendingIntent对象，那么就将先将已有的PendingIntent取消，然后重新生成一个PendingIntent对象。
        FLAG_NO_CREATE：如果当前系统中不存在相同的PendingIntent对象，系统将不会创建该PendingIntent对象而是直接返回null，如果之前设置过，这次就能获取到。
        FLAG_ONE_SHOT：该PendingIntent只作用一次。在该PendingIntent对象通过send()方法触发过后，PendingIntent将自动调用cancel()进行销毁，那么如果你再调用send()方法的话，系统将会返回一个SendIntentException。
        FLAG_UPDATE_CURRENT：如果系统中有一个和你描述的PendingIntent对等的PendingInent，那么系统将使用该PendingIntent对象，但是会使用新的Intent来更新之前PendingIntent中的Intent对象数据，例如更新Intent中的Extras

备注：两个PendingIntent对等是指它们的operation一样, 且其它们的Intent的action, data, categories, components和flags都一样。但是它们的Intent的Extra可以不一样
-->
