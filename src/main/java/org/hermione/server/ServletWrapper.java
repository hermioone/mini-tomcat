package org.hermione.server;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@SuppressWarnings({"UnusedReturnValue", "deprecation"})
@Slf4j
public class ServletWrapper {
    private Servlet instance = null;
    @Getter
    @Setter
    private String servletClass;
    private ClassLoader loader;
    private String name;
    @Getter
    @Setter
    protected ServletContainer parent = null;

    public ServletWrapper(String servletClass, ServletContainer parent) {
        this.parent = parent;
        this.servletClass = servletClass;
        try {
            loadServlet();
        } catch (ServletException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public ClassLoader getLoader() {
        if (loader != null)
            return loader;
        return parent.getLoader();
    }


    public Servlet getServlet() {
        return this.instance;
    }

    public Servlet loadServlet() throws ServletException {
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

    public void invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (instance != null) {
            instance.service(request, response);
        }
    }
}
