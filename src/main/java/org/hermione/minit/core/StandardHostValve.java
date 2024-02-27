package org.hermione.minit.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.ValveContext;
import org.hermione.minit.connector.http.HttpRequestImpl;
import org.hermione.minit.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;

@Slf4j
public class StandardHostValve extends ValveBase {

    public StandardHostValve(Pipeline pipeline, String name) {
        super(pipeline, name);
    }

    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        log.info("StandardHostValve invoke()");
        String docbase = ((HttpRequestImpl) request).getDocbase();
        log.info("StandardHostValve invoke getdocbase : {}", docbase);
        StandardHost host = (StandardHost) getPipeline().getContainer();
        StandardContext servletContext = host.getContext(docbase);
        try {
            servletContext.invoke(request, response);
        } catch (Throwable e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
