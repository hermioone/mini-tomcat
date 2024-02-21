package org.hermione.minit.core;

import lombok.extern.slf4j.Slf4j;
import org.hermione.minit.Container;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.ValveContext;
import org.hermione.minit.connector.HttpRequestFacade;
import org.hermione.minit.connector.HttpResponseFacade;
import org.hermione.minit.connector.http.HttpRequestImpl;
import org.hermione.minit.connector.http.HttpResponseImpl;
import org.hermione.minit.valves.ValveBase;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class StandardWrapperValve extends ValveBase {

    public StandardWrapperValve(Pipeline pipeline) {
        super(pipeline, "StandardWrapperValve");
    }

    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        // TODO Auto-generated method stub
        HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
        HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);
        Servlet instance = ((StandardWrapper)getPipeline().getContainer()).getServlet();
        if (instance != null) {
            log.info("Call Servlet");
            instance.service(requestFacade, responseFacade);
        }
    }
}
