package org.hermione.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import javax.servlet.Servlet;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ServletProcessor {
    //返回串的模板，实际返回时替换变量
    private final static String OKMessage = "HTTP/1.1 ${StatusCode} ${StatusName}\r\n"+
            "Content-Type: ${ContentType}\r\n"+
            "Server: minit\r\n"+
            "Date: ${ZonedDateTime}\r\n"+
            "\r\n";
    public void process(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader = null;
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            //这个URLClassloader的工作目录设置在HttpServer.WEB_ROOT
            File classPath = new File(HttpServer.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString() ;
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        }
        catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        //response默认为UTF-8编码
        response.setCharacterEncoding("UTF-8");
        Class<?> servletClass = null;
        try {
            servletClass = loader.loadClass(servletName);
        }
        catch (ClassNotFoundException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        //回写头信息
        try {
            response.sendHeaders();
        } catch (IOException e1) {
            log.error(ExceptionUtils.getStackTrace(e1));
        }
        //创建servlet新实例，调用service()
        Servlet servlet = null;
        try {
            servlet = (Servlet) servletClass.newInstance();
            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
            servlet.service(requestFacade, responseFacade);
        }
        catch (Throwable e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
    //生成返回头，根据协议格式替换变量
    private String composeResponseHead() {
        Map<String,Object> valuesMap = new HashMap<>();
        valuesMap.put("StatusCode","200");
        valuesMap.put("StatusName","OK");
        valuesMap.put("ContentType","text/html;charset=UTF-8");
        valuesMap.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        return sub.replace(OKMessage);
    }
}
