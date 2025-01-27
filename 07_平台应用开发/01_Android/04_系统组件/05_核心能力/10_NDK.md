# 简介
JVM可以通过JNI技术调用动态链接库，以便实现音视频处理、3D渲染等任务，该技术在Android平台上也是可用的，这种特性极大地扩展了应用程序的功能。

NDK(Native Development Kit)是Android SDK中提供的工具集合，包括C/C++开发环境、编译工具Clang、构建工具CMake等。非Android平台的JNI开发流程较为繁琐，需要我们分别手动配置两种语言的开发环境；而在Android平台中，当模块编译时，NDK能够自动整合工具链编译C/C++源代码，并将生成的库文件打包至构建产物。

NDK使得开发者在Android Studio一款IDE中即可完成应用程序开发，无需手动配置其他环境，提高了开发效率。我们在进行NDK开发之前，首先需要了解以下前置知识：

- [🧭 Java - JNI](../../../../06_编程语言/03_Java/03_高级特性/04_外部交互/02_JNI.md)

<!-- TODO
需要添加指向CMake文档的链接，该文档暂未添加。
- [🧭 构建工具 - CMake](TODO)
-->

本章示例代码详见以下链接：

- [🔗 示例工程：NDK](https://github.com/BI4VMR/Study-Android/tree/master/M04_System/C05_Ability/S06_NDK) 

# 理论基础
## ABI
应用程序二进制接口（Application Binary Interface, ABI）是一种硬件平台与操作系统的交互协议，由于各种硬件平台所使用的数据类型、字节序、指令集有所差异，因此它们能够识别的机器码格式也各不相同。

Java通过JVM作为兼容层，实现了跨平台特性；C/C++源代码编译后直接生成机器码，没有兼容层，因此无法实现跨平台，我们需要分别为每种ABI编译动态链接库，才能使应用程序支持多种平台。

目前常见的硬件平台有"x86"与"ARM"两种，"x86"平台通常是台式电脑、笔记本电脑与工控机等设备，而"ARM"平台通常是智能手机、平板电脑等设备。

Android平台常用的ABI如下文表格所示：

<div align="center">

|    ABI名称    |     详情      |    生命周期    |
| :-----------: | :-----------: | :------------: |
|   `armeabi`   | 32位"ARM"平台 |  目前基本淘汰  |
| `armeabi-v7a` | 32位"ARM"平台 |  正在逐步淘汰  |
|  `arm64-v8a`  | 64位"ARM"平台 | 处于主流应用中 |
|     `x86`     | 32位"x86"平台 |  正在逐步淘汰  |
|   `x86_64`    | 64位"x86"平台 | 处于主流应用中 |

</div>

关于每种ABI的详细信息，可以参考Google官方文档： [🔗 Android ABI支持信息](https://developer.android.google.cn/ndk/guides/abis) 。

一款CPU除了支持当前架构所对应的主要ABI之外，可能还支持多种辅助ABI；例如：64位"x86"平台处理器的主要ABI为"x86_64"，辅助ABI为"x86"。辅助ABI只为提供兼容性支持，为了达到最佳性能，我们应当优先提供主要ABI所对应的动态链接库。

我们可以通过Build类中的一些属性获取当前设备所支持的ABI信息，它们的数据类型都是字符串数组，元素已经按照主要ABI、辅助ABI的顺序进行了排序。

"TestUIBase.java":

```java
// 获取当前硬件平台支持的所有ABI
String[] allABIs = Build.SUPPORTED_ABIS;
Log.i(TAG, "当前硬件平台支持的所有ABI：" + Arrays.toString(allABIs));

// 获取当前硬件平台支持的32位ABI
String[] ABIs32 = Build.SUPPORTED_32_BIT_ABIS;
Log.i(TAG, "当前硬件平台支持的32位ABI：" + Arrays.toString(ABIs32));

// 获取当前硬件平台支持的64位ABI
String[] ABIs64 = Build.SUPPORTED_64_BIT_ABIS;
Log.i(TAG, "当前硬件平台支持的64位ABI：" + Arrays.toString(ABIs64));
```

此时我们在Qualcomm Snapdragon 845平台上运行示例程序，并查看控制台输出信息：

```text
22:43:56.974 21356 21356 I TestApp: 当前硬件平台支持的所有ABI：[arm64-v8a, armeabi-v7a, armeabi]
22:43:56.978 21356 21356 I TestApp: 当前硬件平台支持的32位ABI：[armeabi-v7a, armeabi]
22:43:56.980 21356 21356 I TestApp: 当前硬件平台支持的64位ABI：[arm64-v8a]
```

根据上述输出内容可知：

该硬件平台的主要ABI为"arm64-v8a"，支持64位程序；并且可以兼容"armeabi-v7a"和"armeabi"。

## 32位/64位应用
为了提高应用程序的性能，很多应用市场要求必须提供64位应用程序，我们可以通过以下规则区分应用程序的位数：

如果我们的工程中不包含任何C/C++代码，并且引入的依赖组件也不包含动态链接库，则编译产物与硬件平台无关，必然是一个64位应用程序。

如果我们的工程中通过C/C++调用了动态链接库，或依赖组件包含动态链接库，则要求它们均为64位，编译产物便可被称为64位应用程序；若其中的某个库不支持64位环境，则编译产物为32位应用程序。

我们可以将APK文件解压缩后，检查其中的动态链接库来确定应用程序的位数；也可以先将应用程序安装到设备上，然后通过 [🔗 LibChecker](https://github.com/LibChecker/LibChecker) 等工具进行判断。

## 动态链接库管理
### 系统公共库文件
以下目录是系统的公共库文件目录，用于存放通用组件（例如SQLite、SSL、Vulkan等），它们在系统编译阶段就被固定了，在系统运行过程中不会被改变。

- `/system/lib32/` : 存放系统自带的32位库文件。
- `/system/lib64/` : 存放系统自带的64位库文件。
- `/vendor/lib32/` : 存放设备制造商预置的32位库文件。
- `/vendor/lib64/` : 存放设备制造商预置的64位库文件。

### 第三方应用库文件
一个包含动态链接库的APK文件结构如下文代码块所示：

```text
App.apk
└── lib
    ├── arm64-v8a
    │   ├── lib1.so
    │   └── ...
    ├── armeabi-v7a
    ├── x86
    └── x86_64
```

当应用程序安装时，PMS将会扫描 `lib` 目录下的库文件，优先查找设备硬件平台主要ABI对应的库文件，并将它们复制到 `/data/app/<应用程序包名>/lib/` 目录下，以便程序运行时进行加载。

如果APK中没有主要ABI对应的库文件，PMS将会尝试查找辅助ABI对应的库文件；若辅助ABI也无一匹配，则会安装失败并返回"INSTALL_FAILED_NO_MATCHING_ABIS"消息。

对于前文所述的APK文件，若我们将其安装在Qualcomm Snapdragon 845平台上，最终生成的目录结构如下文代码块所示：

```text
/data/app/<应用包名>
├── lib
│   └── arm64-v8a
│       ├── lib1.so
│       └── ...
└── App.apk
```

该平台的首选ABI为"arm64-v8a"，因此PMS只会复制APK中对应的库文件，而忽略其他ABI。

## 预置应用库文件
对于系统预置的应用程序，由于缺少安装过程中复制库文件的步骤，我们需要手动将库文件放置到 `<APK存放目录>/lib/<目标平台ABI>/` 或系统公共库文件目录中，否则应用程序启动时会因找不到库文件而崩溃。

如果我们需要将前文的 `App.apk` 文件作为预置应用放入 `/system/app/` 目录，则需要将APK解压缩，然后复制与硬件平台相关的库文件，创建以下目录结构：

```text
/system/app/<应用包名>
├── lib
│   └── arm64-v8a
│       ├── lib1.so
│       └── ...
└── App.apk
```

如果我们不清楚该目录的路径与子目录命名规则，可以添加以下代码并运行一次程序进行查询：

```java
// 获取当前硬件平台首选的库文件存放路径
ApplicationInfo appInfo = context.getApplication().getApplicationInfo();
String libDir = appInfo.nativeLibraryDir;
Log.i(TAG, "当前应用的库目录：" + libDir);
```

若 `App.apk` 是由我们自行编译生成的，我们可以为它添加以下Gradle配置，在打包时将库文件排除，这有助于减小APK和系统镜像的体积。

"build.gradle":

```groovy
android {
    // 打包配置
    packagingOptions {
        // 排除ARM64架构的OpenCV库
        jniLibs.excludes.add("lib/arm64-v8a/libopencv_java3.so")

        // 排除ARM64架构所有库
        jniLibs.excludes.add("lib/arm64-v8a/*.so")
    }
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
android {
    // 打包配置
    packagingOptions {
        // 排除ARM64架构的OpenCV库
        jniLibs.excludes.add("lib/arm64-v8a/libopencv_java3.so")

        // 排除ARM64架构所有库
        jniLibs.excludes.add("lib/arm64-v8a/*.so")
    }
}
```

`packagingOptions {}` 小节也可以配合Build Types或Product Flavors使用，以便适应更多的场景。

## 库文件的查找顺序
当应用程序加载动态库时，将会按照预定的顺序查找库文件，一旦命中则不再继续查找。

以64位ARM平台为例，下文列表展示了64位应用程序的库文件加载顺序：

1. `<应用程序安装目录>/lib/arm64/`
2. `vendor/lib64/`
3. `system/lib64/`

# 基本应用
## 添加Java源代码
我们首先在Java源代码目录中创建一个JNIClass，其中包含一些本地方法声明。

"JNIClass.java":

```java
package net.bi4vmr.study.base;

public class JNIClass {

    static {
        // 加载"libjnitest.so"
        System.loadLibrary("jnitest");
    }

    // 本地方法：传递基本数据类型参数
    native void passBasicTypes(boolean b, int i, double d);

    // 本地方法：传递字符串参数
    native void passString(String str);

    // 本地方法：传递字符串数组参数
    native void passStringArray(String[] array);
}
```

## 添加C++源代码
接下来我们在当前模块的 `src/main` 目录下创建一个 `cpp` 目录，用于存放C++源代码，此时模块目录结构如下文代码块所示：

```text
Module_NDK
├── .cxx
├── build
├── src
│   └── main
│       ├── cpp
│       │   ├── CMakeLists.txt
│       │   └── jni.cpp
│       ├── java
│       │   └── Java源代码...
│       └── res
│           └── 资源文件...
└────────── build.gradle
```

`.cxx` 目录和 `build` 目录类似，用于存放C++编译过程中产生的临时文件，我们可以在版本控制工具中忽略该目录。

`cpp` 目录用于存放C++源代码和CMake配置文件，我们首先创建一个 `jni.cpp` 文件实现JNIClass中的本地方法。

"jni.cpp":

```cpp
#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>

// 当前C++源代码对应的Java类名
#define CLASS_NAME "net/bi4vmr/study/base/JNIClass"

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL passBasicTypes(JNIEnv *env, jobject instance, jboolean b, jint i, jdouble d) {
    // 此处省略具体实现，详见示例程序。
}

JNIEXPORT void JNICALL passString(JNIEnv *env, jobject instance, jstring str) {
    // 此处省略具体实现，详见示例程序。
}

JNIEXPORT void JNICALL passStringArray(JNIEnv *env, jobject instance, jobjectArray stringArray) {
    // 此处省略具体实现，详见示例程序。
}

// 方法映射表
static const JNINativeMethod jniNativeMethods[] = {
        {"passBasicTypes",  "(ZID)V",                 (void *) (passBasicTypes)},
        {"passString",      "(Ljava/lang/String;)V",  (void *) (passString)},
        {"passStringArray", "([Ljava/lang/String;)V", (void *) (passStringArray)},
};

// 注册JNI方法
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    // 此处省略具体实现，详见示例程序。
}

#ifdef __cplusplus
}
#endif
```

## 添加编译配置
源代码编写完毕后，我们还需要添加编译配置，实现C++编译器与Gradle的联动。

我们首先编写CMake配置文件，指定动态链接库的相关编译参数。CMake中常见的配置项可参考下文代码块中的注释。

"CMakeLists.txt":

```cmake
# 指定该工程可使用的最低CMake版本
cmake_minimum_required(VERSION 3.22.1)

# 指定工程名称
project("jnitest")

# 编译指令，生成名称为"libjnitest.so"的构建产物。
add_library(
        # 库文件名称，此处引用的变量为"project()"指令的值"jnitest"。
        ${CMAKE_PROJECT_NAME}
        # 库文件类型。
        # "SHARED"为动态库；"STATIC"为静态库。
        SHARED
        # 列出该库包含的所有源代码文件
        jni.cpp)

# 指定构建产物的外部依赖
target_link_libraries(
        # 声明该配置需要对何构建产物生效，此处为"jnitest"。
        ${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        # 列出依赖组件名称
        android
        log)
```

最后我们在当前模块的Gradle配置文件中添加以下内容，声明该模块需要触发C++编译，并指明所使用的CMake配置文件。

"build.gradle":

```groovy
android {
    // C语言编译配置
    externalNativeBuild {
        cmake {
            // 指定CMake配置文件
            path file('src/main/cpp/CMakeLists.txt')
            // 指定CMake版本
            version '3.22.1'
        }
    }
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
android {
    // C语言编译配置
    externalNativeBuild {
        cmake {
            // 指定CMake配置文件
            path = File("src/main/cpp/CMakeLists.txt")
            // 指定CMake版本
            version = "3.22.1"
        }
    }
}
```

此时我们执行一次Gradle Sync任务，当前模块的NDK就配置完成了。当我们编译该模块时，Clang将会编译C++源代码生成库文件，然后由Gradle打包至APK或AAR中。

# Gradle配置
## 指定NDK版本
每个AGP版本都有对应的默认NDK版本，相关信息可在该网页中查询： [🔗 AGP的默认NDK版本](https://developer.android.google.cn/studio/projects/install-ndk?hl=zh-cn#default-ndk-per-agp) 。

如果我们需要使用特定的NDK版本，可以在模块的 `build.gradle` 配置文件中添加以下配置项，覆盖默认版本。

"build.gradle":

```groovy
android {
    ndkVersion = "24.0.8215888"
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
android {
    ndkVersion = "24.0.8215888"
}
```

## 指定启用的ABI
JNI本身支持多种硬件平台，默认情况下，NDK会分别编译"x86"、"x86_64"、"armeabi-v7a"和"arm64-v8a"四种ABI，并将生成的库文件全部打包至APK中。

我们通常不会适配全部的四种ABI，只会在工程中添加特定目标平台的第三方依赖库；此时我们需要禁用未适配的ABI，避免它们因缺少第三方依赖库而导致编译失败。

假如我们有一个仅适配64位平台的NDK工程，则需要添加以下Gradle配置，禁用32位平台的NDK构建。

"build.gradle":

```groovy
android {
    defaultConfig {
        // NDK配置
        ndk {
            /*
             * 声明需要使用的ABI名称
             *
             * 未在此处出现的ABI都将被禁用。
             */
            abiFilters("arm64-v8a", "x86_64")
        }
    }
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
android {
    defaultConfig {
        // NDK配置
        ndk {
            /*
             * 声明需要使用的ABI名称
             *
             * 未在此处出现的ABI都将被禁用。
             */
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86_64")
        }
    }
}
```

`ndk {}` 小节也可以配合Build Types或Product Flavors使用，以便适应更多的场景。

## 修改第三方依赖库目录
默认情况下，第三方依赖库文件应当放置在 `<SourceSet>/jniLibs/` 目录中，如果我们希望使用其他目录，可以添加以下Gradle配置：

"build.gradle":

```groovy
android {
    sourceSets {
        // 此处以"main"为例，也可以在其他SourceSet中使用。
        main {
            // 添加第三方依赖库目录"<SourceSet>/sdks/"
            jniLibs.srcDirs = ["sdks"]
        }
    }
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
android {
    sourceSets {
        // 此处以"main"为例，也可以在其他SourceSet中使用。
        getByName("main") {
            // 添加第三方依赖库目录"<SourceSet>/sdks/"
            jniLibs.srcDir("sdks")
        }
    }
}
```

该属性的值为数组，允许添加多个目录；上述配置添加完成之后，我们还应当同步修改CMake中的相关路径。

## 自动清理C语言构建缓存
默认情况下，Gradle的Clean任务只会清理Java和Kotlin的构建缓存文件，我们可以在模块的 `build.gradle` 文件中添加以下配置，在执行Clean任务时同步清理C/C++构建缓存文件。

"build.gradle":

```groovy
gradle.projectsEvaluated {
    tasks.withType(Delete.class).tap {
        configureEach {
            // 清除C++构建缓存文件
            def cppCacheDir = getProjectDir().absolutePath + File.separator + ".cxx"
            delete(cppCacheDir)
        }
    }
}
```

上述内容也可以使用Kotlin语言编写：

"build.gradle.kts":

```kotlin
gradle.projectsEvaluated {
    tasks.withType(Delete::class.java) {
        // 清除C++构建缓存文件
        delete("${projectDir.absolutePath}${File.separator}.cxx")
    }
}
```

# 疑难解答
## 索引

<div align="center">

|       序号        |                          摘要                          |
| :---------------: | :----------------------------------------------------: |
| [案例一](#案例一) | 新增预装应用后，打开应用时提示“找不到动态链接库文件”。 |

</div>

## 案例一
### 问题描述
在系统内新增预装应用后，打开应用时提示“找不到动态链接库文件”。

### 问题分析
预置应用程序跳过了安装时复制库文件的步骤，虽然APK中包含库文件，但运行时系统仍然无法找到它们。

详见相关章节： [🧭 预置应用库文件](#预置应用库文件) 。

### 解决方案
我们在调试时需要手动将库文件复制到系统公共库文件目录或APK所在目录，调试完毕后还要修改系统编译脚本，确保打包时将库文件添加到对应的目录中。

详见相关章节： [🧭 预置应用库文件](#预置应用库文件) 。
