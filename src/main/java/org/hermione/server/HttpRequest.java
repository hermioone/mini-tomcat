package org.hermione.server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("IfCanBeSwitch")
@Slf4j
public class HttpRequest implements HttpServletRequest {

    private HttpResponse response;
    private InputStream input;
    private SocketInputStream sis;
    @Getter
    private String uri;
    private String queryString;
    InetAddress address;
    int port;
    private boolean parsed = false;
    protected HashMap<String, String> headers = new HashMap<>();
    protected Map<String, String[]> parameters = new ConcurrentHashMap<>();
    HttpRequestLine requestLine = new HttpRequestLine();

    // Cookie 也是放在 Header 里的，固定格式是 Cookie: userName=xxxx;password=pwd;
    Cookie[] cookies;
    HttpSession session;
    String sessionid;
    SessionFacade sessionFacade;

    public HttpRequest(InputStream input) {
        this.input = input;
        this.sis = new SocketInputStream(this.input, 2048);
    }

    public HttpRequest() {
    }

    public void setStream(InputStream input) {
        this.input = input;
        this.sis = new SocketInputStream(this.input, 2048);
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public void parse(Socket socket) {
        try {
            parseConnection(socket);
            this.sis.readRequestLine(requestLine);
            parseRequestLine();
            parseHeaders();
        } catch (IOException | ServletException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }

    }

    private void parseRequestLine() {
        int question = requestLine.indexOf("?");
        if (question >= 0) {
            queryString = new String(requestLine.uri, question + 1, requestLine.uriEnd - question - 1);
            uri = new String(requestLine.uri, 0, question);
            //处理参数串中带有jsessionid的情况
            String tmp = ";" + DefaultHeaders.JSESSIONID_NAME + "=";
            int semicolon = uri.indexOf(tmp);
            if (semicolon >= 0) {
                sessionid = uri.substring(semicolon + tmp.length());
                uri = uri.substring(0, semicolon);
            }
        } else {
            queryString = null;
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
            String tmp = ";" + DefaultHeaders.JSESSIONID_NAME + "=";
            int semicolon = uri.indexOf(tmp);
            if (semicolon >= 0) {
                sessionid = uri.substring(semicolon + tmp.length());
                uri = uri.substring(0, semicolon);
            }
        }
    }

    private void parseConnection(Socket socket) {
        address = socket.getInetAddress();
        port = socket.getPort();
    }

    private void parseHeaders() throws IOException, ServletException {
        while (true) {
            HttpHeader header = new HttpHeader();
            sis.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException("httpProcessor.parseHeaders.colon");
                }
            }
            String name = new String(header.name, 0, header.nameEnd);
            String value = new String(header.value, 0, header.valueEnd);
            name = name.toLowerCase();
            // Set the corresponding request headers
            if (name.equals(DefaultHeaders.ACCEPT_LANGUAGE_NAME)) {
                headers.put(name, value);
            } else if (name.equals(DefaultHeaders.CONTENT_LENGTH_NAME)) {
                headers.put(name, value);
            } else if (name.equals(DefaultHeaders.CONTENT_TYPE_NAME)) {
                headers.put(name, value);
            } else if (name.equals(DefaultHeaders.HOST_NAME)) {
                headers.put(name, value);
            } else if (name.equals(DefaultHeaders.CONNECTION_NAME)) {
                headers.put(name, value);
                if (value.equals("close")) {
                    // 如果客户端请求中带了 Connection: close，那么在处理完这个请求后，就在 HttpProcessor 中把 Socket 关掉
                    response.setHeader("Connection", "close");
                }
            } else if (name.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)) {
                headers.put(name, value);
            } else if (name.equals(DefaultHeaders.COOKIE_NAME)) {
                headers.put(name, value);
                this.cookies = parseCookieHeader(value);
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("jsessionid")) {
                        this.sessionid = cookie.getValue();
                    }
                }
            } else {
                headers.put(name, value);
            }
        }
    }

    // 解析Cookie头，格式为: key1=value1;key2=value2
    public Cookie[] parseCookieHeader(String header) {
        if ((header == null) || (header.isEmpty()))
            return (new Cookie[0]);
        ArrayList<Cookie> cookieal = new ArrayList<>();
        while (!header.isEmpty()) {
            int semicolon = header.indexOf(';');
            if (semicolon < 0)
                semicolon = header.length();
            if (semicolon == 0)
                break;

            String token = header.substring(0, semicolon);
            if (semicolon < header.length())
                header = header.substring(semicolon + 1);
            else
                header = "";

            try {
                int equals = token.indexOf('=');
                if (equals > 0) {
                    String name = token.substring(0, equals).trim();
                    String value = token.substring(equals + 1).trim();
                    cookieal.add(new Cookie(name, value));
                }
            } catch (Throwable ignored) {
            }
        }
        return ((Cookie[]) cookieal.toArray(new Cookie[0]));
    }

    protected void parseParameters() {
        String encoding = getCharacterEncoding();
        System.out.println(encoding);
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }
        String qString = getQueryString();
        System.out.println("getQueryString:" + qString);
        if (qString != null) {
            byte[] bytes = new byte[qString.length()];
            try {
                bytes = qString.getBytes(encoding);
                parseParameters(this.parameters, bytes, encoding);
            } catch (UnsupportedEncodingException e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
        }
        String contentType = getContentType();
        if (contentType == null)
            contentType = "";
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }
        if ("POST".equals(getMethod()) && (getContentLength() > 0)
                && "application/x-www-form-urlencoded".equals(contentType)) {
            try {
                int max = getContentLength();
                int len = 0;
                byte[] buf = new byte[getContentLength()];
                ServletInputStream is = getInputStream();
                while (len < max) {
                    int next = is.read(buf, len, max - len);
                    if (next < 0) {
                        break;
                    }
                    len += next;
                }
                is.close();
                if (len < max) {
                    throw new RuntimeException("Content length mismatch");
                }
                parseParameters(this.parameters, buf, encoding);
            } catch (UnsupportedEncodingException ignored) {
            } catch (IOException e) {
                throw new RuntimeException("Content read fail");
            }
        }
    }

    private byte convertHexDigit(byte b) {
        if ((b >= '0') && (b <= '9')) return (byte) (b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte) (b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte) (b - 'A' + 10);
        return 0;
    }

    public void parseParameters(Map<String, String[]> map, byte[] data, String encoding)
            throws UnsupportedEncodingException {
        if (parsed)
            return;
        log.info(Arrays.toString(data));
        if (data != null && data.length > 0) {
            int pos = 0;
            int ix = 0;
            int ox = 0;
            String key = null;
            String value = null;
            while (ix < data.length) {
                byte c = data[ix++];
                switch ((char) c) {
                    case '&':
                        value = new String(data, 0, ox, encoding);
                        if (key != null) {
                            putMapEntry(map, key, value);
                            key = null;
                        }
                        ox = 0;
                        break;
                    case '=':
                        key = new String(data, 0, ox, encoding);
                        ox = 0;
                        break;
                    case '+':
                        data[ox++] = (byte) ' ';
                        break;
                    case '%':
                        data[ox++] = (byte) ((convertHexDigit(data[ix++]) << 4)
                                + convertHexDigit(data[ix++]));
                        break;
                    default:
                        data[ox++] = c;
                }
            }
            //The last value does not end in '&'.  So save it now.
            if (key != null) {
                value = new String(data, 0, ox, encoding);
                putMapEntry(map, key, value);
            }
        }
        parsed = true;
    }

    private static void putMapEntry(Map<String, String[]> map, String name, String value) {
        String[] newValues = null;
        String[] oldValues = (String[]) map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
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
        return headers.get(DefaultHeaders.TRANSFER_ENCODING_NAME);
    }

    @Override
    public int getContentLength() {
        return Integer.parseInt(headers.get(DefaultHeaders.CONTENT_LENGTH_NAME));
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return headers.get(DefaultHeaders.CONTENT_TYPE_NAME);
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.sis;
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
    public String getParameter(String name) {
        parseParameters();
        String[] values = parameters.get(name);
        if (values != null)
            return (values[0]);
        else
            return (null);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return (this.parameters);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return (Collections.enumeration(parameters.keySet()));
    }

    @Override
    public String[] getParameterValues(String name) {
        parseParameters();
        return (parameters.get(name));
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
    public void removeAttribute(String name) {

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

    @Override
    public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
        return false;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    @Override
    public long getDateHeader(String arg0) {
        return 0;
    }

    @Override
    public String getHeader(String arg0) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String arg0) {
        return null;
    }

    @Override
    public int getIntHeader(String arg0) {
        return 0;
    }

    @Override
    public String getMethod() {
        return new String(this.requestLine.method, 0, this.requestLine.methodEnd);
    }

    @Override
    public Part getPart(String arg0) throws IOException, ServletException {
        return null;
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return this.sessionFacade;
    }

    @Override
    // 如果有存在的session，直接返回，如果没有，创建一个新的session
    public HttpSession getSession(boolean create) {
        if (sessionFacade != null)
            return sessionFacade;
        if (sessionid != null) {
            session = HttpConnector.sessions.get(sessionid);
            if (session != null) {
                sessionFacade = new SessionFacade(session);
                return sessionFacade;
            } else {
                session = HttpConnector.createSession();
                log.info("---------- Create new session: {}----------", session.getId());
                sessionFacade = new SessionFacade(session);
                return sessionFacade;
            }
        } else {
            log.info("Create new session.");
            session = HttpConnector.createSession();
            sessionFacade = new SessionFacade(session);
            sessionid = session.getId();
            return sessionFacade;
        }
    }

    public String getSessionId() {
        return this.sessionid;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isUserInRole(String arg0) {
        return false;
    }

    @Override
    public void login(String arg0, String arg1) throws ServletException {
    }

    @Override
    public void logout() throws ServletException {
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) throws IOException, ServletException {
        return null;
    }
}
