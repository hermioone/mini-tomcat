package org.hermione.minit.startup;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hermione.minit.Loader;
import org.hermione.minit.connector.http.HttpConnector;
import org.hermione.minit.loader.CommonLoader;
import org.hermione.minit.core.StandardHost;

import java.io.File;

@Slf4j
public class Bootstrap {

    public static final String MINIT_HOME = System.getProperty("user.dir");
    public static final String WEB_ROOT = System.getProperty("user.dir");

    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        String file = MINIT_HOME + File.separator + "conf" + File.separator + "server.xml";
        SAXReader reader = new SAXReader();
        Document document;

        String appRoot = WEB_ROOT;
        int port = DEFAULT_PORT;
        try {
            document = reader.read(file);
            Element root = document.getRootElement();
            Element connectorelement = root.element("Connector");
            Attribute portattribute = connectorelement.attribute("port");
            port = Integer.parseInt(portattribute.getText());
            Element hostelement = root.element("Host");
            Attribute appbaseattribute = hostelement.attribute("appBase");
            appRoot = WEB_ROOT + File.separator + appbaseattribute.getText();
        } catch (Exception ignored) {
        }

        System.setProperty("minit.home", MINIT_HOME);
        System.setProperty("minit.base", appRoot);

        HttpConnector connector = new HttpConnector(port);
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