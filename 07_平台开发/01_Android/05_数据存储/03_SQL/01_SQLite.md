# 简介
SQLite是一款开源的轻量级嵌入式数据库，能够在设备本地存储大量的关系型数据，开发者可以通过SQL语句进行基本的增、删、改、查操作，以及使用事务、视图与触发器等高级功能。

SQLite使用C语言编写，运行效率较高，并且支持多种平台。Android平台已经集成了SQLite环境，应用程序可以通过SDK提供的API直接使用SQLite，开发者不必手动集成相关组件。每个应用程序可以创建多个SQLite数据库，系统会将SQLite数据库文件存储在 `/data/data/<应用程序包名>/databases/` 目录中，默认的文件名后缀为 `.db` 。

本章示例工程详见以下链接：

- [🔗 示例工程：SQLite](https://github.com/BI4VMR/Study-Android/tree/master/M05_Storage/C03_SQL/S01_SQLite)


# 基本应用
## 创建数据库
下文将以学生信息管理系统为例，演示Android SDK中SQLite相关接口的基本使用方法。

🔴 示例一：使用SQLite实现学生信息管理系统。

在本示例中，我们使用SQLite实现简单的学生信息管理系统。

我们编写数据库的Helper类，实现初始化逻辑。

"StudentDBHelper.java":

```java
public class StudentDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "student.db";
    private static final int DB_VERSION = 1;

    // 构造方法
    public StudentDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 回调方法：初始化
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 执行SQL语句，创建学生信息表。
        final String createTableSQL = "CREATE TABLE student_info" +
                "(" +
                "student_id INTEGER PRIMARY KEY," +
                "student_name TEXT," +
                "age INTEGER" +
                ")";
        db.execSQL(createTableSQL);
    }

    // 回调方法：升级
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 暂不使用
    }

    // 获取数据库实例
    public SQLiteDatabase getDB() {
        return getWritableDatabase();
    }
}
```

上述内容也可以使用Kotlin语言编写：

"StudentDBHelperKT.kt":

```kotlin
class StudentDBHelperKT(
    context: Context
) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME: String = "student.db"
        private const val DB_VERSION: Int = 1
    }

    // 回调方法：初始化
    override fun onCreate(db: SQLiteDatabase) {
        // 执行SQL语句，创建学生信息表。
        val createTableSQL: String = """
            CREATE TABLE "student_info"
            (
            "student_id" INTEGER PRIMARY KEY,
            "student_name" TEXT,
            "age" INTEGER
            );
        """.trimIndent()
        db.execSQL(createTableSQL)
    }

    // 回调方法：升级
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 暂不使用。
    }

    // 获取数据库实例
    fun getDB(): SQLiteDatabase {
        return writableDatabase
    }
}
```

SQLiteOpenHelper类用于配置数据库，我们需要继承该类并重写一些方法实现自定义配置。

SQLiteOpenHelper的常用构造方法为 `SQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable CursorFactory factory, int version)` ，第一参数 `context` 为上下文环境；第二参数 `name` 为数据库名称，系统生成的数据库文件也会使用该名称命名；第三参数 `factory` 用于在执行查询时返回自定义Cursor实例，我们很少使用该功能，此处传入空值即可；第四参数 `version` 表示二维表结构的版本号，首次创建数据库时将其设为 `1` 即可。

我们定义了 `getDB()` 方法以供外部获取数据库实例，该方法调用了SQLiteOpenHelper的 `getWritableDatabase()` 方法，将会尝试以“读写”模式打开数据库，如果数据文件所在分
区已满，将会导致 `SQLiteException` 异常。

SQLiteOpenHelper还有一个 `getReadableDatabase()` 方法，首先尝试以“读写”模式打开数据库，如果数据文件所在分区已满，再以“只读”模式打开数据库，不会导致 `SQLiteException` 异常。

当我们调用 `getWritableDatabase()` 或 `getReadableDatabase()` 方法访问数据库时，若数据库文件不存在，系统将会创建文件并回调 `onCreate(SQLiteDatabase db)` 方法进行初始化，唯一参数 `db` 即数据库实例，我们可以通过它创建表结构并写入初始数据。

回调方法 `onUpgrade()` 用于执行数据库升级逻辑，此处为首次创建数据库，因此我们保持空实现即可。

> 🚩 提示
>
> `execSQL()` 方法可以执行任意SQL语句，但不支持由分号( `;` )分隔的多条语句，如果我们传入这种语句，只有第一个分号之前的语句会被执行，后续语句都会被忽略。
>
> 如果我们在代码中嵌入SQL语句，可以多次调用 `execSQL()` 方法，每次执行一部分语句即可。如果我们从配置文件读取SQL脚本，可以根据分号拆分语句，再多次调用 `execSQL()` 方法。

## 插入数据
下文示例展示了SQLite插入数据的相关接口及使用方法。

🟠 示例二：实现新增学生记录功能。

在本示例中，我们在“示例一”的基础上实现新增学生记录功能。

我们在测试Activity中创建StudentDBHelper实例，并调用 `getDB()` 方法获取SQLiteDatabase实例，然后进行增删改查操作。

"TestUIBase.java":

```java
// 创建学生信息数据库工具类的实例。
StudentDBHelper dbHelper = new StudentDBHelper(getApplicationContext());

try {
    // 获取待操作的数据项ID
    long id = Long.parseLong(edittext.getText().toString());
    String name = "田所浩二" + id;

    // 创建ContentValues实例，组织一条记录的各个字段与值。
    ContentValues values = new ContentValues();
    values.put("student_id", id);
    values.put("student_name", name);
    values.put("age", 24);

    // 执行插入操作
    long rawID = dbHelper.getDB().insert("student_info", null, values);
    // 显示新表项的RowID
    Log.i(TAG, "插入成功。 RawID:[" + rawID + "]");
} catch (Exception e) {
    Log.e(TAG, "操作失败！请检查是否已输入ID或ID冲突。");
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIBaseKT.kt":

```kotlin
// 创建学生信息数据库工具类的实例。
val dbHelper = StudentDBHelperKT(getApplicationContext());

kotlin.runCatching {
    // 获取待操作的数据项ID
    val id: Long = edittext.text.toString().toLong()
    val name = "田所浩二$id"

    // 创建ContentValues实例，组织一条记录的各个字段与值。
    val values = ContentValues()
    values.put("student_id", id)
    values.put("student_name", name)
    values.put("age", 24)

    // 执行插入操作
    val rawID: Long = dbHelper.getDB().insert("student_info", null, values)
    // 显示新表项的RowID
    Log.i(TAG, "插入成功。 RawID:[$rawID]")
}.onFailure {
    Log.e(TAG, "操作失败！请检查是否已输入ID或ID冲突。")
}
```

每个ContentValues实例对应一条记录，它提供了一系列 `put` 方法，第一参数为列名，第二参数为数据值，此处我们根据学生信息表的结构依次传入ID、姓名与年龄。

SQLiteDatabase的 `insert()` 方法用于插入数据，我们需要传入目标表名与前文创建的ContentValues实例；该方法的返回值为新记录在二维表中的RowID。

> ⚠️ 警告
>
> SQLite中的RowID不一定等同于主键，我们在使用该数值前需要注意鉴别，此处省略具体描述，详见相关章节： [🧭 SQLite - "RowID"字段](../../../../04_软件技巧/04_数据存储/03_关系型数据库/01_SQLite/02_基础应用.md#rowid字段) 。

## 更新数据
下文示例展示了SQLite更新数据的相关接口及使用方法。

🟡 示例三：实现修改学生记录功能。

在本示例中，我们在“示例一”的基础上实现修改学生记录功能。

我们可以使用SQLiteDatabase的 `update()` 方法更新数据。

"TestUIBase.java":

```java
StudentDBHelper dbHelper = new StudentDBHelper(getApplicationContext());

try {
    // 获取待操作的数据项ID
    long id = Long.parseLong(edittext.getText().toString());

    // 创建ContentValues实例，组织一条记录的各个字段与值。
    ContentValues values = new ContentValues();
    values.put("student_name", "德川");
    values.put("age", 25);

    // 执行更新操作
    int lines = dbHelper.getDB().update("student_info", values, "student_id = ?", new String[]{id + ""});
    // 显示受影响的行数
    Log.i(TAG, "更新成功。 Lines:[" + lines + "]");
} catch (Exception e) {
    Log.e(TAG, "操作失败！请检查是否已输入ID或ID冲突。");
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIBaseKT.kt":

```kotlin
val dbHelper = StudentDBHelperKT(getApplicationContext());

kotlin.runCatching {
    // 获取待操作的数据项ID
    val id: Long = edittext.text.toString().toLong()

    // 创建ContentValues实例，组织一条记录的各个字段与值。
    val values = ContentValues()
    values.put("student_name", "德川")
    values.put("age", 25)

    // 执行更新操作
    val lines: Int = dbHelper.getDB().update("student_info", values, "student_id = ?", arrayOf("$id"))
    // 显示受影响的行数
    Log.i(TAG, "更新成功。 Lines:[$lines]")
}.onFailure {
    Log.e(TAG, "操作失败！请检查是否已输入ID或ID冲突。")
}
```

`update()` 方法的第一参数 `table` 为目标表名；第二参数 `values` 为ContentValues实例，其中声明的属性将会覆盖现有记录的对应字段，没有声明的属性不会发生变化；第三参数 `whereClause` 对应SQL中的WHERE子句，其中的问号为占位符，语句执行时将被替换为第四参数 `whereArgs` 中对应的值，例如：我们输入的ID为 `6` 时，该语句等同于 `WHERE "student_id" = 6` 。

`update()` 方法的返回值为受影响的行数，我们可以校验修改范围是否符合预期。

## 删除数据
下文示例展示了SQLite删除数据的相关接口及使用方法。

🟢 示例四：实现删除学生记录功能。

在本示例中，我们在“示例一”的基础上实现删除学生记录功能。

我们可以使用SQLiteDatabase的 `delete()` 方法删除数据。

"TestUIBase.java":

```java
StudentDBHelper dbHelper = new StudentDBHelper(getApplicationContext());

try {
    // 获取待操作的数据项ID
    long id = Long.parseLong(binding.etID.getText().toString());

    // 执行删除操作
    int lines = dbHelper.getDB().delete("student_info", "student_id = ?", new String[]{id + ""});
    // 显示受影响的行数
    Log.i(TAG, "删除成功。 Lines:[" + lines + "]");
} catch (Exception e) {
    Log.e(TAG, "操作失败！请检查是否已输入ID或ID冲突。");
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIBaseKT.kt":

```kotlin
val dbHelper = StudentDBHelperKT(getApplicationContext());

runCatching {
    // 获取待操作的数据项ID
    val id: Long = binding.etID.text.toString().toLong()

    // 执行删除操作
    val lines: Int = dbHelper.getDB().delete("student_info", "student_id = ?", arrayOf("$id"))
    // 显示受影响的行数
    Log.i(TAG, "删除成功。 Lines:[$lines]")
}.onFailure {
    Log.e(TAG, "操作失败！请检查是否已输入ID或ID冲突。")
}
```

`delete()` 方法的第一参数 `table` 为目标表名；其他参数与 `update()` 方法类似，用于组合WHERE子句确定删除条件，返回值为受影响的行数。

## 查询数据
下文示例展示了SQLite查询数据的相关接口及使用方法。

🔵 示例五：实现查询学生记录功能。

在本示例中，我们在“示例一”的基础上实现查询学生记录功能。

SQLiteDatabase的 `query()` 方法用于查询数据，该方法参数较多，后文将进行详细说明，此处我们只指定目标表名，其他参数均设为空值，查询该表中的所有数据。

"TestUIBase.java":

```java
StudentDBHelper dbHelper = new StudentDBHelper(getApplicationContext());

Cursor cursor = dbHelper.getDB()
        .query("student_info", null, null, null, null, null, null)
try (cursor) {
    // 判断游标实例中是否存在数据项。
    if (cursor.moveToFirst()) {
        // 遍历游标实例，读取所有数据项。
        do {
            // 根据列索引与类型，读取当前行的属性。
            long id = cursor.getLong(0);
            String name = cursor.getString(1);
            int age = cursor.getInt(2);

            // 生成Java对象
            Student student = new Student(id, name, age);
            // 显示对象信息
            Log.i(TAG, student.toString());
        } while (cursor.moveToNext());
    } else {
        Log.e(TAG, "查询结果为空！");
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIBaseKT.kt":

```kotlin
val dbHelper = StudentDBHelperKT(getApplicationContext());

val cursor: Cursor = dbHelper.getDB()
    .query("student_info", null, null, null, null, null, null)
cursor.use {
    // 判断游标实例中是否存在数据项。
    if (it.moveToFirst()) {
        // 遍历游标实例，读取所有数据项。
        do {
            // 根据列索引与类型，读取当前行的属性。
            val id: Long = it.getLong(0)
            val name: String = it.getString(1)
            val age: Int = it.getInt(2)

            // 生成Kotlin对象
            val studentKT = StudentKT(id, name, age)
            // 显示对象信息
            Log.i(TAG, studentKT.toString())
        } while (it.moveToNext())
    } else {
        Log.e(TAG, "查询结果为空！")
    }
}
```

`query()` 方法的返回值为Cursor实例，包含查询结果，它是一个二维表结构，“游标”指向表中的“行”，我们可以切换游标位置读取各行的数据。当Cursor实例使用完毕后，我们应当调用 `close()` 方法释放资源；在Java中我们使用了 `try-with-resources` 语法，在Kotlin中我们使用了扩展函数 `use()` ，它们都能实现使用完毕后自动关闭Cursor。

Cursor实例的 `moveToFirst()` 方法会将游标移动至第一行。如果该行不存在，返回 `false` ；如果该行存在，则返回 `true` 。在遍历二维表之前，我们需要通过该方法检查表是否为空，顺便初始化游标位置。

Cursor实例的 `moveToNext()` 方法会将游标移动至当前位置的后一行。如果该行不存在，返回 `false` ；如果该行存在，则返回 `true` 。每轮循环完成时，我们可以通过该方法检查是否还有后继表项，并将游标移动至后一项。

在循环体中，我们可以通过Cursor实例的 `get` 系列方法获取数据，唯一参数为列索引，我们选择的方法要与该列的数据类型相匹配，否则会出现错误。


# 事务支持




默认是最高隔离级别，一个事务开启后其他事务所在线程将被阻塞，直到首个事务执行完毕后才会继续运行


使用协程时需要注意线程切换 https://zhuanlan.zhihu.com/p/250511061

# 版本迁移
SQLite数据库使用整数版本号标识二维表结构，初始版本号为 `1` 。随着程序的演进，如果我们需要进行新增二维表、修改现有表结构、删除现有表等操作，就要提升版本号。

当SQLiteOpenHelper实例被构造时，SQLite API将会检查当前版本号是否与本地数据库的版本号一致，若不一致则回调 `onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)` 方法，第二参数 `oldVersion` 为本地数据库的版本号；第三参数 `newVersion` 为新的版本号，我们需要在此处实现升级逻辑，将旧版本的表结构变更到新版本。

新增二维表、删除现有表、在现有表中新增列等操作都可以使用SQL语句完成，但SQLite不支持修改或删除现有表中的字段，如果要进行此类操作，只能将旧表中的数据读取到内存中，转换数据结构后再写入新表。

以前文示例为基础，我们将学生信息表的整型字段年龄 `age` 变更为字符型字段出生日期 `birthday` ，并将数据结构升级至版本2。

"StudentDBHelper.java":

```java
public class StudentDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "student";
    // 数据库表结构版本
    private static final int DB_VERSION = 2;

    // 构造方法
    public StudentDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 根据版本号执行对应的升级流程。
        if (oldVersion == 1 && newVersion == 2) {
            migrateV1ToV2(db);
        }
    }

    // 版本1至版本2的数据结构升级逻辑
    private void migrateV1ToV2(SQLiteDatabase db) {
        // 修改旧表的名称
        db.execSQL("ALTER TABLE student_info RENAME TO student_info_temp;");

        // 以新的数据结构创建学生信息表
        final String createTableSQL = "CREATE TABLE student_info" +
                "(" +
                "student_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_name TEXT," +
                "birthday TEXT" +
                ")";
        db.execSQL(createTableSQL);

        // 读取旧表中的数据
        List<Student> oldDatas = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM student_info_temp", null);
        try (cursor) {
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    int age = cursor.getInt(2);

                    Student student = new Student(id, name, age);
                    oldDatas.add(student);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 迁移数据至新的类型，并写入新表。
        for (Student student : oldDatas) {
            long id = student.getID();
            String name = student.getName();
            String birthday = "2024-01-01";

            String sql = "INSERT INTO student_info VALUES(" + id + ", '" + name + "', '" + birthday + "');";
            db.execSQL(sql);
        }

        // 删除旧表
        db.execSQL("DROP TABLE student_info_temp");
    }
}
```

上述内容也可以使用Kotlin语言编写：

"StudentDBHelper.kt":

```kotlin
class StudentDBHelperKT(
    context: Context
) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME: String = "student"
        private const val DB_VERSION: Int = 2
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 根据版本号执行对应的升级流程。
        if (oldVersion == 1 && newVersion == 2) {
            migrateV1ToV2(db)
        }
    }

    // 版本1至版本2的数据结构升级逻辑
    private fun migrateV1ToV2(db: SQLiteDatabase) {
        // 修改旧表的名称
        db.execSQL("ALTER TABLE student_info RENAME TO student_info_temp;")

        // 以新的数据结构创建学生信息表
        val createTableSQL: String = """
            CREATE TABLE student_info
            (
            student_id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_name TEXT,
            birthday TEXT
            );
        """.trimIndent()
        db.execSQL(createTableSQL)

        // 读取旧表中的数据
        val oldDatas: MutableList<StudentKT> = mutableListOf()
        val cursor: Cursor = db.rawQuery("SELECT * FROM student_info_temp", null)
        cursor.use {
            if (cursor.moveToFirst()) {
                do {
                    // 根据列索引与类型，读取当前行的属性。
                    val id: Long = it.getLong(0)
                    val name: String = it.getString(1)
                    val age: Int = it.getInt(2)

                    val student = StudentKT(id, name, age)
                    oldDatas.add(student)
                } while (it.moveToNext())
            }
        }

        // 迁移数据至新的类型，并写入新表。
        oldDatas.forEach {
            val id: Long = it.id
            val name: String = it.name
            val birthday = "2024-01-01"

            val sql = "INSERT INTO student_info VALUES($id, '$name', '$birthday');"
            db.execSQL(sql)
        }

        // 删除旧表
        db.execSQL("DROP TABLE student_info_temp")
    }
}
```

当程序更新并运行后，当前版本号为"2"，本地数据库版本号为"1"，将会回调 `onUpgrade()` 方法，并满足"if"语句的条件，执行 `migrateV1ToV2()` 方法中的数据结构迁移逻辑。

对于需要修改或删除现有表字段的场景，基本操作步骤如下文列表所示：

1. 将旧表改为其他名称。
2. 将旧表中的现有数据读入内存。
3. 创建新的二维表。
4. 将现有数据转换为符合新表结构的形式，存入新表。
5. 删除旧表。

当迁移操作完成后，我们就可以使用 `String` 类型读写 `birthday` 字段了。

"TestUIUpgrade.java":

```java
Cursor cursor = dbHelper.getDB()
        .query("student_info", null, null, null, null, null, null);
try (cursor) {
    if (cursor.moveToFirst()) {
        do {
            // 根据列索引与类型，读取当前行的属性。
            long id = cursor.getLong(0);
            String name = cursor.getString(1);
            String birthday = cursor.getString(2);

            // 生成Java对象。
            StudentV2 student = new StudentV2(id, name, birthday);
            // 显示对象信息。
            Log.i(TAG, student.toString());
        } while (cursor.moveToNext());
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

上述内容也可以使用Kotlin语言编写：

"TestUIUpgradeKT.kt":

```kotlin
val cursor: Cursor = dbHelper.getDB()
    .query("student_info", null, null, null, null, null, null)
cursor.use {
    if (it.moveToFirst()) {
        do {
            // 根据列索引与类型，读取当前行的属性。
            val id: Long = it.getLong(0)
            val name: String = it.getString(1)
            val birthday: String = it.getString(2)

            // 生成Kotlin对象。
            val student = StudentV2KT(id, name, birthday)
            // 显示对象信息。
            Log.i(TAG, student.toString())
        } while (it.moveToNext())
    }
}
```

上述示例代码只是一个简单的迁移过程示范，在实际应用中我们还可以进行以下优化：

- 每组数据库版本之间都要有对应的升级逻辑，不可遗漏，因为用户可能会从任意旧版本更新至最新版本。
- 跨版本升级时，可以依次执行升级逻辑，例如：从版本1升级至版本3时，可以依次执行“版本1升级至版本2”与“版本2升级至版本3”的逻辑，避免重复书写代码。
- 迁移过程将在首个数据库查询调用执行时被触发，并以该调用的线程执行 `onUpgrade()` 方法，直到迁移完成后，该线程才会执行调用者请求的查询并返回结果。如果我们希望程序启动后即刻开始迁移，减少用户后续查询的等待时长，可以在初始化阶段调用任意查询方法。
