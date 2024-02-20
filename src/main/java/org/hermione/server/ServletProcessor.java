package org.hermione.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ServletProcessor {
    private final HttpConnector connector;
    public ServletProcessor(HttpConnector connector) {
        this.connector = connector;
    }
    public void process(HttpRequest request, HttpResponse response) throws IOException, ServletException {
        this.connector.getContainer().invoke(request, response);
    }
}
