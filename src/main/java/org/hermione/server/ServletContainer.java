package org.hermione.server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

//Servlet容器
@Slf4j
public class ServletContainer {
    @Getter
    HttpConnector connector = null;
    @Getter
    ClassLoader loader = null;
    //包含servlet类和实例的map
    Map<String, String> servletClsMap = new ConcurrentHashMap<>(); //servletName - ServletClassName
    Map<String, ServletWrapper> servletInstanceMap = new ConcurrentHashMap<>();//servletName - servlet

    public ServletContainer() {
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(HttpServer.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
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

    public String getName() {
        return null;
    }

    public void setName(String name) {
    }

    //invoke方法用于从map中找到相关的servlet，然后调用
    public void invoke(HttpRequest request, HttpResponse response)
            throws ServletException {
        ServletWrapper servletWrapper = null;
        ClassLoader loader = getLoader();
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        servletWrapper = servletInstanceMap.get(servletName);
        //如果容器内没有这个servlet，先要load类，创建新实例
        if (servletWrapper == null) {
            servletWrapper = new ServletWrapper(servletName,this);
            servletWrapper.setParent(this);
            servletClsMap.put(servletName, servletName);
            servletInstanceMap.put(servletName, servletWrapper);
        }
        //然后调用service()
        try {
            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
            System.out.println("Call service()");
            servletWrapper.invoke(requestFacade, responseFacade);
        } catch (Throwable e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
