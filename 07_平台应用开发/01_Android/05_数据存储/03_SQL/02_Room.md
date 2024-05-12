# 简介
<!-- TODO -->

本章示例代码详见以下链接：

- [🔗 示例工程：Room](https://github.com/BI4VMR/Study-Android/tree/master/M05_Storage/C03_SQL/S02_Room)

# 基本应用
下文将以学生信息管理系统为例，演示Room框架的基本使用方法。

我们首先创建一个实体类Student，用于描述“学生”的属性，此处设置“ID、姓名、年龄”三个属性。为了建立实体类与数据库表的关联，我们还需要在Student类的属性与方法上添加一些Room注解。

"Student.java":

```java
@Entity(tableName = "student_info")
public class Student {

    // ID（主键）
    @PrimaryKey
    @ColumnInfo(name = "student_id")
    private long id;

    // 姓名
    @ColumnInfo(name = "student_name")
    private String name;

    // 年龄
    private int age;

    // 具有1个参数的构造方法
    @Ignore
    public Student(long id) {
        this.id = id;
    }

    // 具有3个参数的构造方法
    public Student(long id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    /* 此处省略部分代码... */
}
```

注解 `@Entity` 表示这是一个Room实体类，当程序编译时，Room会以该类的属性作为字段，生成 `tableName` 所指定名称的二维表 `student_info` 。

注解 `@PrimaryKey` 表示 `id` 属性是二维表的主键。

注解 `@ColumnInfo` 用于设置属性在二维表中对应的字段名称。如果某个属性未被添加该注解，则字段名称与属性名称一致。

注解 `@Ignore` 可以被添加在属性与方法上，对于拥有该注解的属性，初始化数据库时Room不会在二维表中创建对应的字段；读取数据时Room也不会寻找对应的字段并进行赋值。对于拥有该注解的方法，它们不会参与Room编译。Room只能使用具有全部属性对应参数的构造方法，若扫描到缺少参数的构造方法，将会出现错误，此处我们通过 `@Ignore` 注解使只有 `id` 参数的构造方法被Room忽略，避免编译失败。

接着，我们创建一个StudentDAO接口，提供对“学生信息表”进行增删改查的方法。

"StudentDAO.java":

```java
@Dao
public interface StudentDAO {

    @Query("SELECT * FROM student_info")
    List<Student> getStudent();

    @Insert
    void addStudent(Student student);

    @Update
    void updateStudent(Student student);

    @Delete
    void delStudent(Student student);
}
```

注解 `@Dao` 表示这是一个Room数据访问类，其中的方法通过 `@Query` 等注解声明了各自的用途，分别对应“查询所有学生”、“新增学生记录”、“更新学生记录”、“删除学生记录”功能。我们并不需要实现这些方法，当程序编译时，Room的注解处理器将会自动生成DAO的实现类，这正是ORM框架的主要功能，简化了开发流程。

最后，我们需要创建一个StudentDB抽象类，继承自RoomDatabase类，此类包含数据库实例的创建与配置逻辑，对于数据库使用者是唯一的入口。

"StudentDB.java":

```java
@Database(entities = Student.class, version = 1)
public abstract class StudentDB extends RoomDatabase {

    private volatile static StudentDB instance = null;

    // 获取数据库实例的方法
    public static StudentDB getInstance(Context context) {
        if (instance == null) {
            synchronized (StudentDB.class) {
                if (instance == null) {
                    // 构造实例并进行配置
                    instance = Room.databaseBuilder(context.getApplicationContext(), StudentDB.class, "student")
                            // Room默认不允许在主线程执行操作，此配置允许在主线程操作，仅适用于调试。
                            .allowMainThreadQueries()
                            // 构建实例
                            .build();
                }
            }
        }
        return instance;
    }

    // 抽象方法，返回StudentDAO实例
    public abstract StudentDAO getStudentDAO();
}
```

Room数据库类通常使用单例模式实现，对外提供该数据库的全局访问点。我们需要在其中书写构建数据库的逻辑与获取每个DAO实例的抽象方法，这些抽象方法将在编译过程中自动生成实现。

注解 `@Database` 表示这是一个Room数据库，属性 `entities` 用于声明本数据库包含的所有实体类，存在多个实体类时，使用逗号(",")分隔，例如： `entities = {A.class, B.class, ...}` 。属性 `version` 表示数据库的版本号，程序启动时用于判断数据库是否需要执行升级或降级操作。

在获取实例的 `getInstance()` 方法中，我们通过Room的 `Room.databaseBuilder(Context context, Class<T> cls, String name)` 方法初始化数据库，此处的三个参数依次为：上下文环境、DB抽象类的Class和数据库文件名称，该方法返回的Builder实例可以配置其他功能，最后我们调用Builder的 `build()` 方法创建StudentDB的实例。

Room默认禁止在主线程操作数据库，因为耗时操作可能会导致ANR；此处为了便于调试，我们添加配置项 `allowMainThreadQueries()` 允许在主线程操作数据库。



<!-- TODO -->

至此，一个完整的数据库模块就编写完成了。我们可以在测试Activity中放置一些控件，并测试StudentDB的增删改查方法。

"TestUIBase.java":

```java
// 获取学生数据库实例
StudentDB studentDB = StudentDB.getInstance(getApplicationContext());
// 获取学生信息表DAO实例
StudentDAO dao = studentDB.getStudentDAO();


/* 查询所有记录 */
List<Student> result = dao.getStudent();
Log.i(TAG, result.toString());


/* 新增记录 */
try {
    // "etID"是一个输入框，先从中获取数据项ID。
    long id = Long.parseLong(binding.etID.getText().toString());
    String name = "田所浩二" + id;
    // 插入记录
    Student student = new Student(id, name, 24);
    dao.addStudent(student);
} catch (Exception e) {
    e.printStackTrace();
}


/* 更新记录 */
try {
    // 获取待更新的数据项ID
    long id = Integer.parseInt(binding.etID.getText().toString());
    // 更新记录
    Student s = new Student(id, "多田野数人", 25);
    dao.updateStudent(s);
} catch (Exception e) {
    e.printStackTrace();
}


/* 删除记录 */
try {
    // 获取待删除的数据项ID
    long id = Integer.parseInt(binding.etID.getText().toString());
    // 删除记录
    Student student = new Student(id);
    dao.delStudent(student);
} catch (Exception e) {
    e.printStackTrace();
}
```

此处只展示关键的逻辑代码，完整内容详见本章示例代码。

<!-- TODO
# 异步查询
-->

# 疑难解答
## 索引

<div align="center">

|       序号        |                         摘要                         |
| :---------------: | :--------------------------------------------------: |
| [案例一](#案例一) | Android Debug Database工具无法查看Room数据库的内容。 |

</div>

## 案例一
### 问题描述
使用Android Debug Database工具调试Room框架生成的数据库时，Web查看到的内容为空。

### 问题分析
当API Level大于16时，Room框架的默认日志模式为WAL，这种模式不会将变更立即写入磁盘，因此Android Debug Database工具无法实时读取内容。

### 解决方案
在构建Database实例时，将日志模式设为"TRUNCATE"。

```java
Room.databaseBuilder(context.getApplicationContext(), StudentDB.class, "student")
    // 设置日志模式为"TRUNCATE"
    .setJournalMode(JournalMode.TRUNCATE)
    .build();
```
