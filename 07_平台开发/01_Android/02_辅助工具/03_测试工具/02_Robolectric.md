# 简介
https://developer.android.google.cn/training/testing/local-tests/robolectric?hl=zh-cn

    // Robolectric
    testOptions {
        // 加载Android资源
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("robolectric.logging.enabled", true)
        }
    }
