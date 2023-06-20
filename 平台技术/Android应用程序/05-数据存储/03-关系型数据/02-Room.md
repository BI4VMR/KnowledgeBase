<!-- TODO
# 概述

# 基本应用
我们以“学生”信息表为例，演示Room框架的基本应用。

首先创建一个实体类，用于描述“学生”的属性，此处我们设置“ID、名称、年龄”三个属性。

```java
@Entity
public class Student {

    @PrimaryKey
    private int id;
    private String name;
    private int age;

    [此处省略Get、Set与构造方法...]
}
```

注解"@Entity"表示这是一个Room实体类，编译时Room将会以该类的属性为字段，生成二维表"Student"。注解"@PrimaryKey"声明该属性"id"是表的主键，用于保证记录的唯一性。

接着我们创建一个接口StudentDAO，其中包含查询全部学生、添加学生、删除指定学生、更新指定学生信息四个方法，分别对应注解“Query、Insert、Delete、Update”。Room会在编译时根据接口方法自动生成类"StudentDAOImpl"，其中包含各方法的具体实现，我们可以直接调用这些方法。

```java
@Dao
public interface StudentDAO {

    @Query("SELECT * FROM Student")
    List<Student> getStudent();

    @Insert
    void addStudent(Student student);

    @Delete
    void delStudent(Student student);

    @Update
    void updateStudent(Student student);
}
```

最后创建一个抽象类StudentDB，继承自RoomDatabase，此类包含数据库的配置，并将实体类、DAO类关联起来，对于数据库使用者是唯一的入口。

```java
@Database(entities = Student.class, version = 1, exportSchema = false)
public abstract class StudentDB extends RoomDatabase {

    private static StudentDB instance;

    // 单例模式：获取DB实例。
    public static synchronized StudentDB getInstance(Context context) {
        if (instance == null) {
            Context appCtx = context.getApplicationContext();
            instance = Room.databaseBuilder(appCtx, StudentDB.class, "Test")
                    // Room默认不允许在主线程执行操作，此配置允许在主线程操作，仅适用于调试。
                    .allowMainThreadQueries()
                    // 构建实例
                    .build();
        }
        return instance;
    }

    // 抽象方法，返回StudentDAO实例
    public abstract StudentDAO getStudentDAO();
}
```

注解"@Database"的"entities"字段声明本数据库包含的实体，"version"字段申明数据库版本号，"exportSchema"字段声明是否允许导出表结构，这些字段是必选的。

此类中我们使用单例模式对外提供唯一实例，该实例通过Room的构造器"Room.databaseBuilder()"创建，构造器的三个参数分别为：上下文环境、DB类Class和数据库名称。Room默认禁止在主线程操作数据库，因为耗时操作可能会导致ANR，此处我们为了便于测试，添加配置项"allowMainThreadQueries()"允许主线程操作数据库。

此类中的抽象方法"getStudentDAO()"返回值是StudentDAO实例，编译时将会自动生成实现，调用者通过该方法获取学生DAO实例并进行增删改查操作。

上述操作完成了数据库的各项配置，接下来我们在MainActivity中调用数据库。

```java
// 获取数据库实例
StudentDB studentDB = StudentDB.getInstance(this);
// 获取DAO实例
StudentDAO dao = studentDB.getStudentDAO();

btSelect.setOnClickListener(v -> {
    // 查询记录
    List<Student> result = dao.getStudent();
    tvInfo.setText(result.toString());
});

btAdd.setOnClickListener(v -> {
    // 获取待操作的数据项ID
    int id = Integer.parseInt(etID.getText().toString());
    // 新增记录
    Student s = new Student(id, "田所浩二", 24);
    dao.addStudent(s);
});

btDel.setOnClickListener(v -> {
    // 获取待操作的数据项ID
    int id = Integer.parseInt(etID.getText().toString());
    // 删除记录
    Student s = new Student(id);
    dao.delStudent(s);
});

btUpdate.setOnClickListener(v -> {
    // 获取待操作的数据项ID
    int id = Integer.parseInt(etID.getText().toString());
    // 更新记录
    Student s = new Student(id, "远野", 25);
    dao.updateStudent(s);
});
```

我们通过StudentDB的"getInstance()"方法获取DB实例，然后调用"getStudentDAO()"方法获取学生DAO实例，此时就可以进行增删改查操作了。此处我们使用四个按钮进行操作，详见示例代码。

# 异步查询(TODO) -->