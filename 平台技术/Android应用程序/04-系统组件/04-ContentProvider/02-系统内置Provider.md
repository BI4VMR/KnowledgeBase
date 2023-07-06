# 简介
系统提供了一些内置的ContentProvider，以便我们获取联系人、日历、设置等公共数据，我们需要使用ContentResolver与这些组件交互。

ContentResolver默认是通过Binder在主线程中工作，因此若查询目标需要联网或数据量过大，容易导致应用程序发生ANR，这种情况应当在子线程中进行查询操作。

# 基本应用
此处以联系人为例，我们使用ContentResolver查询所有联系人的ID和名称。

DemoBaseUI.java:

```java
// 获取ContentResolver对象
ContentResolver contentResolver = getContentResolver();
// 查询联系人
Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
// 遍历游标，读取信息。
if (cursor != null && cursor.moveToFirst()) {
    // 获取列索引
    int indexID = cursor.getColumnIndex(ContactsContract.Contacts._ID);
    int indexName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
    // 部分手机系统版本可能缺少某些字段，导致索引数值为"-1"，需要注意。
    if (indexID < 0 || indexName < 0) {
        cursor.close();
        return;
    }

    do {
        Log.i("myapp", "ID:" + cursor.getString(indexID));
        Log.i("myapp", "Name:" + cursor.getString(indexName));
    } while (cursor.moveToNext());
    cursor.close();
}
```

访问ContentProvider需要通过ContentResolver对象，我们可以使用Context对象的 `getContentResolver()` 进行获取。

ContentResolver对象拥有增删改查方法，以供我们操作远程数据，此处以查询方法 `query()` 为例，返回值为Cursor，我们可以遍历游标对象获取数据。

对于联系人数据，我们还需要在Manifest文件中声明权限。

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="net.bi4vmr.study">

    <uses-permission android:name="android.permission.READ_CONTACTS" />

    <application ...>
</manifest>
```

我们首先在系统设置中进行授权，再运行示例程序，此时观察日志信息，可以发现能够查询到数据。

```text
2023-06-28 16:17:16.139 23160-23160/? I/myapp: ID:1
2023-06-28 16:17:16.139 23160-23160/? I/myapp: Name:Alice
2023-06-28 16:17:16.139 23160-23160/? I/myapp: ID:2
2023-06-28 16:17:16.139 23160-23160/? I/myapp: Name:Bob
```
