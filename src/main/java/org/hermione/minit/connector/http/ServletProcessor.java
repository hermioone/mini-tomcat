package org.hermione.minit.connector.http;

import lombok.extern.slf4j.Slf4j;
import org.hermione.minit.connector.http.HttpConnector;
import org.hermione.minit.connector.http.HttpRequestImpl;
import org.hermione.minit.connector.http.HttpResponseImpl;

import javax.servlet.ServletException;
import java.io.IOException;

@Slf4j
public class ServletProcessor {
    private final HttpConnector connector;
    public ServletProcessor(HttpConnector connector) {
        this.connector = connector;
    }
    public void process(HttpRequestImpl request, HttpResponseImpl response) throws IOException, ServletException {
        this.connector.getContainer().invoke(request, response);
    }
}
