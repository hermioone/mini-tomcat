package org.hermione.minit.startup;

import lombok.extern.slf4j.Slf4j;
import org.hermione.minit.Logger;
import org.hermione.minit.connector.http.HttpConnector;
import org.hermione.minit.core.StandardContext;
import org.hermione.minit.logger.FileLogger;
import org.hermione.minit.logger.SystemOutLogger;

import java.io.File;

@Slf4j
public class Bootstrap {
    public static final String WEB_ROOT =
            System.getProperty("user.dir") + File.separator + "webroot";

    private static int debug = 0;

    public static void main(String[] args) {
        Logger logger = new SystemOutLogger();
        long start = System.currentTimeMillis();
        //创建connector和container
        HttpConnector connector = new HttpConnector();
        StandardContext container = new StandardContext();
        container.setLogger(logger);
        //connector和container互相指引
        connector.setContainer(container);
        container.setConnector(connector);
        connector.start();
        log.warn("Server start within {}ms", (System.currentTimeMillis() - start));
    }
}