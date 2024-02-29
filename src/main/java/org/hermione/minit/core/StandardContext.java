package org.hermione.minit.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hermione.minit.ContainerEvent;
import org.hermione.minit.ContainerListener;
import org.hermione.minit.Context;
import org.hermione.minit.Loader;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.Wrapper;
import org.hermione.minit.connector.http.HttpConnector;
import org.hermione.minit.valves.AuthorityCheckValve;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
@Slf4j
//Servlet容器，原来的 ServletContainer
public class StandardContext extends ContainerBase implements Context {
    @Getter
    HttpConnector connector = null;
    //包含servlet类和实例的map
    Map<String, String> servletClsMap = new ConcurrentHashMap<>(); //servletName - ServletClassName
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();//servletName - servlet

    public StandardContext() {
        super("StandardContext");
        Pipeline pipeline = getPipeline();
        pipeline.addValve(new AuthorityCheckValve(pipeline));
        pipeline.setBasic(new StandardContextValve(pipeline));
        log("Container created.");
    }

    public void initWrapper(String name, String serverClass) {
        StandardWrapper servletWrapper = servletInstanceMap.get(name);
        if (servletWrapper == null) {
            servletWrapper = new StandardWrapper(serverClass, this, this.getLoader());
            this.servletClsMap.put(name, serverClass);
            this.servletInstanceMap.put(name, servletWrapper);
        }
    }

    public Wrapper initWrapper(String name) {
        return Objects.requireNonNull(servletInstanceMap.get(name), "Invalid context");
    }

