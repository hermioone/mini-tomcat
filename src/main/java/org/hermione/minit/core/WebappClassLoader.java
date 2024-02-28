package org.hermione.minit.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 每一个 Context 都对应一个单独的 WebappClassLoader
 * 在 Java 中，不同 classloader 加载的类在 JVM 看来是两个不同的类，因为在 JVM 里一个类的唯一标识是 classloader+ 类名。通过这种方式我们就能够实现类之间的隔离，甚至可以同时加载某个类的两个不同版本。
 *
 * WebappClassLoader 的 parent 是 CommonClassLoader，WebappClassLoader 和 CommonClassLoader 都打破了双亲委托模型（ delegate = false），都是先由自己加载，自己加载不到的情况下再交给 parent 加载
 * WebappClassLoader 和 CommonClassLoader 的不同之处是：
 *  - WebappClassLoader 加载的是 {TOMCAT_HOME}/webapps/{app}/WEB-INF/classes 下的类
 *  - CommonClassLoader 加载的是 {TOMCAT_HOME}/lib 目录下的类
 */
@Slf4j
public class WebappClassLoader extends URLClassLoader {

    @Getter
    @Setter
    protected boolean delegate = false;

    /**
     * 就是 CommonClassLoader
     */
    private ClassLoader parent = null;

    /**
     * 系统内置的类加载器，包括：app, ext和根
     */
    private ClassLoader system = null;
    public WebappClassLoader() {
        super(new URL[0]);
        this.parent = getParent();
        system = getSystemClassLoader();
    }
    public WebappClassLoader(URL[] urls) {
        super(urls);
        this.parent = getParent();
        system = getSystemClassLoader();
    }
    public WebappClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }
    public WebappClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        clazz = super.findClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        // Return the class we have located
        return (clazz);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return (loadClass(name, false));
    }

    /**
     * 核心方法，按照自定义的加载次序加载类
     *  1. 尝试用系统的 ClassLoader 去加载某个类，防止覆盖 Java 自身的类
     *  2. 如果是 delegate 模式（Java 类加载机制的标准模式），就由 parent 去加载这个类，随后再试着自己加载类
     *  3. 如果不是 delegate 模式，先自己加载类，失败了再用 parent 加载，如果 parent 为空，就用 system 加载。
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            //首先是用系统类加载器加载类
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException ignored) {
        }

        boolean delegateLoad = delegate;
        //到了这里，系统类加载器加载不成功，则判断是否为双亲委托模式，
        if (delegateLoad) {
            // 如果是双亲委托模式，则用 parent 来加载器来加载
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        //到了这里，或者是父类加载器加载不成功，或者是不支持双亲委托模式，
        //所以要自己去加载类
        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException ignored) {
        }
        //到这里，自己加载不成功，则反过来交给父类加载器去加载
        if (!delegateLoad) {
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(name);
    }
    private void log(String message) {
        log.info("WebappClassLoader: {}", message);
    }
    private void log(String message, Throwable throwable) {
        log.error("WebappClassLoader: {}, {}", message, ExceptionUtils.getStackTrace(throwable));
    }
}
