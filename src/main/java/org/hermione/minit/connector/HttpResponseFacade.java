package org.hermione.minit.connector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

public class HttpResponseFacade implements HttpServletResponse {
    private final HttpServletResponse response;
    public HttpResponseFacade(HttpServletResponse response) {
        this.response = response;
    }
    public void addDateHeader(String name, long value) {
        response.addDateHeader(name, value);
    }
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
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

    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }
    public String encodeUrl(String url) {
        return response.encodeRedirectURL(url);
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

    public String encodeURL(String url) {
        return response.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    public void flushBuffer() throws IOException {
        response.flushBuffer();
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

    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }
    @Override
    public String getContentType() {
        return null;
    }
    @Override
    public void setCharacterEncoding(String s) {
    }
    public void setContentLength(int length) {
        response.setContentLength(length);
    }

    @Override
    public void setContentLengthLong(long len) {

    }

    public void setContentType(String type) {
        response.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }
    public ServletOutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }
    @Override
    public PrintWriter getWriter() throws IOException {
        return response.getWriter();
    }
    @Override
    public void addCookie(Cookie arg0) {
        response.addCookie(arg0);
    }
    @Override
    public String getHeader(String arg0) {
        return response.getHeader(arg0);
    }
    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }
    @Override
    public Collection<String> getHeaders(String arg0) {
        return response.getHeaders(arg0);
    }
}
