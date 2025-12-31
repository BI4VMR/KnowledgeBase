在 Kotlin 裡面 when是关键字，Mockito 的when ，必须加上反引号才能使用：

  

`when`(xxxx).thenReturn(xxx)

如果看起来不舒服，也可以舍弃 Mockito 改用 mockito-kotlin。

在 mockito-kotlin中使用了whenever代替when，也有更简洁的写法，但是归根到底还是在使用Mockito的Api，所以功能上依然有局限性。
