package org.hermione.minit.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.ContainerEvent;
import org.hermione.minit.ContainerListener;
import org.hermione.minit.Loader;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.connector.http.HttpConnector;
import org.hermione.minit.loader.WebappLoader;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
@Slf4j
public class StandardHost extends ContainerBase {
    @Getter
    @Setter
    HttpConnector connector = null;
    //host中用一个map存储了所管理的 context，一个 context 代表了一个独立的 web 应用
    Map<String, StandardContext> contextMap = new ConcurrentHashMap<>();//contextName - servletContext
    //下面的listener是host本身的监听
    private final ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();
    private final ArrayList<ContainerListener> listeners = new ArrayList<>();

    public StandardHost() {
        super("StandardHost");
        Pipeline pipeline = getPipeline();
        pipeline.setBasic(new StandardHostValve(pipeline, "StandardHostValve"));
        log("Host created.");
    }

    public String getInfo() {
        return "Minit host, vesion 0.1";
    }

    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        System.out.println("StandardHost invoke()");
        super.invoke(request, response);
    }

    //从host中根据context名(路径名)找到对应的context
    //如果找不到就新建一个context
    public StandardContext getContext(String name) {
        StandardContext context = contextMap.get(name);
        if (context == null) {
            log.info("loading context: {}", name);
            //创建新的context，有自己独立的根目录和类加载器
            Loader loader = new WebappLoader(name, this.getLoader().getClassLoader());
            context = new StandardContext();
            context.setDocBase(name);
            context.setConnector(connector);
            context.setLoader(loader);
            loader.start();
            context.start();
            this.contextMap.put(name, context);
        }
        return context;
    }

    //host的启动方法，现在没有做什么事情，仅仅是启用监听器
    //在MiniTomcat中，Host是一个极简化的形态
    public void start() {
        fireContainerEvent("Host Started", this);
        ContainerListenerDef listenerDef = new ContainerListenerDef();
        listenerDef.setListenerName("TestListener");
        listenerDef.setListenerClass("app1.TestListener");
        // addListenerDef(listenerDef);
        listenerStart();

        // 在启动时加载 /webapps目录下 所有的上下文（Context）
        // minit.base 就是应用的基础目录，比如 webapps，我们认为它下面的每个子目录都代表了一个不同的应用。这个属性是在 BootStrap 里设置的
        File classPath = new File(System.getProperty("minit.base"));
        String[] dirs = Objects.requireNonNull(classPath.list());
        for (String dir : dirs) {
            getContext(dir);
        }
    }

    private void initListeners() {

    }

    public void addContainerListener(ContainerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeContainerListener(ContainerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void fireContainerEvent(String type, Object data) {
        if (listeners.isEmpty())
            return;
        ContainerEvent event = new ContainerEvent(this, type, data);
        ContainerListener[] list = new ContainerListener[0];
        synchronized (listeners) {
            list = (ContainerListener[]) listeners.toArray(list);
        }
        for (ContainerListener containerListener : list) {
            containerListener.containerEvent(event);
        }
    }

    public void addListenerDef(ContainerListenerDef listenererDef) {
        synchronized (listenerDefs) {
            listenerDefs.add(listenererDef);
        }
    }

    //初始化监听器
    public void listenerStart() {
        log.info("Listener Start..........");
        synchronized (listeners) {
            listeners.clear();
            for (ContainerListenerDef def : listenerDefs) {
                ContainerListener listener = null;
                try {
                    // Identify the class loader we will be using
                    String listenerClass = def.getListenerClass();
                    Loader classLoader = null;
                    //host对应的loader就是listener的loader
                    classLoader = this.getLoader();
                    // Instantiate a new instance of this filter and return it
                    Class<?> clazz = classLoader.getClassLoader().loadClass(listenerClass);
                    listener = (ContainerListener) clazz.newInstance();
                    addContainerListener(listener);
                } catch (Throwable t) {
                    log.error(ExceptionUtils.getStackTrace(t));
                }
            }
        }
    }
}
