package org.hermione.minit.loader;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.Container;
import org.hermione.minit.Loader;
import org.hermione.minit.loader.WebappClassLoader;

import java.io.File;
import java.net.URL;
import java.net.URLStreamHandler;

/**
 * 从应用的 WEB-INF/classes 目录下加载类
 */
@Slf4j
public class WebappLoader implements Loader {
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
    @Setter
    Container container;

    public WebappLoader(String docbase) {
        this.docbase = docbase;
    }

    public WebappLoader(String docbase, ClassLoader parent) {
        this.docbase = docbase;
        this.parent = parent;
    }


    public String getInfo() {
        return "A simple loader: " + docbase;
    }

    public void addRepository(String repository) {
    }

    public String[] findRepositories() {
        return null;
    }

    public synchronized void start() {
        log.info("Starting WebappLoader");
        try {
            // create a URLClassLoader
            //加载目录是minit.base规定的根目录，加上应用目录，
            //然后之下的WEB-INF/classes目录
            //这意味着每一个应用有自己的类加载器，达到隔离的目的
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(System.getProperty("minit.base"));
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            if (docbase != null && !docbase.isEmpty()) {
                repository = repository + docbase + File.separator;
            }
            repository = repository + "WEB-INF" + File.separator + "classes" + File.separator;
            urls[0] = new URL(null, repository, streamHandler);
            log.info("Webapp classloader Repository: {}", repository);
            classLoader = new WebappClassLoader(urls, parent);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void stop() {
    }
}
