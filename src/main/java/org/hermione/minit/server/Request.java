package org.hermione.minit.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Deprecated
public class Request implements ServletRequest {
    private final InputStream input;
    private String uri;
    //以输入流作为Request的接收参数
    public Request(InputStream input) {
        this.input = input;
    }
    //简单的parser，假定从输入流中一次性获取全部字节，存放到2K缓存中
    public void parse() {
        StringBuffer request = new StringBuffer(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = input.read(buffer);
        }
        catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            i = -1;
        }
        for (int j=0; j<i; j++) {
            request.append((char) buffer[j]);
        }
        //从输入的字符串中解析URI
        uri = parseUri(request.toString());
    }
    //根据协议格式，以空格为界，截取中间的一段，即为URI
    private String parseUri(String requestString) {
        int index1, index2;
        index1 = requestString.indexOf(' ');
        if (index1 != -1) {
            index2 = requestString.indexOf(' ', index1 + 1);
            if (index2 > index1)
                return requestString.substring(index1 + 1, index2);
        }
        return null;
    }
    public String getUri() {
        return uri;
    }
    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }
    @Override
    public Object getAttribute(String arg0) {
        return null;
    }
    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }
    @Override
    public String getCharacterEncoding() {
        return null;
    }
    @Override
    public int getContentLength() {
        return 0;
    }
    @Override
    public long getContentLengthLong() {
        return 0;
    }
    @Override
    public String getContentType() {
        return null;
    }
    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }
    @Override
    public String getLocalAddr() {
        return null;
    }
    @Override
    public String getLocalName() {
        return null;
    }
    @Override
    public int getLocalPort() {
        return 0;
    }
    @Override
    public Locale getLocale() {
        return null;
    }
    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }
    @Override
    public String getParameter(String arg0) {
        return null;
    }
    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }
    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }
    @Override
    public String[] getParameterValues(String arg0) {
        return null;
    }
    @Override
    public String getProtocol() {
        return null;
    }
    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }
    @Override
    public String getRealPath(String arg0) {
        return null;
    }
    @Override
    public String getRemoteAddr() {
        return null;
    }
    @Override
    public String getRemoteHost() {
        return null;
    }
    @Override
    public int getRemotePort() {
        return 0;
    }
    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
    }
    @Override
    public String getScheme() {
        return null;
    }
    @Override
    public String getServerName() {
        return null;
    }
    @Override
    public int getServerPort() {
        return 0;
    }
    @Override
    public ServletContext getServletContext() {
        return null;
    }
    @Override
    public boolean isAsyncStarted() {
        return false;
    }
    @Override
    public boolean isAsyncSupported() {
        return false;
    }
    @Override
    public boolean isSecure() {
        return false;
    }
    @Override
    public void removeAttribute(String arg0) {
    }
    @Override
    public void setAttribute(String arg0, Object arg1) {
    }
    @Override
    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
    }
    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }
    @Override
    public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
        return null;
    }
}
