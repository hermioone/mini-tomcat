package org.hermione.minit.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.Container;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.ValveContext;
import org.hermione.minit.connector.http.HttpRequestImpl;
import org.hermione.minit.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;

@Slf4j
final class StandardContextValve extends ValveBase {
    private static final String info = "org.apache.catalina.core.StandardContextValve/1.0";

    public StandardContextValve(Pipeline pipeline) {
        super(pipeline, "StandardContextValve");
    }

    public String getInfo() {
        return (info);
    }

    public void invoke(Request request, Response response, ValveContext valveContext)
            throws IOException, ServletException {
        StandardWrapper servletWrapper = null;
        String uri = ((HttpRequestImpl) request).getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        StandardContext context = (StandardContext) getPipeline().getContainer();
        servletWrapper = (StandardWrapper) context.getWrapper(servletName);
        try {
            log.info("Call ServletWrapper");
            servletWrapper.invoke(request, response);
        } catch (Throwable e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
