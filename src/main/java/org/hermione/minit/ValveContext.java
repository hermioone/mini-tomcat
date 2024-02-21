package org.hermione.minit;

import java.io.IOException;
import javax.servlet.ServletException;

/**
 * ValveContext 接口负责调用下一个 Valve，这样就会形成一系列对 Valve 的调用。
 */
public interface ValveContext {
    public String getInfo();

    public void invokeNext(Request request, Response response) throws IOException, ServletException;
}
