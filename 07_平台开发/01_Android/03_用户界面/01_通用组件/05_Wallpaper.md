<!-- TODO




# 选择壁纸

adb shell am start -a android.intent.action.SET_WALLPAPER


#重置用户壁纸
rm -f /data/system/users/0/wallpaper*

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





config_enableWallpaperService

07-20 22:36:10.812679  1627  1627 I SystemServer: Wallpaper service disabled by config

需要该配置项改为true，否则不显示壁纸


default_wallpaper_component

配置默认壁纸，关联服务需要directbootaware/defaultToDeviceProtectedStorage，否则壁纸启动时加密分区未解锁，会出现

07-22 20:49:42.956818  1679  1679 V WallpaperManagerService: bindWallpaperComponentLocked: componentName=ComponentInfo{-/Service}
07-22 20:49:42.956849  1679  1679 W WallpaperManagerService: Attempted wallpaper ComponentInfo{-/Service} is unavailable




-->
