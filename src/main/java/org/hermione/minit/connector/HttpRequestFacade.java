package org.hermione.minit.connector;

/**
 * 因为 HttpRequest 和 HttpResponse 会被传入 Servlet 中，如果我们在它们的实现类中封装了很多内部方法，这些内部方法也会暴露给用户
 * 因此使用门面设计模式来解决这个问题
 */

import org.hermione.minit.connector.http.HttpRequestImpl;

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
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class HttpRequestFacade implements HttpServletRequest {
    private final HttpServletRequest request;
    public HttpRequestFacade(HttpRequestImpl request) {
        this.request = request;
    }
    /* implementation of the HttpServletRequest*/
    public Object getAttribute(String name) {
        return request.getAttribute(name);
    }
    public Enumeration<String> getAttributeNames() {
        return request.getAttributeNames();
    }
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public String getAuthType() {
        return null;
    }

    public Cookie[] getCookies() {
        return request.getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    public Enumeration<String> getHeaderNames() {
        return request.getHeaderNames();
    }
    public String getHeader(String name) {
        return request.getHeader(name);
    }
    public Enumeration<String> getHeaders(String name) {
        return request.getHeaders(name);
    }
    public ServletInputStream getInputStream() throws IOException {
        return request.getInputStream();
    }
    public int getIntHeader(String name) {
        return request.getIntHeader(name);
    }
    public String getMethod() {
        return request.getMethod();
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
    public String getContextPath() {
        return null;
    }

    public String getParameter(String name) {
        return request.getParameter(name);
    }
    public Map<String, String[]> getParameterMap() {
        return request.getParameterMap();
    }

    @Override
    public String getProtocol() {
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

    public Enumeration<String> getParameterNames() {
        return request.getParameterNames();
    }
    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    public String getRequestURI() {
        return request.getRequestURI();
    }
    public StringBuffer getRequestURL() {
        return request.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return null;
    }

    public HttpSession getSession() {
        return request.getSession();
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
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
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    public HttpSession getSession(boolean create) {
        return request.getSession(create);
    }
    public void removeAttribute(String attribute) {
        request.removeAttribute(attribute);
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
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
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
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    public void setAttribute(String key, Object value) {
        request.setAttribute(key, value);
    }
    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        request.setCharacterEncoding(encoding);
    }
}
