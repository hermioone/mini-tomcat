package org.hermione.minit.connector.http;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.connector.http.DefaultHeaders;
import org.hermione.minit.connector.http.HttpRequestImpl;
import org.hermione.minit.util.CookieTools;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HttpResponseImpl implements HttpServletResponse {
    HttpRequestImpl request;
    @Getter
    OutputStream output;
    PrintWriter writer;
    String contentType = null;
    /**
     * 对于动态内容，tomcat 中无法确定 content-length，所以就不再使用 content-length，而是使用 Transfer-Encoding: chunked 分块传输
     */
    @Getter
    long contentLength = -1;
    String charset = null;
    String characterEncoding = "UTF-8";
    String protocol = "HTTP/1.1";
    //headers是一个保存头信息的map
    Map<String, String> headers = new ConcurrentHashMap<>();
    //默认返回OK
    String message = getStatusMessage(HttpServletResponse.SC_OK);
    int status = HttpServletResponse.SC_OK;

    public HttpResponseImpl(OutputStream output) {
        this.output = output;
    }

    public HttpResponseImpl() {
    }

    public void setStream(OutputStream output) {
        this.output = output;
    }

    //提供这个方法完成输出
    public void finishResponse() {
        try {
            this.getWriter().flush();
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void setRequest(HttpRequestImpl request) {
        this.request = request;
    }

    //状态码以及消息文本，没有考虑国际化
    protected String getStatusMessage(int status) {
        switch (status) {
            case SC_OK:
                return ("OK");
            case SC_ACCEPTED:
                return ("Accepted");
            case SC_BAD_GATEWAY:
                return ("Bad Gateway");
            case SC_BAD_REQUEST:
                return ("Bad Request");
            case SC_CONTINUE:
                return ("Continue");
            case SC_FORBIDDEN:
                return ("Forbidden");
            case SC_INTERNAL_SERVER_ERROR:
                return ("Internal Server Error");
            case SC_METHOD_NOT_ALLOWED:
                return ("Method Not Allowed");
            case SC_NOT_FOUND:
                return ("Not Found");
            case SC_NOT_IMPLEMENTED:
                return ("Not Implemented");
            case SC_REQUEST_URI_TOO_LONG:
                return ("Request URI Too Long");
            case SC_SERVICE_UNAVAILABLE:
                return ("Service Unavailable");
            case SC_UNAUTHORIZED:
                return ("Unauthorized");
            default:
                return ("HTTP Response Status " + status);
        }
    }

    public String getProtocol() {
        return "HTTP/1.1";
    }

    final ArrayList<Cookie> cookies = new ArrayList<>();

    public void sendHeaders() throws IOException {
        PrintWriter outputWriter = getWriter();
        outputWriter.print(this.getProtocol());
        outputWriter.print(" ");
        outputWriter.print(status);
        if (message != null) {
            outputWriter.print(" ");
            outputWriter.print(message);
        }
        outputWriter.print("\r\n");
        if (getContentType() != null) {
            outputWriter.print("Content-Type: " + getContentType() + "\r\n");
        }
        if (getContentLength() >= 0) {
            outputWriter.print("Content-Length: " + getContentLength() + "\r\n");
        }
        for (String name : headers.keySet()) {
            String value = headers.get(name);
            outputWriter.print(name);
            outputWriter.print(": ");
            outputWriter.print(value);
            outputWriter.print("\r\n");
        }
        HttpSession session = this.request.getSession(false);
        if (session != null) {
            Cookie cookie = new Cookie(DefaultHeaders.JSESSIONID_NAME, session.getId());
            cookie.setMaxAge(-1);
            addCookie(cookie);
        }
        synchronized (cookies) {
            for (Cookie cookie : cookies) {
                outputWriter.print(CookieTools.getCookieHeaderName(cookie));
                outputWriter.print(": ");
                StringBuffer sbValue = new StringBuffer();
                CookieTools.getCookieHeaderValue(cookie, sbValue);
                System.out.println("set cookie jsessionid string : " + sbValue);
                outputWriter.print(sbValue);
                outputWriter.print("\r\n");
            }
        }
        outputWriter.print("\r\n");
        outputWriter.flush();
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(output, getCharacterEncoding()), true);
        return writer;
    }

    @Override
    public void setCharacterEncoding(String arg0) {
        this.characterEncoding = arg0;
    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String arg0) {
        this.contentType = arg0;
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_LENGTH_NAME)) {
            setContentLength(Integer.parseInt(value));
        }
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_TYPE_NAME)) {
            setContentType(value);
        }
    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc) {

    }

    @Override
    public void setStatus(int sc, String sm) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public void addCookie(Cookie cookie) {
        synchronized (cookies) {
            cookies.add(cookie);
        }
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

    }

    @Override
    public void sendError(int sc) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_LENGTH_NAME)) {
            setContentLength(Integer.parseInt(value));
        }
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_TYPE_NAME)) {
            setContentType(value);
        }
    }
}
