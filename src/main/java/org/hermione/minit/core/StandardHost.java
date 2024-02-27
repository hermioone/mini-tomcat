package org.hermione.minit.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.ContainerEvent;
import org.hermione.minit.ContainerListener;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.connector.http.HttpConnector;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
@Slf4j
public class StandardHost extends ContainerBase {
    @Getter
    @Setter
    HttpConnector connector = null;
    //host中用一个map存储了所管理的 context，一个 context 代表了一个独立的web应用
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
            //创建新的context，有自己独立的根目录和类加载器
            WebappClassLoader loader = new WebappClassLoader();
            context = new StandardContext();
            context.setDocBase(name);
            context.setConnector(connector);
            context.setLoader(loader);
            loader.start();
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
                    WebappClassLoader classLoader = null;
                    //host对应的loader就是listener的loader
                    classLoader = this.getLoader();
                    ClassLoader oldCtxClassLoader =
                            Thread.currentThread().getContextClassLoader();
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
