package org.hermione.minit.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.Context;
import org.hermione.minit.Wrapper;
import org.hermione.minit.connector.http.HttpConnector;
import org.hermione.minit.connector.HttpRequestFacade;
import org.hermione.minit.connector.http.HttpRequestImpl;
import org.hermione.minit.connector.HttpResponseFacade;
import org.hermione.minit.startup.Bootstrap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//Servlet容器，原来的 ServletContainer
@Slf4j
public class StandardContext extends ContainerBase implements Context {
    @Getter
    HttpConnector connector = null;
    @Getter
    ClassLoader loader = null;
    //包含servlet类和实例的map
    Map<String, String> servletClsMap = new ConcurrentHashMap<>(); //servletName - ServletClassName
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();//servletName - servlet

    public StandardContext() {
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(Bootstrap.WEB_ROOT);
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
    @Override
    public void invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        StandardWrapper servletWrapper = null;
        String uri = ((HttpRequestImpl)request).getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        //从容器中获取servlet wrapper
        servletWrapper = servletInstanceMap.get(servletName);
        if ( servletWrapper == null) {
            servletWrapper = new StandardWrapper(servletName,this);
            //servletWrapper.setParent(this);
            this.servletClsMap.put(servletName, servletName);
            this.servletInstanceMap.put(servletName, servletWrapper);
        }
        //将调用传递到下层容器即wrapper中
        try {
            HttpServletRequest requestFacade = new HttpRequestFacade(request);
            HttpServletResponse responseFacade = new HttpResponseFacade(response);
            System.out.println("Call service()");
            servletWrapper.invoke(requestFacade, responseFacade);
        }
        catch (Throwable e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setDisplayName(String displayName) {

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
