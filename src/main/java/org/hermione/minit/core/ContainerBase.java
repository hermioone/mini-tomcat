package org.hermione.minit.core;

import lombok.Getter;
import lombok.Setter;
import org.hermione.minit.Container;
import org.hermione.minit.Loader;
import org.hermione.minit.Logger;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.Valve;
import org.hermione.minit.logger.SystemOutLogger;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ContainerBase implements Container, Pipeline {
    //子容器
    protected final Map<String, Container> children = new ConcurrentHashMap<>();
    //类加载器
    protected Loader loader = null;
    @Getter
    protected String name = null;
    //父容器
    protected Container parent = null;

    protected String path;
    protected String docbase;

    protected ContainerBase(String name) {
        this.name = name;
    }

    //下面是基本的get和set方法
    public abstract String getInfo();

    @Override
    public Loader getLoader() {
        if (loader != null)
            return (loader);
        if (parent != null)
            return (parent.getLoader());
        return (null);
    }

    @Override
    public synchronized void setLoader(Loader loader) {
        loader.setPath(path);
        loader.setDocbase(docbase);
        loader.setContainer(this);
        Loader oldLoader = this.loader;
        if (oldLoader == loader)
            return;
        this.loader = loader;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Container getParent() {
        return (parent);
    }

    @Override
    public void setParent(Container container) {
        Container oldParent = this.parent;
        this.parent = container;
    }

    //下面是对children map的增删改查操作
    @Override
    public void addChild(Container child) {
        addChildInternal(child);
    }

    private void addChildInternal(Container child) {
        synchronized (children) {
            if (children.get(child.getName()) != null)
                throw new IllegalArgumentException("addChild:  Child name '" +
                        child.getName() +
                        "' is not unique");
            child.setParent((Container) this);
            children.put(child.getName(), child);
        }
    }

    @Override
    public Container findChild(String name) {
        if (name == null)
            return (null);
        synchronized (children) {       // Required by post-start changes
            return ((Container) children.get(name));
        }
    }

    @Override
    public Container[] findChildren() {
        synchronized (children) {
            Container[] results = new Container[children.size()];
            return ((Container[]) children.values().toArray(results));
        }
    }

    @Override
    public void removeChild(Container child) {
        synchronized (children) {
            if (children.get(child.getName()) == null)
                return;
            children.remove(child.getName());
        }
        child.setParent(null);
    }

    //ContainerBase中增加与日志相关的代码
    protected Logger logger = new SystemOutLogger();

    @Override
    public Logger getLogger() {
        return Objects.requireNonNull(logger);
    }

    @Override
    public synchronized void setLogger(Logger logger) {
        Logger oldLogger = this.logger;
        if (oldLogger == logger) return;
        this.logger = logger;
    }

    protected void log(String message) {
        Logger logger = getLogger();
        logger.log(logName() + ": " + message);
    }

    protected void log(String message, Throwable throwable) {
        Logger logger = getLogger();
        logger.log(logName() + ": " + message, throwable);
    }

    protected String logName() {
        String className = this.getClass().getName();
        int period = className.lastIndexOf(".");
        if (period >= 0) {
            className = className.substring(period + 1);
        }
        return (className + "[" + getName() + "]");
    }

    @Getter
    private final Pipeline pipeline = new StandardPipeline(this); //增加pipeline支持

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        pipeline.invoke(request, response);
    }

    @Override
    public synchronized void addValve(Valve valve) {
        pipeline.addValve(valve);
    }

    @Override
    public Valve getBasic() {
        return pipeline.getBasic();
    }

    @Override
    public synchronized void removeValve(Valve valve) {
        pipeline.removeValve(valve);
    }

    @Override
    public void setBasic(Valve valve) {
        pipeline.setBasic(valve);
    }

    @Override
    public Container getContainer() {
        return this;
    }
}
