package com.pateo.sdk.common.privacymonitor.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import java.lang.reflect.Method

/**
 * 应用程序信息查询工具。
 *
 * @author bi4vmr@outlook.com
 */
class AppInfoHelper private constructor(mContext: Context) {

    companion object {
        // 本实例的生命周期跟随整个进程，不会导致内存泄露，因此可以忽略警告。
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AppInfoHelper? = null

        @JvmStatic
        fun getInstance(context: Context): AppInfoHelper {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AppInfoHelper(context.applicationContext)
                    }
                }
            }
            return instance!!
        }

        private val TAG = "${PrivacyLog.TAG_PREFIX}${AppInfoHelper::class.java.simpleName}"
    }

    private val packageManager: PackageManager = mContext.packageManager

    /**
     * 获取应用标签。
     *
     * @param[packageName] 目标应用程序包名。
     * @return 标签。若查询失败，则返回空值。
     */
    fun getLabel(packageName: String): String? {
        val cSeq: CharSequence? = getApplicationInfo(packageName)?.loadLabel(packageManager)
        return cSeq?.toString()
    }

    /**
     * 获取应用图标。
     *
     * @param[packageName] 目标应用程序包名。
     * @return 图标。若查询失败，则返回空值。
     */
    fun getIcon(packageName: String): Drawable? {
        val drawable: Drawable? = getApplicationInfo(packageName)?.loadIcon(packageManager)
        return drawable
    }

    /**
     * 判断应用是否为系统应用。
     *
     * @param[packageName] 待查询的应用程序包名。
     * @return "true"表示是系统应用，"false"表示不是系统应用。
     */
    fun isSystemApp(packageName: String): Boolean {
        val flags: Int = getApplicationInfo(packageName)?.flags ?: 0
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    /**
     * 获取应用信息。
     *
     * @param[packageName] 待查询的应用程序包名。
     * @return ApplicationInfo对象。若包名对应的应用不存在则返回空值。
     */
    private fun getApplicationInfo(packageName: String): ApplicationInfo? {
        return try {
            val methodGetCurrentUserID: Method = ActivityManager::class.java.getMethod("getCurrentUser")
            val userID: Int = methodGetCurrentUserID.invoke(null) as? Int ?: 0

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val method: Method = PackageManager::class.java.getMethod(
                    "getApplicationInfoAsUser",
                    String::class.java,
                    PackageManager.ApplicationInfoFlags::class.java,
                    Int::class.java
                )
                method.invoke(
                    packageManager,
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0L),
                    userID
                ) as? ApplicationInfo
            } else {
                val method: Method = PackageManager::class.java.getMethod(
                    "getApplicationInfoAsUser",
                    String::class.java,
                    Int::class.java,
                    Int::class.java
                )
                method.invoke(packageManager, packageName, 0, userID) as? ApplicationInfo
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get application info failed! Reason:[${e.message}]")
            e.printStackTrace()
            null
        }
    }
}
