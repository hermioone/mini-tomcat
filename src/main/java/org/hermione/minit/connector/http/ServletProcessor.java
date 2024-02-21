package org.hermione.minit.connector.http;

import lombok.extern.slf4j.Slf4j;
import org.hermione.minit.Request;
import org.hermione.minit.Response;

import javax.servlet.ServletException;
import java.io.IOException;

@Slf4j
public class ServletProcessor {
    private final HttpConnector connector;
    public ServletProcessor(HttpConnector connector) {
        this.connector = connector;
    }
    public void process(Request request, Response response) throws IOException, ServletException {
        this.connector.getContainer().invoke(request, response);
    }
}
