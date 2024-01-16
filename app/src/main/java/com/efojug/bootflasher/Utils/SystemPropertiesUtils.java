package com.efojug.bootflasher.Utils;

import java.lang.reflect.Method;

public final class SystemPropertiesUtils {
    private static final String CLASS_NAME = "android.os.SystemProperties";
    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try{
            Class<?> c = Class.forName(CLASS_NAME);
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
