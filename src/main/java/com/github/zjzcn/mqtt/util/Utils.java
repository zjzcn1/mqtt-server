package com.github.zjzcn.mqtt.util;

import com.github.zjzcn.mqtt.store.MemorySessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(MemorySessionStore.class);

    public static String getRootPath() {
        URL url = Utils.class.getProtectionDomain().getCodeSource().getLocation();
        String rootPath;
        try {
            rootPath = URLDecoder.decode(url.getPath(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        if(rootPath.endsWith(".jar")) {
            rootPath = rootPath.substring(0, rootPath.lastIndexOf("/lib/") + 1);
        } else {
            rootPath = rootPath.substring(0, rootPath.lastIndexOf("/target/classes/") + 1);
        }
        return rootPath;
    }
    public static <T> Class<T> forClass(String className) throws ClassNotFoundException {
        return (Class<T>)Class.forName(className);
    }

    public static <T> T forObject(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            log.error("Cannot find constructor class={}", clazz.getName(), e);
            return null;
        }
    }

    public static String uuid(){
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }
}
