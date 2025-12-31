
# LocalDateTime与DateTimeFormatter
## 简介
由于Calendar和SimpleDateFormat设计上存在缺陷，并且不是线程安全的，从Java 8开始，官方引入了一系列新的时间日期工具，它们位于"java.time"包内，主要有以下组件：

- 本地时间与日期：LocalDateTime、LocalDate、LocalTime。
- 时区时间与日期：ZonedDateTime。
- 格式化工具：DateTimeFormatter。

我们可以使用LocalDateTime取代Calendar，使用DateTimeFormatter取代SimpleDateFormat，这些新的工具都是线程安全的，并且API有所改进，月份等值采用自然序号，不再从0开始计数。

## 常用方法
LocalDate用于处理本地日期；LocalTime用于处理本地时间；LocalDateTime包含LocalDate和LocalTime两个变量，我们可以根据需要进行选择。

🔷 `LocalDateTime now()`

静态方法，获取LocalDateTime实例，其时间为当前时刻。

🔷 `LocalTime of(int h, int m, int s)`

使用参数指定的时分秒创建LocalTime对象。

🔷 `LocalDateTime parse(String s)`

使用参数指定的日期与时间创建LocalDateTime对象，参数为ISO 8601格式的文本。

🔷 `String toString()`

将时间日期转为ISO 8601格式的文本，例如："2022-11-20T15:06:10.453679"，"T"是时间与日期的分界符，"T"之前为年、月、日；"T"之后为时、分、秒、纳秒。

<!-- TODO 需要补充内容 -->

# DateTimeFormatter
DateTimeFormatter是在Java 8中引入的一个格式化器，用于打印和解析日期时间对象。

DateTimeFormatter是不可变的，并且是线程安全的。

DateTimeFormatter使用用户定义的格式（如 "yyyy-MMM-dd hh:mm:ss"）或使用预定义的常数（如ISO_LOCAL_DATE_TIME）来格式化日期时间。

一个DateTimeFormatter可以用所需的Locale、Chronology、ZoneId和DecimalStyle创建。

DateTimeFormatter通过使用其ofPattern方法被实例化为一个日期时间格式字符串。


内置格式：

| Formatter            | Example                                   |
| -------------------- | ----------------------------------------- |
| BASIC_ISO_DATE       | ‘20181203’                                |
| ISO_LOCAL_DATE       | ‘2018-12-03’                              |
| ISO_OFFSET_DATE      | ‘2018-12-03+01:00’                        |
| ISO_DATE             | ‘2018-12-03+01:00’; ‘2018-12-03’          |
| ISO_LOCAL_TIME       | ‘11:15:30.5975254’                                |
| ISO_OFFSET_TIME      | ‘11:15:30+01:00’                          |
| ISO_TIME             | ‘11:15:30+01:00’; ‘11:15:30’              |
| ISO_LOCAL_DATE_TIME  | ‘2018-12-03T11:15:30’                     |
| ISO_OFFSET_DATE_TIME | ‘2018-12-03T11:15:30+01:00’               |
| ISO_ZONED_DATE_TIME  | ‘2018-12-03T11:15:30+01:00[Europe/Paris]’ |
| ISO_DATE_TIME        | ‘2018-12-03T11:15:30+01:00[Europe/Paris]’ |
| ISO_ORDINAL_DATE     | ‘2018-337’                                |
| ISO_WEEK_DATE        | ‘2018-W48-6’                              |
| ISO_INSTANT          | ‘2018-12-03T11:15:30Z’                    |
| RFC_1123_DATE_TIME   | ‘Tue, 3 Jun 2018 11:05:30 GMT’            |
