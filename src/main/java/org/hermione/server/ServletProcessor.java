package org.hermione.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import javax.servlet.Servlet;
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
        //response默认为UTF-8编码
        response.setCharacterEncoding("UTF-8");
        Class<?> servletClass = null;
        try {
            servletClass = HttpConnector.loader.loadClass(servletName);
        }
        catch (ClassNotFoundException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        //创建servlet新实例，调用service()
        Servlet servlet = null;
        try {
            servlet = (Servlet) servletClass.newInstance();
            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
            log.info("Call servlet: {}", servletName);
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
