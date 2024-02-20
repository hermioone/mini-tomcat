package org.hermione.minit;

import javax.servlet.http.HttpSession;

public interface Session {
    String SESSION_CREATED_EVENT = "createSession";
    String SESSION_DESTROYED_EVENT = "destroySession";

    long getCreationTime();

    void setCreationTime(long time);

    String getId();

    void setId(String id);

    String getInfo();

    long getLastAccessedTime();

    int getMaxInactiveInterval();

    void setMaxInactiveInterval(int interval);

    void setNew(boolean isNew);

    HttpSession getSession();

    void setValid(boolean isValid);

    boolean isValid();

    void access();

    void expire();

    void recycle();
}
