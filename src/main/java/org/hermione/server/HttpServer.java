package org.hermione.server;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class HttpServer {
    public static final String WEB_ROOT =
            System.getProperty("user.dir") + File.separator + "webroot";


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        HttpConnector connector = new HttpConnector();
        connector.start();
        log.info("Server starts within {}ms", System.currentTimeMillis() - start);
    }
}