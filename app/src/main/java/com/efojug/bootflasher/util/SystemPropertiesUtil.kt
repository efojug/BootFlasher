package com.efojug.bootflasher.util


object SystemPropertiesUtil {
    private const val CLASS_NAME = "android.os.SystemProperties"

    @JvmStatic
    fun getProperty(key: String, defaultValue: String = ""): String {
        var value = defaultValue

        try {
            val c = Class.forName(CLASS_NAME)
            val get = c.getMethod("get", String::class.java, String::class.java)
            value = get.invoke(c, key, defaultValue) as String
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return value
    }
}