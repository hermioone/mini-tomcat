package org.hermione.server;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class HttpServer {
    public static final String WEB_ROOT =
            System.getProperty("user.dir") + File.separator + "webroot";
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        //创建connector和container
        HttpConnector connector = new HttpConnector();
        ServletContainer container = new ServletContainer();
        //connector和container互相指引
        connector.setContainer(container);
        container.setConnector(connector);
        connector.start();
        log.warn("Server start within {}ms", (System.currentTimeMillis() - start));
    }
}