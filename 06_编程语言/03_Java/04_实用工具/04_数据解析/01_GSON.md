# 基本应用
## 对象转文本



## 文本转对象



String[] nameArray = gson.fromJson(namesJson, String[].class);



null或缺失
对于JSON字符串里没有的变量，Gson在反序列化时会给它一个默认值，int类型默认为0，bool类型默认为false，String类型默认为null。


通过反射解析属性，无视方法

# 注解

## 名称控制
@SerializedName("fullName")
    private String name;

    序列化时使用fullName，反序列化时读取fullName写入name


@SerializedName(value = "name", alternate = "fullName")

，alternate只是反序列化JSON的一个备选变量名，它不会影响序列化，



在将某个对象序列化时，对象中的某些变量是不需要的。



@Expose(serialize = false, deserialize = false)
    String email; // 不参与序列化，也不参与反序列化

transient 关键字 ，使用这个关键字，可以直接让变量不参与序列化/反序列化，如下：



# JSON实例
有时我们并不需要将json文本整个映射为对象，或想要通过代码动态构建JSON，此时需要使用JsonElement
JsonElement
JsonParser.parseString(text)


JsonObject
JsonArray


element.isJsonObject()
isJsonNull()
isJsonArray
isJsonPrimitive()
