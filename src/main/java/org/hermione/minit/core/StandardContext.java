package org.hermione.minit.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hermione.minit.Container;
import org.hermione.minit.Context;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.Wrapper;
import org.hermione.minit.connector.http.HttpConnector;
import org.hermione.minit.startup.Bootstrap;
import org.hermione.minit.valves.AuthorityCheckValve;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
//Servlet容器，原来的 ServletContainer
public class StandardContext extends ContainerBase implements Context {
    @Getter
    HttpConnector connector = null;
    @Getter
    ClassLoader loader = null;
    //包含servlet类和实例的map
    Map<String, String> servletClsMap = new ConcurrentHashMap<>(); //servletName - ServletClassName
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();//servletName - servlet

    public StandardContext() {
        super("StandardContext");
        Pipeline pipeline = getPipeline();
        pipeline.addValve(new AuthorityCheckValve(pipeline));
        pipeline.setBasic(new StandardContextValve(pipeline));
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(Bootstrap.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            log("Fail to init StandardContext.", e);
        }
        log("Container created.");
    }

    public Wrapper getWrapper(String name) {
        StandardWrapper servletWrapper = servletInstanceMap.get(name);
        if (servletWrapper == null) {
            servletWrapper = new StandardWrapper(name, this);
            this.servletClsMap.put(name, name);
            this.servletInstanceMap.put(name, servletWrapper);
        }
        return servletWrapper;
    }

    public String getInfo() {
        return null;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    //invoke方法用于从map中找到相关的servlet，然后调用
    @Override
    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        log.info(getName() + " invoke()");
        super.invoke(request, response);
    }



    @Override
    public String getDocBase() {
        return null;
    }

    @Override
    public void setDocBase(String docBase) {

    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public StandardContext getServletContext() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int timeout) {

    }

    @Override
    public String getWrapperClass() {
        return null;
    }

    @Override
    public void setWrapperClass(String wrapperClass) {

    }

    @Override
    public Wrapper createWrapper() {
        return null;
    }

    @Override
    public String findServletMapping(String pattern) {
        return null;
    }

    @Override
    public String[] findServletMappings() {
        return new String[0];
    }

    @Override
    public void reload() {

    }
}