    public String getInfo() {
        return null;
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
        this.docbase = docBase;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public ServletContext getServletContext() {
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


    //下面的属性记录了filter的配置
    private final Map<String,ApplicationFilterConfig> filterConfigs = new ConcurrentHashMap<>();
    private final Map<String,FilterDef> filterDefs = new ConcurrentHashMap<>();
    private final List<FilterMap> filterMaps = new ArrayList<>();

    public void addFilterDef(FilterDef filterDef) {
        filterDefs.put(filterDef.getFilterName(), filterDef);
    }
    public void addFilterMap(FilterMap filterMap) {
        // 验证所建议的过滤器映射
        String filterName = filterMap.getFilterName();
        String servletName = filterMap.getServletName();
        String urlPattern = filterMap.getUrlPattern();
        if (findFilterDef(filterName) == null)
            throw new IllegalArgumentException("standardContext.filterMap.name"+filterName);
        if ((servletName == null) && (urlPattern == null))
            throw new IllegalArgumentException("standardContext.filterMap.either");
        if ((servletName != null) && (urlPattern != null))
            throw new IllegalArgumentException("standardContext.filterMap.either");
        // 因为过滤器模式是2.3中的新功能，所以不需要调整
        // 对于2.2版本的向后兼容性
        if ((urlPattern != null) && !validateURLPattern(urlPattern)) {
            throw new IllegalArgumentException("standardContext.filterMap.pattern"+urlPattern);
        }
        // 将这个过滤器映射添加到我们已注册的集合中
        synchronized (filterMaps) {
            filterMaps.add(filterMap);
        }
    }
    public FilterDef findFilterDef(String filterName) {
        return filterDefs.get(filterName);
    }
    public FilterDef[] findFilterDefs() {
        synchronized (filterDefs) {
            FilterDef[] results = new FilterDef[filterDefs.size()];
            return filterDefs.values().toArray(results);
        }
    }
    public FilterMap[] findFilterMaps() {
        return filterMaps.toArray(new FilterMap[0]);
    }
    public void removeFilterDef(FilterDef filterDef) {
        filterDefs.remove(filterDef.getFilterName());
    }

    public void removeFilterMap(FilterMap filterMap) {
        synchronized (filterMaps) {
            filterMaps.remove(filterMap);
        }
    }
    //对配置好的所有filter名字，创建实例，存储在filterConfigs中，可以生效了
    public void filterStart() {
        log.info("Filter Start..........");
        // 为每个定义的过滤器实例化并记录一个FilterConfig
        boolean ok = true;
        synchronized (filterConfigs) {
            filterConfigs.clear();
            for (String name : filterDefs.keySet()) {
                ApplicationFilterConfig filterConfig = null;
                try {
                    filterConfig = new ApplicationFilterConfig(this, filterDefs.get(name));
                    filterConfigs.put(name, filterConfig);
                } catch (Throwable t) {
                    ok = false;
                }
            }
        }
    }
    public FilterConfig findFilterConfig(String name) {
        return (filterConfigs.get(name));
    }
    private boolean validateURLPattern(String urlPattern) {
        if (urlPattern == null)
            return (false);
        if (urlPattern.startsWith("*.")) {
            return urlPattern.indexOf('/') < 0;
        }
        return urlPattern.startsWith("/");
    }


    /* ----------------------------- 新增了对 ContainerListener 的处理 ----------------------------- */

    private final ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();
    private final ArrayList<ContainerListener> listeners = new ArrayList<>();

    public void start(){
        // 触发一个容器启动事件
        fireContainerEvent("Container Started",this);

        // 扫描并解析 web.xml
        //扫描 web.xml
        String file = System.getProperty("minit.base") + File.separator +
                this.docbase + File.separator + "WEB-INF" + File.separator + "web.xml";
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(file);
            Element root = document.getRootElement();
            //listeners
            List<Element> listeners = root.elements("listener");
            for (Element listener : listeners) {
                Element listenerclass = listener.element("listener-class");
                String listenerclassname = listenerclass.getText();
                log.info("listenerclassname: {}", listenerclassname);
                //加载 listeners
                ContainerListenerDef listenerDef = new ContainerListenerDef();
                listenerDef.setListenerName(listenerclassname);
                listenerDef.setListenerClass(listenerclassname);
                addListenerDef(listenerDef);
            }
            listenerStart();
            //filters
            List<Element> filters = root.elements("filter");
            for (Element filter : filters) {
                Element filetername = filter.element("filter-name");
                String fileternamestr = filetername.getText();
                Element fileterclass = filter.element("filter-class");
                String fileterclassstr = fileterclass.getText();
                log.info("filter, {}, {}",fileternamestr, fileterclassstr);
                //加载 filters
                FilterDef filterDef = new FilterDef();
                filterDef.setFilterName(fileternamestr);
                filterDef.setFilterClass(fileterclassstr);
                addFilterDef(filterDef);
            }
            //filter 映射
            List<Element> filtermaps = root.elements("filter-mapping");
            for (Element filtermap : filtermaps) {
                Element filetername = filtermap.element("filter-name");
                String fileternamestr = filetername.getText();
                Element urlpattern = filtermap.element("url-pattern");
                String urlpatternstr = urlpattern.getText();
                log.info("filter mapping, {}, {}", fileternamestr, urlpatternstr);
                FilterMap filterMap = new FilterMap();
                filterMap.setFilterName(fileternamestr);
                filterMap.setUrlPattern(urlpatternstr);
                addFilterMap(filterMap);
            }
            filterStart();
            //servlet
            List<Element> servlets = root.elements("servlet");
            for (Element servlet : servlets) {
                Element servletname = servlet.element("servlet-name");
                String servletnamestr = servletname.getText();
                Element servletclass = servlet.element("servlet-class");
                String servletclassstr = servletclass.getText();
                Element loadonstartup = servlet.element("load-on-startup");
                String loadonstartupstr = null;
                if (loadonstartup != null) {
                    loadonstartupstr = loadonstartup.getText();
                }
                log.info("servlet, {}, {}", servletnamestr, servletclassstr);
                this.servletClsMap.put(servletnamestr, servletclassstr);
                if (loadonstartupstr != null) {
                    initWrapper(servletnamestr, servletclassstr);
                }
            }
        } catch (DocumentException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        log.info("Context started.........");
    }
    public void addContainerListener(ContainerListener listener) {
        // 添加一个新的容器监听器到监听器列表，并确保线程安全
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    public void removeContainerListener(ContainerListener listener) {
        // 移除指定的容器监听器，并确保线程安全
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    public void fireContainerEvent(String type, Object data) {
        // 检查是否已经有监听器，如果没有则直接返回
        if (listeners.isEmpty())
            return;
        ContainerEvent event = new ContainerEvent(this, type, data);
        ContainerListener[] list = new ContainerListener[0];
        synchronized (listeners) {
            list = listeners.toArray(list);
        }
        // 遍历所有监听器并触发事件
        for (ContainerListener containerListener : list) {
            containerListener.containerEvent(event);
        }
    }
    public void addListenerDef(ContainerListenerDef listenererDef) {
        synchronized (listenerDefs) {
            listenerDefs.add(listenererDef);
        }
    }

    public void listenerStart() {
        log.info("Listener Start..........");
        synchronized (listeners) {
            listeners.clear();
            for (ContainerListenerDef def : listenerDefs) {
                ContainerListener listener = null;
                try {
                    // 确定我们将要使用的类加载器
                    String listenerClass = def.getListenerClass();
                    Loader loader1 = this.getLoader();
                    // 创建这个过滤器的新实例并返回它
                    Class<?> clazz = loader1.getClassLoader().loadClass(listenerClass);
                    listener = (ContainerListener) clazz.newInstance();
                    addContainerListener(listener);
                } catch (Throwable t) {
                    log.error(ExceptionUtils.getStackTrace(t));
                }
            }
        }
    }
}

