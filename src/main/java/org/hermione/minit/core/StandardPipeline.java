package org.hermione.minit.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.Container;
import org.hermione.minit.Pipeline;
import org.hermione.minit.Request;
import org.hermione.minit.Response;
import org.hermione.minit.Valve;
import org.hermione.minit.ValveContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StandardPipeline implements Pipeline {


    public StandardPipeline(Container container) {
        this.container = container;
    }

    protected Container container;
    protected Valve basic = null;
    protected int debug = 0;
    protected String info = "com.minit.core.StandardPipeline/0.1";

    protected final List<Valve> valves = new ArrayList<>();

    public String getInfo() {
        return (this.info);
    }

    public Container getContainer() {
        return (this.container);
    }

    public Valve getBasic() {
        return (this.basic);
    }

    public void setBasic(Valve valve) {
        // Change components if necessary
        Valve oldBasic = this.basic;
        if (oldBasic == valve)
            return;

        // Start the new component if necessary
        if (valve == null)
            return;
        this.basic = valve;

    }

    @Override
    public synchronized void addValve(Valve valve) {
        // Add this Valve to the set associated with this Pipeline
        synchronized (valves) {
            valves.add(valve);
        }

    }

    @Override
    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        // Invoke the first Valve in this pipeline for this request
        (new StandardPipelineValveContext()).invokeNext(request, response);

    }

    @Override
    public void removeValve(Valve valve) {
        synchronized (valves) {
            valves.remove(valve);
        }

    }

    public void log(String message) {
        log.info("StandardPipeline[" + container.getName() + "]: " + message);
    }

    protected void log(String message, Throwable throwable) {
        log.error("StandardPipeline[" + container.getName() + "]: " + message, ExceptionUtils.getStackTrace(throwable));
    }

    protected class StandardPipelineValveContext implements ValveContext {
        protected int stage = 0;

        public String getInfo() {
            return info;
        }

        public void invokeNext(Request request, Response response)
                throws IOException, ServletException {

            int subscript = stage;
            stage = stage + 1;

            // Invoke the requested Valve for the current request thread
            if (subscript < valves.size()) {
                log("----------------------- " + valves.get(subscript).getName() + " invoke()");
                valves.get(subscript).invoke(request, response, this);
            } else if ((subscript == valves.size()) && (basic != null)) {
                log("----------------------- " + basic.getName() + " invoke()");
                basic.invoke(request, response, this);
            } else {
                throw new ServletException("standardPipeline.noValve");
            }

        }
    }
}
