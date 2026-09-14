<!-- TODO




# 选择壁纸

adb shell am start -a android.intent.action.SET_WALLPAPER


#重置用户壁纸
rm -f /data/system/users/0/wallpaper*



# 代码设置壁纸（系统应用，无引导）
val targetService = ComponentName("", "Service")
setWallpaperComponentSilently(this, targetService)



fun setWallpaperComponentSilently(context: Context, componentName: ComponentName): Boolean {
    val wallpaperManager = WallpaperManager.getInstance(context)
    return try {
        val method = wallpaperManager.javaClass.getMethod(
            "setWallpaperComponent",
            ComponentName::class.java
        )
        method.invoke(wallpaperManager, componentName)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

# 普通应用，打开壁纸选择器

fun applyCustomWallpaper(context: Context) {
    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
        putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context.packageName, MyWallpaperService::class.java.name)
        )
        // 标记在 Activity 外部启动
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}


config_enableWallpaperService

07-20 22:36:10.812679  1627  1627 I SystemServer: Wallpaper service disabled by config

需要该配置项改为true，否则不显示壁纸


配置默认壁纸：

预置你的 APK： 将你的 3D 壁纸 APK 编译或预置到系统的 /system/app/ 或 /system/priv-app/ 目录下。

修改 framework 配置： 找到 Android 源码中的配置文件 config.xml。
路径通常为：frameworks/base/core/res/res/values/config.xml

设置默认组件名： 找到 <string name="default_wallpaper_component"> 这一项。将其修改为你的壁纸服务的 ComponentName（格式为 包名/类名）。

wallpaper及其关联的服务需要directbootaware/defaultToDeviceProtectedStorage，否则壁纸启动时加密分区未解锁，会出现错误：

07-22 20:49:42.956818  1679  1679 V WallpaperManagerService: bindWallpaperComponentLocked: componentName=ComponentInfo{-/Service}
07-22 20:49:42.956849  1679  1679 W WallpaperManagerService: Attempted wallpaper ComponentInfo{-/Service} is unavailable




-->
