package org.hermione.minit.loader;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.Container;
import org.hermione.minit.Loader;

import java.io.File;
import java.net.URL;
import java.net.URLStreamHandler;

/**
 * 加载 tomcat 的 lib 目录
 */
@Slf4j
public class CommonLoader implements Loader {
    @Getter
    ClassLoader classLoader;
    ClassLoader parent;
    @Getter
    @Setter
    String path;
    @Getter
    @Setter
    String docbase;
    @Getter
    Container container;

    public CommonLoader() {
    }

    public CommonLoader(ClassLoader parent) {
        this.parent = parent;
    }

    public void setContainer(Container container) {
        this.container = container;
    }


    public String getInfo() {
        return "A simple loader";
    }

    public void addRepository(String repository) {
    }

    public String[] findRepositories() {
        return null;
    }

    public synchronized void start() {
        System.out.println("Starting Common Loader, docbase: " + docbase);
        try {
            // 创建一个URLClassLoader
            //类加载目录是minit安装目录下的lib目录
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(System.getProperty("minit.home"));
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            repository = repository + "lib" + File.separator;
            urls[0] = new URL(null, repository, streamHandler);
            log.info("Common classloader Repository: {}", repository);
            classLoader = new CommonClassLoader(urls);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void stop() {
    }
}
