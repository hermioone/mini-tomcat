package org.hermione.minit.session;

import org.hermione.minit.Session;
import org.hermione.minit.SessionEvent;
import org.hermione.minit.SessionListener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet 规范就规定了使用 Session 记住用户状态
 * Session 由服务器创建，存在 SessionID，依靠 URL 或者是 Cookie 传送，把名称定义成 jsessionid。今后浏览器与服务器之间的数据交换都带上这个 jsessionid. 然后程序可以根据 jsessionid 拿到这个 Session，把一些状态数据存储在 Session 里。
 */
public class StandardSession implements HttpSession, Session {
    private String sessionid;
    private long creationTime;
    private boolean valid;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.sessionid;
    }

    @Override
    public long getLastAccessedTime() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
    }

    @Override
    public void setNew(boolean isNew) {

    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public int getMaxInactiveInterval() {
        return 0;
    }

    @Override
    @Deprecated
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    @Override
    public String[] getValueNames() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    @Override
    public void putValue(String name, Object value) {
        this.attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public void removeValue(String name) {
    }

    @Override
    public void invalidate() {
        this.valid = false;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    public void setValid(boolean b) {
        this.valid = b;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void access() {

    }

    @Override
    public void expire() {

    }

    @Override
    public void recycle() {

    }

    public void setCreationTime(long currentTimeMillis) {
        this.creationTime = currentTimeMillis;
    }

    public void setId(String sessionId) {
        this.sessionid = sessionId;
        fireSessionEvent(Session.SESSION_CREATED_EVENT, null);
    }

    @Override
    public String getInfo() {
        return null;
    }


    /* ----------------------------- 新增了对 SessionListener 的处理 ----------------------------- */

    private final transient ArrayList<SessionListener> listeners = new ArrayList<>();
    public void addSessionListener(SessionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    public void removeSessionListener(SessionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    public void fireSessionEvent(String type, Object data) {
        if (listeners.isEmpty())
            return;
        SessionEvent event = new SessionEvent(this, type, data);
        SessionListener[] list = new SessionListener[0];
        synchronized (listeners) {
            list = listeners.toArray(list);
        }
        for (SessionListener sessionListener : list) {
            sessionListener.sessionEvent(event);
        }
    }

}
