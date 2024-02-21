package org.hermione.minit.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.Container;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.Wrapper;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Objects;

// 原来的 ServletWrapper，主要提供 Servlet 生命周期管理
@SuppressWarnings({"UnusedReturnValue", "deprecation"})
@Slf4j
public class StandardWrapper extends ContainerBase implements Wrapper {
    private Servlet instance = null;
    @Getter
    @Setter
    private String servletClass;

    public StandardWrapper(String servletClass, StandardContext parent) {

        super("StandardWrapper");
        Pipeline pipeline = getPipeline();
        pipeline.setBasic(new StandardWrapperValve(pipeline));
        this.parent = parent;
        this.servletClass = servletClass;
        try {
            loadServlet();
        } catch (ServletException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public String getInfo() {
        return "Mini Servlet Wrapper, version 0.1";
    }


    public Servlet getServlet() {
        return this.instance;
    }

    private Servlet loadServlet() throws ServletException {
        if (instance != null)
            return instance;
        Servlet servlet = null;
        String actualClass = servletClass;
        if (actualClass == null) {
            throw new ServletException("servlet class has not been specified");
        }
        ClassLoader classLoader = getLoader();
        Class<?> classClass = null;
        try {
            if (classLoader != null) {
                classClass = classLoader.loadClass(actualClass);
            }
        } catch (ClassNotFoundException e) {
            throw new ServletException("Servlet class not found");
        }
        try {
            servlet = (Servlet) Objects.requireNonNull(classClass).newInstance();
        } catch (Throwable e) {
            throw new ServletException("Failed to instantiate servlet");
        }
        try {
            servlet.init(null);
        } catch (Throwable f) {
            throw new ServletException("Failed initialize servlet.");
        }
        instance = servlet;
        return servlet;
    }

    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        log.info(getName() + " invoke()");
        super.invoke(request, response);
    }

    @Override
    public void addChild(Container child) {
    }

    @Override
    public Container findChild(String name) {
        return null;
    }

    @Override
    public Container[] findChildren() {
        return null;
    }

    @Override
    public void removeChild(Container child) {
    }

    @Override
    public int getLoadOnStartup() {
        return 0;
    }

    @Override
    public void setLoadOnStartup(int value) {

    }

    @Override
    public void addInitParameter(String name, String value) {

    }

    @Override
    public Servlet allocate() throws ServletException {
        return null;
    }

    @Override
    public String findInitParameter(String name) {
        return null;
    }

    @Override
    public String[] findInitParameters() {
        return new String[0];
    }

    @Override
    public void load() throws ServletException {

    }

    @Override
    public void removeInitParameter(String name) {

    }
}
