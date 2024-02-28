package org.hermione.minit.core;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.net.URLClassLoader;

@SuppressWarnings("DuplicatedCode")
@Slf4j
public class CommonClassLoader extends URLClassLoader {
    protected boolean delegate = false;
    private ClassLoader parent = null;

    /**
     * 系统内置的类加载器，包括：app, ext和根
     */
    private ClassLoader system = null;

    public CommonClassLoader() {
        super(new URL[0]);
        this.parent = getParent();
        system = getSystemClassLoader();
    }

    public CommonClassLoader(URL[] urls) {
        super(urls);
        this.parent = getParent();
        system = getSystemClassLoader();
    }

    public CommonClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }

    public CommonClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }

    public boolean getDelegate() {
        return (this.delegate);
    }

    public void setDelegate(boolean delegate) {
        this.delegate = delegate;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = super.findClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        // 返回我们定位的类
        return (clazz);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return (loadClass(name, false));
    }

    //加载类，注意加载次序，这个方法同时考虑了双亲委托模式
    public Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        Class<?> clazz = null;
        // 先是尝试使用系统类加载器加载类，以防止Web应用程序覆盖J2SE类
        try {
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return clazz;
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        boolean delegateLoad = delegate;
        // 到这里，系统类加载器不能加载，就判断是不是委托代理模式，将其委托给父类
        if (delegateLoad) {
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
        // 到这里，搜索本地存储库，自己加载
        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException ignored) {
        }
        // (3) 到了这里，自己加载不了，就委托给父类
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
        // 该类未找到
        throw new ClassNotFoundException(name);
    }
}
