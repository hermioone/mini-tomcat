package org.hermione.minit.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.Container;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

/**
 * 每一个 Context 都对应一个单独的 WebappClassLoader
 * 在 Java 中，不同 classloader 加载的类在 JVM 看来是两个不同的类，因为在 JVM 里一个类的唯一标识是 classloader+ 类名。通过这种方式我们就能够实现类之间的隔离，甚至可以同时加载某个类的两个不同版本。
 */
@Getter
@Slf4j
public class WebappClassLoader {

    ClassLoader classLoader;

    /**
     * Context 目录，比如 app1、app2
     */
    @Setter
    String docbase;

    @Setter
    Container container;

    public WebappClassLoader() {
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
        log.info("Starting WebappLoader");
        try {
            // 创建一个 URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            // 整个服务器的根工作目录存放在 System.getProperty("minit.base") 里，这个 property 是 BootStrap 启动时指定的，
            // 所以在 BootStrap 中我们要定义 System.setProperty("minit.base", WEB_ROOT);
            // 举个例子，如果 Minit 放在 d:/minit 目录下，那么 WEB_ROOT 目录为 d:/minit/webroot，而 app1 的 webclassloader 的 docbase 是 app1, 那么它加载的目录就是 d:/minit/webroot/app1/
            File classPath = new File(System.getProperty("minit.base"));
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            if (docbase != null && !docbase.isEmpty()) {
                repository = repository + docbase + File.separator;
            }
            urls[0] = new URL(null, repository, streamHandler);
            log.info("Webapp classloader Repository : {}", repository);
            classLoader = new URLClassLoader(urls);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void stop() {
    }
}
