package org.hermione.minit.startup;

import lombok.extern.slf4j.Slf4j;
import org.hermione.minit.Loader;
import org.hermione.minit.Logger;
import org.hermione.minit.connector.http.HttpConnector;
import org.hermione.minit.core.CommonLoader;
import org.hermione.minit.core.ContainerListenerDef;
import org.hermione.minit.core.FilterDef;
import org.hermione.minit.core.FilterMap;
import org.hermione.minit.core.StandardContext;
import org.hermione.minit.core.StandardHost;
import org.hermione.minit.core.WebappClassLoader;
import org.hermione.minit.logger.FileLogger;
import org.hermione.minit.logger.SystemOutLogger;

import java.io.File;

@Slf4j
public class Bootstrap {

    public static final String MINIT_HOME = System.getProperty("user.dir");
    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webapps";

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        System.setProperty("minit.home", MINIT_HOME);
        System.setProperty("minit.base", WEB_ROOT);

        HttpConnector connector = new HttpConnector();
        StandardHost container = new StandardHost();
        connector.setContainer(container);
        Loader commonLoader = new CommonLoader();
        container.setLoader(commonLoader);
        container.setConnector(connector);
        container.start();
        connector.start();

        log.warn("Server start within {}ms", (System.currentTimeMillis() - start));
    }
}