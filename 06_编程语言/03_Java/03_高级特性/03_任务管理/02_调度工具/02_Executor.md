<!-- TODO
# 简介
线程池就是首先创建一些线程，它们的集合称为线程池。使用线程池可以很好地提高性能，线程池在系统启动时即创建大量空闲的线程，程序将一个任务传给线程池，线程池就会启动一条线程来执行这个任务，执行结束以后，该线程并不会死亡，而是再次返回线程池中成为空闲状态，等待执行下一个任务。


JDK提供的Executor框架

JDK提供了Executor框架，可以让我们有效的管理和控制我们的线程，其实质也就是一个线程池。Executor下的接口和类继承关系如下：


2. 线程池的工作机制

         2.1 在线程池的编程模式下，任务是提交给整个线程池，而不是直接提交给某个线程，线程池在拿到任务后，就在内部寻找是否有空闲的线程，如果有，则将任务交给某个空闲的线程。

         2.1 一个线程同时只能执行一个任务，但可以同时向一个线程池提交多个任务。
线程重用的核心是，我们知道，Thread.start()只能调用一次，一旦这个调用结束，则该线程就到了stop状态，不能再次调用start。
则要达到复用的目的，则必须从Runnable接口的run()方法上入手，可以这样设计这个Runnable.run()方法（就叫外面的run()方法）：
它本质上是个无限循环，跑的过程中不断检查我们是否有新加入的子Runnable对象（就叫内部的runnable:run()吧，它就是用来实现我们自己的任务），有就调一下我们的run()，其实就一个大run()把其它小run()#1,run()#2,...给串联起来了，基本原理就这么简单
不停地处理我们提交的Runnable任务。
3. 使用线程池的好处

　Java中的线程池是运用场景最多的并发框架，几乎所有需要异步或并发执行任务的程序都可以使用线程池。在开发过程中，合理地使用线程池能够带来3个好处：

　　第一：降低资源消耗。通过重复利用已创建的线程降低线程创建和销毁造成的消耗。

　　第二：提高响应速度。当任务到达时，任务可以不需要等到线程创建就能立即执行。

　　第三：提高线程的可管理性。线程是稀缺资源，如果无限制地创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一分配、调优和监控。但是，要做到合理利用线程池，必须对其实现原理了如指掌。



Executors：提供了一系列静态工厂方法用于创建各种线程池

　其中常用几类如下：

public static ExecutorService newFixedThreadPool()
public static ExecutorService newSingleThreadExecutor()
public static ExecutorService newCachedThreadPool()
public static ScheduledExecutorService newSingleThreadScheduledExecutor()
public static ScheduledExecutorService newScheduledThreadPool()



🔷 `ExecutorService newSingleThreadExecutor()`

创建具有单个线程的线程池。

前一个任务执行完毕才能执行后一个任务，此时与Timer类似。

🔷 `ExecutorService newFixedThreadPool(int nThreads)`

创建具有 `nThreads` 个线程的线程池。

🔷 `ExecutorService newCachedThreadPool()`

缓存，动态扩容，1分钟后关闭线程

🔷 `ExecutorService newSingleThreadScheduledExecutor()`

　　4、newSingleThreadScheduledExecutor：该方法和newSingleThreadExecutor的区别是给定了时间执行某任务的功能，可以进行定时执行等；

🔷 `ExecutorService newScheduledThreadPool(int nThreads)`

　　5、newScheduledThreadPool：在4的基础上可以指定线程数量。




`ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)`

1、corePoolSize 核心线程池大小；
2、maximumPoolSize 线程池最大容量大小；
3、keepAliveTime 线程池空闲时，线程存活的时间；
4、TimeUnit 时间单位；
5、ThreadFactory 线程工厂；
6、BlockingQueue任务队列；
7、RejectedExecutionHandler 线程拒绝策略；





execute和submit的区别

（1）execute()方法用于提交不需要返回值的任务，所以无法判断任务是否被线程池执行成功。通过以下代码可知execute()方法输入的任务是一个Runnable类的实例。

（2）submit()方法用于提交需要返回值的任务。线程池会返回一个future类型的对象，通过这个future对象可以判断任务是否执行成功，并且可以通过future的get()方法来获取返回值，get()方法会阻塞当前线程直到任务完成，而使用get（long timeout，TimeUnit unit）方法则会阻塞当前线程一段时间后立即返回，这时候有可能任务没有执行完。
-->